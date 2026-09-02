<script setup>
import ModalShell from '@/components/ui/ModalShell.vue'
import BaseButton from '@/components/ui/BaseButton.vue'

const emit = defineEmits(['close'])
</script>

<template>
  <ModalShell title="링크 만료" @close="emit('close')">
    <p class="message">만료된 링크입니다.</p>
    <BaseButton block @click="emit('close')">확인</BaseButton>
  </ModalShell>
</template>

<style scoped>
.message {
  font-size: 0.95rem;
  color: var(--text-2);
  margin: 0.25rem 0 1.25rem;
}
</style>
