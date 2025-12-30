<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { listMyNotify, markNotifyRead, markNotifyReadAll, type NotifyResponse } from '../api/notify'
import { useRouter } from 'vue-router'

const router = useRouter()
const loading = ref(false)
const list = ref<NotifyResponse[]>([])

const unreadCount = computed(() => list.value.filter((x) => !x.read).length)

async function refresh() {
  loading.value = true
  try {
    const r = await listMyNotify()
    if (r.code !== 0) throw new Error(r.message)
    list.value = r.data ?? []
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

async function onRead(row: NotifyResponse) {
  try {
    const r = await markNotifyRead(row.id)
    if (r.code !== 0) throw new Error(r.message)
    row.read = true
    if (row.link) router.push(row.link)
  } catch (e: any) {
    ElMessage.error(e?.message ?? '操作失败')
  }
}

async function onReadAll() {
  try {
    const r = await markNotifyReadAll()
    if (r.code !== 0) throw new Error(r.message)
    ElMessage.success('已全部标记已读')
    await refresh()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '操作失败')
  }
}

refresh()
</script>

<template>
  <div class="space-y-4">
    <div class="bg-white rounded-xl shadow-card border border-gray-100 px-5 py-4 flex items-center justify-between">
      <div>
        <div class="text-xl font-bold text-gray-800">消息通知</div>
        <div class="text-sm text-gray-500">点赞/评论/系统消息汇总</div>
      </div>
      <div class="flex gap-2 items-center">
        <el-tag :type="unreadCount > 0 ? 'danger' : 'info'">未读：{{ unreadCount }}</el-tag>
        <el-button :loading="loading" @click="refresh">刷新</el-button>
        <el-button :loading="loading" @click="onReadAll">全部已读</el-button>
      </div>
    </div>

    <div class="bg-white rounded-xl shadow-card border border-gray-100 p-5">
      <el-table :data="list" v-loading="loading" style="width:100%;">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column prop="type" label="类型" width="140" />
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="content" label="内容" min-width="240" />
        <el-table-column prop="createTime" label="时间" min-width="180" />
        <el-table-column label="状态" width="120">
          <template #default="{ row }">
            <el-tag :type="row.read ? 'success' : 'warning'">{{ row.read ? '已读' : '未读' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link @click="onRead(row)">{{ row.read ? '查看' : '标记并查看' }}</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>


