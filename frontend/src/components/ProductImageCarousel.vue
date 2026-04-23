<script setup>
import { computed, onMounted, onUnmounted, ref, watch } from 'vue'

const props = defineProps({
  urls: { type: Array, default: () => [] },
  alt: { type: String, default: '' },
})

const galleryRef = ref(null)
const width = ref(320)
const idx = ref(0)
const dragging = ref(false)
const dragOffset = ref(0)
const dragStartX = ref(0)

const slides = computed(() => {
  const u = (props.urls || []).filter(Boolean)
  if (u.length) return u.map((url) => ({ kind: 'img', url }))
  return [
    { kind: 'ph', tone: 'a' },
    { kind: 'ph', tone: 'b' },
    { kind: 'ph', tone: 'c' },
  ]
})

const n = computed(() => slides.value.length)

watch(
  () => props.urls,
  () => {
    idx.value = 0
    dragOffset.value = 0
  },
  { deep: true },
)

function measure() {
  if (galleryRef.value) width.value = galleryRef.value.offsetWidth || 320
}

let ro
onMounted(() => {
  measure()
  ro = new ResizeObserver(() => measure())
  if (galleryRef.value) ro.observe(galleryRef.value)
  window.addEventListener('resize', measure)
})

onUnmounted(() => {
  ro?.disconnect()
  window.removeEventListener('resize', measure)
})

const trackStyle = computed(() => {
  const w = width.value
  const base = -idx.value * w + dragOffset.value
  const t = dragging.value ? 'none' : 'transform 0.38s cubic-bezier(0.25, 0.8, 0.25, 1)'
  return {
    width: `${n.value * w}px`,
    transform: `translateX(${base}px)`,
    transition: t,
  }
})

function slideStyle(i) {
  return { width: `${width.value}px`, flex: `0 0 ${width.value}px` }
}

function prev() {
  if (idx.value <= 0) idx.value = n.value - 1
  else idx.value -= 1
}

function next() {
  if (idx.value >= n.value - 1) idx.value = 0
  else idx.value += 1
}

function onPointerDown(e) {
  if (n.value <= 1) return
  dragging.value = true
  dragStartX.value = e.clientX
  dragOffset.value = 0
  galleryRef.value?.setPointerCapture(e.pointerId)
}

function onPointerMove(e) {
  if (!dragging.value) return
  dragOffset.value = e.clientX - dragStartX.value
}

function onPointerUp(e) {
  if (!dragging.value) return
  const th = Math.min(48, width.value * 0.12)
  if (dragOffset.value < -th) next()
  else if (dragOffset.value > th) prev()
  dragging.value = false
  dragOffset.value = 0
  try {
    galleryRef.value?.releasePointerCapture(e.pointerId)
  } catch {
    /* ignore */
  }
}

function onPointerCancel(e) {
  dragging.value = false
  dragOffset.value = 0
  try {
    galleryRef.value?.releasePointerCapture(e.pointerId)
  } catch {
    /* ignore */
  }
}

const placeholderClass = (tone) => {
  if (tone === 'a') return 'pwb-ph pwb-ph--a'
  if (tone === 'b') return 'pwb-ph pwb-ph--b'
  return 'pwb-ph pwb-ph--c'
}
</script>

<template>
  <div
    ref="galleryRef"
    class="pwb-gallery"
    @pointerdown="onPointerDown"
    @pointermove="onPointerMove"
    @pointerup="onPointerUp"
    @pointercancel="onPointerCancel"
    @pointerleave="onPointerCancel"
  >
    <div class="pwb-track" :style="trackStyle">
      <div v-for="(s, i) in slides" :key="i" class="pwb-slide" :style="slideStyle(i)">
        <img
          v-if="s.kind === 'img'"
          class="pwb-img"
          :src="s.url"
          :alt="alt"
          loading="lazy"
          draggable="false"
        />
        <div v-else :class="placeholderClass(s.tone)" aria-hidden="true" />
      </div>
    </div>

    <template v-if="n > 1">
      <button
        type="button"
        class="pwb-arrow pwb-arrow--prev"
        aria-label="Предыдущее фото"
        @pointerdown.stop
        @click.stop="prev"
      />
      <button
        type="button"
        class="pwb-arrow pwb-arrow--next"
        aria-label="Следующее фото"
        @pointerdown.stop
        @click.stop="next"
      />
      <div class="pwb-dots" role="tablist" aria-label="Фото товара">
        <button
          v-for="(_, i) in slides"
          :key="'d' + i"
          type="button"
          class="pwb-dot"
          :class="{ 'is-active': i === idx }"
          :aria-selected="i === idx"
          :aria-label="`Фото ${i + 1}`"
          @pointerdown.stop
          @click.stop="idx = i"
        />
      </div>
    </template>
  </div>
</template>

<style scoped>
.pwb-gallery {
  position: relative;
  aspect-ratio: 1;
  background: var(--surface-soft, #f0f0f0);
  overflow: hidden;
  touch-action: pan-y;
  cursor: grab;
}

.pwb-gallery:active {
  cursor: grabbing;
}

.pwb-track {
  display: flex;
  height: 100%;
  will-change: transform;
}

.pwb-slide {
  height: 100%;
  flex-shrink: 0;
}

.pwb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  user-select: none;
  pointer-events: none;
}

.pwb-ph {
  width: 100%;
  height: 100%;
}

.pwb-ph--a {
  background: linear-gradient(145deg, #1a1a2e 0%, #16213e 45%, #0f3460 100%);
}

.pwb-ph--b {
  background: linear-gradient(145deg, #2d132c 0%, #801336 50%, #c72c41 100%);
}

.pwb-ph--c {
  background: linear-gradient(145deg, #0f2027 0%, #203a43 50%, #2c5364 100%);
}

.pwb-arrow {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.92);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.12);
  cursor: pointer;
  opacity: 0;
  transition: opacity 0.2s ease, background 0.15s ease;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pwb-gallery:hover .pwb-arrow {
  opacity: 1;
}

.pwb-arrow:hover {
  background: #fff;
}

.pwb-arrow--prev {
  left: 10px;
}

.pwb-arrow--prev::after {
  content: '';
  width: 8px;
  height: 8px;
  border-left: 2px solid #111;
  border-bottom: 2px solid #111;
  transform: rotate(45deg);
  margin-left: 4px;
}

.pwb-arrow--next {
  right: 10px;
}

.pwb-arrow--next::after {
  content: '';
  width: 8px;
  height: 8px;
  border-right: 2px solid #111;
  border-top: 2px solid #111;
  transform: rotate(45deg);
  margin-right: 4px;
}

.pwb-dots {
  position: absolute;
  bottom: 12px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  gap: 6px;
  z-index: 2;
  pointer-events: none;
}

.pwb-dot {
  pointer-events: auto;
  width: 6px;
  height: 6px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.45);
  cursor: pointer;
  transition:
    transform 0.2s ease,
    background 0.2s ease,
    width 0.2s ease;
}

.pwb-dot.is-active {
  background: #fff;
  transform: scale(1.15);
  box-shadow: 0 0 0 1px rgba(0, 0, 0, 0.15);
}
</style>
