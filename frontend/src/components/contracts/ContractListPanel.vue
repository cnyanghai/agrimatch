<script setup lang="ts">
import { computed } from 'vue'
import { FileText, Plus, Search, Trash2 } from 'lucide-vue-next'
import ContractStatusBadge from './ContractStatusBadge.vue'
import type { ContractStatus } from '../../api/contract'
import type { ContractRow } from '../../composables/useContractCenter'

const props = defineProps<{
  contracts: ContractRow[]
  selectedId: number | null
  loading: boolean
  searchQuery: string
  statusFilter: ContractStatus | 'all'
  stats: { all: number; draft: number; pending: number; signed: number; executing: number; completed: number; cancelled: number }
}>()

const emit = defineEmits<{
  (e: 'update:searchQuery', v: string): void
  (e: 'update:statusFilter', v: ContractStatus | 'all'): void
  (e: 'select', id: number): void
  (e: 'create'): void
  (e: 'delete', id: number): void
}>()

const statusTabs = computed(() => [
  { key: 'all' as const, label: `全部 ${props.stats.all}` },
  { key: 'draft' as const, label: `草稿 ${props.stats.draft}` },
  { key: 'pending' as const, label: `待签署 ${props.stats.pending}` },
  { key: 'signed' as const, label: `已签署 ${props.stats.signed}` },
  { key: 'executing' as const, label: `履行中 ${props.stats.executing}` },
  { key: 'completed' as const, label: `已完成 ${props.stats.completed}` }
])
</script>

<template>
  <aside class="w-full lg:w-96 shrink-0 bg-white border border-gray-100 rounded-2xl overflow-hidden flex flex-col">
    <div class="p-6 border-b border-gray-100">
      <div class="flex items-center justify-between gap-4 mb-5">
        <div class="flex items-center gap-2">
          <div class="w-10 h-10 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center border border-emerald-100">
            <FileText :size="18" :stroke-width="2" />
          </div>
          <div class="leading-tight">
            <div class="text-lg font-bold text-gray-900">合同中心</div>
            <div class="text-[10px] font-bold text-gray-400 uppercase tracking-widest">Contract Workspace</div>
          </div>
        </div>

        <button
          class="bg-emerald-600 hover:bg-emerald-700 text-white px-4 py-2 rounded-full font-bold text-sm transition-all active:scale-95 inline-flex items-center gap-2"
          @click="emit('create')"
        >
          <Plus :size="18" :stroke-width="2" />
          新建
        </button>
      </div>

      <div class="relative">
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" :size="18" :stroke-width="2" />
        <input
          :value="searchQuery"
          type="text"
          placeholder="搜索合同编号或名称..."
          class="w-full pl-10 pr-3 py-2.5 bg-gray-50 border-2 border-gray-100 rounded-xl text-sm focus:border-emerald-500 focus:ring-4 focus:ring-emerald-500/10 outline-none transition-all"
          @input="emit('update:searchQuery', ($event.target as HTMLInputElement).value)"
        />
      </div>

      <div class="flex flex-wrap gap-2 mt-4">
        <button
          v-for="t in statusTabs"
          :key="t.key"
          class="px-3 py-1 rounded-full text-xs font-bold border transition-all active:scale-95"
          :class="statusFilter === t.key ? 'bg-emerald-50 text-emerald-700 border-emerald-100' : 'bg-white text-gray-600 border-gray-200 hover:bg-gray-50'"
          @click="emit('update:statusFilter', t.key)"
        >
          {{ t.label }}
        </button>
      </div>
    </div>

    <div class="flex-1 overflow-y-auto p-4 space-y-3 bg-white">
      <div v-if="loading" class="space-y-3">
        <div v-for="i in 4" :key="i" class="h-20 bg-gray-50 animate-pulse rounded-2xl border border-gray-100" />
      </div>

      <div v-else-if="contracts.length === 0" class="text-center py-16 text-gray-400 text-sm">
        暂无合同
      </div>

      <div
        v-for="c in contracts"
        :key="c.id"
        class="group p-4 rounded-2xl border transition-all cursor-pointer"
        :class="selectedId === c.id ? 'bg-emerald-50/40 border-emerald-100 ring-2 ring-emerald-500/20' : 'bg-white border-gray-100 hover:shadow-sm hover:border-emerald-100'"
        @click="emit('select', c.id)"
      >
        <div class="flex items-start justify-between gap-3">
          <div class="min-w-0">
            <div class="text-[10px] font-mono text-gray-400">{{ c.contractNo }}</div>
            <div class="mt-1 text-sm font-semibold text-gray-800 truncate">{{ c.title }}</div>
          </div>
          <ContractStatusBadge :status="c.status" />
        </div>

        <div class="mt-3 flex items-center justify-between gap-3">
          <div class="text-xs text-gray-500 truncate">
            {{ c.partyA }} · {{ c.partyB }}
          </div>
          <button
            class="opacity-0 group-hover:opacity-100 p-2 rounded-xl text-gray-400 hover:text-red-600 hover:bg-red-50 transition-all active:scale-95"
            title="删除"
            @click.stop="emit('delete', c.id)"
          >
            <Trash2 :size="16" :stroke-width="2" />
          </button>
        </div>
      </div>
    </div>
  </aside>
</template>


