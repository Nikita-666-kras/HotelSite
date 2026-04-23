<script setup>
import { computed, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import api from '../api/client'
import { useAuthStore } from '../stores/auth'
import { tourCategoryLabel } from '../utils/tourCategories'

const props = defineProps({
  pool: { type: Array, default: () => [] },
})

const emit = defineEmits(['favorites-changed'])

const auth = useAuthStore()
const deck = ref([])
const dragX = ref(0)
const dragging = ref(false)
const dragStartX = ref(0)
const hintLogin = ref(false)
const replayMode = ref(false)
/** @type {import('vue').Ref<Array<Record<string, unknown>>>} */
const likedDuringSession = ref([])
const exiting = ref(false)
const exitDir = ref(0) // -1 left, 1 right

const TH = 72

const current = computed(() => deck.value[0] || null)
const nextCard = computed(() => deck.value[1] || null)

function shuffle(arr) {
  const a = [...arr]
  for (let i = a.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1))
    ;[a[i], a[j]] = [a[j], a[i]]
  }
  return a
}

function startReplay() {
  if (!likedDuringSession.value.length) return
  replayMode.value = true
  deck.value = shuffle(likedDuringSession.value.map((t) => ({ ...t })))
  dragX.value = 0
  hintLogin.value = false
}

/** Синхронизация колоды с пулом без полной перетасовки после лайка (иначе «карточка откатывается» / прыгает порядок). */
function syncDeckFromPool() {
  if (replayMode.value) return
  const filtered = props.pool.filter(Boolean)
  if (!filtered.length) {
    deck.value = []
    return
  }
  if (!deck.value.length) {
    deck.value = shuffle([...filtered])
    return
  }
  const poolIds = new Set(filtered.map((t) => t.id))
  // Только убираем из колоды то, чего уже нет в пуле. Не подмешиваем «новое» из пула:
  // до обновления favorites лайкнутый тур ещё в pool — иначе он снова попадёт в deck и визуально «вернётся».
  deck.value = deck.value.filter((t) => poolIds.has(t.id))
}

watch(
  () => props.pool,
  () => syncDeckFromPool(),
  { deep: true, immediate: true },
)

const cardStyle = computed(() => {
  if (exiting.value) {
    const w = typeof window !== 'undefined' ? window.innerWidth : 400
    const x = exitDir.value * (w + 80)
    return {
      transform: `translateX(${x}px) rotate(${exitDir.value * 18}deg)`,
      opacity: 1,
      transition: 'transform 0.32s cubic-bezier(0.22, 1, 0.36, 1), opacity 0.28s ease-out',
    }
  }
  const r = dragging.value ? dragX.value * 0.06 : 0
  return {
    transform: `translateX(${dragX.value}px) rotate(${r}deg)`,
    opacity: 1,
    // Без «пружины»: при отпускании ниже порога — короткий ease-out, не отскок к центру
    transition: dragging.value ? 'none' : 'transform 0.2s ease-out, opacity 0.2s ease-out',
  }
})

function onPointerDown(e) {
  if (!current.value || exiting.value) return
  dragging.value = true
  dragStartX.value = e.clientX
  dragX.value = 0
  e.currentTarget.setPointerCapture(e.pointerId)
}

function onPointerMove(e) {
  if (!dragging.value || exiting.value) return
  dragX.value = e.clientX - dragStartX.value
}

function popDeckAfterLike(tour) {
  const i = likedDuringSession.value.findIndex((t) => t.id === tour.id)
  if (i === -1) likedDuringSession.value.push({ ...tour })
}

function maybeEnterReplay() {
  if (deck.value.length > 0) return
  if (replayMode.value) return
  if (likedDuringSession.value.length > 0) startReplay()
}

function finishPointer(e) {
  if (!dragging.value || exiting.value) return
  dragging.value = false
  const dx = dragX.value
  const tour = current.value
  try {
    e.currentTarget.releasePointerCapture(e.pointerId)
  } catch {
    /* ignore */
  }
  if (!tour) return

  // Tinder: вправо — лайк, влево — пропуск
  if (dx > TH) {
    exitDir.value = 1
    exiting.value = true
    window.setTimeout(async () => {
      await likeTour(tour)
      exiting.value = false
      exitDir.value = 0
      dragX.value = 0
    }, 320)
    return
  }
  if (dx < -TH) {
    exitDir.value = -1
    exiting.value = true
    window.setTimeout(() => {
      skipTour()
      exiting.value = false
      exitDir.value = 0
      dragX.value = 0
    }, 320)
    return
  }
  dragX.value = 0
}

async function likeTour(tour) {
  const runAfter = () => {
    popDeckAfterLike(tour)
    deck.value = deck.value.slice(1)
    hintLogin.value = false
    maybeEnterReplay()
  }

  if (auth.isAuthenticated) {
    try {
      await api.post(`/api/favorites/tours/${tour.id}`)
      emit('favorites-changed')
      runAfter()
    } catch {
      hintLogin.value = true
      runAfter()
    }
    return
  }
  hintLogin.value = true
  runAfter()
}

function skipTour() {
  deck.value = deck.value.slice(1)
  maybeEnterReplay()
}

function likeClick() {
  if (!current.value || exiting.value) return
  const t = current.value
  exitDir.value = 1
  exiting.value = true
  window.setTimeout(async () => {
    await likeTour(t)
    exiting.value = false
    exitDir.value = 0
    dragX.value = 0
  }, 320)
}

function skipClick() {
  if (!current.value || exiting.value) return
  exitDir.value = -1
  exiting.value = true
  window.setTimeout(() => {
    skipTour()
    exiting.value = false
    exitDir.value = 0
    dragX.value = 0
  }, 320)
}
</script>

<template>
  <div class="swipe-deck">
    <p class="swipe-deck__hint muted surface-ink">
      Вправо — в избранное · влево — пропустить
      <span v-if="replayMode" class="swipe-deck__replay-tag">ещё раз понравившиеся</span>
    </p>
    <p v-if="hintLogin" class="swipe-deck__warn">
      Войдите в аккаунт, чтобы сохранять туры в избранное.
      <RouterLink to="/login">Вход</RouterLink>
    </p>

    <div class="swipe-deck__stage">
      <div v-if="nextCard && current" class="swipe-deck__under" aria-hidden="true">
        <div
          class="swipe-deck__media"
          :style="nextCard.mediaUrls?.[0] ? `background-image:url(${nextCard.mediaUrls[0]})` : ''"
        />
        <div class="swipe-deck__gradient" />
        <div class="swipe-deck__overlay">
          <strong>{{ nextCard.title }}</strong>
          <span class="swipe-deck__overlay-sub">{{ nextCard.destination }}</span>
        </div>
      </div>

      <div
        v-if="current"
        :key="current.id"
        class="swipe-deck__top"
        :style="cardStyle"
        @pointerdown="onPointerDown"
        @pointermove="onPointerMove"
        @pointerup="finishPointer"
        @pointercancel="finishPointer"
      >
        <div
          class="swipe-deck__stamp swipe-deck__stamp--like"
          :style="{ opacity: dragging && dragX > 16 ? Math.min(1, dragX / TH) : 0 }"
        >
          ♥
        </div>
        <div
          class="swipe-deck__stamp swipe-deck__stamp--skip"
          :style="{ opacity: dragging && dragX < -16 ? Math.min(1, -dragX / TH) : 0 }"
        >
          ✕
        </div>
        <div
          class="swipe-deck__media"
          :style="current.mediaUrls?.[0] ? `background-image:url(${current.mediaUrls[0]})` : ''"
        />
        <div class="swipe-deck__gradient" />
        <div class="swipe-deck__overlay">
          <span v-if="current.category" class="badge badge--on-dark">{{ tourCategoryLabel(current.category) }}</span>
          <strong>{{ current.title }}</strong>
          <span class="swipe-deck__overlay-sub">{{ current.destination }}</span>
          <span class="swipe-deck__price">от {{ current.price }} ₽</span>
          <RouterLink class="btn btn-on-card" :to="`/tours/${current.slug}`" @pointerdown.stop>
            Подробнее
          </RouterLink>
        </div>
      </div>

      <div v-if="!current && !exiting" class="swipe-deck__empty surface-ink">
        <template v-if="!replayMode">
          <p>Пока всё просмотрели.</p>
          <p class="muted">Загляните в каталог туров или зайдите позже.</p>
        </template>
        <template v-else>
          <p>Вы просмотрели все понравившиеся в этой сессии.</p>
          <RouterLink to="/tours" class="btn btn-outline-dark swipe-deck__empty-link">В каталог</RouterLink>
        </template>
      </div>
    </div>

    <div v-if="current" class="swipe-deck__actions">
      <button type="button" class="swipe-deck__btn swipe-deck__btn--skip" aria-label="Пропустить" @click="skipClick">
        ✕
      </button>
      <button type="button" class="swipe-deck__btn swipe-deck__btn--like" aria-label="В избранное" @click="likeClick">
        ♥
      </button>
    </div>
  </div>
</template>

<style scoped>
.swipe-deck {
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  width: 100%;
  max-width: 420px;
  margin-inline: auto;
}

.swipe-deck__hint {
  margin: 0;
  font-size: 0.75rem;
  text-align: center;
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  align-items: center;
  gap: 0.35rem;
}

.swipe-deck__replay-tag {
  font-size: 0.7rem;
  font-weight: 600;
  padding: 0.15rem 0.5rem;
  border-radius: var(--radius-pill);
  background: rgba(234, 88, 12, 0.15);
  color: var(--ink);
}

.swipe-deck__warn {
  margin: 0;
  font-size: 0.8rem;
  padding: 0.5rem 0.75rem;
  border-radius: 12px;
  background: rgba(234, 88, 12, 0.12);
  color: var(--ink);
  text-align: center;
}

.swipe-deck__warn a {
  font-weight: 600;
  text-decoration: underline;
}

.swipe-deck__stage {
  position: relative;
  width: 100%;
  aspect-ratio: 3 / 4;
  max-height: min(72vh, 560px);
  touch-action: none;
  border-radius: var(--radius-card);
}

.swipe-deck__under {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  overflow: hidden;
  border: 1px solid var(--line);
  transform: scale(0.96);
  opacity: 0.88;
  pointer-events: none;
}

.swipe-deck__top {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  overflow: hidden;
  border: 1px solid var(--line);
  background: #1a1a1a;
  box-shadow:
    0 20px 50px rgba(0, 0, 0, 0.2),
    0 0 0 1px rgba(255, 255, 255, 0.06) inset;
  cursor: grab;
  user-select: none;
  will-change: transform;
}

.swipe-deck__top:active {
  cursor: grabbing;
}

.swipe-deck__media {
  position: absolute;
  inset: 0;
  background: linear-gradient(145deg, #2a2a2a, #1a1a1a);
  background-size: cover;
  background-position: center;
}

.swipe-deck__gradient {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    to top,
    rgba(0, 0, 0, 0.92) 0%,
    rgba(0, 0, 0, 0.35) 42%,
    rgba(0, 0, 0, 0.15) 100%
  );
  pointer-events: none;
}

.swipe-deck__overlay {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 1.1rem 1.15rem 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 0.3rem;
  color: #fff;
  z-index: 1;
  text-shadow: 0 1px 8px rgba(0, 0, 0, 0.45);
}

.swipe-deck__overlay strong {
  font-size: 1.2rem;
  line-height: 1.25;
  font-weight: 700;
}

.swipe-deck__overlay-sub {
  font-size: 0.88rem;
  opacity: 0.92;
  color: rgba(255, 255, 255, 0.88);
}

.swipe-deck__price {
  font-weight: 700;
  font-size: 1rem;
  margin-top: 0.2rem;
}

.badge--on-dark {
  align-self: flex-start;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  border: 1px solid rgba(255, 255, 255, 0.25);
}

.btn-on-card {
  margin-top: 0.55rem;
  text-align: center;
  text-decoration: none;
  font-size: 0.88rem;
  padding: 0.5rem 0.85rem;
  border-radius: var(--radius-pill);
  background: rgba(255, 255, 255, 0.95);
  color: var(--ink);
  font-weight: 600;
  border: none;
  box-shadow: 0 4px 14px rgba(0, 0, 0, 0.25);
}

.btn-on-card:hover {
  background: #fff;
}

.swipe-deck__stamp {
  position: absolute;
  top: 42%;
  transform: translateY(-50%);
  font-size: 3.25rem;
  font-weight: 800;
  pointer-events: none;
  z-index: 3;
}

.swipe-deck__stamp--like {
  right: 14px;
  color: #4ade80;
  text-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
}

.swipe-deck__stamp--skip {
  left: 14px;
  color: #f87171;
  text-shadow: 0 0 20px rgba(0, 0, 0, 0.5);
}

.swipe-deck__empty {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 1.25rem;
  text-align: center;
  font-size: 0.95rem;
  border-radius: inherit;
  border: 1px dashed var(--line);
  background: var(--surface-soft);
}

.swipe-deck__empty p {
  margin: 0;
}

.swipe-deck__empty-link {
  margin-top: 0.5rem;
  text-decoration: none;
}

.swipe-deck__actions {
  display: flex;
  justify-content: center;
  gap: 1.25rem;
  padding-top: 0.35rem;
}

.swipe-deck__btn {
  width: 58px;
  height: 58px;
  border-radius: 50%;
  border: 2px solid var(--line);
  font-size: 1.4rem;
  cursor: pointer;
  background: var(--surface);
  transition:
    transform 0.15s ease,
    background 0.15s ease;
}

.swipe-deck__btn:hover {
  transform: scale(1.06);
}

.swipe-deck__btn--skip {
  color: #dc2626;
}

.swipe-deck__btn--like {
  color: #16a34a;
  border-color: rgba(22, 163, 74, 0.35);
}
</style>
