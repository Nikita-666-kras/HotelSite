import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api/client'

export const useAuthStore = defineStore('auth', () => {
  const user = ref(null)
  const loading = ref(false)

  const isAuthenticated = computed(() => !!user.value)
  const isManager = computed(() => user.value?.role === 'MANAGER' || user.value?.role === 'ADMIN')
  const isAdmin = computed(() => user.value?.role === 'ADMIN')

  function setTokens(accessToken, refreshToken) {
    sessionStorage.setItem('ta_tokens', JSON.stringify({ accessToken, refreshToken }))
  }

  function clearTokens() {
    sessionStorage.removeItem('ta_tokens')
  }

  async function hydrate() {
    const raw = sessionStorage.getItem('ta_tokens')
    if (!raw) return
    try {
      loading.value = true
      const { data } = await api.get('/api/auth/me')
      user.value = data
    } catch {
      clearTokens()
      user.value = null
    } finally {
      loading.value = false
    }
  }

  async function login(email, password) {
    const { data } = await api.post('/api/auth/login', { email, password })
    setTokens(data.accessToken, data.refreshToken)
    await hydrate()
    return data
  }

  async function register(payload) {
    await api.post('/api/auth/register', payload)
    await login(payload.email, payload.password)
  }

  async function logout() {
    clearTokens()
    user.value = null
  }

  function setUser(u) {
    user.value = u
  }

  return {
    user,
    loading,
    isAuthenticated,
    isManager,
    isAdmin,
    hydrate,
    login,
    register,
    logout,
    setTokens,
    setUser,
  }
})
