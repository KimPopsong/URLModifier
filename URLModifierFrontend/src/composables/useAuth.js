import { computed, ref } from 'vue'
import { api, API_BASE_URL, clearAuthStorage } from '@/api/client'

// 모듈 스코프 = 앱 전체가 공유하는 단일 인증 상태
const user = ref(null)
const accessToken = ref(null)
const refreshToken = ref(null)

const isLoggedIn = computed(() => !!accessToken.value)

function isTokenExpired(token) {
  try {
    const payload = JSON.parse(atob(token.split('.')[1]))
    return payload.exp * 1000 < Date.now()
  } catch {
    return true
  }
}

function persistTokens() {
  if (accessToken.value) localStorage.setItem('accessToken', accessToken.value)
  if (refreshToken.value) localStorage.setItem('refreshToken', refreshToken.value)
  if (user.value) localStorage.setItem('user', JSON.stringify(user.value))
}

function resetAuth() {
  user.value = null
  accessToken.value = null
  refreshToken.value = null
  clearAuthStorage()
}

// 앱 부팅 시 저장된 토큰 복원 (필요 시 refresh)
async function initAuth() {
  const storedAccess = localStorage.getItem('accessToken')
  const storedRefresh = localStorage.getItem('refreshToken')
  const storedUser = localStorage.getItem('user')

  if (!storedAccess || !storedUser) return

  if (!isTokenExpired(storedAccess)) {
    accessToken.value = storedAccess
    refreshToken.value = storedRefresh
    user.value = JSON.parse(storedUser)
    return
  }

  if (!storedRefresh || isTokenExpired(storedRefresh)) {
    clearAuthStorage()
    return
  }

  try {
    const res = await api.post('/auth/refresh', { refreshToken: storedRefresh })
    accessToken.value = res.data.accessToken
    refreshToken.value = res.data.refreshToken
    user.value = JSON.parse(storedUser)
    localStorage.setItem('accessToken', accessToken.value)
    localStorage.setItem('refreshToken', refreshToken.value)
  } catch {
    clearAuthStorage()
  }
}

async function login({ email, password }) {
  const res = await api.post('/auth/login', { email, password })
  const data = res.data

  user.value = {
    id: data.userId,
    email: data.email,
    nickName: data.nickName,
  }
  accessToken.value = data.jwtResponse?.accessToken || null
  refreshToken.value = data.jwtResponse?.refreshToken || null

  persistTokens()
  return user.value
}

async function register({ email, nickname, password }) {
  await api.post('/auth/register', { email, nickName: nickname, password })
}

async function logout() {
  try {
    if (accessToken.value) {
      await api.post('/auth/logout')
    }
  } catch (e) {
    console.warn('Logout error (ignored):', e)
  } finally {
    resetAuth()
  }
}

async function withdraw(password) {
  await api.delete('/auth/withdraw', { data: { password } })
  resetAuth()
}

export function useAuth() {
  return {
    user,
    accessToken,
    refreshToken,
    isLoggedIn,
    backendBaseUrl: API_BASE_URL,
    initAuth,
    login,
    register,
    logout,
    withdraw,
    resetAuth,
  }
}
