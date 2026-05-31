<template>
  <section class="grid">
    <div class="notice-bar">
      快速检测支持文件上传、代码仓库和本地路径方式，代码仓库 Token 在设置中维护。
      <RouterLink to="/settings">前往设置</RouterLink>
    </div>

    <div class="page-heading">
      <div>
        <h1>快速检测</h1>
        <p class="muted">临时扫描任务会自动关联到新项目，完成后可在扫描详情中查看 SBOM、漏洞、许可证和依赖关系。</p>
      </div>
      <button @click="open = true">立即扫描</button>
    </div>

    <div class="panel">
      <table>
        <thead><tr><th>目标名称</th><th>来源</th><th>分支/方式</th><th>扫描</th></tr></thead>
        <tbody>
          <tr v-for="item in history" :key="item.scanId">
            <td>{{ item.name }}</td>
            <td>{{ item.source }}</td>
            <td>{{ item.mode }}</td>
            <td><RouterLink :to="`/scans/${item.scanId}`">#{{ item.scanId }}</RouterLink></td>
          </tr>
          <tr v-if="!history.length"><td colspan="4" class="muted">暂无快速检测记录</td></tr>
        </tbody>
      </table>
    </div>

    <div v-if="open" class="modal-mask">
      <div class="quick-modal">
        <div class="modal-heading">
          <h2>快速检测</h2>
          <button class="icon-button" type="button" @click="open = false">×</button>
        </div>

        <div class="segmented">
          <button type="button" :class="{ active: mode === 'file' }" @click="mode = 'file'">文件上传</button>
          <button type="button" :class="{ active: mode === 'repo' }" @click="mode = 'repo'">代码仓库</button>
          <button type="button" :class="{ active: mode === 'local' }" @click="mode = 'local'">其他方式</button>
        </div>

        <form v-if="mode === 'file'" class="quick-form" @submit.prevent="submitFile">
          <label>目标名称<input v-model="fileForm.name" required placeholder="nacos-develop.zip" /></label>
          <label class="upload-zone">
            <input type="file" @change="onFileChange" />
            <strong>{{ selectedFile ? selectedFile.name : '点击上传或将文件拖拽到此处' }}</strong>
            <span>支持 zip、rar、jar、tar.gz、tar.bz2 等压缩文件，以及 SPDX、DSDX、CycloneDX、SWID 等 SBOM 标准格式。</span>
          </label>
          <div class="modal-actions">
            <button type="button" class="ghost" @click="open = false">取消</button>
            <button type="submit" :disabled="loading">{{ loading ? '提交中' : '扫描' }}</button>
          </div>
        </form>

        <form v-if="mode === 'repo'" class="quick-form grid two" @submit.prevent="submitRepo">
          <label>目标名称<input v-model="repoForm.name" required placeholder="nacos-develop" /></label>
          <label>仓库地址<input v-model="repoForm.gitUrl" required placeholder="https://github.com/alibaba/nacos.git" /></label>
          <label>分支<input v-model="repoForm.defaultBranch" placeholder="main" /></label>
          <p class="muted full-row">私有仓库 Token 从设置读取，当前无需在此重复填写。</p>
          <div class="modal-actions full-row">
            <button type="button" class="ghost" @click="open = false">取消</button>
            <button type="submit" :disabled="loading">{{ loading ? '提交中' : '扫描' }}</button>
          </div>
        </form>

        <form v-if="mode === 'local'" class="quick-form grid two" @submit.prevent="submitLocal">
          <label>目标名称<input v-model="localForm.name" required placeholder="nacos-develop" /></label>
          <label>本地路径<input v-model="localForm.gitUrl" required placeholder="/Users/yebaolin/Desktop/nacos-develop" /></label>
          <div class="modal-actions full-row">
            <button type="button" class="ghost" @click="open = false">取消</button>
            <button type="submit" :disabled="loading">{{ loading ? '提交中' : '扫描' }}</button>
          </div>
        </form>

        <p v-if="error" class="danger">{{ error }}</p>
        <RouterLink v-if="scanId" class="button ghost" :to="`/scans/${scanId}`">查看扫描 #{{ scanId }}</RouterLink>
      </div>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { api } from '../api/client'

const open = ref(false)
const mode = ref('file')
const loading = ref(false)
const error = ref('')
const scanId = ref(null)
const selectedFile = ref(null)
const history = ref([])

const fileForm = reactive({ name: '' })
const repoForm = reactive({ name: '', gitUrl: '', defaultBranch: 'main' })
const localForm = reactive({ name: '', gitUrl: '', defaultBranch: 'local' })

function onFileChange(event) {
  selectedFile.value = event.target.files?.[0] || null
  if (selectedFile.value && !fileForm.name) {
    fileForm.name = selectedFile.value.name
  }
}

async function submitFile() {
  if (!selectedFile.value) {
    error.value = '请选择上传文件'
    return
  }
  await submit(async () => {
    const body = new FormData()
    body.append('file', selectedFile.value)
    body.append('name', fileForm.name)
    return api('/api/detection/quick/file', { method: 'POST', body })
  }, fileForm.name, selectedFile.value.name, '文件上传')
}

async function submitRepo() {
  await submit(() => api('/api/detection/quick', {
    method: 'POST',
    body: JSON.stringify(repoForm)
  }), repoForm.name, repoForm.gitUrl, repoForm.defaultBranch || 'main')
}

async function submitLocal() {
  await submit(() => api('/api/detection/quick', {
    method: 'POST',
    body: JSON.stringify(localForm)
  }), localForm.name, localForm.gitUrl, '本地路径')
}

async function submit(request, name, source, submitMode) {
  loading.value = true
  error.value = ''
  scanId.value = null
  try {
    const result = await request()
    scanId.value = result.scan.id
    history.value.unshift({ name, source, mode: submitMode, scanId: result.scan.id })
  } catch (err) {
    error.value = err.message
  } finally {
    loading.value = false
  }
}
</script>
