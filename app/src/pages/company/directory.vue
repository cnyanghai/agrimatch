<script setup lang="ts">
import { ref, computed } from 'vue'
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import {
  getCompanyDirectory,
  companyTypeMap,
  type CompanyCardResponse,
} from '../../api/company'

const companyTypes = Object.entries(companyTypeMap)
const activeType = ref('feed_factory')
const activeLetter = ref('')
const companies = ref<CompanyCardResponse[]>([])
const currentPage = ref(1)
const total = ref(0)
const loading = ref(false)
const PAGE_SIZE = 20

const alphabet = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z']

const hasMore = computed(() => companies.value.length < total.value)

const loadStatus = computed<'loading' | 'more' | 'noMore'>(() => {
  if (loading.value) return 'loading'
  if (hasMore.value) return 'more'
  return 'noMore'
})

/** 当前选中的企业类型标签 */
const activeTypeLabel = computed(() => companyTypeMap[activeType.value] || '')

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
      companies.value = reset ? res.records : [...companies.value, ...res.records]
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
</script>

<template>
  <view class="directory-page">
    <!-- 类型 tabs -->
    <view class="type-tabs">
      <scroll-view scroll-x :show-scrollbar="false" class="type-tabs__scroll">
        <view class="type-tabs__list">
          <view
            v-for="[key, label] in companyTypes"
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
          <uni-icons type="location" size="14" color="#A8A29E" />
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
            <uni-icons type="list" size="14" color="#2D6A4F" />
            <text class="company-card__stat-text">已发布 {{ c.count }} 条供应</text>
          </view>
          <view v-else class="company-card__stat">
            <text class="company-card__stat-text company-card__stat-text--muted">暂无供应</text>
          </view>
          <uni-icons type="right" size="14" color="#d1d5db" />
        </view>
      </view>

      <WgLoadMore :status="loadStatus" @loadMore="loadNextPage" />
    </view>

    <!-- 空状态 -->
    <WgEmpty v-else-if="!loading" text="暂无企业" description="换个条件试试" />

    <!-- 骨架屏 -->
    <WgSkeleton v-if="loading && companies.length === 0" type="card" :rows="4" />
  </view>
</template>

<style lang="scss" scoped>
.directory-page {
  min-height: 100vh;
  background: $bg-page;
}

/* ===== Type tabs ===== */
.type-tabs {
  background: #ffffff;
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
      color: #ffffff;
      font-weight: bold;
    }
  }
}

/* ===== Letter bar ===== */
.letter-bar {
  background: #ffffff;
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
    width: 56rpx;
    height: 56rpx;
    border-radius: $radius-sm;
    font-size: $font-sm;
    color: $text-secondary;
    background: $bg-page;

    &--active {
      background: $brand-600;
      color: #ffffff;
      font-weight: bold;
    }
  }
}

/* ===== Company list ===== */
.company-list {
  padding: $spacing-sm;
}

.company-card {
  background: #ffffff;
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
