<template>
  <div class="login-container">
    <div class="login-background">
      <div class="shape shape1"></div>
      <div class="shape shape2"></div>
      <div class="shape shape3"></div>
    </div>
    
    <el-card class="login-card">
      <div class="title-container">
        <h3 class="title">科研经费管理系统</h3>
        <p class="subtitle">账号登录</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        class="login-form"
        autocomplete="on"
      >
        <el-form-item prop="username">
          <el-input
            ref="usernameRef"
            v-model="loginForm.username"
            placeholder="请输入用户名"
            type="text"
            size="large"
            :prefix-icon="User"
            autocomplete="username"
          />
        </el-form-item>
        
        <el-form-item prop="password">
          <el-input
            ref="passwordRef"
            v-model="loginForm.password"
            :type="passwordVisible ? 'text' : 'password'"
            placeholder="请输入密码"
            size="large"
            autocomplete="current-password"
            :prefix-icon="Lock"
          >
            <template #suffix>
              <el-icon class="password-icon" @click="passwordVisible = !passwordVisible">
                <component :is="passwordVisible ? 'View' : 'Hide'" />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <div class="remember-container">
          <el-checkbox v-model="loginForm.rememberMe">记住我</el-checkbox>
          <el-button type="text" class="forgot-btn" @click="handleForgotPassword">忘记密码？</el-button>
        </div>
        
        <el-button
          :loading="loading"
          type="primary"
          class="login-button"
          @click="handleLogin"
        >
          登 录
        </el-button>
        
        
        <div class="register-container">
          <span>暂时没有账号？</span>
          <el-button type="text" class="register-btn" @click="handleRegister">去注册</el-button>
        </div>
      </el-form>
      
      <div class="login-footer">
        <p>© {{ currentYear }} 科研经费管理系统 - 专业的研究资金管理平台</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, View, Hide } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref(null)
const usernameRef = ref(null)
const passwordRef = ref(null)
const loading = ref(false)
const passwordVisible = ref(false)
const currentYear = computed(() => new Date().getFullYear())

// 登录表单
const loginForm = reactive({
  username: '',
  password: '',
  rememberMe: false
})

// 表单验证规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少为6位', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = () => {
  loginFormRef.value.validate(valid => {
    if (valid) {
      loading.value = true
      console.log('发送登录请求:', {
        username: loginForm.username,
        password: loginForm.password.length + '位字符'
      })
      userStore.login({
        username: loginForm.username,
        password: loginForm.password,
        rememberMe: loginForm.rememberMe
      }).then(() => {
        ElMessage({
          message: '登录成功',
          type: 'success',
          duration: 2000
        })
        router.push({ path: '/dashboard' })
      }).catch(error => {
        console.error('登录失败:', error)
        ElMessage.error('登录失败，请检查用户名和密码')
      }).finally(() => {
        loading.value = false
      })
    } else {
      return false
    }
  })
}

// 测试模式登录 (绕过后端验证，仅开发环境使用)
const handleTestLogin = () => {
  // 直接在localStorage中设置模拟的token
  localStorage.setItem('token', 'test-mode-token-for-development')
  
  // 设置用户状态
  userStore.$patch({
    token: 'test-mode-token-for-development',
    userId: 1,
    name: '测试管理员',
    roles: ['admin'],
    permissions: ['*:*:*']
  })
  
  ElMessage({
    message: '已使用测试账号登录（仅用于开发）',
    type: 'warning',
    duration: 3000
  })
  
  // 跳转到首页
  router.push({ path: '/dashboard' })
}

// 处理忘记密码
const handleForgotPassword = () => {
  ElMessage.info('请联系系统管理员重置密码')
}

// 处理注册
const handleRegister = () => {
  router.push('/register')
}

// 如果存在本地保存的用户信息，尝试自动填充
onMounted(() => {
  const rememberedUser = localStorage.getItem('rememberedUser')
  if (rememberedUser) {
    try {
      const userInfo = JSON.parse(rememberedUser)
      loginForm.username = userInfo.username || ''
      loginForm.rememberMe = true
    } catch (e) {
      // 忽略解析错误
    }
  }
  // 自动聚焦用户名输入框
  usernameRef.value?.focus()
})
</script>

<style scoped>
.login-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.login-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  z-index: 0;
}

.shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  animation: float 6s ease-in-out infinite;
}

.shape1 {
  width: 500px;
  height: 500px;
  top: -150px;
  right: -100px;
  animation-delay: 0s;
}

.shape2 {
  width: 300px;
  height: 300px;
  bottom: -80px;
  left: -80px;
  animation-delay: 2s;
}

.shape3 {
  width: 200px;
  height: 200px;
  bottom: 50%;
  right: 30%;
  animation-delay: 4s;
}

@keyframes float {
  0% {
    transform: translatey(0px) rotate(0deg);
  }
  50% {
    transform: translatey(-20px) rotate(5deg);
  }
  100% {
    transform: translatey(0px) rotate(0deg);
  }
}

.login-card {
  width: 420px;
  padding: 35px 35px 15px;
  border-radius: 16px;
  box-shadow: 0 10px 50px rgba(0, 0, 0, 0.1);
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 2;
  transition: all 0.3s ease;
}

.login-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 60px rgba(0, 0, 0, 0.15);
}

.title-container {
  text-align: center;
  margin-bottom: 30px;
}

.logo-box {
  display: flex;
  justify-content: center;
  margin-bottom: 15px;
}

.logo {
  height: 60px;
  width: auto;
}

.title {
  font-size: 24px;
  color: #303133;
  margin: 0 0 5px;
  font-weight: bold;
  letter-spacing: 2px;
}

.subtitle {
  font-size: 16px;
  color: #606266;
  margin: 10px 0 0;
}

.login-form {
  width: 100%;
}

.remember-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin: 5px 0 25px;
}

.forgot-btn {
  padding: 0;
  color: #606266;
}

.forgot-btn:hover {
  color: #409EFF;
}

.login-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 2px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
}

.login-button:hover {
  background: linear-gradient(90deg, #5a6edb 0%, #6a3c9a 100%);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(118, 75, 162, 0.4);
}

.password-icon {
  cursor: pointer;
  color: #909399;
}

.password-icon:hover {
  color: #409EFF;
}

.login-footer {
  margin-top: 40px;
  text-align: center;
  color: #909399;
  font-size: 12px;
}

.register-container {
  text-align: center;
  margin-top: 15px;
  color: #606266;
  font-size: 14px;
}

.register-btn {
  padding: 0;
  font-size: 14px;
  color: #409EFF;
}

.register-btn:hover {
  text-decoration: underline;
}

.login-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
  margin-bottom: 0;
}

/* 响应式调整 */
@media (max-width: 500px) {
  .login-card {
    width: 90%;
    padding: 25px 20px 10px;
  }
  
  .title {
    font-size: 20px;
  }
  
  .subtitle {
    font-size: 14px;
  }
}

.test-login-button {
  background: linear-gradient(90deg, #ff9800 0%, #f57c00 100%);
  border: none;
}

.test-login-button:hover {
  background: linear-gradient(90deg, #f57c00 0%, #e65100 100%);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(245, 124, 0, 0.4);
}
</style> 