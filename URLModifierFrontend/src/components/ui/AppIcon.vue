<script setup>
// 라인 아이콘 (stroke currentColor). 이모지 대체용. 외부 의존성 없음.
defineProps({
  name: { type: String, required: true },
  size: { type: [Number, String], default: 18 },
})

const paths = {
  link: [
    'M10 13a5 5 0 0 0 7.54.54l3-3a5 5 0 0 0-7.07-7.07l-1.72 1.71',
    'M14 11a5 5 0 0 0-7.54-.54l-3 3a5 5 0 0 0 7.07 7.07l1.71-1.71',
  ],
  qr: [
    'M4 4h5v5H4z',
    'M15 4h5v5h-5z',
    'M4 15h5v5H4z',
    'M15 15h2v2h-2z',
    'M19 15h1M15 19h1M19 19h1M20 17h.01',
  ],
  chart: ['M4 4v16h16', 'M8 16v-4', 'M12.5 16V9', 'M17 16v-7'],
  clock: ['M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z', 'M12 7.5V12l3 1.8'],
  user: ['M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8z', 'M4.5 20a7.5 7.5 0 0 1 15 0'],
  close: ['M18 6 6 18', 'M6 6l12 12'],
  refresh: ['M21 12a9 9 0 1 1-2.64-6.36L21 8', 'M21 3v5h-5'],
  cursor: ['M4 3l7 17 2.2-6.8L20 11 4 3z'],
  external: ['M14 4h6v6', 'M20 4 10 14', 'M19 14v5a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1h5'],
  copy: ['M9 9h11v11H9z', 'M5 15H4V4h11v1'],
  check: ['M20 6 9 17l-5-5'],
  warn: ['M10.3 3.9 1.8 18a2 2 0 0 0 1.7 3h17a2 2 0 0 0 1.7-3L13.7 3.9a2 2 0 0 0-3.4 0z', 'M12 9v4', 'M12 17h.01'],
  trash: ['M3 6h18', 'M8 6V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2', 'M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6'],
}
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.5"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <path v-for="(d, i) in paths[name]" :key="i" :d="d" />
  </svg>
</template>
