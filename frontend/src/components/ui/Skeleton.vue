<script setup lang="ts">
withDefaults(defineProps<{
  type?: 'text' | 'title' | 'avatar' | 'card' | 'button' | 'image'
  width?: string
  height?: string
  rounded?: 'sm' | 'md' | 'lg' | 'xl' | '2xl' | 'full'
  lines?: number
}>(), {
  type: 'text',
  rounded: 'lg',
  lines: 1
})

// 圆角映射
const roundedClasses: Record<string, string> = {
  sm: 'rounded-sm',
  md: 'rounded-md',
  lg: 'rounded-lg',
  xl: 'rounded-xl',
  '2xl': 'rounded-2xl',
  full: 'rounded-full'
}
</script>

<template>
  <div>
    <!-- 文本骨架 -->
    <template v-if="type === 'text'">
      <div 
        v-for="i in lines" 
        :key="i"
        class="skeleton h-4 mb-2 last:mb-0"
        :class="roundedClasses[rounded]"
        :style="{ 
          width: i === lines && lines > 1 ? '70%' : (width || '100%'),
          height: height
        }"
      />
    </template>
    
    <!-- 标题骨架 -->
    <template v-else-if="type === 'title'">
      <div 
        class="skeleton h-6 mb-2"
        :class="roundedClasses[rounded]"
        :style="{ width: width || '60%', height: height }"
      />
    </template>
    
    <!-- 头像骨架 -->
    <template v-else-if="type === 'avatar'">
      <div 
        class="skeleton rounded-full"
        :style="{ width: width || '48px', height: height || '48px' }"
      />
    </template>
    
    <!-- 卡片骨架 -->
    <template v-else-if="type === 'card'">
      <div class="bg-white rounded-2xl border border-gray-100 p-5 space-y-4">
        <div class="flex items-center gap-3">
          <div class="skeleton w-10 h-10 rounded-xl" />
          <div class="flex-1 space-y-2">
            <div class="skeleton h-4 w-1/3 rounded-lg" />
            <div class="skeleton h-3 w-1/2 rounded-lg" />
          </div>
        </div>
        <div class="space-y-2">
          <div class="skeleton h-4 w-full rounded-lg" />
          <div class="skeleton h-4 w-full rounded-lg" />
          <div class="skeleton h-4 w-2/3 rounded-lg" />
        </div>
      </div>
    </template>
    
    <!-- 按钮骨架 -->
    <template v-else-if="type === 'button'">
      <div 
        class="skeleton rounded-xl"
        :style="{ width: width || '100px', height: height || '40px' }"
      />
    </template>
    
    <!-- 图片骨架 -->
    <template v-else-if="type === 'image'">
      <div 
        class="skeleton"
        :class="roundedClasses[rounded]"
        :style="{ width: width || '100%', height: height || '200px' }"
      />
    </template>
  </div>
</template>

<style scoped>
.skeleton {
  background: linear-gradient(
    90deg,
    #f0f0f0 25%,
    #e0e0e0 50%,
    #f0f0f0 75%
  );
  background-size: 200% 100%;
  animation: shimmer 1.5s infinite;
}

@keyframes shimmer {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}
</style>

