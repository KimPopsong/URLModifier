<script setup>
defineProps({
  size: { type: [Number, String], default: 16 },
})
</script>

<template>
  <span class="spinner" :style="{ width: size + 'px', height: size + 'px' }" aria-hidden="true" />
</template>

<style scoped>
.spinner {
  display: inline-block;
  border: 2px solid currentColor;
  border-top-color: transparent;
  border-radius: 50%;
  opacity: 0.9;
  animation: spin 0.6s linear infinite;
  flex: none;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
