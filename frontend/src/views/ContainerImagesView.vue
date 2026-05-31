<template>
  <section class="grid">
    <div class="page-heading">
      <div>
        <h1>容器镜像服务</h1>
        <p class="muted">使用 Trivy image 扫描容器镜像，例如 nginx:latest 或 registry.example.com/app:1.0。</p>
      </div>
    </div>

    <form class="panel grid two" @submit.prevent="submit">
      <label>任务名称<input v-model="form.name" placeholder="nginx latest" /></label>
      <label>镜像地址<input v-model="form.imageRef" required placeholder="nginx:latest" /></label>
      <div class="actions full-row">
        <button type="submit" :disabled="loading">{{ loading ? '提交中' : '开始扫描' }}</button>
        <RouterLink v-if="scanId" class="button ghost" :to="`/scans/${scanId}`">查看扫描 #{{ scanId }}</RouterLink>
      </div>
      <p v-if="error" class="danger full-row">{{ error }}</p>
    </form>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { api } from '../api/client'

const loading = ref(false)
const error = ref('')
const scanId = ref(null)
const form = reactive({ name: '', imageRef: '' })

async function submit() {
  loading.value = true
  error.value = ''
  scanId.value = null
  try {
    const result = await api('/api/container-images/scans', {
      method: 'POST',
      body: JSON.stringify(form)
    })
    scanId.value = result.scan.id
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}
</script>
