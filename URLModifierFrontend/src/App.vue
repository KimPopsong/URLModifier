<script setup>
import { computed, onMounted, ref } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { useMyPage } from '@/composables/useMyPage'
import AppHeader from '@/components/AppHeader.vue'
import ShortenForm from '@/components/ShortenForm.vue'
import FeaturePanel from '@/components/FeaturePanel.vue'
import MyPageView from '@/components/MyPageView.vue'
import UrlDetailPanel from '@/components/UrlDetailPanel.vue'
import AuthModal from '@/components/modals/AuthModal.vue'
import WithdrawModal from '@/components/modals/WithdrawModal.vue'
import ExpiredModal from '@/components/modals/ExpiredModal.vue'

const { initAuth, logout, isLoggedIn } = useAuth()
const { myPage, selectedUrlDetail, fetchMyPage, closeUrlDetail, resetMyPage } = useMyPage()

const activeTab = ref('shorten')
const showAuthModal = ref(false)
const authMode = ref('login')
const showWithdrawModal = ref(false)
const expiredLinkCode = ref(null)

const isDetailView = computed(() => activeTab.value === 'mypage' && !!selectedUrlDetail.value)
const showSide = computed(() => activeTab.value === 'shorten' || isDetailView.value)

onMounted(() => {
  const params = new URLSearchParams(window.location.search)
  const expiredCode = params.get('expired')
  if (expiredCode) {
    expiredLinkCode.value = expiredCode
    window.history.replaceState({}, '', '/')
  }
  initAuth()
})

function openShorten() {
  closeUrlDetail()
  activeTab.value = 'shorten'
}

async function openMyPage() {
  activeTab.value = 'mypage'
  if (isLoggedIn.value && !myPage.value) {
    await fetchMyPage()
  }
}

function openAuthModal(mode = 'login') {
  authMode.value = mode
  showAuthModal.value = true
}

async function onLogout() {
  await logout()
  resetMyPage()
}

function onLoginSuccess() {
  fetchMyPage(false)
}

function onWithdrawSuccess() {
  resetMyPage()
  activeTab.value = 'shorten'
}

const year = new Date().getFullYear()
</script>

<template>
  <div class="app">
    <AppHeader
      :active-tab="activeTab"
      @shorten="openShorten"
      @mypage="openMyPage"
      @login="openAuthModal('login')"
      @logout="onLogout"
    />

    <main class="main">
      <div class="grid" :class="{ 'grid--detail': isDetailView }">
        <section class="card card--main">
          <transition name="swap" mode="out-in">
            <ShortenForm v-if="activeTab === 'shorten'" key="shorten" />
            <MyPageView
              v-else
              key="mypage"
              @open-shorten="openShorten"
              @open-login="openAuthModal('login')"
              @open-withdraw="showWithdrawModal = true"
            />
          </transition>
        </section>

        <transition name="swap">
          <aside v-if="showSide" class="card card--side">
            <FeaturePanel v-if="activeTab === 'shorten'" />
            <UrlDetailPanel v-else />
          </aside>
        </transition>
      </div>
    </main>

    <footer class="footer">
      <p>&copy; {{ year }} URLcut · kimds5344@naver.com</p>
    </footer>

    <transition name="fade">
      <AuthModal
        v-if="showAuthModal"
        :initial-mode="authMode"
        @close="showAuthModal = false"
        @success="onLoginSuccess"
      />
    </transition>

    <transition name="fade">
      <WithdrawModal
        v-if="showWithdrawModal"
        @close="showWithdrawModal = false"
        @success="onWithdrawSuccess"
      />
    </transition>

    <transition name="fade">
      <ExpiredModal v-if="expiredLinkCode" @close="expiredLinkCode = null" />
    </transition>
  </div>
</template>

<style scoped>
.app {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg);
  overflow: hidden;
}

.main {
  flex: 1;
  min-height: 0;
  padding: 2.2rem 1.5rem 1.5rem;
  display: flex;
  justify-content: center;
  overflow: hidden;
}

.grid {
  display: flex;
  width: 100%;
  max-width: var(--container);
  height: 100%;
  gap: 20px;
}

.card {
  background: var(--surface);
  border: 1px solid var(--border);
  border-radius: var(--radius);
  height: 100%;
  overflow-y: auto;
}

.card--main {
  flex: 1.5;
  transition: flex 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.card--side {
  flex: 0 0 340px;
}

.grid--detail .card--main {
  flex: 0 0 400px;
}
.grid--detail .card--side {
  flex: 1 1 0;
  min-width: 0;
}

.footer {
  padding: 1rem 1.5rem 1.2rem;
  text-align: center;
  color: var(--text-3);
  font-size: 0.8rem;
}

/* 카드 내부 스크롤바 */
.card::-webkit-scrollbar {
  width: 6px;
}
.card::-webkit-scrollbar-thumb {
  background-color: rgba(0, 0, 0, 0.15);
  border-radius: 4px;
}

/* 콘텐츠 전환 */
.swap-enter-active,
.swap-leave-active {
  transition: opacity 0.18s ease;
}
.swap-enter-from,
.swap-leave-to {
  opacity: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 900px) {
  .grid {
    flex-direction: column;
    height: auto;
  }
  .card {
    height: auto;
  }
  .main {
    overflow-y: auto;
  }
  .card--side {
    flex: none;
  }
  .grid--detail .card--main {
    flex: none;
  }
}

@media (max-width: 640px) {
  .main {
    padding: 1.5rem 1rem 1rem;
  }
}
</style>
