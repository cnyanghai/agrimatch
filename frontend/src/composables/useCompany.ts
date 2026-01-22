import { ref, computed } from 'vue'
import {
  getCompanyProfile,
  getCompanyDirectory,
  searchCompanies,
  type CompanyProfileResponse,
  type CompanyCardResponse,
  type CompanyBriefResponse
} from '../api/company'

export function useCompany() {
  const loading = ref(false)
  const error = ref<string | null>(null)
  const profile = ref<CompanyProfileResponse | null>(null)
  const companies = ref<CompanyCardResponse[]>([])
  const total = ref(0)
  const searchResults = ref<CompanyBriefResponse[]>([])

  const company = computed(() => profile.value?.company)
  const supplies = computed(() => profile.value?.supplies ?? [])
  const requirements = computed(() => profile.value?.requirements ?? [])

  async function loadProfile(id: number | string) {
    loading.value = true
    error.value = null
    try {
      const res = await getCompanyProfile(id)
      if (res.code === 0 && res.data) {
        profile.value = res.data
      }
    } catch (e: any) {
      error.value = e?.message || '加载公司资料失败'
      console.error('Failed to load company profile:', e)
    } finally {
      loading.value = false
    }
  }

  async function loadDirectory(
    type: 'supplier' | 'buyer',
    letter?: string,
    page = 1,
    size = 24
  ) {
    loading.value = true
    error.value = null
    try {
      const res = await getCompanyDirectory(type, letter, page, size)
      if (res.code === 0 && res.data) {
        companies.value = res.data.list
        total.value = res.data.total
      }
    } catch (e: any) {
      error.value = e?.message || '加载公司目录失败'
      console.error('Failed to load company directory:', e)
    } finally {
      loading.value = false
    }
  }

  async function searchCompaniesByKeyword(keyword: string, limit = 20) {
    if (!keyword.trim()) {
      searchResults.value = []
      return
    }

    loading.value = true
    error.value = null
    try {
      const res = await searchCompanies(keyword, limit)
      if (res.code === 0 && res.data) {
        searchResults.value = res.data
      }
    } catch (e: any) {
      error.value = e?.message || '搜索公司失败'
      console.error('Failed to search companies:', e)
    } finally {
      loading.value = false
    }
  }

  function clearProfile() {
    profile.value = null
    error.value = null
  }

  function clearCompanies() {
    companies.value = []
    total.value = 0
    error.value = null
  }

  function clearSearchResults() {
    searchResults.value = []
    error.value = null
  }

  return {
    loading,
    error,
    profile,
    company,
    supplies,
    requirements,
    companies,
    total,
    searchResults,
    loadProfile,
    loadDirectory,
    searchCompaniesByKeyword,
    clearProfile,
    clearCompanies,
    clearSearchResults
  }
}
