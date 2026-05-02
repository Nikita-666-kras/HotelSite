<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import api from '../api/client'

const tab = ref('analytics')
const error = ref('')
const sales = ref(null)

const managersPage = ref({ content: [], totalElements: 0, totalPages: 0, number: 0 })
const clientsPage = ref({ content: [], totalElements: 0, totalPages: 0, number: 0 })
const mgrPage = ref(0)
const cliPage = ref(0)
const mgrQ = ref('')
const cliQ = ref('')

const newManager = ref({ email: '', password: '', fullName: '', phone: '' })

const editingMgr = ref(null)
const editingCli = ref(null)
const pwdTarget = ref(null)
const pwdNew = ref('')
const pwd2 = ref('')

function resetNewManager() {
  newManager.value = { email: '', password: '', fullName: '', phone: '' }
}

async function loadSales() {
  const { data } = await api.get('/api/admin/analytics/sales', { params: { days: 30 } })
  sales.value = data
}

async function loadManagers() {
  const { data } = await api.get('/api/admin/users', {
    params: { role: 'MANAGER', q: mgrQ.value.trim() || undefined, page: mgrPage.value, size: 15, sort: 'createdAt,desc' },
  })
  managersPage.value = data
}

async function loadClients() {
  const { data } = await api.get('/api/admin/users', {
    params: { role: 'USER', q: cliQ.value.trim() || undefined, page: cliPage.value, size: 15, sort: 'createdAt,desc' },
  })
  clientsPage.value = data
}

async function createManager() {
  error.value = ''
  if (!newManager.value.email.trim() || !newManager.value.password || !newManager.value.fullName.trim()) {
    error.value = 'Заполните email, пароль и имя'
    return
  }
  try {
    await api.post('/api/admin/managers', {
      email: newManager.value.email.trim(),
      password: newManager.value.password,
      fullName: newManager.value.fullName.trim(),
      phone: newManager.value.phone.trim() || null,
    })
    resetNewManager()
    await loadManagers()
  } catch (e) {
    error.value = e.response?.data?.error || 'Не удалось создать менеджера'
  }
}

function startEditMgr(row) {
  editingCli.value = null
  editingMgr.value = {
    id: row.id,
    email: row.email,
    fullName: row.fullName,
    phone: row.phone || '',
    enabled: row.enabled,
  }
}

function startEditCli(row) {
  editingMgr.value = null
  editingCli.value = {
    id: row.id,
    email: row.email,
    fullName: row.fullName,
    phone: row.phone || '',
    enabled: row.enabled,
  }
}

async function saveEdit(payload) {
  error.value = ''
  try {
    await api.patch(`/api/admin/users/${payload.id}`, {
      email: payload.email.trim(),
      fullName: payload.fullName.trim(),
      phone: payload.phone.trim() ? payload.phone.trim() : null,
      enabled: payload.enabled,
    })
    cancelEdit()
    await Promise.all([loadManagers(), loadClients()])
  } catch (e) {
    error.value = e.response?.data?.error || 'Не удалось сохранить'
  }
}

async function disableUser(id) {
  error.value = ''
  if (!confirm('Отключить пользователя? Вход будет невозможен.')) return
  try {
    await api.delete(`/api/admin/users/${id}`)
    cancelEdit()
    await Promise.all([loadManagers(), loadClients()])
  } catch (e) {
    error.value = e.response?.data?.error || 'Не удалось отключить'
  }
}

function openPwd(row) {
  pwdTarget.value = row
  pwdNew.value = ''
  pwd2.value = ''
}

async function savePwd() {
  error.value = ''
  if (pwdNew.value.length < 8) {
    error.value = 'Пароль не короче 8 символов'
    return
  }
  if (pwdNew.value !== pwd2.value) {
    error.value = 'Пароли не совпадают'
    return
  }
  try {
    await api.patch(`/api/admin/users/${pwdTarget.value.id}/password`, { newPassword: pwdNew.value })
    cancelPwd()
  } catch (e) {
    error.value = e.response?.data?.error || 'Не удалось задать пароль'
  }
}

function cancelEdit() {
  editingMgr.value = null
  editingCli.value = null
}

function cancelPwd() {
  pwdTarget.value = null
}

async function refreshTab() {
  error.value = ''
  try {
    if (tab.value === 'analytics') await loadSales()
    if (tab.value === 'managers') await loadManagers()
    if (tab.value === 'clients') await loadClients()
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка загрузки'
  }
}

watch(tab, () => {
  refreshTab()
})

watch(mgrPage, () => loadManagers())
watch(cliPage, () => loadClients())

onMounted(async () => {
  await refreshTab()
})

const editForm = computed(() => editingMgr.value || editingCli.value)

function applyMgrSearch() {
  mgrPage.value = 0
  loadManagers()
}

function applyCliSearch() {
  cliPage.value = 0
  loadClients()
}
</script>

<template>
  <section class="section">
    <div class="container stack">
      <div class="page-hero" style="border: none; padding: 0 0 1rem; margin: 0">
        <h1 style="font-size: 1.85rem">Администрирование</h1>
        <p class="lead">Аналитика, менеджеры и клиенты.</p>
        <p class="muted"><RouterLink to="/manager/crm">CRM →</RouterLink></p>
      </div>

      <div class="row admin-tabs" style="gap: 0.5rem; flex-wrap: wrap">
        <button type="button" class="btn" :class="tab === 'analytics' ? 'btn-gradient' : 'btn-ghost'" @click="tab = 'analytics'">
          Аналитика
        </button>
        <button type="button" class="btn" :class="tab === 'managers' ? 'btn-gradient' : 'btn-ghost'" @click="tab = 'managers'">
          Менеджеры
        </button>
        <button type="button" class="btn" :class="tab === 'clients' ? 'btn-gradient' : 'btn-ghost'" @click="tab = 'clients'">
          Клиенты
        </button>
      </div>

      <p v-if="error" class="error">{{ error }}</p>

      <article v-if="tab === 'analytics'" class="card stack">
        <h3 style="margin: 0">Продажи за 30 дней</h3>
        <div v-if="sales" class="stack">
          <div><strong>Всего заявок:</strong> {{ sales.totalBookings }}</div>
          <div><strong>Подтверждено:</strong> {{ sales.confirmedBookings }}</div>
          <div><strong>Потеряно:</strong> {{ sales.cancelledOrRejected }}</div>
          <div><strong>Выручка:</strong> {{ sales.confirmedRevenue }}</div>
          <div><strong>Конверсия:</strong> {{ sales.conversionRatePercent }}%</div>
        </div>
        <p v-else class="muted">Загрузка…</p>
      </article>

      <template v-if="tab === 'managers'">
        <article class="card stack admin-form-card">
          <h3 style="margin: 0">Новый менеджер</h3>
          <div class="grid grid-2">
            <input v-model="newManager.email" class="input" type="email" placeholder="Email" autocomplete="off" />
            <input v-model="newManager.password" class="input" type="password" placeholder="Пароль (≥8)" autocomplete="new-password" />
            <input v-model="newManager.fullName" class="input" type="text" placeholder="Имя и фамилия" />
            <input v-model="newManager.phone" class="input" type="tel" placeholder="Телефон" />
          </div>
          <button type="button" class="btn btn-gradient" @click="createManager">Зарегистрировать менеджера</button>
        </article>

        <article class="card stack">
          <div class="row-between" style="flex-wrap: wrap; gap: 0.5rem">
            <h3 style="margin: 0">Список менеджеров</h3>
            <div class="row" style="gap: 0.5rem">
              <input v-model="mgrQ" class="input" style="min-width: 12rem" placeholder="Поиск…" @keyup.enter="applyMgrSearch" />
              <button type="button" class="btn btn-ghost" @click="applyMgrSearch">Найти</button>
            </div>
          </div>
          <div class="stack">
            <div v-for="u in managersPage.content" :key="u.id" class="admin-user-row">
              <div>
                <strong>{{ u.fullName }}</strong>
                <span class="muted" style="display: block; font-size: 0.85rem">{{ u.email }} · {{ u.phone || '—' }}</span>
                <span class="badge" :style="{ opacity: u.enabled ? 1 : 0.6 }">{{ u.enabled ? 'Активен' : 'Отключён' }}</span>
              </div>
              <div class="row" style="gap: 0.35rem; flex-wrap: wrap">
                <button type="button" class="btn btn-ghost" @click="startEditMgr(u)">Изменить</button>
                <button type="button" class="btn btn-ghost" @click="openPwd(u)">Пароль</button>
                <button v-if="u.enabled" type="button" class="btn btn-ghost" @click="disableUser(u.id)">Отключить</button>
              </div>
            </div>
          </div>
          <div class="row-between" style="margin-top: 0.75rem">
            <span class="muted">Стр. {{ managersPage.number + 1 }} / {{ Math.max(managersPage.totalPages, 1) }}</span>
            <div class="row" style="gap: 0.5rem">
              <button type="button" class="btn btn-ghost" :disabled="mgrPage <= 0" @click="mgrPage--">Назад</button>
              <button
                type="button"
                class="btn btn-ghost"
                :disabled="mgrPage >= managersPage.totalPages - 1"
                @click="mgrPage++"
              >
                Вперёд
              </button>
            </div>
          </div>
        </article>
      </template>

      <article v-if="tab === 'clients'" class="card stack">
        <div class="row-between" style="flex-wrap: wrap; gap: 0.5rem">
          <h3 style="margin: 0">Клиенты</h3>
          <div class="row" style="gap: 0.5rem">
            <input v-model="cliQ" class="input" style="min-width: 12rem" placeholder="Поиск…" @keyup.enter="applyCliSearch" />
            <button type="button" class="btn btn-ghost" @click="applyCliSearch">Найти</button>
          </div>
        </div>
        <div class="stack">
          <div v-for="u in clientsPage.content" :key="u.id" class="admin-user-row">
            <div>
              <strong>{{ u.fullName }}</strong>
              <span class="muted" style="display: block; font-size: 0.85rem">{{ u.email }} · {{ u.phone || '—' }}</span>
              <span class="badge" :style="{ opacity: u.enabled ? 1 : 0.6 }">{{ u.enabled ? 'Активен' : 'Отключён' }}</span>
            </div>
            <div class="row" style="gap: 0.35rem; flex-wrap: wrap">
              <button type="button" class="btn btn-ghost" @click="startEditCli(u)">Изменить</button>
              <button type="button" class="btn btn-ghost" @click="openPwd(u)">Пароль</button>
              <button v-if="u.enabled" type="button" class="btn btn-ghost" @click="disableUser(u.id)">Отключить</button>
            </div>
          </div>
        </div>
        <div class="row-between" style="margin-top: 0.75rem">
          <span class="muted">Стр. {{ clientsPage.number + 1 }} / {{ Math.max(clientsPage.totalPages, 1) }}</span>
          <div class="row" style="gap: 0.5rem">
            <button type="button" class="btn btn-ghost" :disabled="cliPage <= 0" @click="cliPage--">Назад</button>
            <button
              type="button"
              class="btn btn-ghost"
              :disabled="cliPage >= clientsPage.totalPages - 1"
              @click="cliPage++"
            >
              Вперёд
            </button>
          </div>
        </div>
      </article>

      <div v-if="editForm" class="card stack admin-edit-overlay">
        <h3 style="margin: 0">Редактирование</h3>
        <div class="grid grid-2">
          <div class="field">
            <label class="label">Email</label>
            <input v-model="editForm.email" class="input" type="email" />
          </div>
          <div class="field">
            <label class="label">Имя</label>
            <input v-model="editForm.fullName" class="input" type="text" />
          </div>
          <div class="field">
            <label class="label">Телефон</label>
            <input v-model="editForm.phone" class="input" type="tel" />
          </div>
          <div class="field row" style="align-items: center; gap: 0.5rem">
            <label class="label" style="margin: 0">Активен</label>
            <input v-model="editForm.enabled" type="checkbox" />
          </div>
        </div>
        <div class="row" style="gap: 0.5rem">
          <button type="button" class="btn btn-gradient" @click="saveEdit(editForm)">Сохранить</button>
          <button type="button" class="btn btn-ghost" @click="cancelEdit">Отмена</button>
        </div>
      </div>

      <div v-if="pwdTarget" class="card stack admin-edit-overlay">
        <h3 style="margin: 0">Новый пароль: {{ pwdTarget.email }}</h3>
        <input v-model="pwdNew" class="input" type="password" placeholder="Новый пароль" autocomplete="new-password" />
        <input v-model="pwd2" class="input" type="password" placeholder="Повтор" autocomplete="new-password" />
        <div class="row" style="gap: 0.5rem">
          <button type="button" class="btn btn-gradient" @click="savePwd">Сохранить</button>
          <button type="button" class="btn btn-ghost" @click="cancelPwd">Отмена</button>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.admin-form-card {
  padding: 1.25rem;
}

.admin-user-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 1rem;
  padding: 0.75rem;
  border: 1px solid var(--line, #e5e5e5);
  border-radius: 12px;
  flex-wrap: wrap;
}

.admin-edit-overlay {
  border: 2px solid var(--ink, #0a0a0a);
}
</style>
