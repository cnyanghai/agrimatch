import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    proxy: {
      '/ws': {
        target: 'ws://localhost:8080',
        ws: true,
        changeOrigin: true
      },
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (!id.includes('node_modules')) return
          if (id.includes('/vue/') || id.includes('/@vue/')) return 'vendor-vue'
          if (id.includes('/vue-router/')) return 'vendor-vue-router'
          if (id.includes('/pinia/')) return 'vendor-pinia'
          // element-plus 进一步拆分，避免单 chunk 过大
          if (id.includes('/element-plus/es/locale') || id.includes('/element-plus/es/locale')) return 'vendor-element-plus-locale'
          if (id.includes('/element-plus/es/components')) {
            const m = id.match(/element-plus\/es\/components\/([^/]+)/)
            if (m?.[1]) return `ep-${m[1]}`
            return 'vendor-element-plus-components'
          }
          if (id.includes('/element-plus/es')) return 'vendor-element-plus-core'
          if (id.includes('/element-plus/')) return 'vendor-element-plus-core'
          if (id.includes('/@element-plus/')) return 'vendor-element-plus-core'
          if (id.includes('/@element-plus/icons-vue/')) return 'vendor-icons'
          if (id.includes('/lucide-vue-next/')) return 'vendor-icons'
          if (id.includes('/axios/')) return 'vendor-axios'
          return 'vendor'
        }
      }
    }
  },
})
