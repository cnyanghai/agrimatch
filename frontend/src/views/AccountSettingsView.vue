<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { useAuthStore } from '../store/auth'
import { updateMe, type UserUpdateRequest } from '../api/user'
import { getMyCompany, createCompany, updateCompany, type CompanyResponse, type CompanyCreateRequest } from '../api/company'
import { User, Building2, Lock, Check, Upload, AlertTriangle, RefreshCw, Truck, ChevronRight, FileText, X, ZoomIn } from 'lucide-vue-next'
import { BaseButton } from '../components/ui'
import { regionData, codeToText } from 'element-china-area-data'
import { uploadImage } from '../api/file'

const auth = useAuthStore()
const loading = ref(false)
const activeTab = ref('user')

// 公司数据
const company = ref<CompanyResponse | null>(null)

// 用户表单
const userForm = reactive({
  displayName: '',
  phonenumber: '',
  position: '',
  birthDate: '',
  gender: 1 as number,
  bio: ''
})

// 公司表单
const companyForm = reactive({
  companyName: '',
  licenseNo: '',
  licenseImgUrl: '',
  contacts: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  address: ''
})

// 密码表单
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 快照
const userSnapshot = ref({ displayName: '', phonenumber: '', position: '', birthDate: '', gender: 1, bio: '' })
const companySnapshot = ref({ companyName: '', licenseNo: '', licenseImgUrl: '', contacts: '', phone: '', province: '', city: '', district: '', address: '' })

// 省市区级联选择值
const regionValue = ref<string[]>([])

// 查找名称对应的代码（用于回显）
function findCodesByName(province: string, city: string, district: string): string[] {
  if (!province) return []
  
  // 如果存储的是代码（之前的错误逻辑），直接返回
  if (/^\d+$/.test(province)) {
    return [province, city, district].filter(Boolean)
  }

  const result: string[] = []
  const p = regionData.find(item => item.label === province)
  if (p) {
    result.push(p.value)
    if (city) {
      const c = p.children?.find(item => item.label === city)
      if (c) {
        result.push(c.value)
        if (district) {
          const d = c.children?.find(item => item.label === district)
          if (d) {
            result.push(d.value)
          }
        }
      }
    }
  }
  return result
}

// 监听省市区选择变化，同步到 companyForm (保存中文名称而非代码)
watch(regionValue, (val) => {
  if (!val || val.length === 0) {
    companyForm.province = ''
    companyForm.city = ''
    companyForm.district = ''
    return
  }
  companyForm.province = val[0] ? (codeToText[val[0]] || '') : ''
  companyForm.city = val[1] ? (codeToText[val[1]] || '') : ''
  companyForm.district = val[2] ? (codeToText[val[2]] || '') : ''
})

// 营业执照上传
const licenseUploading = ref(false)
const licensePreview = ref(false)

async function handleLicenseUpload(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  
  // 校验文件类型
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请上传图片格式文件')
    return
  }
  // 校验文件大小（最大 10MB）
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 10MB')
    return
  }
  
  licenseUploading.value = true
  try {
    const res = await uploadImage(file)
    if (res.code === 0 && res.data?.fileUrl) {
      companyForm.licenseImgUrl = res.data.fileUrl
      ElMessage.success('营业执照上传成功')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  } finally {
    licenseUploading.value = false
    input.value = '' // 清空以便重复上传同一文件
  }
}

function removeLicenseImage() {
  companyForm.licenseImgUrl = ''
}

onMounted(async () => {
  await loadUserData()
  await loadCompanyData()
})

async function loadUserData() {
  try {
    await auth.fetchMe()
    if (auth.me) {
      userForm.displayName = auth.me.nickName || ''
      userForm.phonenumber = auth.me.phonenumber || ''
      userForm.position = auth.me.position || ''
      userForm.birthDate = auth.me.birthDate ? auth.me.birthDate.slice(0, 7) : ''
      userForm.gender = auth.me.gender || 1
      userForm.bio = auth.me.bio || ''
    }
    userSnapshot.value = { ...userForm }
  } catch (e) {
    console.error('Failed to load user data', e)
  }
}

async function loadCompanyData() {
  try {
    const res = await getMyCompany()
    if (res.code === 0 && res.data) {
      company.value = res.data
      companyForm.companyName = res.data.companyName || ''
      companyForm.licenseNo = res.data.licenseNo || ''
      companyForm.licenseImgUrl = res.data.licenseImgUrl || ''
      companyForm.contacts = res.data.contacts || ''
      companyForm.phone = res.data.phone || ''
      companyForm.province = res.data.province || ''
      companyForm.city = res.data.city || ''
      companyForm.district = res.data.district || ''
      companyForm.address = res.data.address || ''
      // 初始化省市区级联选择值（支持代码或名称）
      if (res.data.province) {
        regionValue.value = findCodesByName(res.data.province, res.data.city || '', res.data.district || '')
      }
    }
    companySnapshot.value = { ...companyForm }
  } catch (e) {
    console.error('Failed to load company data', e)
  }
}

async function saveUserInfo() {
  if (!userForm.displayName?.trim()) {
    ElMessage.warning('请输入姓名/昵称')
    return
  }
  loading.value = true
  try {
    const birthDate = userForm.birthDate?.trim() ? `${userForm.birthDate}-01` : undefined
    const req: UserUpdateRequest = {
      nickName: userForm.displayName,
      phonenumber: userForm.phonenumber,
      position: userForm.position,
      birthDate,
      gender: userForm.gender,
      bio: userForm.bio
    }
    const res = await updateMe(req)
    if (res.code === 0) {
      ElMessage.success('保存成功')
      await auth.fetchMe()
      userSnapshot.value = { ...userForm }
    } else {
      throw new Error(res.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '保存失败')
  } finally {
    loading.value = false
  }
}

async function saveCompanyInfo() {
  if (!companyForm.companyName?.trim()) {
    ElMessage.warning('请输入公司名称')
    return
  }
  loading.value = true
  try {
    const req: CompanyCreateRequest = { ...companyForm }
    let res
    if (company.value?.id) {
      res = await updateCompany(company.value.id, req)
    } else {
      res = await createCompany(req)
    }
    if (res.code === 0) {
      ElMessage.success('保存成功')
      await loadCompanyData()
      companySnapshot.value = { ...companyForm }
    } else {
      throw new Error(res.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '保存失败')
  } finally {
    loading.value = false
  }
}

async function changePassword() {
  if (!passwordForm.oldPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.warning('两次密码输入不一致')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    ElMessage.warning('密码长度不能少于6位')
    return
  }
  ElMessage.info('密码修改功能开发中...')
}

function resetUserForm() { Object.assign(userForm, userSnapshot.value) }
function resetCompanyForm() { Object.assign(companyForm, companySnapshot.value) }
function resetPasswordForm() { passwordForm.oldPassword = ''; passwordForm.newPassword = ''; passwordForm.confirmPassword = '' }

const genderOptions = [{ label: '男', value: 1 }, { label: '女', value: 2 }]

const currentName = computed(() => auth.me?.nickName || auth.me?.userName || '用户')
const currentPhone = computed(() => auth.me?.phonenumber || '未绑定手机号')
const currentPosition = computed(() => auth.me?.position || '暂无职位')
const avatarText = computed(() => {
  const n = currentName.value.trim()
  const ch = (n[0] ?? 'U').toUpperCase()
  return /\d/.test(ch) ? 'U' : ch
})

const userDirty = computed(() => JSON.stringify(userForm) !== JSON.stringify(userSnapshot.value))
const companyDirty = computed(() => JSON.stringify(companyForm) !== JSON.stringify(companySnapshot.value))
const passwordDirty = computed(() => !!(passwordForm.oldPassword || passwordForm.newPassword || passwordForm.confirmPassword))

const tabs = [
  { key: 'user', label: '基本信息', icon: User },
  { key: 'company', label: '公司信息', icon: Building2 },
  { key: 'security', label: '账户安全', icon: Lock }
]
</script>

<template>
  <div class="space-y-6">
    <!-- 页面标题 -->
    <div class="flex items-center justify-between">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">用户资料</h1>
        <p class="text-sm text-gray-500 mt-1">管理账户信息和公司资料</p>
      </div>
      <BaseButton type="secondary" size="sm" :loading="loading" @click="loadUserData(); loadCompanyData()">
        <RefreshCw class="w-4 h-4" />
        刷新
      </BaseButton>
    </div>

    <!-- 用户概览卡 -->
    <div class="bg-white rounded-xl border border-gray-200 p-6 animate-fade-in">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div class="flex items-center gap-4">
          <div class="relative">
            <div class="w-16 h-16 rounded-xl bg-gradient-to-br from-brand-500 to-brand-600 text-white flex items-center justify-center text-2xl font-bold shadow-md">
              {{ avatarText }}
            </div>
            <button
              class="absolute -bottom-1 -right-1 w-7 h-7 rounded-full bg-white border border-gray-200 shadow-sm hover:bg-gray-50 transition-all flex items-center justify-center"
              title="更换头像（暂未开放）"
            >
              <Upload class="w-3.5 h-3.5 text-gray-500" />
            </button>
          </div>
          <div>
            <div class="flex items-center gap-2 flex-wrap">
              <h2 class="text-lg font-bold text-gray-900">{{ currentName }}</h2>
              <span class="px-2 py-0.5 rounded-full text-xs font-bold bg-gray-50 text-gray-500 border border-gray-200">
                ID: {{ auth.me?.userId ?? '-' }}
              </span>
            </div>
            <p class="text-sm text-gray-500 mt-1">{{ currentPhone }} · {{ currentPosition }}</p>
          </div>
        </div>

        <!-- Tab 切换 -->
        <div class="flex items-center gap-1 bg-gray-100 rounded-xl p-1">
          <button
            v-for="tab in tabs"
            :key="tab.key"
            :class="[
              'flex items-center gap-2 px-4 py-2 rounded-lg text-sm font-bold transition-all',
              activeTab === tab.key 
                ? 'bg-white text-brand-600 shadow-sm' 
                : 'text-gray-600 hover:text-gray-900'
            ]"
            @click="activeTab = tab.key"
          >
            <component :is="tab.icon" class="w-4 h-4" />
            {{ tab.label }}
          </button>
        </div>
      </div>
    </div>

    <!-- 基本信息 -->
    <div v-show="activeTab === 'user'" class="bg-white rounded-xl border border-gray-200 overflow-hidden animate-fade-in">
      <div class="p-6 border-b border-gray-200">
        <h3 class="font-bold text-gray-900">基本信息</h3>
        <p class="text-sm text-gray-500 mt-1">修改后点击保存生效</p>
      </div>
      
      <div class="p-6 space-y-6">
        <!-- 表单 -->
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-3xl">
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
              姓名/昵称 <span class="text-red-500">*</span>
            </label>
            <input
              v-model="userForm.displayName"
              type="text"
              placeholder="请输入姓名/昵称"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">手机号码</label>
            <input
              v-model="userForm.phonenumber"
              type="text"
              placeholder="请输入手机号"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">公司职务</label>
            <input
              v-model="userForm.position"
              type="text"
              placeholder="如：采购经理、销售总监"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">出生年月</label>
            <el-date-picker
              v-model="userForm.birthDate"
              type="month"
              :locale="zhCn"
              placeholder="选择出生年月"
              format="YYYY年MM月"
              value-format="YYYY-MM"
              class="w-full neo-picker"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">性别</label>
            <div class="flex gap-4">
              <label
                v-for="opt in genderOptions"
                :key="opt.value"
                :class="[
                  'flex items-center gap-2 px-4 py-2.5 rounded-xl border-2 cursor-pointer transition-all',
                  userForm.gender === opt.value 
                    ? 'border-brand-500 bg-brand-50 text-brand-600' 
                    : 'border-gray-200 hover:border-gray-200'
                ]"
              >
                <input
                  v-model="userForm.gender"
                  type="radio"
                  :value="opt.value"
                  class="sr-only"
                />
                <span class="font-bold">{{ opt.label }}</span>
              </label>
            </div>
          </div>
        </div>
        
        <div class="max-w-3xl">
          <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">个人介绍</label>
          <textarea
            v-model="userForm.bio"
            rows="3"
            maxlength="500"
            placeholder="简单介绍一下自己，让合作伙伴更好地了解您"
            class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl focus:border-brand-500 outline-none transition-all resize-none"
          ></textarea>
          <p class="text-xs text-gray-400 mt-1 text-right">{{ userForm.bio?.length || 0 }}/500</p>
        </div>

        <!-- 保存按钮 -->
        <div v-if="userDirty" class="pt-6 border-t border-gray-200 flex justify-end gap-3">
          <BaseButton type="secondary" :disabled="loading" @click="resetUserForm">取消</BaseButton>
          <BaseButton type="primary" :loading="loading" @click="saveUserInfo">保存修改</BaseButton>
        </div>
      </div>
    </div>

    <!-- 公司信息 -->
    <div v-show="activeTab === 'company'" class="bg-white rounded-xl border border-gray-200 overflow-hidden animate-fade-in">
      <div class="p-6 border-b border-gray-200">
        <h3 class="font-bold text-gray-900">公司信息</h3>
        <p class="text-sm text-gray-500 mt-1">完善公司信息以便开展业务</p>
      </div>
      
      <div class="p-6 space-y-6">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-6 max-w-3xl">
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">
              公司名称 <span class="text-red-500">*</span>
            </label>
            <input
              v-model="companyForm.companyName"
              type="text"
              placeholder="请输入公司全称"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">统一社会信用代码</label>
            <input
              v-model="companyForm.licenseNo"
              type="text"
              placeholder="请输入统一社会信用代码"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">联系人</label>
            <input
              v-model="companyForm.contacts"
              type="text"
              placeholder="请输入公司联系人"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">联系电话</label>
            <input
              v-model="companyForm.phone"
              type="text"
              placeholder="请输入公司联系电话"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div class="md:col-span-2">
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">所在地区</label>
            <el-cascader
              v-model="regionValue"
              :options="regionData"
              :props="{ expandTrigger: 'hover' }"
              placeholder="请选择省/市/区"
              class="w-full neo-cascader"
              clearable
            />
          </div>
        </div>
        
        <div class="max-w-3xl">
          <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">详细地址</label>
          <input
            v-model="companyForm.address"
            type="text"
            placeholder="请输入详细地址（街道、门牌号等）"
            class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl focus:border-brand-500 outline-none transition-all"
          />
        </div>

        <!-- 营业执照上传 -->
        <div class="max-w-3xl">
          <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">营业执照</label>
          <div class="flex items-start gap-4">
            <!-- 上传区域 -->
            <div v-if="!companyForm.licenseImgUrl" class="relative">
              <label 
                class="flex flex-col items-center justify-center w-40 h-32 border-2 border-dashed border-gray-200 rounded-xl cursor-pointer hover:border-brand-400 hover:bg-brand-50/50 transition-all"
                :class="{ 'opacity-50 pointer-events-none': licenseUploading }"
              >
                <FileText v-if="!licenseUploading" class="w-8 h-8 text-gray-300 mb-2" />
                <div v-else class="w-8 h-8 border-2 border-brand-500 border-t-transparent rounded-full animate-spin mb-2" />
                <span class="text-xs text-gray-400 font-bold">{{ licenseUploading ? '上传中...' : '点击上传' }}</span>
                <span class="text-[10px] text-gray-300 mt-1">支持 JPG/PNG</span>
                <input 
                  type="file" 
                  class="hidden" 
                  accept="image/*"
                  @change="handleLicenseUpload"
                />
              </label>
            </div>
            
            <!-- 已上传预览 -->
            <div v-else class="relative group">
              <img 
                :src="companyForm.licenseImgUrl" 
                alt="营业执照" 
                class="w-40 h-32 object-cover rounded-xl border-2 border-gray-200"
              />
              <!-- 操作遮罩 -->
              <div class="absolute inset-0 bg-black/50 rounded-xl opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2">
                <button 
                  type="button"
                  class="w-8 h-8 bg-white/90 rounded-lg flex items-center justify-center hover:bg-white transition-colors"
                  @click="licensePreview = true"
                  title="预览"
                >
                  <ZoomIn class="w-4 h-4 text-gray-700" />
                </button>
                <button 
                  type="button"
                  class="w-8 h-8 bg-white/90 rounded-lg flex items-center justify-center hover:bg-white transition-colors"
                  @click="removeLicenseImage"
                  title="删除"
                >
                  <X class="w-4 h-4 text-red-500" />
                </button>
              </div>
              <!-- 上传成功标记 -->
              <div class="absolute -top-1 -right-1 w-5 h-5 bg-brand-500 rounded-full flex items-center justify-center">
                <Check class="w-3 h-3 text-white" />
              </div>
            </div>
            
            <!-- 说明文字 -->
            <div class="flex-1 text-xs text-gray-400 space-y-1 pt-2">
              <p>• 请上传清晰的营业执照原件照片</p>
              <p>• 图片大小不超过 10MB</p>
              <p>• 上传后仅供平台审核使用</p>
            </div>
          </div>
        </div>

        <!-- 状态提示 -->
        <div
          :class="[
            'flex items-center gap-4 p-4 rounded-xl',
            company?.id ? 'bg-brand-50' : 'bg-amber-50'
          ]"
        >
          <div
            :class="[
              'w-10 h-10 rounded-xl flex items-center justify-center',
              company?.id ? 'bg-brand-100' : 'bg-amber-100'
            ]"
          >
            <Check v-if="company?.id" class="w-5 h-5 text-brand-600" />
            <AlertTriangle v-else class="w-5 h-5 text-amber-600" />
          </div>
          <div>
            <div :class="['font-bold', company?.id ? 'text-brand-800' : 'text-amber-800']">
              {{ company?.id ? '公司信息已完善' : '公司信息未完善' }}
            </div>
            <div :class="['text-sm', company?.id ? 'text-brand-600' : 'text-amber-600']">
              {{ company?.id ? '您的公司信息已保存，可以正常使用平台功能' : '请完善公司信息，以便更好地使用平台功能' }}
            </div>
          </div>
        </div>

        <!-- 快捷管理 -->
        <div class="pt-6 border-t border-gray-200">
          <h4 class="font-bold text-gray-900 mb-4">快捷管理</h4>
          <router-link 
            to="/vehicles"
            class="flex items-center gap-4 p-4 bg-blue-50 hover:bg-blue-100 rounded-xl transition-all group"
          >
            <div class="w-12 h-12 rounded-xl bg-blue-500 text-white flex items-center justify-center">
              <Truck class="w-6 h-6" />
            </div>
            <div class="flex-1">
              <div class="font-bold text-gray-900">常用车辆管理</div>
              <div class="text-sm text-gray-500">管理公司提货车辆信息，方便快速选择</div>
            </div>
            <ChevronRight class="w-5 h-5 text-gray-400 group-hover:text-blue-500 transition-colors" />
          </router-link>
        </div>

        <!-- 保存按钮 -->
        <div v-if="companyDirty" class="pt-6 border-t border-gray-200 flex justify-end gap-3">
          <BaseButton type="secondary" :disabled="loading" @click="resetCompanyForm">取消</BaseButton>
          <BaseButton type="primary" :loading="loading" @click="saveCompanyInfo">保存修改</BaseButton>
        </div>
      </div>
    </div>

    <!-- 账户安全 -->
    <div v-show="activeTab === 'security'" class="bg-white rounded-xl border border-gray-200 overflow-hidden animate-fade-in">
      <div class="p-6 border-b border-gray-200">
        <h3 class="font-bold text-gray-900">修改密码</h3>
        <p class="text-sm text-gray-500 mt-1">定期更换密码可以保护账户安全</p>
      </div>
      
      <div class="p-6 space-y-6">
        <div class="max-w-md space-y-4">
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">原密码</label>
            <input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入原密码"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">新密码</label>
            <input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码（至少6位）"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl focus:border-brand-500 outline-none transition-all"
            />
          </div>
          <div>
            <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">确认新密码</label>
            <input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-xl focus:border-brand-500 outline-none transition-all"
            />
          </div>
        </div>

        <!-- 保存按钮 -->
        <div v-if="passwordDirty" class="pt-6 border-t border-gray-200 flex justify-end gap-3">
          <BaseButton type="secondary" :disabled="loading" @click="resetPasswordForm">取消</BaseButton>
          <BaseButton type="primary" :loading="loading" @click="changePassword">确认修改</BaseButton>
        </div>
      </div>
    </div>

    <!-- 营业执照预览弹窗 -->
    <el-dialog
      v-model="licensePreview"
      title="营业执照预览"
      width="600px"
      align-center
      class="!rounded-[24px]"
    >
      <div class="flex items-center justify-center">
        <img 
          v-if="companyForm.licenseImgUrl"
          :src="companyForm.licenseImgUrl" 
          alt="营业执照" 
          class="max-w-full max-h-[70vh] object-contain rounded-xl"
        />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 统一 Element Plus 日期选择器样式 */
:deep(.neo-picker .el-input__wrapper) {
  border: 2px solid rgb(243 244 246);
  border-radius: 12px;
  box-shadow: none;
  transition: all 0.15s ease;
}
:deep(.neo-picker .el-input__wrapper.is-focus) {
  border-color: rgb(16 185 129);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.18);
}

/* 级联选择器样式 */
:deep(.neo-cascader .el-input__wrapper) {
  border: 2px solid rgb(243 244 246);
  border-radius: 12px;
  box-shadow: none;
  transition: all 0.15s ease;
  padding: 6px 12px;
}
:deep(.neo-cascader .el-input__wrapper.is-focus) {
  border-color: rgb(16 185 129);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.18);
}
:deep(.neo-cascader .el-input__wrapper:hover) {
  border-color: rgb(209 213 219);
}
</style>
