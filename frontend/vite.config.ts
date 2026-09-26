import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'

// https://vite.dev/config/
export default defineConfig({
  // GitHub Pages serves this repository below /dynamic-qr-ticketing/.
  base: process.env.GITHUB_PAGES === 'true' ? '/dynamic-qr-ticketing/' : '/',
  plugins: [
    react(),
    tailwindcss(),
  ],
  server: {
    port: 5173,
    host: true,
  },
})

