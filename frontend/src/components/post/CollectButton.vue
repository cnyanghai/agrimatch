<script setup lang="ts">
import { ref } from 'vue'
import { Star } from 'lucide-vue-next'
import { togglePostCollect } from '../../api/post'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '../../store/auth'
import { requireAuth } from '../../utils/requireAuth'

const props = defineProps<{
  postId: number
  initialStatus?: boolean
}>()

const auth = useAuthStore()
const isCollected = ref(props.initialStatus ?? false)
const loading = ref(false)

async function onToggle() {
  if (!requireAuth()) return
  
  loading.value = true
  try {
    const r = await togglePostCollect(props.postId)
    if (r.code === 0) {
      isCollected.value = r.data ?? !isCollected.value
      ElMessage.success(isCollected.value ? '已收藏' : '已取消收藏')
    } else {
      throw new Error(r.message)
    }
  } catch (e: any) {
    ElMessage.error(e.message || '操作失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <button 
    class="flex items-center gap-1.5 transition-all active:scale-95 disabled:opacity-50"
    :class="isCollected ? 'text-amber-500' : 'text-gray-400 hover:text-amber-500'"
    :disabled="loading"
    @click.stop="onToggle"
  >
    <Star :size="18" :fill="isCollected ? 'currentColor' : 'none'" :stroke-width="isCollected ? 0 : 2" />
    <span class="text-xs font-bold">{{ isCollected ? '已收藏' : '收藏' }}</span>
  </button>
</template>

