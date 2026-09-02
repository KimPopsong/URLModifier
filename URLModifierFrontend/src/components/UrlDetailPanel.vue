<script setup>
import { nextTick, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { Chart, registerables } from 'chart.js'
import zoomPlugin from 'chartjs-plugin-zoom'
import { useMyPage } from '@/composables/useMyPage'
import { formatExpiry, formatDateTime } from '@/utils/format'
import TagChip from '@/components/ui/TagChip.vue'
import AppIcon from '@/components/ui/AppIcon.vue'
import SpinnerDot from '@/components/ui/SpinnerDot.vue'

Chart.register(...registerables)
Chart.register(zoomPlugin)

const { selectedUrlDetail, urlDetailLoading, closeUrlDetail } = useMyPage()

const chartCanvas = ref(null)
let chartInstance = null

function destroyChart() {
  if (chartInstance) {
    chartInstance.stop()
    chartInstance.destroy()
    chartInstance = null
  }
}

function createChart() {
  if (!chartCanvas.value) return

  const dailyClicks = selectedUrlDetail.value?.dailyClicks
  if (!dailyClicks) return

  // 'YYYY-MM-DD' 문자열 키를 그대로 사용 (Date 타임스탬프 변환 시
  // UTC 해석·DST 때문에 키가 어긋나 카운트가 0으로 표시될 수 있음)
  const dateKeys = Object.keys(dailyClicks).sort()
  if (dateKeys.length === 0) return

  const parseDate = (s) => {
    const [y, m, d] = s.split('-').map(Number)
    return new Date(y, m - 1, d) // 로컬 자정 기준
  }
  const toKey = (d) =>
    `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`

  const startDate = parseDate(dateKeys[0])
  const endDate = parseDate(dateKeys[dateKeys.length - 1])

  // 단일 날짜인 경우 앞뒤 하루씩 확장
  if (dateKeys.length === 1) {
    startDate.setDate(startDate.getDate() - 1)
    endDate.setDate(endDate.getDate() + 1)
  }

  const labels = []
  const data = []

  const currentDate = new Date(startDate)
  while (currentDate <= endDate) {
    labels.push(currentDate.toLocaleString('ko-KR', { month: 'short', day: 'numeric' }))
    data.push(dailyClicks[toKey(currentDate)] || 0)
    currentDate.setDate(currentDate.getDate() + 1)
  }

  destroyChart()

  const ctx = chartCanvas.value.getContext('2d')
  const mono = "'Geist Mono', ui-monospace, monospace"
  chartInstance = new Chart(ctx, {
    type: 'line',
    data: {
      labels,
      datasets: [
        {
          label: '접속량',
          data,
          borderColor: '#171717',
          backgroundColor: 'rgba(0, 0, 0, 0.05)',
          borderWidth: 2,
          fill: true,
          tension: 0.35,
          pointRadius: 3,
          pointHoverRadius: 5,
          pointBackgroundColor: '#171717',
          pointBorderColor: '#ffffff',
          pointBorderWidth: 1,
        },
      ],
    },
    options: {
      animation: false,
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { display: false },
        tooltip: {
          backgroundColor: '#171717',
          titleColor: '#ffffff',
          bodyColor: '#e5e5e5',
          borderColor: 'transparent',
          padding: 10,
          displayColors: false,
          titleFont: { family: mono },
          bodyFont: { family: mono },
        },
        zoom: {
          zoom: {
            wheel: { enabled: true, speed: 0.1 },
            pinch: { enabled: true },
            mode: 'x',
          },
          pan: { enabled: true, mode: 'x' },
        },
      },
      scales: {
        x: {
          title: { display: true, text: '날짜', color: '#9b9b9b', font: { size: 12 } },
          ticks: { color: '#9b9b9b', maxRotation: 45, minRotation: 45, font: { family: mono } },
          grid: { color: 'rgba(0, 0, 0, 0.06)' },
        },
        y: {
          title: { display: true, text: '접속량', color: '#9b9b9b', font: { size: 12 } },
          ticks: {
            color: '#9b9b9b',
            stepSize: 1,
            beginAtZero: true,
            precision: 0,
            font: { family: mono },
          },
          grid: { color: 'rgba(0, 0, 0, 0.06)' },
        },
      },
    },
  })
}

function scheduleChart() {
  // opacity 페이드는 레이아웃을 지연시키지 않지만, 한 프레임 뒤에 그려
  // 캔버스 컨테이너 폭이 확정된 뒤 렌더링되도록 보장
  nextTick(() => {
    requestAnimationFrame(() => createChart())
  })
}

onMounted(() => {
  if (selectedUrlDetail.value) scheduleChart()
})

watch(
  selectedUrlDetail,
  (val) => {
    if (val) scheduleChart()
    else destroyChart()
  },
)

onBeforeUnmount(destroyChart)

const hasChart = (d) => d?.dailyClicks && Object.keys(d.dailyClicks).length > 0
</script>

<template>
  <div class="pane">
    <div class="head">
      <h2 class="title">URL 통계</h2>
      <button class="close" @click="closeUrlDetail">
        <AppIcon name="close" :size="16" />
      </button>
    </div>

    <div class="body" v-if="selectedUrlDetail">
      <transition name="fade">
        <div v-if="urlDetailLoading" class="overlay">
          <SpinnerDot :size="20" />
        </div>
      </transition>

      <dl class="fields">
        <div class="field">
          <dt>원본 URL</dt>
          <dd class="mono">{{ selectedUrlDetail.originURL }}</dd>
        </div>
        <div class="field">
          <dt>단축 URL</dt>
          <dd class="mono">{{ selectedUrlDetail.shortenedURL }}</dd>
        </div>
        <div class="field">
          <dt>생성 일시</dt>
          <dd class="mono">{{ formatDateTime(selectedUrlDetail.createdAt) }}</dd>
        </div>
        <div class="field">
          <dt>총 클릭 수</dt>
          <dd class="mono big">{{ selectedUrlDetail.totalClicks || 0 }}회</dd>
        </div>
        <div v-if="selectedUrlDetail.expiresAt || selectedUrlDetail.maxClicks" class="field">
          <dt>만료 조건</dt>
          <dd class="chips">
            <TagChip v-if="selectedUrlDetail.expiresAt">
              <AppIcon name="clock" :size="13" />{{ formatExpiry(selectedUrlDetail.expiresAt) }}
            </TagChip>
            <TagChip v-if="selectedUrlDetail.maxClicks">
              <AppIcon name="cursor" :size="13" />최대 {{ selectedUrlDetail.maxClicks }}회
            </TagChip>
          </dd>
        </div>
      </dl>

      <div v-if="hasChart(selectedUrlDetail)" class="chart-wrap">
        <div class="chart"><canvas ref="chartCanvas"></canvas></div>
        <p class="hint">마우스 휠로 확대/축소 가능</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.pane {
  padding: 1.9rem 1.7rem;
}
.head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.2rem;
}
.title {
  font-size: 1.05rem;
  font-weight: 600;
  color: var(--text);
}
.close {
  border: none;
  background: none;
  color: var(--text-2);
  cursor: pointer;
  display: inline-flex;
  padding: 0.25rem;
  border-radius: var(--radius-sm);
}
.close:hover {
  background: var(--surface-2);
  color: var(--text);
}

.body {
  position: relative;
}
.overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.7);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1;
  color: var(--text);
  border-radius: var(--radius-sm);
}

.fields {
  display: flex;
  flex-direction: column;
  gap: 1rem;
}
.field dt {
  font-size: 0.75rem;
  color: var(--text-3);
  margin-bottom: 0.25rem;
}
.field dd {
  font-size: 0.9rem;
  color: var(--text);
  word-break: break-all;
}
.field dd.big {
  font-size: 1.15rem;
  font-weight: 600;
}
.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 0.3rem;
}

.chart-wrap {
  margin-top: 1.5rem;
  padding-top: 1.5rem;
  border-top: 1px solid var(--border);
}
.chart {
  width: 100%;
  height: 420px;
}
.chart canvas {
  width: 100% !important;
  height: 420px !important;
}
.hint {
  margin-top: 0.5rem;
  font-size: 0.74rem;
  color: var(--text-3);
  text-align: center;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
