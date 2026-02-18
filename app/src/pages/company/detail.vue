<script setup lang="ts">
import { ref, computed } from 'vue'
import { BRAND_600, WARM_400, WHITE } from '../../constants/colors'
import { onLoad } from '@dcloudio/uni-app'
import { getCompanyProfile, companyTypeMap, type CompanyResponse } from '../../api/company'
import { openConversation } from '../../api/chat'
import { useFollow } from '../../composables/useFollow'
import { useAuthStore } from '../../store/auth'
import { maskPhone } from '../../utils/format'

const authStore = useAuthStore()
const company = ref<CompanyResponse | null>(null)
const supplies = ref<any[]>([])
const requirements = ref<any[]>([])
const loading = ref(true)
const chatLoading = ref(false)

const { isFollowing, followLoading, loadFollowStatus, handleToggleFollow, canFollow }
  = useFollow(() => company.value?.ownerUserId)

const canChat = computed(() => {
  if (!company.value?.ownerUserId) return false
  if (!authStore.isLoggedIn) return true
  return company.value.id !== authStore.user?.companyId
})

const hasCoords = computed(() =>
  company.value?.lat != null && company.value?.lng != null
    && company.value.lat !== 0 && company.value.lng !== 0
)

const fullAddress = computed(() => {
  if (!company.value) return ''
  return [company.value.province, company.value.city, company.value.district, company.value.address]
    .filter(Boolean).join('')
})

const showMapSection = computed(() => hasCoords.value || !!fullAddress.value)

const mapMarkers = computed(() => {
  if (!hasCoords.value) return []
  return [{
    id: 1, latitude: company.value!.lat!, longitude: company.value!.lng!,
    title: company.value!.companyName || '企业位置', width: 30, height: 30,
  }]
})

function handleOpenNavigation() {
  if (hasCoords.value) {
    uni.openLocation({
      latitude: Number(company.value!.lat), longitude: Number(company.value!.lng),
      name: company.value!.companyName || '企业位置', address: fullAddress.value || '',
      fail: () => { uni.showToast({ title: '无法打开导航', icon: 'none' }) },
    })
  } else if (fullAddress.value) {
    uni.setClipboardData({ data: fullAddress.value, success: () => { uni.showToast({ title: '地址已复制', icon: 'none' }) } })
  }
}

const certificates = computed<string[]>(() => {
  if (!company.value?.certificatesJson) return []
  try {
    const parsed = JSON.parse(company.value.certificatesJson)
    return Array.isArray(parsed) ? parsed.filter((url: any) => typeof url === 'string' && url.length > 0) : []
  } catch { return [] }
})

function handlePreviewCertificate(index: number) {
  uni.previewImage({ urls: certificates.value, current: certificates.value[index] || certificates.value[0] })
}

const infoRows = computed(() => {
  if (!company.value) return []
  const c = company.value
  const rows: { label: string; value: string }[] = []
  if (c.legalPerson) rows.push({ label: '法人代表', value: c.legalPerson })
  if (c.registeredCapital) rows.push({ label: '注册资本', value: c.registeredCapital })
  if (c.scale) rows.push({ label: '企业规模', value: c.scale })
  if (c.licenseNo) rows.push({ label: '营业执照', value: c.licenseNo })
  if (c.businessScope) rows.push({ label: '经营范围', value: c.businessScope })
  if (c.address) rows.push({ label: '详细地址', value: c.address })
  if (c.contacts) rows.push({ label: '联系人', value: c.contacts })
  if (c.phone) rows.push({ label: '联系电话', value: maskPhone(c.phone) })
  return rows
})

onLoad(async (options) => {
  if (options?.id) {
    try {
      const res = await getCompanyProfile(Number(options.id))
      company.value = res.company
      supplies.value = res.supplies ?? []
      requirements.value = res.requirements ?? []
      await loadFollowStatus()
    } catch { /* handled */ } finally {
      loading.value = false
    }
  }
})

function formatLocation(c: CompanyResponse): string {
  return [c.province, c.city, c.district].filter(Boolean).join(' ')
}

function formatType(type?: string): string {
  if (!type) return ''
  return companyTypeMap[type] ?? type
}

async function handleChat() {
  if (!authStore.isLoggedIn) { uni.navigateTo({ url: '/pages/auth/login' }); return }
  if (!company.value?.ownerUserId) { uni.showToast({ title: '该企业未关联用户', icon: 'none' }); return }
  if (company.value.id === authStore.user?.companyId) { uni.showToast({ title: '不能和自己的企业聊天', icon: 'none' }); return }
  if (chatLoading.value) return
  chatLoading.value = true
  try {
    let subjectType = 'SUPPLY'
    let subjectId = 0
    if (supplies.value.length > 0) { subjectId = supplies.value[0].id }
    else if (requirements.value.length > 0) { subjectType = 'NEED'; subjectId = requirements.value[0].id }
    if (!subjectId) { subjectId = company.value.id }
    const conversationId = await openConversation({ peerUserId: company.value.ownerUserId, subjectType, subjectId })
    const peerName = company.value.companyName || ''
    uni.navigateTo({ url: `/pages/chat/conversation?id=${conversationId}&peerId=${company.value.ownerUserId}&name=${encodeURIComponent(peerName)}` })
  } catch { uni.showToast({ title: '打开会话失败', icon: 'none' }) } finally { chatLoading.value = false }
}

function handleViewSupply(id: number) { uni.navigateTo({ url: `/pages/supply/detail?id=${id}` }) }
function handleViewRequirement(id: number) { uni.navigateTo({ url: `/pages/requirement/detail?id=${id}` }) }
</script>

<template>
  <view class="company-page">
    <WgNavBar title="企业详情" />

    <WgSkeleton v-if="loading" type="detail" />
    <WgEmpty v-else-if="!company" text="企业信息不存在" icon="empty" />

    <scroll-view v-else scroll-y class="company-scroll">
      <!-- Hero Header -->
      <view class="company-hero">
        <view class="company-hero__inner">
          <WgAvatar :name="company.companyName" size="lg" shape="square" />
          <view class="company-hero__info">
            <text class="company-hero__name">{{ company.companyName }}</text>
            <view class="company-hero__meta">
              <WgStatusChip v-if="company.companyType" :label="formatType(company.companyType)" variant="brand" size="sm" />
              <text v-if="formatLocation(company)" class="company-hero__location">
                <WgIcon name="map-pin" :size="12" color="rgba(255,255,255,0.7)" /> {{ formatLocation(company) }}
              </text>
            </view>
          </view>
        </view>
        <!-- 统计数字浮于 Hero 底部 -->
        <view class="company-hero__stats">
          <view class="company-hero__stat">
            <text class="company-hero__stat-num font-mono">{{ supplies.length }}</text>
            <text class="company-hero__stat-label">供应</text>
          </view>
          <view class="company-hero__stat-divider" />
          <view class="company-hero__stat">
            <text class="company-hero__stat-num font-mono">{{ requirements.length }}</text>
            <text class="company-hero__stat-label">采购</text>
          </view>
        </view>
      </view>

      <!-- 企业信息 -->
      <view class="section-card stitch-card stitch-fade-up">
        <view class="section-card__title">
          <text class="stitch-section-title">企业信息</text>
        </view>
        <view v-for="(row, idx) in infoRows" :key="idx" class="info-row">
          <text class="info-row__label">{{ row.label }}</text>
          <text class="info-row__value">{{ row.value }}</text>
        </view>
      </view>

      <!-- 企业简介 -->
      <view v-if="company.companyIntro" class="section-card stitch-card stitch-fade-up" style="animation-delay: .05s">
        <view class="section-card__title">
          <text class="stitch-section-title">企业简介</text>
        </view>
        <text class="intro-text">{{ company.companyIntro }}</text>
      </view>

      <!-- 资质证书 -->
      <view v-if="certificates.length > 0" class="section-card stitch-card stitch-fade-up" style="animation-delay: .1s">
        <view class="section-card__title">
          <text class="stitch-section-title">资质证书</text>
        </view>
        <scroll-view scroll-x :show-scrollbar="false" class="cert-scroll">
          <view class="cert-list">
            <view v-for="(cert, index) in certificates" :key="index" class="cert-item tap-feedback" @tap="handlePreviewCertificate(index)">
              <image class="cert-item__img" :src="cert" mode="aspectFill" />
            </view>
          </view>
        </scroll-view>
      </view>

      <!-- 厂区位置 -->
      <view v-if="showMapSection" class="section-card stitch-card stitch-fade-up" style="animation-delay: .15s">
        <view class="section-card__title">
          <text class="stitch-section-title">厂区位置</text>
        </view>
        <view v-if="hasCoords" class="map-container">
          <map class="map-view" :latitude="company.lat" :longitude="company.lng" :markers="mapMarkers" :scale="12" :show-location="false" />
        </view>
        <view v-if="fullAddress" class="map-address">
          <WgIcon name="map-pin" :size="16" :color="WARM_400" />
          <text class="map-address__text">{{ fullAddress }}</text>
        </view>
        <view class="map-nav-btn stitch-pill stitch-pill--brand" @tap="handleOpenNavigation">
          <WgIcon name="navigation" :size="16" :color="BRAND_600" />
          <text>{{ hasCoords ? '导航前往' : '复制地址' }}</text>
        </view>
      </view>

      <!-- 供应信息 -->
      <view v-if="supplies.length" class="section-card stitch-card stitch-fade-up" style="animation-delay: .2s">
        <view class="section-card__title">
          <text class="stitch-section-title">供应信息 ({{ supplies.length }})</text>
        </view>
        <view v-for="item in supplies" :key="item.id" class="list-item tap-feedback" @tap="handleViewSupply(item.id)">
          <text class="list-item__name">{{ item.categoryName || item.productName || '供应' }}</text>
          <WgIcon name="chevron-right" :size="14" :color="WARM_400" />
        </view>
      </view>

      <!-- 采购需求 -->
      <view v-if="requirements.length" class="section-card stitch-card stitch-fade-up" style="animation-delay: .25s">
        <view class="section-card__title">
          <text class="stitch-section-title">采购需求 ({{ requirements.length }})</text>
        </view>
        <view v-for="item in requirements" :key="item.id" class="list-item tap-feedback" @tap="handleViewRequirement(item.id)">
          <text class="list-item__name">{{ item.categoryName || item.productName || '采购' }}</text>
          <WgIcon name="chevron-right" :size="14" :color="WARM_400" />
        </view>
      </view>

      <view style="height: 160rpx" />
    </scroll-view>

    <!-- 底部操作 -->
    <WgActionBar>
      <button v-if="canFollow()" class="wg-btn" :class="isFollowing ? 'wg-btn--ghost' : 'wg-btn--secondary'" @tap="handleToggleFollow" style="max-width: 180rpx">
        {{ followLoading ? '...' : (isFollowing ? '已关注' : '+ 关注') }}
      </button>
      <button v-if="canChat" class="wg-btn wg-btn--primary" @tap="handleChat">
        <WgIcon name="message-circle" :size="18" :color="WHITE" />
        <text>{{ chatLoading ? '连接中...' : '在线聊天' }}</text>
      </button>
    </WgActionBar>
  </view>
</template>

<style lang="scss" scoped>
.company-page {
  min-height: 100vh;
  background: $bg-page;
}

.company-scroll {
  height: 100vh;
}

/* Hero Header */
.company-hero {
  background: linear-gradient(145deg, $brand-700 0%, $brand-500 55%, $warm-300 100%);
  padding: $spacing-lg $spacing-lg $spacing-sm;

  &__inner {
    display: flex;
    align-items: center;
    gap: $spacing-md;
    margin-bottom: $spacing-lg;
  }

  &__info { flex: 1; min-width: 0; }

  &__name {
    font-size: $font-xl;
    font-weight: 800;
    color: $text-inverse;
    display: block;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  &__meta {
    display: flex;
    align-items: center;
    gap: $spacing-sm;
    margin-top: $spacing-xs;
    flex-wrap: wrap;
  }

  &__location {
    font-size: $font-sm;
    color: rgba(255, 255, 255, 0.7);
    display: flex;
    align-items: center;
    gap: 4rpx;
  }

  &__stats {
    display: flex;
    align-items: center;
    justify-content: center;
    background: rgba(255, 255, 255, 0.15);
    backdrop-filter: blur(10px);
    border-radius: $radius-xl;
    padding: $spacing-sm 0;
  }

  &__stat {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  &__stat-num {
    font-size: $font-2xl;
    font-weight: 800;
    color: $text-inverse;
  }

  &__stat-label {
    font-size: $font-sm;
    color: rgba(255, 255, 255, 0.7);
    margin-top: 4rpx;
  }

  &__stat-divider {
    width: 1rpx;
    height: 48rpx;
    background: rgba(255, 255, 255, 0.25);
  }
}

/* Section card */
.section-card {
  margin: $spacing-sm;
  padding: $spacing-sm 0;

  &__title {
    padding: $spacing-sm $spacing-lg;
    border-bottom: 1rpx solid $border-light;
    margin-bottom: $spacing-xs;
  }
}

.info-row {
  display: flex;
  padding: $spacing-sm $spacing-lg;

  &__label {
    width: 160rpx;
    font-size: $font-md;
    color: $text-secondary;
    flex-shrink: 0;
  }

  &__value {
    flex: 1;
    font-size: $font-md;
    color: $text-primary;
    word-break: break-all;
  }
}

.intro-text {
  display: block;
  padding: $spacing-sm $spacing-lg;
  font-size: $font-md;
  color: $text-secondary;
  line-height: 1.8;
}

/* Certs */
.cert-scroll { white-space: nowrap; padding: 0 $spacing-lg; }
.cert-list { display: inline-flex; gap: $spacing-sm; padding: 4rpx 0; }
.cert-item {
  width: 200rpx;
  height: 150rpx;
  border-radius: $radius-lg;
  overflow: hidden;
  flex-shrink: 0;
  border: 1rpx solid $border-light;

  &__img { width: 200rpx; height: 150rpx; }
}

/* Map */
.map-container {
  margin: 0 $spacing-lg;
  height: 400rpx;
  border-radius: $radius-lg;
  overflow: hidden;
  margin-bottom: $spacing-sm;
}

.map-view { width: 100%; height: 400rpx; }

.map-address {
  display: flex;
  align-items: flex-start;
  gap: 8rpx;
  padding: 0 $spacing-lg;
  margin-bottom: $spacing-sm;

  &__text {
    flex: 1;
    font-size: $font-sm;
    color: $text-secondary;
    line-height: 1.5;
    word-break: break-all;
  }
}

.map-nav-btn {
  margin: 0 $spacing-lg $spacing-sm;
  justify-content: center;
}

/* List item */
.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-sm $spacing-lg;
  border-bottom: 1rpx solid $border-light;

  &:last-child { border-bottom: none; }

  &__name {
    flex: 1;
    font-size: $font-md;
    color: $text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
