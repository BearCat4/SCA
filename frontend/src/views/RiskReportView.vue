<template>
  <section class="grid">
    <div class="page-heading">
      <div>
        <h1>风险报告</h1>
        <p class="muted">按项目汇总最新扫描风险、组件、漏洞和许可证数据。</p>
      </div>
      <button @click="load">刷新</button>
    </div>

    <div v-if="report" class="stat-grid">
      <div class="stat-card">
        <div><h3>项目数</h3><strong>{{ report.projectCount }}</strong></div>
      </div>
      <div class="stat-card">
        <div><h3>风险分</h3><strong>{{ report.riskScore }}</strong></div>
      </div>
      <div class="stat-card">
        <div><h3>未通过项目</h3><strong>{{ report.failedProjects }}</strong></div>
      </div>
      <div class="stat-card">
        <div><h3>未扫描项目</h3><strong>{{ report.unscannedProjects }}</strong></div>
      </div>
    </div>

    <div v-if="report" class="stat-grid">
      <div class="stat-card">
        <div><h3>组件数</h3><strong>{{ report.components }}</strong></div>
      </div>
      <div class="stat-card">
        <div><h3>漏洞数</h3><strong>{{ report.vulnerabilities }}</strong></div>
      </div>
      <div class="stat-card">
        <div><h3>严重/高危</h3><strong>{{ report.critical }} / {{ report.high }}</strong></div>
      </div>
      <div class="stat-card">
        <div><h3>许可证数</h3><strong>{{ report.licenses }}</strong></div>
      </div>
    </div>

    <div v-if="report" class="panel">
      <div class="toolbar compact">
        <div>
          <h2>项目风险明细</h2>
          <p class="muted">生成时间：{{ formatTime(report.generatedAt) }}</p>
        </div>
      </div>
      <table>
        <thead><tr><th>项目</th><th>扫描</th><th>状态</th><th>风险分</th><th>组件</th><th>漏洞</th><th>严重/高危</th><th>中/低危</th><th>许可证</th></tr></thead>
        <tbody>
          <tr v-for="item in report.projects" :key="item.projectId">
            <td><RouterLink :to="`/projects/${item.projectId}`">{{ item.projectName }}</RouterLink></td>
            <td><RouterLink v-if="item.scanId" :to="`/scans/${item.scanId}`">#{{ item.scanId }}</RouterLink><span v-else>-</span></td>
            <td>{{ labelOf(scanStatusLabels, item.status) }}</td>
            <td>{{ item.riskScore }}</td>
            <td>{{ item.components }}</td>
            <td>{{ item.vulnerabilities }}</td>
            <td>{{ item.critical }} / {{ item.high }}</td>
            <td>{{ item.medium }} / {{ item.low }}</td>
            <td>{{ item.licenses }}</td>
          </tr>
          <tr v-if="!report.projects.length"><td colspan="9" class="muted">暂无风险报告数据</td></tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../api/client'
import { labelOf, scanStatusLabels } from '../utils/labels'

const report = ref(null)

async function load() {
  report.value = await api('/api/reports/risk')
}

function formatTime(value) {
  if (!value) return '-'
  return new Date(value).toLocaleString('zh-CN')
}

onMounted(load)
</script>
