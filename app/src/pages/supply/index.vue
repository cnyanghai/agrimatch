<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { listSupplies, type SupplyResponse } from '../../api/supply'
import { getSchemaTree, type ProductSchemaVO, type CategoryNode } from '../../api/productSchema'
import { formatPrice, formatRelativeTime } from '../../utils/format'

const PAGE_SIZE = 20

const allData = ref<SupplyResponse[]>([])
const displayCount = ref(PAGE_SIZE)
const loading = ref(false)
const keyword = ref('')
const sortMode = ref<'newest' | 'priceDesc' | 'priceAsc'>('newest')

// Schema + Category 筛选
const schemaList = ref<ProductSchemaVO[]>([])
const activeSchemaIndex = ref(-1) // -1 = 全部
const activeCategoryName = ref('') // 空 = 该 schema 下全部

const activeSchema = computed(() => {
  if (activeSchemaIndex.value < 0) return null
  return schemaList.value[activeSchemaIndex.value] || null
})

/** 当前 schema 下的一级分类（展平二级） */
const categoryPills = computed<string[]>(() => {
  const schema = activeSchema.value
  if (!schema) return []
  const names: string[] = []
  for (const cat of schema.categories) {
    names.push(cat.name)
    if (cat.children?.length) {
      for (const child of cat.children) {
        names.push(child.name)
      }
    }
  }
  return names
})

const filteredList = computed(() => {
  let list = [...allData.value]
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    list = list.filter(item =>
      item.categoryName.toLowerCase().includes(kw) ||
      (item.companyName || '').toLowerCase().includes(kw) ||
      (item.origin || '').toLowerCase().includes(kw)
    )
  }
  if (sortMode.value === 'priceDesc') {
    list.sort((a, b) => (b.exFactoryPrice || 0) - (a.exFactoryPrice || 0))
  } else if (sortMode.value === 'priceAsc') {
    list.sort((a, b) => (a.exFactoryPrice || 0) - (b.exFactoryPrice || 0))
  }
  return list
})

const displayList = computed(() => filteredList.value.slice(0, displayCount.value))

const loadStatus = computed(() => {
  if (loading.value) return 'loading'
  if (displayCount.value >= filteredList.value.length) return 'noMore'
  return 'more'
})

watch([keyword, sortMode], () => {
  displayCount.value = PAGE_SIZE
})

onMounted(() => {
  loadSchemas()
  loadData()
})

onPullDownRefresh(() => {
  loadData().finally(() => {
    uni.stopPullDownRefresh()
  })
})

onReachBottom(() => {
  loadMore()
})

async function loadSchemas() {
  try {
    const res = await getSchemaTree()
    schemaList.value = res || []
  } catch {
    // schema 加载失败不影响列表
  }
}

function selectSchema(index: number) {
  if (activeSchemaIndex.value === index) return
  activeSchemaIndex.value = index
  activeCategoryName.value = ''
  displayCount.value = PAGE_SIZE
  loadData()
}

function selectCategory(name: string) {
  if (activeCategoryName.value === name) {
    activeCategoryName.value = ''
  } else {
    activeCategoryName.value = name
  }
  displayCount.value = PAGE_SIZE
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const params: any = { activeOnly: true, orderBy: 'create_time', order: 'desc' }
    const schema = activeSchema.value
    if (schema) {
      params.schemaCode = schema.schemaCode
    }
    if (activeCategoryName.value) {
      params.categoryName = activeCategoryName.value
    }
    const res = await listSupplies(params)
    allData.value = res || []
    displayCount.value = PAGE_SIZE
  } catch {
    // handled by request.ts
  } finally {
    loading.value = false
  }
}

function loadMore() {
  if (displayCount.value < filteredList.value.length) {
    displayCount.value += PAGE_SIZE
  }
}

function goDetail(id: number) {
  uni.navigateTo({ url: `/pages/supply/detail?id=${id}` })
}

function goPublish() {
  uni.navigateTo({ url: '/pages/supply/publish' })
}
</script>

<template>
  <view class="supply-page">
    <!-- Sticky filter bar -->
    <view class="filter-bar">
      <view class="filter-bar__search">
        <uni-icons type="search" size="16" color="#A8A29E" />
        <input
          v-model="keyword"
          class="filter-bar__input"
          placeholder="搜索商品/企业/产地"
          placeholder-class="filter-bar__placeholder"
          confirm-type="search"
        />
      </view>

      <!-- Schema 标签 -->
      <scroll-view scroll-x class="filter-bar__schema-scroll" :show-scrollbar="false">
        <view class="filter-bar__schemas">
          <text
            class="filter-bar__schema"
            :class="{ 'filter-bar__schema--active': activeSchemaIndex === -1 }"
            @tap="selectSchema(-1)"
          >全部</text>
          <text
            v-for="(schema, idx) in schemaList"
            :key="schema.schemaCode"
            class="filter-bar__schema"
            :class="{ 'filter-bar__schema--active': activeSchemaIndex === idx }"
            @tap="selectSchema(idx)"
          >{{ schema.schemaName }}</text>
        </view>
      </scroll-view>

      <!-- 二级分类胶囊 -->
      <scroll-view v-if="categoryPills.length > 0" scroll-x class="filter-bar__cat-scroll" :show-scrollbar="false">
        <view class="filter-bar__cats">
          <text
            class="filter-bar__cat"
            :class="{ 'filter-bar__cat--active': activeCategoryName === '' }"
            @tap="activeCategoryName = ''; displayCount = PAGE_SIZE; loadData()"
          >全部</text>
          <text
            v-for="name in categoryPills"
            :key="name"
            class="filter-bar__cat"
            :class="{ 'filter-bar__cat--active': activeCategoryName === name }"
            @tap="selectCategory(name)"
          >{{ name }}</text>
        </view>
      </scroll-view>

      <!-- 排序 -->
      <view class="filter-bar__pills">
        <text
          class="filter-bar__pill"
          :class="{ 'filter-bar__pill--active': sortMode === 'newest' }"
          @tap="sortMode = 'newest'"
        >最新</text>
        <text
          class="filter-bar__pill"
          :class="{ 'filter-bar__pill--active': sortMode === 'priceDesc' }"
          @tap="sortMode = 'priceDesc'"
        >价高</text>
        <text
          class="filter-bar__pill"
          :class="{ 'filter-bar__pill--active': sortMode === 'priceAsc' }"
          @tap="sortMode = 'priceAsc'"
        >价低</text>
      </view>
    </view>

    <!-- List -->
    <view v-if="displayList.length > 0" class="list">
      <view
        v-for="item in displayList"
        :key="item.id"
        class="supply-card tap-feedback"
        @tap="goDetail(item.id)"
      >
        <view class="supply-card__content">
          <view class="supply-card__top">
            <view class="supply-card__title-row">
              <text class="supply-card__name">{{ item.categoryName }}</text>
              <text v-if="item.origin" class="supply-card__badge">{{ item.origin }}</text>
            </view>
            <text class="supply-card__price">{{ formatPrice(item.exFactoryPrice) }}</text>
          </view>
          <view class="supply-card__company-row">
            <uni-icons type="shop" size="14" color="#A8A29E" />
            <text class="supply-card__company">{{ item.companyName || item.nickName || item.userName }}</text>
          </view>
          <view class="supply-card__tags">
            <text v-if="item.quantity" class="supply-card__tag">{{ item.quantity }}吨</text>
            <text v-if="item.deliveryMode" class="supply-card__tag">{{ item.deliveryMode }}</text>
            <text v-if="item.paymentMethod" class="supply-card__tag">{{ item.paymentMethod }}</text>
          </view>
          <view class="supply-card__bottom">
            <view class="supply-card__meta">
              <text class="supply-card__time">{{ formatRelativeTime(item.createTime) }}</text>
              <view v-if="item.shipAddress" class="supply-card__location">
                <uni-icons type="location" size="12" color="#A8A29E" />
                <text class="supply-card__address">{{ item.shipAddress }}</text>
              </view>
            </view>
            <view class="supply-card__action" @tap.stop="goDetail(item.id)">
              <text class="supply-card__action-text">咨询</text>
            </view>
          </view>
        </view>
      </view>
      <WgLoadMore :status="loadStatus" @loadMore="loadMore" />
    </view>

    <!-- Empty -->
    <WgEmpty v-else-if="!loading" text="暂无供应信息" description="目前还没有供应信息发布" />

    <!-- Loading (initial only) -->
    <WgSkeleton v-if="loading && allData.length === 0" type="card" :rows="3" />

    <!-- FAB -->
    <view class="fab anim-fab-enter" @tap="goPublish">
      <uni-icons type="plusempty" size="28" color="#fff" />
    </view>

    <WgTabBar :current="1" />
  </view>
</template>

<style lang="scss" scoped>
.supply-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: 130rpx;
}

/* ===== Filter Bar ===== */
.filter-bar {
  position: sticky;
  top: 0;
  z-index: 10;
  background: #ffffff;
  padding: $spacing-sm $spacing-md 0;
  box-shadow: $shadow-warm-card;

  &__search {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    background: $warm-100;
    border-radius: $radius-pill;
    padding: $spacing-sm $spacing-lg;
    margin-bottom: $spacing-sm;
  }

  &__input {
    flex: 1;
    font-size: $font-md;
    color: $text-primary;
    background: transparent;
  }

  &__placeholder {
    color: $text-placeholder;
    font-size: $font-md;
  }

  /* Schema 标签行 */
  &__schema-scroll {
    white-space: nowrap;
    margin-bottom: $spacing-xs;
  }

  &__schemas {
    display: inline-flex;
    gap: $spacing-sm;
    padding: 0 $spacing-xs $spacing-xs;
  }

  &__schema {
    display: inline-block;
    font-size: $font-md;
    color: $text-secondary;
    padding: $spacing-xs $spacing-lg;
    border-radius: $radius-pill;
    background: $warm-100;
    white-space: nowrap;
    transition: all 0.2s;
    font-weight: 500;

    &--active {
      color: #ffffff;
      background: $brand-600;
      font-weight: 600;
    }
  }

  /* 二级分类胶囊行 */
  &__cat-scroll {
    white-space: nowrap;
    margin-bottom: $spacing-xs;
  }

  &__cats {
    display: inline-flex;
    gap: $spacing-xs;
    padding: 0 $spacing-xs $spacing-xs;
  }

  &__cat {
    display: inline-block;
    font-size: $font-sm;
    color: $text-secondary;
    padding: 6rpx $spacing-md;
    border-radius: $radius-pill;
    background: $warm-100;
    white-space: nowrap;
    transition: all 0.2s;

    &--active {
      color: $brand-600;
      background: $brand-50;
      font-weight: 600;
    }
  }

  /* 排序行 */
  &__pills {
    display: flex;
    gap: $spacing-xs;
    padding: $spacing-xs 0 $spacing-sm;
  }

  &__pill {
    font-size: $font-sm;
    color: $text-secondary;
    padding: $spacing-xs $spacing-md;
    border-radius: $radius-pill;
    background: $warm-100;
    transition: all 0.2s;

    &--active {
      color: $brand-600;
      background: $brand-50;
      font-weight: 600;
    }
  }
}

/* ===== List ===== */
.list {
  padding: $spacing-md;
}

/* ===== Supply Card ===== */
.supply-card {
  background: #ffffff;
  border-radius: $radius-xl;
  margin-bottom: $spacing-md;
  overflow: hidden;
  box-shadow: $shadow-warm-card;
  transition: transform 0.15s;

  &:active {
    transform: scale(0.98);
  }

  &__content {
    padding: $spacing-lg;
  }

  &__top {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: $spacing-sm;
  }

  &__title-row {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    flex: 1;
    min-width: 0;
  }

  &__name {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__badge {
    font-size: $font-xs;
    padding: 4rpx 14rpx;
    border-radius: $radius-pill;
    flex-shrink: 0;
    white-space: nowrap;
    color: $warm-500;
    background: $warm-100;
  }

  &__price {
    font-size: $font-xl;
    font-weight: bold;
    color: $accent-400;
    flex-shrink: 0;
    margin-left: $spacing-sm;
  }

  &__company-row {
    display: flex;
    align-items: center;
    gap: 6rpx;
    margin-bottom: $spacing-sm;
  }

  &__company {
    font-size: $font-sm;
    color: $text-secondary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs;
    margin-bottom: $spacing-sm;
  }

  &__tag {
    font-size: $font-xs;
    color: $warm-500;
    background: $warm-100;
    padding: 4rpx 14rpx;
    border-radius: $radius-pill;
  }

  &__bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: $spacing-sm;
    border-top: 1rpx solid $warm-100;
  }

  &__meta {
    flex: 1;
    min-width: 0;
  }

  &__time {
    font-size: $font-xs;
    color: $text-placeholder;
    display: block;
  }

  &__location {
    display: flex;
    align-items: center;
    gap: 4rpx;
    max-width: 300rpx;
    margin-top: 4rpx;
  }

  &__address {
    font-size: $font-xs;
    color: $text-placeholder;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__action {
    flex-shrink: 0;
    padding: $spacing-xs $spacing-lg;
    background: $brand-600;
    border-radius: $radius-pill;
    margin-left: $spacing-sm;

    &:active {
      opacity: 0.85;
    }
  }

  &__action-text {
    font-size: $font-sm;
    color: #ffffff;
    font-weight: 600;
  }
}

/* ===== FAB ===== */
.fab {
  position: fixed;
  right: 32rpx;
  bottom: 180rpx;
  width: 100rpx;
  height: 100rpx;
  border-radius: 50%;
  background: $brand-600;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 6rpx 20rpx rgba(45, 106, 79, 0.2);
  transition: transform 0.15s;
  z-index: 20;

  &:active {
    transform: scale(0.92);
  }
}
</style>
