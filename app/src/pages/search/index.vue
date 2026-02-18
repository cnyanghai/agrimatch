<script setup lang="ts">
import { ref, computed, nextTick } from 'vue'
import { onLoad, onReachBottom } from '@dcloudio/uni-app'
import { WARM_400 } from '../../constants/colors'
import { searchUnified, type UnifiedSearchResult, type SearchPageResult } from '../../api/search'
import { formatRelativeTime } from '../../utils/format'

/* ===== Tab 配置 ===== */
interface TabItem {
  label: string
  value: string
}

const tabs: TabItem[] = [
  { label: '全部', value: '' },
  { label: '供应', value: 'supply' },
  { label: '采购', value: 'requirement' },
  { label: '话题', value: 'post' },
]

/* ===== 状态 ===== */
const keyword = ref('')
const activeTab = ref('')
const searchHistory = ref<string[]>([])
const results = ref<UnifiedSearchResult[]>([])
const currentPage = ref(1)
const total = ref(0)
const loading = ref(false)
const searched = ref(false)

const PAGE_SIZE = 20
const HISTORY_KEY = 'searchHistory'
const MAX_HISTORY = 15

/* ===== 计算属性 ===== */
const hasMore = computed(() => results.value.length < total.value)

const loadStatus = computed<'loading' | 'more' | 'noMore'>(() => {
  if (loading.value) return 'loading'
  if (!searched.value) return 'noMore'
  if (hasMore.value) return 'more'
  return 'noMore'
})

const showHistory = computed(() => !searched.value && searchHistory.value.length > 0)
const showResults = computed(() => searched.value && !loading.value && results.value.length > 0)
const showEmpty = computed(() => searched.value && !loading.value && results.value.length === 0 && currentPage.value === 1)
const showSkeleton = computed(() => loading.value && results.value.length === 0)

/* ===== 生命周期 ===== */
onLoad((query) => {
  loadHistory()
  // 支持预填 tab（supply / requirement / post）
  if (query?.tab && tabs.some(t => t.value === query.tab)) {
    activeTab.value = query.tab
  }
  if (query?.keyword) {
    keyword.value = query.keyword
    nextTick(() => handleSearch())
  }
})

onReachBottom(() => {
  if (loadStatus.value === 'more') {
    loadNextPage()
  }
})

/* ===== 搜索历史 ===== */
function loadHistory() {
  try {
    const raw = uni.getStorageSync(HISTORY_KEY)
    if (raw) searchHistory.value = JSON.parse(raw)
  } catch { /* ignore */ }
}

function saveHistory(kw: string) {
  const list = searchHistory.value.filter(h => h !== kw)
  list.unshift(kw)
  if (list.length > MAX_HISTORY) list.length = MAX_HISTORY
  searchHistory.value = list
  uni.setStorageSync(HISTORY_KEY, JSON.stringify(list))
}

function handleClearHistory() {
  searchHistory.value = []
  uni.removeStorageSync(HISTORY_KEY)
}

function handleHistoryTap(kw: string) {
  keyword.value = kw
  handleSearch()
}

/* ===== 搜索执行 ===== */
async function handleSearch() {
  const kw = keyword.value.trim()
  if (!kw) return

  saveHistory(kw)
  currentPage.value = 1
  total.value = 0
  results.value = []
  searched.value = true

  await loadSearchResults()
}

async function handleTabChange(tabValue: string) {
  activeTab.value = tabValue
  if (!searched.value) return
  currentPage.value = 1
  total.value = 0
  results.value = []
  await loadSearchResults()
}

async function loadSearchResults() {
  if (loading.value) return
  loading.value = true

  try {
    const params: Record<string, any> = {
      keyword: keyword.value.trim(),
      page: currentPage.value,
      size: PAGE_SIZE,
    }
    if (activeTab.value) {
      params.entityType = activeTab.value
    }

    const res: SearchPageResult = await searchUnified(params)
    if (currentPage.value === 1) {
      results.value = res.records || []
    } else {
      results.value = [...results.value, ...(res.records || [])]
    }
    total.value = res.total || 0
  } catch {
    // error handled by request.ts
  } finally {
    loading.value = false
  }
}

async function loadNextPage() {
  currentPage.value++
  await loadSearchResults()
}

function handleLoadMore() {
  if (loadStatus.value === 'more') {
    loadNextPage()
  }
}

/* ===== 导航 ===== */
function handleCancel() {
  uni.navigateBack()
}

function handleResultTap(item: UnifiedSearchResult) {
  const routeMap: Record<string, string> = {
    supply: '/pages/supply/detail',
    requirement: '/pages/requirement/detail',
    post: '/pages/topic/detail',
  }
  const base = routeMap[item.entityType]
  if (base) {
    uni.navigateTo({ url: `${base}?id=${item.entityId}` })
  }
}

/* ===== 辅助函数 ===== */
function getTypeBadge(type: string): { label: string; colorClass: string } {
  switch (type) {
    case 'supply':
      return { label: '供应', colorClass: 'badge--supply' }
    case 'requirement':
      return { label: '采购', colorClass: 'badge--requirement' }
    case 'post':
      return { label: '话题', colorClass: 'badge--post' }
    default:
      return { label: type, colorClass: '' }
  }
}

function parseTags(tagsJson?: string): string[] {
  if (!tagsJson) return []
  try {
    const arr = JSON.parse(tagsJson)
    return Array.isArray(arr) ? arr.slice(0, 3) : []
  } catch {
    return []
  }
}

function getContentPreview(content?: string): string {
  if (!content) return ''
  return content.length > 100 ? content.slice(0, 100) + '...' : content
}
</script>

<template>
  <view class="search-page">
    <WgNavBar title="搜索" />

    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-bar__input-wrap">
        <WgIcon name="search" :size="16" :color="WARM_400" />
        <input
          v-model="keyword"
          class="search-bar__input"
          placeholder="搜索供应/采购/话题"
          confirm-type="search"
          focus
          @confirm="handleSearch"
        />
        <view
          v-if="keyword"
          class="search-bar__clear"
          @tap="keyword = ''"
        >
          <WgIcon name="clear" :size="16" color="#ccc" />
        </view>
      </view>
      <text class="search-bar__cancel" @tap="handleCancel">取消</text>
    </view>

    <!-- Tab 筛选 -->
    <view v-if="searched" class="tab-bar">
      <view
        v-for="tab in tabs"
        :key="tab.value"
        class="tab-bar__item"
        :class="{ 'tab-bar__item--active': activeTab === tab.value }"
        @tap="handleTabChange(tab.value)"
      >
        <text>{{ tab.label }}</text>
      </view>
    </view>

    <!-- 搜索历史 -->
    <view v-if="showHistory" class="history-section">
      <view class="history-section__header">
        <text class="history-section__title">搜索历史</text>
        <view class="history-section__clear" @tap="handleClearHistory">
          <WgIcon name="trash" :size="16" :color="WARM_400" />
          <text>清除</text>
        </view>
      </view>
      <view class="history-section__tags">
        <view
          v-for="(item, index) in searchHistory"
          :key="index"
          class="history-tag"
          @tap="handleHistoryTap(item)"
        >
          <text class="history-tag__text">{{ item }}</text>
        </view>
      </view>
    </view>

    <!-- 加载骨架屏 -->
    <WgSkeleton
      v-if="showSkeleton"
      type="list"
      :rows="5"
    />

    <!-- 搜索结果列表 -->
    <view v-if="showResults" class="result-list">
      <view
        v-for="item in results"
        :key="`${item.entityType}-${item.entityId}`"
        class="result-card"
        @tap="handleResultTap(item)"
      >
        <view class="result-card__header">
          <view
            class="result-card__badge"
            :class="getTypeBadge(item.entityType).colorClass"
          >
            <text class="result-card__badge-text">{{ getTypeBadge(item.entityType).label }}</text>
          </view>
          <text class="result-card__time">{{ formatRelativeTime(item.createTime) }}</text>
        </view>

        <view class="result-card__body">
          <view class="result-card__main">
            <text class="result-card__title">{{ item.title }}</text>
            <text class="result-card__content">{{ getContentPreview(item.content) }}</text>
          </view>
          <image
            v-if="item.imageUrl"
            class="result-card__image"
            :src="item.imageUrl"
            mode="aspectFill"
          />
        </view>

        <!-- 标签 -->
        <view v-if="parseTags(item.tagsJson).length > 0" class="result-card__tags">
          <text
            v-for="(tag, idx) in parseTags(item.tagsJson)"
            :key="idx"
            class="result-card__tag"
          >{{ tag }}</text>
        </view>

        <view class="result-card__footer">
          <view class="result-card__info">
            <text v-if="item.companyName" class="result-card__company">{{ item.companyName }}</text>
            <text v-if="item.companyName && item.userName" class="result-card__dot"> · </text>
            <text v-if="item.userName" class="result-card__user">{{ item.userName }}</text>
          </view>
          <WgIcon name="right" :size="12" color="#ccc" />
        </view>
      </view>

      <WgLoadMore :status="loadStatus" @loadMore="handleLoadMore" />
    </view>

    <!-- 空结果 -->
    <WgEmpty
      v-if="showEmpty"
      icon="search"
      text="未找到相关结果"
      description="换个关键词试试吧"
    />
  </view>
</template>

<style lang="scss" scoped>
.search-page {
  min-height: 100vh;
  background: $bg-page;
}

/* ===== 搜索栏 ===== */
.search-bar {
  display: flex;
  align-items: center;
  padding: $spacing-sm $spacing-md;
  background: $bg-card;
  gap: $spacing-sm;

  &__input-wrap {
    flex: 1;
    display: flex;
    align-items: center;
    height: 72rpx;
    background: $bg-page;
    border-radius: $radius-lg;
    padding: 0 $spacing-sm;
    gap: $spacing-xs;
  }

  &__input {
    flex: 1;
    height: 72rpx;
    font-size: $font-md;
  }

  &__clear {
    padding: $spacing-xs;
    display: flex;
    align-items: center;
    justify-content: center;
  }

  &__cancel {
    color: $text-secondary;
    font-size: $font-md;
    flex-shrink: 0;
  }
}

/* ===== Tab 栏 ===== */
.tab-bar {
  display: flex;
  background: $bg-card;
  padding: 0 $spacing-md;
  border-bottom: 1rpx solid $border-light;

  &__item {
    padding: $spacing-sm $spacing-md;
    font-size: $font-md;
    color: $text-secondary;
    position: relative;
    flex-shrink: 0;

    &--active {
      color: $brand-600;
      font-weight: bold;

      &::after {
        content: '';
        position: absolute;
        bottom: 0;
        left: 50%;
        transform: translateX(-50%);
        width: 48rpx;
        height: 4rpx;
        background: $brand-600;
        border-radius: 2rpx;
      }
    }
  }
}

/* ===== 搜索历史 ===== */
.history-section {
  padding: $spacing-md;

  &__header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: $spacing-sm;
  }

  &__title {
    font-size: $font-md;
    color: $text-primary;
    font-weight: bold;
  }

  &__clear {
    display: flex;
    align-items: center;
    gap: 4rpx;
    font-size: $font-sm;
    color: $text-placeholder;
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }
}

.history-tag {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-xs $spacing-md;
  border: 1rpx solid $border-light;

  &:active {
    background: $bg-hover;
  }

  &__text {
    font-size: $font-sm;
    color: $text-secondary;
  }
}

/* ===== 搜索结果 ===== */
.result-list {
  padding: $spacing-sm;
}

.result-card {
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;

  &:active {
    background: $bg-hover;
  }

  &__header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: $spacing-sm;
  }

  &__badge {
    padding: 4rpx 16rpx;
    border-radius: $radius-sm;

    &--supply {
      background: $brand-50;
    }
    &--requirement {
      background: rgba($autumn-400, 0.12);
    }
    &--post {
      background: rgba($action-600, 0.08);
    }
  }

  &__badge-text {
    font-size: $font-xs;
    font-weight: bold;

    .badge--supply & {
      color: $brand-600;
    }
    .badge--requirement & {
      color: $autumn-500;
    }
    .badge--post & {
      color: $action-600;
    }
  }

  &__time {
    font-size: $font-xs;
    color: $text-placeholder;
  }

  &__body {
    display: flex;
    gap: $spacing-sm;
    margin-bottom: $spacing-sm;
  }

  &__main {
    flex: 1;
    min-width: 0;
  }

  &__title {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-primary;
    display: -webkit-box;
    overflow: hidden;
    text-overflow: ellipsis;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    margin-bottom: $spacing-xs;
  }

  &__content {
    font-size: $font-sm;
    color: $text-secondary;
    line-height: 1.6;
    display: -webkit-box;
    overflow: hidden;
    text-overflow: ellipsis;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }

  &__image {
    width: 160rpx;
    height: 120rpx;
    border-radius: $radius-sm;
    flex-shrink: 0;
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs;
    margin-bottom: $spacing-sm;
  }

  &__tag {
    font-size: $font-xs;
    color: $brand-600;
    background: $brand-50;
    padding: 4rpx 16rpx;
    border-radius: $radius-sm;
  }

  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: $spacing-sm;
    border-top: 1rpx solid $border-light;
  }

  &__info {
    display: flex;
    align-items: center;
    min-width: 0;
    flex: 1;
    overflow: hidden;
  }

  &__company {
    font-size: $font-xs;
    color: $text-secondary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__dot {
    font-size: $font-xs;
    color: $text-placeholder;
    flex-shrink: 0;
  }

  &__user {
    font-size: $font-xs;
    color: $text-placeholder;
    flex-shrink: 0;
  }
}
</style>
