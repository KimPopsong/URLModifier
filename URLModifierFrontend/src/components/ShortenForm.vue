<script setup>
import { ref } from 'vue'
import { api, getSafeErrorMessage } from '@/api/client'
import { useAuth } from '@/composables/useAuth'
import { useMyPage } from '@/composables/useMyPage'
import { formatExpiry } from '@/utils/format'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import TagChip from '@/components/ui/TagChip.vue'
import AppIcon from '@/components/ui/AppIcon.vue'

const { isLoggedIn, backendBaseUrl } = useAuth()
const { fetchMyPage } = useMyPage()

const originalUrl = ref('')
const useCustomUrl = ref(false)
const customSlug = ref('')
const useExpiry = ref(false)
const expiryDate = ref('')
const expiryClicks = ref(null)

const shortenedUrl = ref('')
const qrCode = ref('')
const createdExpiresAt = ref(null)
const createdMaxClicks = ref(null)
const loading = ref(false)
const error = ref('')
const isCopied = ref(false)

const nowLocal = () => new Date().toISOString().slice(0, 16)

async function shortenUrl() {
  if (!originalUrl.value.trim()) return

  loading.value = true
  error.value = ''
  shortenedUrl.value = ''
  qrCode.value = ''
  createdExpiresAt.value = null
  createdMaxClicks.value = null
  isCopied.value = false

  const expiresAt =
    useExpiry.value && isLoggedIn.value && expiryDate.value ? expiryDate.value + ':00' : null
  const clicksNum = Number(expiryClicks.value)
  const maxClicks =
    useExpiry.value && isLoggedIn.value && clicksNum > 0 ? clicksNum : null

  try {
    let response

    if (useCustomUrl.value) {
      if (!isLoggedIn.value) {
        error.value = '커스텀 URL을 사용하려면 로그인이 필요합니다.'
        return
      }
      if (!customSlug.value.trim()) {
        error.value = '사용할 커스텀 URL을 입력해 주세요.'
        return
      }

      response = await api.post('/short-urls/custom', {
        originURL: originalUrl.value.trim(),
        customURL: customSlug.value.trim(),
        expiresAt,
        maxClicks,
      })
    } else {
      response = await api.post('/short-urls', {
        url: originalUrl.value.trim(),
        expiresAt,
        maxClicks,
      })
    }

    shortenedUrl.value = response.data.shortenedUrl
    qrCode.value = `data:image/png;base64,${response.data.qrCode}`
    createdExpiresAt.value = response.data.expiresAt || null
    createdMaxClicks.value = response.data.maxClicks || null

    if (isLoggedIn.value) {
      fetchMyPage(false)
    }
  } catch (err) {
    error.value = getSafeErrorMessage(err, 'URL 단축에 실패했습니다. 잠시 후 다시 시도해 주세요.')
    console.error('Error shortening URL:', err)
  } finally {
    loading.value = false
  }
}

async function copyToClipboard() {
  try {
    await navigator.clipboard.writeText(shortenedUrl.value)
    isCopied.value = true
    setTimeout(() => {
      isCopied.value = false
    }, 2000)
  } catch (err) {
    error.value = 'URL을 클립보드에 복사하지 못했습니다.'
    console.error('Failed to copy:', err)
  }
}
</script>

<template>
  <div class="pane">
    <h1 class="title">URL 단축하기</h1>
    <p class="desc">긴 링크를 짧고 기억하기 쉬운 링크로 바꾸고, QR 코드까지 한 번에 생성해 보세요.</p>

    <form class="form" @submit.prevent="shortenUrl">
      <label class="label">원본 URL</label>
      <div class="row">
        <BaseInput
          v-model="originalUrl"
          type="url"
          placeholder="https://example.com/very/long/url..."
          required
          :disabled="loading"
          mono
        />
        <BaseButton type="submit" :loading="loading" :disabled="!originalUrl">
          {{ loading ? '단축 중' : 'URL 단축' }}
        </BaseButton>
      </div>

      <div class="options">
        <label class="check" :class="{ disabled: !isLoggedIn }">
          <input type="checkbox" v-model="useCustomUrl" :disabled="!isLoggedIn" />
          <span>커스텀 URL 사용 <span class="muted">(로그인 필요)</span></span>
        </label>

        <transition name="fade">
          <div v-if="useCustomUrl" class="custom">
            <span class="prefix mono">{{ backendBaseUrl }}/</span>
            <input
              v-model="customSlug"
              type="text"
              placeholder="원하는 별칭 (예: my-link)"
              class="custom-input mono"
              :disabled="loading"
            />
          </div>
        </transition>

        <label class="check" :class="{ disabled: !isLoggedIn }">
          <input type="checkbox" v-model="useExpiry" :disabled="!isLoggedIn" />
          <span>링크 만료 설정 <span class="muted">(로그인 필요)</span></span>
        </label>

        <transition name="fade">
          <div v-if="useExpiry && isLoggedIn" class="expiry">
            <div class="expiry-row">
              <label class="label">만료 시각 <span class="muted">(선택)</span></label>
              <BaseInput
                v-model="expiryDate"
                type="datetime-local"
                :min="nowLocal()"
                :disabled="loading"
              />
            </div>
            <div class="expiry-row">
              <label class="label">최대 클릭 수 <span class="muted">(선택)</span></label>
              <BaseInput
                v-model="expiryClicks"
                type="number"
                min="1"
                placeholder="제한 없음"
                :disabled="loading"
                mono
              />
            </div>
          </div>
        </transition>
      </div>
    </form>

    <transition name="fade">
      <div v-if="error" class="alert">
        <AppIcon name="warn" :size="16" />
        <span>{{ error }}</span>
      </div>
    </transition>

    <transition name="slide-up">
      <div v-if="shortenedUrl" class="result">
        <h2 class="result-title">생성된 단축 URL</h2>
        <p class="desc">아래 링크를 클릭해 이동하거나, 복사해서 바로 공유해 보세요.</p>

        <div class="result-url">
          <a
            :href="'https://' + shortenedUrl"
            target="_blank"
            rel="noopener noreferrer"
            class="short-link mono"
          >
            {{ shortenedUrl }}
          </a>
          <BaseButton variant="outline" size="sm" @click="copyToClipboard">
            <AppIcon :name="isCopied ? 'check' : 'copy'" :size="14" />
            {{ isCopied ? '복사됨' : '복사' }}
          </BaseButton>
        </div>

        <div v-if="qrCode" class="qr">
          <img :src="qrCode" alt="QR Code" />
        </div>

        <div v-if="createdExpiresAt || createdMaxClicks" class="tags">
          <span class="tags-label">만료 조건</span>
          <TagChip v-if="createdExpiresAt">
            <AppIcon name="clock" :size="13" />{{ formatExpiry(createdExpiresAt) }}
          </TagChip>
          <TagChip v-if="createdMaxClicks">
            <AppIcon name="cursor" :size="13" />최대 {{ createdMaxClicks }}회
          </TagChip>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.pane {
  padding: 1.9rem 2rem;
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

.form {
  margin-top: 1.6rem;
}

.label {
  display: block;
  font-size: 0.82rem;
  color: var(--text-2);
  margin-bottom: 0.4rem;
}

.muted {
  color: var(--text-3);
  font-weight: 400;
}

.row {
  display: flex;
  gap: 0.6rem;
}

.options {
  margin-top: 1.1rem;
  display: flex;
  flex-direction: column;
  gap: 0.7rem;
}

.check {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  font-size: 0.85rem;
  color: var(--text-2);
  cursor: pointer;
}
.check.disabled {
  opacity: 0.55;
  cursor: not-allowed;
}
.check input {
  accent-color: var(--accent);
}

.custom {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  border: 1px solid var(--border-strong);
  border-radius: var(--radius-sm);
  padding: 0.5rem 0.75rem;
  background: var(--surface);
}
.prefix {
  font-size: 0.8rem;
  color: var(--text-3);
  white-space: nowrap;
}
.custom-input {
  flex: 1;
  border: none;
  background: transparent;
  color: var(--text);
  font-size: 0.88rem;
  outline: none;
}

.expiry {
  display: flex;
  flex-direction: column;
  gap: 0.7rem;
  padding: 0.9rem;
  background: var(--surface-2);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
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

.result {
  margin-top: 1.9rem;
  padding-top: 1.7rem;
  border-top: 1px solid var(--border);
}
.result-title {
  font-size: 1.05rem;
  font-weight: 600;
  color: var(--text);
}

.result-url {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-top: 1.1rem;
  padding: 0.75rem 0.9rem;
  background: var(--surface-2);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
}
.short-link {
  flex: 1;
  color: var(--text);
  text-decoration: none;
  font-size: 0.9rem;
  font-weight: 500;
  word-break: break-all;
}
.short-link:hover {
  text-decoration: underline;
}

.qr {
  display: flex;
  justify-content: center;
  margin-top: 1rem;
  padding: 1rem;
  background: var(--surface-2);
  border: 1px solid var(--border);
  border-radius: var(--radius-sm);
}
.qr img {
  width: 176px;
  height: 176px;
  border-radius: 6px;
  background: #fff;
}

.tags {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 0.4rem;
  margin-top: 1rem;
}
.tags-label {
  font-size: 0.78rem;
  color: var(--text-3);
  margin-right: 0.15rem;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
.slide-up-enter-active {
  transition: all 0.25s ease;
}
.slide-up-enter-from {
  opacity: 0;
  transform: translateY(10px);
}

@media (max-width: 640px) {
  .pane {
    padding: 1.5rem 1.3rem;
  }
  .row {
    flex-direction: column;
  }
}
</style>
