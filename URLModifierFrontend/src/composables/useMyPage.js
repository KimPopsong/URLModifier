import { ref } from 'vue'
import { api, getSafeErrorMessage } from '@/api/client'
import { useAuth } from '@/composables/useAuth'

const { isLoggedIn } = useAuth()

const myPage = ref(null)
const myPageLoading = ref(false)
const myPageError = ref('')
const selectedUrlDetail = ref(null)
const urlDetailLoading = ref(false)

// 버전 카운터: await 이후에도 최신 요청인지 확인해 경쟁 조건 방지
let reqVersion = 0

async function fetchMyPage(showLoading = true) {
  if (!isLoggedIn.value) {
    myPageError.value = ''
    myPage.value = null
    return
  }
  if (showLoading) {
    myPageLoading.value = true
  }
  myPageError.value = ''
  try {
    const res = await api.get('/me')
    myPage.value = res.data
  } catch (err) {
    myPageError.value = getSafeErrorMessage(err, '마이페이지 정보를 불러오는 데 실패했습니다.')
    console.error('MyPage error:', err)
  } finally {
    myPageLoading.value = false
  }
}

async function showUrlDetail(url) {
  if (!isLoggedIn.value) return

  reqVersion += 1
  const myVersion = reqVersion
  const panelWasOpen = !!selectedUrlDetail.value

  if (panelWasOpen) {
    urlDetailLoading.value = true
  } else {
    selectedUrlDetail.value = null
  }

  try {
    const res = await api.get(`/urls/${url.id}`)
    // 더 최신 요청이 생겼으면 이 결과는 버림
    if (myVersion !== reqVersion) return
    selectedUrlDetail.value = res.data
    urlDetailLoading.value = false
  } catch (err) {
    if (myVersion !== reqVersion) return
    urlDetailLoading.value = false
    console.error('URL detail error:', err)
    myPageError.value = getSafeErrorMessage(err, 'URL 통계를 불러오는 데 실패했습니다.')
  }
}

function closeUrlDetail() {
  selectedUrlDetail.value = null
  urlDetailLoading.value = false
}

async function deleteUrl(url) {
  if (!isLoggedIn.value) return
  if (!confirm('이 URL을 정말 삭제하시겠습니까?')) return

  try {
    await api.delete(`/urls/${url.id}`)
    if (myPage.value && myPage.value.urls) {
      myPage.value.urls = myPage.value.urls.filter((u) => u.id !== url.id)
    }
    if (selectedUrlDetail.value && selectedUrlDetail.value.id === url.id) {
      closeUrlDetail()
    }
  } catch (err) {
    console.error('Delete URL error:', err)
    myPageError.value = getSafeErrorMessage(err, 'URL 삭제에 실패했습니다.')
  }
}

function resetMyPage() {
  myPage.value = null
  closeUrlDetail()
}

export function useMyPage() {
  return {
    myPage,
    myPageLoading,
    myPageError,
    selectedUrlDetail,
    urlDetailLoading,
    fetchMyPage,
    showUrlDetail,
    closeUrlDetail,
    deleteUrl,
    resetMyPage,
  }
}
