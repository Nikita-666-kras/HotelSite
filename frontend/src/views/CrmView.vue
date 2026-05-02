<script setup>
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import api from '../api/client'
import { bookingStatusLabel, BOOKING_STATUSES } from '../utils/bookingStatuses'
import { tourCategoryLabel } from '../utils/tourCategories'
import { useAuthStore } from '../stores/auth'

const statusFilter = ref('')
const searchTerm = ref('')
const managerFilter = ref('')
const tourFilter = ref('')
const dateFrom = ref('')
const dateTo = ref('')
const page = ref(0)
const pageSize = ref(20)
const totalPages = ref(0)
const totalElements = ref(0)
const countInWork = ref(0)
const countConfirmed = ref(0)
const tours = ref([])
const emailConfigured = ref(false)
const taskFilter = ref('ALL')
const items = ref([])
const error = ref('')
const editId = ref(null)
const forms = ref({})
const managers = ref([])
const tasks = ref([])
const reminders = ref([])
const notifications = ref([])
const bookingDetails = ref({})
const bookingComments = ref({})
const loadingDetails = ref({})
const commentDrafts = ref({})
const taskForm = ref({
  title: '',
  description: '',
  assigneeId: '',
  bookingId: '',
  dueDate: '',
})
const notifyForm = ref({
  recipientId: '',
  channel: 'IN_APP',
  subject: '',
  body: '',
})
const auth = useAuthStore()

const statuses = BOOKING_STATUSES
const taskStatuses = ['OPEN', 'IN_PROGRESS', 'DONE']
const canManageTasks = computed(() => auth.isAdmin)
const unreadNotifications = computed(() => notifications.value.filter((n) => !n.read).length)

const bookingMetrics = computed(() => ({
  total: totalElements.value,
  inWork: countInWork.value,
  confirmed: countConfirmed.value,
}))

const filteredTasks = computed(() => {
  if (taskFilter.value === 'ALL') return tasks.value
  return tasks.value.filter((t) => t.status === taskFilter.value)
})

function taskStatusLabel(status) {
  if (status === 'OPEN') return 'Новая'
  if (status === 'IN_PROGRESS') return 'В работе'
  if (status === 'DONE') return 'Выполнена'
  return status
}

function formFor(id) {
  if (!forms.value[id]) {
    forms.value[id] = { newStatus: '', managerId: '', note: '' }
  }
  return forms.value[id]
}

function detailFor(booking) {
  return bookingDetails.value[booking.id] || booking
}

function commentsFor(bookingId) {
  return bookingComments.value[bookingId] || []
}

function commentDraftFor(bookingId) {
  if (!commentDrafts.value[bookingId]) {
    commentDrafts.value[bookingId] = ''
  }
  return commentDrafts.value[bookingId]
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

function applyFilters() {
  page.value = 0
  load()
}

async function load() {
  error.value = ''
  try {
    const params = {
      page: page.value,
      size: pageSize.value,
      sort: 'createdAt,desc',
    }
    if (statusFilter.value) params.status = statusFilter.value
    if (searchTerm.value.trim()) params.q = searchTerm.value.trim()
    if (managerFilter.value) params.managerId = managerFilter.value
    if (tourFilter.value) params.tourId = tourFilter.value
    if (dateFrom.value) params.createdFrom = dateFrom.value
    if (dateTo.value) params.createdTo = dateTo.value
    const { data } = await api.get('/api/manager/crm/bookings', { params })
    items.value = data.content || []
    totalPages.value = data.totalPages ?? 0
    totalElements.value = data.totalElements ?? 0
    countInWork.value = data.countInWork ?? 0
    countConfirmed.value = data.countConfirmed ?? 0
    editId.value = null
    if (auth.isAdmin && !managers.value.length) {
      await loadManagers()
    }
  } catch (e) {
    error.value = e.response?.data?.error || 'Нет доступа или ошибка'
  }
}

async function loadBookingDetails(bookingId) {
  const { data } = await api.get(`/api/manager/crm/bookings/${bookingId}`)
  bookingDetails.value[bookingId] = data
}

async function loadBookingComments(bookingId) {
  const { data } = await api.get(`/api/manager/crm/bookings/${bookingId}/comments`)
  bookingComments.value[bookingId] = data
}

async function toggleBookingDetails(bookingId) {
  if (loadingDetails.value[bookingId]) return
  if (bookingDetails.value[bookingId]) {
    delete bookingDetails.value[bookingId]
    return
  }
  loadingDetails.value[bookingId] = true
  try {
    await Promise.all([loadBookingDetails(bookingId), loadBookingComments(bookingId)])
  } finally {
    loadingDetails.value[bookingId] = false
  }
}

async function addBookingComment(bookingId) {
  const body = commentDraftFor(bookingId).trim()
  if (!body) return
  await api.post(`/api/manager/crm/bookings/${bookingId}/comments`, { body })
  commentDrafts.value[bookingId] = ''
  await loadBookingComments(bookingId)
}

async function loadTours() {
  try {
    const { data } = await api.get('/api/manager/tours')
    tours.value = data
  } catch {
    tours.value = []
  }
}

async function loadEmailDelivery() {
  if (!auth.isAdmin) return
  try {
    const { data } = await api.get('/api/manager/crm/email-delivery')
    emailConfigured.value = !!data.configured
  } catch {
    emailConfigured.value = false
  }
}

function goPrevPage() {
  if (page.value <= 0) return
  page.value--
  load()
}

function goNextPage() {
  if (totalPages.value === 0 || page.value >= totalPages.value - 1) return
  page.value++
  load()
}

async function quickSetStatus(id, status) {
  await api.patch(`/api/manager/crm/bookings/${id}`, { status })
  await load()
}

async function loadManagers() {
  const { data } = await api.get('/api/manager/crm/managers')
  managers.value = data
}

async function loadTasks() {
  const params = { mineOnly: !auth.isAdmin }
  const { data } = await api.get('/api/manager/crm/tasks', { params })
  tasks.value = data
}

async function createTask() {
  if (!taskForm.value.title.trim()) return
  const body = {
    title: taskForm.value.title,
    description: taskForm.value.description || null,
    assigneeId: taskForm.value.assigneeId?.trim() || null,
    bookingId: taskForm.value.bookingId?.trim() || null,
    dueDate: taskForm.value.dueDate || null,
  }
  await api.post('/api/manager/crm/tasks', body)
  taskForm.value = { title: '', description: '', assigneeId: '', bookingId: '', dueDate: '' }
  await Promise.all([loadTasks(), loadReminders(), loadNotifications()])
}

async function updateTaskStatus(task, status) {
  await api.patch(`/api/manager/crm/tasks/${task.id}`, { status })
  await Promise.all([loadTasks(), loadReminders()])
}

async function loadNotifications() {
  const { data } = await api.get('/api/manager/crm/notifications')
  notifications.value = data
}

async function sendNotification() {
  if (!notifyForm.value.subject.trim() || !notifyForm.value.body.trim()) return
  const body = {
    recipientId: notifyForm.value.recipientId?.trim() || null,
    channel: notifyForm.value.channel,
    subject: notifyForm.value.subject,
    body: notifyForm.value.body,
  }
  await api.post('/api/manager/crm/notifications/send', body)
  notifyForm.value.subject = ''
  notifyForm.value.body = ''
  await loadNotifications()
}

async function markRead(id) {
  await api.patch(`/api/manager/crm/notifications/${id}/read`)
  await loadNotifications()
}

async function loadReminders() {
  const { data } = await api.get('/api/manager/crm/reminders')
  reminders.value = data
}

async function patch(id) {
  error.value = ''
  const f = formFor(id)
  try {
    const body = {}
    if (f.newStatus) body.status = f.newStatus
    if (f.note) body.notes = f.note
    if (f.managerId) body.assignedManagerId = f.managerId
    await api.patch(`/api/manager/crm/bookings/${id}`, body)
    await load()
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка сохранения'
  }
}

async function copyId(id) {
  if (!navigator?.clipboard) return
  await navigator.clipboard.writeText(id)
}

async function loadAll() {
  error.value = ''
  try {
    await Promise.all([load(), loadTours(), loadEmailDelivery()])
    await Promise.all([loadTasks(), loadNotifications(), loadReminders()])
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка CRM'
  }
}

onMounted(loadAll)
</script>

<template>
  <section class="section">
    <div class="container stack">
      <div class="page-hero" style="border: none; padding: 0 0 1rem; margin: 0">
        <h1 style="font-size: 1.85rem">CRM · заявки</h1>
        <p class="lead">Рабочее место менеджера: заявки, задачи и коммуникации в одном экране.</p>
        <p class="muted"><RouterLink to="/manager/catalog">Каталог туров и услуг →</RouterLink></p>
      </div>

      <div class="crm-kpis">
        <article class="card crm-kpi"><p class="muted">Всего заявок</p><strong>{{ bookingMetrics.total }}</strong></article>
        <article class="card crm-kpi"><p class="muted">В работе</p><strong>{{ bookingMetrics.inWork }}</strong></article>
        <article class="card crm-kpi"><p class="muted">Подтверждено</p><strong>{{ bookingMetrics.confirmed }}</strong></article>
        <article class="card crm-kpi"><p class="muted">Непрочитанные</p><strong>{{ unreadNotifications }}</strong></article>
      </div>

      <div class="card stack">
        <div class="row" style="flex-wrap: wrap; gap: 0.5rem">
          <select v-model="statusFilter" class="select crm-select" @change="applyFilters">
            <option value="">Все статусы</option>
            <option v-for="s in statuses" :key="s" :value="s">{{ bookingStatusLabel(s) }}</option>
          </select>
          <select v-if="auth.isAdmin" v-model="managerFilter" class="select crm-select" @change="applyFilters">
            <option value="">Все менеджеры</option>
            <option v-for="m in managers" :key="m.id" :value="String(m.id)">{{ m.fullName }}</option>
          </select>
          <select v-model="tourFilter" class="select crm-select crm-tour-select" @change="applyFilters">
            <option value="">Все туры</option>
            <option v-for="t in tours" :key="t.id" :value="String(t.id)">{{ t.title }}</option>
          </select>
          <label class="crm-date-label muted" style="display: flex; align-items: center; gap: 0.35rem">
            <span>От</span>
            <input v-model="dateFrom" type="date" class="input" style="max-width: 11rem" @change="applyFilters" />
          </label>
          <label class="crm-date-label muted" style="display: flex; align-items: center; gap: 0.35rem">
            <span>До</span>
            <input v-model="dateTo" type="date" class="input" style="max-width: 11rem" @change="applyFilters" />
          </label>
          <input v-model="searchTerm" class="input crm-search" placeholder="Поиск: клиент, email, UUID, тур, телефон" />
          <button type="button" class="btn btn-gradient" @click="applyFilters">Применить</button>
        </div>
        <div class="row-between crm-pager" style="margin-top: 0.75rem; flex-wrap: wrap; gap: 0.5rem">
          <p class="muted" style="margin: 0">
            Страница {{ page + 1 }} из {{ Math.max(totalPages, 1) }} · Всего по фильтру: {{ totalElements }}
          </p>
          <div class="row" style="gap: 0.5rem">
            <button type="button" class="btn btn-ghost" :disabled="page <= 0" @click="goPrevPage">Назад</button>
            <button
              type="button"
              class="btn btn-ghost"
              :disabled="totalPages === 0 || page >= totalPages - 1"
              @click="goNextPage"
            >
              Вперёд
            </button>
          </div>
        </div>
      </div>

      <p v-if="error" class="error">{{ error }}</p>

      <div class="stack">
        <article v-for="b in items" :key="b.id" class="card stack">
          <div class="row-between">
            <div>
              <div class="row" style="gap: 0.4rem; align-items: baseline">
                <strong>{{ b.tour.title }}</strong>
                <span class="badge">{{ bookingStatusLabel(b.status) }}</span>
              </div>
              <p class="muted" style="margin: 0.2rem 0 0">{{ b.client.fullName }} · {{ b.client.email }}</p>
              <p class="muted" style="margin: 0.2rem 0 0">Категория: {{ tourCategoryLabel(b.tour.category) }} · Участников: {{ b.participants.length }} · Тел.: {{ b.contactPhone || '—' }}</p>
            </div>
            <div class="crm-id-wrap">
              <code class="crm-id">{{ b.id }}</code>
              <button class="btn btn-ghost" type="button" @click="copyId(b.id)">Копировать</button>
            </div>
          </div>

          <div class="row">
            <button type="button" class="btn btn-ghost" @click="quickSetStatus(b.id, 'ASSIGNED')">Назначить</button>
            <button type="button" class="btn btn-ghost" @click="quickSetStatus(b.id, 'CONFIRMED')">Подтвердить</button>
            <button type="button" class="btn btn-ghost" @click="startEdit(b.id)">{{ editId === b.id ? 'Свернуть детали' : 'Детальное редактирование' }}</button>
            <button type="button" class="btn btn-ghost" @click="toggleBookingDetails(b.id)">
              {{ bookingDetails[b.id] ? 'Скрыть анкету' : 'Полные данные + комментарии' }}
            </button>
          </div>

          <div v-if="bookingDetails[b.id]" class="stack crm-edit-box">
            <h4 style="margin: 0">Подробные данные заявки</h4>
            <div class="grid grid-2">
              <div>
                <p class="muted" style="margin: 0 0 0.25rem">Телефон</p>
                <div>{{ detailFor(b).contactPhone || '—' }}</div>
              </div>
              <div>
                <p class="muted" style="margin: 0 0 0.25rem">Внутренние заметки</p>
                <div>{{ detailFor(b).notes || '—' }}</div>
              </div>
            </div>

            <div class="grid grid-3">
              <div class="crm-line-card">
                <strong>Авиарегистрация</strong>
                <p class="muted" style="margin: 0.2rem 0">Документ: {{ detailFor(b).flightRegistration?.passengerDocNumber || '—' }}</p>
                <p class="muted" style="margin: 0.2rem 0">Гражданство: {{ detailFor(b).flightRegistration?.citizenship || '—' }}</p>
                <p class="muted" style="margin: 0.2rem 0">Лояльность: {{ detailFor(b).flightRegistration?.loyaltyProgram || '—' }}</p>
                <p class="muted" style="margin: 0.2rem 0">Багаж: {{ detailFor(b).flightRegistration?.baggageNotes || '—' }}</p>
              </div>
              <div class="crm-line-card">
                <strong>Отель</strong>
                <p class="muted" style="margin: 0.2rem 0">Гость: {{ detailFor(b).hotelRegistration?.guestFullName || '—' }}</p>
                <p class="muted" style="margin: 0.2rem 0">Документ: {{ detailFor(b).hotelRegistration?.documentNumber || '—' }}</p>
                <p class="muted" style="margin: 0.2rem 0">Прибытие: {{ detailFor(b).hotelRegistration?.estimatedArrivalTime || '—' }}</p>
                <p class="muted" style="margin: 0.2rem 0">Пожелания: {{ detailFor(b).hotelRegistration?.specialRequests || '—' }}</p>
              </div>
              <div class="crm-line-card">
                <strong>Ж/д</strong>
                <p class="muted" style="margin: 0.2rem 0">Документ: {{ detailFor(b).railRegistration?.passengerDocNumber || '—' }}</p>
                <p class="muted" style="margin: 0.2rem 0">Место: {{ detailFor(b).railRegistration?.preferredSeat || '—' }}</p>
                <p class="muted" style="margin: 0.2rem 0">Вагон: {{ detailFor(b).railRegistration?.wagonPreferences || '—' }}</p>
                <p class="muted" style="margin: 0.2rem 0">Комментарий: {{ detailFor(b).railRegistration?.notes || '—' }}</p>
              </div>
            </div>

            <div class="stack">
              <strong>Участники</strong>
              <div v-for="p in detailFor(b).participants" :key="p.id" class="crm-line-card">
                <div>
                  {{ p.lastName }} {{ p.firstName }}
                  <span class="muted">· {{ p.child ? 'ребёнок' : 'взрослый' }}</span>
                </div>
                <p class="muted" style="margin: 0.2rem 0">Дата рождения: {{ p.dateOfBirth || '—' }}</p>
                <p class="muted" style="margin: 0.2rem 0">Паспорт: {{ p.passportSeries || '—' }} {{ p.passportNumber || '' }}</p>
                <p class="muted" style="margin: 0.2rem 0">Дата выдачи: {{ p.passportIssueDate || '—' }}</p>
                <p class="muted" style="margin: 0.2rem 0">Свидетельство: {{ p.birthCertificateNumber || '—' }}</p>
                <p class="muted" style="margin: 0.2rem 0">Телефон: {{ p.phone || '—' }}</p>
                <p class="muted" style="margin: 0.2rem 0">Комментарий: {{ p.comment || '—' }}</p>
              </div>
            </div>

            <div class="stack">
              <strong>Комментарии менеджеров</strong>
              <div v-if="!commentsFor(b.id).length" class="muted">Пока нет комментариев.</div>
              <div v-for="c in commentsFor(b.id)" :key="c.id" class="crm-line-card">
                <p style="margin: 0">{{ c.body }}</p>
                <p class="muted" style="margin: 0.2rem 0 0">{{ c.author?.fullName || c.author?.email }} · {{ c.createdAt }}</p>
              </div>
              <div class="row">
                <textarea
                  v-model="commentDrafts[b.id]"
                  class="textarea"
                  rows="2"
                  placeholder="Внутренний комментарий для менеджеров и админов"
                />
                <button type="button" class="btn btn-gradient" @click="addBookingComment(b.id)">Добавить комментарий</button>
              </div>
            </div>
          </div>

          <div v-if="editId === b.id" class="stack crm-edit-box">
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
            <div class="field" style="margin: 0">
              <label class="label">Заметки</label>
              <textarea v-model="formFor(b.id).note" class="textarea" rows="2" />
            </div>
            <button type="button" class="btn btn-gradient" @click="patch(b.id)">Сохранить</button>
          </div>
        </article>
      </div>

      <div class="grid grid-2" style="align-items: start; margin-top: 1rem">
        <article class="card stack">
          <div class="row-between">
            <h3 style="margin: 0">Задачи</h3>
            <div class="row" style="gap: 0.4rem">
              <button v-for="s in ['ALL', ...taskStatuses]" :key="s" type="button" class="btn btn-ghost" :disabled="taskFilter === s" @click="taskFilter = s">
                {{ s === 'ALL' ? 'Все' : taskStatusLabel(s) }}
              </button>
            </div>
          </div>

          <div v-if="canManageTasks" class="stack crm-edit-box">
            <input v-model="taskForm.title" class="input" placeholder="Название задачи" />
            <textarea v-model="taskForm.description" class="textarea" rows="2" placeholder="Описание (опционально)" />
            <div class="grid grid-2">
              <select v-model="taskForm.assigneeId" class="select">
                <option value="">Кому назначить</option>
                <option v-for="m in managers" :key="m.id" :value="String(m.id)">{{ m.fullName }}</option>
              </select>
              <input v-model="taskForm.bookingId" class="input" type="text" placeholder="UUID заявки (опционально)" />
            </div>
            <input v-model="taskForm.dueDate" class="input" type="date" />
            <button type="button" class="btn btn-gradient" @click="createTask">Создать задачу</button>
          </div>

          <div class="stack">
            <div v-for="t in filteredTasks" :key="t.id" class="crm-line-card">
              <strong>{{ t.title }}</strong>
              <p class="muted" style="margin: 0.2rem 0">Исполнитель: {{ t.assignee?.fullName }} · Срок: {{ t.dueDate || '—' }}</p>
              <p class="muted" style="margin: 0.2rem 0">
                Статус: {{ taskStatusLabel(t.status) }}
                <span v-if="t.bookingId"> · Заявка {{ t.bookingId }}</span>
              </p>
              <div class="row">
                <button
                  v-for="s in taskStatuses"
                  :key="`${t.id}-${s}`"
                  type="button"
                  class="btn btn-ghost"
                  :disabled="t.status === s"
                  @click="updateTaskStatus(t, s)"
                >
                  {{ taskStatusLabel(s) }}
                </button>
              </div>
            </div>
          </div>
        </article>

        <article class="card stack">
          <h3 style="margin: 0">Уведомления</h3>
          <div v-if="auth.isAdmin" class="stack crm-edit-box">
            <select v-model="notifyForm.recipientId" class="select">
              <option value="">Кому отправить</option>
              <option v-for="m in managers" :key="m.id" :value="String(m.id)">{{ m.fullName }}</option>
            </select>
            <select v-model="notifyForm.channel" class="select">
              <option value="IN_APP">IN_APP</option>
              <option value="EMAIL">EMAIL</option>
            </select>
            <p v-if="notifyForm.channel === 'EMAIL' && !emailConfigured" class="muted" style="margin: 0; font-size: 0.85rem">
              Resend не настроен — сервер отклонит EMAIL. Используйте IN_APP или задайте app.resend.api-key и app.mail.from.
            </p>
            <input v-model="notifyForm.subject" class="input" placeholder="Тема" />
            <textarea v-model="notifyForm.body" class="textarea" rows="2" placeholder="Текст уведомления" />
            <button type="button" class="btn btn-gradient" @click="sendNotification">Отправить</button>
          </div>
          <div class="stack">
            <div v-for="n in notifications" :key="n.id" class="crm-line-card">
              <strong>{{ n.subject }}</strong>
              <p class="muted" style="margin: 0.2rem 0">{{ n.channel }} · {{ n.recipient?.fullName }}</p>
              <p style="margin: 0.2rem 0">{{ n.body }}</p>
              <button v-if="!n.read" type="button" class="btn btn-ghost" @click="markRead(n.id)">Отметить как прочитанное</button>
            </div>
          </div>
        </article>
      </div>

      <article class="card stack" style="margin-top: 1rem">
        <h3 style="margin: 0">Ближайшие напоминания</h3>
        <div v-if="!reminders.length" class="muted">Срочных напоминаний нет.</div>
        <div v-for="r in reminders" :key="`${r.type}-${r.taskId || r.bookingId}`" class="crm-line-card">
          <div>{{ r.message }}</div>
          <div class="muted" v-if="r.dueDate">Срок: {{ r.dueDate }}</div>
        </div>
        <p v-if="auth.isAdmin" class="muted" style="margin: 0.75rem 0 0; font-size: 0.85rem">
          Аналитика продаж — в разделе «Админка».
        </p>
      </article>
    </div>
  </section>
</template>

<style scoped>
.crm-kpis {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 0.85rem;
}

.crm-kpi {
  padding: 0.95rem 1.1rem;
}

.crm-kpi strong {
  display: block;
  margin-top: 0.15rem;
  font-size: 1.4rem;
  line-height: 1.1;
}

  .crm-select {
    max-width: 220px;
  }

  .crm-tour-select {
    max-width: 280px;
  }

.crm-search {
  flex: 1;
  min-width: 260px;
}

.crm-id-wrap {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.crm-id {
  font-size: 0.75rem;
  color: #525252;
}

.crm-edit-box {
  padding: 0.75rem;
  border: 1px dashed #d4d4d4;
  border-radius: 14px;
}

.crm-line-card {
  padding: 0.65rem;
  border: 1px solid #e5e5e5;
  border-radius: 12px;
}

.grid-3 {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 0.75rem;
}

@media (max-width: 900px) {
  .crm-kpis {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .grid-3 {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .crm-kpis {
    grid-template-columns: 1fr;
  }

  .crm-search {
    min-width: 100%;
  }

  .crm-select {
    max-width: 100%;
  }

  .crm-id-wrap {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
