<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'
import api from '../api/client'

const props = defineProps({
  modelValue: { type: String, default: '' },
  placeholder: { type: String, default: '' },
  inputId: { type: String, default: '' },
  variant: { type: String, default: 'default' },
  minChars: { type: Number, default: 1 },
})

const emit = defineEmits(['update:modelValue'])

const uid = Math.random().toString(36).slice(2, 9)
const id = computed(() => props.inputId || `dest-ac-${uid}`)

const rootEl = ref(null)
const open = ref(false)
const suggestions = ref([])
const highlighted = ref(-1)

let debounceTimer = null

const inputClass = computed(() => {
  if (props.variant === 'default') return ['dest-ac__input', 'input']
  return ['dest-ac__input', 'dest-ac__input--bare']
})

function scheduleFetch(q) {
  clearTimeout(debounceTimer)
  const t = (q || '').trim()
  if (t.length < props.minChars) {
    suggestions.value = []
    open.value = false
    highlighted.value = -1
    return
  }
  debounceTimer = setTimeout(() => fetchSuggestions(t), 220)
}

async function fetchSuggestions(q) {
  try {
    const { data } = await api.get('/api/destinations/suggest', { params: { q } })
    suggestions.value = Array.isArray(data) ? data : []
    open.value = suggestions.value.length > 0
    highlighted.value = suggestions.value.length > 0 ? 0 : -1
  } catch {
    suggestions.value = []
    open.value = false
    highlighted.value = -1
  }
}

function onInput(e) {
  const v = e.target.value
  emit('update:modelValue', v)
  scheduleFetch(v)
}

function selectItem(s) {
  emit('update:modelValue', s)
  suggestions.value = []
  open.value = false
  highlighted.value = -1
}

function onKeydown(e) {
  if (!open.value || !suggestions.value.length) return
  if (e.key === 'ArrowDown') {
    e.preventDefault()
    highlighted.value = Math.min(highlighted.value + 1, suggestions.value.length - 1)
  } else if (e.key === 'ArrowUp') {
    e.preventDefault()
    highlighted.value = Math.max(highlighted.value - 1, 0)
  } else if (e.key === 'Enter' && highlighted.value >= 0) {
    e.preventDefault()
    selectItem(suggestions.value[highlighted.value])
  } else if (e.key === 'Escape') {
    open.value = false
  }
}

function onFocus() {
  const t = (props.modelValue || '').trim()
  if (t.length >= props.minChars) {
    scheduleFetch(props.modelValue || '')
  }
}

function onDocClick(e) {
  if (!rootEl.value?.contains(e.target)) {
    open.value = false
  }
}

watch(
  () => props.modelValue,
  (v, old) => {
    if ((v || '').trim().length < props.minChars && (old || '').trim().length >= props.minChars) {
      suggestions.value = []
      open.value = false
    }
  },
)

onMounted(() => document.addEventListener('click', onDocClick, true))
onUnmounted(() => {
  document.removeEventListener('click', onDocClick, true)
  clearTimeout(debounceTimer)
})
</script>

<template>
  <div
    ref="rootEl"
    class="dest-ac"
    :class="[`dest-ac--${variant}`]"
  >
    <input
      :id="id"
      type="search"
      autocomplete="off"
      autocorrect="off"
      spellcheck="false"
      :class="inputClass"
      :placeholder="placeholder"
      :value="modelValue"
      role="combobox"
      :aria-expanded="open && suggestions.length > 0"
      :aria-controls="open && suggestions.length ? `${id}-list` : undefined"
      aria-autocomplete="list"
      @input="onInput"
      @keydown="onKeydown"
      @focus="onFocus"
    />
    <ul
      v-show="open && suggestions.length"
      :id="`${id}-list`"
      class="dest-ac__list"
      role="listbox"
      :aria-label="placeholder || 'Подсказки направлений'"
    >
      <li
        v-for="(s, i) in suggestions"
        :key="s + i"
        role="option"
        :aria-selected="i === highlighted"
        class="dest-ac__item"
        :class="{ 'is-active': i === highlighted }"
        @mousedown.prevent="selectItem(s)"
      >
        {{ s }}
      </li>
    </ul>
  </div>
</template>

<style scoped>
.dest-ac {
  position: relative;
  width: 100%;
}

.dest-ac--hero,
.dest-ac--pill {
  flex: 1;
  min-width: 0;
}

.dest-ac__input--bare {
  border: none;
  background: transparent;
  font: inherit;
  color: var(--ink);
  min-width: 0;
  width: 100%;
  padding: 0;
}

.dest-ac__input--bare:focus {
  outline: none;
}

.dest-ac__list {
  position: absolute;
  left: 0;
  right: 0;
  top: calc(100% + 6px);
  margin: 0;
  padding: 0.35rem 0;
  list-style: none;
  background: var(--surface);
  border: 1px solid var(--line);
  border-radius: var(--radius-sm);
  box-shadow: 0 16px 48px rgba(0, 0, 0, 0.14);
  max-height: min(240px, 45vh);
  overflow-y: auto;
  z-index: 100;
}

.dest-ac--hero .dest-ac__list {
  top: auto;
  bottom: calc(100% + 8px);
  border-radius: 20px;
}

.dest-ac__item {
  padding: 0.55rem 1rem;
  font-size: 0.95rem;
  color: var(--ink);
  cursor: pointer;
  transition: background 0.12s ease;
}

@media (max-width: 640px) {
  .dest-ac__item {
    padding: 0.75rem 1rem;
    font-size: 16px;
    min-height: 48px;
    display: flex;
    align-items: center;
  }
}

.dest-ac__item:hover,
.dest-ac__item.is-active {
  background: var(--surface-soft);
}

.dest-ac__item.is-active {
  font-weight: 500;
}
</style>
