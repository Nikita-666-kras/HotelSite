<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import api from '../api/client'
import { bookingStatusLabel, BOOKING_STATUSES } from '../utils/bookingStatuses'
import { tourCategoryLabel } from '../utils/tourCategories'
import { useAuthStore } from '../stores/auth'

const statusFilter = ref('')
const items = ref([])
const error = ref('')
const editId = ref(null)
const forms = ref({})
const managers = ref([])
const auth = useAuthStore()

const statuses = BOOKING_STATUSES

function formFor(id) {
  if (!forms.value[id]) {
    forms.value[id] = { newStatus: '', managerId: '', note: '' }
  }
  return forms.value[id]
}

function startEdit(id) {
  editId.value = id
  const b = items.value.find((x) => x.id === id)
  if (b) {
    formFor(id).newStatus = b.status
    formFor(id).note = b.notes || ''
    formFor(id).managerId = b.assignedManager?.id?.toString() || ''
  }
}

async function load() {
  error.value = ''
  try {
    const params = {}
    if (statusFilter.value) params.status = statusFilter.value
    const { data } = await api.get('/api/manager/crm/bookings', { params })
    items.value = data
    editId.value = null
    if (auth.isAdmin && !managers.value.length) {
      await loadManagers()
    }
  } catch (e) {
    error.value = e.response?.data?.error || 'Нет доступа или ошибка'
  }
}

async function loadManagers() {
  const { data } = await api.get('/api/manager/crm/managers')
  managers.value = data
}

async function patch(id) {
  error.value = ''
  const f = formFor(id)
  try {
    const body = {}
    if (f.newStatus) body.status = f.newStatus
    if (f.note) body.notes = f.note
    if (f.managerId) body.assignedManagerId = Number(f.managerId)
    await api.patch(`/api/manager/crm/bookings/${id}`, body)
    await load()
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка сохранения'
  }
}

onMounted(load)
</script>

<template>
  <section class="section">
    <div class="container stack">
      <div class="page-hero" style="border: none; padding: 0 0 1rem; margin: 0">
        <h1 style="font-size: 1.85rem">CRM · заявки</h1>
        <p class="lead">Распределяй заявки и веди статусы без лишней суеты.</p>
        <p class="muted">
          <RouterLink to="/manager/catalog">Каталог туров и услуг →</RouterLink>
        </p>
      </div>
      <div class="row">
        <select v-model="statusFilter" class="select" style="max-width: 220px" @change="load">
          <option value="">Все статусы</option>
          <option v-for="s in statuses" :key="s" :value="s">{{ bookingStatusLabel(s) }}</option>
        </select>
        <button type="button" class="btn btn-ghost" @click="load">Обновить</button>
      </div>
      <p v-if="error" class="error">{{ error }}</p>
      <div class="stack">
        <article v-for="b in items" :key="b.id" class="card">
          <div class="row-between">
            <div>
              <strong>#{{ b.id }} — {{ b.tour.title }}</strong>
              <p class="muted" style="margin: 0.25rem 0 0">
                Категория: {{ tourCategoryLabel(b.tour.category) }}
              </p>
              <p class="muted" style="margin: 0.25rem 0 0">{{ b.client.fullName }} · {{ b.client.email }}</p>
            </div>
            <span class="badge">{{ bookingStatusLabel(b.status) }}</span>
          </div>
          <p class="muted">Участников: {{ b.participants.length }} · Тел.: {{ b.contactPhone }}</p>
          <button type="button" class="btn btn-ghost" @click="startEdit(b.id)">
            {{ editId === b.id ? 'Свернуть' : 'Распределить / статус' }}
          </button>
          <div v-if="editId === b.id" class="stack" style="margin-top: 0.75rem">
            <div class="grid grid-2">
              <div class="field">
                <label class="label">Статус</label>
                <select v-model="formFor(b.id).newStatus" class="select">
                  <option v-for="s in statuses" :key="s" :value="s">{{ bookingStatusLabel(s) }}</option>
                </select>
              </div>
              <div class="field">
                <label class="label">Менеджер</label>
                <select v-if="auth.isAdmin" v-model="formFor(b.id).managerId" class="select">
                  <option value="">Не менять</option>
                  <option v-for="m in managers" :key="m.id" :value="String(m.id)">{{ m.fullName }} ({{ m.email }})</option>
                </select>
                <input v-else v-model="formFor(b.id).managerId" class="input" disabled placeholder="Только админ" />
              </div>
            </div>
            <div class="field">
              <label class="label">Заметки</label>
              <textarea v-model="formFor(b.id).note" class="textarea" rows="2" />
            </div>
            <button type="button" class="btn btn-gradient" @click="patch(b.id)">Сохранить</button>
          </div>
        </article>
      </div>
    </div>
  </section>
</template>
