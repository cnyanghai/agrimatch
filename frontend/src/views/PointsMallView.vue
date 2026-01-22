<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Coins, ArrowLeft, Package, Gift, CreditCard, Sparkles } from 'lucide-vue-next'
import { getPointsMe, redeemPoints } from '../api/points'
import { BaseButton, Skeleton } from '../components/ui'

const router = useRouter()
const loading = ref(false)
const pointsBalance = ref(0)

// 商品数据（模拟数据，后续可接入真实API）
interface MallProduct {
  id: number
  name: string
  description: string
  image?: string
  points: number
  category: 'card' | 'gift' | 'service'
  stock: number
  tags?: string[]
}

const products = ref<MallProduct[]>([
  {
    id: 1,
    name: '京东购物卡 ¥50',
    description: '可在京东商城使用的购物卡',
    points: 50,
    category: 'card',
    stock: 999,
    tags: ['热门']
  },
  {
    id: 2,
    name: '京东购物卡 ¥100',
    description: '可在京东商城使用的购物卡',
    points: 100,
    category: 'card',
    stock: 999,
    tags: ['热门', '推荐']
  },
  {
    id: 3,
    name: '京东购物卡 ¥200',
    description: '可在京东商城使用的购物卡',
    points: 200,
    category: 'card',
    stock: 999,
    tags: ['推荐']
  },
  {
    id: 4,
    name: '京东购物卡 ¥500',
    description: '可在京东商城使用的购物卡',
    points: 500,
    category: 'card',
    stock: 999
  },
  {
    id: 5,
    name: '平台服务包（1个月）',
    description: '享受优先推荐、专属客服等权益',
    points: 300,
    category: 'service',
    stock: 100,
    tags: ['限时']
  },
  {
    id: 6,
    name: '定制礼品盒',
    description: '精美定制礼品，适合商务赠送',
    points: 800,
    category: 'gift',
    stock: 50
  }
])

const selectedCategory = ref<'all' | 'card' | 'gift' | 'service'>('all')

const filteredProducts = computed(() => {
  if (selectedCategory.value === 'all') return products.value
  return products.value.filter(p => p.category === selectedCategory.value)
})

function getCategoryIcon(category: string) {
  switch (category) {
    case 'card': return CreditCard
    case 'gift': return Gift
    case 'service': return Sparkles
    default: return Package
  }
}

function getCategoryColor(category: string) {
  switch (category) {
    case 'card': return 'bg-autumn-50 text-autumn-600'
    case 'gift': return 'bg-purple-50 text-purple-600'
    case 'service': return 'bg-brand-50 text-brand-600'
    default: return 'bg-gray-50 text-gray-600'
  }
}

async function loadPoints() {
  loading.value = true
  try {
    const res = await getPointsMe()
    if (res.code === 0 && res.data) {
      pointsBalance.value = res.data.pointsBalance
    }
  } catch (e) {
    console.error('加载积分失败', e)
  } finally {
    loading.value = false
  }
}

async function handleRedeem(product: MallProduct) {
  if (pointsBalance.value < product.points) {
    ElMessage.warning('积分余额不足')
    return
  }

  try {
    await ElMessageBox.confirm(
      `确认使用 ${product.points} 积分兑换「${product.name}」？`,
      '确认兑换',
      { confirmButtonText: '确认兑换', cancelButtonText: '取消', type: 'info' }
    )
  } catch {
    return
  }

  loading.value = true
  try {
    const res = await redeemPoints(product.points)
    if (res.code === 0) {
      ElMessage.success('兑换成功！')
      await loadPoints()
    } else {
      throw new Error(res.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '兑换失败')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPoints()
})
</script>

<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div class="flex items-center gap-4">
        <button
          class="w-10 h-10 rounded-xl bg-gray-100 hover:bg-gray-200 flex items-center justify-center transition-colors"
          @click="router.back()"
        >
          <ArrowLeft class="w-5 h-5 text-gray-600" />
        </button>
        <div>
          <h1 class="text-2xl font-bold text-gray-900">积分商城</h1>
          <p class="text-sm text-gray-500 mt-1">使用积分兑换精美商品</p>
        </div>
      </div>
      <div class="flex items-center gap-3">
        <div class="px-4 py-2 bg-gradient-to-r from-brand-500 to-brand-600 rounded-xl text-white">
          <div class="flex items-center gap-2">
            <Coins class="w-4 h-4" />
            <span class="text-sm font-bold">{{ pointsBalance.toLocaleString() }} 积分</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 分类筛选 -->
    <div class="flex items-center gap-2 overflow-x-auto pb-2 scrollbar-hide">
      <button
        v-for="cat in [
          { key: 'all', label: '全部商品' },
          { key: 'card', label: '购物卡' },
          { key: 'gift', label: '礼品' },
          { key: 'service', label: '服务' }
        ]"
        :key="cat.key"
        :class="[
          'px-4 py-2 rounded-xl font-bold text-sm whitespace-nowrap transition-all',
          selectedCategory === cat.key
            ? 'bg-brand-600 text-white shadow-md'
            : 'bg-white border border-gray-200 text-gray-600 hover:border-brand-300'
        ]"
        @click="selectedCategory = cat.key as any"
      >
        {{ cat.label }}
      </button>
    </div>

    <!-- 商品网格 -->
    <div v-if="loading && filteredProducts.length === 0" class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <div v-for="i in 6" :key="i" class="bg-white rounded-2xl border border-gray-100 p-6">
        <Skeleton type="card" />
      </div>
    </div>

    <div v-else-if="filteredProducts.length === 0" class="bg-white rounded-2xl border border-gray-100 p-12 text-center">
      <Package class="w-16 h-16 text-gray-300 mx-auto mb-4" />
      <h3 class="text-lg font-bold text-gray-900 mb-2">暂无商品</h3>
      <p class="text-sm text-gray-500">该分类下暂无可用商品</p>
    </div>

    <div v-else class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
      <div
        v-for="product in filteredProducts"
        :key="product.id"
            class="group bg-white rounded-2xl border border-gray-100 overflow-hidden hover:shadow-lg hover:border-brand-200 transition-all"
      >
        <!-- 商品图片区域 -->
        <div class="relative h-48 bg-gradient-to-br from-gray-50 to-gray-100 flex items-center justify-center">
          <component
            :is="getCategoryIcon(product.category)"
            :class="['w-16 h-16', getCategoryColor(product.category).split(' ')[1]]"
          />
          
          <!-- 标签 -->
          <div v-if="product.tags && product.tags.length > 0" class="absolute top-3 left-3 flex gap-2">
            <span
              v-for="tag in product.tags"
              :key="tag"
              :class="[
                'px-2 py-0.5 rounded-full text-[10px] font-bold uppercase',
                tag === '热门' ? 'bg-red-500 text-white' :
                tag === '推荐' ? 'bg-brand-500 text-white' :
                tag === '限时' ? 'bg-orange-500 text-white' :
                'bg-gray-500 text-white'
              ]"
            >
              {{ tag }}
            </span>
          </div>
        </div>

        <!-- 商品信息 -->
        <div class="p-6">
          <h3 class="font-bold text-gray-900 mb-2 group-hover:text-brand-600 transition-colors">
            {{ product.name }}
          </h3>
          <p class="text-sm text-gray-500 mb-4 line-clamp-2">
            {{ product.description }}
          </p>

          <!-- 价格与库存 -->
          <div class="flex items-center justify-between mb-4">
            <div class="flex items-center gap-2">
              <Coins class="w-4 h-4 text-amber-500" />
              <span class="text-lg font-black text-gray-900">{{ product.points }}</span>
              <span class="text-sm text-gray-500">积分</span>
            </div>
            <span class="text-xs text-gray-400">库存 {{ product.stock }}</span>
          </div>

          <!-- 兑换按钮 -->
          <BaseButton
            type="primary"
            block
            :disabled="pointsBalance < product.points || product.stock === 0"
            :loading="loading"
            @click="handleRedeem(product)"
          >
            <component :is="getCategoryIcon(product.category)" class="w-4 h-4" />
            {{ pointsBalance < product.points ? '积分不足' : product.stock === 0 ? '已售罄' : '立即兑换' }}
          </BaseButton>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.scrollbar-hide {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
.scrollbar-hide::-webkit-scrollbar {
  display: none;
}
</style>

