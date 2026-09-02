import axios from 'axios'

export const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080'

export const api = axios.create({ baseURL: API_BASE_URL })

// 동시에 여러 요청이 401을 받아도 refresh는 한 번만 수행하고 결과를 공유
let refreshPromise = null

export function clearAuthStorage() {
  localStorage.removeItem('user')
  localStorage.removeItem('accessToken')
  localStorage.removeItem('refreshToken')
}

api.interceptors.request.use(
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

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const status = error?.response?.status
    const originalConfig = error?.config

    if (
      status !== 401 ||
      !originalConfig ||
      originalConfig._isRetry ||
      originalConfig.url?.includes('/auth/login')
    ) {
      return Promise.reject(error)
    }

    const storedRefresh = localStorage.getItem('refreshToken')

    if (!storedRefresh) {
      clearAuthStorage()
      window.location.reload()
      return Promise.reject(error)
    }

    try {
      // 이미 진행 중인 refresh가 있으면 그 결과를 기다렸다가 같이 사용
      if (!refreshPromise) {
        refreshPromise = api
          .post('/auth/refresh', { refreshToken: storedRefresh }, { _isRetry: true })
          .finally(() => {
            refreshPromise = null
          })
      }
      const res = await refreshPromise

      localStorage.setItem('accessToken', res.data.accessToken)
      localStorage.setItem('refreshToken', res.data.refreshToken)

      originalConfig.headers = originalConfig.headers || {}
      originalConfig.headers['Authorization'] = `Bearer ${res.data.accessToken}`
      originalConfig._isRetry = true
      return api(originalConfig)
    } catch {
      clearAuthStorage()
      window.location.reload()
      return Promise.reject(error)
    }
  },
)

// BE의 ErrorResponse 구조: { errorCode, message }
export function getSafeErrorMessage(
  err,
  defaultMessage = '요청 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.',
) {
  const errorData = err?.response?.data

  if (errorData?.message) {
    // BE에서 이미 안전한 메시지를 보내므로 그대로 사용
    return errorData.message
  }

  if (errorData?.errorCode) {
    // errorCode만 있고 메시지가 없는 경우 기본 메시지 사용
    return defaultMessage
  }

  // 예상치 못한 에러 메시지가 오는 경우 기본 메시지 사용 (내부 정보 노출 방지)
  return defaultMessage
}
