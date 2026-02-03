<script setup lang="ts">
import { watch } from 'vue'
import { useRouter } from 'vue-router'
import { useUiStore } from '../store/ui'

const router = useRouter()
const ui = useUiStore()

// Bridge: any code calling ui.openAuthDialog() will trigger redirect to /login
watch(() => ui.authDialogVisible, (v) => {
  if (v) {
    const pending = ui.pendingNav
    ui.closeAuthDialog()
    ui.clearPendingNav()
    const redirect = pending?.path
      ? pending.path + (pending.query ? '?' + new URLSearchParams(pending.query as any).toString() : '')
      : undefined
    router.push({ path: '/login', query: redirect ? { redirect } : undefined })
  }
})
</script>

<template>
  <!-- Bridge component: redirects to /login route -->
</template>
