<template>
  <div class="change-password-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>修改密码</span>
        </div>
      </template>
      
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="rules"
        label-width="100px"
        class="password-form"
      >
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input 
            v-model="passwordForm.oldPassword" 
            type="password" 
            placeholder="请输入旧密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="新密码" prop="newPassword">
          <el-input 
            v-model="passwordForm.newPassword" 
            type="password" 
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="passwordForm.confirmPassword"
            type="password" 
            placeholder="请再次输入新密码"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="loading">确认修改</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { updatePassword } from '@/api/user'

// 表单数据
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单校验规则
const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能小于6个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

// 表单引用
const passwordFormRef = ref(null)
// 加载状态
const loading = ref(false)

// 提交表单
const submitForm = () => {
  passwordFormRef.value.validate(valid => {
    if (!valid) {
      return
    }
    
    loading.value = true
    
    updatePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
      .then(response => {
        if (response.code === 200) {
          ElMessage.success('密码修改成功')
          resetForm()
        } else {
          ElMessage.error(response.message || '修改失败')
        }
      })
      .catch(error => {
        console.error('修改密码出错:', error)
        ElMessage.error(error.message || '修改失败，请重试')
      })
      .finally(() => {
        loading.value = false
      })
  })
}

// 重置表单
const resetForm = () => {
  passwordFormRef.value.resetFields()
}
</script>

<style scoped>
.change-password-container {
  padding: 20px;
}

.box-card {
  width: 600px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.password-form {
  margin-top: 20px;
}
</style> 