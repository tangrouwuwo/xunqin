<template>
  <div class="login-container">
    <div class="login-card">
      <h1 class="text-center mb-4 fw-bold"><i class="fas fa-sign-in-alt me-2"></i>登录</h1>
      <form @submit.prevent="handleLogin" autocomplete="off">
        <div class="mb-3">
          <label class="fw-semibold mb-2">用户名/手机号</label>
          <div class="input-group">
            <span class="input-group-text bg-light"><i class="fas fa-user text-muted"></i></span>
            <input v-model="username" type="text" class="form-control" placeholder="请输入用户名或手机号" required autocomplete="off" :readonly="isUsernameReadonly" @focus="onUsernameFocus" @click="onUsernameFocus">
          </div>
        </div>
        <div class="mb-3">
          <label class="fw-semibold mb-2">密码</label>
          <div class="input-group">
            <span class="input-group-text bg-light"><i class="fas fa-lock text-muted"></i></span>
            <input v-model="password" type="password" class="form-control" placeholder="请输入密码" required autocomplete="new-password" :readonly="isPasswordReadonly" @focus="onPasswordFocus" @click="onPasswordFocus">
          </div>
        </div>
        <button type="submit" class="btn btn-primary w-100 py-3 fw-semibold mt-2" :disabled="loading">
          <span v-if="loading" class="spinner-border spinner-border-sm me-2"></span>
          <i v-else class="fas fa-sign-in-alt me-2"></i> 登录
        </button>
      </form>
      <div v-if="error" class="alert alert-danger mt-3 rounded-3">{{ error }}</div>
      <div class="text-center mt-3">
        <span class="text-muted">没有账号？</span>
        <router-link to="/register" class="text-primary fw-semibold">立即注册</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/store/auth'

const router = useRouter()
const authStore = useAuthStore()

const username = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

// 强制清除浏览器自动填充的残留信息
const isUsernameReadonly = ref(true)
const isPasswordReadonly = ref(true)

function onUsernameFocus() {
  if (isUsernameReadonly.value) {
    isUsernameReadonly.value = false
    username.value = ''
  }
}

function onPasswordFocus() {
  if (isPasswordReadonly.value) {
    isPasswordReadonly.value = false
    password.value = ''
  }
}

onMounted(() => {
  username.value = ''
  password.value = ''
  const inputs = document.querySelectorAll('.login-card input')
  inputs.forEach(input => { input.value = '' })
  setTimeout(() => {
    username.value = ''
    password.value = ''
    const inputs = document.querySelectorAll('.login-card input')
    inputs.forEach(input => { input.value = '' })
  }, 50)
  setTimeout(() => {
    username.value = ''
    password.value = ''
  }, 300)
})

async function handleLogin() {
  error.value = ''
  loading.value = true
  try {
    const res = await authStore.login(username.value, password.value)
    if (res.code === 200) {
      router.push('/dashboard')
    } else {
      error.value = res.message || '登录失败'
    }
  } catch (e) {
    error.value = '登录请求失败，请稍后重试'
  } finally {
    loading.value = false
  }
}
</script>