<script setup lang="ts">
import { ref, computed } from 'vue'
import { BRAND_600, WARM_300, WARM_400 } from '../../constants/colors'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import {
  getCompanyDirectory,
  searchCompanies,
  type CompanyCardResponse,
} from '../../api/company'

/** 与后端一致的两大分类：supplier / buyer */
const directoryTypes: [string, string][] = [
  ['supplier', '供应商'],
  ['buyer', '采购商'],
]
const activeType = ref('supplier')
const activeLetter = ref('')
const companies = ref<CompanyCardResponse[]>([])
const currentPage = ref(1)
const total = ref(0)
const loading = ref(false)
const PAGE_SIZE = 20

// ==================== 搜索相关 ====================
const searchKeyword = ref('')
const searchResults = ref<CompanyCardResponse[]>([])
const isSearching = ref(false)
const searchLoading = ref(false)
let searchTimer: ReturnType<typeof setTimeout> | null = null

/** 搜索状态下是否为空结果 */
const searchEmpty = computed(() => isSearching.value && !searchLoading.value && searchResults.value.length === 0)

const alphabet = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '0-9']

const hasMore = computed(() => companies.value.length < total.value)

const loadStatus = computed<'loading' | 'more' | 'noMore'>(() => {
  if (loading.value) return 'loading'
  if (hasMore.value) return 'more'
  return 'noMore'
})

/** 当前选中的企业类型标签 */
const activeTypeLabel = computed(() => {
  const found = directoryTypes.find(([k]) => k === activeType.value)
  return found ? found[1] : ''
})

onLoad(() => {
  loadData(true)
})

onPullDownRefresh(() => {
  loadData(true).finally(() => uni.stopPullDownRefresh())
})

onReachBottom(() => {
  if (hasMore.value && !loading.value) {
    loadNextPage()
  }
})

function handleTypeChange(type: string) {
  if (type === activeType.value) return
  activeType.value = type
  loadData(true)
}

function handleLetterChange(letter: string) {
  activeLetter.value = activeLetter.value === letter ? '' : letter
  loadData(true)
}

async function loadData(reset = false) {
  if (reset) {
    currentPage.value = 1
    companies.value = []
  }
  loading.value = true
  try {
    const res = await getCompanyDirectory(
      activeType.value,
      activeLetter.value || undefined,
      currentPage.value,
      PAGE_SIZE,
    )
    if (res) {
      const list = res.list || []
      companies.value = reset ? list : [...companies.value, ...list]
      total.value = res.total
    }
  } catch {
    // handled
  } finally {
    loading.value = false
  }
}

async function loadNextPage() {
  currentPage.value++
  await loadData(false)
}

function goCompanyDetail(id: number) {
  uni.navigateTo({ url: `/pages/company/detail?id=${id}` })
}

function getInitial(c: CompanyCardResponse): string {
  return c.companyName?.charAt(0) || '企'
}

function formatLocation(c: CompanyCardResponse): string {
  return [c.province, c.city].filter(Boolean).join(' ')
}

function getCategoryTags(c: CompanyCardResponse): string[] {
  if (c.categoryNames && c.categoryNames.length > 0) return c.categoryNames.slice(0, 4)
  if (c.categoryNamesStr) return c.categoryNamesStr.split(',').slice(0, 4)
  return []
}

// ==================== 搜索逻辑 ====================

/** 防抖搜索：输入变化后延迟300ms执行 */
function onSearchInput() {
  if (searchTimer) clearTimeout(searchTimer)
  const keyword = searchKeyword.value.trim()

  if (!keyword) {
    // 清空搜索，恢复列表
    isSearching.value = false
    searchResults.value = []
    return
  }

  isSearching.value = true
  searchLoading.value = true

  searchTimer = setTimeout(async () => {
    await doSearch(keyword)
  }, 300)
}

async function doSearch(keyword: string) {
  searchLoading.value = true
  try {
    const res = await searchCompanies(keyword, 50)
    searchResults.value = res ?? []
  } catch {
    searchResults.value = []
  } finally {
    searchLoading.value = false
  }
}

/** 清空搜索框 */
function clearSearch() {
  searchKeyword.value = ''
  isSearching.value = false
  searchResults.value = []
}
</script>

<template>
  <view class="directory-page">
    <WgNavBar title="企业目录" />

    <!-- 搜索栏 -->
    <view class="search-bar">
      <view class="search-bar__inner">
        <WgIcon name="search" :size="16" :color="WARM_400" />
        <input
          v-model="searchKeyword"
          class="search-bar__input"
          placeholder="搜索企业名称"
          confirm-type="search"
          @input="onSearchInput"
        />
        <view v-if="searchKeyword" class="search-bar__clear" @tap="clearSearch">
          <WgIcon name="clear" :size="16" :color="WARM_400" />
        </view>
      </view>
    </view>

    <!-- 搜索结果 -->
    <template v-if="isSearching">
      <!-- 搜索加载中 -->
      <WgSkeleton v-if="searchLoading" type="card" :rows="3" />

      <!-- 搜索结果列表 -->
      <view v-else-if="searchResults.length > 0" class="company-list">
        <view
          v-for="c in searchResults"
          :key="c.id"
          class="company-card tap-feedback"
          @tap="goCompanyDetail(c.id)"
        >
          <view class="company-card__header">
            <view class="company-card__avatar">
              <image
                v-if="c.logo"
                class="company-card__avatar-img"
                :src="c.logo"
                mode="aspectFill"
              />
              <text v-else class="company-card__avatar-text">{{ getInitial(c) }}</text>
            </view>
            <view class="company-card__header-info">
              <text class="company-card__name">{{ c.companyName }}</text>
            </view>
          </view>
          <view v-if="formatLocation(c)" class="company-card__location">
            <WgIcon name="map-pin" :size="14" :color="WARM_400" />
            <text class="company-card__location-text">{{ formatLocation(c) }}</text>
          </view>
          <view v-if="getCategoryTags(c).length > 0" class="company-card__tags">
            <text
              v-for="tag in getCategoryTags(c)"
              :key="tag"
              class="company-card__tag"
            >{{ tag }}</text>
          </view>
          <view class="company-card__footer">
            <view v-if="c.count" class="company-card__stat">
              <WgIcon name="layout-grid" :size="14" :color="BRAND_600" />
              <text class="company-card__stat-text">已发布 {{ c.count }} 条供应</text>
            </view>
            <view v-else class="company-card__stat">
              <text class="company-card__stat-text company-card__stat-text--muted">暂无供应</text>
            </view>
            <WgIcon name="right" :size="14" :color="WARM_300" />
          </view>
        </view>
      </view>

      <!-- 搜索无结果 -->
      <WgEmpty v-else text="未找到相关企业" description="换个关键词试试" />
    </template>

    <!-- 正常列表模式（非搜索状态） -->
    <template v-else>
    <!-- 类型 tabs -->
    <view class="type-tabs">
      <scroll-view scroll-x :show-scrollbar="false" class="type-tabs__scroll">
        <view class="type-tabs__list">
          <view
            v-for="[key, label] in directoryTypes"
            :key="key"
            class="type-tabs__item"
            :class="{ 'type-tabs__item--active': activeType === key }"
            @tap="handleTypeChange(key)"
          >
            <text>{{ label }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 字母索引 -->
    <view class="letter-bar">
      <scroll-view scroll-x :show-scrollbar="false" class="letter-bar__scroll">
        <view class="letter-bar__list">
          <view
            class="letter-bar__item"
            :class="{ 'letter-bar__item--active': activeLetter === '' }"
            @tap="handleLetterChange('')"
          >
            <text>全部</text>
          </view>
          <view
            v-for="l in alphabet"
            :key="l"
            class="letter-bar__item"
            :class="{ 'letter-bar__item--active': activeLetter === l }"
            @tap="handleLetterChange(l)"
          >
            <text>{{ l }}</text>
          </view>
        </view>
      </scroll-view>
    </view>

    <!-- 企业列表 -->
    <view v-if="companies.length > 0" class="company-list">
      <view
        v-for="c in companies"
        :key="c.id"
        class="company-card tap-feedback"
        @tap="goCompanyDetail(c.id)"
      >
        <!-- 顶部：头像 + 名称 + 类型标签 -->
        <view class="company-card__header">
          <view class="company-card__avatar">
            <image
              v-if="c.logo"
              class="company-card__avatar-img"
              :src="c.logo"
              mode="aspectFill"
            />
            <text v-else class="company-card__avatar-text">{{ getInitial(c) }}</text>
          </view>
          <view class="company-card__header-info">
            <text class="company-card__name">{{ c.companyName }}</text>
            <view class="company-card__type-badge">
              <text class="company-card__type-text">{{ activeTypeLabel }}</text>
            </view>
          </view>
        </view>

        <!-- 位置信息 -->
        <view v-if="formatLocation(c)" class="company-card__location">
          <WgIcon name="map-pin" :size="14" :color="WARM_400" />
          <text class="company-card__location-text">{{ formatLocation(c) }}</text>
        </view>

        <!-- 品类标签 -->
        <view v-if="getCategoryTags(c).length > 0" class="company-card__tags">
          <text
            v-for="tag in getCategoryTags(c)"
            :key="tag"
            class="company-card__tag"
          >{{ tag }}</text>
        </view>

        <!-- 底部统计 -->
        <view class="company-card__footer">
          <view v-if="c.count" class="company-card__stat">
            <WgIcon name="layout-grid" :size="14" :color="BRAND_600" />
            <text class="company-card__stat-text">已发布 {{ c.count }} 条供应</text>
          </view>
          <view v-else class="company-card__stat">
            <text class="company-card__stat-text company-card__stat-text--muted">暂无供应</text>
          </view>
          <WgIcon name="right" :size="14" :color="WARM_300" />
        </view>
      </view>

      <WgLoadMore :status="loadStatus" @loadMore="loadNextPage" />
    </view>

    <!-- 空状态 -->
    <WgEmpty v-else-if="!loading" text="暂无企业" description="换个条件试试" />

    <!-- 骨架屏 -->
    <WgSkeleton v-if="loading && companies.length === 0" type="card" :rows="4" />
    </template>
  </view>
</template>

<style lang="scss" scoped>
.directory-page {
  min-height: 100vh;
  background: $bg-page;
}

/* ===== Search bar ===== */
.search-bar {
  background: $bg-card;
  padding: $spacing-sm $spacing-md;
  border-bottom: 1rpx solid $border-light;

  &__inner {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    background: $bg-page;
    border-radius: $radius-pill;
    padding: $spacing-xs $spacing-md;
    height: 72rpx;
  }

  &__input {
    flex: 1;
    font-size: $font-md;
    color: $text-primary;
    height: 72rpx;
  }

  &__clear {
    padding: 4rpx;
    flex-shrink: 0;
  }
}

/* ===== Type tabs ===== */
.type-tabs {
  background: $bg-card;
  border-bottom: 1rpx solid $border-light;

  &__scroll {
    white-space: nowrap;
  }

  &__list {
    display: inline-flex;
    padding: $spacing-sm $spacing-md;
    gap: $spacing-sm;
  }

  &__item {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    padding: $spacing-xs $spacing-md;
    border-radius: 100rpx;
    font-size: $font-md;
    color: $text-secondary;
    background: $bg-page;
    white-space: nowrap;

    &--active {
      background: $brand-600;
      color: $text-inverse;
      font-weight: bold;
    }
  }
}

/* ===== Letter bar ===== */
.letter-bar {
  background: $bg-card;
  border-bottom: 1rpx solid $border-light;

  &__scroll {
    white-space: nowrap;
  }

  &__list {
    display: inline-flex;
    padding: $spacing-xs $spacing-md;
    gap: $spacing-xs;
  }

  &__item {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 56rpx;
    height: 56rpx;
    padding: 0 10rpx;
    border-radius: $radius-sm;
    font-size: $font-sm;
    color: $text-secondary;
    background: $bg-page;
    white-space: nowrap;

    &--active {
      background: $brand-600;
      color: $text-inverse;
      font-weight: bold;
    }
  }
}

/* ===== Company list ===== */
.company-list {
  padding: $spacing-sm;
}

.company-card {
  background: $bg-card;
  border-radius: $radius-xl;
  padding: $spacing-lg;
  margin-bottom: $spacing-md;
  box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
  transition: transform 0.15s;

  &:active {
    transform: scale(0.98);
  }

  /* Header: avatar + name + type */
  &__header {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    margin-bottom: $spacing-md;
  }

  &__avatar {
    width: 100rpx;
    height: 100rpx;
    border-radius: $radius-lg;
    background: $brand-50;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    overflow: hidden;
  }

  &__avatar-img {
    width: 100rpx;
    height: 100rpx;
  }

  &__avatar-text {
    font-size: 40rpx;
    font-weight: 800;
    color: $brand-600;
  }

  &__header-info {
    flex: 1;
    min-width: 0;
  }

  &__name {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-primary;
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin-bottom: $spacing-xs;
  }

  &__type-badge {
    display: inline-flex;
    padding: 4rpx 16rpx;
    background: $brand-50;
    border-radius: $radius-sm;
  }

  &__type-text {
    font-size: $font-xs;
    color: $brand-600;
    font-weight: 600;
  }

  /* Location */
  &__location {
    display: flex;
    align-items: center;
    gap: 6rpx;
    margin-bottom: $spacing-sm;
  }

  &__location-text {
    font-size: $font-sm;
    color: $text-secondary;
  }

  /* Category tags */
  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs;
    margin-bottom: $spacing-md;
  }

  &__tag {
    font-size: $font-xs;
    color: $autumn-500;
    background: $autumn-50;
    padding: 6rpx 18rpx;
    border-radius: $radius-pill;
    font-weight: 500;
  }

  /* Footer */
  &__footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding-top: $spacing-sm;
    border-top: 1rpx solid $border-light;
  }

  &__stat {
    display: flex;
    align-items: center;
    gap: 6rpx;
  }

  &__stat-text {
    font-size: $font-sm;
    color: $brand-600;
    font-weight: 600;

    &--muted {
      color: $text-placeholder;
      font-weight: 400;
    }
  }
}
</style>
