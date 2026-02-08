import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'path'

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  build: {
    // 번들 크기 경고 임계값 (500kb)
    chunkSizeWarningLimit: 500,

    // 코드 스플리팅 최적화
    rollupOptions: {
      output: {
        manualChunks: {
          // React 관련 라이브러리를 별도 청크로 분리
          'react-vendor': ['react', 'react-dom', 'react-router-dom'],

          // UI 컴포넌트 라이브러리를 별도 청크로 분리
          'ui-vendor': [
            '@radix-ui/react-alert-dialog',
            '@radix-ui/react-dialog',
            '@radix-ui/react-progress',
            '@radix-ui/react-toast',
          ],

          // 데이터 페칭 및 폼 라이브러리를 별도 청크로 분리
          'data-vendor': [
            '@tanstack/react-query',
            'axios',
            'react-hook-form',
            '@hookform/resolvers',
            'zod',
          ],
        },
      },
    },
  },
})
