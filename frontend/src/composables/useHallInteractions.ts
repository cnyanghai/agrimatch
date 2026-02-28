/**
 * Shared composable for Supply/Purchase Hall interactions.
 * Handles follow status, chat drawer, and card focus.
 */
import { computed, nextTick, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { requireAuth } from '../utils/requireAuth'
import { useAuthStore } from '../store/auth'
import { openChatConversation } from '../api/chat'
import { followUser, unfollowUser, checkFollowStatus } from '../api/follow'
import { showToast } from '@/composables/useToast'

export interface ListingItem {
  id: number
  userId?: number
  userName?: string
  nickName?: string
  companyName?: string
}

export function useHallInteractions(hallPath: string) {
  const route = useRoute()
  const router = useRouter()
  const authStore = useAuthStore()

  // -- Follow state --
  const followingMap = ref<Map<number, boolean>>(new Map())

  async function loadFollowStatus(userIds: number[]) {
    if (!authStore.token) return
    for (const userId of userIds) {
      if (followingMap.value.has(userId)) continue
      try {
        const r = await checkFollowStatus(userId)
        if (r.code === 0) {
          followingMap.value.set(userId, r.data || false)
        }
      } catch { /* ignore */ }
    }
  }

  async function toggleFollow(item: ListingItem) {
    if (!requireAuth(hallPath)) return
    if (!item.userId) {
      showToast.warning('无法关注该用户')
      return
    }
    const isFollowing = followingMap.value.get(item.userId) || false
    try {
      if (isFollowing) {
        await unfollowUser(item.userId)
        followingMap.value.set(item.userId, false)
        showToast.success(`已取消关注 ${item.nickName || item.companyName || '该用户'}`)
      } else {
        await followUser(item.userId)
        followingMap.value.set(item.userId, true)
        showToast.success(`已关注 ${item.nickName || item.companyName || '该用户'}`)
      }
    } catch (e: any) {
      showToast.error(e?.message || '操作失败')
    }
  }

  function isFollowingUser(userId?: number): boolean {
    if (!userId) return false
    return followingMap.value.get(userId) || false
  }

  // -- Card focus (from map/URL) --
  const focusedId = ref<number | null>(null)
  const cardEls = new Map<number, HTMLElement>()
  let focusTimer: number | null = null

  const focusIdFromRoute = computed(() => {
    const raw = route.query.focusId
    const s = Array.isArray(raw) ? raw[0] : raw
    const n = s ? Number(s) : NaN
    return Number.isFinite(n) ? n : null
  })

  function setCardEl(id: number, el: Element | null) {
    if (!id) return
    if (el) cardEls.set(id, el as HTMLElement)
    else cardEls.delete(id)
  }

  async function applyFocusIfNeeded() {
    const id = focusIdFromRoute.value
    if (!id) return
    await nextTick()
    const el = cardEls.get(id)
    if (!el) return

    focusedId.value = id
    el.scrollIntoView({ behavior: 'smooth', block: 'center' })
    router.replace({ path: route.path, query: { ...route.query, focusId: undefined } })

    if (focusTimer) window.clearTimeout(focusTimer)
    focusTimer = window.setTimeout(() => {
      focusedId.value = null
      focusTimer = null
    }, 2500)
  }

  // -- Chat drawer --
  const drawerOpen = ref(false)
  const drawerConversationId = ref<number | null>(null)
  const drawerPeerName = ref('')
  const drawerSubjectSnapshotJson = ref<string | null>(null)
  const drawerSubjectId = ref<number | null>(null)

  async function openConsultDrawer(
    item: ListingItem,
    subjectType: 'SUPPLY' | 'REQUIREMENT',
    snapshotJson: string
  ) {
    if (!requireAuth(hallPath)) return
    if (!item.userId || !item.id) {
      showToast.warning('该条信息暂不支持咨询')
      return
    }
    try {
      const res = await openChatConversation({
        peerUserId: item.userId,
        subjectType,
        subjectId: item.id,
        subjectSnapshotJson: snapshotJson
      })
      if (res.code !== 0 || !res.data) throw new Error(res.message)

      drawerConversationId.value = res.data
      drawerPeerName.value = item.nickName || item.userName || item.companyName || '对方'
      drawerSubjectId.value = item.id
      drawerSubjectSnapshotJson.value = snapshotJson
      drawerOpen.value = true
    } catch (e: any) {
      showToast.error(e?.message || '发起咨询失败')
    }
  }

  function onDrawerClosed() {
    drawerConversationId.value = null
    drawerSubjectSnapshotJson.value = null
    drawerSubjectId.value = null
    drawerPeerName.value = ''
  }

  return {
    // Follow
    followingMap,
    loadFollowStatus,
    toggleFollow,
    isFollowingUser,

    // Card focus
    focusedId,
    focusIdFromRoute,
    setCardEl,
    applyFocusIfNeeded,

    // Chat drawer
    drawerOpen,
    drawerConversationId,
    drawerPeerName,
    drawerSubjectSnapshotJson,
    drawerSubjectId,
    openConsultDrawer,
    onDrawerClosed,
  }
}
