<template>
  <section class="login">
    <form class="panel login-panel" @submit.prevent="submit">
      <h1>登录</h1>
      <p class="muted">使用内置账号进入 SCA 管理台。</p>
      <label>
        用户名
        <input v-model="username" autocomplete="username" />
      </label>
      <label>
        密码
        <input v-model="password" type="password" autocomplete="current-password" />
      </label>
      <p v-if="error" class="danger">{{ error }}</p>
      <button type="submit" :disabled="loading">{{ loading ? '登录中' : '登录' }}</button>
    </form>
  </section>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { api, setToken } from '../api/client'

const router = useRouter()
const username = ref('admin')
const password = ref('admin123')
const loading = ref(false)
const error = ref('')

async function submit() {
  loading.value = true
  error.value = ''
  try {
    const response = await api('/api/auth/login', {
      method: 'POST',
      body: JSON.stringify({ username: username.value, password: password.value })
    })
    setToken(response.token)
    router.push('/')
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login {
  display: grid;
  place-items: center;
  min-height: calc(100vh - 112px);
}

.login-panel {
  width: min(420px, 100%);
  display: grid;
  gap: 16px;
}
</style>
