<template>
  <el-breadcrumb class="app-breadcrumb" separator="/">
    <transition-group name="breadcrumb">
      <el-breadcrumb-item v-for="(item, index) in breadcrumbs" :key="item.path">
        <span
          v-if="index === breadcrumbs.length - 1"
          class="no-redirect"
        >{{ item.meta.title }}</span>
        <a v-else @click.prevent="handleLink(item)">{{ item.meta.title }}</a>
      </el-breadcrumb-item>
    </transition-group>
  </el-breadcrumb>
</template>

<script setup>
import { ref, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const breadcrumbs = ref([])

// 生成面包屑导航
const getBreadcrumb = () => {
  let matched = route.matched.filter(item => item.meta && item.meta.title)
  
  // 如果第一个不是首页，则加入首页作为第一个面包屑
  const first = matched[0]
  if (first && first.path !== '/dashboard') {
    matched = [{ path: '/dashboard', meta: { title: '首页' } }].concat(matched)
  }
  
  breadcrumbs.value = matched
}

// 处理导航链接点击
const handleLink = (item) => {
  const { path } = item
  router.push(path)
}

// 监听路由变化，更新面包屑
watch(
  () => route.path,
  () => getBreadcrumb(),
  { immediate: true }
)
</script>

<style scoped>
.app-breadcrumb {
  display: inline-block;
  font-size: 14px;
  line-height: 50px;
  margin-left: 8px;
}

.app-breadcrumb .no-redirect {
  color: #97a8be;
  cursor: text;
}

/* 面包屑过渡动画 */
.breadcrumb-enter-active,
.breadcrumb-leave-active {
  transition: all 0.5s;
}

.breadcrumb-enter-from,
.breadcrumb-leave-to {
  opacity: 0;
  transform: translateX(20px);
}

.breadcrumb-leave-active {
  position: absolute;
}
</style> 