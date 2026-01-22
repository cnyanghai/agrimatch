<script setup lang="ts">
import { onMounted, ref, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useCompany } from '../composables/useCompany'
import { useCompanyStore } from '../stores/company'
import { followUser, unfollowUser, checkFollowStatus } from '../api/follow'
import { useAuthStore } from '../store/auth'
import { openChatConversation } from '../api/chat'
import PublicFooter from '../components/PublicFooter.vue'
import { 
  Info, Shield, MapPin, Search, TrendingUp, TrendingDown, 
  Minus, Circle, Heart, MessageCircle, Share2, Factory,
  Building2, User, FileText, Calendar, Star, BarChart3,
  Briefcase, Award, Package, Truck, Tag
} from 'lucide-vue-next'
import CompanySkeleton from '../components/company/CompanySkeleton.vue'

const route = useRoute()
const router = useRouter()
const companyStore = useCompanyStore()
const authStore = useAuthStore()

const { loading, error, profile, company, supplies, requirements, loadProfile } = useCompany()
const searchKeyword = ref('')
const isFollowing = ref(false)
const followLoading = ref(false)

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
    s.categoryName?.toLowerCase().includes(keyword) ||
    s.origin?.toLowerCase().includes(keyword)
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

// 获取企业规模
function getCompanyScale() {
  // 这里可以根据实际业务逻辑判断
  return '100人以上'
}

// 格式化价格显示
function formatPrice(supply: any) {
  if (supply.priceType === 1) {
    // 基差报价模式
    if (supply.basisQuotes && supply.basisQuotes.length > 0) {
      const quotes = supply.basisQuotes.slice(0, 2) // 最多显示2个合约
      return quotes.map((q: any) => {
        const price = q.referencePrice ? `¥${q.referencePrice.toFixed(0)}` : '待定'
        return `${q.contractName || q.contractCode}: ${price}`
      }).join(' / ')
    }
    return '基差报价'
  } else {
    // 现货一口价模式
    if (supply.exFactoryPrice != null) {
      return `¥${supply.exFactoryPrice.toFixed(2)}/吨`
    }
    return '面议'
  }
}

// 解析规格参数
function getParamsList(paramsJson?: string): Array<{ key: string; value: string }> {
  if (!paramsJson) return []
  try {
    const parsed = JSON.parse(paramsJson)
    if (typeof parsed !== 'object' || parsed === null) return []
    
    // 支持新格式：{"参数名": "值"}
    const params: Array<{ key: string; value: string }> = []
    Object.entries(parsed).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== '') {
        params.push({ key, value: String(value) })
      }
    })
    return params
  } catch (e) {
    console.error('Failed to parse paramsJson:', e)
    return []
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

// 关注/取消关注
async function toggleFollow() {
  if (!authStore.me) {
    ElMessage.warning('请先登录')
    return
  }
  
  if (!company.value?.ownerUserId) {
    console.error('Company ownerUserId is missing:', company.value)
    ElMessage.warning('该企业未关联用户，无法关注')
    return
  }
  
  console.log('Toggle follow - ownerUserId:', company.value.ownerUserId, 'current status:', isFollowing.value)
  
  followLoading.value = true
  try {
    if (isFollowing.value) {
      const r = await unfollowUser(company.value.ownerUserId)
      if (r.code === 0) {
        isFollowing.value = false
        ElMessage.success('已取消关注')
      } else {
        ElMessage.error(r.message || '取消关注失败')
      }
    } else {
      const r = await followUser(company.value.ownerUserId)
      if (r.code === 0) {
        isFollowing.value = true
        ElMessage.success('关注成功')
      } else {
        ElMessage.error(r.message || '关注失败')
      }
    }
  } catch (e: any) {
    console.error('Follow operation error:', e)
    ElMessage.error(e?.message || '操作失败，请稍后重试')
  } finally {
    followLoading.value = false
  }
}

// 联系商家（通用）
async function contactMerchant() {
  if (!authStore.me) {
    ElMessage.warning('请先登录')
    return
  }
  
  if (!company.value?.ownerUserId) {
    ElMessage.warning('该企业未关联用户，无法联系')
    return
  }
  
  // 如果有供应信息，使用第一个供应作为主题
  // 如果没有，使用公司ID作为subjectId，subjectType为SUPPLY（通用联系）
  const subjectId = supplies.value.length > 0 ? supplies.value[0].id : company.value.id
  const subjectType = 'SUPPLY' as const
  
  try {
    const res = await openChatConversation({
      peerUserId: company.value.ownerUserId,
      subjectType: subjectType,
      subjectId: subjectId,
      subjectSnapshotJson: company.value.companyName ? JSON.stringify({ companyName: company.value.companyName }) : undefined
    })
    
    if (res.code === 0 && res.data) {
      // 跳转到聊天页面，使用query参数
      router.push({ path: '/chat', query: { conversationId: String(res.data) } })
    } else {
      ElMessage.error(res.message || '打开聊天失败')
    }
  } catch (e: any) {
    console.error('Contact merchant error:', e)
    ElMessage.error(e?.message || '联系商家失败，请稍后重试')
  }
}

// 针对特定供应信息联系商家
async function sendInquiry(supply: any) {
  if (!authStore.me) {
    ElMessage.warning('请先登录')
    return
  }
  
  if (!company.value?.ownerUserId) {
    ElMessage.warning('该企业未关联用户，无法联系')
    return
  }
  
  try {
    const res = await openChatConversation({
      peerUserId: company.value.ownerUserId,
      subjectType: 'SUPPLY',
      subjectId: supply.id,
      subjectSnapshotJson: JSON.stringify({
        companyName: company.value.companyName,
        categoryName: supply.categoryName,
        supplyNo: supply.supplyNo
      })
    })
    
    if (res.code === 0 && res.data) {
      router.push({ path: '/chat', query: { conversationId: String(res.data) } })
    } else {
      ElMessage.error(res.message || '打开聊天失败')
    }
  } catch (e: any) {
    console.error('Send inquiry error:', e)
    ElMessage.error(e?.message || '联系商家失败，请稍后重试')
  }
}

// 分享主页
function shareProfile() {
  ElMessage.info('分享功能开发中')
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
  
  // 加载关注状态
  await loadFollowStatus()
  
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
  <div class="bg-gray-50 min-h-screen flex flex-col">
    <!-- Loading State -->
    <div v-if="loading" class="max-w-[1440px] mx-auto px-4 sm:px-6 lg:px-8 py-12">
      <CompanySkeleton type="profile" />
    </div>

    <!-- Company Profile Content -->
    <div v-else-if="company" class="flex-1">
      <!-- Header -->
      <header class="bg-white border-b border-slate-200 py-8 shadow-sm">
        <div class="max-w-[1440px] mx-auto px-4 sm:px-6 lg:px-8">
          <div class="flex flex-col md:flex-row justify-between items-start md:items-center gap-6">
            <div class="flex flex-col gap-3">
              <div class="flex items-center gap-4 flex-wrap">
                <h1 class="text-3xl font-bold text-slate-900">{{ company.companyName }}</h1>
                <div class="flex gap-2">
                  <span class="flex items-center gap-1 bg-green-100 text-green-700 text-xs font-bold px-2 py-1 rounded">
                    <Shield class="w-3 h-3" />
                    资质已核验
                  </span>
                  <span v-if="company.businessScope" class="flex items-center gap-1 bg-blue-100 text-blue-700 text-xs font-bold px-2 py-1 rounded">
                    <Star class="w-3 h-3" />
                    核心供应商
                  </span>
                </div>
              </div>
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
      <main class="max-w-[1440px] mx-auto px-4 sm:px-6 lg:px-8 py-8">
        <div class="grid-layout">
          <!-- Left Sidebar -->
          <aside class="space-y-6">
            <!-- 企业介绍 -->
            <section class="bg-white rounded-lg border border-slate-200 p-5 shadow-sm">
              <h3 class="font-bold text-slate-900 border-b border-slate-100 pb-3 mb-4 flex items-center gap-2">
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
              <h3 class="font-bold text-slate-900 border-b border-slate-100 pb-3 mb-4 flex items-center gap-2">
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
              <h3 class="font-bold text-slate-900 border-b border-slate-100 pb-3 mb-4 flex items-center gap-2">
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
                <h3 class="font-bold text-slate-900 flex items-center gap-2">
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
              <div v-if="filteredSupplies.length > 0" class="overflow-x-auto">
                <table class="w-full text-left border-collapse">
                  <thead class="bg-slate-50 text-slate-500 text-xs font-bold uppercase tracking-wider">
                    <tr>
                      <th class="px-6 py-4">基础信息</th>
                      <th class="px-6 py-4">规格参数</th>
                      <th class="px-6 py-4">物流交付</th>
                      <th class="px-6 py-4">操作</th>
                    </tr>
                  </thead>
                  <tbody class="divide-y divide-slate-100">
                    <tr 
                      v-for="supply in filteredSupplies"
                      :key="supply.id"
                      class="hover:bg-slate-50 transition"
                    >
                      <!-- 基础信息列 -->
                      <td class="px-6 py-4">
                        <div class="space-y-2">
                          <div>
                            <div class="font-semibold text-slate-900 text-base">{{ supply.categoryName }}</div>
                            <div v-if="supply.origin" class="text-xs text-slate-500 mt-0.5">产地：{{ supply.origin }}</div>
                          </div>
                          <div class="flex items-center gap-4 text-sm">
                            <div class="flex items-center gap-1.5">
                              <Tag class="w-4 h-4 text-brand-600" />
                              <span class="font-bold text-brand-600">{{ formatPrice(supply) }}</span>
                            </div>
                            <div class="flex items-center gap-1.5">
                              <Package class="w-4 h-4 text-slate-500" />
                              <span class="text-slate-700">{{ supply.quantity || 0 }}吨</span>
                            </div>
                          </div>
                        </div>
                      </td>
                      <!-- 规格参数列 -->
                      <td class="px-6 py-4">
                        <div v-if="getParamsList(supply.paramsJson).length > 0" class="flex flex-wrap gap-1.5">
                          <span
                            v-for="param in getParamsList(supply.paramsJson)"
                            :key="param.key"
                            class="inline-flex items-center gap-1 px-2 py-1 bg-slate-50 border border-slate-200 rounded text-xs text-slate-700"
                          >
                            <span class="font-medium">{{ param.key }}:</span>
                            <span>{{ param.value }}</span>
                          </span>
                        </div>
                        <span v-else class="text-xs text-slate-400">暂无参数</span>
                      </td>
                      <!-- 物流交付列 -->
                      <td class="px-6 py-4">
                        <div class="space-y-1.5 text-sm">
                          <div v-if="supply.deliveryMode" class="flex items-center gap-1.5 text-slate-700">
                            <Truck class="w-4 h-4 text-slate-500" />
                            <span>{{ supply.deliveryMode }}</span>
                          </div>
                          <div v-if="supply.packaging" class="flex items-center gap-1.5 text-slate-700">
                            <Package class="w-4 h-4 text-slate-500" />
                            <span>{{ supply.packaging }}</span>
                          </div>
                          <div v-if="supply.shipAddress" class="flex items-center gap-1.5 text-slate-600 text-xs">
                            <MapPin class="w-3 h-3 text-slate-400" />
                            <span class="truncate max-w-[200px]">{{ supply.shipAddress }}</span>
                          </div>
                        </div>
                      </td>
                      <!-- 操作列 -->
                      <td class="px-6 py-4">
                        <button 
                          class="flex items-center gap-1.5 px-4 py-2 bg-brand-600 hover:bg-brand-700 text-white text-sm font-medium rounded-lg transition-all active:scale-95"
                          @click="sendInquiry(supply)"
                        >
                          <MessageCircle class="w-4 h-4" />
                          联系商家
                        </button>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

              <!-- 空状态 -->
              <div v-else class="p-12 text-center">
                <div class="w-16 h-16 bg-gray-50 rounded-full flex items-center justify-center mx-auto mb-4">
                  <FileText class="w-8 h-8 text-gray-300" />
                </div>
                <h3 class="text-sm font-bold text-gray-900 mb-1">
                  {{ searchKeyword ? '未找到匹配的产品' : '暂无在售货源' }}
                </h3>
                <p class="text-xs text-gray-500">
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
              <h3 class="font-bold text-slate-900 border-b border-slate-100 pb-3 mb-4 flex items-center gap-2">
                <BarChart3 class="w-5 h-5 text-brand-700" />
                资信仪表盘
              </h3>
              <div class="space-y-6">
                <div>
                  <div class="flex justify-between items-end mb-2">
                    <span class="text-sm text-slate-500">合同履约率</span>
                    <span class="text-lg font-bold text-brand-700">99.8%</span>
                  </div>
                  <div class="w-full bg-slate-100 rounded-full h-2">
                    <div class="bg-brand-700 h-2 rounded-full" style="width: 99.8%"></div>
                  </div>
                </div>
                <div class="grid grid-cols-2 gap-4">
                  <div class="bg-slate-50 p-3 rounded text-center">
                    <div class="text-xs text-slate-400 mb-1">平均发货时效</div>
                    <div class="text-base font-bold text-slate-800">1.2天</div>
                  </div>
                  <div class="bg-slate-50 p-3 rounded text-center">
                    <div class="text-xs text-slate-400 mb-1">买家好评率</div>
                    <div class="text-base font-bold text-slate-800">4.9/5.0</div>
                  </div>
                </div>
                <div class="border-t border-slate-100 pt-4">
                  <div class="flex items-center justify-between mb-4">
                    <span class="text-sm font-medium text-slate-700">买家评价 (218)</span>
                    <a class="text-brand-700 text-xs hover:underline" href="#">查看全部</a>
                  </div>
                  <div class="space-y-3">
                    <div class="text-xs p-2 bg-slate-50 rounded">
                      <div class="flex items-center gap-1 mb-1">
                        <Star class="w-3 h-3 text-amber-500 fill-amber-500" />
                        <Star class="w-3 h-3 text-amber-500 fill-amber-500" />
                        <Star class="w-3 h-3 text-amber-500 fill-amber-500" />
                        <Star class="w-3 h-3 text-amber-500 fill-amber-500" />
                        <Star class="w-3 h-3 text-amber-500 fill-amber-500" />
                      </div>
                      <p class="text-slate-600">"长期合作伙伴，质量非常稳定，物流速度快。"</p>
                      <div class="text-[10px] text-slate-400 mt-1">某大型饲料厂采购部 · 2天前</div>
                    </div>
                  </div>
                </div>
              </div>
            </section>

            <!-- 厂区位置 -->
            <section class="bg-white rounded-lg border border-slate-200 p-5 shadow-sm overflow-hidden">
              <h3 class="font-bold text-slate-900 border-b border-slate-100 pb-3 mb-4 flex items-center gap-2">
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
      <h3 class="text-lg font-bold text-gray-900 mb-2">加载失败</h3>
      <p class="text-sm text-gray-500">{{ error }}</p>
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

