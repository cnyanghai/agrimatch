<script setup lang="ts">
import { ref } from 'vue'
import { Card } from '../components/ui'
import BaseButton from '../components/ui/BaseButton.vue'
import FormInput from '../components/ui/FormInput.vue'
import { Search, Bell, User, Settings, TrendingUp, Users, CreditCard, AlertCircle } from 'lucide-vue-next'

const loading = ref(false)
const formData = ref({
  search: '',
  username: '',
  password: ''
})

function handleLogin() {
  loading.value = true
  setTimeout(() => {
    loading.value = false
  }, 2000)
}
</script>

<template>
  <div class="min-h-screen bg-bg-light dark:bg-bg-dark">
    <!-- 顶部导航 -->
    <header class="sticky top-0 z-50 bg-white dark:bg-neutral-900 border-b-2 border-neutral-200 dark:border-neutral-800 backdrop-blur-md">
      <div class="max-w-[1440px] mx-auto px-6 py-4 flex items-center justify-between">
        <!-- Logo -->
        <div class="flex items-center gap-3">
          <div class="w-10 h-10 bg-primary-600 rounded-xl flex items-center justify-center shadow-primary/25">
            <TrendingUp :size="20" class="text-white" />
          </div>
          <h1 class="text-2xl font-bold text-gray-900">AgriMatch</h1>
        </div>

        <!-- 搜索框 -->
        <div class="hidden md:flex flex-1 max-w-xl mx-8">
          <FormInput
            v-model="formData.search"
            placeholder="搜索产品、供应商..."
            type="text"
            class="mb-0"
          >
            <template #prefix>
              <Search :size="18" />
            </template>
          </FormInput>
        </div>

        <!-- 右侧操作 -->
        <div class="flex items-center gap-4">
          <button class="p-2 rounded-xl bg-neutral-100 dark:bg-neutral-800 hover:bg-neutral-200 dark:hover:bg-neutral-700 transition-all">
            <Bell :size="20" class="text-neutral-600 dark:text-neutral-400" />
          </button>
          <button class="p-2 rounded-xl bg-neutral-100 dark:bg-neutral-800 hover:bg-neutral-200 dark:hover:bg-neutral-700 transition-all">
            <User :size="20" class="text-neutral-600 dark:text-neutral-400" />
          </button>
          <BaseButton type="primary" size="sm">登录</BaseButton>
        </div>
      </div>
    </header>

    <!-- 主内容 -->
    <main class="max-w-[1440px] mx-auto px-6 py-8">
      <!-- 欢迎区域 -->
      <section class="mb-12">
        <h2 class="text-2xl font-bold text-gray-900 mb-4">
          欢迎来到
          <span class="text-primary-600">AgriMatch</span>
        </h2>
        <p class="text-lg text-neutral-600 dark:text-neutral-400 mb-8 max-w-2xl">
          连接饲料行业的供需双方，打造透明、高效的交易生态系统
        </p>
        <div class="flex gap-4">
          <BaseButton type="primary" size="lg">发布采购</BaseButton>
          <BaseButton type="secondary" size="lg">发布供应</BaseButton>
        </div>
      </section>

      <!-- 数据统计卡片 -->
      <section class="grid md:grid-cols-3 gap-6 mb-12">
        <Card shadow="lg" hover>
          <div class="flex items-center gap-4">
            <div class="w-12 h-12 bg-primary/10 rounded-xl flex items-center justify-center">
              <Users :size="24" class="text-primary-600" />
            </div>
            <div>
              <p class="text-xs font-bold text-neutral-500 uppercase tracking-wider">注册用户</p>
              <p class="text-3xl font-black text-neutral-900 dark:text-white">12,480+</p>
            </div>
          </div>
        </Card>

        <Card shadow="lg" hover>
          <div class="flex items-center gap-4">
            <div class="w-12 h-12 bg-blue-50 dark:bg-blue-900/20 rounded-xl flex items-center justify-center">
              <CreditCard :size="24" class="text-blue-600 dark:text-blue-400" />
            </div>
            <div>
              <p class="text-xs font-bold text-neutral-500 uppercase tracking-wider">本月交易</p>
              <p class="text-3xl font-black text-neutral-900 dark:text-white">158万 吨</p>
            </div>
          </div>
        </Card>

        <Card shadow="lg" hover>
          <div class="flex items-center gap-4">
            <div class="w-12 h-12 bg-warning/10 dark:bg-warning/10 rounded-xl flex items-center justify-center">
              <TrendingUp :size="24" class="text-warning" />
            </div>
            <div>
              <p class="text-xs font-bold text-neutral-500 uppercase tracking-wider">活跃供应商</p>
              <p class="text-3xl font-black text-neutral-900 dark:text-white">85+</p>
            </div>
          </div>
        </Card>
      </section>

      <!-- 功能卡片 -->
      <section class="mb-12">
        <h3 class="text-2xl font-bold text-gray-900 mb-6">核心功能</h3>
        <div class="grid md:grid-cols-2 lg:grid-cols-3 gap-6">
          <Card shadow="md" hover>
            <div class="flex flex-col gap-3">
              <div class="w-12 h-12 bg-primary/10 rounded-xl flex items-center justify-center">
                <Search :size="24" class="text-primary-600" />
              </div>
              <div>
                <h4 class="text-lg font-bold text-neutral-900 dark:text-white mb-2">高效寻源</h4>
                <p class="text-sm text-neutral-600 dark:text-neutral-400 leading-relaxed">
                  直接对接全球经认证的供应商，精简化采购流程
                </p>
              </div>
            </div>
          </Card>

          <Card shadow="md" hover>
            <div class="flex flex-col gap-3">
              <div class="w-12 h-12 bg-blue-50 dark:bg-blue-900/20 rounded-xl flex items-center justify-center">
                <Settings :size="24" class="text-blue-600 dark:text-blue-400" />
              </div>
              <div>
                <h4 class="text-lg font-bold text-neutral-900 dark:text-white mb-2">交易保障</h4>
                <p class="text-sm text-neutral-600 dark:text-neutral-400 leading-relaxed">
                  智能合约与第三方托管，降低交易风险
                </p>
              </div>
            </div>
          </Card>

          <Card shadow="md" hover>
            <div class="flex flex-col gap-3">
              <div class="w-12 h-12 bg-warning/10 dark:bg-warning/10 rounded-xl flex items-center justify-center">
                <AlertCircle :size="24" class="text-warning" />
              </div>
              <div>
                <h4 class="text-lg font-bold text-neutral-900 dark:text-white mb-2">专家行情</h4>
                <p class="text-sm text-neutral-600 dark:text-neutral-400 leading-relaxed">
                  实时市场数据与深度分析报告
                </p>
              </div>
            </div>
          </Card>
        </div>
      </section>

      <!-- 登录示例 -->
      <section class="max-w-md mx-auto">
        <Card shadow="xl" padding="lg">
          <h3 class="text-2xl font-bold text-gray-900 mb-6">登录账户</h3>

          <div class="space-y-5">
            <FormInput
              v-model="formData.username"
              label="用户名"
              placeholder="请输入用户名"
              required
            />

            <FormInput
              v-model="formData.password"
              label="密码"
              placeholder="请输入密码"
              type="password"
              required
            />

            <div class="flex items-center justify-between text-sm">
              <label class="flex items-center gap-2 cursor-pointer">
                <input type="checkbox" class="rounded border-neutral-200 text-primary focus:ring-primary">
                <span class="text-neutral-600 dark:text-neutral-400">记住我</span>
              </label>
              <a href="#" class="text-primary-600 hover:underline">忘记密码？</a>
            </div>

            <BaseButton
              type="primary"
              size="lg"
              :loading="loading"
              block
              @click="handleLogin"
            >
              立即登录
            </BaseButton>
          </div>
        </Card>
      </section>
    </main>
  </div>
</template>
