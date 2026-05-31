<template>
  <section class="grid">
    <div class="page-heading">
      <div><h1>团队成员</h1><p class="muted">管理员可创建控制台成员账号。</p></div>
      <button @click="load">刷新</button>
    </div>
    <form class="panel grid two" @submit.prevent="create">
      <label>用户名<input v-model="form.username" required /></label>
      <label>密码<input v-model="form.password" type="password" required /></label>
      <label>角色<select v-model="form.role"><option value="USER">成员</option><option value="ADMIN">管理员</option></select></label>
      <div class="actions"><button type="submit">新增成员</button></div>
      <p v-if="error" class="danger">{{ error }}</p>
    </form>
    <div class="panel">
      <table>
        <thead><tr><th>ID</th><th>用户名</th><th>角色</th><th>新密码</th><th>操作</th></tr></thead>
        <tbody>
          <tr v-for="item in members" :key="item.id">
            <td>{{ item.id }}</td>
            <td>{{ item.username }}</td>
            <td>
              <select v-model="item.role">
                <option value="USER">成员</option>
                <option value="ADMIN">管理员</option>
              </select>
            </td>
            <td><input v-model="item.password" type="password" placeholder="留空不修改" /></td>
            <td class="actions">
              <button class="ghost" @click="update(item)">保存</button>
              <button class="ghost danger" @click="remove(item)">删除</button>
            </td>
          </tr>
          <tr v-if="!members.length"><td colspan="5" class="muted">暂无成员</td></tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { api } from '../api/client'

const members = ref([])
const error = ref('')
const form = reactive({ username: '', password: '', role: 'USER' })

async function load() {
  members.value = (await api('/api/team/members')).map((item) => ({ ...item, password: '' }))
}

async function create() {
  error.value = ''
  try {
    await api('/api/team/members', { method: 'POST', body: JSON.stringify(form) })
    Object.assign(form, { username: '', password: '', role: 'USER' })
    await load()
  } catch (err) {
    error.value = err.message
  }
}

function roleLabel(role) {
  return role === 'ADMIN' ? '管理员' : '成员'
}

async function update(item) {
  error.value = ''
  try {
    await api(`/api/team/members/${item.id}`, {
      method: 'PUT',
      body: JSON.stringify({ role: item.role, password: item.password })
    })
    await load()
  } catch (err) {
    error.value = err.message
  }
}

async function remove(item) {
  if (!window.confirm(`确认删除成员「${item.username}」？`)) return
  error.value = ''
  try {
    await api(`/api/team/members/${item.id}`, { method: 'DELETE' })
    await load()
  } catch (err) {
    error.value = err.message
  }
}

onMounted(load)
</script>
