<template>
  <div id="app" class="app-container">
    <!-- 헤더 / 내비게이션 -->
    <header class="app-header">
      <div class="header-inner">
        <div class="brand" @click="openShorten()">
          <span class="brand-icon">🔗</span>
          <div class="brand-text">
            <span class="brand-title">URL Modifier</span>
            <span class="brand-subtitle">Smart URL Shortener</span>
          </div>
        </div>

        <nav class="nav">
          <button
            class="nav-item"
            :class="{ active: activeTab === 'shorten' }"
            @click="openShorten()"
          >
            URL 단축
          </button>
          <button class="nav-item" :class="{ active: activeTab === 'mypage' }" @click="openMyPage">
            마이페이지
          </button>
        </nav>

        <div class="auth-area">
          <button v-if="!isLoggedIn" class="btn-ghost" @click="openAuthModal('login')">
            로그인 / 회원가입
          </button>
          <div v-else class="user-chip">
            <span class="user-email">{{ user.nickName || user.email }}</span>
            <button class="btn-ghost small" @click="logout">로그아웃</button>
          </div>
        </div>
      </div>
    </header>

    <!-- 메인 콘텐츠 -->
    <main class="app-main">
      <div
        class="content-grid"
        :class="{ 'mypage-detail-view': activeTab === 'mypage' && selectedUrlDetail }"
      >
        <section class="card main-card">
          <!-- 좌측: URL 단축 카드 -->
          <transition name="content-fade" mode="out-in">
            <div class="card-body" v-if="activeTab === 'shorten'" key="shorten-view">
              <h2 class="card-title">URL 단축하기</h2>
              <p class="card-description">
                긴 링크를 짧고 기억하기 쉬운 링크로 바꾸고, QR 코드까지 한 번에 생성해 보세요.
              </p>

              <form @submit.prevent="shortenUrl" class="url-form">
                <label class="field-label">원본 URL</label>
                <div class="input-group">
                  <input
                    v-model="originalUrl"
                    type="url"
                    placeholder="https://example.com/very/long/url..."
                    required
                    class="url-input"
                    :disabled="loading"
                  />
                  <button type="submit" class="btn-submit" :disabled="loading || !originalUrl">
                    <span v-if="loading" class="spinner"></span>
                    <span>{{ loading ? '단축 중...' : 'URL 단축' }}</span>
                  </button>
                </div>

                <div class="advanced-options">
                  <label class="checkbox" :class="{ disabled: !isLoggedIn }">
                    <input type="checkbox" v-model="useCustomUrl" :disabled="!isLoggedIn" />
                    <span :class="{ 'text-muted': !isLoggedIn }"
                      >커스텀 URL 사용 (로그인 필요)</span
                    >
                  </label>

                  <transition name="fade">
                    <div v-if="useCustomUrl" class="custom-input-group">
                      <span class="prefix">{{ backendBaseUrl }}/</span>
                      <input
                        v-model="customSlug"
                        type="text"
                        placeholder="원하는 별칭 (예: my-link)"
                        class="custom-input"
                        :disabled="loading"
                      />
                    </div>
                  </transition>
                </div>
              </form>

              <transition name="fade">
                <div v-if="error" class="alert alert-error">
                  <span class="alert-icon">⚠️</span>
                  <span>{{ error }}</span>
                </div>
              </transition>

              <transition name="slide-up">
                <div v-if="shortenedUrl" class="result">
                  <div class="result-header">
                    <h3>생성된 단축 URL</h3>
                    <p class="result-sub">
                      아래 링크를 클릭해 이동하거나, 복사해서 바로 공유해 보세요.
                    </p>
                  </div>
                  <div class="result-content">
                    <div class="shortened-url-container">
                      <a
                        :href="shortenedUrl"
                        target="_blank"
                        class="shortened-url"
                        rel="noopener noreferrer"
                      >
                        {{ shortenedUrl }}
                      </a>
                      <button
                        @click="copyToClipboard"
                        class="btn-copy"
                        :class="{ copied: isCopied }"
                      >
                        {{ isCopied ? '✓ 복사됨' : '복사' }}
                      </button>
                    </div>
                    <div v-if="qrCode" class="qr-container">
                      <img :src="qrCode" alt="QR Code" class="qr-code" />
                    </div>
                  </div>
                </div>
              </transition>
            </div>
            <!-- 마이페이지 -->
            <div class="card-body" v-else-if="activeTab === 'mypage'" key="mypage-view">
              <div class="card-header-row">
                <div>
                  <h2 class="card-title">마이페이지</h2>
                  <p class="card-description">
                    내가 생성한 단축 URL 목록과 간단한 통계를 확인할 수 있어요.
                  </p>
                </div>
                <button class="btn-outline small" @click="fetchMyPage" :disabled="myPageLoading">
                  {{ myPageLoading ? '새로고침 중...' : '새로고침' }}
                </button>
              </div>

              <div v-if="!isLoggedIn" class="empty-state">
                <p>마이페이지를 보려면 로그인이 필요합니다.</p>
                <button class="btn-submit" @click="openAuthModal('login')">로그인 하러 가기</button>
              </div>

              <template v-else>
                <transition name="fade">
                  <div v-if="myPageError" class="alert alert-error">
                    <span class="alert-icon">⚠️</span>
                    <span>{{ myPageError }}</span>
                  </div>
                </transition>

                <div v-if="myPageLoading" class="loading-state">
                  <span class="spinner"></span>
                  <span>마이페이지 정보를 불러오는 중입니다...</span>
                </div>

                <div v-else-if="myPage && myPage.urls && myPage.urls.length">
                  <div class="profile-summary">
                    <h3>{{ myPage.nickname || myPage.email }}</h3>
                    <p>{{ myPage.email }}</p>
                    <p class="muted">총 {{ myPage.urls.length }}개의 단축 URL을 관리 중입니다.</p>
                  </div>

                  <div class="url-list">
                    <div v-for="url in myPage.urls" :key="url.id" class="url-item">
                      <div class="url-main">
                        <a href="javascript:void(0)" class="url-short" @click="clickUrl(url)">
                          {{ url.shortenedUrl }}
                        </a>
                        <p class="url-origin">{{ url.originUrl }}</p>
                      </div>
                      <div class="url-actions">
                        <button class="btn-outline small" @click="showUrlDetail(url)">통계</button>
                        <button class="btn-danger small" @click="deleteUrl(url)">삭제</button>
                      </div>
                    </div>
                  </div>

                  <!-- 통계 패널 -->
                </div>

                <div v-else class="empty-state">
                  <p>아직 생성한 단축 URL이 없습니다.</p>
                  <button class="btn-submit" @click="openShorten()">첫 URL 만들러 가기</button>
                </div>
              </template>
            </div>
          </transition>
        </section>

        <!-- 우측: 소개 / 통계 패널 -->
        <transition :name="transitionName">
          <aside class="card side-card" v-if="activeTab === 'shorten'">
            <div class="card-body">
              <h2 class="side-title">URL Modifier를 더 잘 활용하는 법</h2>
              <ul class="feature-list">
                <li>
                  <h3>✨ 단축 &amp; QR 코드</h3>
                  <p>
                    링크를 단축하면 QR 코드가 자동으로 생성되어 오프라인에서도 쉽게 공유할 수
                    있어요.
                  </p>
                </li>
                <li>
                  <h3>👤 마이페이지 관리</h3>
                  <p>
                    로그인 후 내가 만든 모든 링크를 한 번에 관리하고, 필요 없는 링크는 즉시 삭제할
                    수 있습니다.
                  </p>
                </li>
                <li>
                  <h3>📊 간단 통계</h3>
                  <p>
                    각 링크별 클릭 이력(클릭 수, 시간 정보 등)을 통해 어느 링크가 인기 있는지 확인해
                    보세요.
                  </p>
                </li>
              </ul>
            </div>
          </aside>
          <aside class="card side-card" v-else-if="activeTab === 'mypage' && selectedUrlDetail">
            <div class="card-body">
              <div class="card-header-row">
                <div>
                  <h2 class="side-title">URL 통계</h2>
                  <p class="card-description">좌측 목록에서 통계를 확인할 URL을 선택하세요.</p>
                </div>
                <button class="btn-ghost small" v-if="selectedUrlDetail" @click="closeUrlDetail">
                  닫기
                </button>
              </div>

              <div v-if="selectedUrlDetail">
                <p class="detail-label">원본 URL</p>
                <p class="detail-value">{{ selectedUrlDetail.originURL }}</p>
                <br />
                <p class="detail-label">단축 URL</p>
                <p class="detail-value">
                  {{ backendBaseUrl }}/{{ selectedUrlDetail.shortenedURL }}
                </p>
                <br />
                <p class="detail-label">생성 일시</p>
                <p class="detail-value">{{ formatDateTime(selectedUrlDetail.createdAt) }}</p>
                <br />
                <p class="detail-label">총 클릭 수</p>
                <p class="detail-value">{{ selectedUrlDetail.clickEventList?.length || 0 }}회</p>

                <div
                  v-if="
                    selectedUrlDetail.clickEventList && selectedUrlDetail.clickEventList.length > 0
                  "
                  class="chart-container"
                >
                  <canvas ref="chartCanvas"></canvas>
                  <p class="chart-hint">마우스 휠로 확대/축소 가능</p>
                </div>
              </div>
              <div v-else class="empty-state" style="margin-top: 1rem">
                <p>좌측 목록에서 통계를 볼 URL을 선택하세요.</p>
              </div>
            </div>
          </aside>
        </transition>
      </div>
    </main>

    <!-- 인증 모달 -->
    <transition name="fade">
      <div v-if="showAuthModal" class="modal-backdrop" @click.self="closeAuthModal">
        <div class="modal">
          <div class="modal-header">
            <h2>{{ authMode === 'login' ? '로그인' : '회원가입' }}</h2>
            <button class="btn-ghost small" @click="closeAuthModal">✕</button>
          </div>

          <div class="modal-tabs">
            <button
              class="modal-tab"
              :class="{ active: authMode === 'login' }"
              @click="authMode = 'login'"
            >
              로그인
            </button>
            <button
              class="modal-tab"
              :class="{ active: authMode === 'register' }"
              @click="authMode = 'register'"
            >
              회원가입
            </button>
          </div>

          <transition name="fade">
            <div v-if="authError" class="alert alert-error">
              <span class="alert-icon">⚠️</span>
              <span>{{ authError }}</span>
            </div>
          </transition>

          <form v-if="authMode === 'login'" @submit.prevent="login" class="auth-form">
            <label class="field-label">이메일</label>
            <input
              v-model="loginForm.email"
              type="email"
              class="text-input"
              placeholder="you@example.com"
              required
            />

            <label class="field-label">비밀번호</label>
            <input
              v-model="loginForm.password"
              type="password"
              class="text-input"
              placeholder="비밀번호"
              required
            />

            <button class="btn-submit full" type="submit" :disabled="authLoading">
              <span v-if="authLoading" class="spinner"></span>
              <span>{{ authLoading ? '로그인 중...' : '로그인' }}</span>
            </button>
          </form>

          <form v-else @submit.prevent="register" class="auth-form">
            <label class="field-label">이메일</label>
            <input
              v-model="registerForm.email"
              type="email"
              class="text-input"
              placeholder="you@example.com"
              required
            />

            <label class="field-label">닉네임</label>
            <input
              v-model="registerForm.nickname"
              type="text"
              class="text-input"
              placeholder="표시할 이름"
              required
            />

            <label class="field-label">비밀번호</label>
            <input
              v-model="registerForm.password"
              type="password"
              class="text-input"
              placeholder="비밀번호"
              required
            />

            <button class="btn-submit full" type="submit" :disabled="authLoading">
              <span v-if="authLoading" class="spinner"></span>
              <span>{{ authLoading ? '회원가입 중...' : '회원가입' }}</span>
            </button>
          </form>
        </div>
      </div>
    </transition>

    <footer class="app-footer">
      <p>&copy; {{ new Date().getFullYear() }} URL Modifier · kimds5344@naver.com</p>
    </footer>
  </div>
</template>

<script>
import axios from 'axios'
import { Chart, registerables } from 'chart.js'
import zoomPlugin from 'chartjs-plugin-zoom'

Chart.register(...registerables)
Chart.register(zoomPlugin)

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

// Axios 전역 인터셉터 설정
// 모든 요청에 accessToken이 있으면 Authorization 헤더를 자동으로 추가
// 응답에서 401(만료) 발생 시 localStorage 토큰/유저 정보를 제거
axios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('accessToken')

    if (token) {
      config.headers = config.headers || {}

      if (!config.headers.Authorization) {
        config.headers.Authorization = `Bearer ${token}`
      }
    }

    return config
  },
  (error) => Promise.reject(error),
)

axios.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error?.response?.status
    const message = error?.response?.data?.message

    // 백엔드에서 만료된 토큰에 대해 401 + "Access token expired"를 내려줌
    if (status === 401 && typeof message === 'string' && message.includes('Access token expired')) {
      localStorage.removeItem('user')
      localStorage.removeItem('accessToken')
      localStorage.removeItem('refreshToken')

      // 새로고침하여 App.created()에서 더 이상 토큰을 복구하지 않도록 함
      window.location.reload()
    }

    return Promise.reject(error)
  },
)

export default {
  name: 'App',
  data() {
    return {
      // 공통 상태
      activeTab: 'shorten',
      backendBaseUrl: API_BASE_URL,

      // URL 단축
      originalUrl: '',
      useCustomUrl: false,
      customSlug: '',
      shortenedUrl: '',
      qrCode: '',
      loading: false,
      error: '',
      isCopied: false,

      // 인증/유저
      user: null,
      accessToken: null,
      refreshToken: null,
      showAuthModal: false,
      authMode: 'login',
      authLoading: false,
      authError: '',
      loginForm: {
        email: '',
        password: '',
      },
      registerForm: {
        email: '',
        nickname: '',
        password: '',
      },

      // 마이페이지
      myPage: null,
      myPageLoading: false,
      myPageError: '',
      selectedUrlDetail: null,
      chartInstance: null,
    }
  },
  computed: {
    isLoggedIn() {
      return !!this.accessToken
    },
    transitionName() {
      // 마이페이지에서는 flex 기반의 다른 애니메이션을 적용
      return this.activeTab === 'mypage' ? 'slide-pane-flex' : 'slide-pane'
    },
  },
  created() {
    // 로컬 스토리지에 저장된 토큰/유저 복구
    const storedAccess = localStorage.getItem('accessToken')
    const storedRefresh = localStorage.getItem('refreshToken')
    const storedUser = localStorage.getItem('user')

    if (storedAccess && storedUser) {
      this.accessToken = storedAccess
      this.refreshToken = storedRefresh
      this.user = JSON.parse(storedUser)
    }
  },
  methods: {
    // ===== 유틸리티 =====
    /**
     * BE에서 받은 에러 메시지를 안전하게 변환
     * 내부 시스템 정보나 민감한 정보가 노출되지 않도록 처리
     */
    getSafeErrorMessage(
      err,
      defaultMessage = '요청 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.',
    ) {
      // BE의 ErrorResponse 구조: { errorCode, message }
      const errorData = err?.response?.data

      if (errorData?.message) {
        // BE에서 이미 안전한 메시지를 보내므로 그대로 사용
        return errorData.message
      }

      if (errorData?.error) {
        // errorCode가 있는 경우 기본 메시지 사용
        return defaultMessage
      }

      // 예상치 못한 에러 메시지가 오는 경우 기본 메시지 사용
      // (내부 시스템 정보가 노출될 수 있으므로)
      return defaultMessage
    },

    // ===== URL 단축 =====
    async shortenUrl() {
      if (!this.originalUrl.trim()) return

      this.loading = true
      this.error = ''
      this.shortenedUrl = ''
      this.qrCode = ''
      this.isCopied = false

      try {
        let response

        if (this.useCustomUrl) {
          if (!this.isLoggedIn) {
            this.error = '커스텀 URL을 사용하려면 로그인이 필요합니다.'
            return
          }
          if (!this.customSlug.trim()) {
            this.error = '사용할 커스텀 URL을 입력해 주세요.'
            return
          }

          response = await axios.post(`${API_BASE_URL}/short-urls/custom`, {
            originURL: this.originalUrl.trim(),
            customURL: this.customSlug.trim(),
          })
        } else {
          // 로그인한 사용자가 일반 URL을 만들 때도 인증 헤더 전송
          response = await axios.post(`${API_BASE_URL}/short-urls`, {
            url: this.originalUrl.trim(),
          })
        }

        this.shortenedUrl = response.data.shortenedUrl
        this.qrCode = `data:image/png;base64,${response.data.qrCode}`

        // 마이페이지 갱신
        if (this.isLoggedIn) {
          this.fetchMyPage(false)
        }
      } catch (err) {
        this.error = this.getSafeErrorMessage(
          err,
          'URL 단축에 실패했습니다. 잠시 후 다시 시도해 주세요.',
        )
        console.error('Error shortening URL:', err)
      } finally {
        this.loading = false
      }
    },

    async copyToClipboard() {
      try {
        await navigator.clipboard.writeText(this.shortenedUrl)
        this.isCopied = true
        setTimeout(() => {
          this.isCopied = false
        }, 2000)
      } catch (err) {
        this.error = 'URL을 클립보드에 복사하지 못했습니다.'
        console.error('Failed to copy:', err)
      }
    },

    // ===== 인증 관련 =====
    openAuthModal(mode = 'login') {
      this.authMode = mode
      this.showAuthModal = true
      this.authError = ''
    },
    closeAuthModal() {
      this.showAuthModal = false
      this.authError = ''
      this.authLoading = false
    },

    async login() {
      this.authLoading = true
      this.authError = ''
      try {
        const res = await axios.post(`${API_BASE_URL}/auth/login`, {
          email: this.loginForm.email,
          password: this.loginForm.password,
        })

        const data = res.data
        this.user = {
          id: data.userId,
          email: data.email,
          nickName: data.nickName,
        }
        this.accessToken = data.jwtResponse?.accessToken || null
        this.refreshToken = data.jwtResponse?.refreshToken || null

        // 저장
        localStorage.setItem('user', JSON.stringify(this.user))
        if (this.accessToken) {
          localStorage.setItem('accessToken', this.accessToken)
        }
        if (this.refreshToken) {
          localStorage.setItem('refreshToken', this.refreshToken)
        }

        this.closeAuthModal()
        this.fetchMyPage(false)
      } catch (err) {
        this.authError = this.getSafeErrorMessage(
          err,
          '로그인에 실패했습니다. 이메일과 비밀번호를 확인해 주세요.',
        )
        console.error('Login error:', err)
      } finally {
        this.authLoading = false
      }
    },

    async register() {
      this.authLoading = true
      this.authError = ''
      try {
        await axios.post(`${API_BASE_URL}/auth/register`, {
          email: this.registerForm.email,
          nickName: this.registerForm.nickname,
          password: this.registerForm.password,
        })

        // 회원가입 후 바로 로그인 탭으로 전환
        this.authMode = 'login'
        this.loginForm.email = this.registerForm.email
        this.loginForm.password = ''
      } catch (err) {
        this.authError = this.getSafeErrorMessage(
          err,
          '회원가입에 실패했습니다. 입력 정보를 확인해 주세요.',
        )
        console.error('Register error:', err)
      } finally {
        this.authLoading = false
      }
    },

    async logout() {
      try {
        if (this.accessToken) {
          await axios.post(`${API_BASE_URL}/auth/logout`)
        }
      } catch (e) {
        console.warn('Logout error (ignored):', e)
      } finally {
        this.user = null
        this.accessToken = null
        this.refreshToken = null
        localStorage.removeItem('user')
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        this.myPage = null
        this.selectedUrlDetail = null
      }
    },

    openShorten() {
      this.selectedUrlDetail = false
      this.activeTab = 'shorten'
    },

    // ===== 마이페이지 =====
    async openMyPage() {
      this.activeTab = 'mypage'

      if (this.isLoggedIn && !this.myPage) {
        await this.fetchMyPage()
      }
    },

    async fetchMyPage(showLoading = true) {
      if (!this.isLoggedIn) {
        this.myPageError = ''
        this.myPage = null
        return
      }
      if (showLoading) {
        this.myPageLoading = true
      }
      this.myPageError = ''
      try {
        const res = await axios.get(`${API_BASE_URL}/me`)
        this.myPage = res.data
      } catch (err) {
        this.myPageError = this.getSafeErrorMessage(
          err,
          '마이페이지 정보를 불러오는 데 실패했습니다.',
        )
        console.error('MyPage error:', err)
      } finally {
        this.myPageLoading = false
      }
    },

    clickUrl(url) {
      navigator.clipboard.writeText(API_BASE_URL + '/' + url.shortenedUrl)
    },

    async showUrlDetail(url) {
      if (!this.isLoggedIn) return
      this.selectedUrlDetail = null
      // 기존 차트 인스턴스 제거
      if (this.chartInstance) {
        this.chartInstance.destroy()
        this.chartInstance = null
      }
      try {
        const res = await axios.get(`${API_BASE_URL}/urls/${url.id}`)
        this.selectedUrlDetail = res.data
        // 차트 생성은 nextTick에서 수행
        this.$nextTick(() => {
          if (this.selectedUrlDetail?.clickEventList?.length > 0) {
            this.createChart()
          }
        })
      } catch (err) {
        console.error('URL detail error:', err)
        this.myPageError = this.getSafeErrorMessage(err, 'URL 통계를 불러오는 데 실패했습니다.')
      }
    },

    closeUrlDetail() {
      if (this.chartInstance) {
        this.chartInstance.destroy()
        this.chartInstance = null
      }
      this.selectedUrlDetail = null
    },

    async deleteUrl(url) {
      if (!this.isLoggedIn) return
      if (!confirm('이 URL을 정말 삭제하시겠습니까?')) return

      try {
        await axios.delete(`${API_BASE_URL}/urls/${url.id}`)
        // 목록에서 제거
        if (this.myPage && this.myPage.urls) {
          this.myPage.urls = this.myPage.urls.filter((u) => u.id !== url.id)
        }
        if (this.selectedUrlDetail && this.selectedUrlDetail.id === url.id) {
          this.closeUrlDetail()
        }
      } catch (err) {
        console.error('Delete URL error:', err)
        this.myPageError = this.getSafeErrorMessage(err, 'URL 삭제에 실패했습니다.')
      }
    },

    // ===== 유틸 =====
    formatDateTime(value) {
      if (!value) return '-'
      try {
        const d = new Date(value)
        // 로컬 포맷 (간단하게)
        return d.toLocaleString()
      } catch {
        return value
      }
    },

    createChart() {
      if (!this.$refs.chartCanvas || !this.selectedUrlDetail?.clickEventList) return

      // 기존 차트 제거
      if (this.chartInstance) {
        this.chartInstance.destroy()
      }

      const clickEvents = this.selectedUrlDetail.clickEventList

      // 시간대별로 그룹화 (시간 단위)
      const timeMap = new Map()

      clickEvents.forEach((event) => {
        const date = new Date(event.clickedAt)
        // 일 단위로 그룹화 (YYYY-MM-DD)
        const timeKey = new Date(date.getFullYear(), date.getMonth(), date.getDate()).getTime()
        timeMap.set(timeKey, (timeMap.get(timeKey) || 0) + 1)
      })

      // 최소/최대 날짜 구하기
      const times = Array.from(timeMap.keys())
      if (times.length === 0) return

      const minTime = Math.min(...times)
      const maxTime = Math.max(...times)

      const labels = []
      const data = []

      // 빈 날짜 채우기 (시작일 ~ 종료일)
      const currentDate = new Date(minTime)
      const endDate = new Date(maxTime)

      while (currentDate <= endDate) {
        const timeKey = currentDate.getTime()
        labels.push(
          currentDate.toLocaleString('ko-KR', {
            month: 'short',
            day: 'numeric',
          }),
        )
        data.push(timeMap.get(timeKey) || 0)
        currentDate.setDate(currentDate.getDate() + 1)
      }

      const ctx = this.$refs.chartCanvas.getContext('2d')
      this.chartInstance = new Chart(ctx, {
        type: 'line',
        data: {
          labels: labels,
          datasets: [
            {
              label: '접속량',
              data: data,
              borderColor: '#818cf8',
              backgroundColor: 'rgba(129, 140, 248, 0.1)',
              borderWidth: 2,
              fill: true,
              tension: 0.4,
              pointRadius: 3,
              pointHoverRadius: 5,
              pointBackgroundColor: '#818cf8',
              pointBorderColor: '#ffffff',
              pointBorderWidth: 1,
            },
          ],
        },
        options: {
          responsive: true,
          maintainAspectRatio: false,
          plugins: {
            legend: {
              display: false,
            },
            tooltip: {
              backgroundColor: 'rgba(255, 255, 255, 0.95)',
              titleColor: '#1f2937',
              bodyColor: '#374151',
              borderColor: 'rgba(0, 0, 0, 0.1)',
              borderWidth: 1,
              padding: 12,
              displayColors: false,
            },
            zoom: {
              limits: {
                y: { min: 0 },
              },
              zoom: {
                wheel: {
                  enabled: true,
                  speed: 0.1,
                },
                pinch: {
                  enabled: true,
                },
                mode: 'xy',
              },
              pan: {
                enabled: true,
                mode: 'xy',
              },
            },
          },
          scales: {
            x: {
              title: {
                display: true,
                text: '날짜',
                color: '#9ca3af',
                font: {
                  size: 12,
                },
              },
              ticks: {
                color: '#9ca3af',
                maxRotation: 45,
                minRotation: 45,
              },
              grid: {
                color: 'rgba(148, 163, 184, 0.2)',
              },
            },
            y: {
              title: {
                display: true,
                text: '접속량',
                color: '#9ca3af',
                font: {
                  size: 12,
                },
              },
              ticks: {
                color: '#9ca3af',
                stepSize: 1,
                beginAtZero: true,
                precision: 0,
              },
              grid: {
                color: 'rgba(148, 163, 184, 0.2)',
              },
            },
          },
        },
      })
    },
  },
  beforeUnmount() {
    // 컴포넌트가 언마운트될 때 차트 인스턴스 제거
    if (this.chartInstance) {
      this.chartInstance.destroy()
      this.chartInstance = null
    }
  },
}
</script>

<style scoped>
.app-container {
  height: 100vh; /* 전체 높이를 화면에 고정 */
  display: flex;
  flex-direction: column;
  background: linear-gradient(180deg, #eef2f6 0%, #e8edf3 50%, #e5ebf2 100%);
  overflow: hidden;
}

.app-header {
  position: sticky;
  top: 0;
  z-index: 10;
  background: #ffffff;
  backdrop-filter: blur(18px);
  border-bottom: 1px solid rgba(0, 0, 0, 0.1);
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.header-inner {
  max-width: 1120px;
  margin: 0 auto;
  padding: 0.9rem 1.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
}

.brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  cursor: pointer;
}

.brand-icon {
  font-size: 1.6rem;
}

.brand-text {
  display: flex;
  flex-direction: column;
}

.brand-title {
  color: #1f2937;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.brand-subtitle {
  color: #6b7280;
  font-size: 0.8rem;
}

.nav {
  display: flex;
  gap: 0.5rem;
}

.nav-item {
  border: none;
  background: transparent;
  color: #6b7280;
  padding: 0.45rem 0.9rem;
  border-radius: 999px;
  font-size: 0.9rem;
  cursor: pointer;
  transition:
    background 0.2s ease,
    color 0.2s ease,
    transform 0.1s ease;
}

.nav-item:hover {
  background: rgba(0, 0, 0, 0.05);
  transform: translateY(-1px);
}

.nav-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #ffffff;
}

.auth-area {
  display: flex;
  align-items: center;
}

.user-chip {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.35rem 0.75rem;
  border-radius: 999px;
  color: #111827;
  font-size: 0.85rem;
}

.user-email {
  max-width: 180px;
  text-overflow: ellipsis;
  white-space: nowrap;
  overflow: hidden;
}

.btn-ghost {
  border-radius: 999px;
  padding: 0.4rem 0.9rem;
  border: 1px solid rgba(0, 0, 0, 0.2);
  background: transparent;
  color: #111827;
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-ghost.small {
  padding: 0.25rem 0.6rem;
  font-size: 0.78rem;
}

.btn-ghost:hover {
  background: rgba(0, 0, 0, 0.05);
}

.app-main {
  flex: 1;
  padding: 2.5rem 1.5rem 2rem;
  display: flex;
  justify-content: center;
  overflow: hidden; /* 메인 영역 밖으로 스크롤 생기는 것 방지 */
}

.content-grid {
  display: flex;
  width: 100%;
  max-width: 1120px;
  height: 100%; /* 높이를 꽉 채움 */
  gap: 20px;
  position: relative;
}

.card {
  background: #ffffff;
  border-radius: 18px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border: 1px solid rgba(0, 0, 0, 0.1);
  color: #1f2937;
}

.main-card {
  flex: 1.5;
  z-index: 2; /* 사이드 카드보다 위에 위치 */
  background: #ffffff;
  height: 100%; /* 높이 고정 */
  overflow-y: auto; /* 내부 스크롤 */
  transition: flex 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

.side-card {
  flex: 0 0 320px;
  z-index: 1; /* 메인 카드 뒤로 배치 */
  min-width: 320px;
  background: #ffffff;
  transform-origin: left center;
  height: 100%; /* 높이 고정 */
  overflow-y: auto; /* 내부 스크롤 */
  /* flex 전환은 main-card의 자체 규칙과 transition 컴포넌트가 각각 담당하므로, 기본 스타일에서 제거합니다. */
}
.side-card .card-body {
  display: flex;
  flex-direction: column;
  min-height: 100%; /* 내용이 많으면 늘어나도록 수정 */
  gap: 0.75rem;
  color: #111827;
  min-width: 260px;
}

.content-grid.mypage-detail-view .main-card {
  flex: 0 0 400px;
}

.content-grid.mypage-detail-view .side-card {
  flex: 1 1 0;
}

.card-body {
  padding: 1.75rem 1.9rem 1.9rem;
}

.card-title {
  font-size: 1.4rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.4rem;
}

.card-description {
  font-size: 0.9rem;
  color: #6b7280;
  margin-bottom: 1.75rem;
}

.card-header-row {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 1rem;
}

.url-form {
  margin-bottom: 1.5rem;
}

.field-label {
  display: block;
  font-size: 0.85rem;
  color: #6b7280;
  margin-bottom: 0.4rem;
}

.input-group {
  display: flex;
  gap: 0.75rem;
}

.url-input {
  flex: 1;
  padding: 0.9rem 1.2rem;
  border: 1px solid rgba(0, 0, 0, 0.2);
  border-radius: 10px;
  font-size: 0.95rem;
  background: #ffffff;
  color: #1f2937;
  outline: none;
  transition: all 0.2s ease;
}

.url-input::placeholder {
  color: rgba(148, 163, 184, 0.8);
}

.url-input:focus {
  border-color: #818cf8;
  box-shadow: 0 0 0 1px rgba(129, 140, 248, 0.7);
}

.url-input:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.advanced-options {
  margin-top: 1rem;
}

.checkbox {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  font-size: 0.85rem;
  color: #6b7280;
  cursor: pointer;
}

.checkbox.disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.checkbox input {
  accent-color: #6366f1;
}

.checkbox input:disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.text-muted {
  color: #6b7280;
}

.custom-input-group {
  margin-top: 0.75rem;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.2);
  border-radius: 10px;
  padding: 0.55rem 0.8rem;
}

.prefix {
  font-size: 0.8rem;
  color: #6b7280;
  white-space: nowrap;
}

.custom-input {
  flex: 1;
  border: none;
  background: transparent;
  color: #1f2937;
  font-size: 0.9rem;
  outline: none;
}

.btn-submit {
  padding: 0.9rem 1.7rem;
  background: linear-gradient(135deg, #4f46e5 0%, #8b5cf6 50%, #ec4899 100%);
  color: white;
  border: none;
  border-radius: 999px;
  font-size: 0.95rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.25s ease;
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  white-space: nowrap;
  box-shadow: 0 12px 25px rgba(79, 70, 229, 0.45);
}

.btn-submit:hover:not(:disabled) {
  transform: translateY(-1px);
  box-shadow: 0 16px 35px rgba(79, 70, 229, 0.6);
}

.btn-submit:disabled {
  opacity: 0.65;
  cursor: not-allowed;
  box-shadow: none;
}

.btn-submit.full {
  width: 100%;
  justify-content: center;
}

.btn-outline {
  border-radius: 999px;
  padding: 0.35rem 0.85rem;
  border: 1px solid rgba(0, 0, 0, 0.2);
  background: transparent;
  color: #1f2937;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-outline.small {
  padding: 0.25rem 0.7rem;
}

.btn-outline:hover {
  background: rgba(0, 0, 0, 0.05);
}

.btn-danger {
  border-radius: 999px;
  padding: 0.35rem 0.85rem;
  border: 1px solid #ef4444;
  background: #fee2e2;
  color: #b91c1c;
  font-size: 0.8rem;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-danger.small {
  padding: 0.25rem 0.7rem;
}

.btn-danger:hover {
  background: #fecdd3;
}

.spinner {
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

.alert {
  padding: 0.85rem 0.9rem;
  border-radius: 10px;
  margin: 0.75rem 0 0;
  display: flex;
  align-items: center;
  gap: 0.6rem;
  font-size: 0.85rem;
}

.alert-error {
  background-color: rgba(127, 29, 29, 0.95);
  color: #fee2e2;
  border: 1px solid rgba(248, 113, 113, 0.8);
}

.alert-icon {
  font-size: 1.1rem;
}

.result {
  margin-top: 2rem;
  padding-top: 2rem;
  border-top: 1px solid rgba(148, 163, 184, 0.4);
}

.result-header h3 {
  font-size: 1.05rem;
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 0.25rem;
}

.result-sub {
  font-size: 0.85rem;
  color: #6b7280;
}

.result-content {
  margin-top: 1.25rem;
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
}

.shortened-url-container {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  background: #f9fafb;
  padding: 0.85rem 0.95rem;
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.shortened-url {
  flex: 1;
  color: #4f46e5;
  text-decoration: none;
  font-weight: 500;
  word-break: break-all;
  font-size: 0.9rem;
}

.shortened-url:hover {
  color: #6366f1;
  text-decoration: underline;
}

.btn-copy {
  padding: 0.55rem 1.15rem;
  background: #10b981;
  color: white;
  border: none;
  border-radius: 999px;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  white-space: nowrap;
}

.btn-copy:hover {
  background: #059669;
  transform: translateY(-1px);
}

.btn-copy.copied {
  background: #14b8a6;
}

.qr-container {
  display: flex;
  justify-content: center;
  padding: 0.9rem;
  background: #f9fafb;
  border-radius: 10px;
  border: 1px dashed rgba(0, 0, 0, 0.15);
}

.qr-code {
  max-width: 180px;
  height: auto;
  border-radius: 10px;
}

.side-title {
  font-size: 1.1rem;
  font-weight: 600;
  margin-bottom: 1rem;
  color: #1f2937;
}

.feature-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 1rem;
  font-size: 0.88rem;
}

.feature-list h3 {
  font-size: 0.95rem;
  font-weight: 600;
  margin-bottom: 0.25rem;
  color: #1f2937;
}

.feature-list p {
  color: #6b7280;
}

.profile-summary {
  margin: 1.5rem 0 1rem;
}

.profile-summary h3 {
  font-size: 1.1rem;
  margin-bottom: 0.1rem;
}

.profile-summary p {
  font-size: 0.9rem;
  color: #6b7280;
}

.profile-summary .muted {
  margin-top: 0.2rem;
}

.url-list {
  margin-top: 1rem;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
}

.url-item {
  display: flex;
  justify-content: space-between;
  gap: 0.75rem;
  padding: 0.8rem 0.95rem;
  border-radius: 10px;
  background: #f9fafb;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.url-main {
  flex: 1;
  min-width: 0;
}

.url-short {
  display: block;
  font-size: 0.9rem;
  font-weight: 500;
  color: #4f46e5;
  text-decoration: none;
  word-break: break-all;
}

.url-short:hover {
  color: #6366f1;
  text-decoration: underline;
}

.url-origin {
  margin-top: 0.15rem;
  font-size: 0.8rem;
  color: #6b7280;
  word-break: break-all;
}

.url-actions {
  display: flex;
  flex-direction: column;
  gap: 0.35rem;
}

.detail-panel {
  margin-top: 1.25rem;
  padding: 1rem;
  border-radius: 12px;
  background: rgba(15, 23, 42, 0.95);
  border: 1px solid rgba(129, 140, 248, 0.5);
}

.detail-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.6rem;
}

.detail-header h3 {
  font-size: 0.95rem;
}

.detail-body {
  font-size: 0.82rem;
  color: #374151;
  display: grid;
  grid-template-columns: minmax(0, 1fr);
  row-gap: 0.35rem;
}

.detail-label {
  font-size: 0.78rem;
  color: #374151;
  font-weight: bold;
}

.detail-value {
  margin-bottom: 0.35rem;
  color: #111827;
  word-break: break-all;
}

.chart-container {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid rgba(148, 163, 184, 0.3);
  width: 100%;
  max-width: 100%;
}

.chart-container canvas {
  max-height: 500px;
  width: 100% !important;
  height: 500px !important;
}

.chart-hint {
  margin-top: 0.5rem;
  font-size: 0.75rem;
  color: #6b7280;
  text-align: center;
}

.empty-state,
.loading-state {
  margin-top: 2rem;
  padding: 1.25rem;
  border-radius: 12px;
  background: #f9fafb;
  border: 1px dashed rgba(0, 0, 0, 0.15);
  text-align: center;
  font-size: 0.9rem;
  color: #6b7280;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.7rem;
}

.loading-state {
  flex-direction: row;
  justify-content: center;
}

/* 모달 */
.modal-backdrop {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 40;
}

.modal {
  width: 100%;
  max-width: 420px;
  background: #ffffff;
  border-radius: 18px;
  border: 1px solid rgba(0, 0, 0, 0.1);
  padding: 1.4rem 1.6rem 1.6rem;
  color: #1f2937;
  box-shadow: 0 18px 45px rgba(0, 0, 0, 0.2);
}

.modal-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.8rem;
}

.modal-header h2 {
  font-size: 1.1rem;
  color: #1f2937;
}

.modal-tabs {
  display: flex;
  gap: 0.25rem;
  margin-bottom: 0.8rem;
  padding: 0.25rem;
  border-radius: 999px;
  background: #f3f4f6;
  border: 1px solid rgba(0, 0, 0, 0.1);
}

.modal-tab {
  flex: 1;
  border: none;
  background: transparent;
  color: #6b7280;
  font-size: 0.85rem;
  padding: 0.4rem 0;
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.modal-tab.active {
  background: linear-gradient(135deg, #4f46e5 0%, #8b5cf6 100%);
  color: #ffffff;
}

.auth-form {
  margin-top: 0.4rem;
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.text-input {
  width: 100%;
  border-radius: 10px;
  border: 1px solid rgba(0, 0, 0, 0.2);
  padding: 0.6rem 0.85rem;
  font-size: 0.9rem;
  background: #ffffff;
  color: #1f2937;
  outline: none;
}

.text-input::placeholder {
  color: rgba(148, 163, 184, 0.8);
}

.text-input:focus {
  border-color: #818cf8;
  box-shadow: 0 0 0 1px rgba(129, 140, 248, 0.7);
}

.app-footer {
  padding: 1.1rem 1.5rem 1.4rem;
  text-align: center;
  color: #6b7280;
  font-size: 0.8rem;
}

.app-footer p {
  margin: 0;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-up-enter-active {
  transition: all 0.25s ease;
}

.slide-up-leave-active {
  transition: all 0.2s ease;
}

.slide-up-enter-from {
  opacity: 0;
  transform: translateY(12px);
}

.slide-up-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

/* 반응형 */
@media (max-width: 900px) {
  .content-grid {
    grid-template-columns: minmax(0, 1.3fr);
  }

  .side-card {
    display: none;
  }
}

@media (max-width: 640px) {
  .header-inner {
    flex-wrap: wrap;
    justify-content: center;
  }

  .nav {
    order: 3;
  }

  .app-main {
    padding: 1.8rem 1rem 1.5rem;
  }

  .card-body {
    padding: 1.5rem 1.4rem 1.6rem;
  }

  .input-group {
    flex-direction: column;
  }

  .btn-submit {
    width: 100%;
    justify-content: center;
  }

  .shortened-url-container {
    flex-direction: column;
    align-items: stretch;
  }

  .btn-copy {
    width: 100%;
    text-align: center;
  }

  /* 반응형 유지 */
  @media (max-width: 900px) {
    .content-grid {
      flex-direction: column;
    }
    .side-card {
      margin-left: 0 !important;
      transform: translateY(-20px);
    }
  }
}

/* 슬라이드 애니메이션 */
.slide-pane-enter-active,
.slide-pane-leave-active {
  transition:
    transform 0.5s cubic-bezier(0.4, 0, 0.2, 1),
    opacity 0.5s cubic-bezier(0.4, 0, 0.2, 1),
    margin-left 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 나타날 때: 왼쪽(메인카드 뒤)에서 오른쪽으로 밀려나옴 */
.slide-pane-enter-from {
  opacity: 0;
  transform: translateX(-50%) scale(0.95);
  margin-left: -340px; /* 고정된 너비(320px) + 부모의 gap(20px) */
}

/* 사라질 때: 다시 메인카드 뒤로 들어가며 투명해짐 */
.slide-pane-leave-to {
  opacity: 0;
  transform: translateX(-30%) scale(0.95);
  margin-left: -340px;
}

/* 마이페이지용 슬라이드 애니메이션 */
.slide-pane-flex-enter-active,
.slide-pane-flex-leave-active {
  transition:
    flex 0.5s cubic-bezier(0.4, 0, 0.2, 1),
    opacity 0.4s ease-in-out;
  overflow: hidden;
}

.slide-pane-flex-enter-from,
.slide-pane-flex-leave-to {
  flex: 0 0 0px;
  min-width: 0 !important; /* 애니메이션을 위해 일시적으로 min-width 무시 */
  opacity: 0;
  margin-left: -20px;
}

/* 메인 콘텐츠 페이드 전환 */
.content-fade-enter-active,
.content-fade-leave-active {
  transition: opacity 0.15s ease-in-out;
}

.content-fade-enter-from,
.content-fade-leave-to {
  opacity: 0;
}

/* 카드 내부 커스텀 스크롤바 */
.main-card::-webkit-scrollbar,
.side-card::-webkit-scrollbar {
  width: 6px;
}
.main-card::-webkit-scrollbar-track,
.side-card::-webkit-scrollbar-track {
  background: transparent;
}
.main-card::-webkit-scrollbar-thumb,
.side-card::-webkit-scrollbar-thumb {
  background-color: rgba(156, 163, 175, 0.5);
  border-radius: 4px;
}
</style>
