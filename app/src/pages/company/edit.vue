<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { BRAND_600, WARM_400, ACCENT_400 } from '../../constants/colors'
import { useAuthStore } from '../../store/auth'
import { getBaseUrl } from '../../utils/request'
import {
  getMyCompany,
  createCompany,
  updateCompany,
  companyTypeMap,
  type CompanyResponse,
  type CompanyCreateRequest,
} from '../../api/company'

const authStore = useAuthStore()
const loading = ref(false)
const saving = ref(false)
const isEdit = ref(false)
const companyId = ref<number | null>(null)
const certificates = ref<string[]>([])
const certUploading = ref(false)

const form = reactive<CompanyCreateRequest>({
  companyName: '',
  companyType: '',
  licenseNo: '',
  legalPerson: '',
  businessScope: '',
  contacts: '',
  phone: '',
  wechat: '',
  province: '',
  city: '',
  district: '',
  address: '',
  registeredCapital: '',
  establishDate: '',
  scale: '',
  companyIntro: '',
  certificatesJson: '',
})

const companyTypes = Object.entries(companyTypeMap).map(([k, v]) => ({ value: k, label: v }))
const scaleOptions = ['1-10人', '11-50人', '51-200人', '201-500人', '500人以上']

const currentTypeName = computed(() => companyTypeMap[form.companyType || ''] || '请选择')

onLoad(() => {
  loadData()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getMyCompany()
    if (res && res.id) {
      isEdit.value = true
      companyId.value = res.id
      fillForm(res)
      uni.setNavigationBarTitle({ title: '编辑企业信息' })
    } else {
      uni.setNavigationBarTitle({ title: '创建企业信息' })
    }
  } catch {
    // no company yet
  } finally {
    loading.value = false
  }
}

function fillForm(c: CompanyResponse) {
  form.companyName = c.companyName || ''
  form.companyType = c.companyType || ''
  form.licenseNo = c.licenseNo || ''
  form.legalPerson = c.legalPerson || ''
  form.businessScope = c.businessScope || ''
  form.contacts = c.contacts || ''
  form.phone = c.phone || ''
  form.wechat = c.wechat || ''
  form.province = c.province || ''
  form.city = c.city || ''
  form.district = c.district || ''
  form.address = c.address || ''
  form.registeredCapital = c.registeredCapital || ''
  form.establishDate = c.establishDate || ''
  form.scale = c.scale || ''
  form.companyIntro = c.companyIntro || ''
  form.certificatesJson = c.certificatesJson || ''
  try {
    certificates.value = c.certificatesJson ? JSON.parse(c.certificatesJson) : []
  } catch { certificates.value = [] }
}

function pickCompanyType() {
  uni.showActionSheet({
    itemList: companyTypes.map(t => t.label),
    success: (res) => {
      form.companyType = companyTypes[res.tapIndex].value
    }
  })
}

function pickScale() {
  uni.showActionSheet({
    itemList: scaleOptions,
    success: (res) => {
      form.scale = scaleOptions[res.tapIndex]
    }
  })
}

function pickEstablishDate() {
  // picker handled by template
}

function uploadCertificate() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: (res) => {
      certUploading.value = true
      const token = uni.getStorageSync('token')
      uni.uploadFile({
        url: `${getBaseUrl()}/api/files/upload/image`,
        filePath: res.tempFilePaths[0],
        name: 'file',
        header: token ? { Authorization: `Bearer ${token}` } : {},
        success: (uploadRes) => {
          try {
            const data = JSON.parse(uploadRes.data)
            if (data.code === 0 && data.data?.fileUrl) {
              certificates.value.push(data.data.fileUrl)
              form.certificatesJson = JSON.stringify(certificates.value)
              uni.showToast({ title: '上传成功', icon: 'success' })
            }
          } catch { /* ignore */ }
        },
        complete: () => { certUploading.value = false }
      })
    }
  })
}

function removeCertificate(idx: number) {
  certificates.value.splice(idx, 1)
  form.certificatesJson = JSON.stringify(certificates.value)
}

function previewCert(url: string) {
  uni.previewImage({ urls: certificates.value, current: url })
}

async function handleSave() {
  if (!form.companyName?.trim()) {
    uni.showToast({ title: '请填写企业名称', icon: 'none' }); return
  }
  saving.value = true
  try {
    if (isEdit.value && companyId.value) {
      await updateCompany(companyId.value, form)
    } else {
      const newId = await createCompany(form)
      if (newId) {
        companyId.value = newId
        isEdit.value = true
      }
    }
    await authStore.checkSession()
    uni.showToast({ title: '保存成功', icon: 'success' })
    setTimeout(() => uni.navigateBack(), 1000)
  } catch (e: any) {
    uni.showToast({ title: e?.message || '保存失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <scroll-view scroll-y class="edit-company-page">
    <view v-if="loading" class="loading-wrap">
      <WgSkeleton :rows="8" />
    </view>

    <view v-else class="form-wrap">
      <!-- 基本信息 -->
      <view class="section-title">基本信息</view>
      <view class="form-card stitch-card">
        <view class="field">
          <text class="field__label">企业名称 <text class="field__required">*</text></text>
          <input v-model="form.companyName" placeholder="请输入企业名称" class="field__input" />
        </view>

        <view class="field" @tap="pickCompanyType">
          <text class="field__label">企业类型</text>
          <view class="field__picker">
            <text :class="form.companyType ? 'field__picker-val' : 'field__picker-placeholder'">
              {{ currentTypeName }}
            </text>
            <WgIcon name="chevron-right" :size="16" :color="WARM_400" />
          </view>
        </view>

        <view class="field">
          <text class="field__label">法人代表</text>
          <input v-model="form.legalPerson" placeholder="请输入法人姓名" class="field__input" />
        </view>

        <view class="field">
          <text class="field__label">营业执照号</text>
          <input v-model="form.licenseNo" placeholder="请输入统一社会信用代码" class="field__input" />
        </view>

        <view class="field">
          <text class="field__label">注册资本</text>
          <input v-model="form.registeredCapital" placeholder="如：500万元" class="field__input" />
        </view>

        <view class="field">
          <text class="field__label">成立日期</text>
          <picker mode="date" :value="form.establishDate" @change="(e: any) => form.establishDate = e.detail.value">
            <view class="field__picker">
              <text :class="form.establishDate ? 'field__picker-val' : 'field__picker-placeholder'">
                {{ form.establishDate || '请选择' }}
              </text>
              <WgIcon name="chevron-right" :size="16" :color="WARM_400" />
            </view>
          </picker>
        </view>

        <view class="field" @tap="pickScale">
          <text class="field__label">企业规模</text>
          <view class="field__picker">
            <text :class="form.scale ? 'field__picker-val' : 'field__picker-placeholder'">
              {{ form.scale || '请选择' }}
            </text>
            <WgIcon name="chevron-right" :size="16" :color="WARM_400" />
          </view>
        </view>

        <view class="field">
          <text class="field__label">经营范围</text>
          <textarea
            v-model="form.businessScope"
            placeholder="请输入经营范围"
            class="field__textarea"
            :maxlength="500"
            auto-height
          />
        </view>

        <view class="field">
          <text class="field__label">企业简介</text>
          <textarea
            v-model="form.companyIntro"
            placeholder="请输入企业简介"
            class="field__textarea"
            :maxlength="500"
            auto-height
          />
        </view>
      </view>

      <!-- 联系信息 -->
      <view class="section-title">联系信息</view>
      <view class="form-card stitch-card">
        <view class="field">
          <text class="field__label">联系人</text>
          <input v-model="form.contacts" placeholder="请输入联系人姓名" class="field__input" />
        </view>

        <view class="field">
          <text class="field__label">联系电话</text>
          <input v-model="form.phone" type="tel" placeholder="请输入联系电话" class="field__input" />
        </view>

        <view class="field">
          <text class="field__label">微信号</text>
          <input v-model="form.wechat" placeholder="请输入微信号（选填）" class="field__input" />
        </view>
      </view>

      <!-- 地址信息 -->
      <view class="section-title">地址信息</view>
      <view class="form-card stitch-card">
        <view class="field">
          <text class="field__label">省份</text>
          <input v-model="form.province" placeholder="如：山东省" class="field__input" />
        </view>
        <view class="field">
          <text class="field__label">城市</text>
          <input v-model="form.city" placeholder="如：济南市" class="field__input" />
        </view>
        <view class="field">
          <text class="field__label">区/县</text>
          <input v-model="form.district" placeholder="如：历下区" class="field__input" />
        </view>
        <view class="field">
          <text class="field__label">详细地址</text>
          <input v-model="form.address" placeholder="请输入详细地址" class="field__input" />
        </view>
      </view>

      <!-- 资质证照 -->
      <view class="section-title">资质证照</view>
      <view class="form-card stitch-card">
        <view class="cert-grid">
          <view
            v-for="(url, idx) in certificates"
            :key="idx"
            class="cert-item"
          >
            <image :src="url" class="cert-item__img" mode="aspectFill" @tap="previewCert(url)" />
            <view class="cert-item__del" @tap="removeCertificate(idx)">
              <WgIcon name="x" :size="14" color="#fff" />
            </view>
          </view>

          <view
            class="cert-upload"
            :class="{ 'cert-upload--loading': certUploading }"
            @tap="uploadCertificate"
          >
            <WgIcon v-if="!certUploading" name="plus" :size="28" :color="WARM_400" />
            <text v-else class="cert-upload__text">上传中...</text>
            <text v-if="!certUploading" class="cert-upload__text">上传证书</text>
          </view>
        </view>
        <text class="cert-hint">上传企业相关资质证书，可提升信誉等级</text>
      </view>

      <!-- 保存按钮 -->
      <view class="btn-wrap">
        <button
          class="save-btn"
          :class="{ 'save-btn--loading': saving }"
          :disabled="saving"
          @tap="handleSave"
        >
          {{ saving ? '保存中...' : (isEdit ? '保存修改' : '创建企业') }}
        </button>
      </view>

      <view style="height: 80rpx;" />
    </view>
  </scroll-view>
</template>

<style lang="scss" scoped>
.edit-company-page {
  height: 100vh;
  background: $bg-page;
}

.loading-wrap {
  padding: $spacing-lg;
}

.form-wrap {
  padding: $spacing-md;
}

.section-title {
  font-size: $font-md;
  font-weight: 700;
  color: $text-primary;
  margin-bottom: $spacing-sm;
  margin-top: $spacing-md;
  padding-left: $spacing-xs;
}

.form-card {
  padding: $spacing-md;
  margin-bottom: $spacing-sm;
}

.field {
  margin-bottom: $spacing-md;

  &__label {
    display: block;
    font-size: $font-sm;
    color: $text-secondary;
    font-weight: 600;
    margin-bottom: $spacing-xs;
  }

  &__required {
    color: $color-error;
  }

  &__input {
    display: block;
    width: 100%;
    height: 84rpx;
    padding: 0 $spacing-md;
    background: $bg-page;
    border: 2rpx solid $border-color;
    border-radius: $radius-md;
    font-size: $font-md;
    color: $text-primary;
    box-sizing: border-box;
  }

  &__textarea {
    display: block;
    width: 100%;
    min-height: 160rpx;
    padding: $spacing-sm $spacing-md;
    background: $bg-page;
    border: 2rpx solid $border-color;
    border-radius: $radius-md;
    font-size: $font-md;
    color: $text-primary;
    box-sizing: border-box;
  }

  &__picker {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 84rpx;
    padding: 0 $spacing-md;
    background: $bg-page;
    border: 2rpx solid $border-color;
    border-radius: $radius-md;
  }

  &__picker-val {
    font-size: $font-md;
    color: $text-primary;
  }

  &__picker-placeholder {
    font-size: $font-md;
    color: $text-placeholder;
  }
}

.btn-wrap {
  padding: $spacing-lg 0;
}

.save-btn {
  width: 100%;
  height: 96rpx;
  line-height: 96rpx;
  background: $color-brand;
  color: white;
  border-radius: $radius-lg;
  font-size: $font-md;
  font-weight: 700;

  &--loading {
    opacity: 0.6;
  }
}

/* ===== 资质证照 ===== */
.cert-grid {
  display: flex;
  flex-wrap: wrap;
  gap: $spacing-sm;
}

.cert-item {
  position: relative;
  width: 200rpx;
  height: 200rpx;
  border-radius: $radius-md;
  overflow: hidden;

  &__img {
    width: 100%;
    height: 100%;
  }

  &__del {
    position: absolute;
    top: 4rpx;
    right: 4rpx;
    width: 40rpx;
    height: 40rpx;
    border-radius: 50%;
    background: rgba(0,0,0,0.5);
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.cert-upload {
  width: 200rpx;
  height: 200rpx;
  border: 2rpx dashed $border-color;
  border-radius: $radius-md;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8rpx;

  &--loading {
    opacity: 0.5;
  }

  &__text {
    font-size: $font-xs;
    color: $text-placeholder;
  }
}

.cert-hint {
  display: block;
  font-size: $font-xs;
  color: $text-placeholder;
  margin-top: $spacing-sm;
}
</style>
