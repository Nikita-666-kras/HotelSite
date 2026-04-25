<script setup>
import { computed, ref, watch } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { useAuthStore } from './stores/auth'

const auth = useAuthStore()
const route = useRoute()
const menuOpen = ref(false)

watch(
  () => route.path,
  () => {
    menuOpen.value = false
  },
)

const initials = computed(() => {
  const n = auth.user?.fullName || auth.user?.email || '?'
  const p = n.trim().split(/\s+/)
  if (p.length >= 2) return (p[0][0] + p[1][0]).toUpperCase()
  return n.slice(0, 2).toUpperCase()
})
</script>

<template>
  <div class="app-bento">
    <aside class="bento-rail" aria-label="Навигация">
      <RouterLink
        to="/"
        class="bento-rail-brand bento-rail-brand--logo"
        title="TripWave"
        aria-label="TripWave — главная"
      >
        <img class="bento-rail-logo" src="/logo/logo_tripWave.png" alt="TripWave logo" />
      </RouterLink>

      <nav class="bento-rail-nav">
        <RouterLink to="/" class="bento-rail-link" title="Главная">
          <svg viewBox="0 0 24 24"><path d="M3 10.5L12 3l9 7.5V21a1 1 0 01-1 1h-5v-6H9v6H4a1 1 0 01-1-1v-10.5z" /></svg>
        </RouterLink>
        <RouterLink to="/tours" class="bento-rail-link" title="Туры">
          <svg viewBox="0 0 24 24"><circle cx="11" cy="11" r="7" /><path d="M21 21l-4.35-4.35" /></svg>
        </RouterLink>
        <RouterLink to="/services" class="bento-rail-link" title="Отели и билеты">
          <svg viewBox="0 0 24 24"><rect x="3" y="4" width="18" height="16" rx="2" /><path d="M7 8h4M7 12h10" /></svg>
        </RouterLink>
        
        <RouterLink to="/inspire" class="bento-rail-link" title="Вдохновение">
          <svg viewBox="0 0 24 24" aria-hidden="true">
            <path
              fill="currentColor"
              d="m12 3 2.2 6.8H21l-5.8 4.2 2.2 6.8L12 16.6 6.6 20.8 8.8 14 3 9.8h6.8L12 3z"
            />
          </svg>
        </RouterLink>
        <RouterLink to="/favorites" class="bento-rail-link" title="Избранное">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M12 21l-1.45-1.32C5.4 15.36 2 12.28 2 8.5 2 5.42 4.42 3 7.5 3c1.74 0 3.41.81 4.5 2.09C13.09 3.81 14.76 3 16.5 3 19.58 3 22 5.42 22 8.5c0 3.78-3.4 6.86-8.55 11.18L12 21z" />
          </svg>
        </RouterLink>
        <RouterLink to="/about" class="bento-rail-link" title="О нас">
          <svg viewBox="0 0 24 24"><circle cx="12" cy="12" r="9" /><path d="M12 10v5M12 7v.01" /></svg>
        </RouterLink>
        <template v-if="auth.isAuthenticated">
          <RouterLink to="/bookings" class="bento-rail-link" title="Заявки">
            <svg viewBox="0 0 24 24"><path d="M6 4h12v16H6zM9 8h6M9 12h6" /></svg>
          </RouterLink>
          <RouterLink to="/support" class="bento-rail-link" title="Поддержка">
            <svg viewBox="0 0 24 24"><path d="M21 12a8 8 0 01-8 8H6l-3 3v-5a8 8 0 018-8h10z" /></svg>
          </RouterLink>
          <template v-if="auth.isManager">
            <RouterLink to="/manager/catalog" class="bento-rail-link" title="Каталог">
              <svg viewBox="0 0 24 24" aria-hidden="true">
                <path
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  d="M4 7h6v6H4V7zm10 0h6v6h-6V7zM4 17h6v4H4v-4zm10 0h6v4h-6v-4z"
                />
              </svg>
            </RouterLink>
            <RouterLink to="/manager/crm" class="bento-rail-link" title="CRM">
              <svg viewBox="0 0 24 24"><path d="M4 6h16M4 12h10M4 18h16" /></svg>
            </RouterLink>
            <RouterLink to="/manager/support" class="bento-rail-link" title="Чаты">
              <svg viewBox="0 0 24 24"><path d="M8 10h8M8 14h5M4 18l2-3H5a3 3 0 01-3-3V7a3 3 0 013-3h14a3 3 0 013 3v5a3 3 0 01-3 3h-1l2 3" /></svg>
            </RouterLink>
          </template>
        </template>
      </nav>

      <div class="bento-rail-footer">
        <template v-if="auth.isAuthenticated">
          <RouterLink to="/profile" class="bento-rail-avatar" :title="auth.user?.fullName || 'Профиль'">
            {{ initials }}
          </RouterLink>
          <button type="button" class="bento-rail-link" title="Выйти" @click="auth.logout">
            <svg viewBox="0 0 24 24"><path d="M10 17l-1-1 3-3H3v-2h9l-3-3 1-1 5 5-5 5zM21 3v18h-8v-2h6V5h-6V3h8z" /></svg>
          </button>
        </template>
        <template v-else>
          <RouterLink to="/login" class="bento-rail-link" title="Вход">
            <svg viewBox="0 0 24 24"><path d="M15 3h4v18h-4M10 17l5-5-5-5M15 12H3" /></svg>
          </RouterLink>
          <RouterLink to="/register" class="bento-rail-link" title="Регистрация">
            <svg viewBox="0 0 24 24"><path d="M16 21v-2a4 4 0 00-4-4H6a4 4 0 00-4 4v2M12 7a4 4 0 100 8 4 4 0 000-8zM20 8v6M23 11h-6" /></svg>
          </RouterLink>
        </template>
      </div>
    </aside>

    <div class="bento-body">
      <!-- В body: fixed реально к вьюпорту, не к предку с transform/flex -->
      <Teleport to="body">
        <div class="bento-mobile-sticky">
          <header class="bento-top-mobile">
            <RouterLink to="/" class="bento-top-mobile-brand" aria-label="TripWave — главная">
              <img class="bento-top-mobile-logo" src="/logo/logo_tripWave.png" alt="TripWave logo" />
              <span>TripWave</span>
            </RouterLink>
            <button type="button" class="bento-mobile-toggle" aria-label="Меню" @click="menuOpen = !menuOpen">
              <svg v-if="!menuOpen" width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M4 6h16M4 12h16M4 18h16" />
              </svg>
              <svg v-else width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <path d="M6 6l12 12M18 6L6 18" />
              </svg>
            </button>
          </header>

          <div class="bento-mobile-drawer" :class="{ 'is-open': menuOpen }">
            <RouterLink to="/">Главная</RouterLink>
            <RouterLink to="/tours">Туры</RouterLink>
            <RouterLink to="/services">Отели и билеты</RouterLink>

            <RouterLink to="/inspire">Вдохновение</RouterLink>
            <RouterLink to="/favorites">Избранное</RouterLink>
            <RouterLink to="/about">О нас</RouterLink>
            <template v-if="auth.isAuthenticated">
              <RouterLink to="/profile">Профиль</RouterLink>
              <RouterLink to="/bookings">Мои заявки</RouterLink>
              <RouterLink to="/support">Поддержка</RouterLink>
              <template v-if="auth.isManager">
                <RouterLink to="/manager/catalog">Каталог</RouterLink>
                <RouterLink to="/manager/crm">CRM</RouterLink>
                <RouterLink to="/manager/support">Чаты</RouterLink>
              </template>
              <a href="#" @click.prevent="auth.logout">Выйти</a>
            </template>
            <template v-else>
              <RouterLink to="/login">Вход</RouterLink>
              <RouterLink to="/register">Регистрация</RouterLink>
            </template>
          </div>
        </div>
      </Teleport>

      <main class="app-main">
        <RouterView />
      </main>

      <footer class="site-footer">
        <div class="bento-container row-between">
          <span>© TripWave · {{ new Date().getFullYear() }}</span>
          <RouterLink to="/about">Контакты</RouterLink>
        </div>
      </footer>
    </div>
  </div>
</template>
