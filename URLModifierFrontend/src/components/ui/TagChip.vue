<script setup>
defineProps({
  variant: { type: String, default: 'neutral' }, // neutral | danger
})
</script>

<template>
  <span class="chip" :class="`chip--${variant}`">
    <slot />
  </span>
</template>

<style scoped>
.chip {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  font-size: 0.74rem;
  font-weight: 500;
  padding: 0.2rem 0.55rem;
  border-radius: var(--radius-sm);
  border: 1px solid var(--border);
  white-space: nowrap;
}

.chip--neutral {
  background: var(--surface-2);
  color: var(--text-2);
}

.chip--danger {
  background: var(--danger-bg);
  border-color: var(--danger-border);
  color: var(--danger);
}

.chip :deep(svg) {
  opacity: 0.8;
}
</style>
