<template>
  <section class="grid">
    <div class="page-heading">
      <div>
        <h1>项目管理</h1>
        <p class="muted">管理检测项目、扫描入口和项目令牌。</p>
      </div>
      <button @click="load">刷新</button>
    </div>

    <div class="panel">
      <table>
        <thead><tr><th>名称</th><th>地址</th><th>分支</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="project in projects" :key="project.id">
            <td><RouterLink :to="`/projects/${project.id}`">{{ project.name }}</RouterLink></td>
            <td>{{ project.gitUrl }}</td>
            <td>{{ project.defaultBranch }}</td>
            <td class="actions">
              <button class="ghost" @click="edit(project)">编辑</button>
              <button class="ghost" @click="scan(project.id)">触发扫描</button>
              <RouterLink class="button ghost" :to="`/projects/${project.id}`">详情</RouterLink>
              <button class="ghost danger" @click="remove(project)">删除</button>
            </td>
          </tr>
          <tr v-if="!projects.length"><td colspan="4" class="muted">暂无项目</td></tr>
        </tbody>
      </table>
    </div>

    <form v-if="editing" class="panel grid two" @submit.prevent="update">
      <h2>编辑项目</h2>
      <label>名称<input v-model="editing.name" required /></label>
      <label>Git 地址 / 本地路径<input v-model="editing.gitUrl" required /></label>
      <label>默认分支<input v-model="editing.defaultBranch" /></label>
      <div class="actions">
        <button type="submit">保存</button>
        <button type="button" class="ghost" @click="editing = null">取消</button>
      </div>
    </form>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { api } from '../api/client'

const projects = ref([])
const editing = ref(null)

async function load() {
  projects.value = await api('/api/projects')
}

async function scan(id) {
  await api(`/api/projects/${id}/scans`, { method: 'POST' })
}

function edit(project) {
  editing.value = { ...project }
}

async function update() {
  await api(`/api/projects/${editing.value.id}`, {
    method: 'PUT',
    body: JSON.stringify(editing.value)
  })
  editing.value = null
  await load()
}

async function remove(project) {
  if (!window.confirm(`确认删除项目「${project.name}」及其扫描数据？`)) return
  await api(`/api/projects/${project.id}`, { method: 'DELETE' })
  if (editing.value && editing.value.id === project.id) editing.value = null
  await load()
}

onMounted(load)
</script>
