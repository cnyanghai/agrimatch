<script setup lang="ts">
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { 
  Search, ArrowRight, User, ShoppingCart, Document, ChatDotRound,
  Bowl, Coin, Apple, Grape, Box, Tools,
  UserFilled, Lock, Present, Star
} from '@element-plus/icons-vue'

const router = useRouter()

// 模拟分类数据
const categories = [
  { 
    id: 1, 
    name: '粮食作物', 
    icon: Bowl, 
    subCategories: ['玉米', '小麦', '大豆', '稻谷', '高粱', '大麦'] 
  },
  { 
    id: 2, 
    name: '经济作物', 
    icon: Coin, 
    subCategories: ['棉花', '花生', '油菜籽', '甘蔗', '甜菜', '烟叶'] 
  },
  { 
    id: 3, 
    name: '果蔬', 
    icon: Apple, 
    subCategories: ['苹果', '柑橘', '葡萄', '西瓜', '番茄', '黄瓜'] 
  },
  { 
    id: 4, 
    name: '特色作物', 
    icon: Grape, 
    subCategories: ['茶叶', '桑蚕', '中药材', '花卉', '苗木'] 
  },
  { 
    id: 5, 
    name: '生产作物', 
    icon: Box, 
    subCategories: ['马铃薯', '甘薯', '木薯', '魔芋'] 
  },
  { 
    id: 6, 
    name: '生产物资', 
    icon: Tools, 
    subCategories: ['农药', '化肥', '农膜', '农机', '种子'] 
  }
]

const activeCategory = ref<number | null>(null)

// 模拟优选货源数据
const products = [
  { id: 1, title: '优质东北玉米 二等 500吨', price: '2780', unit: '元/吨', location: '山东临沂', image: 'https://images.unsplash.com/photo-1551754655-cd27e38d2076?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60', tags: ['企业认证'] },
  { id: 2, title: '进口秘鲁鱼粉 65%蛋白 100吨', price: '面议', unit: '', location: '天津港', image: 'https://images.unsplash.com/photo-1519708227418-c8fd9a32b7a2?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60', tags: ['实地考察'] },
  { id: 3, title: '河南新季小麦 容重790 800吨', price: '3050', unit: '元/吨', location: '产地直发', image: 'https://images.unsplash.com/photo-1574323347407-f5e1ad6d020b?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60', tags: ['信用良好'] },
  { id: 4, title: '豆粕 43%蛋白 现货 200吨', price: '2900', unit: '元/吨', location: '江苏油厂', image: 'https://images.unsplash.com/photo-1542365858-a469a4632878?ixlib=rb-1.2.1&auto=format&fit=crop&w=500&q=60', tags: ['企业认证'] }
]

// 模拟观点数据
const quotes = [
  { id: 1, author: '李总(某大型饲料厂)', text: '东北产区新粮上市推迟，短期内华北玉米价格或将坚挺。' },
  { id: 2, author: '王博士(行业分析师)', text: '豆粕库存处于低位，关注下周进口大豆到港情况，谨慎看多。' },
  { id: 3, author: '张经理(贸易商)', text: '近期小麦饲用替代需求增加，注意陈粮出库节奏。' },
  { id: 4, author: '赵总(物流公司)', text: '近期运费有所上涨，建议提前安排车辆。' },
  { id: 5, author: '孙专家(农科院)', text: '今年大豆品质普遍较好，蛋白含量高。' }
]

function navigateTo(path: string) {
  router.push(path)
}
</script>

<template>
  <div class="min-h-screen flex flex-col bg-gray-50 font-sans">
    <!-- Header -->
    <header class="bg-white h-20 flex items-center shadow-sm sticky top-0 z-50">
      <div class="container mx-auto px-4 flex items-center justify-between">
        <!-- Logo -->
        <div class="flex items-center gap-3 cursor-pointer" @click="navigateTo('/')">
          <div class="w-10 h-10 rounded-lg bg-blue-600 flex items-center justify-center text-white font-bold text-xl">A</div>
          <div class="flex flex-col">
            <span class="text-xl font-bold text-gray-800 leading-none">农汇通</span>
            <span class="text-xs text-gray-500 font-medium">AgriMatch</span>
          </div>
        </div>

        <!-- Search -->
        <div class="flex-1 max-w-xl mx-8">
          <div class="relative">
            <input 
              type="text" 
              placeholder="搜索产品、企业、行情..." 
              class="w-full h-10 pl-4 pr-10 rounded-full border border-gray-300 bg-gray-50 focus:outline-none focus:border-blue-500 focus:ring-1 focus:ring-blue-500 transition-all text-sm"
            >
            <button class="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-blue-600">
              <Search class="w-4 h-4" />
            </button>
          </div>
        </div>

        <!-- Nav -->
        <nav class="flex items-center gap-8">
          <a href="#" class="text-blue-600 font-medium border-b-2 border-blue-600 pb-1">首页</a>
          <a href="#" class="text-gray-600 hover:text-blue-600 font-medium transition-colors" @click.prevent="navigateTo('/supply-browse')">供应大厅</a>
          <a href="#" class="text-gray-600 hover:text-blue-600 font-medium transition-colors" @click.prevent="navigateTo('/requirement-browse')">求购大厅</a>
          <a href="#" class="text-gray-600 hover:text-blue-600 font-medium transition-colors">行情资讯</a>
          <a href="#" class="text-gray-600 hover:text-blue-600 font-medium transition-colors">APP下载</a>
        </nav>

        <!-- Auth -->
        <div class="flex items-center gap-3 ml-8">
          <button class="px-5 py-1.5 rounded-full border border-blue-600 text-blue-600 hover:bg-blue-50 transition-colors font-medium text-sm" @click="navigateTo('/login')">登录</button>
          <button class="px-5 py-1.5 rounded-full bg-blue-600 text-white hover:bg-blue-700 transition-colors font-medium text-sm" @click="navigateTo('/login?tab=register')">注册</button>
        </div>
      </div>
    </header>

    <!-- Hero Section -->
    <div class="relative bg-blue-900 h-[420px] overflow-visible">
      <!-- Background Image Overlay -->
      <div class="absolute inset-0 z-0">
        <img src="https://images.unsplash.com/photo-1625246333195-09d9b630dc0a?ixlib=rb-1.2.1&auto=format&fit=crop&w=1920&q=80" alt="Farm" class="w-full h-full object-cover opacity-40 mix-blend-overlay">
        <div class="absolute inset-0 bg-gradient-to-r from-blue-900/90 to-transparent"></div>
      </div>

      <div class="container mx-auto px-4 h-full relative z-10 flex">
        <!-- Left Category Sidebar -->
        <div class="w-64 bg-white/95 backdrop-blur-sm rounded-xl shadow-xl h-[460px] mt-4 flex flex-col py-2 relative group">
          <div class="px-4 py-3 border-b border-gray-100 mb-1">
            <h3 class="font-bold text-gray-800 text-lg">产品分类</h3>
          </div>
          
          <div 
            v-for="cat in categories" 
            :key="cat.id"
            class="px-4 py-3 hover:bg-blue-50 cursor-pointer flex items-center justify-between group/item"
            @mouseenter="activeCategory = cat.id"
            @mouseleave="activeCategory = null"
          >
            <div class="flex items-center gap-3">
              <el-icon class="text-lg text-gray-400 group-hover/item:text-blue-600"><component :is="cat.icon" /></el-icon>
              <span class="text-gray-700 font-medium group-hover/item:text-blue-600">{{ cat.name }}</span>
            </div>
            <ArrowRight class="w-3 h-3 text-gray-400" />
          </div>

          <!-- Subcategory Popup -->
          <div 
            v-if="activeCategory"
            class="absolute left-full top-0 w-[400px] h-full bg-white rounded-r-xl shadow-xl p-6 z-50 ml-1"
            @mouseenter="activeCategory = activeCategory"
            @mouseleave="activeCategory = null"
          >
            <div class="grid grid-cols-3 gap-4">
              <div 
                v-for="(sub, idx) in categories.find(c => c.id === activeCategory)?.subCategories" 
                :key="idx"
                class="text-gray-600 hover:text-blue-600 cursor-pointer text-sm"
              >
                {{ sub }}
              </div>
            </div>
          </div>
        </div>

        <!-- Center Content -->
        <div class="flex-1 flex flex-col justify-center items-center text-center text-white pb-10 pl-8">
          <h1 class="text-5xl font-bold mb-4 tracking-wide shadow-black drop-shadow-lg">连接田间与工厂，让农贸交易更简单</h1>
          <div class="flex items-center gap-2 text-blue-100 text-lg mb-10 bg-black/20 px-4 py-1 rounded-full backdrop-blur-sm">
            <span>今日新增供需 <span class="text-white font-bold">3,421</span> 条</span>
            <span class="w-1 h-1 bg-blue-300 rounded-full mx-1"></span>
            <span>累计撮合交易 <span class="text-yellow-400 font-bold">¥2.5亿+</span></span>
          </div>
          
          <div class="flex gap-6">
            <button class="px-8 py-3 bg-blue-500 hover:bg-blue-600 text-white rounded-lg font-bold text-lg transition-all shadow-lg shadow-blue-500/30 flex items-center gap-2">
              <el-icon><Box /></el-icon>
              发布供应
            </button>
            <button class="px-8 py-3 bg-orange-500 hover:bg-orange-600 text-white rounded-lg font-bold text-lg transition-all shadow-lg shadow-orange-500/30 flex items-center gap-2">
              <el-icon><ShoppingCart /></el-icon>
              发布采购
            </button>
          </div>
        </div>
      </div>
    </div>

    <!-- Ticker Section -->
    <div class="bg-white border-b border-gray-100 shadow-sm relative z-0">
      <div class="container mx-auto px-4 h-16 flex items-center">
        <div class="flex items-center gap-3 pr-6 border-r border-gray-200">
          <span class="text-4xl text-gray-200 font-serif">“</span>
          <span class="font-bold text-gray-800 text-lg whitespace-nowrap">业内观点精选</span>
        </div>
        <div class="flex-1 overflow-hidden relative h-16">
          <div class="absolute inset-0 flex items-center animate-marquee whitespace-nowrap">
            <div v-for="quote in quotes" :key="quote.id" class="flex items-center gap-3 mx-8">
              <div class="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center text-xs font-bold text-gray-600">
                {{ quote.author.substring(0, 1) }}
              </div>
              <div>
                <span class="font-bold text-gray-900 text-sm">{{ quote.author }}：</span>
                <span class="text-gray-600 text-sm">"{{ quote.text }}"</span>
              </div>
            </div>
            <!-- Duplicate for infinite scroll effect -->
             <div v-for="quote in quotes" :key="quote.id + '_dup'" class="flex items-center gap-3 mx-8">
              <div class="w-8 h-8 rounded-full bg-gray-200 flex items-center justify-center text-xs font-bold text-gray-600">
                {{ quote.author.substring(0, 1) }}
              </div>
              <div>
                <span class="font-bold text-gray-900 text-sm">{{ quote.author }}：</span>
                <span class="text-gray-600 text-sm">"{{ quote.text }}"</span>
              </div>
            </div>
          </div>
        </div>
        <div class="pl-6 text-4xl text-gray-200 font-serif transform rotate-180">“</div>
      </div>
    </div>

    <!-- Main Content -->
    <div class="container mx-auto px-4 py-12">
      <div class="flex gap-8">
        <!-- Left Column: Insights -->
        <div class="w-2/3 space-y-8">
          <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-bold text-gray-800">行业焦点与深度研讨</h2>
            <a href="#" class="text-gray-500 hover:text-blue-600 text-sm flex items-center gap-1">更多 <ArrowRight class="w-3 h-3"/></a>
          </div>

          <!-- Article Card 1 -->
          <div class="bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow p-6 border border-gray-100 flex gap-6">
            <div class="w-48 h-32 rounded-lg overflow-hidden flex-shrink-0 bg-gray-100">
              <img src="https://images.unsplash.com/photo-1554995207-c18c203602cb?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80" class="w-full h-full object-cover">
            </div>
            <div class="flex-1 flex flex-col justify-between">
              <div>
                <h3 class="text-xl font-bold text-gray-800 mb-2 line-clamp-2 hover:text-blue-600 cursor-pointer">2025年饲料原料采购策略前瞻：如何规避价差波动风险？</h3>
                <p class="text-gray-500 text-sm line-clamp-2">2025年饲料原料市场面临多重不确定性因素，如何制定有效的采购策略来规避价格波动带来的风险？本文将深度解析...</p>
              </div>
              <div class="flex items-center justify-between mt-4">
                <div class="flex items-center gap-2">
                  <div class="w-6 h-6 rounded-full bg-blue-100 text-blue-600 flex items-center justify-center text-xs font-bold">李</div>
                  <span class="text-xs text-gray-500">李淑芬</span>
                </div>
                <div class="flex items-center gap-4 text-xs text-gray-400">
                  <span class="flex items-center gap-1"><ChatDotRound class="w-3 h-3"/> 236人参与讨论</span>
                </div>
              </div>
            </div>
          </div>

          <!-- Article Card 2 -->
          <div class="bg-white rounded-xl shadow-sm hover:shadow-md transition-shadow p-6 border border-gray-100 flex gap-6">
             <div class="w-48 h-32 rounded-lg overflow-hidden flex-shrink-0 bg-gray-100">
              <img src="https://images.unsplash.com/photo-1560668325-187514d244a0?ixlib=rb-1.2.1&auto=format&fit=crop&w=300&q=80" class="w-full h-full object-cover">
            </div>
            <div class="flex-1 flex flex-col justify-between">
              <div>
                <h3 class="text-xl font-bold text-gray-800 mb-2 line-clamp-2 hover:text-blue-600 cursor-pointer">非标品交易的信任重构：电子合同与可视化物流的应用实践</h3>
                <p class="text-gray-500 text-sm line-clamp-2">非标农产品交易中长期存在信任难题，电子合同与全程可视化物流追踪技术的应用，正在重构行业信任体系...</p>
              </div>
              <div class="flex items-center justify-between mt-4">
                <div class="flex items-center gap-2">
                  <div class="w-6 h-6 rounded-full bg-orange-100 text-orange-600 flex items-center justify-center text-xs font-bold">华</div>
                  <span class="text-xs text-gray-500">华伟君</span>
                </div>
                 <div class="flex items-center gap-4 text-xs text-gray-400">
                  <span class="flex items-center gap-1">👍 158人点赞</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Right Column: Resources -->
        <div class="w-1/3">
           <div class="flex items-center justify-between mb-6">
            <h2 class="text-2xl font-bold text-gray-800">最新优选货源</h2>
            <a href="#" class="text-gray-500 hover:text-blue-600 text-sm flex items-center gap-1">更多 <ArrowRight class="w-3 h-3"/></a>
          </div>

          <div class="bg-white rounded-xl shadow-sm border border-gray-100 p-2">
            <div 
              v-for="product in products" 
              :key="product.id"
              class="flex gap-4 p-3 hover:bg-gray-50 rounded-lg transition-colors cursor-pointer border-b last:border-0 border-gray-50"
            >
              <div class="w-20 h-20 rounded-lg overflow-hidden flex-shrink-0 bg-gray-200">
                <img :src="product.image" class="w-full h-full object-cover">
              </div>
              <div class="flex-1 min-w-0 flex flex-col justify-between py-0.5">
                <h4 class="font-bold text-gray-800 text-sm truncate">{{ product.title }}</h4>
                <div class="flex items-center gap-2">
                   <span class="text-xs text-gray-500">{{ product.location }}</span>
                   <span 
                    v-for="tag in product.tags" 
                    :key="tag"
                    class="text-[10px] px-1.5 py-0.5 bg-blue-50 text-blue-600 rounded"
                   >
                    {{ tag }}
                   </span>
                </div>
                <div class="flex items-baseline gap-1">
                  <span class="text-orange-500 font-bold text-lg">
                    <span v-if="product.price !== '面议'">¥</span>{{ product.price }}
                  </span>
                  <span class="text-xs text-gray-400">{{ product.unit }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Features / Footer Top -->
    <div class="bg-white border-t border-gray-200 py-12 mt-auto">
      <div class="container mx-auto px-4">
        <h2 class="text-2xl font-bold text-gray-800 text-center mb-12">为什么选择我们</h2>
        <div class="grid grid-cols-3 gap-8">
          <div class="flex items-start gap-4 p-6 bg-gray-50 rounded-xl hover:shadow-md transition-shadow">
            <div class="w-12 h-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 text-2xl">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div>
              <h3 class="font-bold text-lg text-gray-800 mb-2">专业社区</h3>
              <p class="text-gray-500 text-sm">汇聚行业精英，分享一手资讯与经验，拓展人脉圈。</p>
            </div>
          </div>
           <div class="flex items-start gap-4 p-6 bg-gray-50 rounded-xl hover:shadow-md transition-shadow">
            <div class="w-12 h-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 text-2xl">
              <el-icon><Lock /></el-icon>
            </div>
            <div>
              <h3 class="font-bold text-lg text-gray-800 mb-2">撮合交易</h3>
              <p class="text-gray-500 text-sm">安全高效的交易保障体系，资金托管，交易无忧。</p>
            </div>
          </div>
           <div class="flex items-start gap-4 p-6 bg-gray-50 rounded-xl hover:shadow-md transition-shadow">
            <div class="w-12 h-12 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 text-2xl">
              <el-icon><Present /></el-icon>
            </div>
            <div>
              <h3 class="font-bold text-lg text-gray-800 mb-2">积分奖励</h3>
              <p class="text-gray-500 text-sm">活跃交易获取积分，兑换超值礼品与增值服务。</p>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Bottom Footer -->
    <footer class="bg-gray-100 py-8 border-t border-gray-200 text-sm">
      <div class="container mx-auto px-4 flex justify-between items-center">
        <div class="text-gray-500">
          <div class="flex gap-6 mb-4">
             <a href="#" class="hover:text-blue-600">首页</a>
             <a href="#" class="hover:text-blue-600">供应大厅</a>
             <a href="#" class="hover:text-blue-600">采购大厅</a>
             <a href="#" class="hover:text-blue-600">行情资讯</a>
             <a href="#" class="hover:text-blue-600">APP下载</a>
          </div>
          <p>联系热线: 070-327 2680 &nbsp;|&nbsp; 电话: 微信同号中心</p>
          <p class="mt-2">© 2024 农汇通 AgriMatch. All rights reserved.</p>
        </div>
        <div class="flex items-center gap-4">
          <div class="text-right">
             <p class="font-bold text-gray-800 mb-1">下载APP</p>
             <p class="text-xs text-gray-500">随时随地 掌握商机</p>
          </div>
          <div class="w-20 h-20 bg-white p-1 rounded shadow-sm">
             <img src="https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=AgriMatch" alt="QR Code" class="w-full h-full">
          </div>
        </div>
      </div>
    </footer>
  </div>
</template>

<style scoped>
.animate-marquee {
  animation: marquee 30s linear infinite;
}

@keyframes marquee {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}
</style>
