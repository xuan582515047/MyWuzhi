import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
      '#': fileURLToPath(new URL('./public', import.meta.url)),
    },
  },
  base: './', // 设置为相对路径
  build: {
    outDir: 'dist',
    chunkSizeWarningLimit: 1000, // 提高chunk大小警告限制
  },
  // 服务器配置
  server: {
    host: '0.0.0.0',
    port: 5173,
    // 代理配置，解决跨域问题
    proxy: {
      '/api': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      },
      // 静态文件代理，用于访问上传的文件
      '/files': {
        target: 'http://localhost:8090',
        changeOrigin: true
        // 注意：不要添加 rewrite，保持 /files/xxx 路径不变
      }
    }
  }
})
