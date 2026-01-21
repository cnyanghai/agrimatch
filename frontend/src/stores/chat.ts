import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ChatConversationResponse, ChatMessageResponse } from '@/api/chat'
import { listChatConversations, getConversationMessages, markConversationRead } from '@/api/chat'

export const useChatStore = defineStore('chat', () => {
    const conversations = ref<ChatConversationResponse[]>([])
    const messages = ref<Map<number, ChatMessageResponse[]>>(new Map())
    const activeConversationId = ref<number | null>(null)
    const loading = ref(false)
    const wsConnected = ref(false)
    const wsReconnectTimer = ref<number | null>(null)
    const wsCloseHinted = ref(false)

    const activeConversation = computed(() =>
        conversations.value.find((c: ChatConversationResponse) => c.id === activeConversationId.value)
    )

    const activeMessages = computed(() =>
        activeConversationId.value ? messages.value.get(activeConversationId.value) ?? [] : []
    )

    const groupedByPeer = computed(() => {
        const map = new Map<number, {
            peerUserId: number
            conversations: ChatConversationResponse[]
            totalUnread: number
            lastTime: string
            lastContent: string
        }>()

        conversations.value.forEach((c: ChatConversationResponse) => {
            const existing = map.get(c.peerUserId)
            if (existing) {
                existing.conversations.push(c)
                existing.totalUnread += c.unreadCount || 0
                if (c.lastTime && c.lastTime > existing.lastTime) {
                    existing.lastTime = c.lastTime
                    existing.lastContent = c.lastContent || ''
                }
            } else {
                map.set(c.peerUserId, {
                    peerUserId: c.peerUserId,
                    conversations: [c],
                    totalUnread: c.unreadCount || 0,
                    lastTime: c.lastTime || '',
                    lastContent: c.lastContent || ''
                })
            }
        })

        return Array.from(map.values()).sort((a, b) =>
            (b.lastTime || '').localeCompare(a.lastTime || '')
        )
    })

    async function loadConversations() {
        loading.value = true
        try {
            const res = await listChatConversations()
            conversations.value = res.data ?? []
        } finally {
            loading.value = false
        }
    }

    async function loadMessages(conversationId: number) {
        loading.value = true
        try {
            const res = await getConversationMessages(conversationId)
            messages.value.set(conversationId, res.data ?? [])
        } finally {
            loading.value = false
        }
    }

    async function markAsRead(conversationId: number) {
        const conversation = conversations.value.find((c: ChatConversationResponse) => c.id === conversationId)
        if (conversation && (conversation.unreadCount ?? 0) > 0) {
            await markConversationRead(conversationId)
            conversation.unreadCount = 0
        }
    }

    function setActiveConversation(conversationId: number | null) {
        activeConversationId.value = conversationId
        if (conversationId && !messages.value.has(conversationId)) {
            loadMessages(conversationId)
        }
    }

    function addMessage(conversationId: number, message: ChatMessageResponse) {
        const convMessages = messages.value.get(conversationId)
        if (convMessages) {
            convMessages.push(message)
        } else {
            messages.value.set(conversationId, [message])
        }
    }

    function updateConversation(conversation: ChatConversationResponse) {
        const index = conversations.value.findIndex((c: ChatConversationResponse) => c.id === conversation.id)
        if (index > -1) {
            conversations.value[index] = conversation
        } else {
            conversations.value.unshift(conversation)
        }
    }

    function setWsConnected(connected: boolean) {
        wsConnected.value = connected
        if (connected) {
            wsCloseHinted.value = false
            if (wsReconnectTimer.value) {
                clearTimeout(wsReconnectTimer.value)
                wsReconnectTimer.value = null
            }
        }
    }

    return {
        conversations,
        messages,
        activeConversationId,
        activeConversation,
        activeMessages,
        groupedByPeer,
        loading,
        wsConnected,
        wsCloseHinted,
        loadConversations,
        loadMessages,
        markAsRead,
        setActiveConversation,
        addMessage,
        updateConversation,
        setWsConnected
    }
})
