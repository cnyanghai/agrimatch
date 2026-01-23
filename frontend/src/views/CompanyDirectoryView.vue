<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCompany } from '../composables/useCompany'
import { useCompanyStore } from '../stores/company'
import { debounce } from '../utils/debounce'
import PublicFooter from '../components/PublicFooter.vue'
import CompanyCard from '../components/company/CompanyCard.vue'
import { Search, ShoppingBag, Truck, ChevronRight } from 'lucide-vue-next'
import CompanySkeleton from '../components/company/CompanySkeleton.vue'
import EmptyState from '../components/common/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const companyStore = useCompanyStore()

const type = ref<'supplier' | 'buyer'>((route.query.type as any) || 'supplier')
const letter = ref<string>((route.query.letter as string) || '')
const page = ref(Number(route.query.page) || 1)
const size = ref(24)
const searchKeyword = ref<string>((route.query.keyword as string) || '')

const { loading, companies, total, loadDirectory, searchCompaniesByKeyword, searchResults } = useCompany()

const alphabet = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0-9']

const handleSearch = debounce((keyword: string) => {
  searchKeyword.value = keyword
  if (keyword.trim()) {
    searchCompaniesByKeyword(keyword)
  } else {
    searchResults.value = []
  }
  updateUrl()
}, 500)

async function loadDirectoryData() {
  const cacheKey = `${type.value}-${letter.value || 'all'}-${page.value}-${size.value}`
  const cached = companyStore.activeDirectory(type.value, letter.value || 'all')
  
  if (cached && cached.list.length > 0) {
    companies.value = cached.list
    total.value = cached.total
  } else {
    await loadDirectory(type.value, letter.value, page.value, size.value)
    companyStore.directories.set(cacheKey, { list: companies.value, total: total.value })
  }
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
      keyword: searchKeyword.value || undefined,
      page: page.value > 1 ? page.value : undefined
    }
  })
}

function goProfile(id: number) {
  router.push(`/companies/${id}`)
}

onMounted(loadDirectoryData)

watch(() => route.query, () => {
  type.value = (route.query.type as any) || 'supplier'
  letter.value = (route.query.letter as string) || ''
  searchKeyword.value = (route.query.keyword as string) || ''
  page.value = Number(route.query.page) || 1
  loadDirectoryData()
}, { deep: true })
</script>

<template>
  <div class="bg-gray-50 min-h-screen flex flex-col">
    <main class="flex-1 max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 w-full">
      <!-- Header -->
      <div class="mb-12">
        <h1 class="text-2xl font-bold text-gray-900 mb-4 flex items-center gap-3">
          <Truck v-if="type === 'supplier'" class="text-brand-600" :size="32" aria-hidden="true" />
          <ShoppingBag v-else class="text-blue-600" :size="32" aria-hidden="true" />
          {{ type === 'supplier' ? '制造商名录' : '采购商名录' }}
        </h1>
        <p class="text-gray-500">查找并对比优质{{ type === 'supplier' ? '饲料原料供应商' : '行业采购商' }}</p>
      </div>

      <!-- Search Bar -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 mb-8">
        <div class="relative">
          <Search :size="20" class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400" aria-hidden="true" />
          <input
            type="text"
            v-model="searchKeyword"
            @input="handleSearch(($event.target as HTMLInputElement).value)"
            placeholder="搜索公司名称..."
            class="w-full pl-12 pr-4 py-3 rounded-lg border border-gray-200 focus:border-brand-500 focus:ring-2 focus:ring-brand-200 outline-none transition-all text-sm"
            aria-label="搜索公司"
          />
        </div>
      </div>

      <!-- Search Results Dropdown -->
      <div v-if="searchResults.length > 0 && searchKeyword" class="bg-white rounded-xl shadow-lg border border-gray-200 mb-8 overflow-hidden">
        <div class="p-4 bg-gray-50 border-b border-gray-100">
          <div class="text-xs font-bold text-gray-500 uppercase tracking-widest">搜索结果</div>
        </div>
        <div class="max-h-96 overflow-y-auto">
          <div
            v-for="result in searchResults"
            :key="result.id"
            @click="goProfile(result.id)"
            class="flex items-center justify-between p-4 hover:bg-brand-50 cursor-pointer transition-colors border-b border-gray-50 last:border-0"
          >
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 bg-brand-100 rounded-lg flex items-center justify-center font-bold text-brand-600 text-sm">
                {{ result.companyName[0] }}
              </div>
              <div>
                <div class="font-bold text-gray-900">{{ result.companyName }}</div>
                <div class="text-xs text-gray-400">{{ result.address || '暂无地址' }}</div>
              </div>
            </div>
            <ChevronRight :size="16" class="text-gray-300" aria-hidden="true" />
          </div>
        </div>
      </div>

      <!-- Type Switch & Alphabet Index -->
      <div class="bg-white rounded-xl shadow-sm border border-gray-200 p-8 mb-8">
        <div class="flex flex-wrap gap-2">
          <span class="text-xs font-bold text-gray-400 uppercase tracking-widest mr-4 self-center">跳转到</span>
          <button
            class="w-10 h-10 rounded-xl flex items-center justify-center font-bold text-xs transition-all"
            :class="!letter ? 'bg-brand-600 text-white shadow-md shadow-brand-200' : 'bg-gray-50 text-gray-500 hover:bg-gray-100'"
            @click="setLetter('')"
          >
            全部
          </button>
          <button
            v-for="l in alphabet"
            :key="l"
            class="w-10 h-10 rounded-xl flex items-center justify-center font-bold text-xs transition-all"
            :class="letter === l ? (type === 'supplier' ? 'bg-brand-600 text-white shadow-md shadow-brand-200' : 'bg-blue-600 text-white shadow-md shadow-blue-200') : 'bg-gray-50 text-gray-500 hover:bg-gray-100'"
            @click="setLetter(l)"
          >
            {{ l }}
          </button>
        </div>
      </div>

      <!-- Company Grid -->
      <CompanySkeleton v-if="loading" :count="12" type="card" />
      <div v-else-if="companies.length === 0" class="bg-white rounded-xl border border-dashed border-gray-200">
        <EmptyState
          :icon="Search"
          title="未找到相关公司"
          description="尝试切换字母索引或浏览全部"
        />
      </div>
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <CompanyCard
          v-for="c in companies"
          :key="c.id"
          :id="c.id"
          :company-name="c.companyName"
          :province="c.province"
          :city="c.city"
          :category-names="c.categoryNames"
          :count="c.count"
          :type="type"
          @click="goProfile(c.id)"
        />
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

