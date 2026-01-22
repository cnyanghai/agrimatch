import { defineStore } from 'pinia'
import type { CompanyCardResponse, CompanyProfileResponse } from '../api/company'

const CACHE_DURATION = 5 * 60 * 1000

interface CacheEntry<T> {
  data: T
  timestamp: number
}

interface CompanyState {
  profiles: Map<number, CacheEntry<CompanyProfileResponse>>
  directories: Map<string, CacheEntry<{ companies: CompanyCardResponse[]; total: number }>>
  topSuppliers: CacheEntry<CompanyCardResponse[]> | null
  topBuyers: CacheEntry<CompanyCardResponse[]> | null
  loading: boolean
  error: string | null
}

export const useCompanyStore = defineStore('company', {
  state: (): CompanyState => ({
    profiles: new Map(),
    directories: new Map(),
    topSuppliers: null,
    topBuyers: null,
    loading: false,
    error: null
  }),

  getters: {
    isExpired: () => (entry: CacheEntry<any> | null): boolean => {
      if (!entry) return true
      return Date.now() - entry.timestamp > CACHE_DURATION
    },

    getProfile: (state) => (id: number): CompanyProfileResponse | null => {
      const entry = state.profiles.get(id)
      if (!entry) return null
      if (Date.now() - entry.timestamp > CACHE_DURATION) {
        state.profiles.delete(id)
        return null
      }
      return entry.data
    },

    getDirectory: (state) => (key: string): { companies: CompanyCardResponse[]; total: number } | null => {
      const entry = state.directories.get(key)
      if (!entry) return null
      if (Date.now() - entry.timestamp > CACHE_DURATION) {
        state.directories.delete(key)
        return null
      }
      return entry.data
    },

    getTopSuppliers: (state): CompanyCardResponse[] | null => {
      if (!state.topSuppliers) return null
      if (Date.now() - state.topSuppliers.timestamp > CACHE_DURATION) {
        state.topSuppliers = null
        return null
      }
      return state.topSuppliers.data
    },

    getTopBuyers: (state): CompanyCardResponse[] | null => {
      if (!state.topBuyers) return null
      if (Date.now() - state.topBuyers.timestamp > CACHE_DURATION) {
        state.topBuyers = null
        return null
      }
      return state.topBuyers.data
    }
  },

  actions: {
    setLoading(loading: boolean) {
      this.loading = loading
    },

    setError(error: string | null) {
      this.error = error
    },

    setProfile(id: number, data: CompanyProfileResponse) {
      this.profiles.set(id, {
        data,
        timestamp: Date.now()
      })
    },

    setDirectory(key: string, data: { companies: CompanyCardResponse[]; total: number }) {
      this.directories.set(key, {
        data,
        timestamp: Date.now()
      })
    },

    setTopSuppliers(data: CompanyCardResponse[]) {
      this.topSuppliers = {
        data,
        timestamp: Date.now()
      }
    },

    setTopBuyers(data: CompanyCardResponse[]) {
      this.topBuyers = {
        data,
        timestamp: Date.now()
      }
    },

    clearProfileCache(id?: number) {
      if (id) {
        this.profiles.delete(id)
      } else {
        this.profiles.clear()
      }
    },

    clearDirectoryCache(key?: string) {
      if (key) {
        this.directories.delete(key)
      } else {
        this.directories.clear()
      }
    },

    clearTopSuppliersCache() {
      this.topSuppliers = null
    },

    clearTopBuyersCache() {
      this.topBuyers = null
    },

    clearAllCache() {
      this.profiles.clear()
      this.directories.clear()
      this.topSuppliers = null
      this.topBuyers = null
    }
  }
})
