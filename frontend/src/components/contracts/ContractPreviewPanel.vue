<script setup lang="ts">
import { computed } from 'vue'
import { Download, Eye, FileText, Printer, ShieldCheck } from 'lucide-vue-next'
import ContractStatusBadge from './ContractStatusBadge.vue'
import type { ContractStatus } from '../../api/contract'
import type { ContractPreviewData, ContractRow } from '../../composables/useContractCenter'

const props = defineProps<{
  contract: ContractRow | null
  preview: ContractPreviewData | null
}>()

const emit = defineEmits<{
  (e: 'print'): void
  (e: 'download'): void
  (e: 'initiateSign'): void
  (e: 'sign'): void
}>()

const canInitiate = computed(() => props.contract?.status === 'draft')
const canSign = computed(() => props.contract?.status === 'pending')
const canDownload = computed(() => ['signed', 'executing', 'completed'].includes((props.contract?.status as ContractStatus) || ''))
</script>

<template>
  <main class="flex-1 bg-gray-50 rounded-2xl border border-gray-100 overflow-hidden flex flex-col">
    <div v-if="contract && preview" class="flex-1 overflow-y-auto">
      <div class="p-6 md:p-8 space-y-6">
        <!-- 工具栏 -->
        <div class="flex flex-wrap items-center justify-between gap-4 bg-white p-4 rounded-2xl border border-gray-100">
          <div class="flex items-center gap-4 min-w-0">
            <div class="w-12 h-12 rounded-2xl bg-emerald-50 border border-emerald-100 text-emerald-600 flex items-center justify-center shrink-0">
              <FileText :size="22" :stroke-width="2" />
            </div>
            <div class="min-w-0">
              <div class="flex items-center gap-2 min-w-0">
                <h2 class="font-bold text-gray-900 truncate">{{ contract.title }}</h2>
                <ContractStatusBadge :status="contract.status" />
              </div>
              <p class="text-xs text-gray-400 mt-1">最后更新：{{ contract.signTime || contract.createTime }}</p>
            </div>
          </div>

          <div class="flex items-center gap-2">
            <button
              class="flex items-center gap-2 px-4 py-2 text-sm font-bold text-gray-700 hover:bg-gray-50 rounded-xl transition-all active:scale-95 border border-gray-200"
              @click="emit('print')"
            >
              <Printer :size="18" :stroke-width="2" />
              打印
            </button>

            <button
              v-if="canDownload"
              class="flex items-center gap-2 px-4 py-2 text-sm font-bold text-gray-700 hover:bg-gray-50 rounded-xl transition-all active:scale-95 border border-gray-200 disabled:opacity-50 disabled:cursor-not-allowed"
              @click="emit('download')"
            >
              <Download :size="18" :stroke-width="2" />
              下载
            </button>

            <button
              v-if="canInitiate"
              class="flex items-center gap-2 px-4 py-2 text-sm font-bold bg-emerald-600 text-white hover:bg-emerald-700 rounded-xl transition-all active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
              @click="emit('initiateSign')"
            >
              发起签署
            </button>

            <button
              v-if="canSign"
              class="flex items-center gap-2 px-4 py-2 text-sm font-bold bg-slate-900 text-white hover:bg-slate-800 rounded-xl transition-all active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed"
              @click="emit('sign')"
            >
              确认签署
            </button>
          </div>
        </div>

        <!-- 文档预览 -->
        <div class="bg-white rounded-2xl border border-gray-100 overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-100 flex items-center justify-between">
            <div class="text-xs font-bold text-gray-400 uppercase tracking-widest">
              {{ preview.metadata.contractNo }} · {{ preview.metadata.version }}
            </div>
            <div class="text-xs text-gray-400">状态：{{ preview.metadata.status }}</div>
          </div>

          <div class="relative p-8 md:p-12">
            <div class="absolute top-6 right-6 text-gray-100">
              <ShieldCheck :size="44" :stroke-width="2" />
            </div>

            <div class="max-w-3xl mx-auto space-y-10">
              <h1 class="text-2xl md:text-3xl font-extrabold tracking-tight text-gray-900 text-center border-b border-gray-900/10 pb-4">
                {{ preview.header }}
              </h1>

              <section v-for="(s, idx) in preview.sections" :key="idx" class="space-y-3">
                <h2 class="text-lg font-bold text-gray-900">{{ s.title }}</h2>
                <p class="text-sm text-gray-600 leading-relaxed whitespace-pre-wrap">
                  {{ s.content }}
                </p>
              </section>

              <div class="pt-10 grid grid-cols-1 md:grid-cols-2 gap-8">
                <div class="space-y-3">
                  <p class="text-sm font-bold text-gray-900">甲方：(签章)</p>
                  <div class="h-14 border-b border-gray-200 border-dashed" />
                </div>
                <div class="space-y-3">
                  <p class="text-sm font-bold text-gray-900">乙方：(签章)</p>
                  <div class="h-14 border-b border-gray-200 border-dashed" />
                </div>
              </div>

              <div class="pt-8 border-t border-gray-100 text-center text-[10px] text-gray-400 font-mono">
                CONTRACT_NO: {{ preview.metadata.contractNo }}
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 空态 -->
    <div v-else class="flex-1 flex flex-col items-center justify-center text-gray-400 p-10">
      <div class="w-20 h-20 bg-white rounded-3xl border border-gray-100 flex items-center justify-center mb-6">
        <Eye :size="34" :stroke-width="2" class="text-gray-200" />
      </div>
      <div class="text-lg font-bold text-gray-500">从左侧选择合同进行预览</div>
      <div class="text-sm text-gray-400 mt-2">支持搜索、筛选、签署与下载</div>
    </div>
  </main>
</template>


