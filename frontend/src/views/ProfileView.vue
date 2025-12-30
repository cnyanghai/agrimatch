<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../store/auth'
import { updateMe, type UserUpdateRequest } from '../api/user'
import { getMyCompany, createCompany, updateCompany, type CompanyResponse, type CompanyCreateRequest } from '../api/company'
import { User, OfficeBuilding, Lock, Edit, Check, Upload, ShoppingCart, Box, Warning } from '@element-plus/icons-vue'

const auth = useAuthStore()
const loading = ref(false)
const activeTab = ref('user')
const isEditingUser = ref(false)
const isEditingCompany = ref(false)

// 用户身份
const isBuyer = computed(() => auth.me?.isBuyer === 1)
const isSeller = computed(() => auth.me?.isSeller === 1)

// 公司数据
const company = ref<CompanyResponse | null>(null)

// 用户表单
const userForm = reactive({
  realName: '',
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

// 初始化数据
onMounted(async () => {
  await loadUserData()
  await loadCompanyData()
})

async function loadUserData() {
  try {
    await auth.fetchMe()
    if (auth.me) {
      userForm.realName = auth.me.realName || auth.me.nickName || ''
      userForm.phonenumber = auth.me.phonenumber || ''
      userForm.position = auth.me.position || ''
      userForm.birthDate = auth.me.birthDate || ''
      userForm.gender = auth.me.gender || 1
      userForm.bio = auth.me.bio || ''
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
  } catch (e) {
    console.error('Failed to load company data', e)
  }
}

// 保存用户信息
async function saveUserInfo() {
  if (!userForm.realName?.trim()) {
    ElMessage.warning('请输入姓名')
    return
  }
  loading.value = true
  try {
    const req: UserUpdateRequest = {
      nickName: userForm.realName, // 同时更新昵称
      realName: userForm.realName,
      phonenumber: userForm.phonenumber,
      position: userForm.position,
      birthDate: userForm.birthDate,
      gender: userForm.gender,
      bio: userForm.bio
    }
    const res = await updateMe(req)
    if (res.code === 0) {
      ElMessage.success('保存成功')
      isEditingUser.value = false
      await auth.fetchMe()
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
      isEditingCompany.value = false
      await loadCompanyData()
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

// 取消编辑用户信息
function cancelEditUser() {
  isEditingUser.value = false
  loadUserData()
}

// 取消编辑公司信息
function cancelEditCompany() {
  isEditingCompany.value = false
  loadCompanyData()
}

// 性别选项 (不包含保密)
const genderOptions = [
  { label: '男', value: 1 },
  { label: '女', value: 2 }
]
</script>

<template>
  <div class="max-w-5xl mx-auto">
    <!-- 页面标题 -->
    <div class="mb-6">
      <h1 class="text-2xl font-bold text-gray-800">个人中心</h1>
      <p class="text-gray-500 mt-1">管理您的账户信息和公司资料</p>
    </div>

    <!-- 用户卡片 -->
    <div class="bg-white rounded-2xl shadow-sm mb-6 overflow-hidden">
      <div class="bg-gradient-to-r from-blue-600 to-blue-700 px-6 py-8">
        <div class="flex items-center gap-5">
          <!-- 头像 -->
          <div class="relative">
            <div class="w-20 h-20 rounded-full bg-white/20 flex items-center justify-center text-white text-3xl font-bold border-4 border-white/30">
              {{ (auth.me?.realName || auth.me?.nickName || auth.me?.userName || 'U')[0].toUpperCase() }}
            </div>
            <button class="absolute bottom-0 right-0 w-7 h-7 bg-white rounded-full flex items-center justify-center shadow-lg hover:bg-gray-50 transition-all">
              <el-icon class="text-blue-600"><Upload /></el-icon>
            </button>
          </div>
          <!-- 用户信息 -->
          <div class="text-white">
            <div class="text-xl font-bold">{{ auth.me?.realName || auth.me?.nickName || '未设置姓名' }}</div>
            <div class="text-blue-100 mt-1">{{ auth.me?.phonenumber || '未绑定手机号' }}</div>
            <div class="flex items-center gap-2 mt-3">
              <el-tag :type="isBuyer ? 'success' : 'warning'" effect="dark" class="!rounded-lg">
                {{ isBuyer ? '采购商' : '供应商' }}
              </el-tag>
              <el-tag type="info" effect="dark" class="!rounded-lg">
                ID: {{ auth.me?.userId || '-' }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Tab 内容 -->
    <el-tabs v-model="activeTab" class="profile-tabs">
      <!-- 用户基本信息 -->
      <el-tab-pane name="user">
        <template #label>
          <div class="flex items-center gap-2">
            <el-icon><User /></el-icon>
            <span>基本信息</span>
          </div>
        </template>

        <div class="bg-white rounded-2xl shadow-sm p-6">
          <div class="flex justify-between items-center mb-6">
            <h3 class="text-lg font-semibold text-gray-800">基本信息</h3>
            <el-button v-if="!isEditingUser" type="primary" :icon="Edit" @click="isEditingUser = true">编辑</el-button>
            <div v-else class="flex gap-2">
              <el-button @click="cancelEditUser">取消</el-button>
              <el-button type="primary" :icon="Check" :loading="loading" @click="saveUserInfo">保存</el-button>
            </div>
          </div>

          <el-form label-position="top" class="max-w-2xl">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-x-6">
              <el-form-item label="用户姓名" required>
                <el-input 
                  v-model="userForm.realName" 
                  :disabled="!isEditingUser"
                  placeholder="请输入您的真实姓名"
                />
              </el-form-item>
              <el-form-item label="手机号码">
                <el-input 
                  v-model="userForm.phonenumber" 
                  :disabled="!isEditingUser"
                  placeholder="请输入手机号"
                />
              </el-form-item>
              <el-form-item label="公司职务">
                <el-input 
                  v-model="userForm.position" 
                  :disabled="!isEditingUser"
                  placeholder="如：采购经理、销售总监"
                />
              </el-form-item>
              <el-form-item label="出生年月">
                <el-date-picker
                  v-model="userForm.birthDate"
                  type="month"
                  placeholder="选择出生年月"
                  :disabled="!isEditingUser"
                  format="YYYY年MM月"
                  value-format="YYYY-MM"
                  class="w-full"
                />
              </el-form-item>
              <el-form-item label="性别">
                <el-radio-group v-model="userForm.gender" :disabled="!isEditingUser">
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
                :disabled="!isEditingUser"
                placeholder="简单介绍一下自己，让合作伙伴更好地了解您"
                maxlength="500"
                show-word-limit
              />
            </el-form-item>
          </el-form>

          <!-- 身份切换 -->
          <div class="mt-8 pt-6 border-t border-gray-100">
            <h4 class="text-base font-semibold text-gray-800 mb-4">用户身份</h4>
            <p class="text-sm text-gray-500 mb-4">当前身份：<span :class="isBuyer ? 'text-blue-600' : 'text-orange-600'" class="font-medium">{{ isBuyer ? '采购商' : '供应商' }}</span></p>
            
            <div class="grid grid-cols-2 gap-4 max-w-lg">
              <div
                class="relative p-4 border-2 rounded-xl cursor-pointer transition-all"
                :class="isBuyer ? 'border-blue-500 bg-blue-50' : 'border-gray-200 hover:border-blue-300'"
                @click="!isBuyer && changeUserType('buyer')"
              >
                <div class="flex flex-col items-center">
                  <div class="w-12 h-12 rounded-full flex items-center justify-center mb-2"
                       :class="isBuyer ? 'bg-blue-500 text-white' : 'bg-gray-100 text-gray-500'">
                    <el-icon :size="24"><ShoppingCart /></el-icon>
                  </div>
                  <div class="font-medium" :class="isBuyer ? 'text-blue-700' : 'text-gray-700'">采购商</div>
                </div>
                <div v-if="isBuyer" class="absolute top-2 right-2 w-5 h-5 bg-blue-500 rounded-full flex items-center justify-center">
                  <el-icon class="text-white" :size="12"><Check /></el-icon>
                </div>
              </div>

              <div
                class="relative p-4 border-2 rounded-xl cursor-pointer transition-all"
                :class="isSeller ? 'border-orange-500 bg-orange-50' : 'border-gray-200 hover:border-orange-300'"
                @click="!isSeller && changeUserType('seller')"
              >
                <div class="flex flex-col items-center">
                  <div class="w-12 h-12 rounded-full flex items-center justify-center mb-2"
                       :class="isSeller ? 'bg-orange-500 text-white' : 'bg-gray-100 text-gray-500'">
                    <el-icon :size="24"><Box /></el-icon>
                  </div>
                  <div class="font-medium" :class="isSeller ? 'text-orange-700' : 'text-gray-700'">供应商</div>
                </div>
                <div v-if="isSeller" class="absolute top-2 right-2 w-5 h-5 bg-orange-500 rounded-full flex items-center justify-center">
                  <el-icon class="text-white" :size="12"><Check /></el-icon>
                </div>
              </div>
            </div>
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

        <div class="bg-white rounded-2xl shadow-sm p-6">
          <div class="flex justify-between items-center mb-6">
            <h3 class="text-lg font-semibold text-gray-800">公司信息</h3>
            <el-button v-if="!isEditingCompany" type="primary" :icon="Edit" @click="isEditingCompany = true">编辑</el-button>
            <div v-else class="flex gap-2">
              <el-button @click="cancelEditCompany">取消</el-button>
              <el-button type="primary" :icon="Check" :loading="loading" @click="saveCompanyInfo">保存</el-button>
            </div>
          </div>

          <el-form label-position="top" class="max-w-2xl">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-x-6">
              <el-form-item label="公司名称" required>
                <el-input 
                  v-model="companyForm.companyName" 
                  :disabled="!isEditingCompany"
                  placeholder="请输入公司全称" 
                />
              </el-form-item>
              <el-form-item label="统一社会信用代码">
                <el-input 
                  v-model="companyForm.licenseNo" 
                  :disabled="!isEditingCompany"
                  placeholder="请输入统一社会信用代码" 
                />
              </el-form-item>
              <el-form-item label="联系人">
                <el-input 
                  v-model="companyForm.contacts" 
                  :disabled="!isEditingCompany"
                  placeholder="请输入公司联系人" 
                />
              </el-form-item>
              <el-form-item label="联系电话">
                <el-input 
                  v-model="companyForm.phone" 
                  :disabled="!isEditingCompany"
                  placeholder="请输入公司联系电话" 
                />
              </el-form-item>
              <el-form-item label="省份">
                <el-input 
                  v-model="companyForm.province" 
                  :disabled="!isEditingCompany"
                  placeholder="如：山东省" 
                />
              </el-form-item>
              <el-form-item label="城市">
                <el-input 
                  v-model="companyForm.city" 
                  :disabled="!isEditingCompany"
                  placeholder="如：济南市" 
                />
              </el-form-item>
              <el-form-item label="区/县">
                <el-input 
                  v-model="companyForm.district" 
                  :disabled="!isEditingCompany"
                  placeholder="如：历下区" 
                />
              </el-form-item>
            </div>
            <el-form-item label="详细地址">
              <el-input 
                v-model="companyForm.address" 
                :disabled="!isEditingCompany"
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

        <div class="bg-white rounded-2xl shadow-sm p-6">
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
            <el-form-item>
              <el-button type="primary" :loading="loading" @click="changePassword">确认修改</el-button>
            </el-form-item>
          </el-form>
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<style scoped>
.profile-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.profile-tabs :deep(.el-tabs__nav-wrap::after) {
  display: none;
}

.profile-tabs :deep(.el-tabs__item) {
  padding: 0 24px;
  height: 50px;
  line-height: 50px;
}

.profile-tabs :deep(.el-tabs__content) {
  padding-top: 24px;
}
</style>
