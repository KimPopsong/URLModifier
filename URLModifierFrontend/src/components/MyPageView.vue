<script setup>
import { ref } from 'vue'
import { useAuth } from '@/composables/useAuth'
import { useMyPage } from '@/composables/useMyPage'
import { formatExpiry } from '@/utils/format'
import BaseButton from '@/components/ui/BaseButton.vue'
import TagChip from '@/components/ui/TagChip.vue'
import AppIcon from '@/components/ui/AppIcon.vue'
import SpinnerDot from '@/components/ui/SpinnerDot.vue'

const emit = defineEmits(['open-shorten', 'open-login', 'open-withdraw'])

const { isLoggedIn } = useAuth()
const { myPage, myPageLoading, myPageError, fetchMyPage, showUrlDetail, deleteUrl } = useMyPage()

const copiedUrlId = ref(null)

function clickUrl(url) {
  navigator.clipboard.writeText(url.shortenedUrl)
  copiedUrlId.value = url.id
  setTimeout(() => {
    copiedUrlId.value = null
  }, 2000)
}
</script>

<template>
  <div class="pane">
    <div class="head">
      <div>
        <h1 class="title">마이페이지</h1>
        <p class="desc">내가 생성한 단축 URL 목록과 간단한 통계를 확인할 수 있어요.</p>
      </div>
      <BaseButton
        v-if="isLoggedIn"
        variant="outline"
        size="sm"
        :loading="myPageLoading"
        @click="fetchMyPage()"
      >
        <AppIcon v-if="!myPageLoading" name="refresh" :size="14" />
        새로고침
      </BaseButton>
    </div>

    <div v-if="!isLoggedIn" class="empty">
      <p>마이페이지를 보려면 로그인이 필요합니다.</p>
      <BaseButton @click="emit('open-login')">로그인 하러 가기</BaseButton>
    </div>

    <template v-else>
      <transition name="fade">
        <div v-if="myPageError" class="alert">
          <AppIcon name="warn" :size="16" />
          <span>{{ myPageError }}</span>
        </div>
      </transition>

      <div v-if="myPageLoading" class="loading">
        <SpinnerDot :size="16" />
        <span>마이페이지 정보를 불러오는 중입니다...</span>
      </div>

      <div v-else-if="myPage && myPage.urls && myPage.urls.length">
        <div class="profile">
          <h2>{{ myPage.nickname || myPage.email }}</h2>
          <p class="mono">{{ myPage.email }}</p>
          <p class="count">총 {{ myPage.urls.length }}개의 단축 URL을 관리 중입니다.</p>
        </div>

        <div class="list">
          <div v-for="url in myPage.urls" :key="url.id" class="url">
            <div class="url-main">
              <div class="url-top">
                <button class="url-short mono" @click="clickUrl(url)">{{ url.shortenedUrl }}</button>
                <transition name="fade">
                  <span v-if="copiedUrlId === url.id" class="copied">
                    <AppIcon name="check" :size="12" />복사됨
                  </span>
                </transition>
              </div>
              <p class="url-origin mono">{{ url.originUrl }}</p>
              <div class="url-tags">
                <TagChip v-if="url.expired" variant="danger">만료됨</TagChip>
                <TagChip v-if="url.expiresAt && !url.expired">
                  <AppIcon name="clock" :size="13" />{{ formatExpiry(url.expiresAt) }}
                </TagChip>
                <TagChip v-if="url.maxClicks">
                  <AppIcon name="cursor" :size="13" />{{ url.clickCount }}/{{ url.maxClicks }}회
                </TagChip>
              </div>
            </div>
            <div class="url-actions">
              <BaseButton variant="outline" size="sm" @click="showUrlDetail(url)">
                <AppIcon name="chart" :size="14" />통계
              </BaseButton>
              <BaseButton variant="danger" size="sm" @click="deleteUrl(url)">
                <AppIcon name="trash" :size="14" />삭제
              </BaseButton>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="empty">
        <p>아직 생성한 단축 URL이 없습니다.</p>
        <BaseButton @click="emit('open-shorten')">첫 URL 만들러 가기</BaseButton>
      </div>

      <div v-if="!myPageLoading" class="account">
        <button class="withdraw" @click="emit('open-withdraw')">회원 탈퇴</button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.pane {
  padding: 1.9rem 2rem;
}
.head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
}
.title {
  font-size: 1.75rem;
  font-weight: 700;
  letter-spacing: -0.02em;
  color: var(--text);
}
.desc {
  font-size: 0.9rem;
  color: var(--text-2);
  margin-top: 0.4rem;
}

.alert {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-top: 1rem;
  padding: 0.75rem 0.85rem;
  border-radius: var(--radius-sm);
  background: var(--danger-bg);
  border: 1px solid var(--danger-border);
  color: var(--danger);
  font-size: 0.85rem;
}

.loading,
.empty {
  margin-top: 1.8rem;
  padding: 1.5rem;
  border: 1px dashed var(--border-strong);
  border-radius: var(--radius);
  background: var(--surface-2);
  color: var(--text-2);
  font-size: 0.9rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.9rem;
  text-align: center;
}
.loading {
  flex-direction: row;
  justify-content: center;
}

.profile {
  margin: 1.6rem 0 1rem;
}
.profile h2 {
  font-size: 1.1rem;
  font-weight: 600;
  color: var(--text);
}
.profile .mono {
  font-size: 0.85rem;
  color: var(--text-2);
  margin-top: 0.1rem;
}
.profile .count {
  font-size: 0.85rem;
  color: var(--text-3);
  margin-top: 0.3rem;
}

.list {
  display: flex;
  flex-direction: column;
  gap: 0.7rem;
  margin-top: 0.5rem;
}
.url {
  display: flex;
  justify-content: space-between;
  gap: 0.8rem;
  padding: 0.9rem 1rem;
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
  background: var(--surface);
  transition: border-color 0.15s ease;
}
.url:hover {
  border-color: var(--border-strong);
}
.url-main {
  flex: 1;
  min-width: 0;
}
.url-top {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}
.url-short {
  border: none;
  background: none;
  padding: 0;
  font-size: 0.9rem;
  font-weight: 500;
  color: var(--text);
  cursor: pointer;
  word-break: break-all;
  text-align: left;
}
.url-short:hover {
  text-decoration: underline;
}
.copied {
  display: inline-flex;
  align-items: center;
  gap: 0.2rem;
  font-size: 0.72rem;
  color: var(--text-2);
  white-space: nowrap;
}
.url-origin {
  margin-top: 0.2rem;
  font-size: 0.78rem;
  color: var(--text-3);
  word-break: break-all;
}
.url-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.3rem;
  margin-top: 0.5rem;
}
.url-actions {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
  flex: none;
}

.account {
  margin-top: 1.8rem;
  padding-top: 1rem;
  border-top: 1px solid var(--border);
  text-align: center;
}
.withdraw {
  border: none;
  background: none;
  color: var(--text-3);
  font-size: 0.8rem;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
}
.withdraw:hover {
  color: var(--danger);
  text-decoration: underline;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 640px) {
  .pane {
    padding: 1.5rem 1.3rem;
  }
  .url {
    flex-direction: column;
  }
  .url-actions {
    flex-direction: row;
  }
}
</style>
