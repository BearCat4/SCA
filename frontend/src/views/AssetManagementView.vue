<template>
  <section class="grid">
    <div class="page-heading">
      <div>
        <h1>资产管理</h1>
        <p class="muted">统一查看组件资产与漏洞风险。</p>
      </div>
    </div>
    <div class="stat-grid">
      <RouterLink class="stat-card" to="/assets/components">
        <div><h3>组件资产</h3><strong>查看</strong></div>
      </RouterLink>
      <RouterLink class="stat-card" to="/assets/vulnerabilities">
        <div><h3>漏洞风险</h3><strong>查看</strong></div>
      </RouterLink>
    </div>
  </section>
</template>
