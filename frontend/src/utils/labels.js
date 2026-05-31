export const severityLabels = {
  CRITICAL: '严重',
  HIGH: '高危',
  MEDIUM: '中危',
  LOW: '低危',
  UNKNOWN: '未知'
}

export const scanStatusLabels = {
  PENDING: '等待中',
  RUNNING: '扫描中',
  PASSED: '通过',
  FAILED: '未通过',
  ERROR: '异常'
}

export const vulnerabilityStatusLabels = {
  OPEN: '待处理',
  ACCEPTED_RISK: '接受风险',
  FALSE_POSITIVE: '误报',
  FIXED: '已修复'
}

export const triggerLabels = {
  MANUAL: '手动',
  CI: 'CI'
}

export const dependencyScopeLabels = {
  compile: '编译',
  provided: '已提供',
  runtime: '运行时',
  test: '测试',
  system: '系统',
  import: '导入'
}

export const severityOptions = Object.entries(severityLabels)
  .filter(([value]) => value !== 'UNKNOWN')
  .map(([value, label]) => ({ value, label }))

export const vulnerabilityStatusOptions = Object.entries(vulnerabilityStatusLabels)
  .map(([value, label]) => ({ value, label }))

export function labelOf(labels, value) {
  return labels[value] || (value ? '未知' : '-')
}
