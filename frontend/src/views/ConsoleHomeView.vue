<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../store/auth'
import { ArrowRight, ShoppingCart, Box, ChatDotRound, Location } from '@element-plus/icons-vue'

const router = useRouter()
const auth = useAuthStore()
const loading = ref(false)

const isBuyer = computed(() => auth.me?.isBuyer === 1)

const followedUpdates = ref<any[]>([])

// 快捷操作配置 - 根据角色显示不同图标颜色
const quickActions = computed(() => {
  if (isBuyer.value) {
    return [
      { title: '发布采购', desc: '快速发布采购需求', icon: ShoppingCart, path: '/requirements', color: 'orange' },
      { title: '浏览供应', desc: '查找优质供应商', icon: Box, path: '/supply-browse', color: 'emerald' },
      { title: '地图找商', desc: '附近供应商地图', icon: Location, path: '/map', color: 'blue' },
      { title: '商务聊天', desc: '在线沟通洽谈', icon: ChatDotRound, path: '/chat', color: 'indigo' }
    ]
  }
  return [
    { title: '发布供应', desc: '展示您的产品', icon: Box, path: '/supply', color: 'emerald' },
    { title: '浏览采购', desc: '查找采购商', icon: ShoppingCart, path: '/requirement-browse', color: 'orange' },
    { title: '地图展示', desc: '让客户找到您', icon: Location, path: '/map', color: 'blue' },
    { title: '商务聊天', desc: '在线沟通洽谈', icon: ChatDotRound, path: '/chat', color: 'indigo' }
  ]
})

// 获取图标容器的样式类
function getIconClass(color: string) {
  const colorMap: Record<string, string> = {
    indigo: 'bg-indigo-50 text-indigo-600 group-hover:bg-indigo-600 group-hover:text-white',
    emerald: 'bg-emerald-50 text-emerald-600 group-hover:bg-emerald-600 group-hover:text-white',
    orange: 'bg-orange-50 text-orange-600 group-hover:bg-orange-600 group-hover:text-white',
    blue: 'bg-blue-50 text-blue-600 group-hover:bg-blue-600 group-hover:text-white'
  }
  return colorMap[color] || colorMap.indigo
}

async function loadFollowedUpdates() {
  loading.value = true
  try {
    await new Promise((r) => setTimeout(r, 150))
    followedUpdates.value = isBuyer.value
      ? [
          { id: 1, type: 'supply', title: '优质小麦现货', category: '小麦', quantity: '200吨', price: '2850', location: '山东', company: '山东粮食集团', avatar: '山', time: '10分钟前' },
          { id: 2, type: 'supply', title: '东北玉米直供', category: '玉米', quantity: '500吨', price: '2520', location: '黑龙江', company: '黑龙江优农合作社', avatar: '黑', time: '30分钟前' }
        ]
      : [
          { id: 1, type: 'demand', title: '急采优质小麦', category: '小麦', quantity: '500吨', price: '2900', location: '北京', company: '北京粮油贸易公司', avatar: '北', time: '20分钟前' },
          { id: 2, type: 'demand', title: '长期采购玉米', category: '玉米', quantity: '1000吨', price: '2650', location: '上海', company: '上海食品加工厂', avatar: '上', time: '1小时前' }
        ]
  } finally {
    loading.value = false
  }
}

function go(path: string) {
  router.push(path)
}

onMounted(() => {
  loadFollowedUpdates()
})
</script>

<template>
  <div class="min-h-screen bg-slate-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 space-y-8">
      
      <!-- 欢迎区域 -->
      <section class="bg-white rounded-[24px] border shadow-sm p-8">
        <div class="flex items-center justify-between">
          <div>
            <h2 class="text-2xl font-bold text-gray-800 mb-2">
              下午好，{{ auth.me?.nickName || auth.me?.userName || '用户' }}
            </h2>
            <p class="text-sm text-gray-500">
              {{ isBuyer ? '今日有新供应信息等待您查看' : '今日有新采购需求等待您报价' }}
            </p>
          </div>
          <div>
            <span 
              class="text-[10px] font-bold px-3 py-1.5 rounded-full uppercase tracking-wider"
              :class="isBuyer ? 'bg-emerald-100 text-emerald-700 border border-emerald-200' : 'bg-indigo-100 text-indigo-700 border border-indigo-200'"
            >
              {{ isBuyer ? '采购商' : '供应商' }}
            </span>
          </div>
        </div>
      </section>

      <!-- 数据统计卡片 -->
      <div class="grid grid-cols-1 md:grid-cols-4 gap-6">
        <div class="bg-white p-6 rounded-[24px] border shadow-sm">
          <p class="text-xs font-bold text-gray-400 mb-2 uppercase tracking-wider">正在销售</p>
          <div class="flex items-end gap-2">
            <span class="text-2xl font-black text-gray-800">12</span>
            <span class="text-xs text-emerald-500 font-bold pb-1">张挂单</span>
          </div>
        </div>
        <div class="bg-white p-6 rounded-[24px] border shadow-sm">
          <p class="text-xs font-bold text-gray-400 mb-2 uppercase tracking-wider">今日询价</p>
          <div class="flex items-end gap-2">
            <span class="text-2xl font-black text-gray-800">28</span>
            <span class="text-xs text-indigo-500 font-bold pb-1">次访问</span>
          </div>
        </div>
        <div class="bg-white p-6 rounded-[24px] border shadow-sm">
          <p class="text-xs font-bold text-gray-400 mb-2 uppercase tracking-wider">累计成交</p>
          <div class="flex items-end gap-2">
            <span class="text-2xl font-black text-gray-800">1,420</span>
            <span class="text-xs text-gray-500 font-bold pb-1">吨</span>
          </div>
        </div>
        <div class="bg-white p-6 rounded-[24px] border shadow-sm">
          <p class="text-xs font-bold text-gray-400 mb-2 uppercase tracking-wider">待处理</p>
          <div class="flex items-end gap-2">
            <span class="text-2xl font-black text-gray-800">3</span>
            <span class="text-xs text-amber-500 font-bold pb-1">项进行中</span>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <section>
        <div class="flex items-center justify-between mb-6">
          <h3 class="text-lg font-bold text-gray-800">快捷操作</h3>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <button
            v-for="action in quickActions"
            :key="action.title"
            class="flex items-center gap-4 bg-white p-4 rounded-2xl border shadow-sm hover:shadow-md transition-all group cursor-pointer"
            @click="go(action.path)"
          >
            <div :class="`w-12 h-12 rounded-xl flex items-center justify-center transition-all ${getIconClass(action.color)}`">
              <el-icon :size="24"><component :is="action.icon" /></el-icon>
            </div>
            <div class="text-left flex-1">
              <p class="font-bold text-gray-800 group-hover:text-indigo-600 transition-colors">{{ action.title }}</p>
              <p class="text-[10px] text-gray-400 font-medium">{{ action.desc }}</p>
            </div>
          </button>
        </div>
      </section>

      <!-- 关注商户动态 -->
      <section class="bg-white rounded-[24px] border shadow-sm overflow-hidden">
        <div class="px-8 py-6 border-b border-gray-100 flex items-center justify-between">
          <div class="flex items-center gap-2">
            <div class="w-1.5 h-6 bg-indigo-600 rounded-full"></div>
            <h3 class="text-lg font-bold text-gray-800">
              关注商户动态
            </h3>
            <span class="text-xs font-normal text-gray-400 ml-2">
              {{ isBuyer ? '关注的供应商发布的供应信息' : '关注的采购商发布的采购需求' }}
            </span>
          </div>
          <button 
            class="px-4 py-2 bg-indigo-50 text-indigo-600 rounded-xl text-sm font-bold hover:bg-indigo-100 transition-all flex items-center gap-2"
            @click="go(isBuyer ? '/supply-browse' : '/requirement-browse')"
          >
            查看更多
            <el-icon :size="16"><ArrowRight /></el-icon>
          </button>
        </div>

        <div v-loading="loading">
          <div
            v-for="item in followedUpdates"
            :key="item.id"
            class="px-8 py-4 border-b border-gray-50 last:border-b-0 hover:bg-gray-50/50 cursor-pointer transition-colors"
          >
            <div class="flex items-start gap-4">
              <div
                class="w-10 h-10 rounded-full flex items-center justify-center text-white text-sm font-bold flex-shrink-0"
                :class="item.type === 'supply' ? 'bg-emerald-500' : 'bg-indigo-500'"
              >
                {{ item.avatar }}
              </div>
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-2 mb-1">
                  <span class="font-bold text-gray-800">{{ item.company }}</span>
                  <span class="text-xs text-gray-400 ml-auto">{{ item.time }}</span>
                </div>
                <div class="flex items-center gap-2 mb-2">
                  <span 
                    class="text-[10px] font-bold px-2 py-0.5 rounded-full uppercase tracking-wider"
                    :class="item.type === 'supply' ? 'bg-emerald-50 text-emerald-600 border border-emerald-100' : 'bg-indigo-50 text-indigo-600 border border-indigo-100'"
                  >
                    {{ item.type === 'supply' ? '供应' : '采购' }}
                  </span>
                  <span class="text-gray-700 font-medium">{{ item.title }}</span>
                </div>
                <div class="flex items-center gap-4 text-xs text-gray-500">
                  <span>📦 {{ item.category }}</span>
                  <span>📍 {{ item.location }}</span>
                  <span class="text-orange-600 font-bold">¥{{ item.price }}/吨</span>
                  <span>{{ item.quantity }}</span>
                </div>
              </div>
            </div>
          </div>

          <div v-if="followedUpdates.length === 0 && !loading" class="py-12 text-center">
            <div class="text-5xl mb-4">⭐</div>
            <div class="text-gray-600 font-bold mb-2">您还没有关注任何商户</div>
            <div class="text-gray-400 text-sm mb-6">关注商户后，这里会显示他们发布的最新信息</div>
            <button 
              class="px-6 py-2.5 bg-indigo-600 text-white rounded-xl text-sm font-bold shadow-lg shadow-indigo-100 hover:bg-indigo-700 transition-all"
              @click="go(isBuyer ? '/supply-browse' : '/requirement-browse')"
            >
              去发现优质商户
            </button>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;600;700&display=swap');
</style>
