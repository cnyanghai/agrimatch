<script setup lang="ts">
/**
 * WgIcon - 基于 Lucide Icons 的统一图标组件
 * 替代 uni-icons，提供更精致优雅的 SVG 图标
 *
 * 用法：<WgIcon name="store" :size="20" color="#2D6A4F" />
 */
import { computed, h, type Component } from 'vue'
import {
  Search, X, Plus, Check, ChevronRight, ChevronLeft, ChevronDown, ChevronUp,
  Heart, MessageCircle, SquarePen, Store, ShoppingBag, FileText, LayoutGrid,
  TrendingUp, Building2, Building, Settings, Award, Truck, Bookmark,
  UserPlus, MapPin, Bell, Phone, Mail, Trash2, Star, Lock, User,
  Camera, Info, RefreshCw, Eye, EyeOff, Share2, Package,
  MessageSquare, Navigation, Coins, ArrowLeft,
} from 'lucide-vue-next'

const props = withDefaults(defineProps<{
  name: string
  size?: number | string
  color?: string
  strokeWidth?: number
}>(), {
  size: 16,
  color: 'currentColor',
  strokeWidth: 1.75,
})

/** uni-icons type → lucide 组件映射 */
const iconMap: Record<string, Component> = {
  // 导航 & 通用
  search: Search,
  clear: X,
  closeempty: X,
  right: ChevronRight,
  left: ChevronLeft,
  'chevron-right': ChevronRight,
  'chevron-left': ChevronLeft,
  'chevron-down': ChevronDown,
  'chevron-up': ChevronUp,
  'arrow-left': ArrowLeft,
  plus: Plus,
  plusempty: Plus,
  check: Check,
  checkmarkempty: Check,

  // 业务图标
  store: Store,
  shop: Store,
  'shopping-bag': ShoppingBag,
  cart: ShoppingBag,
  'file-text': FileText,
  list: FileText,
  'layout-grid': LayoutGrid,
  'trending-up': TrendingUp,
  bars: TrendingUp,
  building2: Building2,
  contact: Building2,
  building: Building,
  flag: Building,
  settings: Settings,
  gear: Settings,
  'square-pen': SquarePen,
  compose: SquarePen,

  // 社交 & 互动
  heart: Heart,
  'heart-filled': Heart,
  'message-circle': MessageCircle,
  chat: MessageCircle,
  chatboxes: MessageSquare,
  'message-square': MessageSquare,
  bell: Bell,
  'user-plus': UserPlus,
  personadd: UserPlus,
  bookmark: Bookmark,
  'star-filled': Bookmark,
  star: Star,
  share: Share2,
  redo: Share2,

  // 用户
  user: User,
  person: User,
  camera: Camera,
  lock: Lock,
  locked: Lock,

  // 物流 & 地图
  'map-pin': MapPin,
  location: MapPin,
  truck: Truck,
  car: Truck,
  navigate: Navigation,
  navigation: Navigation,

  // 通信
  phone: Phone,
  mail: Mail,
  email: Mail,

  // 操作
  trash: Trash2,
  'trash-2': Trash2,
  info: Info,
  'refresh-cw': RefreshCw,
  refreshempty: RefreshCw,
  award: Award,
  gift: Award,
  coins: Coins,
  package: Package,
  eye: Eye,
  'eye-off': EyeOff,
}

const iconComponent = computed(() => iconMap[props.name] || null)

const numericSize = computed(() => {
  const s = props.size
  if (typeof s === 'number') return s
  const n = parseFloat(s)
  return isNaN(n) ? 16 : n
})
</script>

<template>
  <component
    v-if="iconComponent"
    :is="iconComponent"
    :size="numericSize"
    :color="color"
    :stroke-width="strokeWidth"
  />
  <view
    v-else
    :style="{
      width: numericSize + 'px',
      height: numericSize + 'px',
      display: 'inline-flex',
      alignItems: 'center',
      justifyContent: 'center',
      color: color,
      fontSize: (numericSize * 0.6) + 'px',
    }"
  >?</view>
</template>
