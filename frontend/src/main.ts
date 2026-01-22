import { createApp } from 'vue'
import './styles/design-system.css'
import './style.css'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
// 全局覆盖样式需放在第三方组件库样式之后，保证生效
import './assets/animations.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import dayjs from 'dayjs'
import 'dayjs/locale/zh-cn'
import { createPinia } from 'pinia'
import router from './router'
import { prefetchCommonRoutes } from './utils/prefetchRoutes'
import { ErrorHandler } from './utils/error-handler'
import lazyLoad from './directives/lazyLoad'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.directive('lazy', lazyLoad)
dayjs.locale('zh-cn')
app.use(ElementPlus, { locale: zhCn })

// 初始化全局错误处理器
ErrorHandler.init()

app.mount('#app')

// 首屏完成后，后台预加载高频路由页面（不影响主流程）
prefetchCommonRoutes()
