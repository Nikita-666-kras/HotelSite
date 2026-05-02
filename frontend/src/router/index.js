import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/', name: 'home', component: () => import('../views/HomeView.vue') },
  { path: '/tours', name: 'tours', component: () => import('../views/ToursView.vue') },
  { path: '/tours/:slug', name: 'tour', component: () => import('../views/TourDetailView.vue') },
  {
    path: '/tours/:slug/book',
    name: 'book',
    component: () => import('../views/BookTourView.vue'),
    meta: { requiresAuth: true },
  },
  { path: '/services', name: 'services', component: () => import('../views/ServicesView.vue') },
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue') },
  { path: '/register', name: 'register', component: () => import('../views/RegisterView.vue') },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('../views/ProfileView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/bookings',
    name: 'bookings',
    component: () => import('../views/MyBookingsView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/support',
    name: 'support',
    component: () => import('../views/SupportView.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/manager/crm',
    name: 'crm',
    component: () => import('../views/CrmView.vue'),
    meta: { requiresAuth: true, requiresManager: true },
  },
  {
    path: '/manager/catalog',
    name: 'manager-catalog',
    component: () => import('../views/ManagerCatalogView.vue'),
    meta: { requiresAuth: true, requiresManager: true },
  },
  {
    path: '/manager/support',
    name: 'manager-support',
    component: () => import('../views/ManagerSupportView.vue'),
    meta: { requiresAuth: true, requiresManager: true },
  },
  {
    path: '/admin',
    name: 'admin',
    component: () => import('../views/AdminView.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
  },
  { path: '/about', name: 'about', component: () => import('../views/AboutView.vue') },
  { path: '/inspire', name: 'inspire', component: () => import('../views/InspireView.vue') },
  { path: '/favorites', name: 'favorites', component: () => import('../views/FavoritesView.vue') },
  { path: '/:pathMatch(.*)*', name: 'notfound', component: () => import('../views/NotFoundView.vue') },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach(async (to) => {
  const auth = useAuthStore()
  if (auth.loading) {
    await new Promise((r) => setTimeout(r, 50))
  }
  if (to.meta.requiresAuth && !auth.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.meta.requiresManager && !auth.isManager) {
    return { name: 'home' }
  }
  if (to.meta.requiresAdmin && !auth.isAdmin) {
    return { name: 'home' }
  }
})

export default router
