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

const quickActions = computed(() => {
  if (isBuyer.value) {
    return [
      { title: '发布采购', desc: '快速发布采购需求', icon: ShoppingCart, path: '/requirements' },
      { title: '浏览供应', desc: '查找优质供应商', icon: Box, path: '/supply-browse' },
      { title: '地图找商', desc: '附近供应商地图', icon: Location, path: '/map' },
      { title: '商务聊天', desc: '在线沟通洽谈', icon: ChatDotRound, path: '/chat' }
    ]
  }
  return [
    { title: '发布供应', desc: '展示您的产品', icon: Box, path: '/supply' },
    { title: '浏览采购', desc: '查找采购商', icon: ShoppingCart, path: '/requirement-browse' },
    { title: '地图展示', desc: '让客户找到您', icon: Location, path: '/map' },
    { title: '商务聊天', desc: '在线沟通洽谈', icon: ChatDotRound, path: '/chat' }
  ]
})

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
  <div class="max-w-6xl mx-auto">
    <!-- 欢迎 -->
    <section class="bg-white rounded-2xl shadow-sm p-6 mb-6">
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-xl font-bold text-gray-800">
            👋 你好，{{ auth.me?.nickName || auth.me?.userName || '用户' }}！
          </h2>
          <p class="text-gray-500 mt-1">
            {{ isBuyer ? '今日有新供应信息等待您查看' : '今日有新采购需求等待您报价' }}
          </p>
        </div>
        <el-tag :type="isBuyer ? 'success' : 'warning'" effect="light" class="!rounded-lg">
          {{ isBuyer ? '采购商' : '供应商' }}
        </el-tag>
      </div>
    </section>

    <!-- 快捷入口 -->
    <section class="mb-6">
      <div class="flex items-center justify-between mb-4">
        <h3 class="text-lg font-semibold text-gray-800">快捷操作</h3>
      </div>
      <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
        <div
          v-for="action in quickActions"
          :key="action.title"
          class="bg-white rounded-2xl shadow-sm p-5 cursor-pointer hover:shadow-md transition-all group"
          @click="go(action.path)"
        >
          <div class="flex items-center gap-4">
            <div class="w-12 h-12 rounded-xl flex items-center justify-center text-white bg-blue-600">
              <el-icon :size="24"><component :is="action.icon" /></el-icon>
            </div>
            <div class="flex-1">
              <div class="font-medium text-gray-800 group-hover:text-blue-600 transition-colors">{{ action.title }}</div>
              <div class="text-sm text-gray-500">{{ action.desc }}</div>
            </div>
            <el-icon class="text-gray-300 group-hover:text-blue-500 transition-colors"><ArrowRight /></el-icon>
          </div>
        </div>
      </div>
    </section>

    <!-- 关注商户动态 -->
    <section class="bg-white rounded-2xl shadow-sm overflow-hidden">
      <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
        <h3 class="font-semibold text-gray-800">
          ⭐ 关注商户动态
          <span class="text-sm font-normal text-gray-400 ml-2">
            {{ isBuyer ? '关注的供应商发布的供应信息' : '关注的采购商发布的采购需求' }}
          </span>
        </h3>
        <el-button text type="primary" @click="go(isBuyer ? '/supply-browse' : '/requirement-browse')">
          查看更多 <el-icon class="ml-1"><ArrowRight /></el-icon>
        </el-button>
      </div>

      <div v-loading="loading">
        <div
          v-for="item in followedUpdates"
          :key="item.id"
          class="px-6 py-4 border-b border-gray-50 last:border-b-0 hover:bg-gray-50 cursor-pointer transition-colors"
        >
          <div class="flex items-start gap-4">
            <div
              class="w-10 h-10 rounded-full flex items-center justify-center text-white text-sm font-medium flex-shrink-0"
              :class="item.type === 'supply' ? 'bg-orange-500' : 'bg-blue-500'"
            >
              {{ item.avatar }}
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 mb-1">
                <span class="font-medium text-gray-800">{{ item.company }}</span>
                <span class="text-xs text-gray-400 ml-auto">{{ item.time }}</span>
              </div>
              <div class="flex items-center gap-2 mb-2">
                <el-tag :type="item.type === 'supply' ? 'warning' : 'primary'" size="small">
                  {{ item.type === 'supply' ? '供应' : '采购' }}
                </el-tag>
                <span class="text-gray-700">{{ item.title }}</span>
              </div>
              <div class="flex items-center gap-4 text-sm text-gray-500">
                <span>📦 {{ item.category }}</span>
                <span>📍 {{ item.location }}</span>
                <span class="text-orange-600 font-medium">¥{{ item.price }}/吨</span>
                <span>{{ item.quantity }}</span>
              </div>
            </div>
          </div>
        </div>

        <div v-if="followedUpdates.length === 0 && !loading" class="py-12 text-center">
          <div class="text-5xl mb-4">⭐</div>
          <div class="text-gray-600 font-medium mb-2">您还没有关注任何商户</div>
          <div class="text-gray-400 text-sm mb-4">关注商户后，这里会显示他们发布的最新信息</div>
          <el-button type="primary" @click="go(isBuyer ? '/supply-browse' : '/requirement-browse')">去发现优质商户</el-button>
        </div>
      </div>
    </section>
  </div>
</template>


