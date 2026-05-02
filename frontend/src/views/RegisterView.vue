<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const form = ref({
  email: '',
  password: '',
  fullName: '',
  phone: '',
})
const code = ref('')
const otpRequested = ref(false)
const error = ref('')
const info = ref('')

async function onSubmit() {
  error.value = ''
  info.value = ''
  try {
    if (!otpRequested.value) {
      await auth.requestRegisterOtp(form.value)
      otpRequested.value = true
      info.value = 'Код подтверждения отправлен на почту.'
      return
    }
    await auth.registerWithOtp(form.value.email, code.value)
    router.push('/')
  } catch (e) {
    error.value = e.response?.data?.error || 'Не удалось зарегистрироваться'
  }
}
</script>

<template>
  <section class="section">
    <div class="container" style="max-width: 480px">
      <div class="page-hero" style="border: none; padding: 0 0 1.5rem; margin: 0">
        <h1 style="font-size: 1.85rem">Регистрация</h1>
        <p class="lead">TripWave: новый аккаунт — и можно собирать заявки на туры за пару минут.</p>
      </div>
      <form class="card stack" @submit.prevent="onSubmit">
        <div class="field">
          <label class="label">ФИО</label>
          <input v-model="form.fullName" class="input" :disabled="otpRequested" required />
        </div>
        <div class="field">
          <label class="label">Email</label>
          <input v-model="form.email" class="input" type="email" autocomplete="username" :disabled="otpRequested" required />
        </div>
        <div class="field">
          <label class="label">Телефон</label>
          <input v-model="form.phone" class="input" type="tel" :disabled="otpRequested" />
        </div>
        <div class="field">
          <label class="label">Пароль (не менее 8 символов)</label>
          <input
            v-model="form.password"
            class="input"
            type="password"
            autocomplete="new-password"
            minlength="8"
            :disabled="otpRequested"
            required
          />
        </div>
        <div v-if="otpRequested" class="field">
          <label class="label">Код подтверждения</label>
          <input v-model="code" class="input" inputmode="numeric" maxlength="6" placeholder="6 цифр" required />
        </div>
        <button class="btn btn-gradient" type="submit" style="width: 100%">
          {{ otpRequested ? 'Подтвердить регистрацию' : 'Получить код' }}
        </button>
        <button v-if="otpRequested" class="btn btn-ghost" type="button" style="width: 100%" @click="otpRequested = false; code = ''; info = ''">
          Изменить данные
        </button>
        <p v-if="info" class="muted">{{ info }}</p>
        <p v-if="error" class="error">{{ error }}</p>
      </form>
    </div>
  </section>
</template>
