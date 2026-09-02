<script setup>
import SpinnerDot from '@/components/ui/SpinnerDot.vue'

defineProps({
  variant: { type: String, default: 'primary' }, // primary | outline | ghost | danger | danger-solid
  size: { type: String, default: 'md' }, // md | sm
  type: { type: String, default: 'button' },
  disabled: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
  block: { type: Boolean, default: false },
})
</script>

<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    class="btn"
    :class="[`btn--${variant}`, `btn--${size}`, { 'btn--block': block }]"
  >
    <SpinnerDot v-if="loading" :size="size === 'sm' ? 13 : 15" />
    <slot />
  </button>
</template>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 0.45rem;
  border: 1px solid transparent;
  border-radius: var(--radius-sm);
  font-size: 0.9rem;
  font-weight: 500;
  line-height: 1;
  padding: 0.7rem 1.15rem;
  cursor: pointer;
  white-space: nowrap;
  transition:
    background 0.15s ease,
    border-color 0.15s ease,
    color 0.15s ease;
}

.btn--sm {
  padding: 0.45rem 0.75rem;
  font-size: 0.82rem;
}

.btn--block {
  width: 100%;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn--primary {
  background: var(--accent);
  color: var(--on-accent);
}
.btn--primary:not(:disabled):hover {
  background: var(--accent-hover);
}

.btn--outline {
  background: transparent;
  border-color: var(--border-strong);
  color: var(--text);
}
.btn--outline:not(:disabled):hover {
  background: var(--surface-2);
}

.btn--ghost {
  background: transparent;
  color: var(--text-2);
}
.btn--ghost:not(:disabled):hover {
  background: var(--surface-2);
  color: var(--text);
}

.btn--danger {
  background: var(--danger-bg);
  border-color: var(--danger-border);
  color: var(--danger);
}
.btn--danger:not(:disabled):hover {
  background: #fee4e2;
}

.btn--danger-solid {
  background: var(--danger);
  color: #fff;
}
.btn--danger-solid:not(:disabled):hover {
  background: var(--danger-hover);
}
</style>
