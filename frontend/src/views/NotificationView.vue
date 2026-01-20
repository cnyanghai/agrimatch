<script setup lang="ts">
import { computed, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { Bell, MessageSquare, Heart, AlertCircle, CheckCircle, Clock, RefreshCw, CheckCheck } from 'lucide-vue-next'
import { listMyNotify, markNotifyRead, markNotifyReadAll, type NotifyResponse } from '../api/notify'
import { BaseButton, EmptyState, Skeleton } from '../components/ui'

const router = useRouter()
const loading = ref(false)
const list = ref<NotifyResponse[]>([])

const unreadCount = computed(() => list.value.filter((x) => !x.read).length)

// 通知类型信息
function getNotifyTypeInfo(type: string) {
  const types: Record<string, { label: string; color: string; icon: any }> = {
    LIKE: { label: '点赞', color: 'pink', icon: Heart },
    COMMENT: { label: '评论', color: 'blue', icon: MessageSquare },
    SYSTEM: { label: '系统', color: 'amber', icon: AlertCircle },
    MESSAGE: { label: '消息', color: 'emerald', icon: Bell }
  }
  return types[type] || { label: type || '通知', color: 'gray', icon: Bell }
}

// 格式化时间
function formatTime(timeStr: string): string {
  if (!timeStr) return '-'
  const date = new Date(timeStr)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)} 分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)} 小时前`
  if (diff < 604800000) return `${Math.floor(diff / 86400000)} 天前`
  
  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

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

onMounted(refresh)
</script>

<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">消息通知</h1>
        <p class="text-sm text-gray-500 mt-1">点赞、评论、系统消息汇总</p>
      </div>
      <div class="flex items-center gap-3">
        <!-- 未读数量标签 -->
        <span 
          :class="[
            'px-3 py-1.5 text-xs font-bold rounded-full border',
            unreadCount > 0 
              ? 'bg-red-50 text-red-600 border-red-100' 
              : 'bg-gray-50 text-gray-500 border-gray-100'
          ]"
        >
          {{ unreadCount > 0 ? `${unreadCount} 条未读` : '全部已读' }}
        </span>
        
        <BaseButton type="secondary" size="sm" :loading="loading" @click="refresh">
          <RefreshCw class="w-4 h-4" />
          刷新
        </BaseButton>
        
        <BaseButton 
          v-if="unreadCount > 0"
          type="outline" 
          size="sm" 
          :loading="loading" 
          @click="onReadAll"
        >
          <CheckCheck class="w-4 h-4" />
          全部已读
        </BaseButton>
      </div>
    </div>

    <!-- 通知列表 -->
    <div class="bg-white rounded-2xl border border-gray-100 overflow-hidden">
      <!-- 加载状态 -->
      <div v-if="loading && list.length === 0" class="p-6 space-y-4">
        <Skeleton type="card" />
        <Skeleton type="card" />
        <Skeleton type="card" />
      </div>
      
      <!-- 空状态 -->
      <EmptyState 
        v-else-if="list.length === 0" 
        type="message"
        title="暂无通知"
        description="有新的消息时，将会显示在这里"
        size="md"
      />
      
      <!-- 通知卡片列表 -->
      <div v-else class="divide-y divide-gray-50">
        <div 
          v-for="(notify, index) in list" 
          :key="notify.id"
          :class="[
            'p-5 transition-all cursor-pointer animate-stagger-in',
            notify.read ? 'bg-white hover:bg-gray-50' : 'bg-emerald-50/30 hover:bg-emerald-50/50'
          ]"
          :style="{ animationDelay: `${index * 30}ms` }"
          @click="onRead(notify)"
        >
          <div class="flex items-start gap-4">
            <!-- 类型图标 -->
            <div 
              :class="[
                'w-10 h-10 rounded-xl flex items-center justify-center flex-shrink-0',
                `bg-${getNotifyTypeInfo(notify.type).color}-50`
              ]"
            >
              <component 
                :is="getNotifyTypeInfo(notify.type).icon" 
                :class="['w-5 h-5', `text-${getNotifyTypeInfo(notify.type).color}-600`]"
              />
            </div>
            
            <!-- 内容 -->
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <h3 :class="['font-bold', notify.read ? 'text-gray-700' : 'text-gray-900']">
                  {{ notify.title || getNotifyTypeInfo(notify.type).label }}
                </h3>
                <span 
                  :class="[
                    'px-2 py-0.5 rounded-full text-[10px] font-bold uppercase',
                    `bg-${getNotifyTypeInfo(notify.type).color}-50 text-${getNotifyTypeInfo(notify.type).color}-600`
                  ]"
                >
                  {{ getNotifyTypeInfo(notify.type).label }}
                </span>
                <!-- 未读标记 -->
                <span v-if="!notify.read" class="w-2 h-2 rounded-full bg-red-500"></span>
              </div>
              
              <p :class="['text-sm line-clamp-2', notify.read ? 'text-gray-500' : 'text-gray-600']">
                {{ notify.content || '无详细内容' }}
              </p>
              
              <div class="flex items-center gap-2 mt-2 text-xs text-gray-400">
                <Clock class="w-3 h-3" />
                <span>{{ formatTime(notify.createTime) }}</span>
                <span v-if="notify.read" class="flex items-center gap-1 text-emerald-500">
                  <CheckCircle class="w-3 h-3" />
                  已读
                </span>
              </div>
            </div>
            
            <!-- 操作 -->
            <div class="flex-shrink-0">
              <BaseButton 
                v-if="!notify.read"
                type="primary" 
                size="sm"
                @click.stop="onRead(notify)"
              >
                标记已读
              </BaseButton>
              <BaseButton 
                v-else-if="notify.link"
                type="ghost" 
                size="sm"
                @click.stop="onRead(notify)"
              >
                查看详情
              </BaseButton>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* 动态颜色类 */
.bg-pink-50 { background-color: rgb(253 242 248); }
.bg-blue-50 { background-color: rgb(239 246 255); }
.bg-amber-50 { background-color: rgb(255 251 235); }
.bg-emerald-50 { background-color: rgb(236 253 245); }
.bg-gray-50 { background-color: rgb(249 250 251); }

.text-pink-600 { color: rgb(219 39 119); }
.text-blue-600 { color: rgb(37 99 235); }
.text-amber-600 { color: rgb(217 119 6); }
.text-emerald-600 { color: rgb(5 150 105); }
.text-gray-600 { color: rgb(75 85 99); }

.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>
