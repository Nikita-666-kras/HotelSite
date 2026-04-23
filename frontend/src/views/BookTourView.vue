<script setup>
import { onMounted, reactive, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import api from '../api/client'

const route = useRoute()
const router = useRouter()
const tour = ref(null)
const error = ref('')
const contactPhone = ref('')
const flightRegistration = reactive({
  passengerDocNumber: '',
  citizenship: '',
  loyaltyProgram: '',
  baggageNotes: '',
})
const hotelRegistration = reactive({
  guestFullName: '',
  documentNumber: '',
  estimatedArrivalTime: '',
  specialRequests: '',
})
const railRegistration = reactive({
  passengerDocNumber: '',
  preferredSeat: '',
  wagonPreferences: '',
  notes: '',
})

const participants = ref([
  {
    firstName: '',
    lastName: '',
    dateOfBirth: '',
    child: false,
    passportSeries: '',
    passportNumber: '',
    passportIssueDate: '',
    birthCertificateNumber: '',
    phone: '',
    comment: '',
  },
])

function addParticipant() {
  participants.value.push({
    firstName: '',
    lastName: '',
    dateOfBirth: '',
    child: false,
    passportSeries: '',
    passportNumber: '',
    passportIssueDate: '',
    birthCertificateNumber: '',
    phone: '',
    comment: '',
  })
}

function removeParticipant(i) {
  if (participants.value.length > 1) {
    participants.value.splice(i, 1)
  }
}

function handleChildToggle(participant) {
  if (participant.child) {
    participant.passportSeries = ''
    participant.passportNumber = ''
    participant.passportIssueDate = ''
  } else {
    participant.birthCertificateNumber = ''
  }
}

async function loadTour() {
  const { data } = await api.get(`/api/tours/${route.params.slug}`)
  tour.value = data
}

onMounted(loadTour)
watch(() => route.params.slug, loadTour)

async function submit() {
  error.value = ''
  try {
    const body = {
      tourId: tour.value.id,
      contactPhone: contactPhone.value || null,
      participants: participants.value.map((p) => ({
        firstName: p.firstName,
        lastName: p.lastName,
        dateOfBirth: p.dateOfBirth,
        child: p.child,
        passportSeries: p.child ? null : p.passportSeries || null,
        passportNumber: p.child ? null : p.passportNumber || null,
        passportIssueDate: p.child ? null : p.passportIssueDate || null,
        birthCertificateNumber: p.child ? p.birthCertificateNumber || null : null,
        phone: p.phone || null,
        comment: p.comment || null,
      })),
      flightRegistration: tour.value.enableFlightRegistration
        ? {
            passengerDocNumber: flightRegistration.passengerDocNumber || null,
            citizenship: flightRegistration.citizenship || null,
            loyaltyProgram: flightRegistration.loyaltyProgram || null,
            baggageNotes: flightRegistration.baggageNotes || null,
          }
        : null,
      hotelRegistration: tour.value.enableHotelRegistration
        ? {
            guestFullName: hotelRegistration.guestFullName || null,
            documentNumber: hotelRegistration.documentNumber || null,
            estimatedArrivalTime: hotelRegistration.estimatedArrivalTime || null,
            specialRequests: hotelRegistration.specialRequests || null,
          }
        : null,
      railRegistration: tour.value.enableRailRegistration
        ? {
            passengerDocNumber: railRegistration.passengerDocNumber || null,
            preferredSeat: railRegistration.preferredSeat || null,
            wagonPreferences: railRegistration.wagonPreferences || null,
            notes: railRegistration.notes || null,
          }
        : null,
    }
    await api.post('/api/bookings', body)
    router.push({ name: 'bookings' })
  } catch (e) {
    error.value = e.response?.data?.error || 'Не удалось отправить заявку'
  }
}
</script>

<template>
  <section class="section" v-if="tour">
    <div class="container stack">
      <h1 style="margin: 0; font-size: clamp(1.35rem, 3.5vw, 1.75rem); font-weight: 800; letter-spacing: -0.03em; line-height: 1.2">
        Заявка: {{ tour.title }}
      </h1>
      <p class="muted">Все участники в одной форме — паспорта, дети со свидетельством о рождении, контакты.</p>
      <form class="card stack" @submit.prevent="submit">
        <div class="field">
          <label class="label" for="phone">Контактный телефон</label>
          <input id="phone" v-model="contactPhone" class="input" type="tel" required />
        </div>
        <div v-if="tour.enableFlightRegistration" class="card stack" style="box-shadow: none; border-style: dashed">
          <strong>Регистрация на рейс</strong>
          <div class="grid grid-2">
            <div class="field">
              <label class="label">Номер документа пассажира</label>
              <input v-model="flightRegistration.passengerDocNumber" class="input" />
            </div>
            <div class="field">
              <label class="label">Гражданство</label>
              <input v-model="flightRegistration.citizenship" class="input" />
            </div>
            <div class="field">
              <label class="label">Программа лояльности</label>
              <input v-model="flightRegistration.loyaltyProgram" class="input" />
            </div>
            <div class="field">
              <label class="label">Багаж/доп. заметки</label>
              <input v-model="flightRegistration.baggageNotes" class="input" />
            </div>
          </div>
        </div>
        <div v-if="tour.enableHotelRegistration" class="card stack" style="box-shadow: none; border-style: dashed">
          <strong>Регистрация в отеле</strong>
          <div class="grid grid-2">
            <div class="field">
              <label class="label">ФИО гостя</label>
              <input v-model="hotelRegistration.guestFullName" class="input" />
            </div>
            <div class="field">
              <label class="label">Номер документа</label>
              <input v-model="hotelRegistration.documentNumber" class="input" />
            </div>
            <div class="field">
              <label class="label">Ожидаемое время прибытия</label>
              <input v-model="hotelRegistration.estimatedArrivalTime" class="input" placeholder="Например: 22:30" />
            </div>
            <div class="field" style="grid-column: 1 / -1">
              <label class="label">Особые пожелания</label>
              <textarea v-model="hotelRegistration.specialRequests" class="textarea" rows="2" />
            </div>
          </div>
        </div>
        <div v-if="tour.enableRailRegistration" class="card stack" style="box-shadow: none; border-style: dashed">
          <strong>Регистрация на ж/д</strong>
          <div class="grid grid-2">
            <div class="field">
              <label class="label">Номер документа пассажира</label>
              <input v-model="railRegistration.passengerDocNumber" class="input" />
            </div>
            <div class="field">
              <label class="label">Предпочтение по месту</label>
              <input v-model="railRegistration.preferredSeat" class="input" placeholder="Нижнее / верхнее / у окна" />
            </div>
            <div class="field">
              <label class="label">Предпочтение по вагону</label>
              <input v-model="railRegistration.wagonPreferences" class="input" />
            </div>
            <div class="field" style="grid-column: 1 / -1">
              <label class="label">Комментарий</label>
              <textarea v-model="railRegistration.notes" class="textarea" rows="2" />
            </div>
          </div>
        </div>
        <div v-for="(p, i) in participants" :key="i" class="card" style="box-shadow: none; border-style: dashed">
          <div class="row-between">
            <strong>Участник {{ i + 1 }}</strong>
            <button v-if="participants.length > 1" type="button" class="btn btn-ghost" @click="removeParticipant(i)">
              Удалить
            </button>
          </div>
          <div class="grid grid-2">
            <div class="field">
              <label class="label">Имя</label>
              <input v-model="p.firstName" class="input" required />
            </div>
            <div class="field">
              <label class="label">Фамилия</label>
              <input v-model="p.lastName" class="input" required />
            </div>
            <div class="field">
              <label class="label">Дата рождения</label>
              <input v-model="p.dateOfBirth" class="input" type="date" required />
            </div>
            <div class="field" style="align-self: end">
              <label class="label">
                <input v-model="p.child" type="checkbox" @change="handleChildToggle(p)" />
                Ребёнок
              </label>
            </div>
            <template v-if="!p.child">
              <div class="field">
                <label class="label">Серия паспорта</label>
                <input v-model="p.passportSeries" class="input" />
              </div>
              <div class="field">
                <label class="label">Номер паспорта</label>
                <input v-model="p.passportNumber" class="input" />
              </div>
              <div class="field">
                <label class="label">Дата выдачи паспорта</label>
                <input v-model="p.passportIssueDate" class="input" type="date" />
              </div>
            </template>
            <div v-else class="field">
              <label class="label">№ свидетельства о рождении</label>
              <input v-model="p.birthCertificateNumber" class="input" />
            </div>
            <div class="field">
              <label class="label">Телефон участника</label>
              <input v-model="p.phone" class="input" type="tel" />
            </div>
            <div class="field" style="grid-column: 1 / -1">
              <label class="label">Комментарий</label>
              <textarea v-model="p.comment" class="textarea" rows="2" />
            </div>
          </div>
        </div>
        <div class="row">
          <button type="button" class="btn btn-ghost" @click="addParticipant">Добавить участника</button>
          <button class="btn btn-gradient" type="submit">Отправить заявку</button>
        </div>
        <p v-if="error" class="error">{{ error }}</p>
      </form>
    </div>
  </section>
</template>
