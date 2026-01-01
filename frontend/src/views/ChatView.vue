<script setup lang="ts">
import { ref, reactive, computed, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useAuthStore } from '../store/auth'
import { Search, Plus, Picture, Document, Position, Close, Present, ChatDotRound } from '@element-plus/icons-vue'
import { giftPoints } from '../api/points'

const auth = useAuthStore()
const loading = ref(false)
const messageInput = ref('')
const chatContainerRef = ref<HTMLElement | null>(null)

// 当前选中的会话
const activeSessionId = ref<number | null>(null)

// 会话列表
const sessions = ref<any[]>([])

// 消息列表
const messages = ref<any[]>([])

// 关联的供求信息（当前会话）
const linkedInfo = ref<any>(null)

// 搜索关键词
const searchKeyword = ref('')

// 赠送积分对话框
const giftDialogVisible = ref(false)
const giftForm = reactive({
  points: 10,
  remark: ''
})
const giftLoading = ref(false)

// 过滤后的会话列表
const filteredSessions = computed(() => {
  if (!searchKeyword.value) return sessions.value
  const kw = searchKeyword.value.toLowerCase()
  return sessions.value.filter(s => 
    s.name?.toLowerCase().includes(kw) ||
    s.lastMessage?.toLowerCase().includes(kw)
  )
})

// 当前会话
const currentSession = computed(() => {
  return sessions.value.find(s => s.id === activeSessionId.value)
})

// 加载会话列表
async function loadSessions() {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 300))
    sessions.value = [
      {
        id: 1,
        name: '山东优质农产品合作社',
        avatar: 'S',
        userId: 10001,
        lastMessage: '您好，请问这批小麦的质量如何？',
        lastTime: '10:30',
        unread: 2,
        linkedInfo: {
          type: 'supply',
          id: 101,
          title: '优质小麦现货供应',
          category: '小麦',
          price: 2800,
          quantity: '150吨'
        }
      },
      {
        id: 2,
        name: '北京粮油贸易公司',
        avatar: 'B',
        userId: 10002,
        lastMessage: '好的，合同我这边审核一下',
        lastTime: '昨天',
        unread: 0,
        linkedInfo: {
          type: 'demand',
          id: 201,
          title: '急采优质小麦500吨',
          category: '小麦',
          price: 2900,
          quantity: '500吨'
        }
      },
      {
        id: 3,
        name: '河南农业发展公司',
        avatar: 'H',
        userId: 10003,
        lastMessage: '物流方面我们可以安排',
        lastTime: '周一',
        unread: 0,
        linkedInfo: {
          type: 'supply',
          id: 102,
          title: '河南玉米产地直发',
          category: '玉米',
          price: 2500,
          quantity: '200吨'
        }
      }
    ]
    
    // 默认选中第一个会话
    if (sessions.value.length > 0 && !activeSessionId.value) {
      selectSession(sessions.value[0])
    }
  } finally {
    loading.value = false
  }
}

// 选择会话
async function selectSession(session: any) {
  activeSessionId.value = session.id
  linkedInfo.value = session.linkedInfo
  session.unread = 0
  
  // 加载消息
  await loadMessages(session.id)
}

// 加载消息
async function loadMessages(sessionId: number) {
  loading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 200))
    
    // 模拟消息数据
    if (sessionId === 1) {
      messages.value = [
        { id: 1, type: 'system', content: '会话已建立 - 关于《优质小麦现货供应》', time: '12月23日 09:00' },
        { id: 2, type: 'received', content: '您好，我看到贵公司发布的小麦供应信息，请问还有货吗？', time: '09:15', sender: '山东优质农产品合作社' },
        { id: 3, type: 'sent', content: '您好！有的，目前库存还有150吨左右', time: '09:18' },
        { id: 4, type: 'received', content: '质量怎么样？水分含量多少？', time: '09:20', sender: '山东优质农产品合作社' },
        { id: 5, type: 'sent', content: '品质一级，水分含量13%以下，可以提供检测报告', time: '09:25' },
        { id: 6, type: 'received', content: '您好，请问这批小麦的质量如何？', time: '10:30', sender: '山东优质农产品合作社' }
      ]
    } else if (sessionId === 2) {
      messages.value = [
        { id: 1, type: 'system', content: '会话已建立 - 关于《急采优质小麦500吨》', time: '12月22日 14:00' },
        { id: 2, type: 'sent', content: '您好，看到贵公司的采购需求，我们可以供应', time: '14:05' },
        { id: 3, type: 'received', content: '好的，请问价格方面可以谈吗？', time: '14:10', sender: '北京粮油贸易公司' },
        { id: 4, type: 'sent', content: '量大的话可以优惠，500吨以上2850/吨', time: '14:15' },
        { id: 5, type: 'received', content: '好的，合同我这边审核一下', time: '14:30', sender: '北京粮油贸易公司' }
      ]
    } else {
      messages.value = [
        { id: 1, type: 'system', content: '会话已建立 - 关于《河南玉米产地直发》', time: '12月20日 10:00' },
        { id: 2, type: 'received', content: '请问可以发往广东吗？', time: '10:15', sender: '河南农业发展公司' },
        { id: 3, type: 'sent', content: '可以的，我们有合作的物流', time: '10:20' },
        { id: 4, type: 'received', content: '物流方面我们可以安排', time: '10:30', sender: '河南农业发展公司' }
      ]
    }
    
    // 滚动到底部
    await nextTick()
    scrollToBottom()
  } finally {
    loading.value = false
  }
}

// 发送消息
async function sendMessage() {
  if (!messageInput.value.trim()) return
  if (!activeSessionId.value) {
    ElMessage.warning('请先选择会话')
    return
  }
  
  const content = messageInput.value.trim()
  messageInput.value = ''
  
  // 添加消息
  messages.value.push({
    id: Date.now(),
    type: 'sent',
    content,
    time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
  })
  
  // 滚动到底部
  await nextTick()
  scrollToBottom()
  
  // 模拟回复
  setTimeout(() => {
    messages.value.push({
      id: Date.now() + 1,
      type: 'received',
      content: '好的，收到您的消息，稍后回复您',
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' }),
      sender: currentSession.value?.name
    })
    scrollToBottom()
  }, 1500)
}

// 滚动到底部
function scrollToBottom() {
  if (chatContainerRef.value) {
    chatContainerRef.value.scrollTop = chatContainerRef.value.scrollHeight
  }
}

// 查看关联信息详情
function viewLinkedInfo() {
  if (linkedInfo.value) {
    ElMessage.info(`查看${linkedInfo.value.type === 'supply' ? '供应' : '采购'}信息：${linkedInfo.value.title}`)
  }
}

// 发起合同
function initiateContract() {
  if (linkedInfo.value) {
    ElMessage.success('正在生成合同...')
    // 实际应该跳转到合同创建页面
  }
}

// 打开赠送积分对话框
function openGiftDialog() {
  if (!currentSession.value) {
    ElMessage.warning('请先选择会话')
    return
  }
  giftForm.points = 10
  giftForm.remark = ''
  giftDialogVisible.value = true
}

// 提交赠送积分
async function submitGiftPoints() {
  if (!currentSession.value?.userId) {
    ElMessage.warning('无法获取对方用户信息')
    return
  }
  
  if (giftForm.points < 1) {
    ElMessage.warning('赠送积分数量至少为1')
    return
  }
  
  giftLoading.value = true
  try {
    await giftPoints(
      currentSession.value.userId, 
      giftForm.points, 
      giftForm.remark || `商务聊天赠送给${currentSession.value.name}`
    )
    ElMessage.success(`已成功赠送 ${giftForm.points} 积分给 ${currentSession.value.name}`)
    giftDialogVisible.value = false
    
    // 添加系统消息
    messages.value.push({
      id: Date.now(),
      type: 'system',
      content: `您已赠送 ${giftForm.points} 积分给对方`,
      time: new Date().toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
    })
    
    await nextTick()
    scrollToBottom()
  } catch (e: any) {
    ElMessage.error(e?.message || '赠送积分失败，请稍后重试')
  } finally {
    giftLoading.value = false
  }
}

onMounted(() => {
  loadSessions()
})
</script>

<template>
  <div class="chat-view h-full">
    <div class="flex h-full bg-white rounded-2xl shadow-sm overflow-hidden">
      <!-- 左侧会话列表 -->
      <div class="w-80 border-r border-gray-100 flex flex-col">
        <!-- 搜索栏 -->
        <div class="p-4 border-b border-gray-100">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索会话"
            :prefix-icon="Search"
            clearable
          />
        </div>

        <!-- 会话列表 -->
        <div class="flex-1 overflow-y-auto">
          <div
            v-for="session in filteredSessions"
            :key="session.id"
            class="px-4 py-3 cursor-pointer hover:bg-gray-50 transition-colors border-b border-gray-50"
            :class="{ 'bg-blue-50': activeSessionId === session.id }"
            @click="selectSession(session)"
          >
            <div class="flex items-start gap-3">
              <!-- 头像 -->
              <div class="w-12 h-12 rounded-full flex items-center justify-center text-white font-medium shrink-0"
                   :class="session.linkedInfo?.type === 'supply' ? 'bg-orange-500' : 'bg-blue-500'">
                {{ session.avatar }}
              </div>
              
              <!-- 会话信息 -->
              <div class="flex-1 min-w-0">
                <div class="flex items-center justify-between">
                  <span class="font-medium text-gray-800 truncate">{{ session.name }}</span>
                  <span class="text-xs text-gray-400 shrink-0 ml-2">{{ session.lastTime }}</span>
                </div>
                <div class="text-sm text-gray-500 truncate mt-1">{{ session.lastMessage }}</div>
                <!-- 关联信息标签 -->
                <div v-if="session.linkedInfo" class="mt-2">
                  <el-tag 
                    :type="session.linkedInfo.type === 'supply' ? 'warning' : 'primary'" 
                    size="small"
                    class="!text-xs"
                  >
                    {{ session.linkedInfo.type === 'supply' ? '供应' : '采购' }} · {{ session.linkedInfo.category }}
                  </el-tag>
                </div>
              </div>
              
              <!-- 未读标记 -->
              <div v-if="session.unread > 0" class="w-5 h-5 rounded-full bg-red-500 text-white text-xs flex items-center justify-center shrink-0">
                {{ session.unread }}
              </div>
            </div>
          </div>

          <div v-if="filteredSessions.length === 0" class="py-12 text-center text-gray-400">
            暂无会话
          </div>
        </div>
      </div>

      <!-- 右侧聊天区域 -->
      <div class="flex-1 flex flex-col">
        <!-- 顶部信息栏 -->
        <div v-if="currentSession" class="px-6 py-4 border-b border-gray-100 bg-white">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="w-10 h-10 rounded-full flex items-center justify-center text-white font-medium"
                   :class="linkedInfo?.type === 'supply' ? 'bg-orange-500' : 'bg-blue-500'">
                {{ currentSession.avatar }}
              </div>
              <div>
                <div class="font-medium text-gray-800">{{ currentSession.name }}</div>
                <div class="text-xs text-gray-400">在线</div>
              </div>
            </div>
            
            <!-- 操作按钮 -->
            <div class="flex items-center gap-2">
              <el-button size="small" @click="viewLinkedInfo">查看详情</el-button>
              <el-button type="warning" size="small" :icon="Present" @click="openGiftDialog">赠送积分</el-button>
              <el-button type="primary" size="small" @click="initiateContract">发起合同</el-button>
            </div>
          </div>

          <!-- 关联信息卡片 -->
          <div v-if="linkedInfo" class="mt-3 p-3 bg-gray-50 rounded-xl">
            <div class="flex items-center justify-between">
              <div class="flex items-center gap-3">
                <el-tag :type="linkedInfo.type === 'supply' ? 'warning' : 'primary'" size="small">
                  {{ linkedInfo.type === 'supply' ? '供应信息' : '采购需求' }}
                </el-tag>
                <span class="font-medium text-gray-800">{{ linkedInfo.title }}</span>
              </div>
              <div class="text-right">
                <div class="text-orange-600 font-medium">¥{{ linkedInfo.price }}/吨</div>
                <div class="text-xs text-gray-500">{{ linkedInfo.quantity }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 消息区域 -->
        <div 
          ref="chatContainerRef"
          class="flex-1 overflow-y-auto p-6 bg-gray-50"
          v-loading="loading"
        >
          <div v-if="activeSessionId" class="space-y-4">
            <div
              v-for="msg in messages"
              :key="msg.id"
              class="flex"
              :class="msg.type === 'sent' ? 'justify-end' : msg.type === 'system' ? 'justify-center' : 'justify-start'"
            >
              <!-- 系统消息 -->
              <div v-if="msg.type === 'system'" class="px-4 py-1.5 bg-gray-200 text-gray-500 text-xs rounded-full">
                {{ msg.content }}
              </div>
              
              <!-- 接收的消息 -->
              <div v-else-if="msg.type === 'received'" class="flex items-start gap-3 max-w-[70%]">
                <div class="w-9 h-9 rounded-full flex items-center justify-center text-white text-sm shrink-0"
                     :class="linkedInfo?.type === 'supply' ? 'bg-orange-500' : 'bg-blue-500'">
                  {{ currentSession?.avatar }}
                </div>
                <div>
                  <div class="bg-white rounded-2xl rounded-tl-sm px-4 py-3 shadow-sm">
                    <div class="text-gray-800">{{ msg.content }}</div>
                  </div>
                  <div class="text-xs text-gray-400 mt-1 ml-1">{{ msg.time }}</div>
                </div>
              </div>
              
              <!-- 发送的消息 -->
              <div v-else class="flex items-start gap-3 max-w-[70%] flex-row-reverse">
                <div class="w-9 h-9 rounded-full bg-emerald-600 flex items-center justify-center text-white text-sm shrink-0">
                  {{ (auth.me?.nickName || 'U')[0] }}
                </div>
                <div>
                  <div class="bg-emerald-600 text-white rounded-2xl rounded-tr-sm px-4 py-3 shadow-sm">
                    <div>{{ msg.content }}</div>
                  </div>
                  <div class="text-xs text-gray-400 mt-1 mr-1 text-right">{{ msg.time }}</div>
                </div>
              </div>
            </div>
          </div>

          <div v-else class="h-full flex items-center justify-center text-gray-400">
            <div class="text-center">
              <el-icon class="mx-auto mb-4 text-gray-300" :size="64"><ChatDotRound /></el-icon>
              <p>选择一个会话开始聊天</p>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div v-if="activeSessionId" class="p-4 border-t border-gray-100 bg-white">
          <div class="flex items-end gap-3">
            <!-- 工具栏 -->
            <div class="flex items-center gap-1 shrink-0">
              <el-tooltip content="发送图片">
                <el-button :icon="Picture" circle size="small" />
              </el-tooltip>
              <el-tooltip content="发送文件">
                <el-button :icon="Document" circle size="small" />
              </el-tooltip>
              <el-tooltip content="赠送积分">
                <el-button :icon="Present" circle size="small" @click="openGiftDialog" />
              </el-tooltip>
            </div>
            
            <!-- 输入框 -->
            <div class="flex-1">
              <el-input
                v-model="messageInput"
                type="textarea"
                :rows="2"
                placeholder="输入消息..."
                resize="none"
                @keydown.enter.prevent="sendMessage"
              />
            </div>
            
            <!-- 发送按钮 -->
            <el-button 
              type="primary" 
              :icon="Position" 
              class="shrink-0"
              :disabled="!messageInput.trim()"
              @click="sendMessage"
            >
              发送
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 赠送积分对话框 -->
    <el-dialog 
      v-model="giftDialogVisible" 
      title="赠送积分" 
      width="400px"
      :close-on-click-modal="false"
    >
      <div class="gift-dialog-content">
        <div class="recipient-info">
          <div class="avatar" :class="linkedInfo?.type === 'supply' ? 'bg-orange-500' : 'bg-blue-500'">
            {{ currentSession?.avatar }}
          </div>
          <div class="info">
            <div class="name">{{ currentSession?.name }}</div>
            <div class="hint">积分将直接转入对方账户</div>
          </div>
        </div>
        
        <el-form label-position="top" class="mt-4">
          <el-form-item label="赠送数量">
            <el-input-number 
              v-model="giftForm.points" 
              :min="1" 
              :max="10000" 
              :step="10"
              style="width: 100%"
            />
          </el-form-item>
          <el-form-item label="赠送留言（可选）">
            <el-input 
              v-model="giftForm.remark" 
              placeholder="感谢合作..." 
              maxlength="100" 
              show-word-limit
            />
          </el-form-item>
        </el-form>
        
        <div class="quick-amounts">
          <span class="label">快捷选择：</span>
          <el-button size="small" @click="giftForm.points = 10">10积分</el-button>
          <el-button size="small" @click="giftForm.points = 50">50积分</el-button>
          <el-button size="small" @click="giftForm.points = 100">100积分</el-button>
          <el-button size="small" @click="giftForm.points = 500">500积分</el-button>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="giftDialogVisible = false">取消</el-button>
        <el-button 
          type="primary" 
          :loading="giftLoading"
          @click="submitGiftPoints"
        >
          确认赠送 {{ giftForm.points }} 积分
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.chat-view {
  height: calc(100vh - 140px);
  min-height: 500px;
}

:deep(.el-textarea__inner) {
  border-radius: 12px;
}

/* 赠送积分对话框 */
.gift-dialog-content {
  padding: 8px 0;
}

.recipient-info {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
  border-radius: 12px;
}

.recipient-info .avatar {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  color: white;
  font-weight: bold;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.recipient-info .info .name {
  font-weight: 600;
  color: #1f2937;
  font-size: 16px;
}

.recipient-info .info .hint {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 2px;
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
