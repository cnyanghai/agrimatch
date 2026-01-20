<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getCompanyDirectory, type CompanyCardResponse } from '../api/company'
import PublicTopNav from '../components/PublicTopNav.vue'
import PublicFooter from '../components/PublicFooter.vue'
import { Search, MapPin, Package, ShoppingBag, Truck, ChevronRight } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

const type = ref<'supplier' | 'buyer'>((route.query.type as any) || 'supplier')
const letter = ref<string>((route.query.letter as string) || '')
const page = ref(Number(route.query.page) || 1)
const size = ref(24)

const companies = ref<CompanyCardResponse[]>([])
const total = ref(0)
const loading = ref(false)

const alphabet = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0-9']

async function loadDirectory() {
  loading.value = true
  try {
    const res = await getCompanyDirectory(type.value, letter.value, page.value, size.value)
    if (res.code === 0 && res.data) {
      companies.value = res.data.list
      total.value = res.data.total
    }
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

function setType(t: 'supplier' | 'buyer') {
  type.value = t
  page.value = 1
  letter.value = ''
  updateUrl()
}

function setLetter(l: string) {
  letter.value = letter.value === l ? '' : l
  page.value = 1
  updateUrl()
}

function handlePageChange(p: number) {
  page.value = p
  updateUrl()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

function updateUrl() {
  router.push({
    path: route.path,
    query: {
      type: type.value,
      letter: letter.value || undefined,
      page: page.value > 1 ? page.value : undefined
    }
  })
}

function goProfile(id: number) {
  router.push(`/companies/${id}`)
}

onMounted(loadDirectory)

watch(() => route.query, () => {
  type.value = (route.query.type as any) || 'supplier'
  letter.value = (route.query.letter as string) || ''
  page.value = Number(route.query.page) || 1
  loadDirectory()
}, { deep: true })
</script>

<template>
  <div class="bg-gray-50 min-h-screen flex flex-col">
    <PublicTopNav />

    <main class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 w-full">
      <!-- Header -->
      <div class="mb-12">
        <h1 class="text-3xl font-extrabold text-gray-900 mb-4 flex items-center gap-3">
          <Truck v-if="type === 'supplier'" class="text-emerald-600" :size="32" />
          <ShoppingBag v-else class="text-blue-600" :size="32" />
          {{ type === 'supplier' ? '制造商名录' : '采购商名录' }}
        </h1>
        <p class="text-gray-500">查找并对比优质{{ type === 'supplier' ? '饲料原料供应商' : '行业采购商' }}</p>
      </div>

      <!-- Type Switch & Alphabet Index -->
      <div class="bg-white rounded-3xl shadow-sm border border-gray-100 p-8 mb-8">
        <div class="flex flex-col md:flex-row gap-8 items-start md:items-center justify-between mb-8 border-b border-gray-50 pb-8">
          <div class="flex p-1 bg-gray-100 rounded-2xl">
            <button
              class="px-8 py-2.5 rounded-xl font-bold text-sm transition-all"
              :class="type === 'supplier' ? 'bg-white text-emerald-700 shadow-sm' : 'text-gray-500 hover:text-gray-700'"
              @click="setType('supplier')"
            >
              供应商
            </button>
            <button
              class="px-8 py-2.5 rounded-xl font-bold text-sm transition-all"
              :class="type === 'buyer' ? 'bg-white text-blue-700 shadow-sm' : 'text-gray-500 hover:text-gray-700'"
              @click="setType('buyer')"
            >
              采购商
            </button>
          </div>

          <div class="flex items-center gap-2 text-sm text-gray-400">
            <span>共找到 <b class="text-gray-900">{{ total }}</b> 家{{ type === 'supplier' ? '供应商' : '采购商' }}</span>
          </div>
        </div>

        <div class="flex flex-wrap gap-2">
          <span class="text-xs font-bold text-gray-400 uppercase tracking-widest mr-4 self-center">跳转到</span>
          <button
            class="w-10 h-10 rounded-xl flex items-center justify-center font-bold text-xs transition-all"
            :class="!letter ? 'bg-emerald-600 text-white shadow-lg shadow-emerald-200' : 'bg-gray-50 text-gray-500 hover:bg-gray-100'"
            @click="setLetter('')"
          >
            全部
          </button>
          <button
            v-for="l in alphabet"
            :key="l"
            class="w-10 h-10 rounded-xl flex items-center justify-center font-bold text-xs transition-all"
            :class="letter === l ? (type === 'supplier' ? 'bg-emerald-600 text-white shadow-lg shadow-emerald-200' : 'bg-blue-600 text-white shadow-lg shadow-blue-200') : 'bg-gray-50 text-gray-500 hover:bg-gray-100'"
            @click="setLetter(l)"
          >
            {{ l }}
          </button>
        </div>
      </div>

      <!-- Company Grid -->
      <div v-if="loading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div v-for="i in 12" :key="i" class="h-48 bg-white rounded-3xl border border-gray-100 animate-pulse"></div>
      </div>
      <div v-else-if="companies.length === 0" class="py-32 text-center bg-white rounded-3xl border border-dashed border-gray-200">
        <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-6">
          <Search :size="32" class="text-gray-300" />
        </div>
        <h3 class="text-lg font-bold text-gray-900 mb-2">未找到相关公司</h3>
        <p class="text-gray-400">尝试切换字母索引或浏览全部</p>
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="c in companies"
          :key="c.id"
          class="group bg-white p-6 rounded-3xl border border-gray-100 hover:shadow-xl hover:border-emerald-100 transition-all cursor-pointer flex flex-col"
          @click="goProfile(c.id)"
        >
          <div class="flex items-center gap-4 mb-6">
            <div
              class="w-14 h-14 rounded-2xl flex items-center justify-center font-bold text-white text-xl shadow-sm transition-transform group-hover:scale-110"
              :class="type === 'supplier' ? 'bg-gradient-to-br from-emerald-500 to-teal-600' : 'bg-gradient-to-br from-blue-500 to-indigo-600'"
            >
              {{ c.companyName[0] }}
            </div>
            <div class="min-w-0">
              <h3 class="font-bold text-gray-900 truncate group-hover:text-emerald-600 transition-colors">
                {{ c.companyName }}
              </h3>
              <div class="flex items-center gap-1.5 text-xs text-gray-400 mt-1">
                <MapPin :size="12" />
                {{ c.province }} · {{ c.city }}
              </div>
            </div>
          </div>

          <div class="space-y-3 mb-6 flex-1">
            <div class="flex items-start gap-3">
              <Package :size="14" class="text-gray-300 mt-0.5 shrink-0" />
              <div class="text-xs text-gray-500 line-clamp-2">
                {{ c.categoryNames?.join(' / ') || '暂无分类' }}
              </div>
            </div>
          </div>

          <div class="flex items-center justify-between pt-4 border-t border-gray-50">
            <div class="text-xs">
              <span class="text-gray-400">{{ type === 'supplier' ? '累计供应' : '累计需求' }}</span>
              <b class="ml-2" :class="type === 'supplier' ? 'text-emerald-600' : 'text-blue-600'">{{ c.count }}</b>
            </div>
            <div class="flex items-center gap-1 text-xs font-bold" :class="type === 'supplier' ? 'text-emerald-600' : 'text-blue-600'">
              进入主页
              <ChevronRight :size="14" />
            </div>
          </div>
        </div>
      </div>

      <!-- Pagination -->
      <div v-if="total > size" class="mt-12 flex justify-center">
        <el-pagination
          v-model:current-page="page"
          :page-size="size"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="handlePageChange"
        />
      </div>
    </main>

    <PublicFooter />
  </div>
</template>

