<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshCw, ShieldCheck, ShieldOff, UserX, UserCheck, ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { listAdminUsers, toggleAdminFlag, toggleUserStatus, type AdminUserResponse } from '../../api/admin'

const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(20)
const total = ref(0)
const users = ref<AdminUserResponse[]>([])

async function load() {
  loading.value = true
  try {
    const res = await listAdminUsers({ keyword: keyword.value || undefined, page: page.value, size: size.value })
    if (res.code !== 0) throw new Error(res.message)
    users.value = res.data.list
    total.value = res.data.total
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

function doSearch() {
  page.value = 1
  load()
}

async function handleToggleAdmin(user: AdminUserResponse) {
  const action = user.isAdmin === 1 ? '取消管理员' : '设为管理员'
  try {
    await ElMessageBox.confirm(`确认将 ${user.nickName || user.userName} ${action}？`, action, { type: 'warning' })
  } catch { return }
  try {
    const res = await toggleAdminFlag(user.userId)
    if (res.code !== 0) throw new Error(res.message)
    ElMessage.success(`已${action}`)
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '操作失败')
  }
}

async function handleToggleStatus(user: AdminUserResponse) {
  const action = user.isDeleted === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确认${action}用户 ${user.nickName || user.userName}？`, action, { type: 'warning' })
  } catch { return }
  try {
    const res = await toggleUserStatus(user.userId)
    if (res.code !== 0) throw new Error(res.message)
    ElMessage.success(`已${action}`)
    await load()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '操作失败')
  }
}

const totalPages = () => Math.ceil(total.value / size.value)

function formatTime(t: string): string {
  return new Date(t).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

onMounted(load)
</script>

<template>
  <div class="space-y-6">
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-neutral-900">用户管理</h1>
        <p class="text-sm text-neutral-500 mt-1">管理平台所有注册用户</p>
      </div>
      <button class="px-3 py-2 rounded-lg bg-white border border-neutral-200 text-sm font-medium text-neutral-600 hover:bg-neutral-50 flex items-center gap-2" :disabled="loading" @click="load">
        <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': loading }" />
        刷新
      </button>
    </div>

    <!-- 搜索 -->
    <div class="flex gap-3">
      <div class="relative flex-1 max-w-md">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-neutral-400" />
        <input
          v-model="keyword"
          class="w-full pl-10 pr-4 py-2.5 rounded-lg border border-neutral-200 text-sm focus:border-brand-500 focus:ring-1 focus:ring-brand-500 outline-none"
          placeholder="搜索用户名、昵称或手机号..."
          @keydown.enter="doSearch"
        />
      </div>
      <button class="px-4 py-2.5 rounded-lg bg-brand-600 hover:bg-brand-700 text-white text-sm font-bold" @click="doSearch">搜索</button>
    </div>

    <!-- 列表 -->
    <div class="bg-white rounded-xl border border-neutral-200 overflow-hidden">
      <div v-if="loading && users.length === 0" class="p-8 text-center text-neutral-400">加载中...</div>
      <div v-else-if="users.length === 0" class="p-8 text-center text-neutral-400">暂无数据</div>
      <table v-else class="w-full text-sm">
        <thead class="bg-neutral-50 border-b border-neutral-200">
          <tr>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">用户</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">手机号</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">企业</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">角色</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">状态</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">注册时间</th>
            <th class="px-4 py-3 text-right font-bold text-neutral-500 text-xs uppercase">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-neutral-100">
          <tr v-for="u in users" :key="u.userId" class="hover:bg-neutral-50 transition-colors">
            <td class="px-4 py-3">
              <div class="flex items-center gap-2">
                <span class="font-bold text-neutral-900">{{ u.nickName || u.userName }}</span>
                <span v-if="u.isAdmin === 1" class="px-1.5 py-0.5 rounded text-[9px] font-bold bg-amber-100 text-amber-700">管理员</span>
              </div>
              <span class="text-xs text-neutral-400">#{{ u.userId }}</span>
            </td>
            <td class="px-4 py-3 text-neutral-600">{{ u.phonenumber || '-' }}</td>
            <td class="px-4 py-3 text-neutral-600">{{ u.companyName || '-' }}</td>
            <td class="px-4 py-3">
              <div class="flex gap-1">
                <span v-if="u.isBuyer === 1" class="px-1.5 py-0.5 rounded text-[9px] font-bold bg-autumn-100 text-autumn-700">采购</span>
                <span v-if="u.isSeller === 1" class="px-1.5 py-0.5 rounded text-[9px] font-bold bg-brand-100 text-brand-700">供应</span>
              </div>
            </td>
            <td class="px-4 py-3">
              <span :class="u.isDeleted === 1 ? 'bg-red-100 text-red-700' : 'bg-brand-100 text-brand-700'" class="px-1.5 py-0.5 rounded text-[9px] font-bold">
                {{ u.isDeleted === 1 ? '已禁用' : '正常' }}
              </span>
            </td>
            <td class="px-4 py-3 text-neutral-500 text-xs">{{ formatTime(u.createTime) }}</td>
            <td class="px-4 py-3 text-right">
              <div class="flex items-center justify-end gap-2">
                <button
                  class="p-1.5 rounded-lg hover:bg-amber-50 transition-colors"
                  :title="u.isAdmin === 1 ? '取消管理员' : '设为管理员'"
                  @click="handleToggleAdmin(u)"
                >
                  <ShieldCheck v-if="u.isAdmin !== 1" class="w-4 h-4 text-amber-600" />
                  <ShieldOff v-else class="w-4 h-4 text-neutral-400" />
                </button>
                <button
                  class="p-1.5 rounded-lg hover:bg-red-50 transition-colors"
                  :title="u.isDeleted === 1 ? '启用用户' : '禁用用户'"
                  @click="handleToggleStatus(u)"
                >
                  <UserCheck v-if="u.isDeleted === 1" class="w-4 h-4 text-brand-600" />
                  <UserX v-else class="w-4 h-4 text-red-500" />
                </button>
              </div>
            </td>
          </tr>
        </tbody>
      </table>

      <!-- 分页 -->
      <div v-if="total > size" class="flex items-center justify-between px-4 py-3 border-t border-neutral-200 bg-neutral-50">
        <span class="text-xs text-neutral-500">共 {{ total }} 条记录</span>
        <div class="flex items-center gap-2">
          <button class="p-1.5 rounded hover:bg-neutral-200 disabled:opacity-50" :disabled="page <= 1" @click="page--; load()">
            <ChevronLeft class="w-4 h-4" />
          </button>
          <span class="text-sm text-neutral-600">{{ page }} / {{ totalPages() }}</span>
          <button class="p-1.5 rounded hover:bg-neutral-200 disabled:opacity-50" :disabled="page >= totalPages()" @click="page++; load()">
            <ChevronRight class="w-4 h-4" />
          </button>
        </div>
      </div>
    </div>
  </div>
</template>
