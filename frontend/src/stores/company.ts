import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { CompanyCardResponse, CompanyProfileResponse } from '@/api/company'
import { getCompanyProfile, getCompanyDirectory, listTopCompanies } from '@/api/company'

export const useCompanyStore = defineStore('company', () => {
  const profiles = ref<Map<number, CompanyProfileResponse>>(new Map())
  const directories = ref<Map<string, { list: CompanyCardResponse[], total: number }>>(new Map())
  const loading = ref(false)
  const error = ref<string | null>(null)

  const activeProfile = computed(() => (companyId: number) => profiles.value.get(companyId))

  const activeDirectory = computed(() => (type: string, letter?: string) => {
    const key = `${type}-${letter || 'all'}`
    return directories.value.get(key)
  })

  async function loadProfile(companyId: number) {
    if (profiles.value.has(companyId)) {
      return profiles.value.get(companyId)
    }

    loading.value = true
    error.value = null

    try {
      const res = await getCompanyProfile(companyId)
      if (res.code === 0 && res.data) {
        profiles.value.set(companyId, res.data)
        return res.data
      }
    } catch (e: any) {
      error.value = e?.message || '加载公司资料失败'
      console.error('Failed to load company profile:', e)
    } finally {
      loading.value = false
    }

    return null
  }

  async function loadDirectory(type: 'supplier' | 'buyer', letter?: string, page = 1, size = 20) {
    const key = `${type}-${letter || 'all'}-${page}-${size}`

    loading.value = true
    error.value = null

    try {
      const res = await getCompanyDirectory(type, letter, page, size)
      if (res.code === 0 && res.data) {
        directories.value.set(key, res.data)
        return res.data
      }
    } catch (e: any) {
      error.value = e?.message || '加载公司目录失败'
      console.error('Failed to load company directory:', e)
    } finally {
      loading.value = false
    }

    return null
  }

  async function loadTopCompanies(type: 'supplier' | 'buyer', limit = 50) {
    loading.value = true
    error.value = null

    try {
      const res = await listTopCompanies(type, limit)
      if (res.code === 0 && res.data) {
        const key = `top-${type}-${limit}`
        directories.value.set(key, { list: res.data, total: res.data.length })
        return res.data
      }
    } catch (e: any) {
      error.value = e?.message || '加载热门公司失败'
      console.error('Failed to load top companies:', e)
    } finally {
      loading.value = false
    }

    return []
  }

  function clearProfile(companyId: number) {
    profiles.value.delete(companyId)
  }

  function clearDirectory(type: string, letter?: string) {
    const key = `${type}-${letter || 'all'}`
    directories.value.delete(key)
  }

  function clearCache() {
    profiles.value.clear()
    directories.value.clear()
  }

  function invalidateProfile(companyId: number) {
    profiles.value.delete(companyId)
  }

  function invalidateDirectory(type: string, letter?: string) {
    const keysToDelete: string[] = []
    directories.value.forEach((_, key) => {
      if (key.startsWith(`${type}-${letter || 'all'}`)) {
        keysToDelete.push(key)
      }
    })
    keysToDelete.forEach(key => directories.value.delete(key))
  }

  return {
    profiles,
    directories,
    loading,
    error,
    activeProfile,
    activeDirectory,
    loadProfile,
    loadDirectory,
    loadTopCompanies,
    clearProfile,
    clearDirectory,
    clearCache,
    invalidateProfile,
    invalidateDirectory
  }
})
