import { ref } from 'vue'
import { useAuthStore } from '../store/auth'
import { followUser, unfollowUser, checkFollowStatus } from '../api/follow'

/**
 * 关注/取关复用逻辑
 * @param getTargetUserId 获取目标用户 ID 的函数
 */
export function useFollow(getTargetUserId: () => number | undefined) {
  const authStore = useAuthStore()
  const isFollowing = ref(false)
  const followLoading = ref(false)

  /** 检查当前关注状态 */
  async function loadFollowStatus() {
    const targetUserId = getTargetUserId()
    if (!targetUserId || !authStore.isLoggedIn) return
    if (targetUserId === authStore.user?.userId) return
    try {
      isFollowing.value = await checkFollowStatus(targetUserId) || false
    } catch {
      // silent
    }
  }

  /** 切换关注/取关 */
  async function handleToggleFollow() {
    const targetUserId = getTargetUserId()
    if (!targetUserId) return
    if (!authStore.isLoggedIn) {
      uni.navigateTo({ url: '/pages/auth/login' })
      return
    }
    if (followLoading.value) return
    followLoading.value = true
    try {
      if (isFollowing.value) {
        await unfollowUser(targetUserId)
        isFollowing.value = false
        uni.showToast({ title: '已取消关注', icon: 'none' })
      } else {
        await followUser(targetUserId)
        isFollowing.value = true
        uni.showToast({ title: '关注成功', icon: 'none' })
      }
    } catch {
      // handled by request.ts
    } finally {
      followLoading.value = false
    }
  }

  /** 是否显示关注按钮（非自己） */
  function canFollow(): boolean {
    const targetUserId = getTargetUserId()
    return !!targetUserId && targetUserId !== authStore.user?.userId
  }

  return {
    isFollowing,
    followLoading,
    loadFollowStatus,
    handleToggleFollow,
    canFollow,
  }
}
