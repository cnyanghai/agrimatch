<script setup lang="ts">
import { useRouter } from 'vue-router'
import { Package, ShoppingCart, ArrowRight, Check, TrendingUp, Users, MapPin, ShieldCheck } from 'lucide-vue-next'
import { BaseButton } from '../components/ui'

const router = useRouter()

function goToPublishSupply() {
  router.push('/supply')
}

function goToPublishRequirement() {
  router.push('/requirements')
}

const supplyFeatures = [
  { icon: TrendingUp, text: '快速展示您的产品' },
  { icon: Users, text: '精准匹配采购商' },
  { icon: ShieldCheck, text: '平台保障交易安全' }
]

const requirementFeatures = [
  { icon: ShoppingCart, text: '发布您的采购需求' },
  { icon: MapPin, text: '获取供应商报价' },
  { icon: ShieldCheck, text: '平台保障交易安全' }
]
</script>

<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div>
      <h1 class="text-2xl font-bold text-gray-900">发布信息</h1>
      <p class="text-sm text-gray-500 mt-1">选择发布类型，快速发布您的供应或采购信息</p>
    </div>

    <!-- 双选卡片 -->
    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- 发布供应 -->
      <div
        class="group relative bg-white rounded-2xl border-2 border-gray-100 p-8 hover:border-brand-200 hover:shadow-xl transition-all cursor-pointer overflow-hidden"
        @click="goToPublishSupply"
      >
        <!-- 背景装饰 -->
        <div class="absolute top-0 right-0 w-32 h-32 bg-brand-50 rounded-full -mr-16 -mt-16 opacity-50 group-hover:opacity-70 transition-opacity"></div>
        
        <div class="relative">
          <!-- 图标与标题 -->
          <div class="flex items-center gap-4 mb-6">
            <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-brand-500 to-brand-600 flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform">
              <Package class="w-8 h-8 text-white" />
            </div>
            <div>
              <h2 class="text-xl font-bold text-gray-900 group-hover:text-brand-600 transition-colors">
                发布供应
              </h2>
              <p class="text-sm text-gray-500">我有货，寻找买家</p>
            </div>
          </div>

          <!-- 功能特点 -->
          <ul class="space-y-3 mb-6">
            <li
              v-for="(feature, index) in supplyFeatures"
              :key="index"
              class="flex items-center gap-3 text-sm text-gray-600"
            >
              <div class="w-6 h-6 rounded-lg bg-brand-50 flex items-center justify-center shrink-0">
                <component :is="feature.icon" class="w-4 h-4 text-brand-600" />
              </div>
              <span>{{ feature.text }}</span>
            </li>
          </ul>

          <!-- 操作按钮 -->
          <BaseButton
            type="primary"
            block
          >
            <span>立即发布供应</span>
            <ArrowRight class="w-4 h-4 ml-2" />
          </BaseButton>
        </div>
      </div>

      <!-- 发布采购 -->
      <div
        class="group relative bg-white rounded-2xl border-2 border-gray-100 p-8 hover:border-autumn-200 hover:shadow-xl transition-all cursor-pointer overflow-hidden"
        @click="goToPublishRequirement"
      >
        <!-- 背景装饰 -->
        <div class="absolute top-0 right-0 w-32 h-32 bg-autumn-50 rounded-full -mr-16 -mt-16 opacity-50 group-hover:opacity-70 transition-opacity"></div>
        
        <div class="relative">
          <!-- 图标与标题 -->
          <div class="flex items-center gap-4 mb-6">
            <div class="w-16 h-16 rounded-2xl bg-gradient-to-br from-autumn-500 to-autumn-600 flex items-center justify-center shadow-lg group-hover:scale-110 transition-transform">
              <ShoppingCart class="w-8 h-8 text-white" />
            </div>
            <div>
              <h2 class="text-xl font-bold text-gray-900 group-hover:text-autumn-600 transition-colors">
                发布采购
              </h2>
              <p class="text-sm text-gray-500">我要货，寻找卖家</p>
            </div>
          </div>

          <!-- 功能特点 -->
          <ul class="space-y-3 mb-6">
            <li
              v-for="(feature, index) in requirementFeatures"
              :key="index"
              class="flex items-center gap-3 text-sm text-gray-600"
            >
              <div class="w-6 h-6 rounded-lg bg-autumn-50 flex items-center justify-center shrink-0">
                <component :is="feature.icon" class="w-4 h-4 text-autumn-600" />
              </div>
              <span>{{ feature.text }}</span>
            </li>
          </ul>

          <!-- 操作按钮 -->
          <BaseButton
            type="primary"
            block
            class="!bg-autumn-600 hover:!bg-autumn-700"
          >
            <span>立即发布采购</span>
            <ArrowRight class="w-4 h-4 ml-2" />
          </BaseButton>
        </div>
      </div>
    </div>

    <!-- 提示信息 -->
    <div class="bg-autumn-50 border border-autumn-100 rounded-xl p-4">
      <div class="flex items-start gap-3">
        <div class="w-5 h-5 rounded-full bg-autumn-500 flex items-center justify-center shrink-0 mt-0.5">
          <Check class="w-3 h-3 text-white" />
        </div>
        <div class="text-sm text-autumn-700">
          <p class="font-bold mb-1">温馨提示</p>
          <p>发布信息前，请确保已完善您的公司资料，以便更好地展示您的企业形象和提升交易成功率。</p>
        </div>
      </div>
    </div>
  </div>
</template>

