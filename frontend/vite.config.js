import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/contract': {
        target: 'http://localhost:1005',
        changeOrigin: true
      }
    }
  }
})
