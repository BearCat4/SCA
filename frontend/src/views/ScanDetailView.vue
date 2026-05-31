<template>
  <section class="scan-console grid" v-if="scan">
    <div class="detection-header">
      <div>
        <p class="eyebrow">检测任务 / 扫描 #{{ scan.id }}</p>
        <h1>{{ scan.projectName }}</h1>
        <p class="muted">分支 {{ scan.branch }} · {{ labelOf(scanStatusLabels, scan.status) }}</p>
      </div>
      <div class="actions">
        <a class="button ghost" :href="reportUrl('html')" target="_blank">网页报告</a>
        <a class="button ghost" :href="reportUrl('json')" target="_blank">原始报告</a>
        <button @click="load">刷新</button>
      </div>
    </div>

    <div class="panel summary-panel">
      <div class="summary-status">
        <span class="status-pill" :class="statusClass(scan.status)">{{ labelOf(scanStatusLabels, scan.status) }}</span>
        <div>
          <h2>{{ riskSummary }}</h2>
          <p class="muted">{{ scan.failureReason || '扫描已生成漏洞、组件、依赖与许可证检测结果。' }}</p>
        </div>
      </div>
      <div class="metric-strip">
        <div class="metric-item risk-critical">
          <span>严重</span>
          <strong>{{ scan.criticalCount }}</strong>
        </div>
        <div class="metric-item risk-high">
          <span>高危</span>
          <strong>{{ scan.highCount }}</strong>
        </div>
        <div class="metric-item risk-medium">
          <span>中危</span>
          <strong>{{ scan.mediumCount }}</strong>
        </div>
        <div class="metric-item risk-low">
          <span>低危</span>
          <strong>{{ scan.lowCount }}</strong>
        </div>
        <div class="metric-item">
          <span>组件</span>
          <strong>{{ scan.componentCount }}</strong>
        </div>
        <div class="metric-item">
          <span>许可证</span>
          <strong>{{ scan.licenseCount }}</strong>
        </div>
      </div>
    </div>

    <div class="scan-steps panel">
      <div class="step done"><span>1</span><strong>创建任务</strong><em>已完成</em></div>
      <div class="step" :class="scan.status === 'RUNNING' ? 'active' : 'done'"><span>2</span><strong>执行检测</strong><em>{{ scan.status === 'RUNNING' ? '进行中' : '已完成' }}</em></div>
      <div class="step" :class="scan.status === 'PENDING' || scan.status === 'RUNNING' ? '' : 'done'"><span>3</span><strong>生成结果</strong><em>{{ scan.status === 'PENDING' || scan.status === 'RUNNING' ? '等待中' : '已完成' }}</em></div>
    </div>

    <div class="result-tabs">
      <button type="button" :class="{ active: activeTab === 'vulnerabilities' }" @click="activeTab = 'vulnerabilities'">漏洞 <span>{{ vulnerabilityPage.totalElements }}</span></button>
      <button type="button" :class="{ active: activeTab === 'components' }" @click="activeTab = 'components'">组件 <span>{{ componentPage.totalElements }}</span></button>
      <button type="button" :class="{ active: activeTab === 'dependencies' }" @click="activeTab = 'dependencies'">依赖关系 <span>{{ dependencyPage.totalElements }}</span></button>
      <button type="button" :class="{ active: activeTab === 'licenses' }" @click="activeTab = 'licenses'">许可证 <span>{{ licensePage.totalElements }}</span></button>
    </div>

    <div v-if="activeTab === 'vulnerabilities'" class="panel result-panel">
      <div class="section-heading">
        <div>
          <h2>漏洞</h2>
          <p class="muted">共 {{ vulnerabilityPage.totalElements }} 条</p>
        </div>
        <PaginationControls :state="vulnerabilityPage" @change="loadVulnerabilities" />
      </div>
      <form class="filter-bar" @submit.prevent="loadVulnerabilities({ page: 0 })">
        <input v-model="vulnerabilityFilters.q" placeholder="CVE / 包 / 标题 / 目标" />
        <select v-model="vulnerabilityFilters.severity">
          <option value="">全部级别</option>
          <option v-for="option in severityOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
        </select>
        <select v-model="vulnerabilityFilters.status">
          <option value="">全部状态</option>
          <option v-for="option in vulnerabilityStatusOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
        </select>
        <button type="submit">筛选</button>
        <button type="button" class="ghost" @click="resetVulnerabilityFilters">重置</button>
      </form>
      <div class="table-wrap">
        <table>
          <thead>
            <tr><th>ID</th><th>包</th><th>版本</th><th>修复</th><th>级别</th><th>状态</th><th>标题</th></tr>
          </thead>
          <tbody>
            <tr v-for="item in vulnerabilities" :key="item.id">
              <td><a v-if="item.referenceUrl" :href="item.referenceUrl" target="_blank">{{ item.vulnerabilityId }}</a><span v-else>{{ item.vulnerabilityId }}</span></td>
              <td>{{ item.packageName }}</td>
              <td>{{ item.installedVersion }}</td>
              <td>{{ item.fixedVersion || '-' }}</td>
              <td><span class="severity-badge" :class="`severity-${String(item.severity || '').toLowerCase()}`">{{ labelOf(severityLabels, item.severity) }}</span></td>
              <td>
                <select class="status-select" :value="item.status" @change="updateVulnerabilityStatus(item, $event)">
                  <option v-for="option in vulnerabilityStatusOptions" :key="option.value" :value="option.value">{{ option.label }}</option>
                </select>
              </td>
              <td>{{ item.title }}</td>
            </tr>
            <tr v-if="!vulnerabilities.length">
              <td colspan="7" class="muted">暂无漏洞</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="activeTab === 'components'" class="panel result-panel">
      <div class="section-heading">
        <div>
          <h2>组件</h2>
          <p class="muted">共 {{ componentPage.totalElements }} 条</p>
        </div>
        <PaginationControls :state="componentPage" @change="loadComponents" />
      </div>
      <form class="filter-bar" @submit.prevent="loadComponents({ page: 0 })">
        <input v-model="componentFilters.q" placeholder="包 / 版本 / 类型 / 目标" />
        <button type="submit">筛选</button>
        <button type="button" class="ghost" @click="resetComponentFilters">重置</button>
      </form>
      <div class="table-wrap">
        <table>
          <thead><tr><th>包</th><th>版本</th><th>类型</th><th>目标</th></tr></thead>
          <tbody>
            <tr v-for="item in components" :key="item.id">
              <td>{{ item.packageName }}</td>
              <td>{{ item.version }}</td>
              <td>{{ item.type }}</td>
              <td>{{ item.target }}</td>
            </tr>
            <tr v-if="!components.length">
              <td colspan="4" class="muted">暂无组件</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="activeTab === 'dependencies'" class="panel result-panel">
      <div class="section-heading">
        <div>
          <h2>依赖关系</h2>
          <p class="muted">共 {{ dependencyPage.totalElements }} 条</p>
        </div>
        <PaginationControls :state="dependencyPage" @change="loadDependencies" />
      </div>
      <form class="filter-bar" @submit.prevent="loadDependencies({ page: 0 })">
        <input v-model="dependencyFilters.q" placeholder="上游组件 / 依赖组件 / 版本 / 作用域" />
        <button type="submit">筛选</button>
        <button type="button" class="ghost" @click="resetDependencyFilters">重置</button>
      </form>
      <div class="table-wrap">
        <table>
          <thead><tr><th>上游组件</th><th>版本</th><th>依赖组件</th><th>版本</th><th>作用域</th></tr></thead>
          <tbody>
            <tr v-for="item in dependencies" :key="item.id">
              <td>{{ item.sourceName }}</td>
              <td>{{ item.sourceVersion || '-' }}</td>
              <td>{{ item.targetName }}</td>
              <td>{{ item.targetVersion || '-' }}</td>
              <td>{{ labelOf(dependencyScopeLabels, item.scope) }}</td>
            </tr>
            <tr v-if="!dependencies.length">
              <td colspan="5" class="muted">暂无依赖关系</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div v-if="activeTab === 'licenses'" class="panel result-panel">
      <div class="section-heading">
        <div>
          <h2>许可证</h2>
          <p class="muted">共 {{ licensePage.totalElements }} 条</p>
        </div>
        <PaginationControls :state="licensePage" @change="loadLicenses" />
      </div>
      <form class="filter-bar" @submit.prevent="loadLicenses({ page: 0 })">
        <input v-model="licenseFilters.q" placeholder="包 / 版本 / 目标" />
        <input v-model="licenseFilters.license" placeholder="许可证" />
        <button type="submit">筛选</button>
        <button type="button" class="ghost" @click="resetLicenseFilters">重置</button>
      </form>
      <div class="table-wrap">
        <table>
          <thead><tr><th>包</th><th>版本</th><th>许可证</th><th>目标</th></tr></thead>
          <tbody>
            <tr v-for="item in licenses" :key="item.id">
              <td>{{ item.packageName }}</td>
              <td>{{ item.version }}</td>
              <td>{{ item.licenseName }}</td>
              <td>{{ item.target }}</td>
            </tr>
            <tr v-if="!licenses.length">
              <td colspan="4" class="muted">暂无许可证</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, defineComponent, h, onMounted, reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import { api, getToken } from '../api/client'
import { dependencyScopeLabels, labelOf, scanStatusLabels, severityLabels, severityOptions, vulnerabilityStatusOptions } from '../utils/labels'

const route = useRoute()
const scan = ref(null)
const vulnerabilities = ref([])
const components = ref([])
const dependencies = ref([])
const licenses = ref([])
const defaultPageSize = 50
const vulnerabilityPage = reactive(pageState())
const componentPage = reactive(pageState())
const dependencyPage = reactive(pageState())
const licensePage = reactive(pageState())
const vulnerabilityFilters = reactive({ q: '', severity: '', status: '' })
const componentFilters = reactive({ q: '' })
const dependencyFilters = reactive({ q: '' })
const licenseFilters = reactive({ q: '', license: '' })
const activeTab = ref('vulnerabilities')

const riskSummary = computed(() => {
  if (scan.value.status === 'RUNNING') {
    return '检测进行中'
  }
  if (scan.value.status === 'PENDING') {
    return '任务等待执行'
  }
  if (scan.value.status === 'ERROR') {
    return '检测异常'
  }
  if (scan.value.vulnerabilityCount > 0) {
    return `发现 ${scan.value.vulnerabilityCount} 个漏洞风险`
  }
  return '未发现漏洞风险'
})

const PaginationControls = defineComponent({
  props: {
    state: {
      type: Object,
      required: true
    }
  },
  emits: ['change'],
  setup(props, { emit }) {
    function changePage(page) {
      emit('change', { page, size: props.state.size })
    }

    function changeSize(event) {
      emit('change', { page: 0, size: Number(event.target.value) })
    }

    return () => h('div', { class: 'pagination' }, [
      h('span', { class: 'muted' }, `第 ${props.state.totalPages ? props.state.number + 1 : 0} / ${props.state.totalPages} 页`),
      h('button', {
        class: 'ghost',
        disabled: props.state.first || props.state.loading,
        onClick: () => changePage(props.state.number - 1)
      }, '上一页'),
      h('button', {
        class: 'ghost',
        disabled: props.state.last || props.state.loading,
        onClick: () => changePage(props.state.number + 1)
      }, '下一页'),
      h('select', {
        value: props.state.size,
        disabled: props.state.loading,
        onChange: changeSize
      }, [20, 50, 100].map((size) => h('option', { value: size }, `${size} 条/页`)))
    ])
  }
})

function pageState() {
  return {
    number: 0,
    size: defaultPageSize,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
    loading: false
  }
}

async function load() {
  const id = route.params.id
  scan.value = await api(`/api/scans/${id}`)
  await Promise.all([
    loadVulnerabilities({ page: vulnerabilityPage.number, size: vulnerabilityPage.size }),
    loadComponents({ page: componentPage.number, size: componentPage.size }),
    loadDependencies({ page: dependencyPage.number, size: dependencyPage.size }),
    loadLicenses({ page: licensePage.number, size: licensePage.size })
  ])
}

async function loadVulnerabilities(options = {}) {
  const page = await loadPage('vulnerabilities', vulnerabilityPage, options, vulnerabilityFilters)
  vulnerabilities.value = page.content || []
}

async function loadComponents(options = {}) {
  const page = await loadPage('components', componentPage, options, componentFilters)
  components.value = page.content || []
}

async function loadDependencies(options = {}) {
  const page = await loadPage('dependencies', dependencyPage, options, dependencyFilters)
  dependencies.value = page.content || []
}

async function loadLicenses(options = {}) {
  const page = await loadPage('licenses', licensePage, options, licenseFilters)
  licenses.value = page.content || []
}

async function loadPage(resource, state, options, filters) {
  const id = route.params.id
  const page = Math.max(0, options.page == null ? state.number : options.page)
  const size = options.size || state.size
  const params = new URLSearchParams({ page: String(page), size: String(size) })
  Object.keys(filters).forEach((key) => {
    const value = filters[key]
    if (value && String(value).trim()) {
      params.set(key, String(value).trim())
    }
  })
  state.loading = true
  try {
    const result = await api(`/api/scans/${id}/${resource}?${params.toString()}`)
    applyPage(state, result)
    return result
  } finally {
    state.loading = false
  }
}

function applyPage(state, page) {
  state.number = page.number || 0
  state.size = page.size || defaultPageSize
  state.totalElements = page.totalElements || 0
  state.totalPages = page.totalPages || 0
  state.first = page.first !== false
  state.last = page.last !== false
}

function resetVulnerabilityFilters() {
  Object.assign(vulnerabilityFilters, { q: '', severity: '', status: '' })
  loadVulnerabilities({ page: 0 })
}

async function updateVulnerabilityStatus(item, event) {
  const status = event.target.value
  const updated = await api(`/api/scans/${scan.value.id}/vulnerabilities/${item.id}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status })
  })
  item.status = updated.status
}

function resetComponentFilters() {
  Object.assign(componentFilters, { q: '' })
  loadComponents({ page: 0 })
}

function resetDependencyFilters() {
  Object.assign(dependencyFilters, { q: '' })
  loadDependencies({ page: 0 })
}

function resetLicenseFilters() {
  Object.assign(licenseFilters, { q: '', license: '' })
  loadLicenses({ page: 0 })
}

function reportUrl(format) {
  return `/api/scans/${scan.value.id}/report.${format}?access_token=${encodeURIComponent(getToken())}`
}

function statusClass(status) {
  if (status === 'PASSED') {
    return 'status-ok'
  }
  if (status === 'FAILED' || status === 'ERROR') {
    return 'status-danger'
  }
  return 'status-running'
}

onMounted(load)
</script>
