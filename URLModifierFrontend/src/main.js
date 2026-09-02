// 자체 호스팅 폰트 (CDN 미사용, Vite 번들에 포함)
import '@fontsource/geist-sans/400.css'
import '@fontsource/geist-sans/500.css'
import '@fontsource/geist-sans/600.css'
import '@fontsource/geist-sans/700.css'
import '@fontsource/geist-mono/400.css'
import '@fontsource/geist-mono/500.css'

import './assets/tokens.css'
import './assets/base.css'

import { createApp } from 'vue'
import App from './App.vue'

createApp(App).mount('#app')
