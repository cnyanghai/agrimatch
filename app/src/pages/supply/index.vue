<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { listSupplies, type SupplyResponse, type BasisQuoteResponse } from '../../api/supply'
import { getSchemaTree, type ProductSchemaVO, type CategoryNode } from '../../api/productSchema'
import { followUser, unfollowUser, batchCheckFollowStatus } from '../../api/follow'
import { openConversation } from '../../api/chat'
import { useAuthStore } from '../../store/auth'
import { formatPrice, formatRelativeTime, formatRemainingTime } from '../../utils/format'
import { parseParamTags } from '../../utils/parseParams'
import { getUnitLabel } from '../../utils/unitConfig'

const authStore = useAuthStore()

/** 记录展开基差详情的卡片 ID */
const expandedBasisIds = ref<Set<number>>(new Set())

function toggleBasis(id: number) {
  const s = expandedBasisIds.value
  if (s.has(id)) {
    s.delete(id)
  } else {
    s.add(id)
  }
}

/** 获取卡片的质量参数标签（最多5个） */
function getParamTags(item: SupplyResponse): string[] {
  return parseParamTags(item.paramsJson, 5)
}

/** 格式化基差价格，正数显示+号 */
function formatBasisPrice(price: number): string {
  return price >= 0 ? `+${price}` : `${price}`
}

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
    list = list.filter(item => {
      // 基础字段
      if (item.categoryName.toLowerCase().includes(kw)) return true
      if ((item.companyName || '').toLowerCase().includes(kw)) return true
      if ((item.origin || '').toLowerCase().includes(kw)) return true
      // 扩展字段：昵称、用户名、包装、付款、发货地、备注
      if ((item.nickName || '').toLowerCase().includes(kw)) return true
      if ((item.userName || '').toLowerCase().includes(kw)) return true
      if ((item.packaging || '').toLowerCase().includes(kw)) return true
      if ((item.paymentMethod || '').toLowerCase().includes(kw)) return true
      if ((item.shipAddress || '').toLowerCase().includes(kw)) return true
      if ((item.remark || '').toLowerCase().includes(kw)) return true
      // paramsJson 参数键值匹配
      if (item.paramsJson) {
        const tags = parseParamTags(item.paramsJson, 999)
        if (tags.some(tag => tag.toLowerCase().includes(kw))) return true
      }
      return false
    })
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
    // Task 6: 批量加载关注状态
    loadFollowStatus(allData.value)
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

// ===== Task 6: Follow functionality =====
const followingMap = ref<Map<number, boolean>>(new Map())

/** 批量加载关注状态（列表加载后调用，不逐卡片请求） */
async function loadFollowStatus(items: SupplyResponse[]) {
  if (!authStore.isLoggedIn) return
  const myUserId = authStore.user?.userId
  const userIds = [...new Set(
    items
      .map(item => item.userId)
      .filter((uid): uid is number => !!uid && uid !== myUserId)
  )]
  // 过滤已缓存的
  const unchecked = userIds.filter(uid => !followingMap.value.has(uid))
  if (unchecked.length === 0) return

  try {
    const result = await batchCheckFollowStatus(unchecked)
    for (const [uid, following] of result) {
      followingMap.value.set(uid, following)
    }
  } catch {
    // 静默失败，不影响列表展示
  }
}

function isFollowingUser(userId?: number): boolean {
  if (!userId) return false
  return followingMap.value.get(userId) || false
}

function isSelfUser(userId?: number): boolean {
  if (!userId || !authStore.user?.userId) return true
  return userId === authStore.user.userId
}

async function toggleFollow(item: SupplyResponse) {
  if (!authStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  if (!item.userId) {
    uni.showToast({ title: '无法关注该用户', icon: 'none' })
    return
  }
  if (isSelfUser(item.userId)) {
    uni.showToast({ title: '不能关注自己', icon: 'none' })
    return
  }

  const isFollowing = followingMap.value.get(item.userId) || false
  try {
    if (isFollowing) {
      await unfollowUser(item.userId)
      followingMap.value.set(item.userId, false)
      uni.showToast({ title: '已取消关注', icon: 'success' })
    } else {
      await followUser(item.userId)
      followingMap.value.set(item.userId, true)
      uni.showToast({ title: '已关注', icon: 'success' })
    }
  } catch {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

// ===== Task 7: Direct chat from list =====
function buildSupplySnapshot(item: SupplyResponse): string {
  return JSON.stringify({
    snapshotTime: new Date().toLocaleString('zh-CN'),
    title: item.categoryName,
    categoryName: item.categoryName,
    companyName: item.companyName,
    nickName: item.nickName,
    priceType: item.priceType,
    exFactoryPrice: item.exFactoryPrice,
    basisQuotes: item.basisQuotes,
    quantity: item.quantity,
    remainingQuantity: item.remainingQuantity,
    shipAddress: item.shipAddress,
    deliveryMode: item.deliveryMode,
    packaging: item.packaging,
    storageMethod: item.storageMethod,
    paramsJson: item.paramsJson,
    remark: item.remark,
  })
}

async function handleConsult(item: SupplyResponse) {
  if (!authStore.isLoggedIn) {
    uni.navigateTo({ url: '/pages/auth/login' })
    return
  }
  if (!item.userId || !item.id) {
    uni.showToast({ title: '该条信息暂不支持咨询', icon: 'none' })
    return
  }
  if (isSelfUser(item.userId)) {
    uni.showToast({ title: '不能和自己聊天', icon: 'none' })
    return
  }

  try {
    uni.showLoading({ title: '正在打开会话...' })
    const conversationId = await openConversation({
      peerUserId: item.userId,
      subjectType: 'SUPPLY',
      subjectId: item.id,
      subjectSnapshotJson: buildSupplySnapshot(item),
    })
    uni.hideLoading()
    const peerName = item.companyName || item.nickName || item.userName || ''
    uni.navigateTo({
      url: `/pages/chat/conversation?id=${conversationId}&peerId=${item.userId}&name=${encodeURIComponent(peerName)}`,
    })
  } catch {
    uni.hideLoading()
    uni.showToast({ title: '发起咨询失败', icon: 'none' })
  }
}
</script>

<template>
  <view class="supply-page">
    <!-- Sticky filter bar -->
    <view class="filter-bar">
      <view class="filter-bar__search">
        <WgIcon name="search" :size="16" color="#A8A29E" />
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
              <!-- 基差报价标签 (Task 2) -->
              <text v-if="item.priceType === 1" class="supply-card__badge supply-card__badge--basis">基差报价</text>
            </view>
            <!-- 一口价显示价格，基差报价显示"基差报价"文字 (Task 2) -->
            <text v-if="item.priceType !== 1" class="supply-card__price">{{ formatPrice(item.exFactoryPrice) }}</text>
          </view>
          <view class="supply-card__company-row">
            <WgIcon name="store" :size="14" color="#A8A29E" />
            <text class="supply-card__company">{{ item.companyName || item.nickName || item.userName }}</text>
            <!-- Task 6: Follow button -->
            <view
              v-if="authStore.isLoggedIn && item.userId && !isSelfUser(item.userId)"
              class="supply-card__follow-btn"
              :class="{ 'supply-card__follow-btn--active': isFollowingUser(item.userId) }"
              @tap.stop="toggleFollow(item)"
            >
              <text
                class="supply-card__follow-text"
                :class="{ 'supply-card__follow-text--active': isFollowingUser(item.userId) }"
              >{{ isFollowingUser(item.userId) ? '已关注' : '+ 关注' }}</text>
            </view>
          </view>
          <view class="supply-card__tags">
            <text v-if="item.quantity" class="supply-card__tag">{{ item.quantity }}{{ getUnitLabel(item.schemaCode, 'quantity', item.categoryName) }}</text>
            <text v-if="item.deliveryMode" class="supply-card__tag">{{ item.deliveryMode }}</text>
            <text v-if="item.paymentMethod" class="supply-card__tag">{{ item.paymentMethod }}</text>
          </view>
          <!-- 质量指标标签 (Task 1) -->
          <scroll-view v-if="getParamTags(item).length > 0" scroll-x class="supply-card__params-scroll" :show-scrollbar="false">
            <view class="supply-card__params">
              <text
                v-for="(tag, idx) in getParamTags(item)"
                :key="idx"
                class="supply-card__param-tag"
              >{{ tag }}</text>
            </view>
          </scroll-view>
          <!-- 基差报价展开区域 (Task 2) -->
          <view v-if="item.priceType === 1 && item.basisQuotes && item.basisQuotes.length > 0" class="supply-card__basis-section">
            <view class="supply-card__basis-toggle" @tap.stop="toggleBasis(item.id)">
              <text class="supply-card__basis-toggle-text">
                {{ expandedBasisIds.has(item.id) ? '收起报价' : `查看基差报价 (${item.basisQuotes.length})` }}
              </text>
              <WgIcon :name="expandedBasisIds.has(item.id) ? 'chevron-up' : 'chevron-down'" :size="12" color="#B45309" />
            </view>
            <view v-if="expandedBasisIds.has(item.id)" class="supply-card__basis-list">
              <view
                v-for="bq in item.basisQuotes"
                :key="bq.id"
                class="supply-card__basis-item"
              >
                <text class="supply-card__basis-contract">{{ bq.contractName || bq.contractCode }}</text>
                <text
                  class="supply-card__basis-price"
                  :class="bq.basisPrice >= 0 ? 'supply-card__basis-price--up' : 'supply-card__basis-price--down'"
                >{{ formatBasisPrice(bq.basisPrice) }}</text>
                <text v-if="bq.referencePrice != null" class="supply-card__basis-ref">{{ '\u2192' }} ¥{{ bq.referencePrice }}</text>
                <text class="supply-card__basis-qty">{{ bq.remainingQty ?? bq.availableQty }}{{ getUnitLabel(item.schemaCode, 'quantity', item.categoryName) }}</text>
              </view>
            </view>
          </view>
          <view class="supply-card__bottom">
            <view class="supply-card__meta">
              <view class="supply-card__time-row">
                <text class="supply-card__time">{{ formatRelativeTime(item.createTime) }}</text>
                <text
                  v-if="formatRemainingTime(item.expireTime)"
                  class="supply-card__expire"
                  :class="{
                    'supply-card__expire--warning': formatRemainingTime(item.expireTime)?.level === 'warning',
                    'supply-card__expire--expired': formatRemainingTime(item.expireTime)?.level === 'expired',
                  }"
                >{{ formatRemainingTime(item.expireTime)?.text }}</text>
              </view>
              <view v-if="item.shipAddress" class="supply-card__location">
                <WgIcon name="map-pin" :size="12" color="#A8A29E" />
                <text class="supply-card__address">{{ item.shipAddress }}</text>
              </view>
            </view>
            <view class="supply-card__action" @tap.stop="handleConsult(item)">
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
      <WgIcon name="plus" :size="24" color="#fff" />
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
    flex: 1;
    font-size: $font-sm;
    color: $text-secondary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  /* Task 6: Follow button */
  &__follow-btn {
    flex-shrink: 0;
    padding: 4rpx 16rpx;
    border-radius: $radius-pill;
    border: 1rpx solid $warm-300;
    background: #ffffff;
    margin-left: $spacing-xs;

    &--active {
      border-color: $brand-200;
      background: $brand-50;
    }

    &:active {
      opacity: 0.7;
    }
  }

  &__follow-text {
    font-size: 20rpx;
    font-weight: 600;
    color: $text-secondary;

    &--active {
      color: $brand-600;
    }
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

  /* 质量指标标签 (Task 1) */
  &__params-scroll {
    white-space: nowrap;
    margin-bottom: $spacing-sm;
  }

  &__params {
    display: inline-flex;
    gap: $spacing-xs;
    padding: 0 2rpx;
  }

  &__param-tag {
    display: inline-block;
    font-size: 20rpx;
    color: #57534E;
    background: #F5F5F4;
    border: 1rpx solid #E7E5E4;
    padding: 4rpx 12rpx;
    border-radius: $radius-sm;
    white-space: nowrap;
  }

  /* 基差报价标签 (Task 2) */
  &__badge--basis {
    color: #B45309;
    background: #FFFBEB;
    font-weight: 600;
  }

  /* 基差报价展开区域 (Task 2) */
  &__basis-section {
    margin-bottom: $spacing-sm;
  }

  &__basis-toggle {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    padding: $spacing-xs 0;
  }

  &__basis-toggle-text {
    font-size: $font-xs;
    color: #B45309;
    font-weight: 600;
  }

  &__basis-list {
    display: flex;
    flex-direction: column;
    gap: $spacing-xs;
    margin-top: $spacing-xs;
  }

  &__basis-item {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    padding: $spacing-xs $spacing-sm;
    background: #FFFBEB;
    border: 1rpx solid #FDE68A;
    border-radius: $radius-md;
  }

  &__basis-contract {
    font-size: $font-xs;
    font-weight: bold;
    color: $text-primary;
  }

  &__basis-price {
    font-size: $font-xs;
    font-weight: bold;

    &--up {
      color: #DC2626;
    }

    &--down {
      color: #16A34A;
    }
  }

  &__basis-ref {
    font-size: $font-xs;
    font-weight: bold;
    color: $brand-600;
  }

  &__basis-qty {
    font-size: $font-xs;
    color: $text-secondary;
    margin-left: auto;
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

  &__time-row {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
  }

  &__time {
    font-size: $font-xs;
    color: $text-placeholder;
  }

  &__expire {
    font-size: $font-xs;
    color: $text-secondary;
    font-weight: 500;

    &--warning {
      color: #DC2626;
      font-weight: 600;
    }

    &--expired {
      color: $text-placeholder;
    }
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
