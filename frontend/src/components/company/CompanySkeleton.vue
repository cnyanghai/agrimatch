<script setup lang="ts">
export interface CompanySkeletonProps {
  count?: number
  type?: 'card' | 'profile'
}

withDefaults(defineProps<CompanySkeletonProps>(), {
  count: 6,
  type: 'card'
})
</script>

<template>
  <div v-if="type === 'card'" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
    <div
      v-for="i in count"
      :key="i"
      class="bg-white p-6 rounded-xl border border-gray-200 flex flex-col"
    >
      <div class="flex items-center gap-4 mb-6">
        <div class="w-14 h-14 rounded-xl bg-gray-200 animate-pulse"></div>
        <div class="flex-1 space-y-2">
          <div class="h-4 bg-gray-200 rounded animate-pulse w-3/4"></div>
          <div class="h-3 bg-gray-100 rounded animate-pulse w-1/2"></div>
        </div>
      </div>
      <div class="space-y-3 mb-6 flex-1">
        <div class="flex items-start gap-3">
          <div class="w-4 h-4 bg-gray-200 rounded animate-pulse shrink-0 mt-0.5"></div>
          <div class="space-y-2 flex-1">
            <div class="h-3 bg-gray-100 rounded animate-pulse"></div>
            <div class="h-3 bg-gray-100 rounded animate-pulse w-2/3"></div>
          </div>
        </div>
      </div>
      <div class="flex items-center justify-between pt-4 border-t border-gray-50">
        <div class="h-4 bg-gray-200 rounded animate-pulse w-1/3"></div>
        <div class="h-4 bg-gray-200 rounded animate-pulse w-1/4"></div>
      </div>
    </div>
  </div>

  <div v-else-if="type === 'profile'" class="space-y-8">
    <div class="bg-slate-900 rounded-[32px] p-8 md:p-12 flex flex-col md:flex-row gap-10">
      <div class="w-32 h-32 bg-gray-700 rounded-xl animate-pulse shrink-0"></div>
      <div class="flex-1 space-y-4">
        <div class="h-10 bg-gray-700 rounded animate-pulse w-2/3"></div>
        <div class="flex gap-8">
          <div class="h-5 bg-gray-700 rounded animate-pulse w-1/4"></div>
          <div class="h-5 bg-gray-700 rounded animate-pulse w-1/4"></div>
        </div>
        <div class="flex gap-4">
          <div class="h-10 bg-gray-700 rounded-xl animate-pulse w-32"></div>
          <div class="h-10 bg-gray-700 rounded-xl animate-pulse w-32"></div>
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
      <div class="lg:col-span-2 space-y-8">
        <div class="bg-white rounded-xl p-6 space-y-4">
          <div class="flex gap-4">
            <div class="h-5 bg-gray-200 rounded animate-pulse w-24"></div>
            <div class="h-5 bg-gray-200 rounded animate-pulse w-24"></div>
          </div>
          <div v-for="i in 3" :key="i" class="flex items-center gap-6 p-4 border border-gray-100 rounded-xl">
            <div class="w-16 h-16 bg-gray-100 rounded-xl animate-pulse"></div>
            <div class="flex-1 space-y-2">
              <div class="h-4 bg-gray-200 rounded animate-pulse w-1/2"></div>
              <div class="flex gap-4">
                <div class="h-3 bg-gray-100 rounded animate-pulse w-1/4"></div>
                <div class="h-3 bg-gray-100 rounded animate-pulse w-1/4"></div>
              </div>
            </div>
            <div class="w-20 h-8 bg-gray-100 rounded animate-pulse"></div>
          </div>
        </div>
      </div>

      <div class="space-y-8">
        <div class="bg-white rounded-xl p-6 space-y-4">
          <div class="h-5 bg-gray-200 rounded animate-pulse w-1/3"></div>
          <div class="aspect-square bg-gray-100 rounded-xl animate-pulse"></div>
        </div>
        <div class="bg-slate-800 rounded-xl p-8 space-y-6">
          <div class="h-5 bg-gray-700 rounded animate-pulse w-1/3"></div>
          <div v-for="i in 3" :key="i" class="flex gap-4">
            <div class="w-1.5 h-1.5 bg-gray-600 rounded-full animate-pulse mt-2"></div>
            <div class="flex-1 space-y-2">
              <div class="h-4 bg-gray-700 rounded animate-pulse w-1/2"></div>
              <div class="h-3 bg-gray-600 rounded animate-pulse w-3/4"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>