<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Search, Tag as TagIcon, Building2, User, Clock, LayoutGrid, List, ChevronRight, X, Filter } from 'lucide-vue-next'
import { searchUnified, type UnifiedSearchResult } from '../api/search'
import { getHotTags, type Tag } from '../api/tag'
import PublicFooter from '../components/PublicFooter.vue'

const route = useRoute()
const router = useRouter()

const keyword = ref((route.query.keyword as string) || '')
const domain = ref((route.query.domain as string) || '')
const entityType = ref((route.query.type as string) || '')
const tagFilters = ref<Record<string, any>>({})

const loading = ref(false)
const results = ref<UnifiedSearchResult[]>([])
const total = ref(0)
const page = ref(1)
const size = ref(10)

const hotTags = ref<Tag[]>([])

async function fetchResults() {
  loading.value = true
  try {
    const res = await searchUnified({
      keyword: keyword.value,
      domain: domain.value,
      entityType: entityType.value,
      tagFiltersJson: Object.keys(tagFilters.value).length > 0 ? JSON.stringify(tagFilters.value) : undefined,
      page: page.value,
      size: size.value
    })
    if (res.code === 0) {
      results.value = res.data?.list ?? []
      total.value = res.data?.total ?? 0
    }
  } catch (e) {
    console.error('Search failed', e)
  } finally {
    loading.value = false
  }
}

async function loadHotTags() {
  try {
    const res = await getHotTags()
    if (res.code === 0) hotTags.value = res.data ?? []
  } catch {}
}

function handleSearch() {
  page.value = 1
  updateUrl()
  fetchResults()
}

function updateUrl() {
  router.replace({
    query: {
      ...route.query,
      keyword: keyword.value || undefined,
      domain: domain.value || undefined,
      type: entityType.value || undefined,
      page: page.value > 1 ? page.value : undefined
    }
  })
}

function toggleTag(tag: NhtTag) {
  if (tagFilters.value[tag.tagKey]) {
    delete tagFilters.value[tag.tagKey]
  } else {
    // For hot tags, we don't know the value, so we just filter by existence or a generic search
    // In a real app, clicking a tag might open a value picker or use a default
    tagFilters.value[tag.tagKey] = true 
  }
  handleSearch()
}

function getEntityTypeName(type: string) {
  switch (type) {
    case 'supply': return '供应'
    case 'requirement': return '需求'
    case 'post': return '帖子'
    default: return '未知'
  }
}

function getEntityTypeClass(type: string) {
  switch (type) {
    case 'supply': return 'bg-brand-50 text-brand-700 border-brand-100'
    case 'requirement': return 'bg-autumn-50 text-autumn-700 border-autumn-100'
    case 'post': return 'bg-purple-50 text-purple-700 border-purple-100'
    default: return 'bg-gray-50 text-gray-700 border-gray-100'
  }
}

function formatTime(timeStr: string) {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleDateString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit' })
}

onMounted(() => {
  loadHotTags()
  fetchResults()
})

watch(() => route.query, (newQuery) => {
  keyword.value = (newQuery.keyword as string) || ''
  domain.value = (newQuery.domain as string) || ''
  entityType.value = (newQuery.type as string) || ''
  fetchResults()
})
</script>

<template>
  <div class="min-h-screen bg-gray-50 flex flex-col">
    <!-- Search Header -->
    <div class="bg-white border-b border-gray-200 sticky top-0 z-30">
      <div class="max-w-7xl mx-auto px-4 py-4">
        <div class="flex flex-col md:flex-row items-center gap-4">
          <div class="relative flex-1 w-full">
            <Search class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 w-5 h-5" />
            <input
              v-model="keyword"
              type="text"
              placeholder="搜索全站资源、标签、商户..."
              class="w-full pl-12 pr-4 py-3 bg-gray-100 border-transparent rounded-2xl focus:bg-white focus:ring-2 focus:ring-brand-500/20 focus:border-brand-500 transition-all outline-none"
              @keyup.enter="handleSearch"
            />
          </div>
          <div class="flex items-center gap-2 w-full md:w-auto">
            <select v-model="entityType" class="px-4 py-3 bg-white border border-gray-200 rounded-xl text-sm font-medium focus:outline-none focus:ring-2 focus:ring-brand-500/20" @change="handleSearch">
              <option value="">全部类型</option>
              <option value="supply">供应信息</option>
              <option value="requirement">采购需求</option>
              <option value="post">社区帖子</option>
            </select>
            <button class="bg-brand-600 hover:bg-brand-700 text-white px-8 py-3 rounded-xl font-bold transition-all active:scale-95" @click="handleSearch">
              搜索
            </button>
          </div>
        </div>
      </div>
    </div>

    <div class="flex-1 max-w-7xl mx-auto w-full px-4 py-8 flex flex-col lg:flex-row gap-8">
      <!-- Sidebar Filters -->
      <aside class="w-full lg:w-64 shrink-0 space-y-8">
        <!-- Domains -->
        <div>
          <h3 class="text-sm font-bold text-gray-900 uppercase tracking-wider mb-4 flex items-center gap-2">
            <Filter class="w-4 h-4 text-brand-600" />
            产业板块
          </h3>
          <div class="space-y-1">
            <button
              v-for="d in [
                { key: '', name: '全部领域' },
                { key: 'biological', name: '生物种苗' },
                { key: 'processing', name: '农牧加工' },
                { key: 'material', name: '原料饲料' },
                { key: 'equipment', name: '装备物流' }
              ]"
              :key="d.key"
              class="w-full text-left px-4 py-2.5 rounded-xl text-sm font-medium transition-all"
              :class="domain === d.key ? 'bg-brand-50 text-brand-700' : 'text-gray-600 hover:bg-gray-100'"
              @click="domain = d.key; handleSearch()"
            >
              {{ d.name }}
            </button>
          </div>
        </div>

        <!-- Hot Tags -->
        <div v-if="hotTags.length > 0">
          <h3 class="text-sm font-bold text-gray-900 uppercase tracking-wider mb-4 flex items-center gap-2">
            <TagIcon class="w-4 h-4 text-brand-600" />
            热门标签
          </h3>
          <div class="flex flex-wrap gap-2">
            <button
              v-for="tag in hotTags"
              :key="tag.id"
              class="px-3 py-1.5 rounded-lg text-xs font-medium border transition-all"
              :class="tagFilters[tag.tagKey] ? 'bg-brand-500 text-white border-brand-500' : 'bg-white text-gray-600 border-gray-200 hover:border-brand-300'"
              @click="toggleTag(tag)"
            >
              {{ tag.tagName }}
            </button>
          </div>
        </div>
      </aside>

      <!-- Main Results -->
      <main class="flex-1 min-w-0">
        <!-- Status Info -->
        <div class="flex items-center justify-between mb-6">
          <div class="text-sm text-gray-500">
            找到约 <span class="font-bold text-gray-900">{{ total }}</span> 个结果
          </div>
          <div class="flex items-center gap-2">
            <button class="p-2 text-gray-400 hover:text-brand-600 rounded-lg hover:bg-white transition-all">
              <LayoutGrid class="w-5 h-5" />
            </button>
            <button class="p-2 text-brand-600 bg-white shadow-sm rounded-lg border border-gray-100 transition-all">
              <List class="w-5 h-5" />
            </button>
          </div>
        </div>

        <!-- Results List -->
        <div v-if="loading" class="space-y-4">
          <div v-for="i in 3" :key="i" class="bg-white rounded-2xl p-6 border border-gray-100 animate-pulse">
            <div class="flex gap-6">
              <div class="w-32 h-24 bg-gray-50 rounded-xl"></div>
              <div class="flex-1 space-y-4">
                <div class="h-6 bg-gray-50 rounded w-3/4"></div>
                <div class="h-4 bg-gray-50 rounded w-1/2"></div>
                <div class="h-4 bg-gray-50 rounded w-1/4"></div>
              </div>
            </div>
          </div>
        </div>

        <div v-else-if="results.length === 0" class="bg-white rounded-2xl py-20 text-center border border-dashed border-gray-200">
          <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-6">
            <Search class="w-10 h-10 text-gray-300" />
          </div>
          <h3 class="text-xl font-bold text-gray-900 mb-2">未找到匹配结果</h3>
          <p class="text-gray-500 max-w-xs mx-auto">请尝试调整关键词或筛选条件，或通过更广泛的领域进行搜索。</p>
          <button class="mt-8 text-brand-600 font-bold hover:underline" @click="keyword = ''; domain = ''; entityType = ''; handleSearch()">
            重置搜索
          </button>
        </div>

        <div v-else class="space-y-4">
          <div
            v-for="item in results"
            :key="`${item.entityType}-${item.entityId}`"
            class="group bg-white rounded-2xl p-6 border border-gray-100 hover:shadow-xl hover:border-brand-500/30 transition-all cursor-pointer"
            @click="router.push(item.entityType === 'post' ? `/talks/${item.entityId}` : (item.entityType === 'supply' ? `/hall/supply?id=${item.entityId}` : `/hall/need?id=${item.entityId}`))"
          >
            <div class="flex flex-col md:flex-row gap-6">
              <!-- Content -->
              <div class="flex-1 min-w-0">
                <div class="flex items-center gap-3 mb-3">
                  <span class="px-2.5 py-0.5 rounded-lg text-[10px] font-bold uppercase tracking-wider border" :class="getEntityTypeClass(item.entityType)">
                    {{ getEntityTypeName(item.entityType) }}
                  </span>
                  <div class="flex items-center gap-1 text-[10px] text-gray-400">
                    <Clock class="w-3 h-3" />
                    {{ formatTime(item.createTime) }}
                  </div>
                </div>
                
                <h3 class="text-lg font-bold text-gray-900 mb-2 group-hover:text-brand-600 transition-colors truncate">
                  {{ item.title }}
                </h3>
                
                <p class="text-sm text-gray-500 line-clamp-2 mb-4 leading-relaxed">
                  {{ item.content || '暂无详细描述...' }}
                </p>

                <div class="flex flex-wrap items-center gap-y-3 gap-x-6">
                  <div class="flex items-center gap-2 text-xs font-medium text-gray-600">
                    <Building2 class="w-4 h-4 text-gray-400" />
                    {{ item.companyName || '个人用户' }}
                  </div>
                  <div class="flex items-center gap-2 text-xs font-medium text-gray-600">
                    <User class="w-4 h-4 text-gray-400" />
                    {{ item.userName }}
                  </div>
                </div>
              </div>

              <!-- Action -->
              <div class="flex items-center shrink-0">
                <div class="w-10 h-10 rounded-full bg-gray-50 flex items-center justify-center text-gray-400 group-hover:bg-brand-50 group-hover:text-brand-600 transition-all">
                  <ChevronRight class="w-5 h-5" />
                </div>
              </div>
            </div>
          </div>

          <!-- Pagination -->
          <div class="mt-12 flex justify-center">
            <el-pagination
              v-model:current-page="page"
              v-model:page-size="size"
              :total="total"
              layout="prev, pager, next"
              background
              @current-change="fetchResults"
            />
          </div>
        </div>
      </main>
    </div>

    <PublicFooter />
  </div>
</template>

<style scoped>
.line-clamp-2 {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>

