<script setup lang="ts">
import { computed, reactive } from 'vue'

export type QuoteFields = {
  price?: string
  quantity?: string
  deliveryPlace?: string
  arrivalDate?: string
  paymentMethod?: string
  validUntil?: string
  remark?: string
}

const props = defineProps<{
  disabled?: boolean
  peerLatestQuote?: QuoteFields | null
}>()

const emit = defineEmits<{
  (e: 'send', payload: { msgType: 'QUOTE'; payload: any; summary: string }): void
}>()

const form = reactive<QuoteFields>({
  price: '',
  quantity: '',
  deliveryPlace: '',
  arrivalDate: '',
  paymentMethod: '',
  validUntil: '',
  remark: ''
})

function clean(v?: string) {
  const s = (v ?? '').trim()
  return s || undefined
}

const normalized = computed(() => {
  const out: QuoteFields = {
    price: clean(form.price),
    quantity: clean(form.quantity),
    deliveryPlace: clean(form.deliveryPlace),
    arrivalDate: clean(form.arrivalDate),
    paymentMethod: clean(form.paymentMethod),
    validUntil: clean(form.validUntil),
    remark: clean(form.remark)
  }
  return out
})

const canSend = computed(() => {
  const v = normalized.value
  return !!(v.price || v.quantity || v.deliveryPlace || v.arrivalDate || v.paymentMethod || v.validUntil || v.remark)
})

const diff = computed(() => {
  const peer = props.peerLatestQuote || {}
  const mine = normalized.value
  const keys: Array<keyof QuoteFields> = ['price', 'quantity', 'deliveryPlace', 'arrivalDate', 'paymentMethod', 'validUntil', 'remark']
  const changed: Array<{ k: keyof QuoteFields; mine?: string; peer?: string }> = []
  for (const k of keys) {
    const mv = mine[k]
    const pv = peer[k]
    if ((mv || '') !== (pv || '')) {
      if (mv || pv) changed.push({ k, mine: mv, peer: pv })
    }
  }
  return changed
})

const hasPeerQuote = computed(() => !!props.peerLatestQuote && Object.values(props.peerLatestQuote).some(Boolean))

function labelOf(k: keyof QuoteFields) {
  if (k === 'price') return '单价'
  if (k === 'quantity') return '数量'
  if (k === 'deliveryPlace') return '交付地'
  if (k === 'arrivalDate') return '到货期'
  if (k === 'paymentMethod') return '结算方式'
  if (k === 'validUntil') return '有效期'
  return '备注'
}

function buildSummary(v: QuoteFields) {
  const parts: string[] = []
  if (v.price) parts.push(`¥${v.price}`)
  if (v.quantity) parts.push(`${v.quantity}`)
  if (v.deliveryPlace) parts.push(`交付:${v.deliveryPlace}`)
  if (v.arrivalDate) parts.push(`到货:${v.arrivalDate}`)
  if (v.paymentMethod) parts.push(`结算:${v.paymentMethod}`)
  if (v.validUntil) parts.push(`有效:${v.validUntil}`)
  return parts.length ? parts.join(' · ') : '报价卡'
}

function clear() {
  form.price = ''
  form.quantity = ''
  form.deliveryPlace = ''
  form.arrivalDate = ''
  form.paymentMethod = ''
  form.validUntil = ''
  form.remark = ''
}

function send() {
  if (!canSend.value) return
  const v = normalized.value
  const payload = {
    version: 1,
    kind: 'QUOTE_V1',
    createdAt: new Date().toISOString(),
    fields: v
  }
  emit('send', { msgType: 'QUOTE', payload, summary: buildSummary(v) })
}
</script>

<template>
  <div class="bg-white rounded-2xl border border-gray-100 p-4">
    <div class="flex items-start justify-between gap-4">
      <div>
        <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400">结构化议价</div>
        <div class="mt-1 font-bold text-gray-900">报价草稿</div>
      </div>
      <div class="flex items-center gap-2 shrink-0">
        <button
          class="px-3 py-2 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm font-bold transition-all active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="disabled"
          @click="clear"
        >
          清空
        </button>
        <button
          class="px-3 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-bold transition-all active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
          :disabled="disabled || !canSend"
          @click="send"
        >
          发送报价卡
        </button>
      </div>
    </div>

    <div class="mt-4 grid grid-cols-2 gap-3">
      <div class="space-y-1">
        <div class="text-xs text-gray-500">单价（元/吨）</div>
        <input v-model="form.price" :disabled="disabled" class="w-full border-2 border-gray-100 rounded-xl px-3 py-2 text-sm focus:border-emerald-500 outline-none transition-all" placeholder="如 2350" />
      </div>
      <div class="space-y-1">
        <div class="text-xs text-gray-500">数量</div>
        <input v-model="form.quantity" :disabled="disabled" class="w-full border-2 border-gray-100 rounded-xl px-3 py-2 text-sm focus:border-emerald-500 outline-none transition-all" placeholder="如 300 吨" />
      </div>
      <div class="space-y-1">
        <div class="text-xs text-gray-500">交付地</div>
        <input v-model="form.deliveryPlace" :disabled="disabled" class="w-full border-2 border-gray-100 rounded-xl px-3 py-2 text-sm focus:border-emerald-500 outline-none transition-all" placeholder="如 锦州港" />
      </div>
      <div class="space-y-1">
        <div class="text-xs text-gray-500">到货期</div>
        <input v-model="form.arrivalDate" :disabled="disabled" class="w-full border-2 border-gray-100 rounded-xl px-3 py-2 text-sm focus:border-emerald-500 outline-none transition-all" placeholder="如 2026-01-10" />
      </div>
      <div class="space-y-1">
        <div class="text-xs text-gray-500">结算方式</div>
        <input v-model="form.paymentMethod" :disabled="disabled" class="w-full border-2 border-gray-100 rounded-xl px-3 py-2 text-sm focus:border-emerald-500 outline-none transition-all" placeholder="如 现款/账期" />
      </div>
      <div class="space-y-1">
        <div class="text-xs text-gray-500">有效期</div>
        <input v-model="form.validUntil" :disabled="disabled" class="w-full border-2 border-gray-100 rounded-xl px-3 py-2 text-sm focus:border-emerald-500 outline-none transition-all" placeholder="如 24小时 / 2026-01-03" />
      </div>
      <div class="col-span-2 space-y-1">
        <div class="text-xs text-gray-500">备注</div>
        <textarea v-model="form.remark" :disabled="disabled" rows="2" class="w-full resize-none border-2 border-gray-100 rounded-xl px-3 py-2 text-sm focus:border-emerald-500 outline-none transition-all" placeholder="补充说明（可选）" />
      </div>
    </div>

    <div class="mt-4" v-if="hasPeerQuote">
      <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-2">对方最新报价</div>
      <div class="bg-gray-50 rounded-2xl border border-gray-100 p-3 text-sm text-gray-700">
        <div class="grid grid-cols-2 gap-2">
          <div v-for="(v, k) in peerLatestQuote" :key="k" class="truncate">
            <span class="text-xs text-gray-500">{{ labelOf(k as any) }}：</span>
            <span class="font-semibold">{{ v || '-' }}</span>
          </div>
        </div>
      </div>
    </div>

    <div class="mt-4" v-if="diff.length">
      <div class="text-[10px] font-bold uppercase tracking-widest text-gray-400 mb-2">差异对比</div>
      <div class="space-y-2">
        <div v-for="d in diff" :key="d.k" class="bg-white rounded-xl border border-gray-100 px-3 py-2">
          <div class="flex items-center justify-between gap-3">
            <div class="text-xs text-gray-500">{{ labelOf(d.k) }}</div>
            <div class="text-[10px] font-bold uppercase tracking-widest text-emerald-700 bg-emerald-50 border border-emerald-100 px-2 py-0.5 rounded-full">
              已变更
            </div>
          </div>
          <div class="mt-1 text-sm text-gray-800">
            <span class="text-gray-400">对方：</span><b class="font-semibold">{{ d.peer || '-' }}</b>
            <span class="mx-2 text-gray-300">→</span>
            <span class="text-gray-400">我方：</span><b class="font-semibold">{{ d.mine || '-' }}</b>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>


