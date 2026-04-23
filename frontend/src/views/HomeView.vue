<script setup>
import { onMounted, ref } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import api from '../api/client'
import DestinationAutocomplete from '../components/DestinationAutocomplete.vue'
import { tourCategoryLabel } from '../utils/tourCategories'

const router = useRouter()
const featured = ref([])
const allTours = ref([])
const heroQuery = ref('')

const styleChips = [
  { label: 'Всё включено', category: 'BEACH' },
  { label: 'Горы', category: 'SKI' },
  { label: 'Экскурсии', category: 'EXCURSION' },
  { label: 'С детьми', category: 'CITY' },
]

const activeChip = ref(null)
const recentList = ref([])

onMounted(async () => {
  try {
    const { data } = await api.get('/api/tours/search', { params: { featured: true } })
    featured.value = data
  } catch {
    featured.value = []
  }
  try {
    const { data } = await api.get('/api/tours/search')
    allTours.value = data.slice(0, 5)
  } catch {
    allTours.value = []
  }
  const src = featured.value.length ? featured.value : allTours.value
  recentList.value = src.slice(0, 3)
})

function goSearch(q) {
  const dest = (q || '').trim()
  router.push({ path: '/tours', query: dest ? { destination: dest } : {} })
}

function applyChip(chip) {
  activeChip.value = activeChip.value === chip.category ? null : chip.category
  router.push({
    path: '/tours',
    query: activeChip.value ? { category: activeChip.value } : {},
  })
}
</script>

<template>
  <div class="bento-container">
    <div class="bento-home">
      <article class="bento-hero-card">
        <div class="bento-hero-visual">
          <form class="bento-hero-floating-search" @submit.prevent="goSearch(heroQuery)">
            <svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" style="color: #737373; flex-shrink: 0">
              <circle cx="11" cy="11" r="7" />
              <path d="M21 21l-4.35-4.35" />
            </svg>
            <DestinationAutocomplete v-model="heroQuery" variant="hero" placeholder="Куда дальше? Город или страна" />
            <button type="submit" class="btn btn-gradient" style="padding: 0.5rem 1.1rem; font-size: 0.8rem">Найти</button>
          </form>
        </div>
        <div class="bento-hero-copy">
          <h1>TripWave — путешествия в твоём ритме</h1>
          <p>Туры и билеты для твоего идеального отдыха в одном сервисе</p>
        </div>
      </article>

      <div class="bento-side-stack">
        <div class="bento-panel">
          <p class="bento-panel-title">Недавно в каталоге</p>
          <template v-if="recentList.length">
            <div v-for="(t, i) in recentList" :key="t.id" class="bento-recent-item">
              <span class="bento-recent-num">{{ String(i + 1).padStart(2, '0') }}</span>
              <div class="bento-recent-thumb" :style="t.mediaUrls?.[0] ? `background-image:url(${t.mediaUrls[0]})` : ''" />
              <div class="bento-recent-text">
                <RouterLink :to="`/tours/${t.slug}`">
                  <strong>{{ t.title }}</strong>
                </RouterLink>
                <span>{{ t.destination }}</span>
              </div>
            </div>
          </template>
          <p v-else class="muted surface-ink" style="margin: 0; font-size: 0.85rem">Загрузите туры с сервера.</p>
        </div>

        <div class="bento-panel">
          <p class="bento-panel-title">Выбери стиль</p>
          <div class="chip-row">
            <button
              v-for="c in styleChips"
              :key="c.category"
              type="button"
              class="chip"
              :class="{ 'is-active': activeChip === c.category }"
              @click="applyChip(c)"
            >
              {{ c.label }}
            </button>
          </div>
        </div>

        <div class="bento-panel">
          <p class="bento-panel-title">Вдохновение</p>
          <div class="bento-inspire">
            <div class="bento-inspire-thumbs" aria-hidden="true">
              <img src="/america_!.jpg" alt="" loading="lazy" />
              <img src="/bali_1.jpg" alt="" loading="lazy" />
              <img src="/italia_1.jpg" alt="" loading="lazy" />
            </div>
            <p>Выбери свой идеальный long weekend</p>
          </div>
          <RouterLink class="btn btn-outline-dark" to="/inspire" style="margin-top: 1rem; text-decoration: none; width: 100%; display: block; text-align: center">
            Открыть вдохновение
          </RouterLink>
        </div>
      </div>
    </div>

    <section class="section section-tight">
      <h2 class="section-title">Подборка недели</h2>
      <p class="section-lead">Рекомендуемые направления из каталога.</p>
      <div v-if="featured.length" class="grid grid-2">
        <article v-for="t in featured.slice(0, 2)" :key="t.id" class="card feature-card">
          <div v-if="t.mediaUrls?.[0]" class="home-tour-cover">
            <img :src="t.mediaUrls[0]" :alt="t.title" loading="lazy" />
          </div>
          <div class="row-between">
            <span class="badge">{{ tourCategoryLabel(t.category) }}</span>
            <strong class="surface-ink">от {{ t.price }} ₽</strong>
          </div>
          <h3>{{ t.title }}</h3>
          <p class="muted surface-ink">{{ t.destination }} · {{ t.startDate }}</p>
          <RouterLink class="btn btn-gradient" style="margin-top: 0.75rem; text-decoration: none; width: fit-content" :to="`/tours/${t.slug}`">
            Подробнее
          </RouterLink>
        </article>
      </div>
      <p v-else class="muted">Нет рекомендуемых туров в API.</p>
    </section>

    <section class="section" style="padding-top: 0">
      <h2 class="section-title">Почему TripWave</h2>
      <!-- <p class="section-lead">Меньше кликов — больше ясности.</p> -->
      <div class="grid grid-3">
        <article class="card feature-card">
          <div class="icon-ring">⚡</div>
          <h3>Быстро</h3>
          <p>Заявка и данные участников в одной форме.</p>
        </article>
        <article class="card feature-card">
          <div class="icon-ring">◇</div>
          <h3>Гибко</h3>
          <p>Туры или отдельно отели, авиа и ж/д.</p>
        </article>
        <article class="card feature-card">
          <div class="icon-ring">◎</div>
          <h3>Рядом</h3>
          <p>Чат с поддержкой без ожидания на линии.</p>
        </article>
      </div>
    </section>
  </div>
</template>
