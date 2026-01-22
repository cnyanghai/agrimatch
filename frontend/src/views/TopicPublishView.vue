<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { createPost } from '../api/post'
import { getPointsMe } from '../api/points'
import { useAuthStore } from '../store/auth'
import { requireAuth } from '../utils/requireAuth'

const router = useRouter()
const auth = useAuthStore()

type PostType = 'general' | 'bounty' | 'poll'
const postType = ref<PostType>('general')

const title = ref('')
const content = ref('')
const submitting = ref(false)

const bountyPoints = ref(50)
const myPoints = ref(0)
const showBounty = computed(() => postType.value === 'bounty')
const pointsEnough = computed(() => myPoints.value >= bountyPoints.value)

function close() {
  router.back()
}

async function loadMyPoints() {
  try {
    const r = await getPointsMe()
    if (r.code === 0 && r.data) {
      myPoints.value = r.data.pointsBalance ?? 0
    }
  } catch {
    // ignore
  }
}

async function submit() {
  if (!title.value.trim()) return ElMessage.warning('请输入标题')
  if (!content.value.trim()) return ElMessage.warning('请输入内容')
  
  // 检查登录状态
  if (!requireAuth('/talks/publish')) {
    ElMessage.warning('请先登录后再发布话题')
    return
  }

  // 赏金类型需要确认
  if (postType.value === 'bounty') {
    if (myPoints.value < bountyPoints.value) {
      ElMessage.warning(`积分不足，当前余额：${myPoints.value}，需要：${bountyPoints.value}`)
      return
    }
    try {
      await ElMessageBox.confirm(
        `发布后将扣除 ${bountyPoints.value} 积分，采纳回答后积分将发放给被采纳者。`,
        '确认发布赏金求助',
        { confirmButtonText: '确认发布', cancelButtonText: '取消', type: 'warning' }
      )
    } catch {
      return // 用户取消
    }
  }
  
  submitting.value = true
  try {
    const r = await createPost({ 
      title: title.value.trim(), 
      content: content.value.trim() || undefined,
      postType: postType.value,
      bountyPoints: postType.value === 'bounty' ? bountyPoints.value : undefined
    })
    if (r.code !== 0) throw new Error(r.message)
    ElMessage.success('发布成功！')
    // 清空表单
    title.value = ''
    content.value = ''
    postType.value = 'general'
    // 返回话题广场
    router.push('/talks')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '发布失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

onMounted(() => {
  // 检查登录状态
  if (!auth.me) requireAuth('/talks/publish')
  loadMyPoints()
})
</script>

<template>
  <div class="bg-gray-100 min-h-screen flex items-center justify-center p-4 sm:p-6">
    <div class="fixed inset-0 z-50 flex items-center justify-center p-4 sm:p-6">
      <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-sm" @click="close"></div>

      <div class="relative bg-white w-full max-w-2xl rounded-2xl shadow-2xl overflow-hidden flex flex-col animate-fade-in animate-zoom-in">
        <!-- 头部 -->
        <div class="px-8 py-6 border-b flex items-center justify-between bg-white sticky top-0 z-10">
          <div class="flex items-center gap-3">
            <div class="w-10 h-10 bg-indigo-50 rounded-lg flex items-center justify-center text-indigo-600">
              <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 10h.01M12 10h.01M16 10h.01M9 16H5a2 2 0 01-2-2V6a2 2 0 012-2h14a2 2 0 012 2v8a2 2 0 01-2 2h-5l-5 5v-5z"></path></svg>
            </div>
            <div>
              <h2 class="text-xl font-bold text-gray-900">发布新话题</h2>
              <p class="text-[10px] text-gray-400 font-medium uppercase tracking-wider">分享行业见解，共建透明市场</p>
            </div>
          </div>
          <button class="p-2 hover:bg-gray-100 rounded-full text-gray-400 transition-colors" @click="close">
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path></svg>
          </button>
        </div>

        <!-- 主体 -->
        <div class="flex-1 overflow-y-auto p-8 space-y-8 max-h-[70vh]">
          <section>
            <label class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-4 block">选择发布类型</label>
            <div class="grid grid-cols-3 gap-4">
              <button
                class="type-btn flex flex-col items-center gap-2 p-4 rounded-lg border-2 transition-all"
                :class="postType === 'general' ? 'border-indigo-600 bg-indigo-50 text-indigo-600' : 'border-gray-200 text-gray-500 hover:border-indigo-200'"
                @click="postType = 'general'"
              >
                <span class="text-sm font-bold">普通话题</span>
              </button>
              <button
                class="type-btn flex flex-col items-center gap-2 p-4 rounded-lg border-2 transition-all"
                :class="postType === 'bounty' ? 'border-amber-500 bg-amber-50 text-amber-600' : 'border-gray-200 text-gray-500 hover:border-amber-200'"
                @click="postType = 'bounty'"
              >
                <span class="text-sm font-bold">赏金求助</span>
              </button>
              <button
                class="type-btn flex flex-col items-center gap-2 p-4 rounded-lg border-2 transition-all"
                :class="postType === 'poll' ? 'border-brand-500 bg-brand-50 text-brand-600' : 'border-gray-200 text-gray-500 hover:border-brand-200'"
                @click="postType = 'poll'"
              >
                <span class="text-sm font-bold">发起投票</span>
              </button>
            </div>
          </section>

          <section class="space-y-4">
            <input v-model="title" type="text" placeholder="给你的话题起个响亮的标题" class="w-full text-2xl font-bold placeholder:text-gray-300 outline-none border-none" />
            <div class="h-px bg-gray-100 w-full"></div>
            <textarea v-model="content" placeholder="分享你的见解、经验或疑问..." class="w-full h-40 resize-none outline-none text-gray-700 leading-relaxed placeholder:text-gray-300"></textarea>
          </section>

          <div v-if="showBounty" class="bg-amber-50 rounded-lg p-6 border border-amber-100">
            <div class="flex justify-between items-center mb-4">
              <span class="font-bold text-amber-900">设置悬赏积分</span>
              <span class="text-xl font-black text-amber-600">{{ bountyPoints }} pts</span>
            </div>
            <input v-model="bountyPoints" type="range" min="10" max="500" class="w-full h-2 bg-amber-200 rounded-lg appearance-none cursor-pointer accent-amber-500" />
            <div class="flex items-center justify-between mt-4">
              <p class="text-[10px] text-amber-700 leading-relaxed">* 发布后积分将被扣除，采纳回答后发放给被采纳者。</p>
              <span 
                class="text-xs font-bold px-2 py-1 rounded-lg"
                :class="pointsEnough ? 'bg-brand-100 text-brand-700' : 'bg-red-100 text-red-600'"
              >
                当前余额: {{ myPoints }} pts
              </span>
            </div>
            <p v-if="!pointsEnough" class="text-xs text-red-500 mt-2 font-bold">积分不足，请先充值或降低悬赏额度</p>
          </div>
        </div>

        <!-- 底部 -->
        <div class="px-8 py-6 bg-gray-50 border-t flex items-center justify-between">
          <div class="flex items-center gap-2">
            <div class="w-2 h-2 bg-brand-500 rounded-full animate-pulse"></div>
            <span class="text-[10px] text-gray-500 font-bold">账号已登录 · 发布可获得积分</span>
          </div>
          <div class="flex gap-4">
            <button class="text-sm font-bold text-gray-400 hover:text-gray-600" @click="close">取消</button>
            <button 
              class="bg-indigo-600 text-white px-8 py-2.5 rounded-lg text-sm font-bold shadow-md shadow-indigo-100 hover:bg-indigo-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed" 
              :disabled="submitting"
              @click="submit"
            >
              {{ submitting ? '发布中...' : '立即发布' }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.animate-fade-in {
  animation: fadeIn 0.3s ease-out;
}
.animate-zoom-in {
  animation: zoomIn 0.3s ease-out;
}
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}
@keyframes zoomIn {
  from {
    transform: scale(0.95);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}
</style>


