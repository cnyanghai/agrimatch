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

/** 获取企业名首字 */
function getInitial(c: CompanyCardResponse): string {
  return c.companyName?.charAt(0) || '企'
}

/** 获取位置 */
function formatLocation(c: CompanyCardResponse): string {
  return [c.city, c.province].filter(Boolean).join(' · ')
}

/** 获取品类标签 */
function getCategoryTags(c: CompanyCardResponse): string[] {
  if (c.categoryNames && c.categoryNames.length > 0) return c.categoryNames.slice(0, 3)
  if (c.categoryNamesStr) return c.categoryNamesStr.split(',').slice(0, 3)
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
        <view class="company-card__initial">
          <text class="company-card__initial-text">{{ getInitial(c) }}</text>
        </view>
        <view class="company-card__info">
          <text class="company-card__name">{{ c.companyName }}</text>
          <text v-if="formatLocation(c)" class="company-card__location">{{ formatLocation(c) }}</text>
          <view class="company-card__bottom">
            <view v-if="getCategoryTags(c).length > 0" class="company-card__tags">
              <text
                v-for="tag in getCategoryTags(c)"
                :key="tag"
                class="company-card__tag"
              >{{ tag }}</text>
            </view>
            <text v-if="c.count" class="company-card__count">供应: {{ c.count }}</text>
          </view>
        </view>
        <uni-icons type="right" size="14" color="#d1d5db" />
      </view>

      <WgLoadMore :status="loadStatus" @loadMore="loadNextPage" />
    </view>

    <!-- 空状态 -->
    <WgEmpty v-else-if="!loading" text="暂无企业" description="换个条件试试" />

    <!-- 骨架屏 -->
    <WgSkeleton v-if="loading && companies.length === 0" type="list" :rows="5" />
  </view>
</template>

<style lang="scss" scoped>
.directory-page {
  min-height: 100vh;
  background: $bg-page;
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
      color: #ffffff;
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
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  background: $bg-card;
  border-radius: $radius-lg;
  padding: $spacing-md;
  margin-bottom: $spacing-sm;

  &__initial {
    width: 80rpx;
    height: 80rpx;
    border-radius: $radius-md;
    background: $brand-100;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__initial-text {
    font-size: $font-xl;
    font-weight: 800;
    color: $brand-600;
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__name {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__location {
    font-size: $font-xs;
    color: $text-secondary;
    display: block;
    margin-top: 4rpx;
  }

  &__bottom {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: $spacing-xs;
  }

  &__tags {
    display: flex;
    gap: $spacing-xs;
    flex-wrap: wrap;
  }

  &__tag {
    font-size: 18rpx;
    color: $brand-600;
    background: $brand-50;
    padding: 2rpx 12rpx;
    border-radius: $radius-sm;
  }

  &__count {
    font-size: $font-xs;
    color: $text-placeholder;
    flex-shrink: 0;
  }
}
</style>
