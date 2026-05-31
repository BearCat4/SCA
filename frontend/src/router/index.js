import { createRouter, createWebHistory } from 'vue-router'
import { getToken } from '../api/client'
import LoginView from '../views/LoginView.vue'
import ProjectsView from '../views/ProjectsView.vue'
import ProjectDetailView from '../views/ProjectDetailView.vue'
import ScanDetailView from '../views/ScanDetailView.vue'
import QuickDetectionView from '../views/QuickDetectionView.vue'
import ProjectManagementView from '../views/ProjectManagementView.vue'
import AssetManagementView from '../views/AssetManagementView.vue'
import ComponentAssetsView from '../views/ComponentAssetsView.vue'
import VulnerabilityRisksView from '../views/VulnerabilityRisksView.vue'
import TeamMembersView from '../views/TeamMembersView.vue'
import SettingsView from '../views/SettingsView.vue'
import PolicyManagementView from '../views/PolicyManagementView.vue'
import ContainerImagesView from '../views/ContainerImagesView.vue'
import RiskReportView from '../views/RiskReportView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView },
    { path: '/', component: ProjectsView },
    { path: '/quick-detection', component: QuickDetectionView },
    { path: '/project-management', component: ProjectManagementView },
    { path: '/assets', component: AssetManagementView },
    { path: '/assets/components', component: ComponentAssetsView },
    { path: '/assets/vulnerabilities', component: VulnerabilityRisksView },
    { path: '/policies', component: PolicyManagementView },
    { path: '/container-images', component: ContainerImagesView },
    { path: '/reports/risk', component: RiskReportView },
    { path: '/team', component: TeamMembersView },
    { path: '/settings', component: SettingsView },
    { path: '/projects/:id', component: ProjectDetailView },
    { path: '/scans/:id', component: ScanDetailView }
  ]
})

router.beforeEach((to) => {
  if (to.path !== '/login' && !getToken()) {
    return '/login'
  }
  if (to.path === '/login' && getToken()) {
    return '/'
  }
})

export default router
