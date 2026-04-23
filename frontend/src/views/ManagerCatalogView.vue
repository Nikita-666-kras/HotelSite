<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { RouterLink } from 'vue-router'
import api from '../api/client'
import { TOUR_CATEGORIES, tourCategoryLabel } from '../utils/tourCategories'

const tab = ref('tours')
const error = ref('')
const tours = ref([])
const products = ref([])
const pendingTourFiles = ref([])
const pendingTourPreviewUrls = ref([])

const tourCategories = TOUR_CATEGORIES
const productTypes = [
  { value: 'HOTEL', label: 'Отель' },
  { value: 'FLIGHT', label: 'Авиа' },
  { value: 'TRAIN', label: 'Ж/д' },
]

const tourForm = reactive({
  id: null,
  title: '',
  slug: '',
  description: '',
  destination: '',
  startDate: '',
  endDate: '',
  price: '',
  maxParticipants: 20,
  category: 'EXCURSION',
  featured: false,
  hotels: [],
  carriers: [],
  tickets: [],
  excursions: [],
  layovers: [],
  enableFlightRegistration: false,
  enableHotelRegistration: false,
  enableRailRegistration: false,
})

const tourMetaDraft = reactive({
  hotel: '',
  carrier: '',
  ticket: '',
  excursion: '',
  layover: '',
})

const productForm = reactive({
  id: null,
  type: 'FLIGHT',
  name: '',
  description: '',
  origin: '',
  destination: '',
  departAt: '',
  arriveAt: '',
  checkIn: '',
  checkOut: '',
  price: '',
  stars: '',
  carrier: '',
  externalRef: '',
})

const editingTour = computed(() => tourForm.id != null)
const editingProduct = computed(() => productForm.id != null)

function resetTourForm() {
  tourForm.id = null
  tourForm.title = ''
  tourForm.slug = ''
  tourForm.description = ''
  tourForm.destination = ''
  tourForm.startDate = ''
  tourForm.endDate = ''
  tourForm.price = ''
  tourForm.maxParticipants = 20
  tourForm.category = 'EXCURSION'
  tourForm.featured = false
  tourForm.hotels = []
  tourForm.carriers = []
  tourForm.tickets = []
  tourForm.excursions = []
  tourForm.layovers = []
  tourForm.enableFlightRegistration = false
  tourForm.enableHotelRegistration = false
  tourForm.enableRailRegistration = false
  tourMetaDraft.hotel = ''
  tourMetaDraft.carrier = ''
  tourMetaDraft.ticket = ''
  tourMetaDraft.excursion = ''
  tourMetaDraft.layover = ''
  clearPendingTourFiles()
}

function resetProductForm() {
  productForm.id = null
  productForm.type = 'FLIGHT'
  productForm.name = ''
  productForm.description = ''
  productForm.origin = ''
  productForm.destination = ''
  productForm.departAt = ''
  productForm.arriveAt = ''
  productForm.checkIn = ''
  productForm.checkOut = ''
  productForm.price = ''
  productForm.stars = ''
  productForm.carrier = ''
  productForm.externalRef = ''
}

function editTour(t) {
  tourForm.id = t.id
  tourForm.title = t.title
  tourForm.slug = t.slug
  tourForm.description = t.description
  tourForm.destination = t.destination
  tourForm.startDate = t.startDate
  tourForm.endDate = t.endDate
  tourForm.price = String(t.price)
  tourForm.maxParticipants = t.maxParticipants
  tourForm.category = t.category
  tourForm.featured = t.featured
  tourForm.hotels = Array.isArray(t.hotels) ? [...t.hotels] : []
  tourForm.carriers = Array.isArray(t.carriers) ? [...t.carriers] : []
  tourForm.tickets = Array.isArray(t.tickets) ? [...t.tickets] : []
  tourForm.excursions = Array.isArray(t.excursions) ? [...t.excursions] : []
  tourForm.layovers = Array.isArray(t.layovers) ? [...t.layovers] : []
  tourForm.enableFlightRegistration = !!t.enableFlightRegistration
  tourForm.enableHotelRegistration = !!t.enableHotelRegistration
  tourForm.enableRailRegistration = !!t.enableRailRegistration
}

function addTourMetaItem(field, key) {
  const raw = tourMetaDraft[key]
  const value = raw ? raw.trim() : ''
  if (!value) return
  if (!tourForm[field].includes(value)) {
    tourForm[field].push(value)
  }
  tourMetaDraft[key] = ''
}

function removeTourMetaItem(field, idx) {
  tourForm[field].splice(idx, 1)
}

function editProduct(p) {
  productForm.id = p.id
  productForm.type = p.type
  productForm.name = p.name
  productForm.description = p.description || ''
  productForm.origin = p.origin
  productForm.destination = p.destination
  productForm.departAt = p.departAt ? p.departAt.slice(0, 16) : ''
  productForm.arriveAt = p.arriveAt ? p.arriveAt.slice(0, 16) : ''
  productForm.checkIn = p.checkIn || ''
  productForm.checkOut = p.checkOut || ''
  productForm.price = String(p.price)
  productForm.stars = p.stars != null ? String(p.stars) : ''
  productForm.carrier = p.carrier || ''
  productForm.externalRef = p.externalRef || ''
}

async function loadTours() {
  error.value = ''
  try {
    const { data } = await api.get('/api/manager/tours')
    tours.value = data
  } catch (e) {
    error.value = e.response?.data?.error || 'Нет доступа'
  }
}

async function loadProducts() {
  error.value = ''
  try {
    const { data } = await api.get('/api/manager/products')
    products.value = data
  } catch (e) {
    error.value = e.response?.data?.error || 'Нет доступа'
  }
}

async function saveTour() {
  error.value = ''
  const body = {
    title: tourForm.title,
    slug: tourForm.slug || null,
    description: tourForm.description,
    destination: tourForm.destination,
    startDate: tourForm.startDate,
    endDate: tourForm.endDate,
    price: Number(tourForm.price),
    maxParticipants: Number(tourForm.maxParticipants),
    category: tourForm.category,
    featured: tourForm.featured,
    hotels: tourForm.hotels,
    carriers: tourForm.carriers,
    tickets: tourForm.tickets,
    excursions: tourForm.excursions,
    layovers: tourForm.layovers,
    enableFlightRegistration: tourForm.enableFlightRegistration,
    enableHotelRegistration: tourForm.enableHotelRegistration,
    enableRailRegistration: tourForm.enableRailRegistration,
  }
  try {
    if (tourForm.id) {
      await api.put(`/api/manager/tours/${tourForm.id}`, body)
    } else {
      const { data: created } = await api.post('/api/manager/tours', body)
      await uploadPendingTourFiles(created.id)
    }
    resetTourForm()
    await loadTours()
  } catch (e) {
    error.value = e.response?.data?.error || e.response?.data?.message || 'Ошибка сохранения тура'
  }
}

function onTourFilesSelected(ev) {
  const files = Array.from(ev.target.files || [])
  ev.target.value = ''
  if (!files.length) return
  files.forEach((file) => {
    pendingTourFiles.value.push(file)
    pendingTourPreviewUrls.value.push(URL.createObjectURL(file))
  })
}

function removePendingTourFile(idx) {
  const url = pendingTourPreviewUrls.value[idx]
  if (url) URL.revokeObjectURL(url)
  pendingTourPreviewUrls.value.splice(idx, 1)
  pendingTourFiles.value.splice(idx, 1)
}

function clearPendingTourFiles() {
  pendingTourPreviewUrls.value.forEach((u) => URL.revokeObjectURL(u))
  pendingTourPreviewUrls.value = []
  pendingTourFiles.value = []
}

async function uploadPendingTourFiles(tourId) {
  for (const file of pendingTourFiles.value) {
    const fd = new FormData()
    fd.append('file', file)
    await api.post(`/api/manager/tours/${tourId}/media`, fd)
  }
}

async function saveProduct() {
  error.value = ''
  const body = {
    type: productForm.type,
    name: productForm.name,
    description: productForm.description || null,
    origin: productForm.origin,
    destination: productForm.destination,
    departAt: productForm.departAt || null,
    arriveAt: productForm.arriveAt || null,
    checkIn: productForm.checkIn || null,
    checkOut: productForm.checkOut || null,
    price: Number(productForm.price),
    stars: productForm.stars ? Number(productForm.stars) : null,
    carrier: productForm.carrier || null,
    externalRef: productForm.externalRef || null,
  }
  try {
    if (productForm.id) {
      await api.put(`/api/manager/products/${productForm.id}`, body)
    } else {
      await api.post('/api/manager/products', body)
    }
    resetProductForm()
    await loadProducts()
  } catch (e) {
    error.value = e.response?.data?.error || e.response?.data?.message || 'Ошибка сохранения услуги'
  }
}

async function deleteTour(id) {
  if (!confirm('Удалить тур и все медиа?')) return
  error.value = ''
  try {
    await api.delete(`/api/manager/tours/${id}`)
    if (tourForm.id === id) resetTourForm()
    await loadTours()
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка удаления'
  }
}

async function deleteProduct(id) {
  if (!confirm('Удалить услугу и медиа в хранилище?')) return
  error.value = ''
  try {
    await api.delete(`/api/manager/products/${id}`)
    if (productForm.id === id) resetProductForm()
    await loadProducts()
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка удаления'
  }
}

async function uploadTourMedia(tourId, ev) {
  const file = ev.target.files?.[0]
  ev.target.value = ''
  if (!file) return
  error.value = ''
  const fd = new FormData()
  fd.append('file', file)
  try {
    await api.post(`/api/manager/tours/${tourId}/media`, fd)
    await loadTours()
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка загрузки файла'
  }
}

async function removeTourMedia(tourId, objectKey) {
  error.value = ''
  try {
    await api.delete(`/api/manager/tours/${tourId}/media`, { params: { objectKey } })
    await loadTours()
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка удаления'
  }
}

async function uploadProductImage(id, ev) {
  const file = ev.target.files?.[0]
  ev.target.value = ''
  if (!file) return
  const fd = new FormData()
  fd.append('file', file)
  error.value = ''
  try {
    await api.post(`/api/manager/products/${id}/media/image`, fd)
    await loadProducts()
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка загрузки'
  }
}

async function uploadProductVideo(id, ev) {
  const file = ev.target.files?.[0]
  ev.target.value = ''
  if (!file) return
  const fd = new FormData()
  fd.append('file', file)
  error.value = ''
  try {
    await api.post(`/api/manager/products/${id}/media/video`, fd)
    await loadProducts()
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка загрузки'
  }
}

async function removeProductImage(id, key) {
  error.value = ''
  try {
    await api.delete(`/api/manager/products/${id}/media/image`, { params: { objectKey: key } })
    await loadProducts()
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка'
  }
}

async function removeProductVideo(id, key) {
  error.value = ''
  try {
    await api.delete(`/api/manager/products/${id}/media/video`, { params: { objectKey: key } })
    await loadProducts()
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка'
  }
}

onMounted(() => {
  loadTours()
  loadProducts()
})

function isVideoKey(key) {
  if (!key) return false
  return /\.(mp4|webm|mov|mkv|avi)$/i.test(key)
}
</script>

<template>
  <section class="section">
    <div class="container stack catalog-wrap">
      <div class="page-hero" style="border: none; padding: 0 0 0.5rem; margin: 0">
        <h1 style="font-size: 1.85rem">Каталог · менеджер</h1>
        <p class="lead">Туры и отели/билеты: создание, правка, фото и видео в хранилище.</p>
        <p class="muted">
          <RouterLink to="/manager/crm">← CRM заявки</RouterLink>
        </p>
      </div>

      <div class="row catalog-tabs">
        <button type="button" class="btn" :class="tab === 'tours' ? 'btn-gradient' : 'btn-ghost'" @click="tab = 'tours'">
          Добавление туров
        </button>
        <button
          type="button"
          class="btn"
          :class="tab === 'products' ? 'btn-gradient' : 'btn-ghost'"
          @click="tab = 'products'"
        >
          Добавление билетов и отелей
        </button>
      </div>

      <p v-if="error" class="error">{{ error }}</p>

      <div v-show="tab === 'tours'" class="stack">
        <article class="card catalog-form">
          <h2>{{ editingTour ? 'Редактирование тура' : 'Новый тур' }}</h2>
          <div class="grid grid-2">
            <div class="field">
              <label class="label">Название</label>
              <input v-model="tourForm.title" class="input" required />
            </div>
            <div class="field">
              <label class="label">Slug (необязательно)</label>
              <input v-model="tourForm.slug" class="input" placeholder="авто из названия" />
            </div>
            <div class="field" style="grid-column: 1 / -1">
              <label class="label">Описание</label>
              <textarea v-model="tourForm.description" class="textarea" rows="3" required />
            </div>
            <div class="field">
              <label class="label">Направление</label>
              <input v-model="tourForm.destination" class="input" required />
            </div>
            <div class="field">
              <label class="label">Категория</label>
              <select v-model="tourForm.category" class="select">
                <option v-for="c in tourCategories" :key="c" :value="c">{{ tourCategoryLabel(c) }}</option>
              </select>
            </div>
            <div class="field">
              <label class="label">Начало</label>
              <input v-model="tourForm.startDate" class="input" type="date" required />
            </div>
            <div class="field">
              <label class="label">Конец</label>
              <input v-model="tourForm.endDate" class="input" type="date" required />
            </div>
            <div class="field">
              <label class="label">Цена</label>
              <input v-model="tourForm.price" class="input" type="number" min="0" step="0.01" required />
            </div>
            <div class="field">
              <label class="label">Макс. участников</label>
              <input v-model.number="tourForm.maxParticipants" class="input" type="number" min="1" />
            </div>
            <div class="field">
              <label class="label">
                <input v-model="tourForm.featured" type="checkbox" />
                Избранный на главной
              </label>
            </div>
            <div class="field" style="grid-column: 1 / -1">
              <label class="label">Доступные регистрации в заявке</label>
              <div class="row" style="gap: 1rem; flex-wrap: wrap">
                <label class="label"><input v-model="tourForm.enableFlightRegistration" type="checkbox" /> Рейс</label>
                <label class="label"><input v-model="tourForm.enableHotelRegistration" type="checkbox" /> Отель</label>
                <label class="label"><input v-model="tourForm.enableRailRegistration" type="checkbox" /> Ж/д</label>
              </div>
            </div>
            <div class="field" style="grid-column: 1 / -1">
              <label class="label">Отели (можно несколько)</label>
              <div class="row">
                <input
                  v-model="tourMetaDraft.hotel"
                  class="input"
                  placeholder="Например: Hilton Garden Inn"
                  @keydown.enter.prevent="addTourMetaItem('hotels', 'hotel')"
                />
                <button type="button" class="btn btn-outline-dark" @click="addTourMetaItem('hotels', 'hotel')">Добавить</button>
              </div>
              <div v-if="tourForm.hotels.length" class="row catalog-tags">
                <span v-for="(hotel, idx) in tourForm.hotels" :key="'hotel-' + idx" class="badge">
                  {{ hotel }}
                  <button type="button" class="btn btn-ghost catalog-tag-rm" @click="removeTourMetaItem('hotels', idx)">×</button>
                </span>
              </div>
            </div>
            <div class="field" style="grid-column: 1 / -1">
              <label class="label">Перевозчики (можно несколько)</label>
              <div class="row">
                <input
                  v-model="tourMetaDraft.carrier"
                  class="input"
                  placeholder="Например: Аэрофлот / РЖД"
                  @keydown.enter.prevent="addTourMetaItem('carriers', 'carrier')"
                />
                <button type="button" class="btn btn-outline-dark" @click="addTourMetaItem('carriers', 'carrier')">Добавить</button>
              </div>
              <div v-if="tourForm.carriers.length" class="row catalog-tags">
                <span v-for="(carrier, idx) in tourForm.carriers" :key="'carrier-' + idx" class="badge">
                  {{ carrier }}
                  <button type="button" class="btn btn-ghost catalog-tag-rm" @click="removeTourMetaItem('carriers', idx)">×</button>
                </span>
              </div>
            </div>
            <div class="field" style="grid-column: 1 / -1">
              <label class="label">Билеты (можно несколько)</label>
              <div class="row">
                <input
                  v-model="tourMetaDraft.ticket"
                  class="input"
                  placeholder="Например: SU214 Москва → Стамбул"
                  @keydown.enter.prevent="addTourMetaItem('tickets', 'ticket')"
                />
                <button type="button" class="btn btn-outline-dark" @click="addTourMetaItem('tickets', 'ticket')">Добавить</button>
              </div>
              <div v-if="tourForm.tickets.length" class="row catalog-tags">
                <span v-for="(ticket, idx) in tourForm.tickets" :key="'ticket-' + idx" class="badge">
                  {{ ticket }}
                  <button type="button" class="btn btn-ghost catalog-tag-rm" @click="removeTourMetaItem('tickets', idx)">×</button>
                </span>
              </div>
            </div>
            <div class="field" style="grid-column: 1 / -1">
              <label class="label">Экскурсии (можно несколько)</label>
              <div class="row">
                <input
                  v-model="tourMetaDraft.excursion"
                  class="input"
                  placeholder="Например: Обзорная экскурсия по городу"
                  @keydown.enter.prevent="addTourMetaItem('excursions', 'excursion')"
                />
                <button
                  type="button"
                  class="btn btn-outline-dark"
                  @click="addTourMetaItem('excursions', 'excursion')"
                >
                  Добавить
                </button>
              </div>
              <div v-if="tourForm.excursions.length" class="row catalog-tags">
                <span v-for="(excursion, idx) in tourForm.excursions" :key="'excursion-' + idx" class="badge">
                  {{ excursion }}
                  <button
                    type="button"
                    class="btn btn-ghost catalog-tag-rm"
                    @click="removeTourMetaItem('excursions', idx)"
                  >
                    ×
                  </button>
                </span>
              </div>
            </div>
            <div class="field" style="grid-column: 1 / -1">
              <label class="label">Пересадки</label>
              <div class="row">
                <input
                  v-model="tourMetaDraft.layover"
                  class="input"
                  placeholder="Например: Пересадка в Стамбуле 2ч"
                  @keydown.enter.prevent="addTourMetaItem('layovers', 'layover')"
                />
                <button type="button" class="btn btn-outline-dark" @click="addTourMetaItem('layovers', 'layover')">Добавить</button>
              </div>
              <div v-if="tourForm.layovers.length" class="row catalog-tags">
                <span v-for="(layover, idx) in tourForm.layovers" :key="'layover-' + idx" class="badge">
                  {{ layover }}
                  <button type="button" class="btn btn-ghost catalog-tag-rm" @click="removeTourMetaItem('layovers', idx)">×</button>
                </span>
              </div>
            </div>
            <div v-if="!editingTour" class="field" style="grid-column: 1 / -1">
              <label class="label">Картинки/видео для нового тура</label>
              <label class="btn btn-outline-dark catalog-add">
                + добавить файлы
                <input type="file" accept="image/*,video/*" multiple hidden @change="onTourFilesSelected" />
              </label>
              <div v-if="pendingTourFiles.length" class="catalog-media pending-media">
                <div v-for="(url, idx) in pendingTourPreviewUrls" :key="'pending-' + idx" class="catalog-thumb">
                  <img
                    v-if="pendingTourFiles[idx] && pendingTourFiles[idx].type.startsWith('image/')"
                    :src="url"
                    alt=""
                  />
                  <video v-else :src="url" controls muted playsinline />
                  <button type="button" class="btn btn-ghost catalog-rm" @click="removePendingTourFile(idx)">×</button>
                </div>
              </div>
              <p v-if="pendingTourFiles.length" class="muted" style="margin: 0.4rem 0 0">
                {{ pendingTourFiles.length }} файл(ов) будут загружены после создания тура.
              </p>
            </div>
          </div>
          <div class="row" style="margin-top: 0.75rem">
            <button type="button" class="btn btn-gradient" @click="saveTour">
              {{ editingTour ? 'Сохранить тур' : 'Добавить тур' }}
            </button>
            <button v-if="editingTour" type="button" class="btn btn-ghost" @click="resetTourForm">Отмена</button>
          </div>
        </article>

        <div class="stack">
          <article v-for="t in tours" :key="t.id" class="card catalog-item">
            <div class="row-between catalog-item-head">
              <div>
                <strong>{{ t.title }}</strong>
                <span class="badge">{{ tourCategoryLabel(t.category) }}</span>
                <p class="muted" style="margin: 0.25rem 0 0">
                  {{ t.destination }} · {{ t.price }} ₽ ·
                  <RouterLink :to="`/tours/${t.slug}`">на сайте</RouterLink>
                </p>
              </div>
              <div class="row">
                <button type="button" class="btn btn-ghost" @click="editTour(t)">Править</button>
                <button type="button" class="btn btn-ghost" style="color: var(--danger)" @click="deleteTour(t.id)">
                  Удалить
                </button>
              </div>
            </div>
            <div class="catalog-media">
              <div v-for="(url, idx) in t.mediaUrls" :key="idx" class="catalog-thumb">
                <video v-if="isVideoKey(t.mediaObjectKeys[idx])" :src="url" controls muted playsinline />
                <img v-else :src="url" alt="" />
                <button
                  type="button"
                  class="btn btn-ghost catalog-rm"
                  title="Удалить"
                  @click="removeTourMedia(t.id, t.mediaObjectKeys[idx])"
                >
                  ×
                </button>
              </div>
              <label class="btn btn-outline-dark catalog-add">
                + фото/видео
                <input type="file" accept="image/*,video/*" hidden @change="uploadTourMedia(t.id, $event)" />
              </label>
            </div>
          </article>
        </div>
      </div>

      <div v-show="tab === 'products'" class="stack">
        <article class="card catalog-form">
          <h2>{{ editingProduct ? 'Редактирование услуги' : 'Новая услуга' }}</h2>
          <div class="grid grid-2">
            <div class="field">
              <label class="label">Тип</label>
              <select v-model="productForm.type" class="select">
                <option v-for="pt in productTypes" :key="pt.value" :value="pt.value">{{ pt.label }}</option>
              </select>
            </div>
            <div class="field">
              <label class="label">Название</label>
              <input v-model="productForm.name" class="input" required />
            </div>
            <div class="field" style="grid-column: 1 / -1">
              <label class="label">Описание</label>
              <textarea v-model="productForm.description" class="textarea" rows="2" />
            </div>
            <div class="field">
              <label class="label">Откуда</label>
              <input v-model="productForm.origin" class="input" required />
            </div>
            <div class="field">
              <label class="label">Куда</label>
              <input v-model="productForm.destination" class="input" required />
            </div>
            <template v-if="productForm.type === 'FLIGHT' || productForm.type === 'TRAIN'">
              <div class="field">
                <label class="label">Отправление</label>
                <input v-model="productForm.departAt" class="input" type="datetime-local" />
              </div>
              <div class="field">
                <label class="label">Прибытие</label>
                <input v-model="productForm.arriveAt" class="input" type="datetime-local" />
              </div>
            </template>
            <template v-if="productForm.type === 'HOTEL'">
              <div class="field">
                <label class="label">Заезд</label>
                <input v-model="productForm.checkIn" class="input" type="date" />
              </div>
              <div class="field">
                <label class="label">Выезд</label>
                <input v-model="productForm.checkOut" class="input" type="date" />
              </div>
              <div class="field">
                <label class="label">Звёзды</label>
                <input v-model="productForm.stars" class="input" type="number" min="1" max="5" />
              </div>
            </template>
            <div class="field">
              <label class="label">Цена</label>
              <input v-model="productForm.price" class="input" type="number" min="0" step="0.01" required />
            </div>
            <div class="field">
              <label class="label">Перевозчик</label>
              <input v-model="productForm.carrier" class="input" />
            </div>
            <div class="field">
              <label class="label">Внешний номер (PNR)</label>
              <input v-model="productForm.externalRef" class="input" />
            </div>
          </div>
          <div class="row" style="margin-top: 0.75rem">
            <button type="button" class="btn btn-gradient" @click="saveProduct">Сохранить услугу</button>
            <button v-if="editingProduct" type="button" class="btn btn-ghost" @click="resetProductForm">Отмена</button>
          </div>
        </article>

        <div class="stack">
          <article v-for="p in products" :key="p.id" class="card catalog-item">
            <div class="row-between catalog-item-head">
              <div>
                <strong>{{ p.name }}</strong>
                <span class="badge">{{ p.type }}</span>
                <p class="muted" style="margin: 0.25rem 0 0">{{ p.origin }} → {{ p.destination }} · {{ p.price }} ₽</p>
              </div>
              <div class="row">
                <button type="button" class="btn btn-ghost" @click="editProduct(p)">Править</button>
                <button type="button" class="btn btn-ghost" style="color: var(--danger)" @click="deleteProduct(p.id)">
                  Удалить
                </button>
              </div>
            </div>
            <div class="catalog-media">
              <div v-for="(url, idx) in p.imageUrls" :key="'i' + idx" class="catalog-thumb">
                <img :src="url" alt="" />
                <button
                  type="button"
                  class="btn btn-ghost catalog-rm"
                  @click="removeProductImage(p.id, p.imageObjectKeys[idx])"
                >
                  ×
                </button>
              </div>
              <div v-for="(url, idx) in p.videoUrls" :key="'v' + idx" class="catalog-thumb">
                <video :src="url" controls muted playsinline />
                <button
                  type="button"
                  class="btn btn-ghost catalog-rm"
                  @click="removeProductVideo(p.id, p.videoObjectKeys[idx])"
                >
                  ×
                </button>
              </div>
              <label class="btn btn-outline-dark catalog-add">
                + фото
                <input type="file" accept="image/*" hidden @change="uploadProductImage(p.id, $event)" />
              </label>
              <label class="btn btn-outline-dark catalog-add">
                + видео
                <input type="file" accept="video/*" hidden @change="uploadProductVideo(p.id, $event)" />
              </label>
            </div>
          </article>
        </div>
      </div>
    </div>
  </section>
</template>

<style scoped>
.catalog-wrap {
  max-width: 900px;
}
.catalog-tabs {
  gap: 0.5rem;
  flex-wrap: wrap;
}
.catalog-form h2 {
  margin: 0 0 0.75rem;
  font-size: 1.1rem;
}
.catalog-item-head {
  align-items: flex-start;
  gap: 0.75rem;
  margin-bottom: 0.75rem;
}
.catalog-media {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
  align-items: flex-start;
}
.catalog-thumb {
  position: relative;
  width: 120px;
  height: 80px;
  border-radius: 12px;
  overflow: hidden;
  background: var(--surface-soft);
}
.catalog-thumb img,
.catalog-thumb video {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.catalog-rm {
  position: absolute;
  top: 2px;
  right: 2px;
  min-width: auto;
  padding: 0.1rem 0.35rem;
  line-height: 1;
  background: rgba(255, 255, 255, 0.9);
}
.catalog-add {
  cursor: pointer;
  align-self: center;
  font-size: 0.85rem;
}

.pending-media {
  margin-top: 0.6rem;
}

.catalog-tags {
  margin-top: 0.5rem;
  gap: 0.4rem;
  flex-wrap: wrap;
}

.catalog-tag-rm {
  min-width: auto;
  padding: 0 0.25rem;
  margin-left: 0.25rem;
  line-height: 1;
}
</style>
