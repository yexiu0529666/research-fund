<template>
  <div class="app-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
        </div>
      </template>
      
      <!-- 用户搜索表单 -->
      <el-form :model="queryParams" ref="queryForm" :inline="true">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="queryParams.realName" placeholder="请输入真实姓名" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
          <el-button icon="Refresh" @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <el-row :gutter="10" class="mb-4">
        <el-col :span="1.5">
          <el-button type="primary" plain icon="Plus" @click="handleAdd">添加用户</el-button>
        </el-col>
      </el-row>

      <!-- 用户表格 -->
      <el-table v-loading="loading" :data="userList" stripe style="width: 100%" border>
        <el-table-column prop="id" label="ID" width="70" />
        <el-table-column prop="username" label="用户名" min-width="100" />
        <el-table-column prop="realName" label="真实姓名" min-width="100" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column prop="phone" label="电话" min-width="120" show-overflow-tooltip />
        <el-table-column prop="departmentName" label="部门" min-width="120" show-overflow-tooltip />
        <el-table-column label="角色" min-width="120" show-overflow-tooltip>
          <template #default="scope">
            <span v-if="!scope.row.roles || scope.row.roles.length === 0">-</span>
            <el-tag v-else-if="scope.row.roles.length > 0" type="success">
              {{ scope.row.roles[0].name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="scope">
            <el-dropdown>
              <el-button type="primary" size="small">
                操作<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item v-if="scope.row.username !== 'admin'" @click="handleEdit(scope.row)">
                    <el-icon><Edit /></el-icon> 编辑
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleResetPwd(scope.row)">
                    <el-icon><Key /></el-icon> 重置密码
                  </el-dropdown-item>
                  <el-dropdown-item v-if="scope.row.username !== 'admin'" @click="handleDelete(scope.row)" divided>
                    <el-icon><Delete /></el-icon> 删除
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 -->
      <el-pagination
        v-if="total > 0"
        :total="total"
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        @current-change="handleQuery"
        @size-change="handleSizeChange"
        layout="total, sizes, prev, pager, next, jumper"
        :page-sizes="[10, 20, 50, 100]"
        background
        style="margin-top: 20px; text-align: right"
      />
      
      <!-- 编辑用户对话框 -->
      <el-dialog
        :title="dialogTitle"
        v-model="editDialogVisible"
        width="500px"
        :close-on-click-modal="false"
        append-to-body
      >
        <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
          <el-form-item label="用户名" prop="username">
            <el-input v-model="editForm.username" placeholder="请输入用户名" :disabled="!!editForm.id" />
          </el-form-item>
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="editForm.realName" placeholder="请输入真实姓名" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="editForm.email" placeholder="请输入邮箱" />
          </el-form-item>
          <el-form-item label="电话" prop="phone">
            <el-input v-model="editForm.phone" placeholder="请输入电话" />
          </el-form-item>
          <el-form-item label="部门" prop="departmentId">
            <el-select v-model="editForm.departmentId" placeholder="请选择部门" @change="handleDepartmentChange">
              <el-option v-for="dept in departmentOptions" :key="dept.id" :label="dept.name" :value="dept.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="角色" prop="roleId">
            <el-select v-model="editForm.roleId" placeholder="请选择角色">
              <el-option v-for="role in roleOptions" :key="role.id" :label="role.roleName" :value="role.id" />
            </el-select>
          </el-form-item>
          <el-form-item label="密码" prop="password" v-if="!editForm.id">
            <el-input v-model="editForm.password" type="password" placeholder="请输入密码" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword" v-if="!editForm.id">
            <el-input v-model="editForm.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="editDialogVisible = false">取 消</el-button>
            <el-button type="primary" @click="submitForm">确 定</el-button>
          </div>
        </template>
      </el-dialog>

      <!-- 重置密码对话框 -->
      <el-dialog
        title="重置密码"
        v-model="pwdDialogVisible"
        width="500px"
        :close-on-click-modal="false"
        append-to-body
      >
        <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px">
          <el-form-item label="新密码" prop="password">
            <el-input v-model="pwdForm.password" type="password" placeholder="请输入新密码" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="pwdForm.confirmPassword" type="password" placeholder="请再次输入新密码" show-password />
          </el-form-item>
        </el-form>
        <template #footer>
          <div class="dialog-footer">
            <el-button @click="pwdDialogVisible = false">取 消</el-button>
            <el-button type="primary" @click="submitResetPwd">确 定</el-button>
          </div>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script>
import { ref, reactive, onMounted, getCurrentInstance, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowDown, Edit, Key, Delete, Plus } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { getAllUsers, updateUser, deleteUser, resetPassword, getAllRoles, register, getAllDepartments } from '@/api/user'

export default {
  name: 'UserManagement',
  components: {
    ArrowDown,
    Edit,
    Key,
    Delete,
    Plus
  },
  setup() {
    const { proxy } = getCurrentInstance()
    const router = useRouter()
    const userStore = useUserStore()
    
    // 检查权限
    const checkPermission = () => {
      if (!userStore.isAdmin) {
        ElMessage.error('您没有访问该页面的权限')
        router.push('/dashboard')
        return false
      }
      return true
    }

    // 数据定义
    const loading = ref(false)
    const userList = ref([])
    const total = ref(0)
    const roleOptions = ref([])
    const departmentOptions = ref([])
    
    // 查询参数
    const queryParams = reactive({
      pageNum: 1,
      pageSize: 10,
      username: '',
      realName: ''
    })
    
    // 编辑表单相关
    const editDialogVisible = ref(false)
    const dialogTitle = ref('')
    const editFormRef = ref(null)
    const editForm = reactive({
      id: undefined,
      username: '',
      realName: '',
      email: '',
      phone: '',
      departmentId: null,
      departmentName: '',
      roleId: null,
      password: '',
      confirmPassword: ''
    })
    
    // 表单验证规则
    const editRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
      ],
      realName: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' }
      ],
      email: [
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      phone: [
        { required: true, message: '请输入手机号', trigger: 'blur' },
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
      ],
      departmentId: [
        { required: true, message: '请选择部门', trigger: 'change' }
      ],
      roleId: [
        { required: true, message: '请选择角色', trigger: 'change' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (editForm.password && value !== editForm.password) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }
    
    // 重置密码相关
    const pwdDialogVisible = ref(false)
    const pwdFormRef = ref(null)
    const pwdForm = reactive({
      userId: null,
      password: '',
      confirmPassword: ''
    })
    
    // 密码表单验证规则
    const pwdRules = {
      password: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, message: '密码长度不能少于 6 个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请再次输入新密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== pwdForm.password) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }
    
    // 页面加载时获取数据
    onMounted(async () => {
      if (checkPermission()) {
        getUserList()
        getRoles()
        getDepartments()
      }
    })
    
    // 获取用户列表
    const getUserList = async () => {
      try {
        loading.value = true
        const response = await getAllUsers(queryParams)
        if (response && response.code === 200) {
          userList.value = response.data.rows || []
          total.value = response.data.total || 0
        } else {
          ElMessage.error(response?.msg || '获取用户列表失败')
        }
      } catch (error) {
        console.error('获取用户列表失败:', error)
        ElMessage.error('获取用户列表失败')
      } finally {
        loading.value = false
      }
    }
    
    // 获取角色列表
    const getRoles = async () => {
      try {
        const response = await getAllRoles()
        if (response && response.code === 200) {
          roleOptions.value = response.data || []
        } else {
          console.error('获取角色列表失败:', response?.msg || '获取角色列表失败')
        }
      } catch (error) {
        console.error('获取角色列表失败:', error)
      }
    }
    
    // 获取部门列表
    const getDepartments = async () => {
      try {
        const response = await getAllDepartments()
        if (response && response.code === 200) {
          departmentOptions.value = response.data || []
        } else {
          console.error('获取部门列表失败:', response?.msg || '获取部门列表失败')
        }
      } catch (error) {
        console.error('获取部门列表失败:', error)
      }
    }
    
    // 搜索按钮
    const handleQuery = () => {
      queryParams.pageNum = 1
      getUserList()
    }
    
    // 重置搜索
    const resetQuery = () => {
      proxy.$refs.queryForm.resetFields()
      handleQuery()
    }
    
    // 改变每页显示记录数
    const handleSizeChange = (size) => {
      queryParams.pageSize = size
      getUserList()
    }
    
    // 编辑用户
    const handleEdit = async (row) => {
      try {
        editDialogVisible.value = true
        dialogTitle.value = '编辑用户'
        
        // 重置表单
        nextTick(() => {
          editFormRef.value.resetFields()
          
          // 填充表单数据
          editForm.id = row.id
          editForm.username = row.username
          editForm.realName = row.realName
          editForm.email = row.email
          editForm.phone = row.phone
          editForm.departmentId = row.departmentId
          editForm.departmentName = row.departmentName
          
          // 设置角色ID
          if (row.roles && row.roles.length > 0) {
            editForm.roleId = row.roles[0].id
          } else {
            editForm.roleId = null
          }
          
          // 清空密码字段
          editForm.password = ''
          editForm.confirmPassword = ''
        })
      } catch (error) {
        console.error('编辑用户失败:', error)
        ElMessage.error('编辑用户失败: ' + error.message)
      }
    }
    
    // 提交表单
    const submitForm = async () => {
      if (!editFormRef.value) return
      
      editFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            // 表单验证已经确保必填字段不为空，无需额外检查
            
            if (editForm.id) {
              // 更新用户
              const userData = {
                username: editForm.username,
                realName: editForm.realName,
                email: editForm.email,
                phone: editForm.phone,
                departmentId: editForm.departmentId,
                roleIds: editForm.roleId ? [editForm.roleId] : []
              }
              const response = await updateUser(editForm.id, userData)
              if (response && response.code === 200) {
                ElMessage.success('用户信息更新成功')
                editDialogVisible.value = false
                getUserList()
              } else {
                ElMessage.error(response?.msg || '用户信息更新失败')
              }
            } else {
              // 添加用户
              const userData = {
                username: editForm.username,
                password: editForm.password,
                realName: editForm.realName,
                email: editForm.email,
                phone: editForm.phone,
                departmentId: editForm.departmentId,
                roleIds: editForm.roleId ? [editForm.roleId] : [],
                gender: 0 // 默认值
              }
              const response = await register(userData)
              if (response && response.code === 200) {
                ElMessage.success('用户添加成功')
                editDialogVisible.value = false
                getUserList()
              } else {
                ElMessage.error(response?.msg || '用户添加失败')
              }
            }
          } catch (error) {
            console.error('提交表单异常:', error)
            ElMessage.error('操作失败: ' + error.message)
          }
        }
      })
    }
    
    // 删除用户
    const handleDelete = (row) => {
      if (row.username === 'admin') {
        ElMessage.warning('不能删除管理员账户')
        return
      }
      
      ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？此操作不可恢复`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          loading.value = true
          const response = await deleteUser(row.id)
          if (response && response.code === 200) {
            ElMessage.success('删除用户成功')
            getUserList()
          } else {
            ElMessage.error(response.message || '删除用户失败')
          }
        } catch (error) {
          console.error('删除用户失败:', error)
          ElMessage.error('删除用户失败')
        } finally {
          loading.value = false
        }
      }).catch(() => {
        // 取消删除
      })
    }
    
    // 重置密码
    const handleResetPwd = (row) => {
      pwdDialogVisible.value = true
      nextTick(() => {
        pwdFormRef.value.resetFields()
        pwdForm.userId = row.id
        pwdForm.password = ''
        pwdForm.confirmPassword = ''
      })
    }
    
    // 提交重置密码
    const submitResetPwd = async () => {
      if (!pwdFormRef.value) return
      
      pwdFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            loading.value = true
            const response = await resetPassword(pwdForm.userId, pwdForm.password)
            if (response && response.code === 200) {
              ElMessage.success('重置密码成功')
              pwdDialogVisible.value = false
            } else {
              ElMessage.error(response.message || '重置密码失败')
            }
          } catch (error) {
            console.error('重置密码失败:', error)
            ElMessage.error('重置密码失败')
          } finally {
            loading.value = false
          }
        }
      })
    }
    
    // 添加用户
    const handleAdd = () => {
      editDialogVisible.value = true
      dialogTitle.value = '添加用户'
      nextTick(() => {
        editFormRef.value.resetFields()
        editForm.id = undefined
        editForm.username = ''
        editForm.realName = ''
        editForm.email = ''
        editForm.phone = ''
        editForm.departmentId = null
        editForm.departmentName = ''
        editForm.roleId = null
        editForm.password = ''
        editForm.confirmPassword = ''
      })
    }
    
    // 处理部门变化
    const handleDepartmentChange = () => {
      // 获取选择的部门名称
      const selectedDept = departmentOptions.value.find(dept => dept.id === editForm.departmentId)
      if (selectedDept) {
        editForm.departmentName = selectedDept.name
      } else {
        editForm.departmentName = ''
      }
    }
    
    return {
      loading,
      userList,
      total,
      queryParams,
      roleOptions,
      departmentOptions,
      editDialogVisible,
      dialogTitle,
      editFormRef,
      editForm,
      editRules,
      pwdDialogVisible,
      pwdFormRef,
      pwdForm,
      pwdRules,
      getUserList,
      handleQuery,
      resetQuery,
      handleSizeChange,
      handleEdit,
      submitForm,
      handleDelete,
      handleResetPwd,
      submitResetPwd,
      handleAdd,
      handleDepartmentChange
    }
  }
}
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.mr-5 {
  margin-right: 5px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 