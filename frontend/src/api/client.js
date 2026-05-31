const API_BASE = ''

const apiMessageLabels = {
  'Authentication required': '请先登录',
  'Invalid username or password': '用户名或密码错误',
  'User not found': '用户不存在',
  'Project not found': '项目不存在',
  'Project access denied': '无权访问该项目',
  'Project name is required': '请填写项目名称',
  'Git URL or local path is required': '请填写 Git 地址或本地路径',
  'Invalid project token': '项目令牌无效',
  'Project token is required': '缺少项目令牌',
  'Project token does not match project': '项目令牌与项目不匹配',
  'Scan task not found': '扫描任务不存在',
  'Vulnerability status is required': '请选择漏洞处置状态',
  'Vulnerability not found': '漏洞记录不存在',
  'Raw report is not available': '原始报告不可用',
  'Invalid request': '请求参数无效',
  'Unexpected server error': '服务端异常',
  'Username is required': '请填写用户名',
  'Password must be at least 6 characters': '密码至少 6 位',
  'Username already exists': '用户名已存在',
  'Admin role is required': '需要管理员权限',
  'Cannot delete current user': '不能删除当前登录用户',
  'Upload file is too large': '上传文件过大',
  'Upload file is required': '请选择上传文件',
  'Upload file could not be saved': '上传文件保存失败',
  'Container image is required': '请填写容器镜像'
}

export function getToken() {
  return localStorage.getItem('sca_token')
}

export function setToken(token) {
  localStorage.setItem('sca_token', token)
}

export function clearToken() {
  localStorage.removeItem('sca_token')
}

export async function api(path, options = {}) {
  const isFormData = options.body instanceof FormData
  const headers = {
    ...(isFormData ? {} : { 'Content-Type': 'application/json' }),
    ...(options.headers || {})
  }
  const token = getToken()
  if (token) {
    headers.Authorization = `Bearer ${token}`
  }
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers
  })
  if (!response.ok) {
    let message = `请求失败（${response.status}）`
    try {
      const body = await response.json()
      message = localizeApiMessage(body.message) || message
    } catch (error) {
      // Keep default message for non-JSON errors.
    }
    throw new Error(message)
  }
  const contentType = response.headers.get('content-type') || ''
  return contentType.includes('application/json') ? response.json() : response.text()
}

function localizeApiMessage(message) {
  if (!message) {
    return ''
  }
  return apiMessageLabels[message] || message
}
