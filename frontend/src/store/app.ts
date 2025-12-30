import { defineStore } from 'pinia'

export type AppRole = 'buyer' | 'seller'

export const useAppStore = defineStore('app', {
  state: () => ({
    role: (localStorage.getItem('agrimatch_role') as AppRole) || ('buyer' as AppRole)
  }),
  actions: {
    setRole(role: AppRole) {
      this.role = role
      localStorage.setItem('agrimatch_role', role)
    }
  }
})


