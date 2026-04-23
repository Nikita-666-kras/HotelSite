<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import api from '../api/client'
import SwipeTourDeck from '../components/SwipeTourDeck.vue'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const allTours = ref([])
const favorites = ref([])
const deckReady = ref(false)

const favoriteIdSet = computed(() => new Set(favorites.value.map((t) => t.id)))

const swipePool = computed(() => allTours.value.filter((t) => t && !favoriteIdSet.value.has(t.id)))

async function loadFavorites() {
  if (!auth.isAuthenticated) {
    favorites.value = []
    return
  }
  try {
    const { data } = await api.get('/api/favorites/tours')
    favorites.value = data
  } catch {
    favorites.value = []
  }
}

onMounted(async () => {
  try {
    const { data } = await api.get('/api/tours/search')
    allTours.value = data
  } catch {
    allTours.value = []
  }
  await loadFavorites()
  deckReady.value = true
})

watch(
  () => auth.isAuthenticated,
  () => loadFavorites(),
)
</script>

<template>
  <div>
    <div class="page-hero">
      <div class="container">
        <h1>Вдохновение</h1>
        <p class="lead">Свайпай: вправо — в избранное, влево — пропустить.</p>
      </div>
    </div>
    <section class="section inspire-deck-section">
      <div class="container inspire-deck-wrap">
        <article v-if="deckReady" class="card inspire-deck-card">
          <SwipeTourDeck :pool="swipePool" @favorites-changed="loadFavorites" />
        </article>
        <p v-else class="muted inspire-deck-loading">Загрузка туров…</p>
      </div>
    </section>
  </div>
</template>

<style scoped>
.inspire-deck-section {
  padding-top: 1rem;
}

.inspire-deck-wrap {
  max-width: min(440px, 100%);
}

.inspire-deck-card {
  padding: 1rem 1rem 1.35rem;
}

.inspire-deck-loading {
  text-align: center;
  margin: 2rem 0;
}
</style>
