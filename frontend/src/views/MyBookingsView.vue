<script setup>
import { onMounted, ref } from 'vue'
import api from '../api/client'

const items = ref([])
const error = ref('')

onMounted(async () => {
  try {
    const { data } = await api.get('/api/bookings')
    items.value = data
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка загрузки'
  }
})
</script>

<template>
  <section class="section">
    <div class="container stack">
      <div class="page-hero" style="border: none; padding: 0 0 1rem; margin: 0">
        <h1 style="font-size: 1.85rem">Мои заявки</h1>
        <p class="lead">Статусы и состав группы — в одном месте.</p>
      </div>
      <p v-if="error" class="error">{{ error }}</p>
      <div class="stack">
        <article v-for="b in items" :key="b.id" class="card">
          <div class="row-between">
            <div>
              <strong>{{ b.tour.title }}</strong>
              <p class="muted" style="margin: 0.25rem 0 0">{{ b.tour.destination }}</p>
            </div>
            <span class="badge">{{ b.status }}</span>
          </div>
          <p class="muted">Телефон: {{ b.contactPhone }}</p>
          <p>Участников: {{ b.participants.length }}</p>
          <ul class="muted" style="margin: 0; padding-left: 1.2rem">
            <li v-for="p in b.participants" :key="p.id">
              {{ p.lastName }} {{ p.firstName }}
              <template v-if="p.child"> (ребёнок)</template>
            </li>
          </ul>
        </article>
      </div>
    </div>
  </section>
</template>
