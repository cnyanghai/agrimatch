<script setup lang="ts">
import { computed } from 'vue'
import { ArrowLeft, FileText, Save } from 'lucide-vue-next'
import type { ContractDraft } from '../../composables/useContractCenter'

const props = defineProps<{
  modelValue: ContractDraft
  saving: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: ContractDraft): void
  (e: 'cancel'): void
  (e: 'saveDraft'): void
  (e: 'saveAndInitiate'): void
}>()

const totalAmount = computed(() => Number(props.modelValue.quantity || 0) * Number(props.modelValue.unitPrice || 0))

function patch(p: Partial<ContractDraft>) {
  emit('update:modelValue', { ...props.modelValue, ...p })
}
</script>

<template>
  <main class="flex-1 bg-gray-50 rounded-2xl border border-gray-100 overflow-hidden flex flex-col">
    <div class="p-6 md:p-8 space-y-6 overflow-y-auto">
      <div class="bg-white p-5 rounded-2xl border border-gray-100 flex flex-wrap items-center justify-between gap-4">
        <div class="flex items-center gap-3">
          <button
            class="p-2 rounded-full hover:bg-gray-50 transition-all active:scale-95 text-gray-500"
            @click="emit('cancel')"
          >
            <ArrowLeft :size="18" :stroke-width="2" />
          </button>
          <div class="w-10 h-10 rounded-2xl bg-emerald-50 border border-emerald-100 text-emerald-600 flex items-center justify-center">
            <FileText :size="18" :stroke-width="2" />
          </div>
          <div>
            <div class="text-lg font-bold text-gray-900">新建合同</div>
            <div class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">Draft Builder</div>
          </div>
        </div>

        <div class="flex items-center gap-2">
          <button
            class="px-4 py-2 rounded-xl border border-gray-200 text-gray-700 font-bold text-sm hover:bg-gray-50 transition-all active:scale-95 disabled:opacity-50"
            :disabled="saving"
            @click="emit('saveDraft')"
          >
            保存草稿
          </button>
          <button
            class="px-4 py-2 rounded-xl bg-emerald-600 text-white font-bold text-sm hover:bg-emerald-700 transition-all active:scale-95 disabled:opacity-50 inline-flex items-center gap-2"
            :disabled="saving"
            @click="emit('saveAndInitiate')"
          >
            <Save :size="16" :stroke-width="2" />
            保存并发起签署
          </button>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
        <div class="bg-white p-6 rounded-2xl border border-gray-100 space-y-4">
          <div class="text-sm font-bold text-gray-900">合同基础</div>

          <div>
            <div class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">合同编号</div>
            <input
              :value="modelValue.contractNo"
              class="w-full border-2 border-gray-100 rounded-xl px-4 py-2.5 text-sm bg-gray-50 text-gray-500"
              disabled
            />
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div>
              <div class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">合同类型</div>
              <select
                :value="modelValue.contractType"
                class="w-full border-2 border-gray-100 rounded-xl px-4 py-2.5 text-sm bg-white focus:border-emerald-500 outline-none transition-all"
                @change="patch({ contractType: ($event.target as HTMLSelectElement).value as any })"
              >
                <option value="purchase">采购合同</option>
                <option value="supply">供应合同</option>
              </select>
            </div>
            <div>
              <div class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">合同标题</div>
              <input
                :value="modelValue.title"
                class="w-full border-2 border-gray-100 rounded-xl px-4 py-2.5 text-sm focus:border-emerald-500 outline-none transition-all"
                placeholder="例如：玉米采购合同"
                @input="patch({ title: ($event.target as HTMLInputElement).value })"
              />
            </div>
          </div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div>
              <div class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">甲方（采购方）</div>
              <input
                :value="modelValue.partyA"
                class="w-full border-2 border-gray-100 rounded-xl px-4 py-2.5 text-sm focus:border-emerald-500 outline-none transition-all"
                placeholder="甲方公司名称"
                @input="patch({ partyA: ($event.target as HTMLInputElement).value })"
              />
            </div>
            <div>
              <div class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">乙方（供应方）</div>
              <input
                :value="modelValue.partyB"
                class="w-full border-2 border-gray-100 rounded-xl px-4 py-2.5 text-sm focus:border-emerald-500 outline-none transition-all"
                placeholder="乙方公司名称"
                @input="patch({ partyB: ($event.target as HTMLInputElement).value })"
              />
            </div>
          </div>
        </div>

        <div class="bg-white p-6 rounded-2xl border border-gray-100 space-y-4">
          <div class="text-sm font-bold text-gray-900">标的与价格</div>

          <div>
            <div class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">产品名称</div>
            <input
              :value="modelValue.productName"
              class="w-full border-2 border-gray-100 rounded-xl px-4 py-2.5 text-sm focus:border-emerald-500 outline-none transition-all"
              placeholder="例如：黄玉米"
              @input="patch({ productName: ($event.target as HTMLInputElement).value })"
            />
          </div>

          <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
            <div>
              <div class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">数量</div>
              <input
                :value="modelValue.quantity"
                type="number"
                min="0"
                class="w-full border-2 border-gray-100 rounded-xl px-4 py-2.5 text-sm focus:border-emerald-500 outline-none transition-all"
                @input="patch({ quantity: Number(($event.target as HTMLInputElement).value || 0) })"
              />
            </div>
            <div>
              <div class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">单位</div>
              <select
                :value="modelValue.unit"
                class="w-full border-2 border-gray-100 rounded-xl px-4 py-2.5 text-sm bg-white focus:border-emerald-500 outline-none transition-all"
                @change="patch({ unit: ($event.target as HTMLSelectElement).value })"
              >
                <option value="吨">吨</option>
                <option value="公斤">公斤</option>
                <option value="斤">斤</option>
                <option value="件">件</option>
              </select>
            </div>
            <div>
              <div class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">单价（元）</div>
              <input
                :value="modelValue.unitPrice"
                type="number"
                min="0"
                step="0.01"
                class="w-full border-2 border-gray-100 rounded-xl px-4 py-2.5 text-sm focus:border-emerald-500 outline-none transition-all"
                @input="patch({ unitPrice: Number(($event.target as HTMLInputElement).value || 0) })"
              />
            </div>
          </div>

          <div class="bg-gray-50/60 border border-gray-100 rounded-2xl p-4 flex items-center justify-between">
            <div class="text-xs text-gray-500">预计合同金额</div>
            <div class="text-lg font-black text-gray-900">¥{{ Math.round(totalAmount).toLocaleString('zh-CN') }}</div>
          </div>
        </div>

        <div class="bg-white p-6 rounded-2xl border border-gray-100 space-y-4 lg:col-span-2">
          <div class="text-sm font-bold text-gray-900">交付与条款</div>

          <div class="grid grid-cols-1 md:grid-cols-2 gap-3">
            <div>
              <div class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">交付日期</div>
              <input
                :value="modelValue.deliveryDate"
                type="date"
                class="w-full border-2 border-gray-100 rounded-xl px-4 py-2.5 text-sm focus:border-emerald-500 outline-none transition-all"
                @input="patch({ deliveryDate: ($event.target as HTMLInputElement).value })"
              />
            </div>
            <div>
              <div class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">付款方式</div>
              <input
                :value="modelValue.paymentMethod"
                class="w-full border-2 border-gray-100 rounded-xl px-4 py-2.5 text-sm focus:border-emerald-500 outline-none transition-all"
                placeholder="例如：货到付款/电汇/承兑"
                @input="patch({ paymentMethod: ($event.target as HTMLInputElement).value })"
              />
            </div>
          </div>

          <div>
            <div class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">交付地址</div>
            <input
              :value="modelValue.deliveryAddress"
              class="w-full border-2 border-gray-100 rounded-xl px-4 py-2.5 text-sm focus:border-emerald-500 outline-none transition-all"
              placeholder="请输入交付地址"
              @input="patch({ deliveryAddress: ($event.target as HTMLInputElement).value })"
            />
          </div>

          <div>
            <div class="text-xs font-bold text-gray-400 uppercase tracking-widest mb-2">其他条款</div>
            <textarea
              :value="modelValue.terms"
              rows="5"
              class="w-full border-2 border-gray-100 rounded-2xl px-4 py-3 text-sm focus:border-emerald-500 outline-none transition-all resize-none"
              placeholder="例如：质量标准、验收方式、违约责任、争议解决等"
              @input="patch({ terms: ($event.target as HTMLTextAreaElement).value })"
            />
          </div>
        </div>
      </div>
    </div>
  </main>
</template>


