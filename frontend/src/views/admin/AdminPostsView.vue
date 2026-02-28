<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { showToast } from '@/composables/useToast'
import { showConfirm } from '@/composables/useConfirm'
import { Search, RefreshCw, Trash2, ChevronLeft, ChevronRight } from 'lucide-vue-next'
import { listAdminPosts, deleteAdminPost, type AdminPostResponse } from '../../api/admin'

const loading = ref(false)
const keyword = ref('')
const page = ref(1)
const size = ref(20)
const total = ref(0)
const posts = ref<AdminPostResponse[]>([])

async function load() {
  loading.value = true
  try {
    const res = await listAdminPosts({ keyword: keyword.value || undefined, page: page.value, size: size.value })
    if (res.code !== 0) throw new Error(res.message)
    posts.value = res.data.list
    total.value = res.data.total
  } catch (e: any) {
    showToast.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

function doSearch() {
  page.value = 1
  load()
}

async function handleDelete(post: AdminPostResponse) {
  const ok = await showConfirm({ title: '删除确认', message: `确认删除话题「${post.title}」？此操作不可恢复。`, type: 'warning' })
  if (!ok) return
  try {
    const res = await deleteAdminPost(post.id)
    if (res.code !== 0) throw new Error(res.message)
    showToast.success('已删除')
    await load()
  } catch (e: any) {
    showToast.error(e?.message ?? '操作失败')
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
        <h1 class="text-2xl font-bold text-neutral-900">话题管理</h1>
        <p class="text-sm text-neutral-500 mt-1">管理平台社区话题内容</p>
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
          placeholder="搜索话题标题..."
          @keydown.enter="doSearch"
        />
      </div>
      <button class="px-4 py-2.5 rounded-lg bg-brand-600 hover:bg-brand-700 text-white text-sm font-bold" @click="doSearch">搜索</button>
    </div>

    <!-- 列表 -->
    <div class="bg-white rounded-xl border border-neutral-200 overflow-hidden">
      <div v-if="loading && posts.length === 0" class="p-8 text-center text-neutral-400">加载中...</div>
      <div v-else-if="posts.length === 0" class="p-8 text-center text-neutral-400">暂无数据</div>
      <table v-else class="w-full text-sm">
        <thead class="bg-neutral-50 border-b border-neutral-200">
          <tr>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">标题</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">作者</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">点赞</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">评论</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">状态</th>
            <th class="px-4 py-3 text-left font-bold text-neutral-500 text-xs uppercase">发布时间</th>
            <th class="px-4 py-3 text-right font-bold text-neutral-500 text-xs uppercase">操作</th>
          </tr>
        </thead>
        <tbody class="divide-y divide-neutral-100">
          <tr v-for="post in posts" :key="post.id" class="hover:bg-neutral-50 transition-colors">
            <td class="px-4 py-3">
              <span class="font-bold text-neutral-900 line-clamp-1">{{ post.title }}</span>
            </td>
            <td class="px-4 py-3 text-neutral-600">{{ post.authorName }}</td>
            <td class="px-4 py-3 text-neutral-600">{{ post.likeCount }}</td>
            <td class="px-4 py-3 text-neutral-600">{{ post.commentCount }}</td>
            <td class="px-4 py-3">
              <span :class="['px-1.5 py-0.5 rounded text-[9px] font-bold', post.isDeleted === 1 ? 'bg-red-100 text-red-700' : 'bg-brand-100 text-brand-700']">
                {{ post.isDeleted === 1 ? '已删除' : '正常' }}
              </span>
            </td>
            <td class="px-4 py-3 text-neutral-500 text-xs">{{ formatTime(post.createTime) }}</td>
            <td class="px-4 py-3 text-right">
              <button
                v-if="post.isDeleted !== 1"
                class="p-1.5 rounded-lg hover:bg-red-50 transition-colors"
                title="删除"
                @click="handleDelete(post)"
              >
                <Trash2 class="w-4 h-4 text-red-500" />
              </button>
              <span v-else class="text-xs text-neutral-400">已删除</span>
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
