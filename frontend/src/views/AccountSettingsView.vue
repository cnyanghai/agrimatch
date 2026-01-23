<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { useAuthStore } from '../store/auth'
import { updateMe, type UserUpdateRequest, getLoginLogs, type LoginLogResponse } from '../api/user'
import { getMyCompany, createCompany, updateCompany, type CompanyResponse, type CompanyCreateRequest } from '../api/company'
import { User, Building2, Lock, Check, Upload, AlertTriangle, RefreshCw, Truck, ChevronRight, FileText, X, ZoomIn, Award, Calendar, Users, Plus, Trash2, Edit2, Star, ShieldCheck, History, Monitor, MapPin } from 'lucide-vue-next'
import { BaseButton } from '../components/ui'
import { regionData, codeToText } from 'element-china-area-data'
import { uploadImage } from '../api/file'
import { listVehicles, createVehicle, updateVehicle, deleteVehicle, setDefaultVehicle, type VehicleResponse, type VehicleCreateRequest } from '../api/vehicle'

const auth = useAuthStore()
const loading = ref(false)
const activeTab = ref('profile')

// 公司数据
const company = ref<CompanyResponse | null>(null)

// 用户表单
const userForm = reactive({
  displayName: '',
  phonenumber: '',
  position: '',
  birthDate: '',
  gender: 1 as number,
  bio: '',
  avatar: ''
})

// 公司表单
const companyForm = reactive({
  companyName: '',
  licenseNo: '',
  licenseImgUrl: '',
  legalPerson: '',
  businessScope: '',
  registeredCapital: '',
  establishDate: '',
  scale: '',
  companyIntro: '',
  contacts: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  address: '',
  announcementsJson: '',
  recruitmentJson: '',
  certificatesJson: ''
})

// 密码表单
const passwordForm = reactive({
  newPassword: '',
  confirmPassword: ''
})

// 快照
const userSnapshot = ref({ displayName: '', phonenumber: '', position: '', birthDate: '', gender: 1, bio: '', avatar: '' })
const companySnapshot = ref({ companyName: '', licenseNo: '', licenseImgUrl: '', legalPerson: '', businessScope: '', registeredCapital: '', establishDate: '', scale: '', companyIntro: '', contacts: '', phone: '', province: '', city: '', district: '', address: '', announcementsJson: '', recruitmentJson: '', certificatesJson: '' })

// 车辆管理
const vehicles = ref<VehicleResponse[]>([])
const vehicleDialogVisible = ref(false)
const vehicleDialogMode = ref<'create' | 'edit'>('create')
const editingVehicleId = ref<number | null>(null)
const vehicleForm = reactive<VehicleCreateRequest>({
  driverName: '',
  driverIdCard: '',
  plateNumber: '',
  driverPhone: '',
  vehicleType: '',
  remark: ''
})


// 招聘列表
const recruitments = ref<Array<{ id: string, position: string, requirements: string, salary: string }>>([])
const recruitmentDialogVisible = ref(false)
const editingRecruitmentId = ref<string | null>(null)
const recruitmentForm = reactive({ position: '', requirements: '', salary: '' })

// 资质证书列表
const certificates = ref<string[]>([])
const certificateUploading = ref(false)

// 登录日志
const loginLogs = ref<LoginLogResponse[]>([])
const loginLogsLoading = ref(false)

async function fetchLoginLogs() {
  if (loginLogsLoading.value) return
  loginLogsLoading.value = true
  try {
    const res = await getLoginLogs()
    if (res.code === 0) {
      loginLogs.value = res.data ?? []
    } else {
      console.error('Failed to fetch login logs:', res.message)
    }
  } catch (e) {
    console.error('API Error when fetching login logs:', e)
  } finally {
    loginLogsLoading.value = false
  }
}

watch(activeTab, (newTab) => {
  if (newTab === 'security') {
    fetchLoginLogs()
  }
}, { immediate: true })

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
  await loadVehicles()
})

async function loadUserData() {
  try {
    await auth.fetchMe()
    if (auth.me) {
      userForm.displayName = auth.me.nickName || ''
      userForm.phonenumber = auth.me.phonenumber || ''
      userForm.position = auth.me.position || ''
      // 后端返回 YYYY-MM-DD 格式字符串，直接使用
      userForm.birthDate = auth.me.birthDate || ''
      userForm.gender = auth.me.gender || 1
      userForm.bio = auth.me.bio || ''
      userForm.avatar = auth.me.avatar || ''
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
      companyForm.legalPerson = res.data.legalPerson || ''
      companyForm.businessScope = res.data.businessScope || ''
      companyForm.registeredCapital = res.data.registeredCapital || ''
      companyForm.establishDate = res.data.establishDate || ''
      companyForm.scale = res.data.scale || ''
      companyForm.companyIntro = res.data.companyIntro || ''
      companyForm.contacts = res.data.contacts || ''
      companyForm.phone = res.data.phone || ''
      companyForm.province = res.data.province || ''
      companyForm.city = res.data.city || ''
      companyForm.district = res.data.district || ''
      companyForm.address = res.data.address || ''
      // 解析 JSON 字段
      if (res.data.recruitmentJson) {
        try {
          recruitments.value = JSON.parse(res.data.recruitmentJson)
        } catch (e) {
          recruitments.value = []
        }
      } else {
        recruitments.value = []
      }
      if (res.data.certificatesJson) {
        try {
          certificates.value = JSON.parse(res.data.certificatesJson)
        } catch (e) {
          certificates.value = []
        }
      } else {
        certificates.value = []
      }
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

// 加载车辆列表
async function loadVehicles() {
  try {
    const r = await listVehicles()
    if (r.code === 0) {
      vehicles.value = r.data ?? []
    }
  } catch (e: any) {
    console.error('Failed to load vehicles', e)
  }
}

async function saveUserInfo() {
  if (!userForm.displayName?.trim()) {
    ElMessage.warning('请输入姓名/昵称')
    return
  }
  loading.value = true
  try {
    const birthDate = userForm.birthDate?.trim() || undefined
    const req: UserUpdateRequest = {
      nickName: userForm.displayName,
      phonenumber: userForm.phonenumber,
      position: userForm.position,
      birthDate,
      gender: userForm.gender,
      bio: userForm.bio,
      avatar: userForm.avatar
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
    const req: CompanyCreateRequest = {
      ...companyForm,
      announcementsJson: '',
      recruitmentJson: JSON.stringify(recruitments.value),
      certificatesJson: JSON.stringify(certificates.value)
    }
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
  if (!passwordForm.newPassword || !passwordForm.confirmPassword) {
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
function resetPasswordForm() { passwordForm.newPassword = ''; passwordForm.confirmPassword = '' }

// 头像上传
const avatarUploading = ref(false)
async function handleAvatarUpload(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请上传图片格式文件')
    return
  }
  if (file.size > 5 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 5MB')
    return
  }
  avatarUploading.value = true
  try {
    const res = await uploadImage(file)
    if (res.code === 0 && res.data?.fileUrl) {
      userForm.avatar = res.data.fileUrl
      ElMessage.success('头像上传成功')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  } finally {
    avatarUploading.value = false
    input.value = ''
  }
}

// 车辆管理函数
function openVehicleDialog(mode: 'create' | 'edit', vehicle?: VehicleResponse) {
  vehicleDialogMode.value = mode
  editingVehicleId.value = vehicle?.id || null
  if (mode === 'edit' && vehicle) {
    vehicleForm.driverName = vehicle.driverName
    vehicleForm.driverIdCard = vehicle.driverIdCard
    vehicleForm.plateNumber = vehicle.plateNumber
    vehicleForm.driverPhone = vehicle.driverPhone
    vehicleForm.vehicleType = vehicle.vehicleType || ''
    vehicleForm.remark = vehicle.remark || ''
  } else {
    vehicleForm.driverName = ''
    vehicleForm.driverIdCard = ''
    vehicleForm.plateNumber = ''
    vehicleForm.driverPhone = ''
    vehicleForm.vehicleType = ''
    vehicleForm.remark = ''
  }
  vehicleDialogVisible.value = true
}

async function saveVehicle() {
  if (!vehicleForm.driverName?.trim() || !vehicleForm.plateNumber?.trim() || !vehicleForm.driverPhone?.trim()) {
    ElMessage.warning('请填写完整信息')
    return
  }
  loading.value = true
  try {
    let res
    if (vehicleDialogMode.value === 'edit' && editingVehicleId.value) {
      res = await updateVehicle(editingVehicleId.value, vehicleForm)
    } else {
      res = await createVehicle(vehicleForm)
    }
    if (res.code === 0) {
      ElMessage.success(vehicleDialogMode.value === 'edit' ? '修改成功' : '添加成功')
      vehicleDialogVisible.value = false
      await loadVehicles()
    } else {
      throw new Error(res.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '操作失败')
  } finally {
    loading.value = false
  }
}

async function removeVehicle(id: number) {
  try {
    await ElMessageBox.confirm('确定要删除这辆车吗？', '确认删除', { type: 'warning' })
    const res = await deleteVehicle(id)
    if (res.code === 0) {
      ElMessage.success('删除成功')
      await loadVehicles()
    } else {
      throw new Error(res.message)
    }
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message ?? '删除失败')
    }
  }
}

async function setDefault(id: number) {
  try {
    const res = await setDefaultVehicle(id)
    if (res.code === 0) {
      ElMessage.success('设置成功')
      await loadVehicles()
    } else {
      throw new Error(res.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '设置失败')
  }
}

// 招聘管理函数
function openRecruitmentDialog(mode: 'create' | 'edit', item?: { id: string, position: string, requirements: string, salary: string }) {
  editingRecruitmentId.value = item?.id || null
  if (mode === 'edit' && item) {
    recruitmentForm.position = item.position
    recruitmentForm.requirements = item.requirements
    recruitmentForm.salary = item.salary
  } else {
    recruitmentForm.position = ''
    recruitmentForm.requirements = ''
    recruitmentForm.salary = ''
  }
  recruitmentDialogVisible.value = true
}

function saveRecruitment() {
  if (!recruitmentForm.position?.trim()) {
    ElMessage.warning('请填写岗位名称')
    return
  }
  if (editingRecruitmentId.value) {
    const index = recruitments.value.findIndex(r => r.id === editingRecruitmentId.value)
    if (index >= 0) {
      recruitments.value[index] = { ...recruitments.value[index], ...recruitmentForm }
    }
  } else {
    recruitments.value.push({ id: Date.now().toString(), ...recruitmentForm })
  }
  recruitmentDialogVisible.value = false
  ElMessage.success('保存成功')
}

function removeRecruitment(id: string) {
  recruitments.value = recruitments.value.filter(r => r.id !== id)
  ElMessage.success('删除成功')
}

// 资质证书上传
async function handleCertificateUpload(event: Event) {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return
  if (!file.type.startsWith('image/')) {
    ElMessage.warning('请上传图片格式文件')
    return
  }
  if (file.size > 10 * 1024 * 1024) {
    ElMessage.warning('图片大小不能超过 10MB')
    return
  }
  certificateUploading.value = true
  try {
    const res = await uploadImage(file)
    if (res.code === 0 && res.data?.fileUrl) {
      certificates.value.push(res.data.fileUrl)
      ElMessage.success('上传成功')
    } else {
      ElMessage.error(res.message || '上传失败')
    }
  } catch (e: any) {
    ElMessage.error(e.message || '上传失败')
  } finally {
    certificateUploading.value = false
    input.value = ''
  }
}

function removeCertificate(index: number) {
  certificates.value.splice(index, 1)
  ElMessage.success('删除成功')
}

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
const passwordDirty = computed(() => !!(passwordForm.newPassword || passwordForm.confirmPassword))

const navItems = [
  { key: 'profile', label: '个人资料', icon: User },
  { key: 'company', label: '公司主页', icon: Building2 },
  { key: 'credentials', label: '资质证照', icon: Award },
  { key: 'vehicles', label: '车辆管理', icon: Truck },
  { key: 'security', label: '账号安全', icon: Lock }
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
    </div>

    <!-- 主布局：左侧导航 + 右侧内容 -->
    <div class="flex gap-6">
      <!-- 左侧垂直导航 -->
      <aside class="w-64 shrink-0">
        <nav class="bg-white rounded-xl border border-gray-200 p-2 space-y-1">
          <button
            v-for="item in navItems"
            :key="item.key"
            :class="[
              'w-full text-left px-4 py-3 rounded-lg transition-all flex items-center gap-3',
              activeTab === item.key
                ? 'bg-brand-50 text-brand-700 font-bold'
                : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
            ]"
            @click="activeTab = item.key"
          >
            <component :is="item.icon" class="w-5 h-5" />
            <span>{{ item.label }}</span>
          </button>
        </nav>
      </aside>

      <!-- 右侧内容区 -->
      <div class="flex-1 min-w-0">

        <!-- 个人资料 -->
        <div v-show="activeTab === 'profile'" class="bg-white rounded-xl border border-gray-200 overflow-hidden animate-fade-in">
          <div class="p-5 border-b border-gray-200">
            <h3 class="text-2xl font-bold text-gray-900">个人资料</h3>
            <p class="text-sm text-gray-500 mt-1">修改后点击保存生效</p>
          </div>
          
          <div class="p-6 space-y-6">
            <!-- 头像上传 -->
            <div>
              <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">个人头像</label>
              <div class="flex items-center gap-4">
                <div class="relative">
                  <img
                    v-if="userForm.avatar"
                    :src="userForm.avatar"
                    alt="头像"
                    class="w-20 h-20 rounded-lg object-cover border-2 border-gray-200"
                  />
                  <div
                    v-else
                    class="w-20 h-20 rounded-lg bg-gradient-to-br from-brand-500 to-brand-600 text-white flex items-center justify-center text-2xl font-bold"
                  >
                    {{ avatarText }}
                  </div>
                  <label
                    class="absolute -bottom-1 -right-1 w-7 h-7 rounded-full bg-white border border-gray-200 shadow-sm hover:bg-gray-50 transition-all flex items-center justify-center cursor-pointer"
                    :class="{ 'opacity-50 pointer-events-none': avatarUploading }"
                  >
                    <Upload class="w-3.5 h-3.5 text-gray-500" />
                    <input type="file" class="hidden" accept="image/*" @change="handleAvatarUpload" />
                  </label>
                </div>
                <div class="text-xs text-gray-400">
                  <p>支持 JPG/PNG 格式</p>
                  <p>建议尺寸 200x200px，大小不超过 5MB</p>
                </div>
              </div>
            </div>

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
                  class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
                />
              </div>
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">手机号码</label>
                <input
                  v-model="userForm.phonenumber"
                  type="text"
                  placeholder="请输入手机号"
                  class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
                />
              </div>
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">公司职务</label>
                <input
                  v-model="userForm.position"
                  type="text"
                  placeholder="如：采购经理、销售总监"
                  class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
                />
              </div>
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">出生年月</label>
                <el-date-picker
                  v-model="userForm.birthDate"
                  type="date"
                  :locale="zhCn"
                  placeholder="选择出生日期"
                  format="YYYY年MM月DD日"
                  value-format="YYYY-MM-DD"
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
                      'flex items-center gap-2 px-4 py-2.5 rounded-lg border-2 cursor-pointer transition-all',
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
                class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all resize-none"
              ></textarea>
              <p class="text-xs text-gray-400 mt-1 text-right">{{ userForm.bio?.length || 0 }}/500</p>
            </div>

            <!-- 保存按钮 -->
            <div v-if="userDirty" class="pt-6 border-t border-gray-200 flex justify-end gap-3">
              <BaseButton type="secondary" size="sm" :disabled="loading" @click="resetUserForm">取消</BaseButton>
              <BaseButton type="primary" size="sm" :loading="loading" @click="saveUserInfo">保存</BaseButton>
            </div>
          </div>
        </div>

        <!-- 公司主页 -->
        <div v-show="activeTab === 'company'" class="bg-white rounded-xl border border-gray-200 overflow-hidden animate-fade-in">
          <div class="p-5 border-b border-gray-200">
            <h3 class="text-2xl font-bold text-gray-900">公司主页</h3>
            <p class="text-sm text-gray-500 mt-1">完善公司信息以便开展业务，这些信息将展示在公司主页</p>
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
                  class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
                />
              </div>
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">统一社会信用代码</label>
                <input
                  v-model="companyForm.licenseNo"
                  type="text"
                  placeholder="请输入统一社会信用代码"
                  class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
                />
              </div>
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">法定代表人</label>
                <input
                  v-model="companyForm.legalPerson"
                  type="text"
                  placeholder="请输入法定代表人"
                  class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
                />
              </div>
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">注册资本</label>
                <input
                  v-model="companyForm.registeredCapital"
                  type="text"
                  placeholder="如：1000万元"
                  class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
                />
              </div>
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">成立日期</label>
                <el-date-picker
                  v-model="companyForm.establishDate"
                  type="date"
                  :locale="zhCn"
                  placeholder="选择成立日期"
                  format="YYYY年MM月DD日"
                  value-format="YYYY-MM-DD"
                  class="w-full neo-picker"
                />
              </div>
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">公司规模</label>
                <input
                  v-model="companyForm.scale"
                  type="text"
                  placeholder="如：100-500人"
                  class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
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
                class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
              />
            </div>

            <div class="max-w-3xl">
              <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">经营范围</label>
              <textarea
                v-model="companyForm.businessScope"
                rows="3"
                placeholder="请输入公司经营范围"
                class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all resize-none"
              ></textarea>
            </div>

            <!-- 公司介绍 -->
            <div class="max-w-3xl">
              <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">公司介绍</label>
              <textarea
                v-model="companyForm.companyIntro"
                rows="5"
                placeholder="请输入公司介绍（用于展示在商业主页的企业介绍部分）"
                class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all resize-none"
              ></textarea>
            </div>

            <!-- 人才招聘 -->
            <div class="max-w-3xl">
              <div class="flex items-center justify-between mb-4">
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider">人才招聘</label>
                <BaseButton type="secondary" size="sm" @click="openRecruitmentDialog('create')">
                  <Plus class="w-4 h-4" />
                  添加岗位
                </BaseButton>
              </div>
              <div v-if="recruitments.length === 0" class="text-sm text-gray-400 py-4 text-center border-2 border-dashed border-gray-200 rounded-lg">
                暂无招聘信息，点击上方按钮添加
              </div>
              <div v-else class="space-y-3">
                <div
                  v-for="item in recruitments"
                  :key="item.id"
                  class="p-4 bg-gray-50 rounded-lg border border-gray-200 flex items-start justify-between gap-4"
                >
                  <div class="flex-1">
                    <div class="font-bold text-gray-900 mb-1">{{ item.position }}</div>
                    <div class="text-sm text-gray-500 mb-1">{{ item.requirements }}</div>
                    <div class="text-sm text-brand-600 font-bold">{{ item.salary }}</div>
                  </div>
                  <div class="flex gap-2">
                    <button
                      class="p-2 hover:bg-gray-200 rounded-lg transition-colors"
                      @click="openRecruitmentDialog('edit', item)"
                      title="编辑"
                    >
                      <Edit2 class="w-4 h-4 text-gray-600" />
                    </button>
                    <button
                      class="p-2 hover:bg-red-50 rounded-lg transition-colors"
                      @click="removeRecruitment(item.id)"
                      title="删除"
                    >
                      <Trash2 class="w-4 h-4 text-red-500" />
                    </button>
                  </div>
                </div>
              </div>
            </div>

        <!-- 营业执照上传 -->
        <div class="max-w-3xl">
          <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">营业执照</label>
          <div class="flex items-start gap-4">
            <!-- 上传区域 -->
            <div v-if="!companyForm.licenseImgUrl" class="relative">
              <label 
                class="flex flex-col items-center justify-center w-40 h-32 border-2 border-dashed border-gray-200 rounded-lg cursor-pointer hover:border-brand-400 hover:bg-brand-50/50 transition-all"
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
                class="w-40 h-32 object-cover rounded-lg border-2 border-gray-200"
              />
              <!-- 操作遮罩 -->
              <div class="absolute inset-0 bg-black/50 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2">
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
            'flex items-center gap-4 p-4 rounded-lg',
            company?.id ? 'bg-brand-50' : 'bg-amber-50'
          ]"
        >
          <div
            :class="[
              'w-10 h-10 rounded-lg flex items-center justify-center',
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

        <!-- 保存按钮 -->
        <div v-if="companyDirty" class="pt-6 border-t border-gray-200 flex justify-end gap-3">
          <BaseButton type="secondary" size="sm" :disabled="loading" @click="resetCompanyForm">取消</BaseButton>
          <BaseButton type="primary" size="sm" :loading="loading" @click="saveCompanyInfo">保存</BaseButton>
        </div>
      </div>
        </div>

        <!-- 资质证照 -->
        <div v-show="activeTab === 'credentials'" class="bg-white rounded-xl border border-gray-200 overflow-hidden animate-fade-in">
          <div class="p-5 border-b border-gray-200">
            <h3 class="text-2xl font-bold text-gray-900">资质证照</h3>
            <p class="text-sm text-gray-500 mt-1">上传公司相关资质证书，提升企业信誉</p>
          </div>
          
          <div class="p-6 space-y-6">
            <div class="max-w-3xl">
              <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">其他资质证书</label>
              <div class="grid grid-cols-2 md:grid-cols-3 gap-4">
                <label
                  class="flex flex-col items-center justify-center h-32 border-2 border-dashed border-gray-200 rounded-lg cursor-pointer hover:border-brand-400 hover:bg-brand-50/50 transition-all"
                  :class="{ 'opacity-50 pointer-events-none': certificateUploading }"
                >
                  <Upload v-if="!certificateUploading" class="w-8 h-8 text-gray-300 mb-2" />
                  <div v-else class="w-8 h-8 border-2 border-brand-500 border-t-transparent rounded-full animate-spin mb-2" />
                  <span class="text-xs text-gray-400 font-bold">{{ certificateUploading ? '上传中...' : '上传证书' }}</span>
                  <input type="file" class="hidden" accept="image/*" @change="handleCertificateUpload" />
                </label>
                <div
                  v-for="(url, index) in certificates"
                  :key="index"
                  class="relative group h-32"
                >
                  <img :src="url" alt="资质证书" class="w-full h-full object-cover rounded-lg border-2 border-gray-200" />
                  <div class="absolute inset-0 bg-black/50 rounded-lg opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center gap-2">
                    <button
                      class="w-8 h-8 bg-white/90 rounded-lg flex items-center justify-center hover:bg-white transition-colors"
                      @click="removeCertificate(index)"
                      title="删除"
                    >
                      <X class="w-4 h-4 text-red-500" />
                    </button>
                  </div>
                </div>
              </div>
              <p class="text-xs text-gray-400 mt-2">支持 JPG/PNG 格式，单张不超过 10MB</p>
            </div>

            <div v-if="companyDirty" class="pt-6 border-t border-gray-200 flex justify-end gap-3">
              <BaseButton type="secondary" size="sm" :disabled="loading" @click="resetCompanyForm">取消</BaseButton>
              <BaseButton type="primary" size="sm" :loading="loading" @click="saveCompanyInfo">保存</BaseButton>
            </div>
          </div>
        </div>

        <!-- 车辆管理 -->
        <div v-show="activeTab === 'vehicles'" class="bg-white rounded-xl border border-gray-200 overflow-hidden animate-fade-in">
          <div class="p-5 border-b border-gray-200">
            <div class="flex items-center justify-between">
              <div>
                <h3 class="text-2xl font-bold text-gray-900">车辆管理</h3>
                <p class="text-sm text-gray-500 mt-1">管理公司常用车辆信息，方便快速选择</p>
              </div>
              <BaseButton type="primary" size="sm" @click="openVehicleDialog('create')">
                <Plus class="w-4 h-4" />
                添加车辆
              </BaseButton>
            </div>
          </div>
          
          <div class="p-6">
            <div v-if="vehicles.length === 0" class="text-center py-12 text-gray-400">
              <Truck class="w-16 h-16 mx-auto mb-4 text-gray-300" />
              <p>暂无车辆信息</p>
              <p class="text-sm mt-2">点击上方按钮添加常用车辆</p>
            </div>
            <div v-else class="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div
                v-for="v in vehicles"
                :key="v.id"
                class="p-4 bg-gray-50 rounded-lg border border-gray-200"
              >
                <div class="flex items-start justify-between mb-3">
                  <div class="flex items-center gap-2">
                    <div class="font-bold text-gray-900">{{ v.plateNumber }}</div>
                    <span
                      v-if="v.isDefault"
                      class="px-2 py-0.5 rounded-full text-xs font-bold bg-brand-100 text-brand-700"
                    >
                      默认
                    </span>
                  </div>
                  <div class="flex gap-2">
                    <button
                      class="p-1.5 hover:bg-gray-200 rounded-lg transition-colors"
                      @click="openVehicleDialog('edit', v)"
                      title="编辑"
                    >
                      <Edit2 class="w-4 h-4 text-gray-600" />
                    </button>
                    <button
                      class="p-1.5 hover:bg-red-50 rounded-lg transition-colors"
                      @click="removeVehicle(v.id)"
                      title="删除"
                    >
                      <Trash2 class="w-4 h-4 text-red-500" />
                    </button>
                  </div>
                </div>
                <div class="space-y-1 text-sm text-gray-600">
                  <div>司机：{{ v.driverName }}</div>
                  <div>电话：{{ v.driverPhone }}</div>
                  <div v-if="v.vehicleType">车型：{{ v.vehicleType }}</div>
                </div>
                <div v-if="!v.isDefault" class="mt-3">
                  <BaseButton type="secondary" size="sm" @click="setDefault(v.id)">
                    <Star class="w-3 h-3" />
                    设为默认
                  </BaseButton>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 账户安全 -->
        <div v-show="activeTab === 'security'" class="bg-white rounded-xl border border-gray-200 overflow-hidden animate-fade-in">
          <div class="p-5 border-b border-gray-200">
            <h3 class="text-2xl font-bold text-gray-900">修改密码</h3>
            <p class="text-sm text-gray-500 mt-1">定期更换密码可以保护账户安全</p>
          </div>
          
          <div class="p-6 space-y-6">
            <div class="max-w-md space-y-4">
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">新密码</label>
                <input
                  v-model="passwordForm.newPassword"
                  type="password"
                  placeholder="请输入新密码（至少6位）"
                  class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
                />
              </div>
              <div>
                <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">确认新密码</label>
                <input
                  v-model="passwordForm.confirmPassword"
                  type="password"
                  placeholder="请再次输入新密码"
                  class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
                />
              </div>
            </div>

            <!-- 保存按钮 -->
            <div v-if="passwordDirty" class="pt-6 border-t border-gray-200 flex justify-end gap-3">
              <BaseButton type="secondary" size="sm" :disabled="loading" @click="resetPasswordForm">取消</BaseButton>
              <BaseButton type="primary" size="sm" :loading="loading" @click="changePassword">保存</BaseButton>
            </div>
          </div>

          <!-- 最近登录日志 -->
          <div class="border-t border-gray-100 bg-gray-50/30 p-6">
            <div class="flex items-center justify-between mb-6">
              <div class="flex items-center gap-2">
                <History class="w-5 h-5 text-brand-600" />
                <h4 class="font-bold text-gray-900">最近登录日志</h4>
                <span class="text-[10px] font-bold text-gray-400 uppercase tracking-widest ml-2">Recent Logins</span>
              </div>
              <button 
                class="p-1.5 hover:bg-gray-200 rounded-lg transition-all text-gray-400 hover:text-brand-600"
                :class="{ 'animate-spin text-brand-600': loginLogsLoading }"
                @click="fetchLoginLogs"
                title="刷新日志"
              >
                <RefreshCw class="w-4 h-4" />
              </button>
            </div>

            <div v-if="loginLogsLoading" class="flex justify-center py-8">
              <div class="flex items-center gap-2 text-gray-400 text-sm">
                <RefreshCw class="w-4 h-4 animate-spin" />
                <span>加载中...</span>
              </div>
            </div>
            <div v-else-if="loginLogs.length === 0" class="text-center py-12 bg-white rounded-xl border border-gray-100 shadow-sm">
              <ShieldCheck class="w-12 h-12 text-gray-200 mx-auto mb-3" />
              <p class="text-gray-400 text-sm font-medium">暂无登录记录</p>
            </div>
            <div v-else class="grid grid-cols-1 gap-3">
              <div 
                v-for="log in loginLogs" 
                :key="log.infoId"
                class="bg-white p-4 rounded-xl border border-gray-100 shadow-sm flex flex-col md:flex-row md:items-center justify-between gap-4 transition-all hover:border-brand-200 hover:shadow-md"
              >
                <div class="flex items-center gap-4">
                  <div 
                    :class="[
                      'w-10 h-10 rounded-lg flex items-center justify-center shrink-0',
                      log.status === '0' ? 'bg-brand-50 text-brand-600' : 'bg-red-50 text-red-600'
                    ]"
                  >
                    <Monitor v-if="log.status === '0'" class="w-5 h-5" />
                    <AlertTriangle v-else class="w-5 h-5" />
                  </div>
                  <div>
                    <div class="flex items-center gap-2">
                      <span class="font-bold text-gray-900">{{ log.browser }} / {{ log.os }}</span>
                      <span 
                        :class="[
                          'text-[10px] font-bold px-1.5 py-0.5 rounded uppercase tracking-wider',
                          log.status === '0' ? 'bg-brand-100 text-brand-700' : 'bg-red-100 text-red-700'
                        ]"
                      >
                        {{ log.status === '0' ? '成功' : '失败' }}
                      </span>
                    </div>
                    <div class="flex items-center gap-3 mt-1">
                      <div class="flex items-center gap-1 text-xs text-gray-400">
                        <MapPin class="w-3 h-3" />
                        <span>{{ log.ipaddr }}</span>
                      </div>
                      <div class="flex items-center gap-1 text-xs text-gray-400">
                        <Calendar class="w-3 h-3" />
                        <span>{{ log.loginTime }}</span>
                      </div>
                    </div>
                  </div>
                </div>
                <div class="text-xs font-medium text-gray-400 md:text-right">
                  {{ log.msg }}
                </div>
              </div>
            </div>
            <p class="text-[10px] text-gray-400 mt-6 text-center italic">
              * 仅展示最近 10 条登录记录，如发现异常登录请及时修改密码。
            </p>
          </div>
        </div>
      </div>
    </div>

    <!-- 弹窗 -->
    <!-- 车辆管理弹窗 -->
    <el-dialog
      v-model="vehicleDialogVisible"
      :title="vehicleDialogMode === 'create' ? '添加车辆' : '编辑车辆'"
      width="500px"
      align-center
    >
      <div class="space-y-4">
        <div>
          <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">司机姓名 <span class="text-red-500">*</span></label>
          <input
            v-model="vehicleForm.driverName"
            type="text"
            placeholder="请输入司机姓名"
            class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
          />
        </div>
        <div>
          <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">身份证号 <span class="text-red-500">*</span></label>
          <input
            v-model="vehicleForm.driverIdCard"
            type="text"
            placeholder="请输入身份证号"
            class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
          />
        </div>
        <div>
          <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">车牌号 <span class="text-red-500">*</span></label>
          <input
            v-model="vehicleForm.plateNumber"
            type="text"
            placeholder="请输入车牌号"
            class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
          />
        </div>
        <div>
          <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">联系电话 <span class="text-red-500">*</span></label>
          <input
            v-model="vehicleForm.driverPhone"
            type="text"
            placeholder="请输入联系电话"
            class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
          />
        </div>
        <div>
          <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">车型</label>
          <input
            v-model="vehicleForm.vehicleType"
            type="text"
            placeholder="如：厢式货车、半挂车"
            class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
          />
        </div>
        <div>
          <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">备注</label>
          <textarea
            v-model="vehicleForm.remark"
            rows="2"
            placeholder="其他备注信息"
            class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all resize-none"
          ></textarea>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <BaseButton type="secondary" size="sm" @click="vehicleDialogVisible = false">取消</BaseButton>
          <BaseButton type="primary" size="sm" :loading="loading" @click="saveVehicle">保存</BaseButton>
        </div>
      </template>
    </el-dialog>

    <!-- 招聘弹窗 -->
    <el-dialog
      v-model="recruitmentDialogVisible"
      :title="editingRecruitmentId ? '编辑岗位' : '添加岗位'"
      width="600px"
      align-center
    >
      <div class="space-y-4">
        <div>
          <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">岗位名称 <span class="text-red-500">*</span></label>
          <input
            v-model="recruitmentForm.position"
            type="text"
            placeholder="如：销售经理、采购专员"
            class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
          />
        </div>
        <div>
          <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">任职要求</label>
          <textarea
            v-model="recruitmentForm.requirements"
            rows="4"
            placeholder="请输入任职要求"
            class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all resize-none"
          ></textarea>
        </div>
        <div>
          <label class="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2">薪资待遇</label>
          <input
            v-model="recruitmentForm.salary"
            type="text"
            placeholder="如：5000-8000元/月"
            class="w-full px-4 py-2.5 border-2 border-gray-200 rounded-lg focus:border-brand-500 outline-none transition-all"
          />
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end gap-3">
          <BaseButton type="secondary" size="sm" @click="recruitmentDialogVisible = false">取消</BaseButton>
          <BaseButton type="primary" size="sm" @click="saveRecruitment">保存</BaseButton>
        </div>
      </template>
    </el-dialog>

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
          class="max-w-full max-h-[70vh] object-contain rounded-lg"
        />
      </div>
    </el-dialog>
  </div>
</template>

<style scoped>
/* 统一 Element Plus 日期选择器样式 */
:deep(.neo-picker .el-input__wrapper) {
  border: 2px solid rgb(229 231 235);
  border-radius: 8px;
  box-shadow: none;
  transition: all 0.15s ease;
  height: 50px;
  padding: 10px 16px;
  display: flex;
  align-items: center;
}
:deep(.neo-picker .el-input__wrapper.is-focus) {
  border-color: rgb(16 185 129);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.18);
}
:deep(.neo-picker .el-input__wrapper:hover) {
  border-color: rgb(209 213 219);
}

/* 级联选择器样式 */
:deep(.neo-cascader .el-input__wrapper) {
  border: 2px solid rgb(229 231 235);
  border-radius: 8px;
  box-shadow: none;
  transition: all 0.15s ease;
  height: 50px;
  padding: 10px 16px;
  display: flex;
  align-items: center;
}
:deep(.neo-cascader .el-input__wrapper.is-focus) {
  border-color: rgb(16 185 129);
  box-shadow: 0 0 0 3px rgba(16, 185, 129, 0.18);
}
:deep(.neo-cascader .el-input__wrapper:hover) {
  border-color: rgb(209 213 219);
}
</style>
