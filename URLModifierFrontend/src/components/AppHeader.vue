<script setup>
import { useAuth } from '@/composables/useAuth'

defineProps({
  activeTab: { type: String, required: true },
})
const emit = defineEmits(['shorten', 'mypage', 'login', 'logout'])

const { isLoggedIn, user } = useAuth()
</script>

<template>
  <header class="header">
    <div class="header-inner">
      <button class="brand" @click="emit('shorten')">
        <img src="/logo.png" alt="URLcut" class="brand-icon" />
        <span class="brand-name">URLcut</span>
      </button>

      <nav class="nav">
        <button class="nav-item" :class="{ active: activeTab === 'shorten' }" @click="emit('shorten')">
          URL 단축
        </button>
        <button class="nav-item" :class="{ active: activeTab === 'mypage' }" @click="emit('mypage')">
          마이페이지
        </button>
      </nav>

      <div class="auth">
        <button v-if="!isLoggedIn" class="login-btn" @click="emit('login')">로그인 / 회원가입</button>
        <div v-else class="user">
          <span class="user-name">{{ user?.nickName || user?.email }}</span>
          <button class="logout" @click="emit('logout')">로그아웃</button>
        </div>
      </div>
    </div>
  </header>
</template>

<style scoped>
.header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: var(--surface);
  border-bottom: 1px solid var(--border);
}

.header-inner {
  max-width: var(--container);
  margin: 0 auto;
  padding: 0.85rem 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.55rem;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0;
}

.brand-icon {
  width: 1.6rem;
  height: 1.6rem;
  object-fit: contain;
}

.brand-name {
  font-size: 1.05rem;
  font-weight: 700;
  letter-spacing: -0.01em;
  color: var(--text);
}

.nav {
  display: flex;
  gap: 0.25rem;
}

.nav-item {
  border: none;
  background: transparent;
  color: var(--text-2);
  padding: 0.4rem 0.8rem;
  border-radius: var(--radius-sm);
  font-size: 0.9rem;
  cursor: pointer;
  transition:
    background 0.15s ease,
    color 0.15s ease;
}

.nav-item:hover {
  background: var(--surface-2);
  color: var(--text);
}

.nav-item.active {
  background: var(--accent);
  color: var(--on-accent);
}

.auth {
  display: flex;
  align-items: center;
}

.login-btn {
  border: 1px solid var(--border-strong);
  background: transparent;
  color: var(--text);
  border-radius: var(--radius-sm);
  padding: 0.45rem 0.85rem;
  font-size: 0.85rem;
  cursor: pointer;
  white-space: nowrap;
  transition: background 0.15s ease;
}

.login-btn:hover {
  background: var(--surface-2);
}

.user {
  display: inline-flex;
  align-items: center;
  gap: 0.6rem;
  font-size: 0.85rem;
}

.user-name {
  max-width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text);
}

.logout {
  border: none;
  background: transparent;
  color: var(--text-3);
  font-size: 0.82rem;
  cursor: pointer;
  padding: 0.25rem 0.35rem;
}

.logout:hover {
  color: var(--text);
  text-decoration: underline;
}

@media (max-width: 640px) {
  .header-inner {
    flex-wrap: wrap;
    justify-content: center;
  }
  .nav {
    order: 3;
  }
}
</style>
