<script setup>
import AppIcon from '@/components/ui/AppIcon.vue'

defineProps({
  title: { type: String, default: '' },
})
const emit = defineEmits(['close'])
</script>

<template>
  <div class="backdrop" @click.self="emit('close')">
    <div class="modal">
      <div class="modal-head">
        <h2>{{ title }}</h2>
        <button class="close" @click="emit('close')"><AppIcon name="close" :size="18" /></button>
      </div>
      <slot />
    </div>
  </div>
</template>

<style scoped>
.backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 1rem;
  z-index: 40;
}
.modal {
  width: 100%;
  max-width: 420px;
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 1.5rem;
}
.modal-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
}
.modal-head h2 {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text);
}
.close {
  border: none;
  background: none;
  color: var(--text-2);
  cursor: pointer;
  display: inline-flex;
  padding: 0.2rem;
  border-radius: var(--radius-sm);
}
.close:hover {
  background: var(--surface-2);
  color: var(--text);
}
</style>
