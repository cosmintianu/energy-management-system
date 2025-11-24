import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    watch: {
         usePolling: true, // Enable polling for file changes
       },
    proxy: {
      '/auth': {
        target: 'http://localhost', 
        changeOrigin: true
      },
      '/users': {
        target: 'http://localhost',
        changeOrigin: true
      },
      '/devices': {
        target: 'http://localhost',
        changeOrigin: true
      }
    }
  },
  build: {
    outDir: 'dist',
    sourcemap: false,
    // minify: 'terser',
    rollupOptions: {
      output: {
        manualChunks: {
          'react-vendor': ['react', 'react-dom', 'react-router-dom'],
          'axios-vendor': ['axios']
        }
      }
    }
  }
})
