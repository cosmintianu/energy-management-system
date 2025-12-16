import os
import httpx
import asyncio

GEMINI_API_KEY = os.getenv('GEMINI_API_KEY')
GEMINI_MODEL = os.getenv('GEMINI_MODEL', 'models/gemini-2.5-flash-lite')

async def generate_response(prompt: str) -> str:
    """
    Generates text using the Google Gemini API (v1beta) via REST.
    Requires GEMINI_API_KEY to be set in environment variables.
    """
    if not GEMINI_API_KEY:
        return "Error: GEMINI_API_KEY not found in environment."

    url = f"https://generativelanguage.googleapis.com/v1beta/{GEMINI_MODEL}:generateContent"
    
    # Gemini API expects 'contents' -> 'parts' -> 'text'
    body = {
        "contents": [{
            "parts": [{"text": prompt}]
        }],
        "generationConfig": {
            "temperature": 0.2,
            "maxOutputTokens": 256
        }
    }

    async with httpx.AsyncClient(timeout=30.0) as client:
        try:
            response = await client.post(
                url, 
                json=body, 
                headers={'Content-Type': 'application/json'},
                params={'key': GEMINI_API_KEY}
            )
            response.raise_for_status()
            data = response.json()
            
            # Standard Gemini response path
            return data['candidates'][0]['content']['parts'][0]['text']
            
        except httpx.HTTPStatusError as e:
            return f"API Error ({e.response.status_code}): {e.response.text}"
        except (KeyError, IndexError):
            return "Error: Unexpected response format or empty candidate list."
        except Exception as e:
            return f"Network/Client Error: {str(e)}"

