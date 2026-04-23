<script setup>
import { RouterLink } from 'vue-router'
</script>

<template>
  <section class="section">
    <div class="container stack">
      <h1 class="section-title" style="font-size: 2rem">404 — нет такой страницы</h1>
      <p class="muted" style="margin: 0 0 1rem">Проверь адрес или вернись на главную.</p>
      <RouterLink to="/" class="btn btn-gradient" style="text-decoration: none; width: fit-content">На главную</RouterLink>
    </div>
  </section>
</template>
