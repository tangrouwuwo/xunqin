<template>
  <div>
    <AppNavbar />
    <router-view />
    <AppFooter />
    <AIChat v-if="authStore.isLoggedIn" />
  </div>
</template>

<script setup>
import AppNavbar from '@/components/AppNavbar.vue'
import AppFooter from '@/components/AppFooter.vue'
import AIChat from '@/components/AIChat.vue'
import { useAuthStore } from '@/store/auth'

const authStore = useAuthStore()
</script>