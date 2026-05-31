<template>
  <section class="grid">
    <div class="page-heading">
      <div>
        <h1>策略管理</h1>
        <p class="muted">配置全局扫描判定策略，用于自动标记扫描是否通过。</p>
      </div>
      <button @click="load">刷新</button>
    </div>

    <form v-if="policy" class="panel grid two" @submit.prevent="save">
      <label class="checkbox-row"><input v-model="policy.failOnCritical" type="checkbox" /> 严重漏洞判定为未通过</label>
      <label class="checkbox-row"><input v-model="policy.failOnHigh" type="checkbox" /> 高危漏洞判定为未通过</label>
      <label class="full-row">
        禁止许可证关键字
        <input v-model="policy.forbiddenLicenses" placeholder="GPL,AGPL,LGPL" />
      </label>
      <div class="actions full-row">
        <button type="submit">保存策略</button>
        <span v-if="message" class="muted">{{ message }}</span>
      </div>
    </form>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../api/client'

const policy = ref(null)
const message = ref('')

async function load() {
  policy.value = await api('/api/policies')
  message.value = ''
}

async function save() {
  policy.value = await api('/api/policies', { method: 'PUT', body: JSON.stringify(policy.value) })
  message.value = '已保存'
}

onMounted(load)
</script>
