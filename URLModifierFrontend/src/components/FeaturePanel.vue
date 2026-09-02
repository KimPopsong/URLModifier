<script setup>
import AppIcon from '@/components/ui/AppIcon.vue'

const features = [
  {
    icon: 'qr',
    title: '단축 & QR 코드',
    body: '링크를 단축하면 QR 코드가 자동으로 생성되어 오프라인에서도 쉽게 공유할 수 있어요.',
  },
  {
    icon: 'user',
    title: '마이페이지 관리',
    body: '로그인 후 내가 만든 모든 링크를 한 번에 관리하고, 필요 없는 링크는 즉시 삭제할 수 있습니다.',
  },
  {
    icon: 'chart',
    title: '간단 통계',
    body: '각 링크별 클릭 이력(클릭 수, 시간 정보 등)을 통해 어느 링크가 인기 있는지 확인해 보세요.',
  },
]
</script>

<template>
  <div class="pane">
    <h2 class="title">URLcut을 더 잘 활용하는 법</h2>
    <ul class="list">
      <li v-for="f in features" :key="f.title" class="item">
        <span class="icon"><AppIcon :name="f.icon" :size="18" /></span>
        <div>
          <h3 class="item-title">{{ f.title }}</h3>
          <p class="item-body">{{ f.body }}</p>
        </div>
      </li>
    </ul>
  </div>
</template>

<style scoped>
.pane {
  padding: 1.9rem 1.6rem;
}
.title {
  font-size: 1.05rem;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 1.3rem;
}
.list {
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 1.3rem;
}
.item {
  display: flex;
  gap: 0.8rem;
}
.icon {
  flex: none;
  width: 2.1rem;
  height: 2.1rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  color: var(--text);
  background: var(--surface-2);
}
.item-title {
  font-size: 0.92rem;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 0.2rem;
}
.item-body {
  font-size: 0.85rem;
  color: var(--text-2);
  line-height: 1.55;
}
</style>
