<template>
  <section class="grid">
    <div class="page-heading">
      <div><h1>组件资产</h1><p class="muted">按项目汇总所有扫描组件。</p></div>
    </div>
    <div class="panel">
      <form class="filter-bar" @submit.prevent="load({ page: 0 })">
        <input v-model="filters.q" placeholder="包 / 版本 / 类型 / 目标" />
        <select v-model.number="pageSize" @change="load({ page: 0 })">
          <option :value="20">20 条/页</option>
          <option :value="50">50 条/页</option>
          <option :value="100">100 条/页</option>
        </select>
        <button type="submit">筛选</button>
        <button type="button" class="ghost" @click="reset">重置</button>
      </form>
      <table>
        <thead><tr><th>项目</th><th>包</th><th>版本</th><th>类型</th><th>目标</th><th>扫描</th></tr></thead>
        <tbody>
          <tr v-for="item in items" :key="item.id">
            <td><RouterLink :to="`/projects/${item.projectId}`">{{ item.projectName }}</RouterLink></td>
            <td>{{ item.packageName }}</td>
            <td>{{ item.version }}</td>
            <td>{{ item.type }}</td>
            <td>{{ item.target }}</td>
            <td><RouterLink :to="`/scans/${item.scanId}`">#{{ item.scanId }}</RouterLink></td>
          </tr>
          <tr v-if="!items.length"><td colspan="6" class="muted">暂无组件资产</td></tr>
        </tbody>
      </table>
      <div class="pagination">
        <span>共 {{ total }} 条，第 {{ pageNumber + 1 }} / {{ totalPages || 1 }} 页</span>
        <button class="ghost" :disabled="pageNumber === 0" @click="load({ page: pageNumber - 1 })">上一页</button>
        <button class="ghost" :disabled="pageNumber + 1 >= totalPages" @click="load({ page: pageNumber + 1 })">下一页</button>
      </div>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../api/client'

const items = ref([])
const filters = reactive({ q: '' })
const pageNumber = ref(0)
const pageSize = ref(20)
const total = ref(0)
const totalPages = ref(0)

async function load(options = {}) {
  const nextPage = options.page ?? pageNumber.value
  const params = new URLSearchParams({ page: String(nextPage), size: String(pageSize.value) })
  if (filters.q.trim()) params.set('q', filters.q.trim())
  const page = await api(`/api/assets/components?${params}`)
  items.value = page.content || []
  pageNumber.value = page.number || 0
  total.value = page.totalElements || 0
  totalPages.value = page.totalPages || 0
}

function reset() {
  filters.q = ''
  load()
}

onMounted(load)
</script>
