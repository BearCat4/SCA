<template>
  <div class="app-layout">
    <aside v-if="token" class="sidebar">
      <div class="sidebar-brand">
        <RouterLink to="/" class="brand-wordmark"><span>THS</span><strong>SCA</strong></RouterLink>
      </div>
      <nav class="sidebar-nav">
        <RouterLink to="/" class="nav-item">风险概览</RouterLink>
        <RouterLink to="/quick-detection" class="nav-item">快速检测</RouterLink>
        <RouterLink to="/project-management" class="nav-item">项目管理</RouterLink>
        <RouterLink to="/policies" class="nav-item">策略管理</RouterLink>
        <RouterLink to="/container-images" class="nav-item">容器镜像服务</RouterLink>
        <RouterLink to="/reports/risk" class="nav-item">风险报告</RouterLink>
        <div class="nav-group" :class="{ expanded: assetsOpen }">
          <button type="button" class="nav-item nav-toggle" :class="{ active: route.path.startsWith('/assets') }" @click="assetsOpen = !assetsOpen">
            <span>资产管理</span>
          </button>
          <div v-if="assetsOpen" class="nav-submenu">
            <RouterLink to="/assets/components" class="nav-subitem">组件资产</RouterLink>
            <RouterLink to="/assets/vulnerabilities" class="nav-subitem">漏洞风险</RouterLink>
          </div>
        </div>
        <RouterLink to="/team" class="nav-item">团队成员</RouterLink>
        <RouterLink to="/settings" class="nav-item">设置</RouterLink>
      </nav>
      <button class="ghost sidebar-logout" @click="logout">退出</button>
    </aside>
    <main :class="token ? 'shell console-shell' : 'shell'">
      <RouterView />
    </main>
  </div>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { clearToken, getToken } from './api/client'

const router = useRouter()
const route = useRoute()
const token = computed(() => getToken())
const assetsOpen = ref(route.path.startsWith('/assets'))

watch(() => route.path, (path) => {
  if (path.startsWith('/assets')) {
    assetsOpen.value = true
  }
})

function logout() {
  clearToken()
  router.push('/login')
}
</script>
