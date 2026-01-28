import { createApp } from 'vue'
import { createPinia } from 'pinia'
import '@fortawesome/fontawesome-free/css/all.css'
import App from './App.vue'
import router from './Router'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'; // 引入中文语言包
// @ts-ignore
import vue3videoPlay from "vue3-video-play"; // 视频播放器
import piniaPluginPersistedstate from 'pinia-plugin-persistedstate'; // Pinia持久化
import "vue3-video-play/dist/style.css"; // 引入css
import 'element-plus/theme-chalk/dark/css-vars.css'

import { useThemeStore } from './Stores/ThemeStore';
import '@/Styles/global.css'; // 引入全局样式


const app = createApp(App)

const pinia = createPinia()
pinia.use(piniaPluginPersistedstate)
app.use(pinia)
app.use(router)
app.use(vue3videoPlay)
app.use(ElementPlus, {
    locale: zhCn
})

app.mount('#app')


