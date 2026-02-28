<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from '@/composables/useToast'
import { useCompany } from '../composables/useCompany'
import { useCompanyStore } from '../stores/company'
import { followUser, unfollowUser, checkFollowStatus } from '../api/follow'
import { getCompanyContractStats, getCompanyPartners, type ContractStats, type PartnerCompany } from '../api/contract'
import { useAuthStore } from '../store/auth'
import { openChatConversation } from '../api/chat'
import PublicFooter from '../components/PublicFooter.vue'
import {
  Info, MapPin, Search,
  Heart, MessageCircle, Share2,
  Building2, User, FileText, Calendar, BarChart3,
  Briefcase, Award, Handshake, FileSignature
} from 'lucide-vue-next'
import CompanySkeleton from '../components/company/CompanySkeleton.vue'
import ProductInfoRow from '../components/ProductInfoRow.vue'

const route = useRoute()
const router = useRouter()
const companyStore = useCompanyStore()
const authStore = useAuthStore()

const { loading, error, profile, company, supplies, loadProfile } = useCompany()
const searchKeyword = ref('')
const isFollowing = ref(false)
const followLoading = ref(false)
const contractStats = ref<ContractStats>({ signedContractCount: 0, partnerCount: 0 })
const partnerCompanies = ref<PartnerCompany[]>([])

// 证书预览
const certificatePreviewVisible = ref(false)
const previewCertificateUrl = ref('')
function previewCertificate(url: string) {
  previewCertificateUrl.value = url
  certificatePreviewVisible.value = true
}

// 地图相关
const amapKey = (import.meta as any).env?.VITE_AMAP_JS_KEY as string | undefined
const amapSecurityJsCode = (import.meta as any).env?.VITE_AMAP_SECURITY_JS_CODE as string | undefined
const hasMapKey = computed(() => Boolean(amapKey && String(amapKey).trim().length > 0))
const mapRef = ref<HTMLDivElement | null>(null)
let mapInstance: any = null
let marker: any = null

function loadAmapScript(): Promise<any> {
  return new Promise((resolve, reject) => {
    const w = window as any
    if (w.AMap) return resolve(w.AMap)
    if (!hasMapKey.value) return reject(new Error('缺少 VITE_AMAP_JS_KEY'))

    if (amapSecurityJsCode && String(amapSecurityJsCode).trim().length > 0) {
      w._AMapSecurityConfig = { securityJsCode: String(amapSecurityJsCode).trim() }
    }

    const id = 'amap-js-sdk'
    const existed = document.getElementById(id) as HTMLScriptElement | null
    if (existed) {
      existed.addEventListener('load', () => resolve(w.AMap))
      existed.addEventListener('error', reject)
      return
    }
    const s = document.createElement('script')
    s.id = id
    s.type = 'text/javascript'
    s.src = `https://webapi.amap.com/maps?v=2.0&key=${encodeURIComponent(String(amapKey).trim())}`
    s.onload = () => resolve(w.AMap)
    s.onerror = () => reject(new Error('高德地图脚本加载失败'))
    document.head.appendChild(s)
  })
}

async function initMap() {
  if (!company.value?.lng || !company.value?.lat) return
  
  try {
    const AMap = await loadAmapScript()
    if (!mapRef.value) return
    
    const center = [company.value.lng, company.value.lat]
    
    if (!mapInstance) {
      mapInstance = new AMap.Map(mapRef.value, {
        zoom: 8,
        center: center,
        viewMode: '2D',
        mapStyle: 'amap://styles/whitesmoke'
      })
    } else {
      mapInstance.setCenter(center)
      mapInstance.setZoom(8)
    }
    
    // 清除旧标记
    if (marker) {
      mapInstance.remove(marker)
    }
    
    // 添加新标记
    marker = new AMap.Marker({
      position: center,
      anchor: 'bottom-center'
    })
    mapInstance.add(marker)
    
  } catch (e: any) {
    console.error('Map init failed:', e)
  }
}

// 筛选后的供应列表
const filteredSupplies = computed(() => {
  if (!searchKeyword.value.trim()) return supplies.value
  const keyword = searchKeyword.value.trim().toLowerCase()
  return supplies.value.filter(s => 
    s.categoryName?.toLowerCase().includes(keyword)
  )
})

// 解析招聘信息
const recruitments = computed(() => {
  if (!company.value?.recruitmentJson) return []
  try {
    const parsed = JSON.parse(company.value.recruitmentJson)
    return Array.isArray(parsed) ? parsed : []
  } catch (e) {
    console.error('Failed to parse recruitmentJson:', e)
    return []
  }
})

// 解析资质证书
const certificates = computed(() => {
  if (!company.value?.certificatesJson) return []
  try {
    const parsed = JSON.parse(company.value.certificatesJson)
    return Array.isArray(parsed) ? parsed : []
  } catch (e) {
    console.error('Failed to parse certificatesJson:', e)
    return []
  }
})

// 获取企业运营时长
function getOperationYears(establishDate?: string, createTime?: string) {
  // 优先使用成立日期，如果没有则使用平台创建时间
  const dateStr = establishDate || createTime
  if (!dateStr) return '-'
  
  const date = new Date(dateStr)
  const currentYear = new Date().getFullYear()
  const year = date.getFullYear()
  const years = currentYear - year
  
  if (years > 0) {
    return `${years}年`
  } else {
    return '不足1年'
  }
}

// 加载关注状态
async function loadFollowStatus() {
  if (!authStore.me || !company.value?.ownerUserId) return

  try {
    const r = await checkFollowStatus(company.value.ownerUserId)
    if (r.code === 0) {
      isFollowing.value = r.data || false
    }
  } catch (e) {
    console.error('Failed to load follow status:', e)
  }
}

// 加载公司合作统计
async function loadContractStats() {
  if (!company.value?.id) return

  try {
    const [statsRes, partnersRes] = await Promise.all([
      getCompanyContractStats(company.value.id),
      getCompanyPartners(company.value.id)
    ])
    if (statsRes.code === 0) {
      contractStats.value = statsRes.data ?? { signedContractCount: 0, partnerCount: 0 }
    }
    if (partnersRes.code === 0) {
      partnerCompanies.value = partnersRes.data ?? []
    }
  } catch (e) {
    console.error('Failed to load contract stats:', e)
  }
}

// 跳转到合作商家主页
function goToPartnerProfile(companyId: number) {
  router.push(`/company/${companyId}`)
}

// 关注/取消关注
async function toggleFollow() {
  if (!authStore.me) {
    showToast.warning('请先登录')
    return
  }
  
  if (!company.value?.ownerUserId) {
    console.error('Company ownerUserId is missing:', company.value)
    showToast.warning('该企业未关联用户，无法关注')
    return
  }
  
  console.log('Toggle follow - ownerUserId:', company.value.ownerUserId, 'current status:', isFollowing.value)
  
  followLoading.value = true
  try {
    if (isFollowing.value) {
      const r = await unfollowUser(company.value.ownerUserId)
      if (r.code === 0) {
        isFollowing.value = false
        showToast.success('已取消关注')
      } else {
        showToast.error(r.message || '取消关注失败')
      }
    } else {
      const r = await followUser(company.value.ownerUserId)
      if (r.code === 0) {
        isFollowing.value = true
        showToast.success('关注成功')
      } else {
        showToast.error(r.message || '关注失败')
      }
    }
  } catch (e: any) {
    console.error('Follow operation error:', e)
    showToast.error(e?.message || '操作失败，请稍后重试')
  } finally {
    followLoading.value = false
  }
}

// 联系商家（通用）
async function contactMerchant() {
  if (!authStore.me) {
    showToast.warning('请先登录')
    return
  }
  
  if (!company.value?.ownerUserId) {
    showToast.warning('该企业未关联用户，无法联系')
    return
  }
  
  // 如果有供应信息，使用第一个供应作为主题
  // 如果没有，使用公司ID作为subjectId，subjectType为SUPPLY（通用联系）
  const firstSupply = supplies.value.length > 0 ? supplies.value[0] : null
  const subjectId = firstSupply ? firstSupply.id : company.value.id
  const subjectType = 'SUPPLY' as const

  // 构建完整快照，聊天侧栏依赖这些字段展示产品信息
  const snapshot: Record<string, any> = { companyName: company.value.companyName }
  if (firstSupply) {
    Object.assign(snapshot, {
      productName: firstSupply.categoryName,
      categoryName: firstSupply.categoryName,
      supplyNo: firstSupply.supplyNo,
      exFactoryPrice: firstSupply.exFactoryPrice,
      quantity: firstSupply.quantity,
      remainingQuantity: firstSupply.remainingQuantity ?? firstSupply.quantity,
      shipAddress: firstSupply.shipAddress,
      deliveryMode: firstSupply.deliveryMode,
      packaging: firstSupply.packaging,
      paymentMethod: firstSupply.paymentMethod,
      invoiceType: firstSupply.invoiceType,
      storageMethod: firstSupply.storageMethod,
      paramsJson: firstSupply.paramsJson,
      priceType: firstSupply.priceType,
      basisQuotes: firstSupply.basisQuotes,
    })
  }

  try {
    const res = await openChatConversation({
      peerUserId: company.value.ownerUserId,
      subjectType: subjectType,
      subjectId: subjectId,
      subjectSnapshotJson: JSON.stringify(snapshot)
    })
    
    if (res.code === 0 && res.data) {
      // 跳转到聊天页面，使用query参数
      router.push({ path: '/chat', query: { conversationId: String(res.data) } })
    } else {
      showToast.error(res.message || '打开聊天失败')
    }
  } catch (e: any) {
    console.error('Contact merchant error:', e)
    showToast.error(e?.message || '联系商家失败，请稍后重试')
  }
}

// 针对特定供应信息联系商家
async function sendInquiry(supply: any) {
  if (!authStore.me) {
    showToast.warning('请先登录')
    return
  }
  
  if (!company.value?.ownerUserId) {
    showToast.warning('该企业未关联用户，无法联系')
    return
  }
  
  try {
    const res = await openChatConversation({
      peerUserId: company.value.ownerUserId,
      subjectType: 'SUPPLY',
      subjectId: supply.id,
      subjectSnapshotJson: JSON.stringify({
        companyName: company.value.companyName,
        productName: supply.categoryName,
        categoryName: supply.categoryName,
        supplyNo: supply.supplyNo,
        exFactoryPrice: supply.exFactoryPrice,
        quantity: supply.quantity,
        remainingQuantity: supply.remainingQuantity ?? supply.quantity,
        shipAddress: supply.shipAddress,
        deliveryMode: supply.deliveryMode,
        packaging: supply.packaging,
        paymentMethod: supply.paymentMethod,
        invoiceType: supply.invoiceType,
        storageMethod: supply.storageMethod,
        paramsJson: supply.paramsJson,
        priceType: supply.priceType,
        basisQuotes: supply.basisQuotes,
      })
    })
    
    if (res.code === 0 && res.data) {
      router.push({ path: '/chat', query: { conversationId: String(res.data) } })
    } else {
      showToast.error(res.message || '打开聊天失败')
    }
  } catch (e: any) {
    console.error('Send inquiry error:', e)
    showToast.error(e?.message || '联系商家失败，请稍后重试')
  }
}

// 分享主页
async function shareProfile() {
  const url = window.location.href
  try {
    await navigator.clipboard.writeText(url)
    showToast.success('链接已复制到剪贴板')
  } catch {
    showToast.error('复制失败，请手动复制链接')
  }
}

async function loadCompanyProfile() {
  const id = route.params.id
  if (!id) {
    error.value = '无效的公司ID'
    loading.value = false
    return
  }
  
  const companyId = Number(id)
  // 如果是查看自己的公司主页，强制重新加载而不使用缓存，确保显示最新数据
  const isOwnCompany = authStore.me?.companyId === companyId
  const cached = isOwnCompany ? null : companyStore.activeProfile(companyId)
  
  if (cached) {
    profile.value = cached
  } else {
    await loadProfile(companyId)
    if (profile.value) {
      companyStore.profiles.set(companyId, profile.value)
    }
  }
  
  // 加载关注状态和合作统计
  await Promise.all([loadFollowStatus(), loadContractStats()])

  // 初始化地图
  setTimeout(() => {
    initMap()
  }, 100)
}

onMounted(loadCompanyProfile)

// 监听路由参数变化，解决组件复用不刷新的问题
watch(() => route.params.id, (newId, oldId) => {
  if (newId && newId !== oldId) {
    loadCompanyProfile()
  }
})
</script>

<template>
  <div class="bg-neutral-50 min-h-screen flex flex-col">
    <!-- Loading State -->
    <div v-if="loading" class="w-full px-4 md:px-10 py-12">
      <CompanySkeleton type="profile" />
    </div>

    <!-- Company Profile Content -->
    <div v-else-if="company" class="flex-1">
      <!-- Header -->
      <header class="bg-white border-b border-slate-200 py-8 shadow-sm">
        <div class="w-full px-4 md:px-10">
          <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
            <div class="flex flex-col gap-3">
              <h1 class="text-2xl font-bold text-neutral-900">{{ company.companyName }}</h1>
              <div class="flex items-center gap-6 text-slate-500 text-sm flex-wrap">
                <div v-if="company.establishDate || company.createTime" class="flex items-center gap-1">
                  <Calendar class="w-4 h-4" />
                  运营时长：{{ getOperationYears(company.establishDate, company.createTime) }}
                </div>
                <div v-if="company.province || company.city" class="flex items-center gap-1">
                  <MapPin class="w-4 h-4" />
                  所在地：{{ company.province || '' }}{{ company.city || '' }}
                </div>
                <div class="flex items-center gap-1">
                  <User class="w-4 h-4" />
                  企业规模：{{ company.scale || '未公开' }}
                </div>
              </div>
            </div>
            <div class="flex items-center gap-3">
              <button
                class="flex items-center gap-2 border border-slate-200 px-3 py-2.5 rounded font-medium text-slate-500 hover:text-brand-600 hover:border-brand-300 transition"
                title="分享"
                @click="shareProfile"
              >
                <Share2 class="w-5 h-5" />
              </button>
              <button
                :disabled="followLoading"
                class="flex items-center gap-2 border px-6 py-2.5 rounded font-medium transition disabled:opacity-50 disabled:cursor-not-allowed"
                :class="isFollowing
                  ? 'border-slate-200 text-slate-600 hover:bg-slate-50'
                  : 'border-brand-600 text-brand-600 hover:bg-brand-50'"
                @click="toggleFollow"
              >
                <Heart class="w-5 h-5" :class="{ 'fill-red-500 text-red-500': isFollowing }" />
                {{ isFollowing ? '已关注' : '加入关注' }}
              </button>
              <button
                class="flex items-center gap-2 bg-brand-700 hover:bg-brand-800 text-white px-6 py-2.5 rounded font-medium transition shadow-sm"
                @click="contactMerchant"
              >
                <MessageCircle class="w-5 h-5" />
                联系商家
              </button>
            </div>
          </div>
        </div>
      </header>

      <!-- Main Content -->
      <main class="w-full px-4 md:px-10 py-8">
        <div class="grid-layout">
          <!-- Left Sidebar -->
          <aside class="space-y-6">
            <!-- 企业介绍 -->
            <section class="bg-white rounded-lg border border-slate-200 p-5 shadow-sm">
              <h3 class="text-2xl font-bold text-neutral-900 border-b border-slate-100 pb-3 mb-4 flex items-center gap-2">
                <Info class="w-5 h-5 text-brand-700" />
                企业介绍
              </h3>
              <p class="text-sm text-slate-600 leading-relaxed mb-6">
                {{ company.companyIntro || '暂无企业介绍' }}
              </p>
              <div class="space-y-4">
                <div v-if="company.licenseNo">
                  <div class="text-xs text-slate-400 uppercase font-bold tracking-wider mb-1">统一社会信用代码</div>
                  <div class="text-sm font-mono text-slate-600">{{ company.licenseNo }}</div>
                </div>
                <div v-if="company.legalPerson">
                  <div class="text-xs text-slate-400 uppercase font-bold tracking-wider mb-1">法定代表人</div>
                  <div class="text-sm font-semibold text-slate-800">{{ company.legalPerson }}</div>
                </div>
                <div>
                  <div class="text-xs text-slate-400 uppercase font-bold tracking-wider mb-1">注册资本</div>
                  <div class="text-sm font-semibold text-slate-800">{{ company.registeredCapital || '未公开' }}</div>
                </div>
                <div v-if="company.businessScope">
                  <div class="text-xs text-slate-400 uppercase font-bold tracking-wider mb-1">经营范围</div>
                  <div class="text-sm text-slate-600">{{ company.businessScope }}</div>
                </div>
              </div>
            </section>

            <!-- 人才招聘 -->
            <section v-if="recruitments.length > 0" class="bg-white rounded-lg border border-slate-200 p-5 shadow-sm">
              <h3 class="text-2xl font-bold text-neutral-900 border-b border-slate-100 pb-3 mb-4 flex items-center gap-2">
                <Briefcase class="w-5 h-5 text-brand-700" />
                人才招聘
              </h3>
              <div class="space-y-4">
                <div 
                  v-for="recruitment in recruitments" 
                  :key="recruitment.id || recruitment.position"
                  class="border border-slate-100 rounded-lg p-4 hover:border-brand-200 transition-colors space-y-3"
                >
                  <div>
                    <div class="text-xs text-slate-400 uppercase font-bold tracking-wider mb-1">岗位名称</div>
                    <div class="text-sm font-semibold text-slate-800">{{ recruitment.position }}</div>
                  </div>
                  <div>
                    <div class="text-xs text-slate-400 uppercase font-bold tracking-wider mb-1">薪资待遇</div>
                    <div class="text-sm font-semibold text-brand-600">{{ recruitment.salary || '面议' }}</div>
                  </div>
                  <div v-if="recruitment.requirements">
                    <div class="text-xs text-slate-400 uppercase font-bold tracking-wider mb-1">任职要求</div>
                    <div class="text-sm text-slate-600 leading-relaxed">{{ recruitment.requirements }}</div>
                  </div>
                </div>
              </div>
            </section>

            <!-- 资质证书 -->
            <section v-if="certificates.length > 0" class="bg-white rounded-lg border border-slate-200 p-5 shadow-sm">
              <h3 class="text-2xl font-bold text-neutral-900 border-b border-slate-100 pb-3 mb-4 flex items-center gap-2">
                <Award class="w-5 h-5 text-brand-700" />
                资质证书
              </h3>
              <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
                <div 
                  v-for="(cert, index) in certificates" 
                  :key="index"
                  class="relative group cursor-pointer"
                >
                  <img 
                    :src="cert" 
                    :alt="`资质证书 ${index + 1}`"
                    class="w-full h-32 object-cover rounded-lg border border-slate-200 hover:border-brand-500 transition-all group-hover:shadow-md"
                    @click="() => previewCertificate(cert)"
                  />
                </div>
              </div>
            </section>
          </aside>

          <!-- Main Content Area -->
          <div class="space-y-6">
            <!-- 产品名录 -->
            <section class="bg-white rounded-lg border border-slate-200 shadow-sm overflow-hidden">
              <div class="p-5 border-b border-slate-200 flex justify-between items-center">
                <h3 class="text-2xl font-bold text-neutral-900 flex items-center gap-2">
                  <FileText class="w-5 h-5 text-brand-700" />
                  产品名录 ({{ supplies.length }})
                </h3>
                <div class="flex items-center gap-2">
                  <div class="relative">
                    <input 
                      v-model="searchKeyword"
                      class="pl-8 pr-4 py-1.5 text-sm border border-slate-200 rounded-md focus:ring-brand-600 focus:border-brand-600 outline-none" 
                      placeholder="搜索产品..." 
                      type="text"
                    />
                    <Search class="w-4 h-4 absolute left-2 top-2 text-slate-400" />
                  </div>
                </div>
              </div>

              <!-- 货源列表 -->
              <div v-if="filteredSupplies.length > 0" class="p-4 space-y-3">
                <div
                  v-for="supply in filteredSupplies"
                  :key="supply.id"
                  class="bg-white rounded-xl border border-neutral-200 p-4 hover:shadow-md hover:border-brand-200 transition-all duration-200"
                >
                  <ProductInfoRow
                    :data="{
                      categoryName: supply.categoryName || '未知品类',
                      quantity: supply.quantity,
                      quantityUnit: '吨',
                      price: supply.priceType === 1 ? '基差报价' : supply.exFactoryPrice,
                      priceUnit: '吨',
                      address: supply.shipAddress,
                      packaging: supply.packaging,
                      paymentMethod: supply.paymentMethod,
                      paramsJson: supply.paramsJson,
                      expireTime: supply.expireTime
                    }"
                    type="supply"
                  >
                    <!-- 基差报价详情 -->
                    <template v-if="supply.priceType === 1 && supply.basisQuotes?.length" #extra>
                      <div class="mt-2 flex flex-wrap gap-2">
                        <div
                          v-for="bq in (supply.basisQuotes || []).slice(0, 3)"
                          :key="bq.id"
                          class="inline-flex items-center gap-1.5 px-2 py-1 bg-warning-50 border border-warning-200 rounded-lg text-xs"
                        >
                          <span class="font-bold text-neutral-700">{{ bq.contractName || bq.contractCode }}</span>
                          <span :class="bq.basisPrice >= 0 ? 'text-red-500' : 'text-green-500'" class="font-bold">
                            {{ bq.basisPrice >= 0 ? '+' : '' }}{{ bq.basisPrice }}
                          </span>
                          <span class="text-neutral-400">·</span>
                          <span class="font-medium text-neutral-600">{{ bq.remainingQty ?? bq.availableQty }}吨</span>
                        </div>
                      </div>
                    </template>
                    <template #actions>
                      <button
                        class="flex items-center gap-1.5 px-3 py-1.5 bg-brand-600 hover:bg-brand-700 text-white text-xs font-medium rounded-lg transition-all active:scale-95"
                        @click="sendInquiry(supply)"
                      >
                        <MessageCircle class="w-3.5 h-3.5" />
                        联系商家
                      </button>
                    </template>
                  </ProductInfoRow>
                </div>
              </div>

              <!-- 空状态 -->
              <div v-else class="p-12 text-center">
                <div class="w-16 h-16 bg-neutral-50 rounded-full flex items-center justify-center mx-auto mb-4">
                  <FileText class="w-8 h-8 text-neutral-300" />
                </div>
                <h3 class="text-2xl font-bold text-neutral-900 mb-1">
                  {{ searchKeyword ? '未找到匹配的产品' : '暂无在售货源' }}
                </h3>
                <p class="text-xs text-neutral-500">
                  {{ searchKeyword ? '请尝试其他搜索词' : '该企业暂时没有在售的货源信息' }}
                </p>
              </div>

              <!-- 查看更多 -->
              <div v-if="filteredSupplies.length > 5" class="p-4 bg-slate-50 border-t border-slate-200 text-center">
                <button class="text-slate-500 hover:text-brand-700 text-sm font-medium flex items-center justify-center gap-1 w-full">
                  查看更多产品
                </button>
              </div>
            </section>
          </div>

          <!-- Right Sidebar -->
          <aside class="space-y-6">
            <!-- 资信仪表盘 -->
            <section class="bg-white rounded-lg border border-slate-200 p-5 shadow-sm">
              <h3 class="text-2xl font-bold text-neutral-900 border-b border-slate-100 pb-3 mb-4 flex items-center gap-2">
                <BarChart3 class="w-5 h-5 text-brand-700" />
                资信仪表盘
              </h3>
              <div class="space-y-6">
                <!-- 统计数据 -->
                <div class="grid grid-cols-2 gap-4">
                  <div class="bg-slate-50 p-4 rounded-lg text-center">
                    <div class="flex items-center justify-center gap-1.5 text-xs text-slate-500 mb-2">
                      <FileSignature class="w-4 h-4" />
                      累计签订合同
                    </div>
                    <div class="text-2xl font-bold text-brand-700">{{ contractStats.signedContractCount }}</div>
                    <div class="text-xs text-slate-400 mt-1">份</div>
                  </div>
                  <div class="bg-slate-50 p-4 rounded-lg text-center">
                    <div class="flex items-center justify-center gap-1.5 text-xs text-slate-500 mb-2">
                      <Handshake class="w-4 h-4" />
                      合作商户
                    </div>
                    <div class="text-2xl font-bold text-brand-700">{{ contractStats.partnerCount }}</div>
                    <div class="text-xs text-slate-400 mt-1">家</div>
                  </div>
                </div>

                <!-- 合作商家列表 -->
                <div class="border-t border-slate-100 pt-4">
                  <div class="flex items-center justify-between mb-3">
                    <span class="text-sm font-medium text-slate-700 flex items-center gap-1.5">
                      <Building2 class="w-4 h-4 text-slate-400" />
                      合作商家
                    </span>
                    <span class="text-xs text-slate-400">{{ partnerCompanies.length }}家</span>
                  </div>
                  <div v-if="partnerCompanies.length > 0" class="space-y-2 max-h-64 overflow-y-auto">
                    <div
                      v-for="partner in partnerCompanies"
                      :key="partner.companyId"
                      class="flex items-center justify-between p-2.5 bg-slate-50 rounded-lg hover:bg-brand-50 cursor-pointer transition-colors group"
                      @click="goToPartnerProfile(partner.companyId)"
                    >
                      <div class="flex items-center gap-2 min-w-0">
                        <div class="w-8 h-8 bg-brand-100 rounded-full flex items-center justify-center shrink-0">
                          <Building2 class="w-4 h-4 text-brand-600" />
                        </div>
                        <div class="min-w-0">
                          <div class="text-sm font-medium text-slate-800 truncate group-hover:text-brand-700">
                            {{ partner.companyName }}
                          </div>
                          <div class="text-xs text-slate-400">
                            合作 {{ partner.contractCount }} 次
                          </div>
                        </div>
                      </div>
                      <div class="text-xs text-slate-400 shrink-0">
                        ¥{{ (partner.totalAmount / 10000).toFixed(1) }}万
                      </div>
                    </div>
                  </div>
                  <div v-else class="text-xs text-slate-400 text-center py-6">
                    <Handshake class="w-8 h-8 mx-auto mb-2 text-slate-200" />
                    暂无合作商家
                  </div>
                </div>
              </div>
            </section>

            <!-- 厂区位置 -->
            <section class="bg-white rounded-lg border border-slate-200 p-5 shadow-sm overflow-hidden">
              <h3 class="text-2xl font-bold text-neutral-900 border-b border-slate-100 pb-3 mb-4 flex items-center gap-2">
                <MapPin class="w-5 h-5 text-brand-700" />
                厂区位置
              </h3>
              <div class="h-48 rounded bg-slate-100 relative overflow-hidden border border-slate-100">
                <div v-if="!company.lng || !company.lat" class="absolute inset-0 flex items-center justify-center bg-slate-50 text-slate-400 text-xs">
                  暂无地理位置信息
                </div>
                <div v-else ref="mapRef" class="w-full h-full"></div>
              </div>
              <div v-if="company.address" class="mt-4">
                <p class="text-xs text-slate-500 flex items-start gap-1">
                  <MapPin class="w-4 h-4 mt-0.5 shrink-0" />
                  {{ company.address }}
                </p>
              </div>
            </section>
          </aside>
        </div>
      </main>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="flex-1 max-w-7xl mx-auto px-4 py-20 text-center">
      <h3 class="text-2xl font-bold text-neutral-900 mb-2">加载失败</h3>
      <p class="text-sm text-neutral-500">{{ error }}</p>
    </div>

    <!-- 项目底部组件 -->
    <PublicFooter />

    <!-- 证书预览对话框 -->
    <el-dialog
      v-model="certificatePreviewVisible"
      title="资质证书预览"
      width="80%"
      :before-close="() => { certificatePreviewVisible = false }"
    >
      <div class="flex justify-center">
        <img 
          :src="previewCertificateUrl" 
          alt="资质证书"
          class="max-w-full max-h-[70vh] object-contain rounded-lg"
        />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
.grid-layout {
  display: grid;
  grid-template-columns: 280px 1fr 320px;
  gap: 1.5rem;
}

@media (max-width: 1200px) {
  .grid-layout {
    grid-template-columns: 1fr;
  }
}
</style>

