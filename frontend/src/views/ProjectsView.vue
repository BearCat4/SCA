<template>
  <section class="overview-page grid">
    <div class="page-heading">
      <div>
        <h1>风险概览</h1>
        <p class="muted">汇总项目、组件、漏洞与许可证检测结果。</p>
      </div>
      <button @click="load">刷新</button>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div>
          <h3>项目总数</h3>
          <strong>{{ summaries.length }}</strong>
        </div>
      </div>
      <div class="stat-card">
        <div>
          <h3>组件总数</h3>
          <strong>{{ totals.components }}</strong>
        </div>
      </div>
      <div class="stat-card">
        <div>
          <h3>漏洞总数</h3>
          <strong>{{ totals.vulnerabilities }}</strong>
        </div>
      </div>
      <div class="stat-card">
        <div>
          <h3>许可证总数</h3>
          <strong>{{ totals.licenses }}</strong>
        </div>
      </div>
    </div>

    <div class="overview-grid">
      <div class="panel overview-card risk-share">
        <div class="card-title">漏洞风险占比</div>
        <div v-if="totals.vulnerabilities" class="risk-list">
          <div class="risk-row"><span>严重</span><strong>{{ totals.critical }}</strong></div>
          <div class="risk-row"><span>高危</span><strong>{{ totals.high }}</strong></div>
          <div class="risk-row"><span>中危</span><strong>{{ totals.medium }}</strong></div>
          <div class="risk-row"><span>低危</span><strong>{{ totals.low }}</strong></div>
        </div>
        <div v-else class="empty-state">
          <p>暂无数据</p>
        </div>
      </div>

      <div class="panel overview-card">
        <div class="toolbar compact">
          <div>
            <h2>漏洞影响面</h2>
            <p class="muted">输入 CVE，查看当前项目中有多少仓库涉及。</p>
          </div>
          <form class="actions" @submit.prevent="searchCve">
            <input v-model="cve" placeholder="CVE-2024-46983" />
            <button type="submit">查询</button>
          </form>
        </div>
        <div v-if="cveResult">
          <p>
            <strong>{{ cveResult.cve }}</strong>
            影响仓库 {{ cveResult.affectedProjectCount }} 个，命中 {{ cveResult.findingCount }} 条。
          </p>
          <table v-if="cveResult.findings.length">
            <thead>
              <tr>
                <th>仓库</th>
                <th>扫描</th>
                <th>包</th>
                <th>版本</th>
                <th>修复</th>
                <th>级别</th>
                <th>目标</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in cveResult.findings" :key="`${item.scanId}-${index}`">
                <td><RouterLink :to="`/projects/${item.projectId}`">{{ item.projectName }}</RouterLink></td>
                <td><RouterLink :to="`/scans/${item.scanId}`">#{{ item.scanId }}</RouterLink></td>
                <td>{{ item.packageName }}</td>
                <td>{{ item.installedVersion }}</td>
                <td>{{ item.fixedVersion || '-' }}</td>
                <td>{{ labelOf(severityLabels, item.severity) }}</td>
                <td>{{ item.target }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div v-else class="activity-list">
          <div v-for="(item, index) in summaries.slice(0, 8)" :key="item.projectId" class="activity-row">
            <span class="rank">{{ index + 1 }}</span>
            <span class="tag">项目风险</span>
            <RouterLink :to="`/projects/${item.projectId}`">{{ item.projectName }}</RouterLink>
            <span class="muted">{{ item.vulnerabilityCount }} 个漏洞</span>
          </div>
          <div v-if="!summaries.length" class="empty-line">暂无项目风险数据</div>
        </div>
      </div>
    </div>

    <div class="bottom-grid">
      <div class="panel overview-card">
        <div class="card-title">许可证类型 TOP10</div>
        <div v-if="licenseTop.length" class="risk-list">
          <div v-for="item in licenseTop" :key="item.licenseName" class="risk-row">
            <span>{{ item.licenseName }}</span>
            <strong>{{ item.count }}</strong>
          </div>
        </div>
        <div v-else class="empty-state">
          <p>暂无聚合数据</p>
        </div>
      </div>

      <div class="panel overview-card">
        <div class="toolbar compact">
          <h2>项目风险统计 TOP5</h2>
          <div class="segmented">
            <button type="button" class="active">漏洞风险</button>
            <button type="button">许可合规</button>
          </div>
        </div>
        <table v-if="topRiskProjects.length">
          <thead>
            <tr>
              <th>项目</th>
              <th>组件</th>
              <th>漏洞</th>
              <th>严重/高危</th>
              <th>最新扫描</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="project in topRiskProjects" :key="project.projectId">
              <td><RouterLink :to="`/projects/${project.projectId}`">{{ project.projectName }}</RouterLink></td>
              <td>{{ project.sbomCount }}</td>
              <td>{{ project.vulnerabilityCount }}</td>
              <td>{{ project.criticalCount }} / {{ project.highCount }}</td>
              <td>
                <RouterLink v-if="project.latestScanId" :to="`/scans/${project.latestScanId}`">{{ labelOf(scanStatusLabels, project.latestStatus) }}</RouterLink>
                <span v-else class="muted">未扫描</span>
              </td>
            </tr>
          </tbody>
        </table>
        <div v-else class="empty-state">
          <p>暂无数据</p>
        </div>
      </div>
    </div>

    <div class="panel project-table">
      <div class="card-title">项目列表</div>
      <table>
        <thead>
          <tr>
            <th>名称</th>
            <th>Git 地址 / 本地路径</th>
            <th>分支</th>
            <th>最新扫描</th>
            <th>组件</th>
            <th>漏洞</th>
            <th>严重/高危</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="project in summaries" :key="project.projectId">
            <td><RouterLink :to="`/projects/${project.projectId}`">{{ project.projectName }}</RouterLink></td>
            <td>{{ project.source }}</td>
            <td>{{ project.defaultBranch }}</td>
            <td>
              <RouterLink v-if="project.latestScanId" :to="`/scans/${project.latestScanId}`">{{ labelOf(scanStatusLabels, project.latestStatus) }}</RouterLink>
              <span v-else class="muted">未扫描</span>
            </td>
            <td>{{ project.sbomCount }}</td>
            <td>{{ project.vulnerabilityCount }}</td>
            <td>{{ project.criticalCount }} / {{ project.highCount }}</td>
            <td><RouterLink class="button ghost" :to="`/projects/${project.projectId}`">查看</RouterLink></td>
          </tr>
        </tbody>
      </table>
    </div>

  </section>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { api } from '../api/client'
import { labelOf, scanStatusLabels, severityLabels } from '../utils/labels'

const summaries = ref([])
const licenseTop = ref([])
const cve = ref('')
const cveResult = ref(null)

const totals = computed(() => summaries.value.reduce((acc, item) => {
  acc.components += item.sbomCount || 0
  acc.vulnerabilities += item.vulnerabilityCount || 0
  acc.licenses += item.licenseCount || 0
  acc.critical += item.criticalCount || 0
  acc.high += item.highCount || 0
  acc.medium += item.mediumCount || 0
  acc.low += item.lowCount || 0
  return acc
}, { components: 0, vulnerabilities: 0, licenses: 0, critical: 0, high: 0, medium: 0, low: 0 }))

const topRiskProjects = computed(() => summaries.value
  .slice()
  .sort((a, b) => (b.vulnerabilityCount || 0) - (a.vulnerabilityCount || 0))
  .slice(0, 5))

async function load() {
  summaries.value = await api('/api/analytics/projects')
  licenseTop.value = await api('/api/analytics/licenses/top')
}

async function searchCve() {
  if (!cve.value.trim()) {
    cveResult.value = null
    return
  }
  cveResult.value = await api(`/api/analytics/cves/${encodeURIComponent(cve.value.trim())}`)
}

onMounted(load)
</script>
