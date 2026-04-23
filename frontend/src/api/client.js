import axios from 'axios'

function readCookie(name) {
  const m = document.cookie.match(new RegExp('(?:^|; )' + name.replace(/[$()*+.?[\\\]^{|}]/g, '\\$&') + '=([^;]*)'))
  return m ? decodeURIComponent(m[1]) : null
}

const api = axios.create({
  baseURL: '',
  withCredentials: true,
  headers: { 'Content-Type': 'application/json' },
})

api.interceptors.request.use((config) => {
  if (config.data instanceof FormData) {
    delete config.headers['Content-Type']
  }
  const xsrf = readCookie('XSRF-TOKEN')
  if (xsrf && ['post', 'put', 'patch', 'delete'].includes((config.method || '').toLowerCase())) {
    config.headers['X-XSRF-TOKEN'] = xsrf
  }
  const raw = sessionStorage.getItem('ta_tokens')
  if (raw) {
    try {
      const { accessToken } = JSON.parse(raw)
      if (accessToken) {
        config.headers.Authorization = `Bearer ${accessToken}`
      }
    } catch {
      /* ignore */
    }
  }
  return config
})

let refreshPromise = null

async function refreshTokens() {
  const raw = sessionStorage.getItem('ta_tokens')
  if (!raw) return null
  let refreshToken
  try {
    refreshToken = JSON.parse(raw).refreshToken
  } catch {
    return null
  }
  if (!refreshToken) return null
  const xsrf = readCookie('XSRF-TOKEN')
  const { data } = await axios.post(
    '/api/auth/refresh',
    { refreshToken },
    {
      withCredentials: true,
      headers: {
        'Content-Type': 'application/json',
        ...(xsrf ? { 'X-XSRF-TOKEN': xsrf } : {}),
      },
    },
  )
  return data
}

api.interceptors.response.use(
  (r) => r,
  async (error) => {
    const original = error.config
    if (!original || original._retry) {
      return Promise.reject(error)
    }
    if (error.response?.status === 401 && !original.url?.includes('/api/auth/login')) {
      original._retry = true
      try {
        if (!refreshPromise) {
          refreshPromise = refreshTokens().finally(() => {
            refreshPromise = null
          })
        }
        const tokens = await refreshPromise
        if (tokens?.accessToken) {
          sessionStorage.setItem(
            'ta_tokens',
            JSON.stringify({
              accessToken: tokens.accessToken,
              refreshToken: tokens.refreshToken,
            }),
          )
          original.headers.Authorization = `Bearer ${tokens.accessToken}`
          const xsrf = readCookie('XSRF-TOKEN')
          if (xsrf) {
            original.headers['X-XSRF-TOKEN'] = xsrf
          }
          return api(original)
        }
      } catch {
        sessionStorage.removeItem('ta_tokens')
      }
    }
    return Promise.reject(error)
  },
)

export default api
