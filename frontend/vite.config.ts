import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'

// dev-only proxy per https://vite.dev/config/server-options.html#server-proxy
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/auth': 'http://localhost:8080',
      '/users': 'http://localhost:8080',
      '/events': 'http://localhost:8080',
      '/actuator': 'http://localhost:8080',
    },
  },
})
