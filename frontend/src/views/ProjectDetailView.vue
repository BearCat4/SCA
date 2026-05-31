<template>
  <section class="grid" v-if="project">
    <div class="toolbar">
      <div>
        <h1>{{ project.name }}</h1>
        <p class="muted">{{ project.gitUrl }} · {{ project.defaultBranch }}</p>
      </div>
      <div class="actions">
        <button @click="triggerScan">触发扫描</button>
        <button class="ghost" @click="load">刷新</button>
      </div>
    </div>

    <div class="panel">
      <h2>CI 调用</h2>
      <p class="muted">本地路径项目只适合在当前主机扫描；CI 调用更适合 Git 地址项目。</p>
      <pre>curl -X POST http://localhost:8080/api/ci/projects/{{ project.id }}/scans \
  -H 'Authorization: Bearer &lt;project-token&gt;' \
  -H 'Content-Type: application/json' \
  -d '{"branch":"{{ project.defaultBranch }}"}'</pre>
    </div>

    <div class="panel">
      <table>
        <thead>
          <tr>
            <th>ID</th>
            <th>状态</th>
            <th>分支</th>
            <th>触发</th>
            <th>漏洞</th>
            <th>时间</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="scan in scans" :key="scan.id">
            <td>{{ scan.id }}</td>
            <td :class="scan.status === 'PASSED' ? 'ok' : scan.status === 'FAILED' || scan.status === 'ERROR' ? 'danger' : ''">{{ labelOf(scanStatusLabels, scan.status) }}</td>
            <td>{{ scan.branch }}</td>
            <td>{{ labelOf(triggerLabels, scan.triggerType) }}</td>
            <td>{{ scan.vulnerabilityCount }} / 高危 {{ scan.highCount }} / 严重 {{ scan.criticalCount }}</td>
            <td>{{ scan.startedAt || '-' }}</td>
            <td><RouterLink class="button ghost" :to="`/scans/${scan.id}`">详情</RouterLink></td>
          </tr>
        </tbody>
      </table>
    </div>
    <p v-if="error" class="danger">{{ error }}</p>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { api } from '../api/client'
import { labelOf, scanStatusLabels, triggerLabels } from '../utils/labels'

const route = useRoute()
const project = ref(null)
const scans = ref([])
const error = ref('')

async function load() {
  project.value = await api(`/api/projects/${route.params.id}`)
  scans.value = await api(`/api/projects/${route.params.id}/scans`)
}

async function triggerScan() {
  error.value = ''
  try {
    await api(`/api/projects/${route.params.id}/scans`, { method: 'POST' })
    await load()
  } catch (err) {
    error.value = err.message
  }
}

onMounted(load)
</script>

<style scoped>
pre {
  white-space: pre-wrap;
  background: #101828;
  color: #e4e7ec;
  padding: 14px;
  border-radius: 6px;
}
</style>
