<script setup lang="ts">
import { computed, watch, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChevronLeft, FileText, Shield, PenTool, AlertTriangle, Handshake, MessageSquare, Mail, MapPin, Clock } from 'lucide-vue-next'
import PublicFooter from '../components/PublicFooter.vue'

const route = useRoute()
const router = useRouter()

const COMPANY_NAME = '天津市东丽区农汇通网络科技信息咨询厅'
const PLATFORM_NAME = '沃谷'
const SERVICE_EMAIL = 'cnyanghai@icloud.com'
const EFFECTIVE_DATE = '2026年2月1日'

// 页面类型
type PageType = 'terms' | 'privacy' | 'e-signature' | 'disclaimer' | 'cooperation' | 'feedback'

const pageType = computed(() => (route.params.type as PageType) || 'terms')

const pageMeta: Record<PageType, { title: string; icon: any; subtitle: string }> = {
  terms: { title: '用户协议', icon: FileText, subtitle: '沃谷平台服务协议' },
  privacy: { title: '隐私政策', icon: Shield, subtitle: '个人信息保护政策' },
  'e-signature': { title: '电子签约法律效力', icon: PenTool, subtitle: '电子合同与电子签章说明' },
  disclaimer: { title: '免责声明', icon: AlertTriangle, subtitle: '平台责任限制声明' },
  cooperation: { title: '商务合作', icon: Handshake, subtitle: '合作咨询与联系方式' },
  feedback: { title: '意见反馈', icon: MessageSquare, subtitle: '帮助我们做得更好' },
}

const currentMeta = computed(() => pageMeta[pageType.value] || pageMeta.terms)

watch(pageType, () => {
  nextTick(() => window.scrollTo({ top: 0, behavior: 'smooth' }))
})

// 侧边导航快捷跳转
const navItems: { type: PageType; label: string }[] = [
  { type: 'terms', label: '用户协议' },
  { type: 'privacy', label: '隐私政策' },
  { type: 'e-signature', label: '电子签约效力' },
  { type: 'disclaimer', label: '免责声明' },
  { type: 'cooperation', label: '商务合作' },
  { type: 'feedback', label: '意见反馈' },
]
</script>

<template>
  <div class="min-h-screen bg-gray-50">
    <!-- 顶栏 -->
    <div class="bg-white border-b border-gray-200 sticky top-0 z-30">
      <div class="max-w-6xl mx-auto px-4 h-14 flex items-center gap-4">
        <button
          class="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-900 transition-colors"
          @click="router.push('/')"
        >
          <ChevronLeft class="w-4 h-4" />
          返回首页
        </button>
        <div class="w-px h-5 bg-gray-200"></div>
        <span class="text-sm font-bold text-gray-900">{{ currentMeta.title }}</span>
      </div>
    </div>

    <!-- 主体 -->
    <div class="max-w-6xl mx-auto px-4 py-10 flex gap-8">
      <!-- 左侧导航 -->
      <aside class="hidden lg:block w-56 shrink-0">
        <div class="sticky top-24 space-y-1">
          <div class="text-[10px] font-bold text-gray-400 uppercase tracking-widest mb-3 px-3">法律文档</div>
          <button
            v-for="nav in navItems"
            :key="nav.type"
            class="w-full text-left px-3 py-2 rounded-lg text-sm transition-all"
            :class="pageType === nav.type ? 'bg-brand-50 text-brand-700 font-bold' : 'text-gray-500 hover:text-gray-900 hover:bg-gray-100'"
            @click="router.push(`/legal/${nav.type}`)"
          >
            {{ nav.label }}
          </button>
        </div>
      </aside>

      <!-- 右侧正文 -->
      <main class="flex-1 min-w-0">
        <!-- 页头 -->
        <div class="bg-white rounded-2xl border border-gray-200 p-8 mb-6">
          <div class="flex items-start gap-4">
            <div class="w-12 h-12 rounded-xl bg-brand-50 flex items-center justify-center shrink-0">
              <component :is="currentMeta.icon" class="w-6 h-6 text-brand-600" />
            </div>
            <div>
              <h1 class="text-2xl font-bold text-gray-900">{{ currentMeta.title }}</h1>
              <p class="text-sm text-gray-500 mt-1">{{ currentMeta.subtitle }}</p>
              <div class="flex items-center gap-4 mt-3 text-xs text-gray-400">
                <span>生效日期：{{ EFFECTIVE_DATE }}</span>
                <span>运营主体：{{ COMPANY_NAME }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- 正文容器 -->
        <div class="bg-white rounded-2xl border border-gray-200 p-8 legal-content">

          <!-- ==================== 用户协议 ==================== -->
          <template v-if="pageType === 'terms'">
            <p class="text-sm text-gray-500 mb-8">欢迎您使用{{ PLATFORM_NAME }}平台！请您仔细阅读以下协议条款。注册或使用本平台即表示您已充分理解并同意本协议全部内容。</p>

            <h2 id="t1" data-anchor class="anchor-heading">一、定义与服务范围</h2>
            <p>1.1 「{{ PLATFORM_NAME }}平台」是指由{{ COMPANY_NAME }}（以下简称"本公司"）运营的农牧原料数字化交易撮合平台，包括但不限于网站、移动端应用及相关技术服务。</p>
            <p>1.2 本平台为企业用户（以下简称"用户"）提供农牧原料的供需信息发布、智能匹配、在线议价、电子合同签署、履约跟踪等服务。</p>
            <p>1.3 本平台为信息撮合服务平台，不直接参与买卖双方的交易行为，不对交易标的的质量、数量、价格承担担保责任。</p>

            <h2 id="t2" data-anchor class="anchor-heading">二、用户注册与账号管理</h2>
            <p>2.1 用户须使用真实有效的手机号码注册平台账号。每个手机号仅可注册一个账号。</p>
            <p>2.2 用户注册即视为同意本协议及《隐私政策》全部条款。</p>
            <p>2.3 用户应妥善保管账号及密码，因保管不善导致的账号被盗用、信息泄露等后果由用户自行承担。</p>
            <p>2.4 用户需完善企业档案（包括企业名称、统一社会信用代码、联系方式等），以获得完整的平台功能权限。</p>
            <p>2.5 本公司有权对提交的企业资质信息进行审核，对于信息不实、违规操作的账号，有权采取限制、冻结或注销等措施。</p>

            <h2 id="t3" data-anchor class="anchor-heading">三、信息发布规范</h2>
            <p>3.1 用户发布的供应信息、采购需求应当真实、准确、完整，不得发布虚假、误导性信息。</p>
            <p>3.2 发布的产品信息须包含品名、规格参数、数量、价格等核心要素，质量指标应符合相关国家标准或行业标准。</p>
            <p>3.3 严禁发布以下内容：违反法律法规的信息；假冒伪劣产品信息；侵犯他人知识产权的信息；涉及国家禁止或限制交易品种的信息。</p>
            <p>3.4 本公司有权对违规信息进行删除、屏蔽处理，并视情节轻重对相关账号采取处罚措施。</p>

            <h2 id="t4" data-anchor class="anchor-heading">四、交易规则</h2>
            <p>4.1 <strong>议价与报价：</strong>买卖双方通过平台聊天议价功能进行价格协商。报价信息在双方确认前不构成要约。</p>
            <p>4.2 <strong>合同生成：</strong>双方议价达成一致后，平台根据协商结果自动生成电子合同。合同内容包括品名、规格、数量、单价、总金额、交付方式、付款方式等核心条款。</p>
            <p>4.3 <strong>合同签署：</strong>双方通过平台电子签章功能签署合同。签署需经短信验证码确认，签署完成后合同即生效。</p>
            <p>4.4 <strong>履约跟踪：</strong>合同签署后进入履约阶段，双方应按合同约定履行各自义务。平台提供履约节点跟踪功能。</p>
            <p>4.5 买卖双方的货款结算、货物交付等事项由双方自行协商执行，本平台不参与也不承担相关责任。</p>

            <h2 id="t5" data-anchor class="anchor-heading">五、知识产权</h2>
            <p>5.1 本平台的软件、界面设计、商标、标识等知识产权归本公司所有，未经授权不得复制、修改或用于商业用途。</p>
            <p>5.2 用户在平台发布的原创内容，知识产权归用户所有。用户授予本公司在平台范围内展示、推广该内容的非排他性许可。</p>

            <h2 id="t6" data-anchor class="anchor-heading">六、违约与争议处理</h2>
            <p>6.1 因用户违反本协议导致本公司或第三方遭受损失的，用户应承担赔偿责任。</p>
            <p>6.2 买卖双方因交易产生的纠纷，应首先协商解决。协商不成的，可向本公司所在地人民法院提起诉讼。</p>
            <p>6.3 本协议的解释和执行适用中华人民共和国法律。</p>

            <h2 id="t7" data-anchor class="anchor-heading">七、协议变更与终止</h2>
            <p>7.1 本公司有权根据业务发展需要修改本协议。修改后的协议将在平台公告，自公告之日起生效。</p>
            <p>7.2 用户在协议变更后继续使用平台服务的，视为接受变更后的协议。</p>
            <p>7.3 用户可随时申请注销账号。注销后，本公司将在合理期限内删除用户个人信息（法律法规要求保留的除外）。</p>

            <div class="mt-10 pt-6 border-t border-gray-200 text-sm text-gray-500">
              <p>{{ COMPANY_NAME }}</p>
              <p>生效日期：{{ EFFECTIVE_DATE }}</p>
            </div>
          </template>

          <!-- ==================== 隐私政策 ==================== -->
          <template v-else-if="pageType === 'privacy'">
            <p class="text-sm text-gray-500 mb-8">{{ COMPANY_NAME }}（以下简称"我们"）深知个人信息对您的重要性，将尽全力保护您的个人信息安全。请您仔细阅读本隐私政策。</p>

            <h2 id="p1" data-anchor class="anchor-heading">一、我们收集的信息</h2>
            <h3>1.1 您主动提供的信息</h3>
            <ul>
              <li>注册信息：手机号码、登录密码</li>
              <li>企业信息：企业名称、统一社会信用代码、营业执照、联系地址、联系人、联系电话</li>
              <li>交易信息：供应/采购发布内容、议价记录、合同内容</li>
              <li>电子签章信息：上传的印鉴图片、签署记录</li>
            </ul>
            <h3>1.2 自动收集的信息</h3>
            <ul>
              <li>设备信息：浏览器类型、操作系统、屏幕分辨率</li>
              <li>日志信息：访问时间、访问页面、IP地址</li>
              <li>Cookie信息：用于维持登录状态和改善用户体验</li>
            </ul>

            <h2 id="p2" data-anchor class="anchor-heading">二、信息使用目的</h2>
            <p>我们收集和使用您的信息用于以下目的：</p>
            <ul>
              <li>提供核心服务：账号注册与登录、供需信息匹配、在线议价、合同签署</li>
              <li>身份验证：企业资质审核、电子签章的短信验证</li>
              <li>交易安全：风险防控、防止欺诈、交易纠纷处理</li>
              <li>平台优化：服务改进、用户体验分析（使用匿名化数据）</li>
              <li>通知与推送：交易状态变更通知、平台公告</li>
            </ul>

            <h2 id="p3" data-anchor class="anchor-heading">三、信息共享与披露</h2>
            <p>3.1 我们不会向第三方出售您的个人信息。</p>
            <p>3.2 在以下情形下，我们可能共享您的部分信息：</p>
            <ul>
              <li><strong>交易对手方：</strong>当您发起或接受交易时，我们会向交易对手方展示必要的企业信息和联系方式</li>
              <li><strong>法律要求：</strong>根据法律法规、诉讼需要或政府主管部门的强制要求</li>
              <li><strong>安全需要：</strong>为检测、预防欺诈等安全问题</li>
            </ul>

            <h2 id="p4" data-anchor class="anchor-heading">四、信息存储与安全</h2>
            <p>4.1 您的信息存储在中华人民共和国境内的服务器上。</p>
            <p>4.2 我们采取以下安全措施保护您的信息：</p>
            <ul>
              <li>传输加密：使用 HTTPS 协议加密数据传输</li>
              <li>存储安全：敏感信息加密存储，密码采用不可逆哈希算法</li>
              <li>访问控制：严格的权限管理，仅授权人员可访问用户数据</li>
              <li>安全审计：记录关键操作日志，定期安全检查</li>
            </ul>

            <h2 id="p5" data-anchor class="anchor-heading">五、您的权利</h2>
            <p>根据相关法律法规，您对个人信息享有以下权利：</p>
            <ul>
              <li><strong>查看权：</strong>您可在「账户设置」中查看您的个人信息</li>
              <li><strong>更正权：</strong>当您发现信息有误时，可随时更正</li>
              <li><strong>删除权：</strong>您可申请删除您的个人信息或注销账号</li>
              <li><strong>撤回同意：</strong>您可撤回对非必要信息收集的同意</li>
            </ul>
            <p>行使上述权利，请联系：{{ SERVICE_EMAIL }}</p>

            <h2 id="p6" data-anchor class="anchor-heading">六、Cookie 使用说明</h2>
            <p>6.1 我们使用 Cookie 和类似技术来维持您的登录状态，确保平台正常运行。</p>
            <p>6.2 我们不使用 Cookie 追踪您在其他网站上的活动。</p>
            <p>6.3 您可通过浏览器设置管理或删除 Cookie，但这可能影响平台的正常使用。</p>

            <h2 id="p7" data-anchor class="anchor-heading">七、未成年人保护</h2>
            <p>本平台面向企业用户，不针对未满18周岁的个人提供服务。如果我们发现在未获得法定监护人同意的情况下收集了未成年人的个人信息，将尽快删除相关信息。</p>

            <h2 id="p8" data-anchor class="anchor-heading">八、政策更新</h2>
            <p>我们可能适时修订本隐私政策。重大变更将通过平台公告、站内消息等方式通知您。</p>

            <div class="mt-10 pt-6 border-t border-gray-200 text-sm text-gray-500">
              <p>{{ COMPANY_NAME }}</p>
              <p>生效日期：{{ EFFECTIVE_DATE }}</p>
            </div>
          </template>

          <!-- ==================== 电子签约法律效力 ==================== -->
          <template v-else-if="pageType === 'e-signature'">
            <p class="text-sm text-gray-500 mb-8">本文阐述{{ PLATFORM_NAME }}平台电子合同与电子签章的法律效力依据及技术保障措施。</p>

            <h2 id="e1" data-anchor class="anchor-heading">一、法律依据</h2>
            <p>{{ PLATFORM_NAME }}平台的电子签约服务依据以下法律法规：</p>
            <ul>
              <li><strong>《中华人民共和国电子签名法》</strong>（2005年4月1日施行，2019年修正）</li>
              <li><strong>《中华人民共和国民法典》</strong>第四百六十九条：以电子数据交换等方式能够有形地表现所载内容，并可以随时调取查用的数据电文，视为书面形式</li>
              <li><strong>《中华人民共和国合同法》</strong>相关规定</li>
            </ul>
            <div class="bg-brand-50 border border-brand-200 rounded-xl p-4 my-4">
              <p class="text-sm text-brand-800 font-medium">《电子签名法》第十四条：可靠的电子签名与手写签名或者盖章具有同等的法律效力。</p>
            </div>

            <h2 id="e2" data-anchor class="anchor-heading">二、电子签约流程</h2>
            <p>本平台的电子签约流程遵循以下步骤，确保签署行为真实、可靠：</p>
            <ol>
              <li><strong>身份认证：</strong>签署方须完成企业实名认证，确保签署主体真实</li>
              <li><strong>印章管理：</strong>用户上传实体公章/合同章的印鉴照片，系统提取电子化印章图像</li>
              <li><strong>合同确认：</strong>双方在线议价达成一致后，系统生成标准化合同文本，双方确认合同内容</li>
              <li><strong>签署验证：</strong>签署方选择已认证的电子印章，并通过手机短信验证码进行身份二次确认</li>
              <li><strong>签署完成：</strong>验证通过后，电子印章加盖至合同文档，系统记录签署时间、签署方信息</li>
            </ol>

            <h2 id="e3" data-anchor class="anchor-heading">三、可靠性保障</h2>
            <p>根据《电子签名法》第十三条，可靠电子签名需满足以下条件，本平台均予以满足：</p>
            <table class="w-full text-sm border-collapse my-4">
              <thead>
                <tr class="border-b-2 border-gray-200">
                  <th class="py-3 text-left font-bold text-gray-700">法定要求</th>
                  <th class="py-3 text-left font-bold text-gray-700">平台实现</th>
                </tr>
              </thead>
              <tbody>
                <tr class="border-b border-gray-100">
                  <td class="py-3 text-gray-600">电子签名为签名人专有</td>
                  <td class="py-3 text-gray-600">印章与企业账号绑定，仅授权人员可使用</td>
                </tr>
                <tr class="border-b border-gray-100">
                  <td class="py-3 text-gray-600">签署时电子签名仅由签名人控制</td>
                  <td class="py-3 text-gray-600">签署需输入手机短信验证码，确保签名人亲自操作</td>
                </tr>
                <tr class="border-b border-gray-100">
                  <td class="py-3 text-gray-600">签署后对签名的任何改动可被发现</td>
                  <td class="py-3 text-gray-600">合同签署后锁定，系统记录完整审计日志</td>
                </tr>
                <tr>
                  <td class="py-3 text-gray-600">签署后对内容的任何改动可被发现</td>
                  <td class="py-3 text-gray-600">合同内容签署后不可修改，保留原始数据记录</td>
                </tr>
              </tbody>
            </table>

            <h2 id="e4" data-anchor class="anchor-heading">四、数据存证</h2>
            <p>4.1 所有电子合同及签署记录在平台服务器安全存储，保存期限不少于合同履行完毕后五年。</p>
            <p>4.2 签署日志包括：签署人身份信息、签署时间（精确到毫秒）、签署IP地址、短信验证记录、印章使用记录。</p>
            <p>4.3 用户可随时在「合同管理」中查看和下载已签署的电子合同。</p>

            <h2 id="e5" data-anchor class="anchor-heading">五、法律效力说明</h2>
            <p>5.1 通过本平台签署的电子合同，在符合上述可靠性条件的前提下，与纸质合同具有同等法律效力。</p>
            <p>5.2 如发生争议，电子合同及平台签署日志可作为有效的电子证据。</p>
            <p>5.3 根据《民事诉讼法》第六十六条，电子数据属于法定证据类型之一。</p>

            <div class="mt-10 pt-6 border-t border-gray-200 text-sm text-gray-500">
              <p>{{ COMPANY_NAME }}</p>
              <p>生效日期：{{ EFFECTIVE_DATE }}</p>
            </div>
          </template>

          <!-- ==================== 免责声明 ==================== -->
          <template v-else-if="pageType === 'disclaimer'">
            <p class="text-sm text-gray-500 mb-8">在使用{{ PLATFORM_NAME }}平台前，请仔细阅读以下免责声明。</p>

            <h2 id="d1" data-anchor class="anchor-heading">一、平台角色声明</h2>
            <p>1.1 {{ PLATFORM_NAME }}平台是农牧原料供需信息撮合服务平台，为买卖双方提供信息展示、智能匹配、在线沟通和电子签约等技术服务。</p>
            <p>1.2 <strong>本平台不直接参与任何交易</strong>，不作为买卖任何一方的代理人。买卖双方之间的交易关系独立于平台。</p>
            <p>1.3 平台上展示的产品信息由发布用户自行提供，本公司不对信息的真实性、准确性和合法性作出保证。</p>

            <h2 id="d2" data-anchor class="anchor-heading">二、交易风险提示</h2>
            <div class="bg-amber-50 border border-amber-200 rounded-xl p-4 my-4">
              <p class="text-sm text-amber-800 font-medium">农牧原料交易涉及价格波动、质量差异、运输损耗等风险，请用户在交易前审慎评估。</p>
            </div>
            <p>2.1 <strong>价格风险：</strong>农牧产品价格受市场供需、季节、政策等因素影响，可能发生较大波动。平台展示的价格仅为报价参考，不构成价格承诺。</p>
            <p>2.2 <strong>质量风险：</strong>本平台不对交易标的的质量、数量、规格等进行实物验证。建议买方在交易前要求卖方提供质量检测报告，或约定到货验收标准。</p>
            <p>2.3 <strong>履约风险：</strong>本平台不对交易双方的履约能力和意愿提供担保。因一方违约导致另一方损失的，由违约方承担法律责任。</p>
            <p>2.4 <strong>支付风险：</strong>货款结算由买卖双方自行安排，本平台不提供支付托管或担保。</p>

            <h2 id="d3" data-anchor class="anchor-heading">三、技术服务限制</h2>
            <p>3.1 本公司将尽力保障平台的正常运行，但不承诺平台服务不中断、不延迟、不出错。</p>
            <p>3.2 因以下原因导致的服务中断或数据损失，本公司不承担责任：</p>
            <ul>
              <li>不可抗力（自然灾害、战争、政策变化等）</li>
              <li>基础电信运营商的故障</li>
              <li>计算机病毒、黑客攻击等安全事件</li>
              <li>系统维护和升级（将提前公告）</li>
            </ul>

            <h2 id="d4" data-anchor class="anchor-heading">四、第三方服务</h2>
            <p>4.1 本平台可能包含指向第三方网站或服务的链接，本公司不对第三方的内容、隐私政策或行为负责。</p>
            <p>4.2 用户使用第三方服务时，应自行了解并遵守其相关条款。</p>

            <h2 id="d5" data-anchor class="anchor-heading">五、责任限制</h2>
            <p>5.1 在法律允许的最大范围内，本公司对因使用或无法使用本平台而导致的任何间接损失、利润损失、数据损失不承担责任。</p>
            <p>5.2 本公司对平台服务承担的全部责任，不超过相关用户向本公司支付的服务费用（如有）。</p>

            <div class="mt-10 pt-6 border-t border-gray-200 text-sm text-gray-500">
              <p>{{ COMPANY_NAME }}</p>
              <p>生效日期：{{ EFFECTIVE_DATE }}</p>
            </div>
          </template>

          <!-- ==================== 商务合作 ==================== -->
          <template v-else-if="pageType === 'cooperation'">
            <p class="text-sm text-gray-500 mb-8">{{ PLATFORM_NAME }}致力于构建农牧产业数字化生态，欢迎各类合作伙伴洽谈合作。</p>

            <h2 id="c1" data-anchor class="anchor-heading">一、合作方向</h2>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-4 my-6">
              <div class="border border-gray-200 rounded-xl p-5">
                <h3 class="font-bold text-gray-900 mb-2">产业链合作</h3>
                <p class="text-sm text-gray-600">饲料厂、养殖场、贸易商、加工企业等产业上下游企业的批量入驻及定制化服务合作。</p>
              </div>
              <div class="border border-gray-200 rounded-xl p-5">
                <h3 class="font-bold text-gray-900 mb-2">物流服务合作</h3>
                <p class="text-sm text-gray-600">物流公司、车队、仓储企业的运力接入合作，为平台用户提供配套物流服务。</p>
              </div>
              <div class="border border-gray-200 rounded-xl p-5">
                <h3 class="font-bold text-gray-900 mb-2">金融服务合作</h3>
                <p class="text-sm text-gray-600">银行、保理公司、保险机构等金融服务方的合作，为用户提供供应链金融产品。</p>
              </div>
              <div class="border border-gray-200 rounded-xl p-5">
                <h3 class="font-bold text-gray-900 mb-2">技术与数据合作</h3>
                <p class="text-sm text-gray-600">行情数据、质检机构、行业协会等的数据接入和标准共建合作。</p>
              </div>
            </div>

            <h2 id="c2" data-anchor class="anchor-heading">二、联系方式</h2>
            <div class="bg-gray-50 rounded-xl border border-gray-200 p-6 my-4 space-y-4">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-lg bg-brand-50 flex items-center justify-center">
                  <Mail class="w-5 h-5 text-brand-600" />
                </div>
                <div>
                  <div class="text-xs text-gray-400 font-medium">商务合作邮箱</div>
                  <a :href="`mailto:${SERVICE_EMAIL}`" class="text-sm font-bold text-brand-600 hover:underline">{{ SERVICE_EMAIL }}</a>
                </div>
              </div>
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-lg bg-brand-50 flex items-center justify-center">
                  <Clock class="w-5 h-5 text-brand-600" />
                </div>
                <div>
                  <div class="text-xs text-gray-400 font-medium">工作时间</div>
                  <div class="text-sm font-bold text-gray-900">周一至周五 9:00 - 18:00</div>
                </div>
              </div>
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-lg bg-brand-50 flex items-center justify-center">
                  <MapPin class="w-5 h-5 text-brand-600" />
                </div>
                <div>
                  <div class="text-xs text-gray-400 font-medium">公司地址</div>
                  <div class="text-sm font-bold text-gray-900">天津市东丽区</div>
                </div>
              </div>
            </div>

            <p class="text-sm text-gray-500 mt-6">来函请注明合作方向、公司名称及联系方式，我们将在3个工作日内回复。</p>
          </template>

          <!-- ==================== 意见反馈 ==================== -->
          <template v-else-if="pageType === 'feedback'">
            <p class="text-sm text-gray-500 mb-8">感谢您使用{{ PLATFORM_NAME }}平台！如果您在使用过程中遇到任何问题或有改进建议，欢迎随时联系我们。</p>

            <h2 id="f1" data-anchor class="anchor-heading">联系方式</h2>

            <div class="bg-gray-50 rounded-xl border border-gray-200 p-6 my-4 space-y-4">
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-lg bg-brand-50 flex items-center justify-center">
                  <Mail class="w-5 h-5 text-brand-600" />
                </div>
                <div>
                  <div class="text-xs text-gray-400 font-medium">客服邮箱</div>
                  <a :href="`mailto:${SERVICE_EMAIL}?subject=沃谷平台意见反馈`" class="text-sm font-bold text-brand-600 hover:underline">{{ SERVICE_EMAIL }}</a>
                </div>
              </div>
              <div class="flex items-center gap-3">
                <div class="w-10 h-10 rounded-lg bg-brand-50 flex items-center justify-center">
                  <Clock class="w-5 h-5 text-brand-600" />
                </div>
                <div>
                  <div class="text-xs text-gray-400 font-medium">服务时间</div>
                  <div class="text-sm font-bold text-gray-900">周一至周五 9:00 - 18:00</div>
                </div>
              </div>
            </div>

            <h2 id="f2" data-anchor class="anchor-heading">反馈说明</h2>
            <p>为便于我们快速处理您的问题，建议在邮件中包含以下信息：</p>
            <ul>
              <li><strong>反馈类型：</strong>功能建议 / 使用问题 / BUG报告 / 投诉</li>
              <li><strong>问题描述：</strong>尽量详细描述遇到的问题或您的建议</li>
              <li><strong>截图/录屏：</strong>如有异常界面，请附上截图</li>
              <li><strong>您的联系方式：</strong>便于我们回复您</li>
            </ul>
            <p class="text-sm text-gray-500 mt-4">我们将在1-3个工作日内通过邮件回复您的反馈。感谢您的支持！</p>
          </template>

        </div>
      </main>
    </div>

    <!-- 底部 -->
    <PublicFooter />
  </div>
</template>

<style scoped>
/* 法律文档排版 */
.legal-content h2 {
  @apply text-lg font-bold text-gray-900 mt-8 mb-4 pb-2 border-b border-gray-100;
}
.legal-content h2:first-child,
.legal-content p + h2:first-of-type {
  @apply mt-0;
}
.legal-content h3 {
  @apply text-sm font-bold text-gray-700 mt-5 mb-2;
}
.legal-content p {
  @apply text-sm text-gray-600 leading-relaxed mb-3;
}
.legal-content ul,
.legal-content ol {
  @apply text-sm text-gray-600 leading-relaxed mb-4 pl-5 space-y-1.5;
}
.legal-content ul {
  @apply list-disc;
}
.legal-content ol {
  @apply list-decimal;
}
.legal-content li {
  @apply leading-relaxed;
}
.legal-content table {
  @apply text-sm;
}
.legal-content strong {
  @apply text-gray-800;
}
</style>
