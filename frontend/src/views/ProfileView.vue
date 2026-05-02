<script setup>
import { computed, onMounted, ref } from 'vue'
import { useAuthStore } from '../stores/auth'
import api from '../api/client'

const auth = useAuthStore()
const loading = ref(true)
const error = ref('')
const profileSaving = ref(false)
const passwordSaving = ref(false)
const profileMsg = ref('')
const profileMsgOk = ref(true)
const passwordMsg = ref('')
const passwordMsgOk = ref(true)
const showProfileEdit = ref(false)
const showPasswordForm = ref(false)
const profileForm = ref({ fullName: '', phone: '' })
const passwordForm = ref({ current: '', next: '', next2: '' })

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

function openProfileEdit() {
  showPasswordForm.value = false
  showProfileEdit.value = true
}

function openPasswordForm() {
  showProfileEdit.value = false
  showPasswordForm.value = true
}

function closePanels() {
  showProfileEdit.value = false
  showPasswordForm.value = false
  profileMsg.value = ''
  passwordMsg.value = ''
}

async function refreshMe() {
  const { data } = await api.get('/api/auth/me')
  auth.setUser(data)
  profileForm.value = {
    fullName: data.fullName || '',
    phone: data.phone || '',
  }
}

onMounted(async () => {
  error.value = ''
  loading.value = true
  try {
    await refreshMe()
  } catch (e) {
    error.value = e.response?.data?.error || 'Не удалось загрузить профиль'
  } finally {
    loading.value = false
  }
})

async function saveProfile() {
  profileMsg.value = ''
  profileSaving.value = true
  profileMsgOk.value = true
  try {
    await api.patch('/api/auth/profile', {
      fullName: profileForm.value.fullName.trim(),
      phone: profileForm.value.phone.trim() ? profileForm.value.phone.trim() : null,
    })
    await refreshMe()
    profileMsg.value = 'Изменения сохранены'
    profileMsgOk.value = true
    showProfileEdit.value = false
  } catch (e) {
    profileMsg.value = e.response?.data?.error || 'Не удалось сохранить'
    profileMsgOk.value = false
  } finally {
    profileSaving.value = false
  }
}

async function savePassword() {
  passwordMsg.value = ''
  passwordMsgOk.value = true
  if (passwordForm.value.next !== passwordForm.value.next2) {
    passwordMsg.value = 'Новые пароли не совпадают'
    passwordMsgOk.value = false
    return
  }
  passwordSaving.value = true
  try {
    await api.patch('/api/auth/password', {
      currentPassword: passwordForm.value.current,
      newPassword: passwordForm.value.next,
    })
    passwordForm.value = { current: '', next: '', next2: '' }
    passwordMsg.value = 'Пароль обновлён'
    passwordMsgOk.value = true
    showPasswordForm.value = false
  } catch (e) {
    passwordMsg.value = e.response?.data?.error || 'Не удалось сменить пароль'
    passwordMsgOk.value = false
  } finally {
    passwordSaving.value = false
  }
}
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
        <template v-else-if="auth.user">
          <article class="card profile-card">
            <div class="profile-card__head">
              <div class="profile-card__avatar" aria-hidden="true">{{ initials }}</div>
              <div class="profile-card__title-block">
                <h2 class="profile-card__name">{{ auth.user.fullName || 'Без имени' }}</h2>
                <span class="badge profile-card__role">{{ roleLabel }}</span>
              </div>
              <div class="profile-card__toolbar">
                <button
                  type="button"
                  class="profile-icon-btn"
                  title="Редактировать профиль"
                  :aria-pressed="showProfileEdit"
                  @click="showProfileEdit ? closePanels() : openProfileEdit()"
                >
                  <svg viewBox="0 0 24 24" width="22" height="22" aria-hidden="true">
                    <path
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                      d="M12 20h9M16.5 3.5a2.12 2.12 0 013 3L8 18l-4 1 1-4 11.5-11.5z"
                    />
                  </svg>
                </button>
                <button type="button" class="btn btn-ghost profile-pwd-btn" @click="showPasswordForm ? closePanels() : openPasswordForm()">
                  Смена пароля
                </button>
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
            </dl>

            <div v-if="showProfileEdit" class="profile-panel stack">
              <p v-if="profileMsg" :class="profileMsgOk ? 'muted' : 'error'" style="margin: 0">{{ profileMsg }}</p>
              <form class="stack" style="gap: 0.75rem" @submit.prevent="saveProfile">
                <div class="field">
                  <label class="label" for="pf-name">Имя и фамилия</label>
                  <input id="pf-name" v-model="profileForm.fullName" class="input" type="text" autocomplete="name" />
                </div>
                <div class="field">
                  <label class="label" for="pf-phone">Телефон</label>
                  <input id="pf-phone" v-model="profileForm.phone" class="input" type="tel" autocomplete="tel" />
                </div>
                <div class="row" style="gap: 0.5rem">
                  <button type="submit" class="btn btn-gradient" :disabled="profileSaving">
                    {{ profileSaving ? 'Сохранение…' : 'Сохранить' }}
                  </button>
                  <button type="button" class="btn btn-ghost" @click="closePanels">Отмена</button>
                </div>
              </form>
            </div>

            <div v-if="showPasswordForm" class="profile-panel stack">
              <p v-if="passwordMsg" :class="passwordMsgOk ? 'muted' : 'error'" style="margin: 0">{{ passwordMsg }}</p>
              <form class="stack" style="gap: 0.75rem" @submit.prevent="savePassword">
                <div class="field">
                  <label class="label" for="pw-current">Текущий пароль</label>
                  <input id="pw-current" v-model="passwordForm.current" class="input" type="password" autocomplete="current-password" />
                </div>
                <div class="field">
                  <label class="label" for="pw-next">Новый пароль</label>
                  <input id="pw-next" v-model="passwordForm.next" class="input" type="password" minlength="8" autocomplete="new-password" />
                </div>
                <div class="field">
                  <label class="label" for="pw-next2">Повтор нового пароля</label>
                  <input id="pw-next2" v-model="passwordForm.next2" class="input" type="password" minlength="8" autocomplete="new-password" />
                </div>
                <div class="row" style="gap: 0.5rem">
                  <button type="submit" class="btn btn-gradient" :disabled="passwordSaving">
                    {{ passwordSaving ? 'Обновление…' : 'Обновить пароль' }}
                  </button>
                  <button type="button" class="btn btn-ghost" @click="closePanels">Отмена</button>
                </div>
              </form>
            </div>
          </article>
        </template>
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
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1.25rem;
  padding-bottom: 1.25rem;
  border-bottom: 1px solid var(--line);
  flex-wrap: wrap;
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
  grid-area: avatar;
}

.profile-card__title-block {
  flex: 1;
  min-width: 0;
  grid-area: title;
}

.profile-card__name {
  margin: 0 0 0.5rem;
  font-size: 1.25rem;
  color: var(--ink);
}

.profile-card__role {
  font-size: 0.75rem;
}

.profile-card__toolbar {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  margin-left: auto;
  flex-wrap: wrap;
  grid-area: toolbar;
}

.profile-icon-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 42px;
  height: 42px;
  padding: 0;
  border: 1px solid var(--line);
  border-radius: 12px;
  background: var(--surface);
  color: var(--ink);
  cursor: pointer;
}

.profile-icon-btn:hover {
  border-color: var(--ink);
}

.profile-icon-btn[aria-pressed='true'] {
  border-color: var(--ink);
  background: var(--line);
}

.profile-pwd-btn {
  font-size: 0.875rem;
  padding: 0.45rem 0.75rem;
}

.profile-card__dl {
  margin: 0;
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

.profile-panel {
  margin-top: 1.25rem;
  padding-top: 1.25rem;
  border-top: 1px dashed var(--line);
}

@media (max-width: 640px) {
  .profile-card {
    padding: 1.1rem;
  }

  .profile-card__head {
    display: grid;
    grid-template-columns: 64px 1fr;
    grid-template-areas:
      'avatar title'
      'toolbar toolbar';
    gap: 0.75rem;
    align-items: center;
  }

  .profile-card__avatar {
    width: 64px;
    height: 64px;
    font-size: 1.15rem;
  }

  .profile-card__name {
    margin-bottom: 0.35rem;
    font-size: 1.05rem;
    line-height: 1.15;
  }

  .profile-card__toolbar {
    margin-left: 0;
    width: 100%;
    justify-content: flex-start;
  }

  .profile-pwd-btn {
    font-size: 0.8rem;
    padding: 0.38rem 0.62rem;
  }
}
</style>
