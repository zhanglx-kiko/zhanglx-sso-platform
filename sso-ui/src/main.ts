import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'
import { hasHandledGlobalError } from '@/stores/globalError'
import { setupAuthSessionMonitor } from '@/utils/authMonitor'
import { setupElementPlus } from '@/plugins/element-plus'
import 'element-plus/dist/index.css'
import '@/assets/styles/index.scss'

window.addEventListener('unhandledrejection', (event) => {
  const reason = event.reason
  if (
    hasHandledGlobalError(reason)
    || reason?.name === 'CanceledError'
    || reason?.code === 'ERR_CANCELED'
  ) {
    event.preventDefault()
  }
})

const app = createApp(App)

app.use(createPinia())
app.use(router)
setupElementPlus(app)
setupAuthSessionMonitor()

app.mount('#app')
