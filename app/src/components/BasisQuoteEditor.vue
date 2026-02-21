<template>
  <view class="basis-quote-editor">
    <view class="section-header">
      <text class="section-title">基差报价设置</text>
      <text class="section-desc">设置期货合约和基差价格</text>
    </view>

    <!-- 基差报价列表 -->
    <view class="quotes-list">
      <view
        v-for="(quote, index) in quotes"
        :key="index"
        class="quote-item"
      >
        <!-- 序号和删除按钮 -->
        <view class="quote-item__header">
          <text class="quote-item__number">{{ index + 1 }}</text>
          <view v-if="quotes.length > 1" class="quote-item__remove" @tap="removeQuote(index)">
            <text>删除</text>
          </view>
        </view>

        <!-- 期货合约选择 -->
        <view class="quote-item__field">
          <text class="field-label">期货合约</text>
          <picker
            :value="quote.contractIndex"
            :range="contractOptions"
            range-key="label"
            @change="onContractChange(index, $event)"
            class="field-picker"
          >
            <view class="picker-display">
              {{ quote.contractIndex !== undefined && quote.contractIndex >= 0 ? contractOptions[quote.contractIndex].label : '请选择期货合约' }}
            </view>
          </picker>
        </view>

        <!-- 基差价格 -->
        <view class="quote-item__field">
          <text class="field-label">基差价格</text>
          <view class="price-input">
            <text class="price-sign">¥</text>
            <input
              type="digit"
              v-model="quote.basisPrice"
              placeholder="请输入基差价格"
              class="price-input__input"
            />
            <text class="price-unit">/吨</text>
          </view>
          <text class="price-desc">正数为升水，负数为贴水</text>
        </view>

        <!-- 可售数量 -->
        <view class="quote-item__field">
          <text class="field-label">可售数量</text>
          <view class="quantity-input">
            <input
              type="digit"
              v-model="quote.availableQty"
              placeholder="请输入可售数量"
              class="quantity-input__input"
            />
            <text class="quantity-unit">吨</text>
          </view>
        </view>

        <!-- 参考价格计算 -->
        <view v-if="quote.referencePrice !== null && quote.referencePrice !== undefined" class="quote-item__reference">
          <text class="reference-label">参考价格</text>
          <text class="reference-value">¥{{ (quote.referencePrice || 0).toFixed(2) }}</text>
          <text class="reference-desc">（期货价 + 基差）</text>
        </view>
      </view>
    </view>

    <!-- 添加按钮 -->
    <view class="add-button" @tap="addQuote">
      <text class="add-button__text">+ 添加基差报价</text>
    </view>

    <!-- 基差报价说明 -->
    <view class="tips-section">
      <view class="tips-item">
        <text class="tips-icon">💡</text>
        <text class="tips-text">基差报价适用于大宗商品交易，基差价格为相对期货合约的升贴水</text>
      </view>
      <view class="tips-item">
        <text class="tips-icon">📊</text>
        <text class="tips-text">参考价格 = 期货最新价 + 基差价格</text>
      </view>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { listFuturesContracts, type FuturesContractResponse } from '../api/futures'

export interface BasisQuoteRequest {
  contractCode: string
  contractName: string
  basisPrice: number
  availableQty: number
  referencePrice?: number | null
  contractIndex?: number
}

const props = defineProps<{
  modelValue?: BasisQuoteRequest[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: BasisQuoteRequest[]): void
}>()

const contracts = ref<FuturesContractResponse[]>([])
const quotes = ref<BasisQuoteRequest[]>([])

// 期货合约选项
const contractOptions = computed(() => {
  return contracts.value.map(c => ({
    value: c.contractCode,
    label: `${c.contractName} (${c.contractCode})`
  }))
})

// 初始化数据
watch(() => props.modelValue, (newValue) => {
  if (newValue && newValue.length > 0) {
    quotes.value = newValue.map((q, index) => ({
      ...q,
      contractIndex: contracts.value.findIndex(c => c.contractCode === q.contractCode)
    }))
  } else {
    quotes.value = [{ contractCode: '', contractName: '', basisPrice: 0, availableQty: 1, contractIndex: -1 }]
  }
}, { immediate: true, deep: true })

// 监听变化并发送
watch(quotes, (newQuotes) => {
  emit('update:modelValue', newQuotes)
}, { deep: true })

// 加载期货合约
async function loadFuturesContracts() {
  try {
    const result = await listFuturesContracts()
    contracts.value = result || []
  } catch (error) {
    console.error('加载期货合约失败:', error)
  }
}

// 添加基差报价
function addQuote() {
  quotes.value.push({
    contractCode: '',
    contractName: '',
    basisPrice: 0,
    availableQty: 1,
    referencePrice: null,
    contractIndex: -1
  })
}

// 删除基差报价
function removeQuote(index: number) {
  quotes.value.splice(index, 1)
}

// 期货合约选择变化
function onContractChange(index: number, event: any) {
  const contractIndex = event.detail.value
  const contract = contracts.value[contractIndex]
  
  if (contract) {
    quotes.value[index].contractIndex = contractIndex
    quotes.value[index].contractCode = contract.contractCode
    quotes.value[index].contractName = contract.contractName
    quotes.value[index].referencePrice = contract.lastPrice
  }
}

// 计算参考价格
function calculateReferencePrice(index: number) {
  const quote = quotes.value[index]
  if (quote.referencePrice !== null && quote.basisPrice !== undefined) {
    return quote.referencePrice + quote.basisPrice
  }
  return null
}

// 验证数据
function validate(): boolean {
  const validQuotes = quotes.value.filter(q => 
    q.contractCode && 
    q.basisPrice !== undefined && 
    q.availableQty && q.availableQty > 0
  )
  return validQuotes.length > 0
}

// 获取有效的基差报价
function getValidQuotes(): BasisQuoteRequest[] {
  return quotes.value.filter(q => 
    q.contractCode && 
    q.basisPrice !== undefined && 
    q.availableQty && q.availableQty > 0
  )
}

// 暴露方法
defineExpose({
  validate,
  getValidQuotes
})

onMounted(() => {
  loadFuturesContracts()
})
</script>

<style lang="scss" scoped>
.basis-quote-editor {
  padding: $spacing-md;
}

.section-header {
  margin-bottom: $spacing-lg;
  text-align: center;
}

.section-title {
  font-size: $font-lg;
  font-weight: 600;
  color: $text-primary;
  margin-bottom: $spacing-xs;
}

.section-desc {
  font-size: $font-sm;
  color: $text-secondary;
}

.quotes-list {
  margin-bottom: $spacing-lg;
}

.quote-item {
  background: $bg-card;
  border-radius: $radius-md;
  border: 1rpx solid $border-light;
  padding: $spacing-md;
  margin-bottom: $spacing-md;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.quote-item__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: $spacing-md;
}

.quote-item__number {
  font-size: $font-sm;
  color: $brand-600;
  font-weight: 600;
}

.quote-item__remove {
  font-size: $font-xs;
  color: $error-500;
  padding: $spacing-xs $spacing-sm;
  border: 1rpx solid $error-200;
  border-radius: $radius-sm;
}

.quote-item__field {
  margin-bottom: $spacing-md;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.field-label {
  font-size: $font-sm;
  color: $text-secondary;
  margin-bottom: $spacing-xs;
  display: block;
}

.field-picker {
  width: 100%;
  background: $bg-card;
  border: 1rpx solid $border-light;
  border-radius: $radius-sm;
  padding: $spacing-sm;
}

.picker-display {
  font-size: $font-sm;
  color: $text-primary;
  text-align: center;
}

.price-input {
  display: flex;
  align-items: center;
  background: $bg-card;
  border: 1rpx solid $border-light;
  border-radius: $radius-sm;
  padding: $spacing-sm;
}

.price-input__input {
  flex: 1;
  font-size: $font-sm;
  color: $text-primary;
  border: none;
  outline: none;
}

.price-sign, .price-unit {
  font-size: $font-sm;
  color: $text-secondary;
  margin: 0 $spacing-xs;
}

.price-desc {
  font-size: $font-xs;
  color: $text-placeholder;
  margin-top: $spacing-xs;
}

.quantity-input {
  display: flex;
  align-items: center;
  background: $bg-card;
  border: 1rpx solid $border-light;
  border-radius: $radius-sm;
  padding: $spacing-sm;
}

.quantity-input__input {
  flex: 1;
  font-size: $font-sm;
  color: $text-primary;
  border: none;
  outline: none;
}

.quantity-unit {
  font-size: $font-sm;
  color: $text-secondary;
  margin-left: $spacing-xs;
}

.quote-item__reference {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-sm;
  background: $brand-50;
  border-radius: $radius-sm;
  margin-top: $spacing-sm;
}

.reference-label {
  font-size: $font-sm;
  color: $text-secondary;
}

.reference-value {
  font-size: $font-sm;
  font-weight: 600;
  color: $brand-600;
}

.reference-desc {
  font-size: $font-xs;
  color: $text-placeholder;
}

.add-button {
  width: 100%;
  padding: $spacing-md;
  background: $brand-600;
  border-radius: $radius-md;
  text-align: center;
  margin-bottom: $spacing-lg;
}

.add-button__text {
  font-size: $font-sm;
  color: $bg-card;
  font-weight: 500;
}

.tips-section {
  background: $warm-50;
  border-radius: $radius-md;
  padding: $spacing-md;
}

.tips-item {
  display: flex;
  align-items: flex-start;
  gap: $spacing-sm;
  margin-bottom: $spacing-sm;
  
  &:last-child {
    margin-bottom: 0;
  }
}

.tips-icon {
  font-size: $font-sm;
  margin-top: 2rpx;
}

.tips-text {
  font-size: $font-xs;
  color: $text-secondary;
  line-height: 1.4;
  flex: 1;
}
</style>