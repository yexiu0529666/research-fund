<template>
  <div class="register-container">
    <div class="register-background">
      <div class="shape shape1"></div>
      <div class="shape shape2"></div>
      <div class="shape shape3"></div>
    </div>
    
    <el-card class="register-card">
      <div class="title-container">
        <h3 class="title">科研经费管理系统</h3>
        <p class="subtitle">用户注册</p>
      </div>
      
      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        autocomplete="on"
        label-position="top"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            ref="usernameRef"
            v-model="registerForm.username"
            placeholder="请输入用户名"
            type="text"
            :prefix-icon="User"
            autocomplete="username"
          />
        </el-form-item>
        
        <el-form-item label="姓名" prop="name">
          <el-input
            v-model="registerForm.name"
            placeholder="请输入您的真实姓名"
            :prefix-icon="UserFilled"
          />
        </el-form-item>
        
        <el-form-item label="手机号码" prop="phone">
          <el-input
            v-model="registerForm.phone"
            placeholder="请输入手机号码"
            :prefix-icon="Phone"
          />
        </el-form-item>
        
        <el-form-item label="邮箱" prop="email">
          <el-input
            v-model="registerForm.email"
            placeholder="请输入邮箱（选填）"
            :prefix-icon="Message"
            autocomplete="email"
          />
        </el-form-item>
        
        <el-form-item label="部门" prop="departmentId">
          <el-select
            v-model="registerForm.departmentId"
            placeholder="请选择所属部门"
            style="width: 100%"
          >
            <el-option
              v-for="item in departmentOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="registerForm.password"
            :type="passwordVisible ? 'text' : 'password'"
            placeholder="请输入密码"
            autocomplete="new-password"
            :prefix-icon="Lock"
          >
            <template #suffix>
              <el-icon class="password-icon" @click="passwordVisible = !passwordVisible">
                <component :is="passwordVisible ? 'View' : 'Hide'" />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            :type="confirmPasswordVisible ? 'text' : 'password'"
            placeholder="请再次输入密码"
            autocomplete="new-password"
            :prefix-icon="Lock"
          >
            <template #suffix>
              <el-icon class="password-icon" @click="confirmPasswordVisible = !confirmPasswordVisible">
                <component :is="confirmPasswordVisible ? 'View' : 'Hide'" />
              </el-icon>
            </template>
          </el-input>
        </el-form-item>
        
        <el-button
          :loading="loading"
          type="primary"
          class="register-button"
          @click="handleRegister"
        >
          注 册
        </el-button>
        
        <div class="login-container">
          <span>已有账号？</span>
          <el-button type="text" class="login-btn" @click="handleLogin">去登录</el-button>
        </div>
      </el-form>
      
      <div class="register-footer">
        <p>© {{ currentYear }} 科研经费管理系统 - 专业的研究资金管理平台</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, UserFilled, Lock, Message, View, Hide, Phone } from '@element-plus/icons-vue'
import { getAllDepartments, register } from '@/api/user'

const router = useRouter()
const registerFormRef = ref(null)
const usernameRef = ref(null)
const loading = ref(false)
const passwordVisible = ref(false)
const confirmPasswordVisible = ref(false)
const departmentOptions = ref([])
const currentYear = computed(() => new Date().getFullYear())

// 注册表单
const registerForm = reactive({
  username: '',
  name: '',
  phone: '',
  email: '',
  departmentId: '',
  password: '',
  confirmPassword: ''
})

// 获取部门列表
const fetchDepartments = async () => {
  try {
    const res = await getAllDepartments()
    departmentOptions.value = res.data.map(item => ({
      value: item.id,
      label: item.name
    }))
  } catch (error) {
    console.error('获取部门列表失败:', error)
    ElMessage.error('获取部门列表失败')
  }
}

// 验证手机号码
const validatePhone = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入手机号码'))
  } else if (!/^1[3-9]\d{9}$/.test(value)) {
    callback(new Error('请输入正确的手机号码'))
  } else {
    callback()
  }
}

// 表单验证规则
const validatePass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请输入密码'))
  } else if (value.length < 6) {
    callback(new Error('密码长度至少为6位'))
  } else {
    if (registerForm.confirmPassword !== '') {
      registerFormRef.value.validateField('confirmPassword')
    }
    callback()
  }
}

const validateConfirmPass = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名长度至少为3位', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入您的真实姓名', trigger: 'blur' }
  ],
  phone: [
    { required: true,validator: validatePhone, trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  departmentId: [
    { required: true, message: '请选择所属部门', trigger: 'change' }
  ],
  password: [
    { validator: validatePass, trigger: 'blur' }
  ],
  confirmPassword: [
    { validator: validateConfirmPass, trigger: 'blur' }
  ]
}

// 处理注册
const handleRegister = () => {
  registerFormRef.value.validate(valid => {
    if (valid) {
      loading.value = true
      
      const registerData = {
        username: registerForm.username,
        password: registerForm.password,
        realName: registerForm.name,
        email: registerForm.email || '', // 邮箱可能为空
        phone: registerForm.phone,
        departmentId: registerForm.departmentId,
        gender: 0  // 默认性别
      }
      
      register(registerData).then(response => {
        ElMessage({
          message: '注册成功！请返回登录页面直接登录',
          type: 'success',
          duration: 3000
        })
        router.push('/login')
      }).catch(error => {
        console.error('注册失败:', error)
        ElMessage.error(error.message || '注册失败，请稍后再试')
      }).finally(() => {
        loading.value = false
      })
    } else {
      return false
    }
  })
}

// 处理登录跳转
const handleLogin = () => {
  router.push('/login')
}

onMounted(() => {
  // 获取部门列表
  fetchDepartments()
  
  // 自动聚焦用户名输入框
  usernameRef.value?.focus()
})
</script>

<style scoped>
.register-container {
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  position: relative;
  overflow: hidden;
}

.register-background {
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

.register-card {
  width: 480px;
  padding: 35px 35px 15px;
  border-radius: 16px;
  box-shadow: 0 10px 50px rgba(0, 0, 0, 0.1);
  background-color: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 2;
  transition: all 0.3s ease;
  max-height: 90vh;
  overflow-y: auto;
}

.register-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 15px 60px rgba(0, 0, 0, 0.15);
}

.title-container {
  text-align: center;
  margin-bottom: 30px;
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

.register-form {
  width: 100%;
}

.password-icon {
  cursor: pointer;
  color: #909399;
}

.password-icon:hover {
  color: #409EFF;
}

.register-button {
  width: 100%;
  height: 44px;
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 2px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  border: none;
  transition: all 0.3s ease;
  margin-top: 10px;
}

.register-button:hover {
  background: linear-gradient(90deg, #5a6edb 0%, #6a3c9a 100%);
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(118, 75, 162, 0.4);
}

.login-container {
  text-align: center;
  margin-top: 15px;
  color: #606266;
  font-size: 14px;
}

.login-btn {
  padding: 0;
  font-size: 14px;
  color: #409EFF;
}

.login-btn:hover {
  text-decoration: underline;
}

.register-footer {
  margin-top: 30px;
  text-align: center;
  color: #909399;
  font-size: 12px;
}

/* 响应式调整 */
@media (max-width: 500px) {
  .register-card {
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
</style> 