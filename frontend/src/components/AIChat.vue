<template>
  <div class="ai-assistant">
    <div class="ai-assistant-icon" @click="toggleChat">
      <div class="cartoon-avatar">
        <svg viewBox="0 0 100 100" class="cartoon-svg">
          <circle cx="50" cy="45" r="35" fill="#E8F4FD"/>
          <circle cx="50" cy="38" r="22" fill="white" stroke="#4A90D9" stroke-width="2"/>
          <circle cx="42" cy="34" r="4" fill="#4A90D9"/>
          <circle cx="58" cy="34" r="4" fill="#4A90D9"/>
          <circle cx="43" cy="33" r="1.5" fill="white" opacity="0.8"/>
          <circle cx="59" cy="33" r="1.5" fill="white" opacity="0.8"/>
          <path d="M42 42 Q50 50 58 42" stroke="#4A90D9" stroke-width="2.5" fill="none" stroke-linecap="round"/>
          <rect x="44" y="50" width="12" height="8" rx="4" fill="#7EC8E3"/>
          <rect x="28" y="52" width="8" height="12" rx="3" fill="#6BB5E0"/>
          <rect x="64" y="52" width="8" height="12" rx="3" fill="#6BB5E0"/>
          <rect x="36" y="60" width="28" height="6" rx="3" fill="#4A90D9"/>
          <circle cx="50" cy="67" r="5" fill="#FF6B6B"/>
          <path d="M47 67 L50 63 L53 67 L50 71 Z" fill="white" opacity="0.6"/>
          <line x1="50" y1="6" x2="50" y2="14" stroke="#4A90D9" stroke-width="2.5" stroke-linecap="round"/>
          <circle cx="50" cy="6" r="4" fill="#FF6B6B"/>
          <path d="M48 5 L50 2 L52 5 L50 8 Z" fill="white" opacity="0.7"/>
          <circle cx="28" cy="10" r="2.5" fill="#7EC8E3" opacity="0.6"/>
          <circle cx="72" cy="10" r="2.5" fill="#7EC8E3" opacity="0.6"/>
          <line x1="30" y1="12" x2="40" y2="18" stroke="#7EC8E3" stroke-width="1.5" opacity="0.5"/>
          <line x1="70" y1="12" x2="60" y2="18" stroke="#7EC8E3" stroke-width="1.5" opacity="0.5"/>
        </svg>
      </div>
    </div>
    <div class="ai-chat-window" :class="{ 'open': isOpen }">
      <div class="ai-chat-header">
        <div class="d-flex align-items-center">
          <div class="mini-avatar me-2">
            <svg viewBox="0 0 100 100" class="mini-svg">
              <circle cx="50" cy="45" r="35" fill="#E8F4FD"/>
              <circle cx="50" cy="38" r="22" fill="white" stroke="#4A90D9" stroke-width="2"/>
              <circle cx="42" cy="34" r="4" fill="#4A90D9"/>
              <circle cx="58" cy="34" r="4" fill="#4A90D9"/>
              <path d="M42 42 Q50 50 58 42" stroke="#4A90D9" stroke-width="2.5" fill="none" stroke-linecap="round"/>
              <rect x="44" y="50" width="12" height="8" rx="4" fill="#7EC8E3"/>
              <circle cx="50" cy="67" r="5" fill="#FF6B6B"/>
              <line x1="50" y1="6" x2="50" y2="14" stroke="#4A90D9" stroke-width="2.5" stroke-linecap="round"/>
              <circle cx="50" cy="6" r="4" fill="#FF6B6B"/>
            </svg>
          </div>
          <h5 class="mb-0">AI寻亲助手</h5>
        </div>
        <button class="btn-close btn-close-white" @click="closeChat"></button>
      </div>
      <div class="ai-chat-messages flex-grow-1 p-3" ref="messagesRef" style="background: #f8f9fa; overflow-y: auto;">
        <div v-for="(msg, idx) in messages" :key="idx"
          class="d-flex mb-3"
          :class="msg.role === 'user' ? 'justify-content-end' : 'justify-content-start'">
          <div v-if="msg.role === 'assistant'" class="me-2">
            <div class="chat-mini-avatar">
              <svg viewBox="0 0 100 100" class="chat-mini-svg">
                <circle cx="50" cy="38" r="22" fill="white" stroke="#4A90D9" stroke-width="2"/>
                <circle cx="42" cy="34" r="3.5" fill="#4A90D9"/>
                <circle cx="58" cy="34" r="3.5" fill="#4A90D9"/>
                <path d="M43 42 Q50 48 57 42" stroke="#4A90D9" stroke-width="2" fill="none" stroke-linecap="round"/>
                <line x1="50" y1="8" x2="50" y2="14" stroke="#4A90D9" stroke-width="2" stroke-linecap="round"/>
                <circle cx="50" cy="8" r="3" fill="#FF6B6B"/>
              </svg>
            </div>
          </div>
          <div class="px-3 py-2 rounded-4"
            :class="msg.role === 'user'
              ? 'bg-primary text-white'
              : 'bg-white text-dark border'"
            style="max-width: 80%; word-wrap: break-word;"
            :style="msg.role === 'user' ? 'border-bottom-right-radius: 4px' : 'border-bottom-left-radius: 4px'">
            {{ msg.content }}
          </div>
        </div>
        <div v-if="isLoading" class="d-flex justify-content-start mb-3">
          <div class="me-2">
            <div class="chat-mini-avatar">
              <svg viewBox="0 0 100 100" class="chat-mini-svg">
                <circle cx="50" cy="38" r="22" fill="white" stroke="#4A90D9" stroke-width="2"/>
                <circle cx="42" cy="34" r="3.5" fill="#4A90D9"/>
                <circle cx="58" cy="34" r="3.5" fill="#4A90D9"/>
                <path d="M43 42 Q50 48 57 42" stroke="#4A90D9" stroke-width="2" fill="none" stroke-linecap="round"/>
                <line x1="50" y1="8" x2="50" y2="14" stroke="#4A90D9" stroke-width="2" stroke-linecap="round"/>
                <circle cx="50" cy="8" r="3" fill="#FF6B6B"/>
              </svg>
            </div>
          </div>
          <div class="bg-white border rounded-4 px-3 py-2 d-flex align-items-center" style="border-bottom-left-radius: 4px;">
            <div class="loader me-2"></div>
            <span class="text-muted small">正在思考...</span>
          </div>
        </div>
      </div>
      <div class="p-3 border-top bg-white d-flex align-items-center">
        <input
          v-model="inputText"
          type="text"
          class="form-control rounded-pill me-2"
          placeholder="请输入你的问题..."
          @keypress.enter="sendMessage"
        >
        <button class="btn btn-primary rounded-circle d-flex align-items-center justify-content-center" style="width: 40px; height: 40px;" @click="sendMessage">
          <i class="fas fa-paper-plane"></i>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick } from 'vue'
import api from '@/api'

const isOpen = ref(false)
const isLoading = ref(false)
const inputText = ref('')
const messages = ref([{ role: 'assistant', content: '你好！我是AI寻亲助手，有什么可以帮到你的吗？' }])
const messagesRef = ref(null)

function toggleChat() {
  isOpen.value = !isOpen.value
}

function closeChat() {
  isOpen.value = false
}

async function sendMessage() {
  const text = inputText.value.trim()
  if (!text) return

  messages.value.push({ role: 'user', content: text })
  inputText.value = ''
  isLoading.value = true
  scrollToBottom()

  try {
    const res = await api.post('/ai/ask', { question: text })
    if (res.code === 200) {
      messages.value.push({ role: 'assistant', content: res.data })
    } else {
      messages.value.push({ role: 'assistant', content: '抱歉，我无法回答你的问题，请稍后再试。' })
    }
  } catch (e) {
    messages.value.push({ role: 'assistant', content: '抱歉，我暂时无法连接到服务器，请稍后再试。' })
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

async function scrollToBottom() {
  await nextTick()
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}
</script>

<style scoped>
.ai-chat-header {
  background: linear-gradient(135deg, #4A90D9, #7EC8E3);
  color: white;
  padding: 12px 15px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.cartoon-avatar {
  width: 60px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.cartoon-svg {
  width: 100%;
  height: 100%;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.2));
}

.mini-avatar {
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.mini-svg {
  width: 100%;
  height: 100%;
}

.chat-mini-avatar {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.chat-mini-svg {
  width: 100%;
  height: 100%;
}

.loader {
  border: 2px solid var(--light-gray);
  border-top: 2px solid var(--primary-color);
  border-radius: 50%;
  width: 16px;
  height: 16px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}
</style>