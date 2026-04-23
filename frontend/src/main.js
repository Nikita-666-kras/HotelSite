import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'
import { useAuthStore } from './stores/auth'
import api from './api/client'
import './assets/app.css'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)
app.use(router)

async function bootstrap() {
  try {
    await api.get('/api/tours/search')
  } catch {
    /* ignore: primes CSRF cookie when backend is up */
  }
  const auth = useAuthStore()
  auth.hydrate()
  app.mount('#app')
}

bootstrap()
