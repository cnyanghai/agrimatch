<script setup lang="ts">
import { ref, computed } from 'vue'
import { BRAND_600 } from '../../constants/colors'
import { onLoad } from '@dcloudio/uni-app'

type PageType = 'terms' | 'privacy' | 'e-signature' | 'disclaimer' | 'cooperation' | 'feedback'

const COMPANY_NAME = '天津市东丽区农汇通网络科技信息咨询厅'
const PLATFORM_NAME = '沃谷'
const SERVICE_EMAIL = 'cnyanghai@icloud.com'
const EFFECTIVE_DATE = '2026年2月1日'

const pageType = ref<PageType>('terms')

const pageMeta: Record<PageType, { title: string; icon: string }> = {
  terms: { title: '用户协议', icon: 'list' },
  privacy: { title: '隐私政策', icon: 'locked' },
  'e-signature': { title: '电子签约法律效力', icon: 'compose' },
  disclaimer: { title: '免责声明', icon: 'info' },
  cooperation: { title: '商务合作', icon: 'handUp' },
  feedback: { title: '意见反馈', icon: 'chatboxes' },
}

const currentMeta = computed(() => pageMeta[pageType.value] || pageMeta.terms)

const navItems: { type: PageType; label: string }[] = [
  { type: 'terms', label: '用户协议' },
  { type: 'privacy', label: '隐私政策' },
  { type: 'e-signature', label: '电子签约效力' },
  { type: 'disclaimer', label: '免责声明' },
  { type: 'cooperation', label: '商务合作' },
  { type: 'feedback', label: '意见反馈' },
]

onLoad((options) => {
  if (options?.type && pageMeta[options.type as PageType]) {
    pageType.value = options.type as PageType
  }
  uni.setNavigationBarTitle({ title: currentMeta.value.title })
})

function switchType(type: PageType) {
  pageType.value = type
  uni.setNavigationBarTitle({ title: pageMeta[type].title })
}
</script>

<template>
  <view class="legal-page">
    <!-- Tab navigation -->
    <scroll-view scroll-x class="type-tabs" :show-scrollbar="false">
      <view class="type-tabs__inner">
        <view
          v-for="nav in navItems"
          :key="nav.type"
          class="type-tabs__tab"
          :class="{ 'type-tabs__tab--active': pageType === nav.type }"
          @tap="switchType(nav.type)"
        >
          <text>{{ nav.label }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- Content -->
    <scroll-view scroll-y class="content-scroll">
      <!-- Terms -->
      <view v-if="pageType === 'terms'" class="legal-content">
        <view class="legal-content__header">
          <text class="legal-content__title">{{ PLATFORM_NAME }}平台用户协议</text>
          <text class="legal-content__date">生效日期：{{ EFFECTIVE_DATE }}</text>
        </view>
        <view class="legal-section">
          <text class="legal-section__title">一、服务说明</text>
          <text class="legal-section__text">{{ PLATFORM_NAME }}平台（以下简称"平台"）由{{ COMPANY_NAME }}（以下简称"我们"）运营。平台致力于为农牧原料行业的供应商和采购商提供数字化供需匹配、信息发布、即时沟通和交易撮合服务。</text>
        </view>
        <view class="legal-section">
          <text class="legal-section__title">二、用户注册</text>
          <text class="legal-section__text">1. 用户注册时需提供真实、准确的信息，包括但不限于手机号码、企业名称等。</text>
          <text class="legal-section__text">2. 用户需妥善保管登录凭证，因个人保管不当导致的损失由用户自行承担。</text>
          <text class="legal-section__text">3. 用户注册即视为同意本协议全部条款。</text>
        </view>
        <view class="legal-section">
          <text class="legal-section__title">三、用户行为规范</text>
          <text class="legal-section__text">1. 用户不得利用平台发布虚假信息、不当言论或进行违法活动。</text>
          <text class="legal-section__text">2. 发布的供应/需求信息必须真实、合法，不得侵犯他人权益。</text>
          <text class="legal-section__text">3. 平台有权对违规内容和账号进行处理，包括但不限于删除内容、暂停或终止服务。</text>
        </view>
        <view class="legal-section">
          <text class="legal-section__title">四、知识产权</text>
          <text class="legal-section__text">平台上的所有内容，包括但不限于文字、图片、标识、软件等，均受相关知识产权法律法规保护。未经许可，不得擅自使用。</text>
        </view>
        <view class="legal-section">
          <text class="legal-section__title">五、争议解决</text>
          <text class="legal-section__text">本协议适用中华人民共和国法律。因本协议引发的任何争议，双方应首先协商解决；协商不成的，任何一方可向平台所在地有管辖权的人民法院提起诉讼。</text>
        </view>
      </view>

      <!-- Privacy -->
      <view v-if="pageType === 'privacy'" class="legal-content">
        <view class="legal-content__header">
          <text class="legal-content__title">{{ PLATFORM_NAME }}隐私政策</text>
          <text class="legal-content__date">生效日期：{{ EFFECTIVE_DATE }}</text>
        </view>
        <view class="legal-section">
          <text class="legal-section__title">一、信息收集</text>
          <text class="legal-section__text">我们遵循最小必要原则，仅收集为您提供服务所必需的个人信息：</text>
          <text class="legal-section__text">1. 注册信息：手机号码、用户名、密码（加密存储）</text>
          <text class="legal-section__text">2. 企业信息：企业名称、统一社会信用代码、联系人信息</text>
          <text class="legal-section__text">3. 交易信息：供需发布内容、成交记录</text>
          <text class="legal-section__text">4. 设备信息：设备型号、操作系统版本（用于优化体验）</text>
        </view>
        <view class="legal-section">
          <text class="legal-section__title">二、信息使用</text>
          <text class="legal-section__text">收集的信息仅用于：</text>
          <text class="legal-section__text">1. 提供核心服务功能（供需匹配、交易撮合）</text>
          <text class="legal-section__text">2. 改善服务质量和用户体验</text>
          <text class="legal-section__text">3. 安全保障和风险防控</text>
          <text class="legal-section__text">4. 法律法规要求的义务</text>
        </view>
        <view class="legal-section">
          <text class="legal-section__title">三、信息保护</text>
          <text class="legal-section__text">1. 敏感信息进行脱敏展示（手机号中间4位、身份证中间8位等）</text>
          <text class="legal-section__text">2. 密码使用BCrypt加密存储</text>
          <text class="legal-section__text">3. 采用HTTPS加密传输</text>
          <text class="legal-section__text">4. 严格的数据访问权限控制</text>
        </view>
        <view class="legal-section">
          <text class="legal-section__title">四、用户权利</text>
          <text class="legal-section__text">您有权：查阅、更正个人信息；删除账户及相关数据；撤回授权同意。</text>
        </view>
      </view>

      <!-- E-Signature -->
      <view v-if="pageType === 'e-signature'" class="legal-content">
        <view class="legal-content__header">
          <text class="legal-content__title">电子签约法律效力说明</text>
          <text class="legal-content__date">生效日期：{{ EFFECTIVE_DATE }}</text>
        </view>
        <view class="legal-section">
          <text class="legal-section__title">法律依据</text>
          <text class="legal-section__text">根据《中华人民共和国电子签名法》、《中华人民共和国民法典》相关规定，通过本平台签署的电子合同具有与纸质合同同等的法律效力。</text>
        </view>
        <view class="legal-section">
          <text class="legal-section__title">签署流程保障</text>
          <text class="legal-section__text">1. 身份认证：签署前验证用户身份（手机号/实名认证）</text>
          <text class="legal-section__text">2. 签署意愿：明确的确认动作（勾选 + 点击确认）</text>
          <text class="legal-section__text">3. 完整性保护：合同内容SHA256哈希存证</text>
          <text class="legal-section__text">4. 时间戳：签署时间精确到毫秒</text>
          <text class="legal-section__text">5. 不可篡改：签署后合同内容锁定</text>
        </view>
      </view>

      <!-- Disclaimer -->
      <view v-if="pageType === 'disclaimer'" class="legal-content">
        <view class="legal-content__header">
          <text class="legal-content__title">免责声明</text>
          <text class="legal-content__date">生效日期：{{ EFFECTIVE_DATE }}</text>
        </view>
        <view class="legal-section">
          <text class="legal-section__text">1. 平台作为信息中介，不对交易双方的商品质量、交付履约等承担担保责任。</text>
          <text class="legal-section__text">2. 用户发布的信息由用户自行承担法律责任，平台不对其真实性、准确性做保证。</text>
          <text class="legal-section__text">3. 因不可抗力（包括但不限于自然灾害、政策变化、网络故障等）导致的服务中断或损失，平台不承担责任。</text>
          <text class="legal-section__text">4. 平台展示的到厂价、距离等数据为估算值，仅供参考，不构成交易承诺。</text>
        </view>
      </view>

      <!-- Cooperation -->
      <view v-if="pageType === 'cooperation'" class="legal-content">
        <view class="legal-content__header">
          <text class="legal-content__title">商务合作</text>
        </view>
        <view class="legal-section">
          <text class="legal-section__text">我们欢迎各类合作伙伴与我们共同推动农牧原料行业的数字化转型。</text>
        </view>
        <view class="contact-card">
          <view class="contact-item">
            <WgIcon name="mail" :size="18" :color="BRAND_600" />
            <text class="contact-item__label">商务邮箱</text>
            <text class="contact-item__value" selectable>{{ SERVICE_EMAIL }}</text>
          </view>
          <view class="contact-item">
            <WgIcon name="building" :size="18" :color="BRAND_600" />
            <text class="contact-item__label">运营主体</text>
            <text class="contact-item__value">{{ COMPANY_NAME }}</text>
          </view>
        </view>
      </view>

      <!-- Feedback -->
      <view v-if="pageType === 'feedback'" class="legal-content">
        <view class="legal-content__header">
          <text class="legal-content__title">意见反馈</text>
        </view>
        <view class="legal-section">
          <text class="legal-section__text">您的建议对我们非常重要。如有任何问题或建议，请通过以下方式联系我们：</text>
        </view>
        <view class="contact-card">
          <view class="contact-item">
            <WgIcon name="mail" :size="18" :color="BRAND_600" />
            <text class="contact-item__label">反馈邮箱</text>
            <text class="contact-item__value" selectable>{{ SERVICE_EMAIL }}</text>
          </view>
        </view>
        <view class="legal-section">
          <text class="legal-section__text">我们会在收到反馈后的3个工作日内回复您。感谢您对{{ PLATFORM_NAME }}平台的支持！</text>
        </view>
      </view>

      <view style="height: 60rpx;" />
    </scroll-view>
  </view>
</template>

<style lang="scss" scoped>
.legal-page {
  min-height: 100vh;
  background: $bg-page;
  display: flex;
  flex-direction: column;
}

/* Tab navigation */
.type-tabs {
  background: $bg-card;
  white-space: nowrap;
  border-bottom: 1rpx solid $border-light;

  &__inner {
    display: inline-flex;
    padding: $spacing-sm $spacing-md;
    gap: $spacing-sm;
  }

  &__tab {
    display: inline-block;
    font-size: $font-sm;
    color: $text-secondary;
    padding: $spacing-xs $spacing-md;
    border-radius: $radius-pill;
    background: $warm-100;
    white-space: nowrap;
    transition: all 0.2s;

    &--active {
      color: $text-inverse;
      background: $brand-600;
      font-weight: 600;
    }
  }
}

/* Content scroll */
.content-scroll {
  flex: 1;
}

/* Legal content */
.legal-content {
  padding: $spacing-md;

  &__header {
    background: $bg-card;
    border-radius: $radius-xl;
    padding: $spacing-lg;
    margin-bottom: $spacing-md;
    box-shadow: $shadow-warm-card;
  }

  &__title {
    display: block;
    font-size: $font-xl;
    font-weight: bold;
    color: $text-primary;
    margin-bottom: $spacing-xs;
  }

  &__date {
    display: block;
    font-size: $font-sm;
    color: $text-placeholder;
  }
}

.legal-section {
  background: $bg-card;
  border-radius: $radius-xl;
  padding: $spacing-lg;
  margin-bottom: $spacing-sm;
  box-shadow: $shadow-warm-card;

  &__title {
    display: block;
    font-size: $font-lg;
    font-weight: bold;
    color: $text-primary;
    margin-bottom: $spacing-sm;
  }

  &__text {
    display: block;
    font-size: $font-md;
    color: $text-secondary;
    line-height: 1.8;
    margin-bottom: $spacing-xs;
  }
}

/* Contact card */
.contact-card {
  background: $bg-card;
  border-radius: $radius-xl;
  overflow: hidden;
  margin-bottom: $spacing-sm;
  box-shadow: $shadow-warm-card;
}

.contact-item {
  display: flex;
  align-items: center;
  gap: $spacing-sm;
  padding: $spacing-lg;
  border-bottom: 1rpx solid $border-light;

  &:last-child {
    border-bottom: none;
  }

  &__label {
    font-size: $font-md;
    color: $text-secondary;
    width: 140rpx;
    flex-shrink: 0;
  }

  &__value {
    flex: 1;
    font-size: $font-md;
    color: $text-primary;
    font-weight: 500;
  }
}
</style>
