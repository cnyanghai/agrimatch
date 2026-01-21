<script setup lang="ts">
import { Package, Search, FileText, Users, MessageSquare, ShoppingCart, Box, Folder } from 'lucide-vue-next'
import BaseButton from './BaseButton.vue'

const props = withDefaults(defineProps<{
  type?: 'default' | 'search' | 'data' | 'message' | 'user' | 'order' | 'supply' | 'folder' | 'empty'
  title?: string
  description?: string
  actionText?: string
  size?: 'sm' | 'md' | 'lg'
}>(), {
  type: 'default',
  size: 'md'
})

const emit = defineEmits<{
  (e: 'action'): void
}>()

// 图标映射
const iconMap: Record<string, any> = {
  default: Package,
  search: Search,
  data: FileText,
  message: MessageSquare,
  user: Users,
  order: ShoppingCart,
  supply: Box,
  folder: Folder,
  empty: Package
}

// 默认文案映射
const defaultContent: Record<string, { title: string; description: string }> = {
  default: { title: '暂无数据', description: '数据为空' },
  search: { title: '未找到结果', description: '请尝试其他关键词或筛选条件' },
  data: { title: '暂无数据', description: '还没有任何数据记录' },
  message: { title: '暂无消息', description: '目前没有新的消息' },
  user: { title: '暂无用户', description: '还没有用户数据' },
  order: { title: '暂无订单', description: '还没有订单记录' },
  supply: { title: '暂无供应', description: '暂时没有相关供应信息' },
  folder: { title: '暂无文件夹', description: '文件夹为空' },
  empty: { title: '暂无内容', description: '还没有任何内容' }
}

// 尺寸映射
const sizeClasses: Record<string, { container: string; icon: string; title: string; desc: string }> = {
  sm: {
    container: 'py-8',
    icon: 'w-12 h-12',
    title: 'text-sm',
    desc: 'text-xs'
  },
  md: {
    container: 'py-12',
    icon: 'w-16 h-16',
    title: 'text-lg',
    desc: 'text-sm'
  },
  lg: {
    container: 'py-16',
    icon: 'w-20 h-20',
    title: 'text-xl',
    desc: 'text-base'
  }
}

const currentSizeClasses = sizeClasses[props.size] ?? sizeClasses['md']!
</script>

<template>
  <div :class="['text-center', currentSizeClasses.container]">
    <!-- 图标 -->
    <div 
      :class="[
        'mx-auto mb-4 rounded-xl bg-gray-50 flex items-center justify-center',
        currentSizeClasses.icon
      ]"
    >
      <slot name="icon">
        <component 
          :is="iconMap[type]" 
          class="w-1/2 h-1/2 text-gray-300"
        />
      </slot>
    </div>
    
    <!-- 标题 -->
    <h3 :class="['font-bold text-gray-900 mb-2', currentSizeClasses.title]">
      {{ title || defaultContent[type]?.title || '暂无数据' }}
    </h3>
    
    <!-- 描述 -->
    <p :class="['text-gray-500 mb-6', currentSizeClasses.desc]">
      {{ description || defaultContent[type]?.description || '数据为空' }}
    </p>
    
    <!-- 操作按钮 -->
    <BaseButton 
      v-if="actionText" 
      type="primary"
      @click="emit('action')"
    >
      {{ actionText }}
    </BaseButton>
    
    <!-- 自定义操作插槽 -->
    <slot name="action" />
  </div>
</template>

