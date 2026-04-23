<script setup>
import { onMounted, ref } from 'vue'
import api from '../api/client'

const threads = ref([])
const active = ref(null)
const message = ref('')
const error = ref('')

async function loadList() {
  const { data } = await api.get('/api/manager/support/conversations')
  threads.value = data
}

async function open(id) {
  error.value = ''
  const { data } = await api.get(`/api/manager/support/conversations/${id}`)
  active.value = data
}

async function send() {
  if (!active.value || !message.value.trim()) return
  error.value = ''
  try {
    await api.post(`/api/manager/support/conversations/${active.value.id}/messages`, { body: message.value })
    message.value = ''
    await open(active.value.id)
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка'
  }
}

onMounted(loadList)
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="page-hero" style="border: none; padding: 0 0 1.25rem; margin: 0">
        <h1 style="font-size: 1.85rem">Чаты клиентов</h1>
        <p class="lead">Все обращения</p>
      </div>
    </div>
    <div class="container grid grid-2" style="align-items: start">
      <div class="card stack">
        <h3 style="margin: 0">Все обращения</h3>
        <button
          v-for="t in threads"
          :key="t.id"
          type="button"
          class="btn btn-ghost"
          style="justify-content: flex-start; width: 100%"
          @click="open(t.id)"
        >
          <span style="text-align: left">
            <strong>#{{ t.id }}</strong>
            <span class="admin-support-thread-name">{{ t.subject }}</span>
          </span>
        </button>
      </div>
      <div class="card stack" v-if="active">
        <h3 style="margin: 0">{{ active.subject }}</h3>
        <div class="stack" style="max-height: 480px; overflow-y: auto">
          <div
            v-for="m in active.messages"
            :key="m.id"
            :style="{
              alignSelf: m.staffReply ? 'flex-start' : 'flex-end',
              maxWidth: '85%',
              padding: '0.5rem 0.75rem',
              borderRadius: '16px',
              background: m.staffReply ? '#000' : '#f5f5f5',
              border: '1px solid #e5e5e5',
              color: m.staffReply ? '#fff' : '#0a0a0a',
            }"
          >
            <span class="muted" style="font-size: 0.75rem">{{ m.authorEmail || 'Система' }}</span>
            <div>{{ m.body }}</div>
          </div>
        </div>
        <form class="row" @submit.prevent="send">
          <input v-model="message" class="input" placeholder="Ответ клиенту…" />
          <button class="btn btn-gradient" type="submit">Отправить</button>
        </form>
      </div>
      <p v-else class="muted">Выберите обращение</p>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
  </section>
</template>
