<script setup>
defineProps({
  modelValue: { type: [String, Number], default: '' },
  type: { type: String, default: 'text' },
  mono: { type: Boolean, default: false },
})
defineEmits(['update:modelValue'])
defineOptions({ inheritAttrs: false })
</script>

<template>
  <input
    :type="type"
    :value="modelValue"
    class="input"
    :class="{ mono }"
    v-bind="$attrs"
    @input="$emit('update:modelValue', $event.target.value)"
  />
</template>

<style scoped>
.input {
  width: 100%;
  padding: 0.7rem 0.85rem;
  border: 1px solid var(--border-strong);
  border-radius: var(--radius-sm);
  background: var(--surface);
  color: var(--text);
  font-size: 0.92rem;
  font-family: inherit;
  outline: none;
  transition:
    border-color 0.15s ease,
    box-shadow 0.15s ease;
}

.input.mono {
  font-family: var(--font-mono);
}

.input::placeholder {
  color: var(--text-3);
}

.input:focus {
  border-color: var(--accent);
  box-shadow: 0 0 0 3px rgba(17, 17, 17, 0.08);
}

.input:disabled {
  background: var(--surface-2);
  opacity: 0.7;
  cursor: not-allowed;
}
</style>
