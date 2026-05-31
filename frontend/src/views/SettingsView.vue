<template>
  <section class="grid">
    <div class="page-heading">
      <div><h1>设置</h1><p class="muted">查看当前账号与扫描运行配置。</p></div>
      <button @click="load">刷新</button>
    </div>
    <form class="panel grid two" v-if="settings" @submit.prevent="save">
      <label>当前用户<input :value="settings.username" disabled /></label>
      <label>角色<input :value="settings.role === 'ADMIN' ? '管理员' : '成员'" disabled /></label>
      <label>Trivy 路径<input v-model="settings.trivyBin" required /></label>
      <label>工作目录<input v-model="settings.workDir" required /></label>
      <label>扫描超时（秒）<input v-model.number="settings.timeoutSeconds" type="number" min="1" required /></label>
      <label>Git 超时（秒）<input v-model.number="settings.gitTimeoutSeconds" type="number" min="1" required /></label>
      <label>Git 重试（次）<input v-model.number="settings.gitMaxAttempts" type="number" min="1" required /></label>
      <label>代码仓库 Token<input v-model="settings.repositoryToken" type="password" :placeholder="settings.hasRepositoryToken ? '已设置，留空会清空' : '用于私有 HTTPS 仓库'" /></label>
      <label>依赖采集超时（秒）<input v-model.number="settings.dependencyTimeoutSeconds" type="number" min="1" required /></label>
      <label class="checkbox-row"><input v-model="settings.skipDbUpdate" type="checkbox" /> 跳过 Trivy DB 更新</label>
      <div class="actions">
        <button type="submit">保存设置</button>
        <span v-if="message" class="muted">{{ message }}</span>
      </div>
    </form>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../api/client'

const settings = ref(null)
const message = ref('')

async function load() {
  settings.value = await api('/api/settings')
  message.value = ''
}

async function save() {
  settings.value = await api('/api/settings', {
    method: 'PUT',
    body: JSON.stringify(settings.value)
  })
  message.value = '已保存'
}

onMounted(load)
</script>
