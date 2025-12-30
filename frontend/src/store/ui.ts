import { defineStore } from 'pinia'

export type AuthDialogMode = 'login' | 'register'

export interface PendingNav {
  path: string
  query?: Record<string, any>
}

export const useUiStore = defineStore('ui', {
  state: () => ({
    authDialogVisible: false,
    authDialogMode: 'login' as AuthDialogMode,
    pendingNav: null as PendingNav | null
  }),
  actions: {
    openAuthDialog(mode: AuthDialogMode = 'login', pendingNav?: PendingNav) {
      this.authDialogMode = mode
      this.pendingNav = pendingNav ?? null
      this.authDialogVisible = true
    },
    closeAuthDialog() {
      this.authDialogVisible = false
    },
    clearPendingNav() {
      this.pendingNav = null
    }
  }
})


