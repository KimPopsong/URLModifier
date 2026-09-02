<script setup>
import { ref } from 'vue'
import { getSafeErrorMessage } from '@/api/client'
import { useAuth } from '@/composables/useAuth'
import ModalShell from '@/components/ui/ModalShell.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import AppIcon from '@/components/ui/AppIcon.vue'

const props = defineProps({
  initialMode: { type: String, default: 'login' },
})
const emit = defineEmits(['close', 'success'])

const { login, register } = useAuth()

const mode = ref(props.initialMode)
const loading = ref(false)
const error = ref('')
const loginForm = ref({ email: '', password: '' })
const registerForm = ref({ email: '', nickname: '', password: '' })

async function onLogin() {
  loading.value = true
  error.value = ''
  try {
    await login({ email: loginForm.value.email, password: loginForm.value.password })
    emit('success')
    emit('close')
  } catch (err) {
    error.value = getSafeErrorMessage(err, '로그인에 실패했습니다. 이메일과 비밀번호를 확인해 주세요.')
    console.error('Login error:', err)
  } finally {
    loading.value = false
  }
}

async function onRegister() {
  loading.value = true
  error.value = ''
  try {
    await register({
      email: registerForm.value.email,
      nickname: registerForm.value.nickname,
      password: registerForm.value.password,
    })
    // 회원가입 후 바로 로그인 탭으로 전환
    mode.value = 'login'
    loginForm.value.email = registerForm.value.email
    loginForm.value.password = ''
  } catch (err) {
    error.value = getSafeErrorMessage(err, '회원가입에 실패했습니다. 입력 정보를 확인해 주세요.')
    console.error('Register error:', err)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <ModalShell :title="mode === 'login' ? '로그인' : '회원가입'" @close="emit('close')">
    <div class="tabs">
      <button class="tab" :class="{ active: mode === 'login' }" @click="mode = 'login'">로그인</button>
      <button class="tab" :class="{ active: mode === 'register' }" @click="mode = 'register'">
        회원가입
      </button>
    </div>

    <transition name="fade">
      <div v-if="error" class="alert">
        <AppIcon name="warn" :size="16" />
        <span>{{ error }}</span>
      </div>
    </transition>

    <form v-if="mode === 'login'" class="form" @submit.prevent="onLogin">
      <div>
        <label class="label">이메일</label>
        <BaseInput v-model="loginForm.email" type="email" placeholder="you@example.com" required />
      </div>
      <div>
        <label class="label">비밀번호</label>
        <BaseInput v-model="loginForm.password" type="password" placeholder="비밀번호" required />
      </div>
      <BaseButton type="submit" block :loading="loading">
        {{ loading ? '로그인 중' : '로그인' }}
      </BaseButton>
    </form>

    <form v-else class="form" @submit.prevent="onRegister">
      <div>
        <label class="label">이메일</label>
        <BaseInput v-model="registerForm.email" type="email" placeholder="you@example.com" required />
      </div>
      <div>
        <label class="label">닉네임</label>
        <BaseInput v-model="registerForm.nickname" type="text" placeholder="표시할 이름" required />
      </div>
      <div>
        <label class="label">비밀번호</label>
        <BaseInput
          v-model="registerForm.password"
          type="password"
          placeholder="비밀번호 (8자 이상)"
          minlength="8"
          required
        />
      </div>
      <BaseButton type="submit" block :loading="loading">
        {{ loading ? '회원가입 중' : '회원가입' }}
      </BaseButton>
    </form>
  </ModalShell>
</template>

<style scoped>
.tabs {
  display: flex;
  gap: 0.25rem;
  margin-bottom: 1rem;
  padding: 0.25rem;
  border-radius: var(--radius-sm);
  background: var(--surface-2);
  border: 1px solid var(--border);
}
.tab {
  flex: 1;
  border: none;
  background: transparent;
  color: var(--text-2);
  font-size: 0.85rem;
  padding: 0.4rem 0;
  border-radius: 6px;
  cursor: pointer;
  transition:
    background 0.15s ease,
    color 0.15s ease;
}
.tab.active {
  background: var(--accent);
  color: var(--on-accent);
}

.form {
  display: flex;
  flex-direction: column;
  gap: 0.85rem;
}
.label {
  display: block;
  font-size: 0.8rem;
  color: var(--text-2);
  margin-bottom: 0.35rem;
}

.alert {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.85rem;
  padding: 0.7rem 0.8rem;
  border-radius: var(--radius-sm);
  background: var(--danger-bg);
  border: 1px solid var(--danger-border);
  color: var(--danger);
  font-size: 0.83rem;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
