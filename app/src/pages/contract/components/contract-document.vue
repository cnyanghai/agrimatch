<script setup lang="ts">
/**
 * ContractDocument - 统一合同文档展示组件（App端）
 *
 * 对标 Web 端 UnifiedContractDocument.vue，在移动端以正式合同文档风格渲染：
 * - 合同标题与编号
 * - 买方/卖方完整信息（公司名、执照号、联系人、电话、地址、银行信息）
 * - 产品明细（名称、品类、数量、单价、总额、参数）
 * - 交易条款（付款方式、交付信息、发票、包装）
 * - 法律条款（可折叠，待签署及之后显示）
 * - 签署区域（签章/签名展示）
 */
import { ref, computed } from 'vue'
import type { ContractResponse, BankInfo } from '../../../api/contract'
import { getPaymentMethodText, getDeliveryModeText, parseBankInfo } from '../../../api/contract'
import {
  CONTRACT_TERM_SECTIONS,
  LEGAL_NOTICE,
  getLegalBindingTerms,
  type ContractTermSection,
} from '../../../constants/contractTerms'

const props = withDefaults(defineProps<{
  /** 合同数据 */
  contract: ContractResponse
  /** 是否显示法律条款（status >= 1） */
  showLegalTerms?: boolean
}>(), {
  showLegalTerms: false,
})

// ==================== 计算属性 ====================

const statusLabel = computed(() => {
  const map: Record<number, string> = {
    0: '草稿',
    1: '待签署',
    2: '已签署',
    3: '履约中',
    4: '已完成',
    5: '已取消',
  }
  return map[props.contract.status] || '未知'
})

const isDraft = computed(() => props.contract.status === 0)

// 买方银行信息
const buyerBank = computed<BankInfo | null>(() => {
  return parseBankInfo(props.contract.buyerBankInfo)
})

// 卖方银行信息
const sellerBank = computed<BankInfo | null>(() => {
  return parseBankInfo(props.contract.sellerBankInfo)
})

// 产品参数
const productParams = computed(() => {
  // 优先使用结构化 productParams
  if (props.contract.productParams && props.contract.productParams.length > 0) {
    return props.contract.productParams
  }
  // 回退解析 paramsJson
  if (props.contract.paramsJson) {
    try {
      const data = JSON.parse(props.contract.paramsJson)
      const params = data?.params || data
      if (Array.isArray(params)) {
        return params.map((p: any) => ({
          label: p.label || p.name || '',
          value: String(p.value ?? ''),
        })).filter((p: any) => p.label && p.value)
      }
      if (typeof params === 'object' && params !== null) {
        const BLACKLIST = [
          'snapshotTime', 'priceType', 'id', 'categoryName', 'title',
          'productName', 'companyName', 'nickName', 'exFactoryPrice', 'expectedPrice',
          'remainingQuantity', 'unit', 'basisQuotes', 'basisPrice',
          'contractCode', 'futuresPrice', 'originPrice', 'shipAddress', 'purchaseAddress',
          'deliveryMode', 'storageMethod', 'packaging',
        ]
        return Object.entries(params)
          .filter(([k, v]) => !BLACKLIST.includes(k) && v != null && v !== '')
          .map(([k, v]) => ({ label: k, value: String(v) }))
      }
    } catch { /* ignore */ }
  }
  return []
})

// 法律条款
const legalTerms = computed<ContractTermSection[]>(() => {
  if (!props.showLegalTerms) return []
  const signingPlace = props.contract.buyerAddress || props.contract.sellerAddress
  return getLegalBindingTerms(signingPlace)
})

// 法律条款折叠控制
const legalTermsExpanded = ref(false)

// ==================== 工具函数 ====================

function formatCurrency(amount?: number | null): string {
  if (amount == null) return '-'
  return new Intl.NumberFormat('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(amount)
}

function formatDate(dateStr?: string | null): string {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
  } catch {
    return dateStr
  }
}

function formatDateTime(dateStr?: string | null): string {
  if (!dateStr) return '-'
  try {
    const d = new Date(dateStr)
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
  } catch {
    return dateStr
  }
}
</script>

<template>
  <view class="contract-doc">
    <!-- DRAFT 水印 -->
    <view v-if="isDraft" class="contract-doc__watermark">
      <text class="contract-doc__watermark-text">DRAFT</text>
    </view>

    <!-- ========== 合同头部 ========== -->
    <view class="doc-header">
      <text class="doc-header__title">购销合同</text>
      <text class="doc-header__subtitle">Purchase and Sale Contract</text>
      <view class="doc-header__meta">
        <text class="doc-header__meta-item">合同编号: {{ contract.contractNo }}</text>
        <text class="doc-header__meta-item">
          签订日期: {{ formatDate(contract.buyerSignTime || contract.sellerSignTime || contract.createTime) }}
        </text>
      </view>
      <view class="doc-header__status">
        <text class="doc-header__status-tag" :style="{ color: isDraft ? '#999' : '#2D6A4F' }">
          {{ statusLabel }}
        </text>
      </view>
    </view>

    <!-- ========== 甲方（买方）信息 ========== -->
    <view class="doc-section">
      <view class="doc-section__header doc-section__header--buyer">
        <text class="doc-section__header-text">甲方（买方） The Buyer</text>
      </view>
      <view class="doc-party">
        <view class="doc-party__row">
          <text class="doc-party__label">公司名称:</text>
          <text class="doc-party__value doc-party__value--bold">{{ contract.buyerCompanyName || '-' }}</text>
        </view>
        <view v-if="contract.buyerLicenseNo" class="doc-party__row">
          <text class="doc-party__label">统一信用码:</text>
          <text class="doc-party__value doc-party__value--mono">{{ contract.buyerLicenseNo }}</text>
        </view>
        <view v-if="contract.buyerContacts" class="doc-party__row">
          <text class="doc-party__label">联系人:</text>
          <text class="doc-party__value">{{ contract.buyerContacts }}</text>
        </view>
        <view v-if="contract.buyerPhone" class="doc-party__row">
          <text class="doc-party__label">电话:</text>
          <text class="doc-party__value">{{ contract.buyerPhone }}</text>
        </view>
        <view v-if="contract.buyerAddress" class="doc-party__row">
          <text class="doc-party__label">地址:</text>
          <text class="doc-party__value">{{ contract.buyerAddress }}</text>
        </view>
        <!-- 银行信息 -->
        <view v-if="buyerBank" class="doc-party__bank">
          <text class="doc-party__bank-title">银行账户</text>
          <view v-if="buyerBank.bankName" class="doc-party__row">
            <text class="doc-party__label">开户行:</text>
            <text class="doc-party__value">{{ buyerBank.bankName }}</text>
          </view>
          <view v-if="buyerBank.accountName" class="doc-party__row">
            <text class="doc-party__label">户名:</text>
            <text class="doc-party__value">{{ buyerBank.accountName }}</text>
          </view>
          <view v-if="buyerBank.accountNo" class="doc-party__row">
            <text class="doc-party__label">账号:</text>
            <text class="doc-party__value doc-party__value--mono">{{ buyerBank.accountNo }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- ========== 乙方（卖方）信息 ========== -->
    <view class="doc-section">
      <view class="doc-section__header doc-section__header--seller">
        <text class="doc-section__header-text">乙方（卖方） The Seller</text>
      </view>
      <view class="doc-party">
        <view class="doc-party__row">
          <text class="doc-party__label">公司名称:</text>
          <text class="doc-party__value doc-party__value--bold">{{ contract.sellerCompanyName || '-' }}</text>
        </view>
        <view v-if="contract.sellerLicenseNo" class="doc-party__row">
          <text class="doc-party__label">统一信用码:</text>
          <text class="doc-party__value doc-party__value--mono">{{ contract.sellerLicenseNo }}</text>
        </view>
        <view v-if="contract.sellerContacts" class="doc-party__row">
          <text class="doc-party__label">联系人:</text>
          <text class="doc-party__value">{{ contract.sellerContacts }}</text>
        </view>
        <view v-if="contract.sellerPhone" class="doc-party__row">
          <text class="doc-party__label">电话:</text>
          <text class="doc-party__value">{{ contract.sellerPhone }}</text>
        </view>
        <view v-if="contract.sellerAddress" class="doc-party__row">
          <text class="doc-party__label">地址:</text>
          <text class="doc-party__value">{{ contract.sellerAddress }}</text>
        </view>
        <!-- 银行信息 -->
        <view v-if="sellerBank" class="doc-party__bank">
          <text class="doc-party__bank-title">银行账户</text>
          <view v-if="sellerBank.bankName" class="doc-party__row">
            <text class="doc-party__label">开户行:</text>
            <text class="doc-party__value">{{ sellerBank.bankName }}</text>
          </view>
          <view v-if="sellerBank.accountName" class="doc-party__row">
            <text class="doc-party__label">户名:</text>
            <text class="doc-party__value">{{ sellerBank.accountName }}</text>
          </view>
          <view v-if="sellerBank.accountNo" class="doc-party__row">
            <text class="doc-party__label">账号:</text>
            <text class="doc-party__value doc-party__value--mono">{{ sellerBank.accountNo }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- ========== 一、产品明细与价格 ========== -->
    <view class="doc-section">
      <view class="doc-section__header doc-section__header--product">
        <text class="doc-section__header-text">一、产品明细与价格</text>
      </view>

      <view class="doc-product-table">
        <view class="doc-product-table__row doc-product-table__row--header">
          <text class="doc-product-table__cell doc-product-table__cell--name">产品名称</text>
          <text class="doc-product-table__cell doc-product-table__cell--qty">数量</text>
          <text class="doc-product-table__cell doc-product-table__cell--price">单价(元)</text>
          <text class="doc-product-table__cell doc-product-table__cell--total">金额(元)</text>
        </view>
        <view class="doc-product-table__row">
          <view class="doc-product-table__cell doc-product-table__cell--name">
            <text class="doc-product-table__product-name">{{ contract.productName || '-' }}</text>
            <text v-if="contract.categoryName" class="doc-product-table__product-cat">{{ contract.categoryName }}</text>
          </view>
          <text class="doc-product-table__cell doc-product-table__cell--qty doc-product-table__cell--mono">
            {{ formatCurrency(contract.quantity) }} {{ contract.unit || '吨' }}
          </text>
          <text class="doc-product-table__cell doc-product-table__cell--price doc-product-table__cell--mono">
            {{ formatCurrency(contract.unitPrice) }}
          </text>
          <text class="doc-product-table__cell doc-product-table__cell--total doc-product-table__cell--mono doc-product-table__cell--bold">
            {{ formatCurrency(contract.totalAmount) }}
          </text>
        </view>
        <view class="doc-product-table__row doc-product-table__row--footer">
          <text class="doc-product-table__footer-label">合计 (Total Amount):</text>
          <text class="doc-product-table__footer-value">{{ formatCurrency(contract.totalAmount) }}</text>
        </view>
      </view>

      <!-- 产品参数 -->
      <view v-if="productParams.length > 0" class="doc-params">
        <text class="doc-params__title">产品参数</text>
        <view class="doc-params__grid">
          <view
            v-for="(param, idx) in productParams"
            :key="idx"
            class="doc-params__item"
          >
            <text class="doc-params__item-label">{{ param.label }}</text>
            <text class="doc-params__item-value">{{ param.value }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- ========== 二、付款方式 ========== -->
    <view v-if="contract.paymentMethod" class="doc-section">
      <view class="doc-section__header doc-section__header--terms">
        <text class="doc-section__header-text">二、付款方式</text>
      </view>
      <view class="doc-text-block">
        <text class="doc-text-block__content">{{ getPaymentMethodText(contract.paymentMethod) }}</text>
      </view>
    </view>

    <!-- ========== 发票与包装 ========== -->
    <view v-if="contract.invoiceType || contract.packaging" class="doc-section">
      <view class="doc-section__header doc-section__header--terms">
        <text class="doc-section__header-text">发票与包装</text>
      </view>
      <view class="doc-text-block">
        <view v-if="contract.invoiceType" class="doc-text-block__row">
          <text class="doc-text-block__label">发票类型:</text>
          <text class="doc-text-block__value">{{ contract.invoiceType }}</text>
        </view>
        <view v-if="contract.packaging" class="doc-text-block__row">
          <text class="doc-text-block__label">包装要求:</text>
          <text class="doc-text-block__value">{{ contract.packaging }}</text>
        </view>
      </view>
    </view>

    <!-- ========== 三、交货与验收 ========== -->
    <view class="doc-section">
      <view class="doc-section__header doc-section__header--terms">
        <text class="doc-section__header-text">三、交货与验收</text>
      </view>
      <view class="doc-text-block">
        <view v-if="contract.deliveryAddress" class="doc-text-block__row">
          <text class="doc-text-block__label">交货地点:</text>
          <text class="doc-text-block__value">{{ contract.deliveryAddress }}</text>
        </view>
        <view v-if="contract.deliveryDate" class="doc-text-block__row">
          <text class="doc-text-block__label">交货期限:</text>
          <text class="doc-text-block__value">{{ formatDate(contract.deliveryDate) }} 前完成交付</text>
        </view>
        <view v-if="contract.deliveryMode" class="doc-text-block__row">
          <text class="doc-text-block__label">交货方式:</text>
          <text class="doc-text-block__value">{{ getDeliveryModeText(contract.deliveryMode) }}</text>
        </view>
      </view>
    </view>

    <!-- ========== 法律条款（可折叠） ========== -->
    <view v-if="showLegalTerms && legalTerms.length > 0" class="doc-section">
      <view class="doc-legal-toggle" @tap="legalTermsExpanded = !legalTermsExpanded">
        <text class="doc-legal-toggle__text">法律约束条款</text>
        <text class="doc-legal-toggle__arrow">{{ legalTermsExpanded ? '收起' : '展开查看' }}</text>
      </view>

      <view v-if="legalTermsExpanded" class="doc-legal-terms">
        <view
          v-for="term in legalTerms"
          :key="term.number"
          class="doc-legal-terms__section"
        >
          <view class="doc-legal-terms__header">
            <text class="doc-legal-terms__header-text">
              {{ term.number }}、{{ term.titleCn }} ({{ term.titleEn }})
            </text>
          </view>
          <text class="doc-legal-terms__content">{{ term.content }}</text>
        </view>

        <!-- 法律提示 -->
        <view class="doc-legal-notice">
          <text class="doc-legal-notice__icon">!</text>
          <view class="doc-legal-notice__body">
            <text class="doc-legal-notice__title">法律提示</text>
            <text class="doc-legal-notice__text">{{ LEGAL_NOTICE }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- ========== 备注 ========== -->
    <view v-if="contract.remark" class="doc-section">
      <view class="doc-section__header doc-section__header--remark">
        <text class="doc-section__header-text">{{ showLegalTerms ? '九' : '四' }}、备注</text>
      </view>
      <view class="doc-text-block">
        <text class="doc-text-block__content">{{ contract.remark }}</text>
      </view>
    </view>

    <!-- ========== 签署区域 ========== -->
    <view class="doc-sign-area">
      <view class="doc-sign-area__divider" />

      <view class="doc-sign-area__parties">
        <!-- 买方签署 -->
        <view class="doc-sign-party">
          <text class="doc-sign-party__title">甲方（买方）签章</text>
          <view
            class="doc-sign-party__box"
            :class="contract.buyerSigned ? 'doc-sign-party__box--signed' : 'doc-sign-party__box--pending'"
          >
            <template v-if="contract.buyerSigned">
              <image
                v-if="contract.buyerSealUrl"
                :src="contract.buyerSealUrl"
                class="doc-sign-party__seal-img"
                mode="aspectFit"
              />
              <view v-else class="doc-sign-party__seal-text">
                <text class="doc-sign-party__seal-text-inner">
                  {{ (contract.buyerCompanyName || '').slice(0, 4) }}
                </text>
                <text class="doc-sign-party__seal-text-sub">合同章</text>
              </view>
              <text class="doc-sign-party__time">
                签署时间: {{ formatDateTime(contract.buyerSignTime) }}
              </text>
            </template>
            <template v-else>
              <text class="doc-sign-party__pending-icon">...</text>
              <text class="doc-sign-party__pending-text">待签署</text>
            </template>
          </view>
          <text class="doc-sign-party__name">{{ contract.buyerCompanyName || '-' }}</text>
        </view>

        <!-- 卖方签署 -->
        <view class="doc-sign-party">
          <text class="doc-sign-party__title">乙方（卖方）签章</text>
          <view
            class="doc-sign-party__box"
            :class="contract.sellerSigned ? 'doc-sign-party__box--signed' : 'doc-sign-party__box--pending'"
          >
            <template v-if="contract.sellerSigned">
              <image
                v-if="contract.sellerSealUrl"
                :src="contract.sellerSealUrl"
                class="doc-sign-party__seal-img"
                mode="aspectFit"
              />
              <view v-else class="doc-sign-party__seal-text">
                <text class="doc-sign-party__seal-text-inner">
                  {{ (contract.sellerCompanyName || '').slice(0, 4) }}
                </text>
                <text class="doc-sign-party__seal-text-sub">合同章</text>
              </view>
              <text class="doc-sign-party__time">
                签署时间: {{ formatDateTime(contract.sellerSignTime) }}
              </text>
            </template>
            <template v-else>
              <text class="doc-sign-party__pending-icon">...</text>
              <text class="doc-sign-party__pending-text">待签署</text>
            </template>
          </view>
          <text class="doc-sign-party__name">{{ contract.sellerCompanyName || '-' }}</text>
        </view>
      </view>

      <!-- 双方签署完成提示 -->
      <view v-if="contract.buyerSigned && contract.sellerSigned" class="doc-sign-complete">
        <text class="doc-sign-complete__text">合同已签署完成，双方已完成电子签章，合同正式生效</text>
      </view>
    </view>

    <!-- ========== 防伪验证码（已签署及之后的状态显示） ========== -->
    <view v-if="contract.pdfHash && contract.status >= 2" class="doc-antiforgery">
      <view class="doc-antiforgery__divider" />
      <text class="doc-antiforgery__label">防伪验证码:</text>
      <text class="doc-antiforgery__hash">{{ contract.pdfHash }}</text>
      <text class="doc-antiforgery__hint">此哈希值基于 SHA-256 算法生成，可用于验证合同 PDF 文件的完整性和真实性</text>
    </view>
  </view>
</template>

<style lang="scss" scoped>
.contract-doc {
  background: #ffffff;
  border-radius: $radius-lg;
  margin: 0 $spacing-sm;
  padding: $spacing-lg $spacing-md;
  position: relative;
  overflow: hidden;
  box-shadow: $shadow-warm-elevated;
}

/* ===== 水印 ===== */
.contract-doc__watermark {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  pointer-events: none;
  z-index: 1;
}

.contract-doc__watermark-text {
  font-size: 160rpx;
  font-weight: bold;
  color: rgba(0, 0, 0, 0.04);
  transform: rotate(-30deg);
  letter-spacing: 20rpx;
}

/* ===== 合同头部 ===== */
.doc-header {
  text-align: center;
  padding-bottom: $spacing-lg;
  border-bottom: 4rpx solid $text-primary;
  margin-bottom: $spacing-lg;
  position: relative;
  z-index: 2;

  &__title {
    font-size: 48rpx;
    font-weight: bold;
    color: $text-primary;
    letter-spacing: 8rpx;
    display: block;
    font-family: serif;
  }

  &__subtitle {
    font-size: $font-sm;
    color: $text-secondary;
    display: block;
    margin-top: $spacing-xs;
  }

  &__meta {
    display: flex;
    justify-content: space-between;
    margin-top: $spacing-md;
  }

  &__meta-item {
    font-size: $font-sm;
    color: $text-secondary;
  }

  &__status {
    margin-top: $spacing-sm;
  }

  &__status-tag {
    font-size: $font-xs;
    font-weight: bold;
    padding: 4rpx 20rpx;
    border-radius: $radius-pill;
    background: $bg-page;
  }
}

/* ===== 文档分区 ===== */
.doc-section {
  margin-bottom: $spacing-lg;
  position: relative;
  z-index: 2;

  &__header {
    padding: $spacing-xs $spacing-sm;
    margin-bottom: $spacing-sm;
    border-left: 6rpx solid $brand-600;
    background: $brand-50;

    &--buyer {
      border-left-color: $autumn-400;
      background: $autumn-50;
    }

    &--seller {
      border-left-color: $brand-600;
      background: $brand-50;
    }

    &--product {
      border-left-color: $brand-600;
      background: $brand-50;
    }

    &--terms {
      border-left-color: $brand-600;
      background: $brand-50;
    }

    &--remark {
      border-left-color: $warm-400;
      background: $warm-50;
    }
  }

  &__header-text {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
  }
}

/* ===== 买卖方信息 ===== */
.doc-party {
  padding-left: $spacing-sm;

  &__row {
    display: flex;
    padding: 6rpx 0;
  }

  &__label {
    width: 150rpx;
    flex-shrink: 0;
    font-size: $font-sm;
    color: $text-secondary;
  }

  &__value {
    flex: 1;
    font-size: $font-sm;
    color: $text-primary;

    &--bold {
      font-weight: bold;
    }

    &--mono {
      font-family: monospace;
      letter-spacing: 2rpx;
    }
  }

  &__bank {
    margin-top: $spacing-sm;
    padding: $spacing-sm;
    background: $bg-page;
    border-radius: $radius-md;
  }

  &__bank-title {
    font-size: $font-xs;
    color: $text-secondary;
    font-weight: 600;
    display: block;
    margin-bottom: $spacing-xs;
  }
}

/* ===== 产品明细表格 ===== */
.doc-product-table {
  border: 2rpx solid $border-color;
  border-radius: $radius-md;
  overflow: hidden;

  &__row {
    display: flex;
    align-items: center;
    border-bottom: 2rpx solid $border-light;

    &:last-child {
      border-bottom: none;
    }

    &--header {
      background: $bg-page;
    }

    &--footer {
      background: $bg-page;
      justify-content: flex-end;
      padding: $spacing-sm $spacing-md;
    }
  }

  &__cell {
    padding: $spacing-xs $spacing-sm;
    font-size: $font-sm;
    color: $text-primary;

    &--name {
      flex: 2;
    }

    &--qty,
    &--price {
      flex: 1.2;
      text-align: right;
    }

    &--total {
      flex: 1.5;
      text-align: right;
    }

    &--mono {
      font-family: monospace;
    }

    &--bold {
      font-weight: bold;
    }
  }

  &__product-name {
    font-size: $font-sm;
    font-weight: 600;
    color: $text-primary;
    display: block;
  }

  &__product-cat {
    font-size: $font-xs;
    color: $text-secondary;
    display: block;
    margin-top: 2rpx;
  }

  &__footer-label {
    font-size: $font-sm;
    font-weight: bold;
    color: $text-primary;
    margin-right: $spacing-sm;
  }

  &__footer-value {
    font-size: $font-lg;
    font-weight: bold;
    color: $brand-600;
    font-family: monospace;
  }
}

/* ===== 产品参数 ===== */
.doc-params {
  margin-top: $spacing-md;

  &__title {
    font-size: $font-xs;
    font-weight: bold;
    color: $text-secondary;
    text-transform: uppercase;
    letter-spacing: 4rpx;
    display: block;
    margin-bottom: $spacing-xs;
  }

  &__grid {
    display: flex;
    flex-wrap: wrap;
    gap: $spacing-sm;
  }

  &__item {
    padding: $spacing-xs $spacing-sm;
    background: $bg-page;
    border-radius: $radius-md;
    min-width: 180rpx;
  }

  &__item-label {
    font-size: $font-xs;
    color: $text-secondary;
    display: block;
  }

  &__item-value {
    font-size: $font-sm;
    font-weight: 600;
    color: $text-primary;
    display: block;
    margin-top: 2rpx;
  }
}

/* ===== 文本块（条款展示） ===== */
.doc-text-block {
  padding-left: $spacing-sm;

  &__content {
    font-size: $font-sm;
    color: $text-primary;
    line-height: 1.8;
  }

  &__row {
    display: flex;
    padding: 4rpx 0;
  }

  &__label {
    font-size: $font-sm;
    font-weight: 600;
    color: $text-primary;
    flex-shrink: 0;
    margin-right: $spacing-xs;
  }

  &__value {
    font-size: $font-sm;
    color: $text-primary;
    flex: 1;
  }
}

/* ===== 法律条款（折叠） ===== */
.doc-legal-toggle {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: $spacing-sm $spacing-md;
  background: $warm-50;
  border-radius: $radius-md;
  border: 2rpx solid $border-color;

  &__text {
    font-size: $font-md;
    font-weight: bold;
    color: $text-primary;
  }

  &__arrow {
    font-size: $font-sm;
    color: $brand-600;
    font-weight: 600;
  }
}

.doc-legal-terms {
  margin-top: $spacing-sm;

  &__section {
    margin-bottom: $spacing-md;
  }

  &__header {
    padding: $spacing-xs $spacing-sm;
    background: $warm-50;
    border-left: 6rpx solid $warm-400;
    margin-bottom: $spacing-xs;
  }

  &__header-text {
    font-size: $font-sm;
    font-weight: bold;
    color: $text-primary;
  }

  &__content {
    font-size: $font-sm;
    color: $text-primary;
    line-height: 1.8;
    padding-left: $spacing-sm;
    white-space: pre-line;
  }
}

.doc-legal-notice {
  display: flex;
  padding: $spacing-md;
  background: rgba($color-warning, 0.08);
  border-radius: $radius-md;
  border: 2rpx solid rgba($color-warning, 0.2);
  margin-top: $spacing-md;

  &__icon {
    width: 40rpx;
    height: 40rpx;
    border-radius: 50%;
    background: $color-warning;
    color: #fff;
    font-size: $font-md;
    font-weight: bold;
    text-align: center;
    line-height: 40rpx;
    flex-shrink: 0;
    margin-right: $spacing-sm;
  }

  &__body {
    flex: 1;
  }

  &__title {
    font-size: $font-sm;
    font-weight: bold;
    color: $color-warning;
    display: block;
  }

  &__text {
    font-size: $font-xs;
    color: $text-secondary;
    line-height: 1.6;
    margin-top: 4rpx;
  }
}

/* ===== 签署区域 ===== */
.doc-sign-area {
  position: relative;
  z-index: 2;
  margin-top: $spacing-xl;

  &__divider {
    height: 4rpx;
    background: $border-color;
    margin-bottom: $spacing-lg;
  }

  &__parties {
    display: flex;
    gap: $spacing-md;
  }
}

.doc-sign-party {
  flex: 1;
  text-align: center;

  &__title {
    font-size: $font-sm;
    font-weight: bold;
    color: $text-primary;
    display: block;
    margin-bottom: $spacing-sm;
  }

  &__box {
    height: 200rpx;
    border-radius: $radius-md;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;

    &--signed {
      background: rgba($brand-600, 0.04);
    }

    &--pending {
      background: $bg-page;
      border: 2rpx dashed $border-color;
    }
  }

  &__seal-img {
    width: 160rpx;
    height: 160rpx;
  }

  &__seal-text {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    border: 3rpx solid #c53030;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    opacity: 0.7;
    transform: rotate(-8deg);
  }

  &__seal-text-inner {
    font-size: $font-xs;
    font-weight: bold;
    color: #c53030;
    text-align: center;
  }

  &__seal-text-sub {
    font-size: 18rpx;
    color: #c53030;
    text-align: center;
  }

  &__time {
    font-size: $font-xs;
    color: $text-secondary;
    margin-top: $spacing-xs;
  }

  &__pending-icon {
    font-size: 48rpx;
    color: $text-placeholder;
    margin-bottom: $spacing-xs;
  }

  &__pending-text {
    font-size: $font-sm;
    color: $text-placeholder;
  }

  &__name {
    font-size: $font-xs;
    color: $text-secondary;
    display: block;
    margin-top: $spacing-xs;
  }
}

.doc-sign-complete {
  margin-top: $spacing-md;
  padding: $spacing-md;
  background: $brand-50;
  border-radius: $radius-md;
  border: 2rpx solid rgba($brand-600, 0.2);
  text-align: center;

  &__text {
    font-size: $font-sm;
    color: $brand-600;
    font-weight: 600;
  }
}

/* ===== 防伪验证码 ===== */
.doc-antiforgery {
  margin-top: $spacing-lg;
  padding-top: $spacing-md;
  position: relative;
  z-index: 2;

  &__divider {
    height: 2rpx;
    background: $border-light;
    margin-bottom: $spacing-md;
  }

  &__label {
    font-size: $font-xs;
    color: $text-placeholder;
    display: block;
    margin-bottom: $spacing-xs;
  }

  &__hash {
    font-size: 20rpx;
    font-family: monospace;
    color: $text-secondary;
    background: $bg-page;
    padding: $spacing-xs $spacing-sm;
    border-radius: $radius-md;
    display: block;
    word-break: break-all;
    line-height: 1.6;
  }

  &__hint {
    font-size: 20rpx;
    color: $text-placeholder;
    display: block;
    margin-top: $spacing-xs;
    line-height: 1.4;
  }
}
</style>
