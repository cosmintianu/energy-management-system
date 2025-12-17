import os
import asyncio
import json
from typing import Dict, Set, Optional

import aio_pika
from fastapi import FastAPI, WebSocket, WebSocketDisconnect

from gemini_client import generate_response

RABBITMQ_URL = os.getenv('RABBITMQ_URL', 'amqp://guest:guest@rabbitmq:5672/')
RABBITMQ_QUEUE = os.getenv('RABBITMQ_QUEUE', 'notif.overconsumption.queue')

app = FastAPI()


class ConnectionManager:
    def __init__(self):
        self.active: Dict[str, Set[WebSocket]] = {}
        self.history: Dict[str, list] = {}
        self.max_history = 200

    async def connect(self, room: str, websocket: WebSocket):
        await websocket.accept()
        conns = self.active.setdefault(room, set())
        conns.add(websocket)
        # send recent history to newly connected client
        # Do not send history for support room so each user sees an empty support view
        if room != 'support':
            hist = self.history.get(room, [])
            for msg in hist:
                try:
                    await websocket.send_text(json.dumps(msg))
                except Exception:
                    pass

    def disconnect(self, room: str, websocket: WebSocket):
        conns = self.active.get(room)
        if conns and websocket in conns:
            conns.remove(websocket)

    async def broadcast(self, room: str, message: dict, exclude: Optional[WebSocket] = None):
        # store message in history
        hist = self.history.setdefault(room, [])
        hist.append(message)
        if len(hist) > self.max_history:
            hist.pop(0)

        conns = list(self.active.get(room, []))
        data = json.dumps(message)
        for ws in conns:
            if exclude is not None and ws is exclude:
                continue
            try:
                await ws.send_text(data)
            except Exception:
                pass


manager = ConnectionManager()


@app.on_event('startup')
async def startup():
    asyncio.create_task(rabbit_consumer())


async def rabbit_consumer():
    try:
        connection = await aio_pika.connect_robust(RABBITMQ_URL)
    except Exception as e:
        print('Failed to connect to RabbitMQ:', e)
        return

    async with connection:
        channel = await connection.channel()
        queue = await channel.declare_queue(RABBITMQ_QUEUE, durable=True)

        async with queue.iterator() as queue_iter:
            async for message in queue_iter:
                async with message.process():
                    body = message.body.decode()
                    try:
                        payload = json.loads(body)
                    except Exception:
                        payload = {'text': body}
                    await manager.broadcast('notifications', {'type': 'overconsumption', 'source': 'rabbitmq', 'payload': payload})


@app.websocket('/ws/{room}')
async def websocket_endpoint(websocket: WebSocket, room: str):
    await manager.connect(room, websocket)
    try:
        while True:
            data = await websocket.receive_text()
            try:
                msg = json.loads(data)
            except Exception:
                msg = {'text': data}

            # Expect clients to supply a `username` field in the message payload
            username = msg.get('username') or 'Anonymous'
            user_text = msg.get('text') or ''

            if room == 'support':
                # broadcast user's message to support room (including username), don't echo back to sender
                await manager.broadcast('support', {'username': username, 'text': user_text}, exclude=websocket)
                # get llm reply
                reply = await generate_response(user_text)
                # broadcast LLM reply as coming from Support
                await manager.broadcast('support', {'username': 'Support', 'text': reply})
            else:
                # broadcast to same room including username, don't echo back to sender
                await manager.broadcast(room, {'username': username, 'text': user_text}, exclude=websocket)

    except WebSocketDisconnect:
        manager.disconnect(room, websocket)


if __name__ == '__main__':
    import uvicorn
    uvicorn.run('main:app', host='0.0.0.0', port=8000)
