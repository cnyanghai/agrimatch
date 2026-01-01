import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import { createPinia } from 'pinia'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
dayjs.locale('zh-cn')
app.use(ElementPlus, { locale: zhCn })
app.mount('#app')
