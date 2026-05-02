<script setup>
import { onMounted, onUnmounted, ref } from 'vue'
import api from '../api/client'

const POLL_MS = 5000
let pollTimer = null

const threads = ref([])
const active = ref(null)
const message = ref('')
const error = ref('')

async function loadList() {
  const { data } = await api.get('/api/support/conversations')
  threads.value = data
}

async function open(id) {
  error.value = ''
  try {
    const { data } = await api.get(`/api/support/conversations/${id}`)
    active.value = data
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка'
  }
}

async function create() {
  error.value = ''
  try {
    const { data } = await api.post('/api/support/conversations', {}, { params: { subject: 'Новый вопрос' } })
    active.value = data
    await loadList()
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка'
  }
}

async function send() {
  if (!active.value || !message.value.trim()) return
  error.value = ''
  try {
    await api.post(`/api/support/conversations/${active.value.id}/messages`, { body: message.value })
    message.value = ''
    await open(active.value.id)
    await loadList()
  } catch (e) {
    error.value = e.response?.data?.error || 'Ошибка отправки'
  }
}

onMounted(async () => {
  try {
    await loadList()
  } catch {
    /* ignore */
  }
  pollTimer = setInterval(async () => {
    try {
      await loadList()
      if (active.value?.id) await open(active.value.id)
    } catch {
      /* ignore */
    }
  }, POLL_MS)
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<template>
  <section class="section">
    <div class="container">
      <div class="page-hero" style="border: none; padding: 0 0 1.25rem; margin: 0">
        <h1 style="font-size: 1.85rem">Поддержка</h1>
        <p class="lead">Напиши — менеджер подключится в чате.</p>
      </div>
    </div>
    <div class="container grid grid-2" style="align-items: start">
      <div class="card stack">
        <div class="row-between">
          <h3 style="margin: 0">Диалоги</h3>
          <button type="button" class="btn btn-gradient" @click="create">Новый чат</button>
        </div>
        <button
          v-for="t in threads"
          :key="t.id"
          type="button"
          class="btn btn-ghost"
          style="justify-content: flex-start; width: 100%"
          @click="open(t.id)"
        >
          <span style="text-align: left">
            <strong>#{{ t.id }}</strong> {{ t.subject }}
            <span class="muted" style="display: block; font-size: 0.8rem">{{ t.createdAt }}</span>
          </span>
        </button>
      </div>
      <div class="card stack" v-if="active">
        <h3 style="margin: 0">{{ active.subject }}</h3>
        <div class="stack" style="max-height: 420px; overflow-y: auto">
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
            <span class="muted" style="font-size: 0.75rem">{{ m.authorEmail || 'Поддержка' }}</span>
            <div>{{ m.body }}</div>
          </div>
        </div>
        <form class="row" style="gap: 0.5rem" @submit.prevent="send">
          <input v-model="message" class="input" placeholder="Сообщение…" />
          <button class="btn btn-gradient" type="submit">Отправить</button>
        </form>
      </div>
      <p v-else class="muted">Выберите диалог или создайте новый.</p>
      <p v-if="error" class="error">{{ error }}</p>
    </div>
  </section>
</template>
