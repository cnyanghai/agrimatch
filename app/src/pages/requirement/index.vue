<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { WARM_400, WHITE, AUTUMN_500 } from '../../constants/colors'
import { onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app'
import { listRequirements, type RequirementResponse } from '../../api/requirement'
import { getSchemaTree, type ProductSchemaVO } from '../../api/productSchema'
import { followUser, unfollowUser, batchCheckFollowStatus } from '../../api/follow'
import { openConversation } from '../../api/chat'
import { useAuthStore } from '../../store/auth'
import { formatPrice, formatRelativeTime, formatRemainingTime } from '../../utils/format'
import { parseParamTags } from '../../utils/parseParams'
import { getUnitLabel } from '../../utils/unitConfig'

const authStore = useAuthStore()

function getParamTags(item: RequirementResponse): string[] {
  return parseParamTags(item.paramsJson, 5)
}

const PAGE_SIZE = 20

const allData = ref<RequirementResponse[]>([])
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
      if ((item.purchaseAddress || '').toLowerCase().includes(kw)) return true
      if ((item.nickName || '').toLowerCase().includes(kw)) return true
      if ((item.userName || '').toLowerCase().includes(kw)) return true
      if ((item.packaging || '').toLowerCase().includes(kw)) return true
      if ((item.paymentMethod || '').toLowerCase().includes(kw)) return true
      if ((item.deliveryMethod || '').toLowerCase().includes(kw)) return true
      if ((item.remark || '').toLowerCase().includes(kw)) return true
      if (item.paramsJson) {
        const tags = parseParamTags(item.paramsJson, 999)
        if (tags.some(tag => tag.toLowerCase().includes(kw))) return true
      }
      return false
    })
  }
  if (sortMode.value === 'priceDesc') {
    list.sort((a, b) => (b.expectedPrice || 0) - (a.expectedPrice || 0))
  } else if (sortMode.value === 'priceAsc') {
    list.sort((a, b) => (a.expectedPrice || 0) - (b.expectedPrice || 0))
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
    const params: any = { includeExpired: false, orderBy: 'create_time', order: 'desc' }
    const schema = activeSchema.value
    if (schema) params.schemaCode = schema.schemaCode
    if (activeCategoryName.value) params.categoryName = activeCategoryName.value
    const res = await listRequirements(params)
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
  uni.navigateTo({ url: `/pages/requirement/detail?id=${id}` })
}

function goPublish() {
  uni.navigateTo({ url: '/pages/requirement/publish' })
}

// ===== Follow =====
const followingMap = ref<Map<number, boolean>>(new Map())

async function loadFollowStatus(items: RequirementResponse[]) {
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

async function toggleFollow(item: RequirementResponse) {
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
function buildNeedSnapshot(item: RequirementResponse): string {
  return JSON.stringify({
    snapshotTime: new Date().toLocaleString('zh-CN'),
    title: item.categoryName,
    categoryName: item.categoryName,
    companyName: item.companyName,
    nickName: item.nickName,
    expectedPrice: item.expectedPrice,
    quantity: item.quantity,
    remainingQuantity: item.remainingQuantity,
    purchaseAddress: item.purchaseAddress,
    paymentMethod: item.paymentMethod,
    deliveryMethod: item.deliveryMethod,
    packaging: item.packaging,
    paramsJson: item.paramsJson,
    remark: item.remark,
  })
}

async function handleQuote(item: RequirementResponse) {
  if (!authStore.isLoggedIn) { uni.navigateTo({ url: '/pages/auth/login' }); return }
  if (!item.userId || !item.id) { uni.showToast({ title: '该条需求暂不支持报价', icon: 'none' }); return }
  if (isSelfUser(item.userId)) { uni.showToast({ title: '不能和自己聊天', icon: 'none' }); return }
  try {
    uni.showLoading({ title: '正在打开会话...' })
    const conversationId = await openConversation({
      peerUserId: item.userId,
      subjectType: 'NEED',
      subjectId: item.id,
      subjectSnapshotJson: buildNeedSnapshot(item),
    })
    uni.hideLoading()
    const peerName = item.companyName || item.nickName || item.userName || ''
    uni.navigateTo({
      url: `/pages/chat/conversation?id=${conversationId}&peerId=${item.userId}&name=${encodeURIComponent(peerName)}`,
    })
  } catch { uni.hideLoading(); uni.showToast({ title: '发起报价失败', icon: 'none' }) }
}
</script>

<template>
  <view class="req-page">
    <!-- ===== 顶部搜索 + 筛选 ===== -->
    <view class="filter-bar">
      <view class="filter-bar__search stitch-search--solid stitch-search">
        <WgIcon name="search" :size="16" :color="WARM_400" />
        <input
          v-model="keyword"
          class="filter-bar__input"
          placeholder="搜索商品/企业/地区"
          placeholder-class="filter-bar__placeholder"
          confirm-type="search"
        />
      </view>

      <!-- Schema 标签 -->
      <scroll-view scroll-x class="filter-bar__scroll" :show-scrollbar="false">
        <view class="filter-bar__chips">
          <text
            class="chip chip--autumn-scheme"
            :class="{ 'chip--autumn-active': activeSchemaIndex === -1 }"
            @tap="selectSchema(-1)"
          >全部</text>
          <text
            v-for="(schema, idx) in schemaList"
            :key="schema.schemaCode"
            class="chip chip--autumn-scheme"
            :class="{ 'chip--autumn-active': activeSchemaIndex === idx }"
            @tap="selectSchema(idx)"
          >{{ schema.schemaName }}</text>
        </view>
      </scroll-view>

      <!-- 二级分类 -->
      <scroll-view v-if="categoryPills.length > 0" scroll-x class="filter-bar__scroll" :show-scrollbar="false">
        <view class="filter-bar__chips">
          <text
            class="chip chip--sm"
            :class="{ 'chip--autumn-soft': activeCategoryName === '' }"
            @tap="activeCategoryName = ''; displayCount = PAGE_SIZE; loadData()"
          >全部</text>
          <text
            v-for="name in categoryPills"
            :key="name"
            class="chip chip--sm"
            :class="{ 'chip--autumn-soft': activeCategoryName === name }"
            @tap="selectCategory(name)"
          >{{ name }}</text>
        </view>
      </scroll-view>

      <!-- 排序 -->
      <view class="filter-bar__sort">
        <view class="sort-group sort-group--autumn">
          <text class="sort-item" :class="{ 'sort-item--autumn': sortMode === 'newest' }" @tap="sortMode = 'newest'">最新</text>
          <text class="sort-item" :class="{ 'sort-item--autumn': sortMode === 'priceDesc' }" @tap="sortMode = 'priceDesc'">价高</text>
          <text class="sort-item" :class="{ 'sort-item--autumn': sortMode === 'priceAsc' }" @tap="sortMode = 'priceAsc'">价低</text>
        </view>
      </view>
    </view>

    <!-- ===== 列表 ===== -->
    <view v-if="displayList.length > 0" class="list">
      <view
        v-for="item in displayList"
        :key="item.id"
        class="req-card stitch-card"
        @tap="goDetail(item.id)"
      >
        <!-- 左侧色条 -->
        <view class="req-card__accent" />
        <view class="req-card__body">
          <!-- 标题行 -->
          <view class="req-card__header">
            <view class="req-card__title-row">
              <text class="req-card__name">{{ item.categoryName }}</text>
              <text v-if="item.packaging" class="stitch-tag stitch-tag--autumn">{{ item.packaging }}</text>
            </view>
            <text class="req-card__price">{{ formatPrice(item.expectedPrice) }}</text>
          </view>

          <!-- 企业行 -->
          <view class="req-card__company-row">
            <WgIcon name="store" :size="14" :color="WARM_400" />
            <text class="req-card__company">{{ item.companyName || item.nickName || item.userName }}</text>
            <view
              v-if="authStore.isLoggedIn && item.userId && !isSelfUser(item.userId)"
              class="follow-btn"
              :class="{ 'follow-btn--autumn': isFollowingUser(item.userId) }"
              @tap.stop="toggleFollow(item)"
            >
              <text class="follow-btn__text">{{ isFollowingUser(item.userId) ? '已关注' : '+ 关注' }}</text>
            </view>
          </view>

          <!-- 标签 -->
          <view class="req-card__tags">
            <text v-if="item.quantity" class="stitch-tag stitch-tag--autumn">{{ item.quantity }}{{ getUnitLabel(item.schemaCode, 'quantity', item.categoryName) }}</text>
            <text v-if="item.paymentMethod" class="stitch-tag stitch-tag--autumn">{{ item.paymentMethod }}</text>
            <text v-if="item.deliveryMethod" class="stitch-tag stitch-tag--autumn">{{ item.deliveryMethod }}</text>
          </view>

          <!-- 质量参数 -->
          <scroll-view v-if="getParamTags(item).length > 0" scroll-x class="req-card__params-scroll" :show-scrollbar="false">
            <view class="req-card__params">
              <text v-for="(tag, idx) in getParamTags(item)" :key="idx" class="param-tag">{{ tag }}</text>
            </view>
          </scroll-view>

          <!-- 底部 -->
          <view class="req-card__bottom">
            <view class="req-card__meta">
              <view class="req-card__time-row">
                <text class="req-card__time">{{ formatRelativeTime(item.createTime) }}</text>
                <text
                  v-if="formatRemainingTime(item.expireTime)"
                  class="req-card__expire"
                  :class="{
                    'req-card__expire--warning': formatRemainingTime(item.expireTime)?.level === 'warning',
                    'req-card__expire--expired': formatRemainingTime(item.expireTime)?.level === 'expired',
                  }"
                >{{ formatRemainingTime(item.expireTime)?.text }}</text>
              </view>
              <view v-if="item.purchaseAddress" class="req-card__location">
                <WgIcon name="map-pin" :size="12" :color="WARM_400" />
                <text class="req-card__address">{{ item.purchaseAddress }}</text>
              </view>
            </view>
            <view class="quote-btn stitch-pill stitch-pill--autumn" @tap.stop="handleQuote(item)">
              <text class="quote-btn__text">报价</text>
            </view>
          </view>
        </view>
      </view>
      <WgLoadMore :status="loadStatus" @loadMore="loadMore" />
    </view>

    <WgEmpty v-else-if="!loading" text="暂无采购信息" description="目前还没有采购信息发布" />
    <WgSkeleton v-if="loading && allData.length === 0" type="card" :rows="3" />

    <!-- FAB -->
    <view class="stitch-fab stitch-fab--autumn anim-fab-enter" @tap="goPublish">
      <WgIcon name="plus" :size="20" :color="WHITE" />
      <text class="stitch-fab__label">发布采购</text>
    </view>

    <view style="height: 160rpx;" />
    <WgTabBar :current="2" />
  </view>
</template>

<style lang="scss" scoped>
.req-page {
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

/* ===== Chips (Autumn theme) ===== */
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

  &--sm {
    font-size: $font-sm;
    padding: 6rpx $spacing-lg;
  }

  &--autumn-scheme {
    /* default */
  }

  &--autumn-active {
    color: $text-inverse;
    background: $autumn-400;
    font-weight: 600;
  }

  &--autumn-soft {
    color: $autumn-500;
    background: $autumn-50;
    font-weight: 600;
  }
}

/* ===== Sort Group ===== */
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

  &--autumn {
    color: $autumn-500;
    background: $bg-card;
    font-weight: 700;
    box-shadow: 0 2rpx 8rpx rgba(0, 0, 0, 0.05);
  }
}

/* ===== List ===== */
.list {
  padding: $spacing-md $spacing-lg;
}

/* ===== Requirement Card ===== */
.req-card {
  display: flex;
  margin-bottom: $spacing-md;
  padding: 0;

  &__accent {
    width: 8rpx;
    flex-shrink: 0;
    background: linear-gradient(180deg, $autumn-300 0%, $autumn-400 100%);
    border-radius: $radius-2xl 0 0 $radius-2xl;
  }

  &__body {
    flex: 1;
    padding: $spacing-xl;
    min-width: 0;
  }

  &__header {
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
    font-weight: 800;
    color: $text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__price {
    font-size: $font-xl;
    font-weight: 800;
    color: $autumn-500;
    flex-shrink: 0;
    margin-left: $spacing-sm;
    font-family: 'DIN Alternate', 'Roboto Mono', -apple-system, sans-serif;
  }

  &__company-row {
    display: flex;
    align-items: center;
    gap: 8rpx;
    margin-bottom: $spacing-md;
  }

  &__company {
    flex: 1;
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

  &__params-scroll {
    white-space: nowrap;
    margin-bottom: $spacing-md;
  }

  &__params {
    display: inline-flex;
    gap: $spacing-xs;
  }

  &__bottom {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding-top: $spacing-md;
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

    &--warning { color: #DC2626; font-weight: 600; }
    &--expired { color: $text-placeholder; }
  }

  &__location {
    display: flex;
    align-items: center;
    gap: 6rpx;
    max-width: 340rpx;
    margin-top: 6rpx;
  }

  &__address {
    font-size: $font-xs;
    color: $text-placeholder;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
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

  &--autumn {
    border-color: $autumn-200;
    background: $autumn-50;
  }

  &:active { opacity: 0.7; }

  &__text {
    font-size: 20rpx;
    font-weight: 600;
    color: $text-secondary;
  }

  &--autumn &__text {
    color: $autumn-500;
  }
}

/* ===== Quote Button ===== */
.quote-btn {
  flex-shrink: 0;
  margin-left: $spacing-sm;
  padding: $spacing-sm $spacing-xl;

  &__text {
    font-size: $font-sm;
    color: $text-inverse;
    font-weight: 700;
  }
}
</style>
