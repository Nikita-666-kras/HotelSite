<script setup>
import { reactive, onMounted, ref } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import api from '../api/client'
import DestinationAutocomplete from '../components/DestinationAutocomplete.vue'
import { TOUR_CATEGORIES, tourCategoryLabel } from '../utils/tourCategories'

const route = useRoute()
const categories = TOUR_CATEGORIES
const tours = ref([])
const error = ref('')

const filters = reactive({
  destination: '',
  category: '',
  dateFrom: '',
  dateTo: '',
  maxPrice: '',
  featured: false,
})

async function load() {
  error.value = ''
  try {
    const params = { ...filters }
    if (!params.category) delete params.category
    if (!params.dateFrom) delete params.dateFrom
    if (!params.dateTo) delete params.dateTo
    if (!params.maxPrice) delete params.maxPrice
    if (!params.featured) delete params.featured
    const { data } = await api.get('/api/tours/search', { params })
    tours.value = data
  } catch (e) {
    error.value = e.response?.data?.error || 'Не удалось загрузить туры'
  }
}

function syncFromRoute() {
  const d = route.query.destination
  const c = route.query.category
  if (typeof d === 'string') filters.destination = d
  if (typeof c === 'string' && categories.includes(c)) filters.category = c
}

onMounted(() => {
  syncFromRoute()
  load()
})
</script>

<template>
  <div>
    <div class="page-hero">
      <div class="container">
        <h1>Поиск туров</h1>
        <!-- <p class="lead">Фильтры под твой темп — от чилла до активного отдыха.</p> -->
      </div>
    </div>
    <section class="section" style="padding-top: 1.25rem">
      <div class="container stack">
        <form class="card" @submit.prevent="load">
        <div class="grid grid-2">
          <div class="field">
            <label class="label" for="dest">Направление</label>
            <DestinationAutocomplete
              v-model="filters.destination"
              input-id="dest"
              placeholder="Турция, Сочи…"
            />
          </div>
          <div class="field">
            <label class="label" for="cat">Категория</label>
            <select id="cat" v-model="filters.category" class="select">
              <option value="">Любая</option>
              <option v-for="c in categories" :key="c" :value="c">{{ tourCategoryLabel(c) }}</option>
            </select>
          </div>
          <div class="field">
            <label class="label" for="df">Дата с</label>
            <input id="df" v-model="filters.dateFrom" class="input" type="date" />
          </div>
          <div class="field">
            <label class="label" for="dt">Дата по</label>
            <input id="dt" v-model="filters.dateTo" class="input" type="date" />
          </div>
          <div class="field">
            <label class="label" for="price">Макс. цена</label>
            <input id="price" v-model="filters.maxPrice" class="input" type="number" min="0" step="1" />
          </div>
          <div class="field" style="align-self: end">
            <label class="label">
              <input v-model="filters.featured" type="checkbox" />
              Только рекомендуемые
            </label>
          </div>
        </div>
          <button class="btn btn-gradient" type="submit">Применить</button>
        </form>
        <p v-if="error" class="error">{{ error }}</p>
        <div class="grid grid-2">
          <article v-for="t in tours" :key="t.id" class="card">
            <div v-if="t.mediaUrls?.[0]" class="tour-list-cover">
              <img :src="t.mediaUrls[0]" :alt="t.title" loading="lazy" />
            </div>
            <div class="row-between">
              <h3 style="margin: 0">{{ t.title }}</h3>
              <span class="badge">{{ tourCategoryLabel(t.category) }}</span>
            </div>
            <p class="muted">{{ t.destination }}</p>
            <p class="muted">{{ t.startDate }} — {{ t.endDate }}</p>
            <p>{{ (t.description || '').slice(0, 160) }}{{ (t.description || '').length > 160 ? '…' : '' }}</p>
            <div class="row-between">
              <strong>{{ t.price }} ₽</strong>
              <RouterLink class="btn btn-gradient" style="text-decoration: none" :to="`/tours/${t.slug}`">Открыть</RouterLink>
            </div>
          </article>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.tour-list-cover {
  margin: -1.35rem -1.35rem 0.85rem;
  height: 170px;
  border-radius: 24px 24px 16px 16px;
  overflow: hidden;
}

.tour-list-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
}
</style>
