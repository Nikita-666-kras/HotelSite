<script setup>
import { onMounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import api from '../api/client'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const favorites = ref([])

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

async function removeFavorite(id) {
  try {
    await api.delete(`/api/favorites/tours/${id}`)
    await loadFavorites()
  } catch {
    /* ignore */
  }
}

onMounted(loadFavorites)
watch(
  () => auth.isAuthenticated,
  () => loadFavorites(),
)
</script>

<template>
  <div>
    <div class="page-hero">
      <div class="container">
        <h1>Избранное</h1>
        <p class="lead">Туры, которые ты отметил во «Вдохновении».</p>
      </div>
    </div>
    <section class="section" style="padding-top: 1rem">
      <div class="container stack" style="max-width: 560px">
        <article class="card">
          <template v-if="!auth.isAuthenticated">
            <p class="muted" style="margin: 0">
              <RouterLink to="/login">Войдите</RouterLink>
              — чтобы видеть избранное на всех устройствах.
            </p>
          </template>
          <template v-else-if="!favorites.length">
            <p class="muted" style="margin: 0">Пока пусто — отметьте туры на странице «Вдохновение».</p>
          </template>
          <ul v-else class="bento-fav-list">
            <li v-for="t in favorites" :key="t.id" class="bento-fav-item">
              <RouterLink class="bento-fav-link" :to="`/tours/${t.slug}`">{{ t.title }}</RouterLink>
              <span class="muted surface-ink">{{ t.destination }}</span>
              <button type="button" class="bento-fav-remove" aria-label="Убрать из избранного" @click="removeFavorite(t.id)">
                ×
              </button>
            </li>
          </ul>
        </article>
      </div>
    </section>
  </div>
</template>
