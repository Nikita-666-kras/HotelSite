<script setup>
import { onMounted, ref, watch } from 'vue'
import { useRoute, RouterLink } from 'vue-router'
import api from '../api/client'

const route = useRoute()
const tour = ref(null)
const error = ref('')

async function load() {
  error.value = ''
  try {
    const { data } = await api.get(`/api/tours/${route.params.slug}`)
    tour.value = data
  } catch (e) {
    error.value = e.response?.data?.error || 'Тур не найден'
    tour.value = null
  }
}

onMounted(load)
watch(() => route.params.slug, load)
</script>

<template>
  <section class="section">
    <div class="container stack" v-if="tour">
      <div class="row-between" style="align-items: flex-start">
        <div>
          <h1 style="margin: 0; font-size: clamp(1.5rem, 4vw, 2rem); font-weight: 800; letter-spacing: -0.03em">
            {{ tour.title }}
          </h1>
          <p class="muted" style="margin: 0.5rem 0 0">{{ tour.destination }} · {{ tour.startDate }} — {{ tour.endDate }}</p>
        </div>
        <RouterLink class="btn btn-gradient" style="text-decoration: none; flex-shrink: 0" :to="`/tours/${tour.slug}/book`">
          Оформить заявку
        </RouterLink>
      </div>
      <div v-if="tour.mediaUrls?.length" class="grid grid-2">
        <img
          v-for="(url, i) in tour.mediaUrls"
          :key="i"
          :src="url"
          alt=""
          style="width: 100%; border-radius: var(--radius); object-fit: cover; max-height: 220px"
        />
      </div>
      <article class="card">
        <p style="white-space: pre-wrap">{{ tour.description }}</p>
        <p><strong>Цена:</strong> {{ tour.price }} ₽ · <strong>Мест:</strong> до {{ tour.maxParticipants }}</p>
        <div v-if="tour.hotels?.length">
          <strong>Отели:</strong>
          <ul>
            <li v-for="(hotel, i) in tour.hotels" :key="'hotel-' + i">{{ hotel }}</li>
          </ul>
        </div>
        <div v-if="tour.carriers?.length">
          <strong>Перевозчики:</strong>
          <ul>
            <li v-for="(carrier, i) in tour.carriers" :key="'carrier-' + i">{{ carrier }}</li>
          </ul>
        </div>
        <div v-if="tour.tickets?.length">
          <strong>Билеты:</strong>
          <ul>
            <li v-for="(ticket, i) in tour.tickets" :key="'ticket-' + i">{{ ticket }}</li>
          </ul>
        </div>
        <div v-if="tour.excursions?.length">
          <strong>Экскурсии:</strong>
          <ul>
            <li v-for="(excursion, i) in tour.excursions" :key="'excursion-' + i">{{ excursion }}</li>
          </ul>
        </div>
        <div v-if="tour.layovers?.length">
          <strong>Пересадки:</strong>
          <ul>
            <li v-for="(layover, i) in tour.layovers" :key="'layover-' + i">{{ layover }}</li>
          </ul>
        </div>
        <div
          v-if="tour.enableFlightRegistration || tour.enableHotelRegistration || tour.enableRailRegistration"
          style="margin-top: 0.5rem"
        >
          <strong>Доступна регистрация:</strong>
          <span v-if="tour.enableFlightRegistration"> рейс </span>
          <span v-if="tour.enableHotelRegistration"> отель </span>
          <span v-if="tour.enableRailRegistration"> ж/д </span>
        </div>
      </article>
    </div>
    <p v-else-if="error" class="error container">{{ error }}</p>
  </section>
</template>
