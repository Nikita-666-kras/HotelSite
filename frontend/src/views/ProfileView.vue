<script setup>
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import api from '../api/client'

const auth = useAuthStore()
const loading = ref(true)
const error = ref('')

const roleLabel = computed(() => {
  const r = auth.user?.role
  if (r === 'ADMIN') return 'Администратор'
  if (r === 'MANAGER') return 'Менеджер'
  return 'Клиент'
})

const initials = computed(() => {
  const n = auth.user?.fullName || auth.user?.email || '?'
  const p = n.trim().split(/\s+/)
  if (p.length >= 2) return (p[0][0] + p[1][0]).toUpperCase()
  return n.slice(0, 2).toUpperCase()
})

onMounted(async () => {
  error.value = ''
  loading.value = true
  try {
    const { data } = await api.get('/api/auth/me')
    auth.setUser(data)
  } catch (e) {
    error.value = e.response?.data?.error || 'Не удалось загрузить профиль'
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div>
    <div class="page-hero">
      <div class="container">
        <h1>Профиль</h1>
        <p class="lead">Данные вашего аккаунта TripWave</p>
      </div>
    </div>
    <section class="section" style="padding-top: 1rem">
      <div class="container stack" style="max-width: 520px">
        <p v-if="loading" class="muted">Загрузка…</p>
        <p v-else-if="error" class="error">{{ error }}</p>
        <article v-else-if="auth.user" class="card profile-card">
          <div class="profile-card__head">
            <div class="profile-card__avatar" aria-hidden="true">{{ initials }}</div>
            <div>
              <h2 class="profile-card__name">{{ auth.user.fullName || 'Без имени' }}</h2>
              <span class="badge profile-card__role">{{ roleLabel }}</span>
            </div>
          </div>
          <dl class="profile-card__dl">
            <div>
              <dt>Email</dt>
              <dd>{{ auth.user.email }}</dd>
            </div>
            <div>
              <dt>Телефон</dt>
              <dd>{{ auth.user.phone || '—' }}</dd>
            </div>
            <div>
              <dt>ID</dt>
              <dd class="muted mono">{{ auth.user.id }}</dd>
            </div>
          </dl>
          <p class="muted" style="margin: 0; font-size: 0.85rem">
            Изменение данных в демо-версии по запросу к менеджеру; позже можно подключить редактирование в кабинете.
          </p>
        </article>
      </div>
    </section>
  </div>
</template>

<style scoped>
.profile-card {
  padding: 1.5rem;
}

.profile-card__head {
  display: flex;
  align-items: center;
  gap: 1.25rem;
  margin-bottom: 1.5rem;
  padding-bottom: 1.25rem;
  border-bottom: 1px solid var(--line);
}

.profile-card__avatar {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  background: var(--ink);
  color: var(--surface);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 1.35rem;
  letter-spacing: -0.02em;
  flex-shrink: 0;
}

.profile-card__name {
  margin: 0 0 0.5rem;
  font-size: 1.25rem;
  color: var(--ink);
}

.profile-card__role {
  font-size: 0.75rem;
}

.profile-card__dl {
  margin: 0 0 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1rem;
}

.profile-card__dl dt {
  margin: 0;
  font-size: 0.75rem;
  text-transform: uppercase;
  letter-spacing: 0.04em;
  color: var(--muted);
}

.profile-card__dl dd {
  margin: 0.2rem 0 0;
  font-size: 1rem;
  color: var(--ink);
}

.mono {
  font-family: ui-monospace, monospace;
  font-size: 0.9rem;
}
</style>
