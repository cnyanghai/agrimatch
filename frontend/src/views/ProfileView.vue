<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import { useAuthStore } from '../store/auth'
import { updateMe, type UserUpdateRequest } from '../api/user'
import { getMyCompany, createCompany, updateCompany, type CompanyResponse, type CompanyCreateRequest } from '../api/company'
import { User, OfficeBuilding, Lock, Check, Upload, ShoppingCart, Box, Warning } from '@element-plus/icons-vue'

const auth = useAuthStore()
const loading = ref(false)
const activeTab = ref('user')

// 用户身份
const isBuyer = computed(() => auth.me?.isBuyer === 1)
const isSeller = computed(() => auth.me?.isSeller === 1)

// 公司数据
const company = ref<CompanyResponse | null>(null)

// 用户表单
const userForm = reactive({
  displayName: '',
  phonenumber: '',
  position: '',
  // 预留：后续若要做更完整的用户档案再接入（当前后端未支持持久化）
  birthDate: '',
  gender: 1 as number,
  bio: ''
})

// 公司表单
const companyForm = reactive({
  companyName: '',
  licenseNo: '',
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

// 用于“有改动才显示保存/取消”的快照
const userSnapshot = ref({
  displayName: '',
  phonenumber: '',
  position: '',
  birthDate: '', // YYYY-MM
  gender: 1 as number,
  bio: ''
})
const companySnapshot = ref({
  companyName: '',
  licenseNo: '',
  contacts: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  address: ''
})

// 初始化数据
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
      // birthDate：后端 YYYY-MM-DD，前端用 month picker YYYY-MM
      userForm.birthDate = auth.me.birthDate ? auth.me.birthDate.slice(0, 7) : ''
      userForm.gender = auth.me.gender || 1
      userForm.bio = auth.me.bio || ''
    }
    // 同步快照（用于取消/dirty 判断）
    userSnapshot.value = {
      displayName: userForm.displayName || '',
      phonenumber: userForm.phonenumber || '',
      position: userForm.position || '',
      birthDate: userForm.birthDate || '',
      gender: userForm.gender || 1,
      bio: userForm.bio || ''
    }
  } catch (e) {
    console.error('Failed to load user data', e)
  }
}

async function loadCompanyData() {
  try {
    const res = await getMyCompany()
    if (res.code === 0 && res.data) {
      company.value = res.data
      // 填充表单
      companyForm.companyName = res.data.companyName || ''
      companyForm.licenseNo = res.data.licenseNo || ''
      companyForm.contacts = res.data.contacts || ''
      companyForm.phone = res.data.phone || ''
      companyForm.province = res.data.province || ''
      companyForm.city = res.data.city || ''
      companyForm.district = res.data.district || ''
      companyForm.address = res.data.address || ''
    }
    // 同步快照（用于取消/dirty 判断）
    companySnapshot.value = {
      companyName: companyForm.companyName || '',
      licenseNo: companyForm.licenseNo || '',
      contacts: companyForm.contacts || '',
      phone: companyForm.phone || '',
      province: companyForm.province || '',
      city: companyForm.city || '',
      district: companyForm.district || '',
      address: companyForm.address || ''
    }
  } catch (e) {
    console.error('Failed to load company data', e)
  }
}

// 保存用户信息
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
      // 更新快照
      userSnapshot.value = {
        displayName: userForm.displayName || '',
        phonenumber: userForm.phonenumber || '',
        position: userForm.position || '',
        birthDate: userForm.birthDate || '',
        gender: userForm.gender || 1,
        bio: userForm.bio || ''
      }
    } else {
      throw new Error(res.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '保存失败')
  } finally {
    loading.value = false
  }
}

// 保存公司信息
async function saveCompanyInfo() {
  if (!companyForm.companyName?.trim()) {
    ElMessage.warning('请输入公司名称')
    return
  }
  loading.value = true
  try {
    const req: CompanyCreateRequest = {
      companyName: companyForm.companyName,
      licenseNo: companyForm.licenseNo,
      contacts: companyForm.contacts,
      phone: companyForm.phone,
      province: companyForm.province,
      city: companyForm.city,
      district: companyForm.district,
      address: companyForm.address
    }
    
    let res
    if (company.value?.id) {
      // 更新
      res = await updateCompany(company.value.id, req)
    } else {
      // 创建
      res = await createCompany(req)
    }
    
    if (res.code === 0) {
      ElMessage.success('保存成功')
      await loadCompanyData()
      // 更新快照
      companySnapshot.value = {
        companyName: companyForm.companyName || '',
        licenseNo: companyForm.licenseNo || '',
        contacts: companyForm.contacts || '',
        phone: companyForm.phone || '',
        province: companyForm.province || '',
        city: companyForm.city || '',
        district: companyForm.district || '',
        address: companyForm.address || ''
      }
    } else {
      throw new Error(res.message)
    }
  } catch (e: any) {
    ElMessage.error(e?.message ?? '保存失败')
  } finally {
    loading.value = false
  }
}

// 切换用户身份
async function changeUserType(type: 'buyer' | 'seller') {
  try {
    await ElMessageBox.confirm(
      `确定要切换为${type === 'buyer' ? '采购商' : '供应商'}身份吗？`,
      '切换身份',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    loading.value = true
    await auth.updateUserType(type)
    ElMessage.success('身份切换成功')
  } catch (e: any) {
    if (e !== 'cancel') {
      ElMessage.error(e?.message ?? '切换失败')
    }
  } finally {
    loading.value = false
  }
}

// 修改密码
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

function resetUserForm() {
  userForm.displayName = userSnapshot.value.displayName
  userForm.phonenumber = userSnapshot.value.phonenumber
  userForm.position = userSnapshot.value.position
  userForm.birthDate = userSnapshot.value.birthDate
  userForm.gender = userSnapshot.value.gender
  userForm.bio = userSnapshot.value.bio
}

function resetCompanyForm() {
  companyForm.companyName = companySnapshot.value.companyName
  companyForm.licenseNo = companySnapshot.value.licenseNo
  companyForm.contacts = companySnapshot.value.contacts
  companyForm.phone = companySnapshot.value.phone
  companyForm.province = companySnapshot.value.province
  companyForm.city = companySnapshot.value.city
  companyForm.district = companySnapshot.value.district
  companyForm.address = companySnapshot.value.address
}

function resetPasswordForm() {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}

// 性别选项 (不包含保密)
const genderOptions = [
  { label: '男', value: 1 },
  { label: '女', value: 2 }
]

const currentName = computed(() => auth.me?.nickName || auth.me?.userName || '用户')
const currentPhone = computed(() => auth.me?.phonenumber || '未绑定手机号')
const currentPosition = computed(() => auth.me?.position || '暂无职位')
const currentRoleLabel = computed(() => (isBuyer.value ? '采购商' : isSeller.value ? '供应商' : '未设置'))
const rolePillClass = computed(() => {
  if (isBuyer.value) return 'bg-blue-50 text-blue-700 border-blue-200'
  if (isSeller.value) return 'bg-emerald-50 text-emerald-700 border-emerald-200'
  return 'bg-gray-100 text-gray-600 border-gray-200'
})
const avatarText = computed(() => {
  const n = currentName.value.trim()
  const ch = (n[0] ?? 'U').toUpperCase()
  return /\d/.test(ch) ? 'U' : ch
})

const userDirty = computed(() => {
  return (
    (userForm.displayName || '') !== (userSnapshot.value.displayName || '') ||
    (userForm.phonenumber || '') !== (userSnapshot.value.phonenumber || '') ||
    (userForm.position || '') !== (userSnapshot.value.position || '') ||
    (userForm.birthDate || '') !== (userSnapshot.value.birthDate || '') ||
    (userForm.gender || 1) !== (userSnapshot.value.gender || 1) ||
    (userForm.bio || '') !== (userSnapshot.value.bio || '')
  )
})

const companyDirty = computed(() => {
  return (
    (companyForm.companyName || '') !== (companySnapshot.value.companyName || '') ||
    (companyForm.licenseNo || '') !== (companySnapshot.value.licenseNo || '') ||
    (companyForm.contacts || '') !== (companySnapshot.value.contacts || '') ||
    (companyForm.phone || '') !== (companySnapshot.value.phone || '') ||
    (companyForm.province || '') !== (companySnapshot.value.province || '') ||
    (companyForm.city || '') !== (companySnapshot.value.city || '') ||
    (companyForm.district || '') !== (companySnapshot.value.district || '') ||
    (companyForm.address || '') !== (companySnapshot.value.address || '')
  )
})

const passwordDirty = computed(() => {
  return !!(passwordForm.oldPassword || passwordForm.newPassword || passwordForm.confirmPassword)
})
</script>

<template>
  <div class="max-w-6xl mx-auto space-y-6">
    <!-- 页面标题 -->
    <div>
      <h1 class="text-3xl font-extrabold tracking-tight text-gray-900">个人中心</h1>
      <p class="text-sm text-gray-500 mt-2">管理您的账户信息和公司资料</p>
    </div>

    <!-- 用户概览卡（统一 Card-First） -->
    <div class="bg-white p-6 rounded-2xl border border-gray-100 shadow-sm">
      <div class="flex flex-col md:flex-row md:items-center justify-between gap-6">
        <div class="flex items-center gap-4 min-w-0">
          <div class="relative">
            <div class="w-14 h-14 rounded-full bg-emerald-600 text-white flex items-center justify-center text-xl font-bold">
              {{ avatarText }}
            </div>
            <button
              type="button"
              class="absolute -bottom-1 -right-1 w-8 h-8 rounded-full bg-white border border-gray-100 shadow-sm hover:bg-gray-50 transition-all active:scale-95 flex items-center justify-center"
              title="更换头像（暂未开放）"
            >
              <el-icon class="text-gray-500"><Upload /></el-icon>
            </button>
          </div>

          <div class="min-w-0">
            <div class="flex items-center gap-2 flex-wrap">
              <div class="text-lg font-bold text-gray-900 truncate">{{ currentName }}</div>
              <span class="text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 rounded-full border" :class="rolePillClass">
                {{ currentRoleLabel }}
              </span>
              <span class="text-[10px] font-bold uppercase tracking-widest px-2 py-0.5 rounded-full bg-gray-100 text-gray-600 border border-gray-200">
                ID: {{ auth.me?.userId ?? '-' }}
              </span>
            </div>
            <div class="text-xs text-gray-500 mt-1 truncate">
              {{ currentPhone }} · {{ currentPosition }}
            </div>
          </div>
        </div>

        <!-- Tab 切换（pill segmented） -->
        <div class="flex items-center gap-2 bg-gray-100 rounded-full p-1 w-max">
          <button
            type="button"
            class="px-4 py-2 rounded-full text-sm font-bold transition-all active:scale-95"
            :class="activeTab === 'user' ? 'bg-white shadow-sm text-emerald-700' : 'text-gray-600 hover:text-gray-800'"
            @click="activeTab = 'user'"
          >
            基本信息
          </button>
          <button
            type="button"
            class="px-4 py-2 rounded-full text-sm font-bold transition-all active:scale-95"
            :class="activeTab === 'company' ? 'bg-white shadow-sm text-emerald-700' : 'text-gray-600 hover:text-gray-800'"
            @click="activeTab = 'company'"
          >
            公司信息
          </button>
          <button
            type="button"
            class="px-4 py-2 rounded-full text-sm font-bold transition-all active:scale-95"
            :class="activeTab === 'security' ? 'bg-white shadow-sm text-emerald-700' : 'text-gray-600 hover:text-gray-800'"
            @click="activeTab = 'security'"
          >
            账户安全
          </button>
        </div>
      </div>
    </div>

    <!-- Tab 内容（保留 el-tabs 以复用原逻辑，只隐藏 header） -->
    <el-tabs v-model="activeTab" class="profile-tabs">
      <!-- 用户基本信息 -->
      <el-tab-pane name="user">
        <template #label>
          <div class="flex items-center gap-2">
            <el-icon><User /></el-icon>
            <span>基本信息</span>
          </div>
        </template>

        <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
          <div class="flex justify-between items-center mb-6">
            <h3 class="text-lg font-semibold text-gray-800">基本信息</h3>
            <span class="text-xs text-gray-500">可直接修改，修改后保存</span>
          </div>

          <el-form label-position="top" class="max-w-2xl">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-x-6">
              <el-form-item label="用户姓名/昵称" required>
                <el-input 
                  v-model="userForm.displayName" 
                  placeholder="请输入姓名/昵称"
                />
              </el-form-item>
              <el-form-item label="手机号码">
                <el-input 
                  v-model="userForm.phonenumber" 
                  placeholder="请输入手机号"
                />
              </el-form-item>
              <el-form-item label="公司职务">
                <el-input 
                  v-model="userForm.position" 
                  placeholder="如：采购经理、销售总监"
                />
              </el-form-item>
              <el-form-item label="出生年月">
                <el-date-picker
                  v-model="userForm.birthDate"
                  type="month"
                  :locale="zhCn"
                  placeholder="选择出生年月"
                  format="YYYY年MM月"
                  value-format="YYYY-MM"
                  class="w-full"
                />
              </el-form-item>
              <el-form-item label="性别">
                <el-radio-group v-model="userForm.gender">
                  <el-radio v-for="opt in genderOptions" :key="opt.value" :value="opt.value">
                    {{ opt.label }}
                  </el-radio>
                </el-radio-group>
              </el-form-item>
            </div>
            <el-form-item label="个人介绍">
              <el-input 
                v-model="userForm.bio" 
                type="textarea"
                :rows="3"
                placeholder="简单介绍一下自己，让合作伙伴更好地了解您"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-form>

          <!-- 身份切换 -->
          <div class="mt-8 pt-6 border-t border-gray-100">
            <h4 class="text-base font-semibold text-gray-800 mb-4">用户身份</h4>
            <p class="text-sm text-gray-500 mb-4">当前身份：<span class="font-medium" :class="isBuyer ? 'text-blue-700' : isSeller ? 'text-emerald-700' : 'text-gray-600'">{{ currentRoleLabel }}</span></p>
            
            <div class="grid grid-cols-2 gap-4 max-w-lg">
              <div
                class="relative p-4 border-2 rounded-xl cursor-pointer transition-all"
                :class="isBuyer ? 'border-blue-200 bg-blue-50' : 'border-gray-200 hover:border-blue-200'"
                @click="!isBuyer && changeUserType('buyer')"
              >
                <div class="flex flex-col items-center">
                  <div class="w-12 h-12 rounded-full flex items-center justify-center mb-2"
                       :class="isBuyer ? 'bg-blue-100 text-blue-700' : 'bg-gray-100 text-gray-500'">
                    <el-icon :size="24"><ShoppingCart /></el-icon>
                  </div>
                  <div class="font-medium" :class="isBuyer ? 'text-blue-700' : 'text-gray-700'">采购商</div>
                </div>
                <div v-if="isBuyer" class="absolute top-2 right-2 w-5 h-5 bg-blue-100 rounded-full flex items-center justify-center border border-blue-200">
                  <el-icon class="text-blue-700" :size="12"><Check /></el-icon>
                </div>
              </div>

              <div
                class="relative p-4 border-2 rounded-xl cursor-pointer transition-all"
                :class="isSeller ? 'border-emerald-200 bg-emerald-50' : 'border-gray-200 hover:border-emerald-200'"
                @click="!isSeller && changeUserType('seller')"
              >
                <div class="flex flex-col items-center">
                  <div class="w-12 h-12 rounded-full flex items-center justify-center mb-2"
                       :class="isSeller ? 'bg-emerald-600 text-white' : 'bg-gray-100 text-gray-500'">
                    <el-icon :size="24"><Box /></el-icon>
                  </div>
                  <div class="font-medium" :class="isSeller ? 'text-emerald-800' : 'text-gray-700'">供应商</div>
                </div>
                <div v-if="isSeller" class="absolute top-2 right-2 w-5 h-5 bg-emerald-600 rounded-full flex items-center justify-center">
                  <el-icon class="text-white" :size="12"><Check /></el-icon>
                </div>
              </div>
            </div>
          </div>

          <!-- 保存/取消条：仅在有改动时出现 -->
          <div v-if="userDirty" class="mt-8 pt-6 border-t border-gray-100 flex items-center justify-end gap-3">
            <button
              type="button"
              class="px-4 py-2 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm font-bold transition-all active:scale-95"
              :disabled="loading"
              @click="resetUserForm"
            >
              取消
            </button>
            <button
              type="button"
              class="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-bold transition-all active:scale-95 disabled:opacity-60"
              :disabled="loading"
              @click="saveUserInfo"
            >
              保存
            </button>
          </div>
        </div>
      </el-tab-pane>

      <!-- 公司信息 -->
      <el-tab-pane name="company">
        <template #label>
          <div class="flex items-center gap-2">
            <el-icon><OfficeBuilding /></el-icon>
            <span>公司信息</span>
          </div>
        </template>

        <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
          <div class="flex justify-between items-center mb-6">
            <h3 class="text-lg font-semibold text-gray-800">公司信息</h3>
            <span class="text-xs text-gray-500">可直接修改，修改后保存</span>
          </div>

          <el-form label-position="top" class="max-w-2xl">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-x-6">
              <el-form-item label="公司名称" required>
                <el-input 
                  v-model="companyForm.companyName" 
                  placeholder="请输入公司全称" 
                />
              </el-form-item>
              <el-form-item label="统一社会信用代码">
                <el-input 
                  v-model="companyForm.licenseNo" 
                  placeholder="请输入统一社会信用代码" 
                />
              </el-form-item>
              <el-form-item label="联系人">
                <el-input 
                  v-model="companyForm.contacts" 
                  placeholder="请输入公司联系人" 
                />
              </el-form-item>
              <el-form-item label="联系电话">
                <el-input 
                  v-model="companyForm.phone" 
                  placeholder="请输入公司联系电话" 
                />
              </el-form-item>
              <el-form-item label="省份">
                <el-input 
                  v-model="companyForm.province" 
                  placeholder="如：山东省" 
                />
              </el-form-item>
              <el-form-item label="城市">
                <el-input 
                  v-model="companyForm.city" 
                  placeholder="如：济南市" 
                />
              </el-form-item>
              <el-form-item label="区/县">
                <el-input 
                  v-model="companyForm.district" 
                  placeholder="如：历下区" 
                />
              </el-form-item>
            </div>
            <el-form-item label="详细地址">
              <el-input 
                v-model="companyForm.address" 
                placeholder="请输入详细地址（街道、门牌号等）" 
              />
            </el-form-item>
          </el-form>

          <!-- 公司状态提示 -->
          <div class="mt-6 pt-6 border-t border-gray-100">
            <div class="flex items-center gap-3 p-4 rounded-xl" :class="company?.id ? 'bg-green-50' : 'bg-yellow-50'">
              <div class="w-10 h-10 rounded-full flex items-center justify-center" :class="company?.id ? 'bg-green-100' : 'bg-yellow-100'">
                <el-icon v-if="company?.id" class="text-green-600" :size="20"><Check /></el-icon>
                <el-icon v-else class="text-yellow-600" :size="20"><Warning /></el-icon>
              </div>
              <div>
                <div class="font-medium" :class="company?.id ? 'text-green-800' : 'text-yellow-800'">
                  {{ company?.id ? '公司信息已完善' : '公司信息未完善' }}
                </div>
                <div class="text-sm" :class="company?.id ? 'text-green-600' : 'text-yellow-600'">
                  {{ company?.id ? '您的公司信息已保存，可以正常使用平台功能' : '请完善公司信息，以便更好地使用平台功能' }}
                </div>
              </div>
            </div>
          </div>

          <!-- 保存/取消条：仅在有改动时出现 -->
          <div v-if="companyDirty" class="mt-8 pt-6 border-t border-gray-100 flex items-center justify-end gap-3">
            <button
              type="button"
              class="px-4 py-2 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm font-bold transition-all active:scale-95"
              :disabled="loading"
              @click="resetCompanyForm"
            >
              取消
            </button>
            <button
              type="button"
              class="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-bold transition-all active:scale-95 disabled:opacity-60"
              :disabled="loading"
              @click="saveCompanyInfo"
            >
              保存
            </button>
          </div>
        </div>
      </el-tab-pane>

      <!-- 账户安全 - 只保留密码修改 -->
      <el-tab-pane name="security">
        <template #label>
          <div class="flex items-center gap-2">
            <el-icon><Lock /></el-icon>
            <span>账户安全</span>
          </div>
        </template>

        <div class="bg-white rounded-2xl border border-gray-100 shadow-sm p-6">
          <h3 class="text-lg font-semibold text-gray-800 mb-6">修改密码</h3>

          <el-form label-position="top" class="max-w-md">
            <el-form-item label="原密码">
              <el-input 
                v-model="passwordForm.oldPassword" 
                type="password" 
                show-password
                placeholder="请输入原密码" 
              />
            </el-form-item>
            <el-form-item label="新密码">
              <el-input 
                v-model="passwordForm.newPassword" 
                type="password" 
                show-password
                placeholder="请输入新密码（至少6位）" 
              />
            </el-form-item>
            <el-form-item label="确认新密码">
              <el-input 
                v-model="passwordForm.confirmPassword" 
                type="password" 
                show-password
                placeholder="请再次输入新密码" 
              />
            </el-form-item>
          </el-form>

          <!-- 保存/取消条：仅在有输入时出现 -->
          <div v-if="passwordDirty" class="mt-6 pt-6 border-t border-gray-100 flex items-center justify-end gap-3">
            <button
              type="button"
              class="px-4 py-2 rounded-xl bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm font-bold transition-all active:scale-95"
              :disabled="loading"
              @click="resetPasswordForm"
            >
              取消
            </button>
            <button
              type="button"
              class="px-4 py-2 rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white text-sm font-bold transition-all active:scale-95 disabled:opacity-60"
              :disabled="loading"
              @click="changePassword"
            >
              确认修改
            </button>
          </div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.profile-tabs :deep(.el-tabs__header) {
  display: none;
}

.profile-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.profile-tabs :deep(.el-tabs__item) {
  padding: 0 24px;
  height: 50px;
  line-height: 50px;
}

.profile-tabs :deep(.el-tabs__content) { padding-top: 0; }
</style>
