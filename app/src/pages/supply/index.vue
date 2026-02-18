<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { WARM_400, WHITE, BRAND_600 } from '../../constants/colors'
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

const expandedBasisIds = ref<Set<number>>(new Set())

function toggleBasis(id: number) {
  const s = expandedBasisIds.value
  if (s.has(id)) { s.delete(id) } else { s.add(id) }
}

function getParamTags(item: SupplyResponse): string[] {
  return parseParamTags(item.paramsJson, 5)
}

function formatBasisPrice(price: number): string {
  return price >= 0 ? `+${price}` : `${price}`
}

const PAGE_SIZE = 20

const allData = ref<SupplyResponse[]>([])
const displayCount = ref(PAGE_SIZE)
const loading = ref(false)
const keyword = ref('')
const sortMode = ref<'newest' | 'priceDesc' | 'priceAsc'>('newest')

const schemaList = ref<ProductSchemaVO[]>([])
const activeSchemaIndex = ref(-1)
const activeCategoryName = ref('')

const activeSchema = computed(() => {
  if (activeSchemaIndex.value < 0) return null
  return schemaList.value[activeSchemaIndex.value] || null
})

const categoryPills = computed<string[]>(() => {
  const schema = activeSchema.value
  if (!schema) return []
  const names: string[] = []
  for (const cat of schema.categories) {
    names.push(cat.name)
    if (cat.children?.length) {
      for (const child of cat.children) { names.push(child.name) }
    }
  }
  return names
})

const filteredList = computed(() => {
  let list = [...allData.value]
  if (keyword.value.trim()) {
    const kw = keyword.value.trim().toLowerCase()
    list = list.filter(item => {
      if (item.categoryName.toLowerCase().includes(kw)) return true
      if ((item.companyName || '').toLowerCase().includes(kw)) return true
      if ((item.origin || '').toLowerCase().includes(kw)) return true
      if ((item.nickName || '').toLowerCase().includes(kw)) return true
      if ((item.userName || '').toLowerCase().includes(kw)) return true
      if ((item.packaging || '').toLowerCase().includes(kw)) return true
      if ((item.paymentMethod || '').toLowerCase().includes(kw)) return true
      if ((item.shipAddress || '').toLowerCase().includes(kw)) return true
      if ((item.remark || '').toLowerCase().includes(kw)) return true
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

watch([keyword, sortMode], () => { displayCount.value = PAGE_SIZE })

onMounted(() => {
  loadSchemas()
  loadData()
})

onPullDownRefresh(() => {
  loadData().finally(() => { uni.stopPullDownRefresh() })
})

onReachBottom(() => { loadMore() })

async function loadSchemas() {
  try {
    const res = await getSchemaTree()
    schemaList.value = res || []
  } catch { /* ignore */ }
}

function selectSchema(index: number) {
  if (activeSchemaIndex.value === index) return
  activeSchemaIndex.value = index
  activeCategoryName.value = ''
  displayCount.value = PAGE_SIZE
  loadData()
}

function selectCategory(name: string) {
  activeCategoryName.value = activeCategoryName.value === name ? '' : name
  displayCount.value = PAGE_SIZE
  loadData()
}

async function loadData() {
  loading.value = true
  try {
    const params: any = { activeOnly: true, orderBy: 'create_time', order: 'desc' }
    const schema = activeSchema.value
    if (schema) params.schemaCode = schema.schemaCode
    if (activeCategoryName.value) params.categoryName = activeCategoryName.value
    const res = await listSupplies(params)
    allData.value = res || []
    displayCount.value = PAGE_SIZE
    loadFollowStatus(allData.value)
  } catch { /* handled */ } finally { loading.value = false }
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

// ===== Follow =====
const followingMap = ref<Map<number, boolean>>(new Map())

async function loadFollowStatus(items: SupplyResponse[]) {
  if (!authStore.isLoggedIn) return
  const myUserId = authStore.user?.userId
  const userIds = [...new Set(
    items.map(item => item.userId).filter((uid): uid is number => !!uid && uid !== myUserId)
  )]
  const unchecked = userIds.filter(uid => !followingMap.value.has(uid))
  if (unchecked.length === 0) return
  try {
    const result = await batchCheckFollowStatus(unchecked)
    for (const [uid, following] of result) { followingMap.value.set(uid, following) }
  } catch { /* silent */ }
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
  if (!authStore.isLoggedIn) { uni.navigateTo({ url: '/pages/auth/login' }); return }
  if (!item.userId) { uni.showToast({ title: '无法关注该用户', icon: 'none' }); return }
  if (isSelfUser(item.userId)) { uni.showToast({ title: '不能关注自己', icon: 'none' }); return }
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
  } catch { uni.showToast({ title: '操作失败', icon: 'none' }) }
}

// ===== Chat =====
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
  if (!authStore.isLoggedIn) { uni.navigateTo({ url: '/pages/auth/login' }); return }
  if (!item.userId || !item.id) { uni.showToast({ title: '该条信息暂不支持咨询', icon: 'none' }); return }
  if (isSelfUser(item.userId)) { uni.showToast({ title: '不能和自己聊天', icon: 'none' }); return }
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
  } catch { uni.hideLoading(); uni.showToast({ title: '发起咨询失败', icon: 'none' }) }
}
</script>

<template>
  <view class="supply-page">
    <!-- ===== 顶部搜索 + 筛选 ===== -->
    <view class="filter-bar">
      <view class="filter-bar__search stitch-search--solid stitch-search">
        <WgIcon name="search" :size="16" :color="WARM_400" />
        <input
          v-model="keyword"
          class="filter-bar__input"
          placeholder="搜索商品/企业/产地"
          placeholder-class="filter-bar__placeholder"
          confirm-type="search"
        />
      </view>

      <!-- Schema 标签 -->
      <scroll-view scroll-x class="filter-bar__scroll" :show-scrollbar="false">
        <view class="filter-bar__chips">
          <text
            class="chip"
            :class="{ 'chip--active': activeSchemaIndex === -1 }"
            @tap="selectSchema(-1)"
          >全部</text>
          <text
            v-for="(schema, idx) in schemaList"
            :key="schema.schemaCode"
            class="chip"
            :class="{ 'chip--active': activeSchemaIndex === idx }"
            @tap="selectSchema(idx)"
          >{{ schema.schemaName }}</text>
        </view>
      </scroll-view>

      <!-- 二级分类胶囊 -->
      <scroll-view v-if="categoryPills.length > 0" scroll-x class="filter-bar__scroll" :show-scrollbar="false">
        <view class="filter-bar__chips">
          <text
            class="chip chip--sm"
            :class="{ 'chip--soft': activeCategoryName === '' }"
            @tap="activeCategoryName = ''; displayCount = PAGE_SIZE; loadData()"
          >全部</text>
          <text
            v-for="name in categoryPills"
            :key="name"
            class="chip chip--sm"
            :class="{ 'chip--soft': activeCategoryName === name }"
            @tap="selectCategory(name)"
          >{{ name }}</text>
        </view>
      </scroll-view>

      <!-- 排序 -->
      <view class="filter-bar__sort">
        <view class="sort-group">
          <text class="sort-item" :class="{ 'sort-item--active': sortMode === 'newest' }" @tap="sortMode = 'newest'">最新</text>
          <text class="sort-item" :class="{ 'sort-item--active': sortMode === 'priceDesc' }" @tap="sortMode = 'priceDesc'">价高</text>
          <text class="sort-item" :class="{ 'sort-item--active': sortMode === 'priceAsc' }" @tap="sortMode = 'priceAsc'">价低</text>
        </view>
      </view>
    </view>

    <!-- ===== 列表 ===== -->
    <view v-if="displayList.length > 0" class="list">
      <view
        v-for="item in displayList"
        :key="item.id"
        class="supply-card stitch-card stitch-fade-up"
        @tap="goDetail(item.id)"
      >
        <!-- 价格横幅 -->
        <view class="supply-card__hero">
          <view class="supply-card__hero-left">
            <text class="supply-card__name">{{ item.categoryName }}</text>
            <view class="supply-card__origin-row">
              <text v-if="item.origin" class="supply-card__origin">{{ item.origin }}</text>
              <text v-if="item.priceType === 1" class="supply-card__basis-badge">基差</text>
            </view>
          </view>
          <view v-if="item.priceType !== 1" class="supply-card__price-block">
            <text class="supply-card__price-sign">¥</text>
            <text class="supply-card__price">{{ item.exFactoryPrice ?? '-' }}</text>
            <text class="supply-card__price-unit">/{{ getUnitLabel(item.schemaCode, 'price', item.categoryName) }}</text>
          </view>
        </view>

        <!-- 质量参数 -->
        <scroll-view v-if="getParamTags(item).length > 0" scroll-x class="supply-card__params-scroll" :show-scrollbar="false">
          <view class="supply-card__params">
            <text v-for="(tag, idx) in getParamTags(item)" :key="idx" class="param-tag">{{ tag }}</text>
          </view>
        </scroll-view>

        <!-- 交易条件标签 -->
        <view class="supply-card__tags">
          <text v-if="item.quantity" class="stitch-tag stitch-tag--brand">{{ item.quantity }}{{ getUnitLabel(item.schemaCode, 'quantity', item.categoryName) }}</text>
          <text v-if="item.deliveryMode" class="stitch-tag stitch-tag--warm">{{ item.deliveryMode }}</text>
          <text v-if="item.paymentMethod" class="stitch-tag stitch-tag--warm">{{ item.paymentMethod }}</text>
          <text v-if="item.packaging" class="stitch-tag stitch-tag--warm">{{ item.packaging }}</text>
        </view>

        <!-- 基差报价 -->
        <view v-if="item.priceType === 1 && item.basisQuotes && item.basisQuotes.length > 0" class="basis-section">
          <view class="basis-toggle" @tap.stop="toggleBasis(item.id)">
            <text class="basis-toggle__text">
              {{ expandedBasisIds.has(item.id) ? '收起报价' : `查看基差报价 (${item.basisQuotes.length})` }}
            </text>
            <WgIcon :name="expandedBasisIds.has(item.id) ? 'chevron-up' : 'chevron-down'" :size="12" color="#B45309" />
          </view>
          <view v-if="expandedBasisIds.has(item.id)" class="basis-list">
            <view v-for="bq in item.basisQuotes" :key="bq.id" class="basis-item">
              <text class="basis-item__contract">{{ bq.contractName || bq.contractCode }}</text>
              <text class="basis-item__price" :class="bq.basisPrice >= 0 ? 'basis-item__price--up' : 'basis-item__price--down'">{{ formatBasisPrice(bq.basisPrice) }}</text>
              <text v-if="bq.referencePrice != null" class="basis-item__ref">{{ '\u2192' }} ¥{{ bq.referencePrice }}</text>
              <text class="basis-item__qty">{{ bq.remainingQty ?? bq.availableQty }}{{ getUnitLabel(item.schemaCode, 'quantity', item.categoryName) }}</text>
            </view>
          </view>
        </view>

        <!-- 底部：企业 + 时间 + 咨询 -->
        <view class="supply-card__footer">
          <view class="supply-card__seller">
            <view class="supply-card__seller-avatar">
              <text class="supply-card__seller-char">{{ (item.companyName || item.nickName || '?')[0] }}</text>
            </view>
            <view class="supply-card__seller-info">
              <text class="supply-card__company">{{ item.companyName || item.nickName || item.userName }}</text>
              <view class="supply-card__meta-row">
                <text class="supply-card__time">{{ formatRelativeTime(item.createTime) }}</text>
                <text v-if="item.shipAddress" class="supply-card__dot">·</text>
                <text v-if="item.shipAddress" class="supply-card__address">{{ item.shipAddress }}</text>
              </view>
            </view>
          </view>
          <view class="supply-card__actions">
            <view
              v-if="authStore.isLoggedIn && item.userId && !isSelfUser(item.userId)"
              class="follow-btn"
              :class="{ 'follow-btn--active': isFollowingUser(item.userId) }"
              @tap.stop="toggleFollow(item)"
            >
              <text class="follow-btn__text">{{ isFollowingUser(item.userId) ? '已关注' : '+ 关注' }}</text>
            </view>
            <view class="consult-btn" @tap.stop="handleConsult(item)">
              <WgIcon name="message-circle" :size="14" :color="WHITE" />
              <text class="consult-btn__text">咨询</text>
            </view>
          </view>
        </view>

        <!-- 过期提示 -->
        <view
          v-if="formatRemainingTime(item.expireTime)"
          class="supply-card__expire-bar"
          :class="{
            'supply-card__expire-bar--warning': formatRemainingTime(item.expireTime)?.level === 'warning',
            'supply-card__expire-bar--expired': formatRemainingTime(item.expireTime)?.level === 'expired',
          }"
        >
          <WgIcon name="clock" :size="11" :color="formatRemainingTime(item.expireTime)?.level === 'warning' ? '#DC2626' : WARM_400" />
          <text class="supply-card__expire-text">{{ formatRemainingTime(item.expireTime)?.text }}</text>
        </view>
      </view>
      <WgLoadMore :status="loadStatus" @loadMore="loadMore" />
    </view>

    <WgEmpty v-else-if="!loading" text="暂无供应信息" description="目前还没有供应信息发布" />
    <WgSkeleton v-if="loading && allData.length === 0" type="card" :rows="3" />

    <!-- FAB -->
    <view class="stitch-fab anim-fab-enter" @tap="goPublish">
      <WgIcon name="plus" :size="20" :color="WHITE" />
      <text class="stitch-fab__label">发布供应</text>
    </view>

    <view style="height: 160rpx;" />
    <WgTabBar :current="1" />
  </view>
</template>

<style lang="scss" scoped>
.supply-page {
  min-height: 100vh;
  background: $bg-page;
}

/* ===== Filter Bar ===== */
.filter-bar {
  position: sticky;
  top: 0;
  z-index: 10;
  background: $bg-card;
  padding: $spacing-md $spacing-lg 0;
  box-shadow: 0 2rpx 12rpx rgba(120, 90, 50, 0.04);

  &__search {
    margin-bottom: $spacing-md;
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

  &__scroll {
    white-space: nowrap;
    margin-bottom: $spacing-sm;
  }

  &__chips {
    display: inline-flex;
    gap: $spacing-sm;
    padding: 0 $spacing-xs $spacing-xs;
  }

  &__sort {
    padding: $spacing-xs 0 $spacing-md;
  }
}

/* ===== Chips ===== */
.chip {
  display: inline-block;
  font-size: $font-md;
  color: $text-secondary;
  padding: $spacing-xs $spacing-xl;
  border-radius: $radius-full;
  background: $warm-100;
  white-space: nowrap;
  font-weight: 500;
  transition: all $transition-fast;

  &--active {
    color: $text-inverse;
    background: $brand-600;
    font-weight: 600;
  }

  &--sm {
    font-size: $font-sm;
    padding: 6rpx $spacing-lg;
  }

  &--soft {
    color: $brand-600;
    background: $brand-50;
    font-weight: 600;
  }
}

/* ===== Sort Group (Segmented) ===== */
.sort-group {
  display: inline-flex;
  gap: 4rpx;
  background: $warm-100;
  border-radius: $radius-full;
  padding: 4rpx;
}

.sort-item {
  font-size: $font-sm;
  color: $text-secondary;
  padding: $spacing-xs $spacing-lg;
  border-radius: $radius-full;
  font-weight: 500;
  transition: all $transition-fast;

  &--active {
    color: $brand-600;
    background: $bg-card;
    font-weight: 700;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  }
}

/* ===== List ===== */
.list {
  padding: $spacing-sm $spacing-lg;
}

/* ===== Supply Card ===== */
.supply-card {
  margin-bottom: $spacing-lg;

  &__hero {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    padding: $spacing-lg;
    margin: -#{$spacing-xl} -#{$spacing-xl} $spacing-md;
    background: linear-gradient(135deg, $brand-50 0%, rgba(45, 106, 79, 0.04) 100%);
    border-radius: $radius-2xl $radius-2xl $radius-lg $radius-lg;
  }

  &__hero-left {
    flex: 1;
    min-width: 0;
  }

  &__name {
    font-size: $font-xl;
    font-weight: 800;
    color: $text-primary;
    display: block;
    margin-bottom: 6rpx;
  }

  &__origin-row {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
  }

  &__origin {
    font-size: $font-xs;
    color: $text-secondary;
  }

  &__basis-badge {
    font-size: 20rpx;
    color: $autumn-600;
    background: $autumn-50;
    padding: 2rpx 12rpx;
    border-radius: $radius-full;
    font-weight: 700;
  }

  &__price-block {
    display: flex;
    align-items: baseline;
    flex-shrink: 0;
    margin-left: $spacing-md;
  }

  &__price-sign {
    font-size: $font-md;
    font-weight: 700;
    color: $accent-400;
    margin-right: 2rpx;
  }

  &__price {
    font-size: $font-3xl;
    font-weight: 900;
    color: $accent-400;
    font-family: 'DIN Alternate', 'Roboto Mono', -apple-system, sans-serif;
    line-height: 1;
  }

  &__price-unit {
    font-size: $font-xs;
    color: $text-placeholder;
    margin-left: 4rpx;
  }

  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-xs;
    margin-bottom: $spacing-sm;
  }

  &__params-scroll {
    white-space: nowrap;
    margin-bottom: $spacing-md;
  }

  &__params {
    display: inline-flex;
    gap: $spacing-xs;
  }

  &__footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: $spacing-md;
    border-top: 1rpx solid $warm-100;
  }

  &__seller {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    flex: 1;
    min-width: 0;
  }

  &__seller-avatar {
    width: 56rpx;
    height: 56rpx;
    border-radius: 50%;
    background: $brand-50;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__seller-char {
    font-size: 22rpx;
    font-weight: 700;
    color: $brand-600;
  }

  &__seller-info {
    flex: 1;
    min-width: 0;
  }

  &__company {
    font-size: $font-sm;
    color: $text-primary;
    font-weight: 600;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    display: block;
  }

  &__meta-row {
    display: flex;
    align-items: center;
    gap: 6rpx;
    margin-top: 2rpx;
  }

  &__time {
    font-size: 22rpx;
    color: $text-placeholder;
  }

  &__dot {
    font-size: 22rpx;
    color: $text-placeholder;
  }

  &__address {
    font-size: 22rpx;
    color: $text-placeholder;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__actions {
    display: flex;
    align-items: center;
    gap: $spacing-xs;
    flex-shrink: 0;
  }

  &__expire-bar {
    display: flex;
    align-items: center;
    gap: 6rpx;
    margin-top: $spacing-sm;
    padding: 6rpx $spacing-md;
    background: $warm-50;
    border-radius: $radius-md;

    &--warning {
      background: #fef2f2;
    }

    &--expired {
      opacity: 0.6;
    }
  }

  &__expire-text {
    font-size: 22rpx;
    color: $text-secondary;
    font-weight: 500;

    .supply-card__expire-bar--warning & {
      color: #DC2626;
      font-weight: 600;
    }
  }
}

/* ===== Param Tag ===== */
.param-tag {
  display: inline-block;
  font-size: 20rpx;
  color: $warm-600;
  background: $warm-50;
  border: 1rpx solid $warm-200;
  padding: 4rpx 14rpx;
  border-radius: $radius-md;
  white-space: nowrap;
}

/* ===== Follow Button ===== */
.follow-btn {
  flex-shrink: 0;
  padding: 6rpx 18rpx;
  border-radius: $radius-full;
  border: 1rpx solid $warm-200;
  background: transparent;
  transition: all $transition-fast;

  &--active {
    border-color: $brand-200;
    background: $brand-50;
  }

  &:active { opacity: 0.7; }

  &__text {
    font-size: 20rpx;
    font-weight: 600;
    color: $text-secondary;
  }

  &--active &__text {
    color: $brand-600;
  }
}

/* ===== Consult Button ===== */
.consult-btn {
  display: flex;
  align-items: center;
  gap: 6rpx;
  padding: $spacing-xs $spacing-lg;
  background: $brand-600;
  border-radius: $radius-full;
  box-shadow: $shadow-brand;
  transition: transform $transition-fast;

  &:active { transform: scale(0.93); }

  &__text {
    font-size: $font-xs;
    color: $text-inverse;
    font-weight: 700;
  }
}

/* ===== Basis Section ===== */
.basis-section {
  margin-bottom: $spacing-sm;
}

.basis-toggle {
  display: flex;
  align-items: center;
  gap: $spacing-xs;
  padding: $spacing-xs 0;

  &__text {
    font-size: $font-xs;
    color: #B45309;
    font-weight: 600;
  }
}

.basis-list {
  display: flex;
  flex-direction: column;
  gap: $spacing-xs;
  margin-top: $spacing-xs;
}

.basis-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  background: #FFFBEB;
  border: 1rpx solid #FDE68A;
  border-radius: $radius-lg;

  &__contract {
    font-size: $font-xs;
    font-weight: bold;
    color: $text-primary;
  }

  &__price {
    font-size: $font-xs;
    font-weight: bold;

    &--up { color: #DC2626; }
    &--down { color: #16A34A; }
  }

  &__ref {
    font-size: $font-xs;
    font-weight: bold;
    color: $brand-600;
  }

  &__qty {
    font-size: $font-xs;
    color: $text-secondary;
    margin-left: auto;
  }
}
</style>
