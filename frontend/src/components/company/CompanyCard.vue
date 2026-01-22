<script setup lang="ts">
import { MapPin, Package, ChevronRight } from 'lucide-vue-next'

export interface CompanyCardProps {
  id: number
  companyName: string
  province?: string
  city?: string
  categoryNames?: string[]
  count?: number
  type?: 'supplier' | 'buyer'
  clickable?: boolean
}

const props = withDefaults(defineProps<CompanyCardProps>(), {
  type: 'supplier',
  clickable: true,
  count: 0
})

const emit = defineEmits<{
  click: [id: number]
}>()

function handleClick() {
  if (props.clickable) {
    emit('click', props.id)
  }
}

function handleKeyDown(event: KeyboardEvent) {
  if (props.clickable && (event.key === 'Enter' || event.key === ' ')) {
    event.preventDefault()
    handleClick()
  }
}
</script>

<template>
  <article
    class="group bg-white p-6 rounded-xl border border-gray-200 hover:shadow-md hover:border-brand-100 transition-all flex flex-col"
    :class="{ 'cursor-pointer': clickable }"
    role="article"
    :tabindex="clickable ? 0 : -1"
    :aria-label="`${companyName} - ${type === 'supplier' ? '供应商' : '采购商'}`"
    @click="handleClick"
    @keydown="handleKeyDown"
  >
    <div class="flex items-center gap-4 mb-6">
      <div
        class="w-14 h-14 rounded-xl flex items-center justify-center font-bold text-white text-xl shadow-sm transition-transform group-hover:scale-110 shrink-0"
        :class="type === 'supplier' ? 'bg-gradient-to-br from-brand-500 to-teal-600' : 'bg-gradient-to-br from-blue-500 to-indigo-600'"
      >
        {{ companyName[0] }}
      </div>
      <div class="min-w-0 flex-1">
        <h3 class="font-bold text-gray-900 truncate group-hover:text-brand-600 transition-colors">
          {{ companyName }}
        </h3>
        <div v-if="province || city" class="flex items-center gap-1.5 text-xs text-gray-400 mt-1">
          <MapPin :size="12" aria-hidden="true" />
          <span>{{ province }}{{ city ? ` · ${city}` : '' }}</span>
        </div>
      </div>
    </div>

    <div class="space-y-3 mb-6 flex-1">
      <div v-if="categoryNames && categoryNames.length > 0" class="flex items-start gap-3">
        <Package :size="14" class="text-gray-300 mt-0.5 shrink-0" aria-hidden="true" />
        <div class="text-xs text-gray-500 line-clamp-2">
          {{ categoryNames.join(' / ') }}
        </div>
      </div>
      <div v-else class="flex items-start gap-3">
        <Package :size="14" class="text-gray-300 mt-0.5 shrink-0" aria-hidden="true" />
        <div class="text-xs text-gray-400">
          暂无分类
        </div>
      </div>
    </div>

    <div class="flex items-center justify-between pt-4 border-t border-gray-50">
      <div class="text-xs">
        <span class="text-gray-400">{{ type === 'supplier' ? '累计供应' : '累计需求' }}</span>
        <b class="ml-2" :class="type === 'supplier' ? 'text-brand-600' : 'text-blue-600'">{{ count }}</b>
      </div>
      <div v-if="clickable" class="flex items-center gap-1 text-xs font-bold" :class="type === 'supplier' ? 'text-brand-600' : 'text-blue-600'">
        <span>进入主页</span>
        <ChevronRight :size="14" aria-hidden="true" />
      </div>
    </div>
  </article>
</template>
