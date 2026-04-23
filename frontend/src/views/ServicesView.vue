<script setup>
import { reactive, ref, onMounted } from 'vue'
import api from '../api/client'
import ProductImageCarousel from '../components/ProductImageCarousel.vue'
import DestinationAutocomplete from '../components/DestinationAutocomplete.vue'

const types = [
  { value: '', label: 'Все' },
  { value: 'HOTEL', label: 'Отели' },
  { value: 'FLIGHT', label: 'Авиа' },
  { value: 'TRAIN', label: 'Ж/д' },
]

const filters = reactive({
  type: '',
  origin: '',
  destination: '',
  maxPrice: '',
})
const items = ref([])
const error = ref('')
const bookingId = ref(null)
const details = ref('')
const bookError = ref('')

async function load() {
  error.value = ''
  try {
    const params = { ...filters }
    if (!params.type) delete params.type
    if (!params.maxPrice) delete params.maxPrice
    if (!String(params.origin || '').trim()) delete params.origin
    if (!String(params.destination || '').trim()) delete params.destination
    const { data } = await api.get('/api/products/search', { params })
    items.value = data
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка поиска'
  }
}

async function book(id) {
  bookError.value = ''
  bookingId.value = null
  try {
    const { data } = await api.post('/api/products/bookings', { productId: id, details: details.value })
    bookingId.value = data.id
  } catch (e) {
    bookError.value = e.response?.data?.error || 'Нужна авторизация или ошибка сервера'
  }
}

onMounted(load)
</script>

<template>
  <div>
    <div class="page-hero">
      <div class="container">
        <h1>Отели, авиа и ж/д</h1>
        <p class="lead">Собери маршрут из отдельных услуг.</p>
      </div>
    </div>
    <section class="section" style="padding-top: 1.25rem">
      <div class="container stack">
        <form class="card" @submit.prevent="load">
        <div class="grid grid-2">
          <div class="field">
            <label class="label">Тип</label>
            <select v-model="filters.type" class="select">
              <option v-for="t in types" :key="t.value || 'all'" :value="t.value">{{ t.label }}</option>
            </select>
          </div>
          <div class="field">
            <label class="label">Макс. цена</label>
            <input v-model="filters.maxPrice" class="input" type="number" min="0" />
          </div>
          <div class="field">
            <label class="label" for="srv-origin">Откуда</label>
            <DestinationAutocomplete
              v-model="filters.origin"
              input-id="srv-origin"
              placeholder="Город или пункт"
            />
          </div>
          <div class="field">
            <label class="label" for="srv-dest">Куда</label>
            <DestinationAutocomplete
              v-model="filters.destination"
              input-id="srv-dest"
              placeholder="Город или пункт"
            />
          </div>
        </div>
          <button class="btn btn-gradient" type="submit">Найти</button>
        </form>
        <p v-if="error" class="error">{{ error }}</p>
        <p v-if="bookingId" class="badge">Заявка создана № {{ bookingId }}</p>
        <p v-if="bookError" class="error">{{ bookError }}</p>
        <div class="field">
          <label class="label">Комментарий к бронированию (пожелания по номеру, местам)</label>
          <textarea v-model="details" class="textarea" rows="2" />
        </div>
        <div class="grid grid-2 product-grid-wb">
          <article v-for="p in items" :key="p.id" class="card product-card-wb">
            <ProductImageCarousel :urls="p.imageUrls" :alt="p.name" />
            <div v-if="p.videoUrls?.length" class="product-card-wb__video">
              <video :src="p.videoUrls[0]" controls playsinline class="product-video" />
            </div>
            <div class="product-card-wb__body">
              <div class="row-between">
                <strong>{{ p.name }}</strong>
                <span class="badge">{{ p.type }}</span>
              </div>
              <p class="muted">{{ p.origin }} → {{ p.destination }}</p>
              <p v-if="p.departAt" class="muted">Отправление: {{ p.departAt }}</p>
              <p v-if="p.checkIn" class="muted">Заезд: {{ p.checkIn }} — {{ p.checkOut }}</p>
              <p>{{ p.description }}</p>
              <div class="row-between">
                <strong>{{ p.price }} ₽</strong>
                <button type="button" class="btn btn-gradient" @click="book(p.id)">Забронировать</button>
              </div>
            </div>
          </article>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.product-card-wb {
  padding: 0;
  overflow: hidden;
}

.product-card-wb__body {
  padding: 1rem 1.15rem 1.2rem;
}

.product-card-wb__video {
  padding: 0 0.75rem;
  margin-top: 0.35rem;
}

.product-video {
  width: 100%;
  max-height: 220px;
  border-radius: 12px;
  background: #000;
}
</style>
