<script setup>
import { ref } from 'vue'
import { getSafeErrorMessage } from '@/api/client'
import { useAuth } from '@/composables/useAuth'
import ModalShell from '@/components/ui/ModalShell.vue'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import AppIcon from '@/components/ui/AppIcon.vue'

const emit = defineEmits(['close', 'success'])
const { withdraw } = useAuth()

const password = ref('')
const loading = ref(false)
const error = ref('')

async function onWithdraw() {
  if (!password.value.trim()) return
  loading.value = true
  error.value = ''
  try {
    await withdraw(password.value)
    emit('success')
    emit('close')
  } catch (err) {
    error.value = getSafeErrorMessage(err, '회원 탈퇴에 실패했습니다. 비밀번호를 확인해 주세요.')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <ModalShell title="회원 탈퇴" @close="emit('close')">
    <p class="warning">탈퇴하면 내 모든 단축 URL과 통계 데이터가 영구적으로 삭제됩니다.</p>

    <transition name="fade">
      <div v-if="error" class="alert">
        <AppIcon name="warn" :size="16" />
        <span>{{ error }}</span>
      </div>
    </transition>

    <form class="form" @submit.prevent="onWithdraw">
      <div>
        <label class="label">비밀번호 확인</label>
        <BaseInput
          v-model="password"
          type="password"
          placeholder="현재 비밀번호를 입력하세요"
          required
          :disabled="loading"
        />
      </div>
      <div class="actions">
        <BaseButton variant="outline" block :disabled="loading" @click="emit('close')">
          취소
        </BaseButton>
        <BaseButton
          variant="danger-solid"
          block
          type="submit"
          :loading="loading"
          :disabled="!password"
        >
          {{ loading ? '처리 중' : '탈퇴하기' }}
        </BaseButton>
      </div>
    </form>
  </ModalShell>
</template>

<style scoped>
.warning {
  font-size: 0.85rem;
  color: var(--text-2);
  line-height: 1.55;
  margin-bottom: 0.9rem;
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
.actions {
  display: flex;
  gap: 0.5rem;
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
