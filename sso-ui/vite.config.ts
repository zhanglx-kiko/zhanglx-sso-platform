import { fileURLToPath, URL } from 'node:url'

import { defineConfig, type PluginOption } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueJsx from '@vitejs/plugin-vue-jsx'
import vueDevTools from 'vite-plugin-vue-devtools'

const resolveManualChunk = (id: string) => {
  const normalizedId = id.replace(/\\/g, '/')

  if (!normalizedId.includes('/node_modules/')) {
    return undefined
  }

  if (
    normalizedId.includes('/node_modules/vue/') ||
    normalizedId.includes('/node_modules/@vue/') ||
    normalizedId.includes('/node_modules/vue-router/') ||
    normalizedId.includes('/node_modules/pinia/')
  ) {
    return 'framework'
  }

  if (normalizedId.includes('/node_modules/@element-plus/icons-vue/')) {
    return 'element-icons'
  }

  if (normalizedId.includes('/node_modules/element-plus/')) {
    return 'element-ui'
  }

  if (normalizedId.includes('/node_modules/axios/')) {
    return 'http'
  }

  if (normalizedId.includes('/node_modules/nprogress/')) {
    return 'progress'
  }

  return 'vendor'
}

export default defineConfig(({ command }) => {
  const plugins: PluginOption[] = [vue(), vueJsx()]

  // 仅在开发环境启用调试插件，避免生产构建额外注入调试代码。
  if (command === 'serve') {
    plugins.push(vueDevTools())
  }

  return {
    plugins,
    resolve: {
      alias: {
        '@': fileURLToPath(new URL('./src', import.meta.url)),
      },
    },
    build: {
      rollupOptions: {
        output: {
          manualChunks: resolveManualChunk,
        },
      },
    },
    server: {
      proxy: {
        '/api': {
          target: 'http://localhost:21800',
          changeOrigin: true,
          configure: (proxy) => {
            proxy.on('error', (err, req, res) => {
              console.error(`[Vite Proxy Error]: ${err.message}`)

              // 代理异常时统一返回可读的 JSON，减少联调时的排障成本。
              if ('writeHead' in res) {
                if (!res.headersSent) {
                  res.writeHead(502, { 'Content-Type': 'application/json' })
                }
                res.end(
                  JSON.stringify({
                    code: 502,
                    msg: '网关代理异常，后端服务暂时不可用，请检查本地服务或网关配置。',
                  }),
                )
              } else {
                res.end()
              }
            })
          },
          rewrite: (path) => path.replace(/^\/api/, ''),
        },
      },
    },
  }
})
