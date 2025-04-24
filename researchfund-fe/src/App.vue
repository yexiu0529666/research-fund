<template>
  <router-view v-slot="{ Component }">
    <component :is="Component" :key="$route.fullPath" />
  </router-view>
</template>

<script setup>
import { onMounted, watch } from 'vue'
import { useUserStore } from './store/modules/user'
import { useRoute } from 'vue-router'

const route = useRoute()
const userStore = useUserStore()

// 监听路由变化
watch(
  () => route.fullPath,
  () => {
    console.log('路由变化:', route.fullPath)
  }
)

onMounted(() => {
  const userStore = useUserStore()
  const token = localStorage.getItem('token')
  
  // 如果存在token但没有用户名，则获取用户信息
  if (token && !userStore.name) {
    userStore.getUserInfo().catch(err => {
      console.error('App初始化获取用户信息失败:', err)
    })
  }
})
</script>

<style>
html, body {
  height: 100%;
  margin: 0;
  padding: 0;
  font-family: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  height: 100%;
}

* {
  box-sizing: border-box;
}

/* 全局滚动条样式 */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background-color: #f5f5f5;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background-color: #c0c4cc;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background-color: #909399;
}

/* 全局过渡效果 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style> 