import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    host: '0.0.0.0',
    port: 5173,
    // Cloudflare temporary hostnames differ for each run.
    // Only set a public tunnel when you understand Vite's dev-server exposure.
    allowedHosts: process.env.VITE_PUBLIC_HOST ? [process.env.VITE_PUBLIC_HOST] : [],
    proxy: {
      '/api': { target: 'http://127.0.0.1:8080', changeOrigin: true }
    }
  }
})
