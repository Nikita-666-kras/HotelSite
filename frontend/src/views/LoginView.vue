<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const auth = useAuthStore()
const router = useRouter()
const route = useRoute()
const email = ref('')
const password = ref('')
const error = ref('')

async function onSubmit() {
  error.value = ''
  try {
    await auth.login(email.value, password.value)
    const r = route.query.redirect
    router.push(typeof r === 'string' ? r : '/')
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка входа'
  }
}
</script>

<template>
  <section class="section">
    <div class="container" style="max-width: 420px">
      <div class="page-hero" style="border: none; padding: 0 0 1.5rem; margin: 0">
        <h1 style="font-size: 1.85rem">Вход</h1>
        <p class="lead">С возвращением в TripWave — заявки и чаты ждут в кабинете.</p>
      </div>
      <form class="card stack" @submit.prevent="onSubmit">
        <div class="field">
          <label class="label" for="email">Email</label>
          <input id="email" v-model="email" class="input" type="email" autocomplete="username" required />
        </div>
        <div class="field">
          <label class="label" for="pass">Пароль</label>
          <input id="pass" v-model="password" class="input" type="password" autocomplete="current-password" required />
        </div>
        <button class="btn btn-gradient" type="submit" style="width: 100%">Войти</button>
        <p v-if="error" class="error">{{ error }}</p>
      </form>
    </div>
  </section>
</template>
