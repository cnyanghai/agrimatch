<script setup lang="ts">
import BaseModal from './ui/BaseModal.vue'
import { Compass, Share2, SquarePlus, CheckCircle } from 'lucide-vue-next'

const props = defineProps<{
  modelValue: boolean
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

function close() {
  emit('update:modelValue', false)
}
</script>

<template>
  <BaseModal
    :modelValue="modelValue"
    @update:modelValue="emit('update:modelValue', $event)"
    title="在iPhone上使用沃谷"
    size="sm"
    :showFooter="true"
  >
    <div class="space-y-5 py-2">
      <!-- 步骤 1 -->
      <div class="flex items-start gap-4">
        <div class="flex flex-col items-center gap-1 shrink-0">
          <div class="w-7 h-7 rounded-full bg-brand-600 text-white text-xs font-bold flex items-center justify-center">
            1
          </div>
          <div class="w-px flex-1 bg-neutral-200 min-h-[20px]" />
        </div>
        <div class="flex items-start gap-3 pb-2">
          <div class="w-9 h-9 rounded-xl bg-blue-50 flex items-center justify-center shrink-0 mt-0.5">
            <Compass class="w-5 h-5 text-blue-500" />
          </div>
          <div>
            <p class="text-sm font-semibold text-neutral-900">用 Safari 打开网站</p>
            <p class="text-xs text-neutral-500 mt-0.5">在地址栏输入 <span class="font-mono text-brand-600">wogucloud.com</span></p>
          </div>
        </div>
      </div>

      <!-- 步骤 2 -->
      <div class="flex items-start gap-4">
        <div class="flex flex-col items-center gap-1 shrink-0">
          <div class="w-7 h-7 rounded-full bg-brand-600 text-white text-xs font-bold flex items-center justify-center">
            2
          </div>
          <div class="w-px flex-1 bg-neutral-200 min-h-[20px]" />
        </div>
        <div class="flex items-start gap-3 pb-2">
          <div class="w-9 h-9 rounded-xl bg-green-50 flex items-center justify-center shrink-0 mt-0.5">
            <Share2 class="w-5 h-5 text-green-500" />
          </div>
          <div>
            <p class="text-sm font-semibold text-neutral-900">点击底部分享按钮</p>
            <p class="text-xs text-neutral-500 mt-0.5">Safari 底部工具栏中间的方块箭头图标</p>
          </div>
        </div>
      </div>

      <!-- 步骤 3 -->
      <div class="flex items-start gap-4">
        <div class="flex flex-col items-center gap-1 shrink-0">
          <div class="w-7 h-7 rounded-full bg-brand-600 text-white text-xs font-bold flex items-center justify-center">
            3
          </div>
          <div class="w-px flex-1 bg-neutral-200 min-h-[20px]" />
        </div>
        <div class="flex items-start gap-3 pb-2">
          <div class="w-9 h-9 rounded-xl bg-orange-50 flex items-center justify-center shrink-0 mt-0.5">
            <SquarePlus class="w-5 h-5 text-orange-500" />
          </div>
          <div>
            <p class="text-sm font-semibold text-neutral-900">选择"添加到主屏幕"</p>
            <p class="text-xs text-neutral-500 mt-0.5">在分享菜单中向下滚动找到该选项</p>
          </div>
        </div>
      </div>

      <!-- 步骤 4 -->
      <div class="flex items-start gap-4">
        <div class="shrink-0">
          <div class="w-7 h-7 rounded-full bg-brand-600 text-white text-xs font-bold flex items-center justify-center">
            4
          </div>
        </div>
        <div class="flex items-start gap-3">
          <div class="w-9 h-9 rounded-xl bg-purple-50 flex items-center justify-center shrink-0 mt-0.5">
            <CheckCircle class="w-5 h-5 text-purple-500" />
          </div>
          <div>
            <p class="text-sm font-semibold text-neutral-900">点击右上角"添加"</p>
            <p class="text-xs text-neutral-500 mt-0.5">添加后桌面将出现沃谷图标，点击即可使用</p>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <button
        class="w-full px-6 py-3 bg-brand-600 hover:bg-brand-700 text-white text-base font-semibold rounded-xl transition-all duration-200 hover:scale-105 hover:-translate-y-0.5"
        @click="close"
      >
        我知道了
      </button>
    </template>
  </BaseModal>
</template>
