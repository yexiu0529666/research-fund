import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 导入Token过期管理模块
import { checkExistingToken } from './utils/token-expiry'

// 导入自定义样式
import './styles/index.scss'

// 创建应用实例
const app = createApp(App)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 使用插件
app.use(router)
app.use(createPinia())
app.use(ElementPlus, {
  locale: zhCn,
  size: 'default'
})

// 检查现有token并启动过期计时器
checkExistingToken()

// 挂载应用
app.mount('#app') 