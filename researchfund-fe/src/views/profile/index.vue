<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人信息</span>
          <el-button type="primary" @click="toggleEdit">
            {{ isEdit ? '取消' : '编辑资料' }}
          </el-button>
        </div>
      </template>

      <el-form 
        ref="formRef"
        :model="userForm"
        :rules="rules"
        label-width="100px"
        :disabled="!isEdit"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" disabled />
        </el-form-item>
        
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="userForm.realName" />
        </el-form-item>
        
        <el-form-item label="部门" prop="department">
          <el-input v-model="userForm.department" disabled />
        </el-form-item>
        
        <el-form-item label="角色" prop="role">
          <el-input v-model="userForm.role" disabled />
        </el-form-item>
        
        <el-form-item label="电子邮箱" prop="email">
          <el-input v-model="userForm.email" />
        </el-form-item>
        
        <el-form-item label="手机号码" prop="phone">
          <el-input v-model="userForm.phone" />
        </el-form-item>
        
        <el-form-item label="个人简介" prop="remark">
          <el-input 
            v-model="userForm.remark" 
            type="textarea" 
            :rows="4" 
            placeholder="请输入个人简介"
          />
        </el-form-item>
        
        <el-form-item v-if="isEdit">
          <el-button type="primary" @click="submitForm" :loading="loading">保存更新</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
      
      <div class="password-link">
        <el-link type="primary" @click="goToChangePassword">修改密码</el-link>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { getUserInfo, updateUser } from '@/api/user'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const isEdit = ref(false)

// 用户表单数据
const userForm = reactive({
  username: '',
  realName: '',
  email: '',
  phone: '',
  department: '',
  role: '',
  remark: ''
})

// 表单验证规则
const rules = {
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入电子邮箱', trigger: 'blur' },
    { type: 'email', message: '请输入正确的电子邮箱格式', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
  ]
}

// 切换编辑状态
const toggleEdit = () => {
  if (isEdit.value) {
    // 取消编辑，恢复原数据
    getUserData()
  }
  isEdit.value = !isEdit.value
}

// 获取用户数据
const getUserData = async () => {
  try {
    loading.value = true
    const res = await getUserInfo()
    if (res.code === 200) {
      const userData = res.data.user
      userForm.username = userData.username
      userForm.realName = userData.realName
      userForm.email = userData.email
      userForm.phone = userData.phone
      userForm.department = res.data.department ? res.data.department.name : '无'
      
      // 根据角色代码转换为中文名称
      if (res.data.roles && res.data.roles.length > 0) {
        const roleNames = res.data.roles.map(role => {
          if (role === 'ROLE_ADMIN') return '管理员'
          if (role === 'ROLE_RESEARCHER') return '科研人员'
          return role
        })
        userForm.role = roleNames.join(', ')
      } else {
        userForm.role = '无'
      }
      
      userForm.remark = userData.remark || ''
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    loading.value = true
    
    // 更新用户信息
    const res = await updateUser(userStore.userId, {
      realName: userForm.realName,
      email: userForm.email,
      phone: userForm.phone,
      remark: userForm.remark
    })
    
    if (res.code === 200) {
      ElMessage.success('个人信息更新成功')
      isEdit.value = false
      
      // 更新store中的用户名
      if (userForm.realName !== userStore.name) {
        userStore.name = userForm.realName
      }
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    console.error('提交表单出错:', error)
    ElMessage.error('提交失败，请检查表单')
  } finally {
    loading.value = false
  }
}

// 重置表单
const resetForm = () => {
  getUserData()
}

// 跳转到修改密码页面
const goToChangePassword = () => {
  router.push('/change-password')
}

// 页面加载时获取用户数据
onMounted(() => {
  getUserData()
})
</script>

<style scoped>
.profile-container {
  padding: 20px;
}

.profile-card {
  max-width: 800px;
  margin: 0 auto;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 18px;
  font-weight: bold;
}

.password-link {
  margin-top: 20px;
  text-align: center;
}
</style> 