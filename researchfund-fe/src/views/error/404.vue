<template>
  <div class="not-found">
    <div class="not-found-content">
      <img src="@/assets/404.svg" alt="404" class="error-image">
      <h1 class="error-title">404</h1>
      <h2 class="error-message">抱歉，您访问的页面不存在</h2>
      <div class="error-actions">
        <el-button type="primary" @click="goBack">返回上一页</el-button>
        <el-button @click="goHome">返回首页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// 返回上一页
const goBack = () => {
  router.go(-1)
}

// 返回首页
const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}

.not-found-content {
  text-align: center;
  padding: 40px;
}

.error-image {
  max-width: 300px;
  margin-bottom: 20px;
}

.error-title {
  font-size: 72px;
  color: #409EFF;
  margin: 0 0 20px 0;
  line-height: 1;
}

.error-message {
  font-size: 24px;
  color: #606266;
  margin: 0 0 30px 0;
  font-weight: normal;
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 15px;
}
</style> 