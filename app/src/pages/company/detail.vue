<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import {
  getCompanyProfile,
  companyTypeMap,
  type CompanyResponse,
} from '../../api/company'
import { useFollow } from '../../composables/useFollow'

const company = ref<CompanyResponse | null>(null)
const supplies = ref<any[]>([])
const requirements = ref<any[]>([])
const loading = ref(true)

const { isFollowing, followLoading, loadFollowStatus, handleToggleFollow, canFollow }
  = useFollow(() => company.value?.ownerUserId)

onLoad(async (options) => {
  if (options?.id) {
    try {
      const res = await getCompanyProfile(Number(options.id))
      company.value = res.company
      supplies.value = res.supplies ?? []
      requirements.value = res.requirements ?? []
      await loadFollowStatus()
    } catch {
      // handled by request.ts
    } finally {
      loading.value = false
    }
  }
})

/** Build location string from province/city/district */
function formatLocation(c: CompanyResponse): string {
  return [c.province, c.city, c.district].filter(Boolean).join(' ')
}

/** Format company type code to Chinese label */
function formatType(type?: string): string {
  if (!type) return ''
  return companyTypeMap[type] ?? type
}

/** Make a phone call */
function handleCall() {
  if (!company.value?.phone) {
    uni.showToast({ title: '暂无联系电话', icon: 'none' })
    return
  }
  uni.makePhoneCall({
    phoneNumber: company.value.phone,
    fail: () => {
      // user cancelled or not supported
    },
  })
}

/** Navigate to the full address on the map (future) */
function handleViewSupply(id: number) {
  uni.navigateTo({ url: `/pages/supply/detail?id=${id}` })
}

function handleViewRequirement(id: number) {
  uni.navigateTo({ url: `/pages/requirement/detail?id=${id}` })
}
</script>

<template>
  <view class="company-page">
    <!-- Loading -->
    <WgSkeleton v-if="loading" type="detail" />

    <!-- Not found -->
    <WgEmpty v-else-if="!company" text="企业信息不存在" icon="empty" />

    <template v-else>
      <!-- Header card -->
      <view class="company-header">
        <view class="company-header__icon">
          <text class="company-header__icon-text">
            {{ company.companyName?.charAt(0) ?? '企' }}
          </text>
        </view>
        <view class="company-header__info">
          <text class="company-header__name">{{ company.companyName }}</text>
          <view class="company-header__meta">
            <text
              v-if="company.companyType"
              class="company-header__tag"
            >
              {{ formatType(company.companyType) }}
            </text>
            <text
              v-if="formatLocation(company)"
              class="company-header__location"
            >
              {{ formatLocation(company) }}
            </text>
          </view>
        </view>
      </view>

      <!-- Statistics -->
      <view class="stat-bar">
        <view class="stat-bar__item">
          <text class="stat-bar__num">{{ supplies.length }}</text>
          <text class="stat-bar__label">供应</text>
        </view>
        <view class="stat-bar__divider" />
        <view class="stat-bar__item">
          <text class="stat-bar__num">{{ requirements.length }}</text>
          <text class="stat-bar__label">采购</text>
        </view>
      </view>

      <!-- Company details -->
      <view class="detail-card">
        <text class="detail-card__title">企业信息</text>

        <view v-if="company.legalPerson" class="detail-row">
          <text class="detail-row__label">法人代表</text>
          <text class="detail-row__value">{{ company.legalPerson }}</text>
        </view>
        <view v-if="company.registeredCapital" class="detail-row">
          <text class="detail-row__label">注册资本</text>
          <text class="detail-row__value">{{ company.registeredCapital }}</text>
        </view>
        <view v-if="company.scale" class="detail-row">
          <text class="detail-row__label">企业规模</text>
          <text class="detail-row__value">{{ company.scale }}</text>
        </view>
        <view v-if="company.licenseNo" class="detail-row">
          <text class="detail-row__label">营业执照</text>
          <text class="detail-row__value">{{ company.licenseNo }}</text>
        </view>
        <view v-if="company.businessScope" class="detail-row">
          <text class="detail-row__label">经营范围</text>
          <text class="detail-row__value">{{ company.businessScope }}</text>
        </view>
        <view v-if="company.address" class="detail-row">
          <text class="detail-row__label">详细地址</text>
          <text class="detail-row__value">{{ company.address }}</text>
        </view>
        <view v-if="company.contacts" class="detail-row">
          <text class="detail-row__label">联系人</text>
          <text class="detail-row__value">{{ company.contacts }}</text>
        </view>
        <view v-if="company.phone" class="detail-row">
          <text class="detail-row__label">联系电话</text>
          <text class="detail-row__value detail-row__value--phone">{{ company.phone }}</text>
        </view>
      </view>

      <!-- Company intro -->
      <view v-if="company.companyIntro" class="detail-card">
        <text class="detail-card__title">企业简介</text>
        <text class="detail-card__intro">{{ company.companyIntro }}</text>
      </view>

      <!-- Supply list -->
      <view v-if="supplies.length" class="detail-card">
        <text class="detail-card__title">供应信息 ({{ supplies.length }})</text>
        <view
          v-for="item in supplies"
          :key="item.id"
          class="list-item"
          @tap="handleViewSupply(item.id)"
        >
          <text class="list-item__name">{{ item.categoryName || item.productName || '供应' }}</text>
          <uni-icons type="right" size="14" color="#999" />
        </view>
      </view>

      <!-- Requirement list -->
      <view v-if="requirements.length" class="detail-card">
        <text class="detail-card__title">采购需求 ({{ requirements.length }})</text>
        <view
          v-for="item in requirements"
          :key="item.id"
          class="list-item"
          @tap="handleViewRequirement(item.id)"
        >
          <text class="list-item__name">{{ item.categoryName || item.productName || '采购' }}</text>
          <uni-icons type="right" size="14" color="#999" />
        </view>
      </view>

      <!-- Bottom bar -->
      <view class="bottom-bar safe-area-bottom">
        <view
          v-if="canFollow()"
          class="bottom-bar__btn bottom-bar__btn--follow"
          :class="{ 'bottom-bar__btn--followed': isFollowing }"
          @tap="handleToggleFollow"
        >
          <text>{{ followLoading ? '...' : (isFollowing ? '已关注' : '+ 关注') }}</text>
        </view>
        <button class="bottom-bar__btn bottom-bar__btn--primary" @tap="handleCall">
          <uni-icons type="phone" size="18" color="#fff" />
          <text>{{ company.phone ? '电话联系' : '暂无电话' }}</text>
        </button>
      </view>
    </template>
  </view>
</template>

<style lang="scss" scoped>
.company-page {
  min-height: 100vh;
  background: $bg-page;
  padding-bottom: 140rpx;

}

/* ===== Header card ===== */
.company-header {
  display: flex;
  align-items: center;
  gap: $spacing-md;
  background: $bg-card;
  margin: $spacing-sm;
  border-radius: $radius-lg;
  padding: $spacing-lg;

  &__icon {
    width: 100rpx;
    height: 100rpx;
    border-radius: $radius-md;
    background: $brand-100;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  &__icon-text {
    font-size: $font-2xl;
    font-weight: bold;
    color: $brand-600;
  }

  &__info {
    flex: 1;
    min-width: 0;
  }

  &__name {
    font-size: $font-xl;
    font-weight: bold;
    color: $text-primary;
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

  &__tag {
    font-size: $font-xs;
    color: $brand-600;
    background: $brand-50;
    padding: 4rpx 16rpx;
    border-radius: $radius-sm;
  }

  &__location {
    font-size: $font-sm;
    color: $text-secondary;
  }
}

/* ===== Statistics bar ===== */
.stat-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  background: $bg-card;
  margin: 0 $spacing-sm;
  border-radius: $radius-lg;
  padding: $spacing-md 0;
  margin-top: $spacing-sm;

  &__item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
  }

  &__num {
    font-size: $font-2xl;
    font-weight: bold;
    color: $brand-600;
  }

  &__label {
    font-size: $font-sm;
    color: $text-secondary;
    margin-top: 4rpx;
  }

  &__divider {
    width: 1rpx;
    height: 60rpx;
    background: $border-light;
  }
}

/* ===== Detail card ===== */
.detail-card {
  background: $bg-card;
  margin: $spacing-sm;
  border-radius: $radius-lg;
  padding: $spacing-lg;

  &__title {
    font-size: $font-lg;
    font-weight: bold;
    color: $text-primary;
    display: block;
    margin-bottom: $spacing-md;
  }

  &__intro {
    font-size: $font-md;
    color: $text-secondary;
    line-height: 1.8;
  }
}

/* ===== Detail row ===== */
.detail-row {
  display: flex;
  padding: $spacing-xs 0;

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

    &--phone {
      color: $action-600;
    }
  }
}

/* ===== List item (supply/requirement) ===== */
.list-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-sm 0;
  border-bottom: 1rpx solid $border-light;

  &:last-child {
    border-bottom: none;
  }

  &__name {
    flex: 1;
    font-size: $font-md;
    color: $text-primary;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

}

/* ===== Bottom bar ===== */
.bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  display: flex;
  gap: $spacing-sm;
  padding: $spacing-sm $spacing-md;
  background: $bg-card;
  border-top: 1rpx solid $border-light;

  &__btn {
    flex: 1;
    height: 88rpx;
    line-height: 88rpx;
    border-radius: $radius-lg;
    font-size: $font-md;
    font-weight: bold;
    text-align: center;
    border: none;

    &--follow {
      background: $brand-600;
      color: #ffffff;
      display: flex;
      align-items: center;
      justify-content: center;

      &:active {
        transform: scale(0.95);
      }
    }

    &--followed {
      background: $bg-page;
      border: 1rpx solid $border-color;
      color: $text-secondary;
    }

    &--primary {
      background: $brand-600;
      color: #ffffff;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: $spacing-xs;

      &:active {
        transform: scale(0.95);
      }
    }
  }
}
</style>
