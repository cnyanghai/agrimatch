<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'

const pageUrl = ref('')

onLoad((query) => {
  if (query?.url) {
    pageUrl.value = decodeURIComponent(query.url)
  }
})
</script>

<template>
  <web-view v-if="pageUrl" :src="pageUrl" />
</template>
