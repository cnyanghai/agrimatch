<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Present, Search } from '@element-plus/icons-vue'
import { createPost, createPostComment, deletePost, listPostComments, listPosts, togglePostLike, type PostCommentResponse, type PostResponse } from '../api/post'
import { useAuthStore } from '../store/auth'
import { getMyCompany, type CompanyResponse } from '../api/company'
import { giftPoints } from '../api/points'
import { requireAuth } from '../utils/requireAuth'
import PageHeader from '../components/PageHeader.vue'

const auth = useAuthStore()
const company = ref<CompanyResponse | null>(null)

const creating = ref(false)
const loading = ref(false)
const list = ref<PostResponse[]>([])
const liking = ref<Record<number, boolean>>({})
const commentDialogOpen = ref(false)
const currentPostId = ref<number | null>(null)
const comments = ref<PostCommentResponse[]>([])
const commentText = ref('')

// 打赏对话框
const tipDialogOpen = ref(false)
const currentTipPost = ref<PostResponse | null>(null)
const tipForm = reactive({
  points: 10,
  remark: ''
})
const tipping = ref(false)

const form = reactive({
  title: '',
  content: ''
})

const q = reactive({
  keyword: '',
  orderBy: 'create_time',
  order: 'desc'
})

const canCreate = computed(() => form.title.trim().length > 0 && !!auth.token)
const isLoggedIn = computed(() => !!auth.token)

async function refresh() {
  loading.value = true
  try {
    const r = await listPosts({ keyword: q.keyword || undefined, orderBy: q.orderBy, order: q.order })
    if (r.code !== 0) throw new Error(r.message)
    list.value = r.data ?? []
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载失败')
  } finally {
    loading.value = false
  }
}

async function onToggleLike(row: PostResponse) {
  if (liking.value[row.id]) return
  
  // 检查登录状态
  if (!requireAuth('/posts')) {
    ElMessage.warning('请先登录后再点赞')
    return
  }
  
  liking.value = { ...liking.value, [row.id]: true }
  try {
    const r = await togglePostLike(row.id)
    if (r.code !== 0) throw new Error(r.message)
    row.likedByMe = r.data?.liked ?? row.likedByMe
    row.likeCount = r.data?.likeCount ?? row.likeCount
  } catch (e: any) {
    ElMessage.error(e?.message ?? '点赞失败')
  } finally {
    liking.value = { ...liking.value, [row.id]: false }
  }
}

async function openComments(row: PostResponse) {
  currentPostId.value = row.id
  commentDialogOpen.value = true
  await refreshComments()
}

async function refreshComments() {
  if (!currentPostId.value) return
  try {
    const r = await listPostComments(currentPostId.value)
    if (r.code !== 0) throw new Error(r.message)
    comments.value = r.data ?? []
  } catch (e: any) {
    ElMessage.error(e?.message ?? '加载评论失败')
  }
}

async function onAddComment() {
  const pid = currentPostId.value
  if (!pid) return
  const content = commentText.value.trim()
  if (!content) return
  
  // 检查登录状态
  if (!requireAuth('/posts')) {
    ElMessage.warning('请先登录后再评论')
    return
  }
  
  try {
    const r = await createPostComment(pid, content)
    if (r.code !== 0) throw new Error(r.message)
    commentText.value = ''
    await refreshComments()
    await refresh() // 更新 commentCount
    ElMessage.success('已评论')
  } catch (e: any) {
    ElMessage.error(e?.message ?? '评论失败')
  }
}

async function onCreate() {
  if (!canCreate.value) return
  
  // 检查登录状态
  if (!requireAuth('/posts')) {
    ElMessage.warning('请先登录后再发布话题')
    return
  }
  
  creating.value = true
  try {
    const r = await createPost({ title: form.title.trim(), content: form.content?.trim() || undefined })
    if (r.code !== 0) throw new Error(r.message)
    ElMessage.success('已发布')
    form.title = ''
    form.content = ''
    await refresh()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '发布失败')
  } finally {
    creating.value = false
  }
}

async function onDelete(id: number) {
  loading.value = true
  try {
    const r = await deletePost(id)
    if (r.code !== 0) throw new Error(r.message)
    ElMessage.success('已删除')
    await refresh()
  } catch (e: any) {
    ElMessage.error(e?.message ?? '删除失败')
  } finally {
    loading.value = false
  }
}

// 打开打赏对话框
function openTipDialog(row: PostResponse) {
  if (!row.userId) {
    ElMessage.warning('无法获取作者信息')
    return
  }
  // 不能打赏自己
  if (row.userId === auth.me?.id) {
    ElMessage.info('不能打赏自己的帖子哦')
    return
  }
  currentTipPost.value = row
  tipForm.points = 10
  tipForm.remark = ''
  tipDialogOpen.value = true
}

// 提交打赏
async function submitTip() {
  if (!currentTipPost.value?.userId) {
    ElMessage.warning('无法获取作者信息')
    return
  }
  
  if (tipForm.points < 1) {
    ElMessage.warning('打赏积分数量至少为1')
    return
  }
  
  tipping.value = true
  try {
    const authorName = currentTipPost.value.nickName || currentTipPost.value.userName || '作者'
    await giftPoints(
      currentTipPost.value.userId, 
      tipForm.points, 
      tipForm.remark || `打赏帖子《${currentTipPost.value.title}》`
    )
    ElMessage.success(`已成功打赏 ${tipForm.points} 积分给 ${authorName}`)
    tipDialogOpen.value = false
  } catch (e: any) {
    ElMessage.error(e?.message || '打赏失败，请稍后重试')
  } finally {
    tipping.value = false
  }
}

onMounted(() => {
  refresh()
  ;(async () => {
    try {
      const r = await getMyCompany()
      if (r.code === 0) company.value = r.data ?? null
    } catch {
      // ignore
    }
  })()
})
</script>

<template>
  <div class="space-y-4">
    <PageHeader title="社区论坛" subtitle="发布观点、点赞评论、与供需交流联动" />

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <!-- 发布帖子 -->
      <div class="bg-white rounded-xl shadow-card border border-gray-100 p-5">
        <div class="font-bold text-gray-800 mb-4">发布帖子</div>
        <div v-if="!isLoggedIn" class="text-center py-8">
          <el-empty description="请先登录后再发布话题">
            <el-button type="primary" @click="requireAuth('/posts')">去登录</el-button>
          </el-empty>
        </div>
        <el-form v-else label-width="92px">
          <el-form-item label="用户/公司">
            <el-text>{{ auth.me?.nickName || auth.me?.userName || '-' }}</el-text>
            <el-divider direction="vertical" />
            <el-text type="info">{{ company?.companyName ?? '未绑定公司（可先去"我的档案"完善）' }}</el-text>
          </el-form-item>
          <el-form-item label="标题">
            <el-input v-model="form.title" maxlength="120" show-word-limit placeholder="请输入话题标题" />
          </el-form-item>
          <el-form-item label="内容">
            <el-input v-model="form.content" type="textarea" :rows="5" maxlength="20000" show-word-limit placeholder="分享你的观点..." />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :disabled="!canCreate" :loading="creating" @click="onCreate">发布</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 帖子列表 -->
      <div class="bg-white rounded-xl shadow-card border border-gray-100 p-5">
        <div class="flex items-center justify-between mb-3">
          <div class="font-bold text-gray-800">帖子列表</div>
          <div class="flex gap-2 items-center">
            <el-input
              v-model="q.keyword"
              placeholder="搜索标题/内容"
              :prefix-icon="Search"
              style="width: 220px;"
              clearable
              @keyup.enter="refresh"
            />
            <el-button :loading="loading" @click="refresh">查询</el-button>
          </div>
        </div>

        <el-table :data="list" v-loading="loading" style="width:100%;">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="title" label="标题" min-width="180">
            <template #default="{ row }">
              <span class="line-clamp-1">{{ row.title }}</span>
            </template>
          </el-table-column>
          <el-table-column label="作者" width="140">
            <template #default="{ row }">
              <div class="flex items-center gap-2">
                <div class="w-7 h-7 rounded-full bg-blue-500 text-white flex items-center justify-center text-xs">
                  {{ (row.nickName || row.userName || '?')[0] }}
                </div>
                <span class="text-sm truncate">{{ row.nickName || row.userName || '-' }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="互动" width="240">
            <template #default="{ row }">
              <div class="flex gap-1.5 flex-wrap">
                <el-button
                  :type="row.likedByMe ? 'success' : 'default'"
                  size="small"
                  :loading="!!liking[row.id]"
                  @click="onToggleLike(row)"
                >
                  {{ row.likedByMe ? '👍' : '👍' }} {{ row.likeCount ?? 0 }}
                </el-button>
                <el-button size="small" @click="openComments(row)">💬 {{ row.commentCount ?? 0 }}</el-button>
                <el-button 
                  type="warning" 
                  size="small"
                  :icon="Present"
                  @click="openTipDialog(row)"
                >
                  打赏
                </el-button>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="时间" width="160">
            <template #default="{ row }">
              <span class="text-xs text-gray-500">{{ row.createTime }}</span>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80" fixed="right">
            <template #default="{ row }">
              <el-button type="danger" link size="small" @click="onDelete(row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>
  </div>

  <!-- 评论对话框 -->
  <el-dialog v-model="commentDialogOpen" title="评论" width="720px" @open="refreshComments">
    <el-space direction="vertical" fill size="small" style="width:100%;">
      <el-input v-model="commentText" type="textarea" :rows="3" maxlength="1000" show-word-limit placeholder="输入评论..." />
      <div style="display:flex;justify-content:flex-end;">
        <el-button type="primary" @click="onAddComment" :disabled="!commentText.trim()">发表评论</el-button>
      </div>

      <el-divider />
      <el-empty v-if="comments.length === 0" description="暂无评论" />
      <el-timeline v-else>
        <el-timeline-item v-for="c in comments" :key="c.id" :timestamp="c.createTime">
          <div style="display:flex;gap:8px;align-items:center;flex-wrap:wrap;">
            <el-tag size="small" type="info">{{ c.nickName || c.userName || '匿名用户' }}</el-tag>
            <el-tag v-if="c.companyName" size="small" type="success">{{ c.companyName }}</el-tag>
          </div>
          <div style="margin-top:6px;">{{ c.content }}</div>
        </el-timeline-item>
      </el-timeline>
    </el-space>
  </el-dialog>

  <!-- 打赏对话框 -->
  <el-dialog 
    v-model="tipDialogOpen" 
    title="打赏作者" 
    width="420px"
    :close-on-click-modal="false"
  >
    <div class="tip-dialog-content">
      <!-- 帖子信息 -->
      <div class="post-info">
        <div class="post-title">{{ currentTipPost?.title }}</div>
        <div class="post-author">
          <div class="avatar bg-blue-500">
            {{ (currentTipPost?.nickName || currentTipPost?.userName || '?')[0] }}
          </div>
          <span>{{ currentTipPost?.nickName || currentTipPost?.userName || '作者' }}</span>
          <el-tag v-if="currentTipPost?.companyName" size="small" type="info">{{ currentTipPost?.companyName }}</el-tag>
        </div>
      </div>
      
      <el-form label-position="top" class="mt-4">
        <el-form-item label="打赏积分">
          <el-input-number 
            v-model="tipForm.points" 
            :min="1" 
            :max="10000" 
            :step="10"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="打赏留言（可选）">
          <el-input 
            v-model="tipForm.remark" 
            placeholder="写得好！支持一下..." 
            maxlength="100" 
            show-word-limit
          />
        </el-form-item>
      </el-form>
      
      <div class="quick-amounts">
        <span class="label">快捷选择：</span>
        <el-button size="small" @click="tipForm.points = 5">5积分</el-button>
        <el-button size="small" @click="tipForm.points = 10">10积分</el-button>
        <el-button size="small" @click="tipForm.points = 50">50积分</el-button>
        <el-button size="small" @click="tipForm.points = 100">100积分</el-button>
      </div>
    </div>
    
    <template #footer>
      <el-button @click="tipDialogOpen = false">取消</el-button>
      <el-button 
        type="warning" 
        :loading="tipping"
        @click="submitTip"
      >
        确认打赏 {{ tipForm.points }} 积分
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.line-clamp-1 {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 打赏对话框 */
.tip-dialog-content {
  padding: 8px 0;
}

.post-info {
  padding: 16px;
  background: linear-gradient(135deg, #fefce8 0%, #fef9c3 100%);
  border-radius: 12px;
}

.post-title {
  font-weight: 600;
  color: #1f2937;
  font-size: 16px;
  margin-bottom: 12px;
  line-height: 1.5;
}

.post-author {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #6b7280;
}

.post-author .avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  color: white;
  font-weight: 500;
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.quick-amounts {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px dashed #e5e7eb;
}

.quick-amounts .label {
  font-size: 13px;
  color: #6b7280;
}
</style>
