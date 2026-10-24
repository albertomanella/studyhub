import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    // Il browser parla solo con il dev server di Vite, che inoltra /api al
    // backend: frontend e API sono la stessa origine, quindi i cookie di
    // sessione e CSRF sono cookie "di prima parte" e il CORS non serve.
    proxy: {
      '/api': 'http://127.0.0.1:8081'
    }
  }
})
