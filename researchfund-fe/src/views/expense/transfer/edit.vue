<template>
  <div class="fund-transfer-edit-container">
    <div class="page-header">
      <div class="page-title">编辑经费结转申请</div>
      <div class="right">
        <el-button @click="goBack">返回</el-button>
      </div>
    </div>
    
    <el-card class="form-card" v-loading="loading">
      <el-form 
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="transfer-form"
      >
        <el-form-item label="结转标题" prop="title">
          <el-input 
            v-model="formData.title" 
            placeholder="请输入结转标题"
            maxlength="100"
            show-word-limit
            style="width: 400px;"
          />
        </el-form-item>
        
        <el-form-item label="所属项目" prop="projectId">
          <el-select 
            v-model="formData.projectId"
            placeholder="请选择项目"
            style="width: 400px;"
            @change="handleProjectChange"
            :disabled="!isPending"
          >
            <el-option
              v-for="item in projectOptions"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            >
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span>{{ item.name }}</span>
                <span style="color: #67c23a; font-size: 14px;">
                  余额: ¥{{ calculateProjectBalance(item) }}
                </span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="项目余额" v-if="projectBalance !== null">
          <el-tag type="success" size="large">¥{{ projectBalance.toFixed(2) }}</el-tag>
        </el-form-item>
        
        <el-form-item label="结转金额" prop="amount">
          <el-input-number
            v-model="formData.amount"
            :min="0.01"
            :max="maxAmount"
            :precision="2"
            :step="100"
            style="width: 200px;"
            :disabled="!isPending"
          />
          <span class="form-tip">不能超过项目余额{{ originalTransferAmount > 0 ? '加上原结转金额' : '' }}</span>
        </el-form-item>
        
        <el-form-item label="申请日期">
          <el-date-picker
            v-model="formData.applyDate"
            type="date"
            placeholder="申请日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabled="true"
          />
          <span class="form-tip">申请日期不可修改</span>
        </el-form-item>
        
        <el-form-item label="结转年度" required>
          <div class="year-selection">
            <el-form-item prop="fromYear" style="margin-bottom: 0">
              <el-select 
                v-model="formData.fromYear"
                placeholder="从哪一年度结转"
                style="width: 180px;"
                :disabled="!isPending"
              >
                <el-option
                  v-for="year in yearOptions"
                  :key="year"
                  :label="year + '年'"
                  :value="year"
                />
              </el-select>
            </el-form-item>
            <span class="year-arrow">→</span>
            <el-form-item prop="toYear" style="margin-bottom: 0">
              <el-select 
                v-model="formData.toYear"
                placeholder="结转到哪一年度"
                style="width: 180px;"
                :disabled="!isPending"
              >
                <el-option
                  v-for="year in yearOptions"
                  :key="year"
                  :label="year + '年'"
                  :value="year"
                />
              </el-select>
            </el-form-item>
          </div>
        </el-form-item>
        
        <el-form-item label="结转原因" prop="reason">
          <el-input
            v-model="formData.reason"
            type="textarea"
            :rows="4"
            placeholder="请输入结转原因，例如项目延期、资金使用计划调整等"
            maxlength="500"
            show-word-limit
            style="width: 500px;"
            :disabled="!isPending"
          />
        </el-form-item>
        
        
        <el-form-item label="申请状态">
          <el-tag :type="getStatusType(formData.status)">
            {{ getStatusText(formData.status) }}
          </el-tag>
          
          <template v-if="formData.auditComment && formData.status !== 'pending'">
            <div class="audit-comment">
              <span class="audit-label">审核意见:</span>
              <span>{{ formData.auditComment }}</span>
            </div>
          </template>
        </el-form-item>
        
        <el-form-item v-if="isPending">
          <el-button type="primary" @click="submitForm" :loading="submitting">保存修改</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
        <el-form-item v-else>
          <el-button @click="goBack">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getAvailableProjectsForExpense } from '@/api/project'
import { getTransferDetail, updateTransfer } from '@/api/transfer'

const router = useRouter()
const route = useRoute()
const transferId = route.params.id
const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)

// 项目选项
const projectOptions = ref([])
const projectBalance = ref(null)
const originalTransferAmount = ref(0)
const originalProjectId = ref(null)

// 表单数据
const formData = reactive({
  id: '',
  title: '',
  projectId: '',
  projectName: '',
  amount: '',
  applyDate: '',
  fromYear: '',
  toYear: '',
  reason: '',
  remark: '',
  status: 'pending',
  auditComment: ''
})

// 计算属性
const isPending = computed(() => formData.status === 'pending')

const maxAmount = computed(() => {
  if (projectBalance.value === null) return 9999999
  
  // 如果是原项目，允许的最大金额是项目余额+原结转金额
  if (formData.projectId === originalProjectId.value) {
    return projectBalance.value + originalTransferAmount.value
  }
  
  // 如果是新项目，允许的最大金额是项目余额
  return projectBalance.value
})

// 年度选项（当前年份前后5年）
const yearOptions = computed(() => {
  const currentYear = new Date().getFullYear()
  const years = []
  for (let i = currentYear - 5; i <= currentYear + 5; i++) {
    years.push(i.toString())
  }
  return years
})

// 表单校验规则
const formRules = {
  title: [
    { required: true, message: '请输入结转标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度应在2-100个字符之间', trigger: 'blur' }
  ],
  projectId: [
    { required: true, message: '请选择项目', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '请输入结转金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '结转金额必须大于0', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value && value > maxAmount.value) {
          callback(new Error(`结转金额不能超过${formData.projectId === originalProjectId.value ? '项目余额加上原结转金额' : '项目余额'}`))
        } else {
          callback()
        }
      }, 
      trigger: 'blur' 
    }
  ],
  fromYear: [
    { required: true, message: '请选择源年度', trigger: 'change' }
  ],
  toYear: [
    { required: true, message: '请选择目标年度', trigger: 'change' },
    { 
      validator: (rule, value, callback) => {
        if (value && formData.fromYear && value === formData.fromYear) {
          callback(new Error('结转的目标年度不能与源年度相同'))
        } else {
          callback()
        }
      }, 
      trigger: 'change' 
    }
  ],
  reason: [
    { required: true, message: '请输入结转原因', trigger: 'blur' },
    { min: 5, max: 500, message: '原因说明长度应在5-500个字符之间', trigger: 'blur' }
  ],
  remark: [
    { max: 200, message: '备注长度不能超过200个字符', trigger: 'blur' }
  ]
}

// 计算项目实际余额（包括显示用）
const calculateProjectBalance = (project) => {
  if (!project) return '0.00'
  return (project.budget - project.usedBudget).toFixed(2)
}

// 处理项目选择变化
const handleProjectChange = (projectId) => {
  const selectedProject = projectOptions.value.find(p => p.id === projectId)
  if (selectedProject) {
    formData.projectName = selectedProject.name
    
    // 计算项目实际余额
    const actualBalance = selectedProject.budget - selectedProject.usedBudget
    
    // 如果是原项目，项目余额需要加上原结转金额（因为在获取项目时，原项目余额已经减去了原结转金额）
    if (projectId === originalProjectId.value) {
      projectBalance.value = actualBalance + originalTransferAmount.value
    } else {
      projectBalance.value = actualBalance
    }
    
    // 如果当前输入的金额超过最大允许金额，则重置为最大允许金额
    if (formData.amount > maxAmount.value) {
      formData.amount = maxAmount.value
    }
  } else {
    formData.projectName = ''
    projectBalance.value = null
  }
}

// 获取状态标签类型
const getStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取状态文本
const getStatusText = (status) => {
  const statusMap = {
    pending: '待审核',
    approved: '已批准',
    rejected: '已拒绝'
  }
  return statusMap[status] || '未知状态'
}

// 获取项目列表
const fetchProjects = async () => {
  loading.value = true
  try {
    const response = await getAvailableProjectsForExpense()
    if (response.code === 200) {
      projectOptions.value = response.data || []
    } else {
      ElMessage.warning(response.msg || '获取项目列表失败')
    }
  } catch (error) {
    console.error('获取项目列表失败:', error)
    ElMessage.error('获取项目列表失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 获取经费结转申请详情
const fetchTransferDetail = async () => {
  if (!transferId) {
    ElMessage.error('缺少经费结转申请ID')
    router.push('/expense/transfer/list')
    return
  }
  
  loading.value = true
  try {
    const response = await getTransferDetail(transferId)
    if (response.code === 200 && response.data) {
      const detail = response.data
      
      // 保存原始项目ID和金额，用于后续编辑时的余额计算
      originalProjectId.value = detail.projectId
      originalTransferAmount.value = detail.amount
      
      // 填充表单数据
      Object.keys(formData).forEach(key => {
        if (key in detail) {
          formData[key] = detail[key]
        }
      })
      
      handleProjectChange(formData.projectId)
      
    } else {
      ElMessage.error(response.msg || '获取经费结转申请详情失败')
      router.push('/expense/transfer/list')
    }
  } catch (error) {
    console.error('获取经费结转申请详情失败:', error)
    ElMessage.error('获取经费结转申请详情失败，请稍后重试')
    router.push('/expense/transfer/list')
  } finally {
    loading.value = false
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate(async (valid, fields) => {
    if (!valid) {
      console.error('表单校验失败:', fields)
      return
    }
    
    submitting.value = true
    try {
      const response = await updateTransfer(formData.id, formData)
      
      if (response.code === 200) {
        ElMessage.success('经费结转申请更新成功')
        router.push('/expense/transfer/list')
      } else {
        ElMessage.error(response.msg || '更新失败')
      }
    } catch (error) {
      console.error('更新经费结转申请失败:', error)
      ElMessage.error('更新经费结转申请失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

// 返回列表页面
const goBack = () => {
  router.push('/expense/transfer/list')
}

// 页面加载时获取数据
onMounted(() => {
  fetchProjects()
  fetchTransferDetail()
})
</script>

<style scoped>
.fund-transfer-edit-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: bold;
}

.form-card {
  margin-bottom: 20px;
}

.transfer-form {
  max-width: 800px;
  margin: 0 auto;
}

.form-tip {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}

.year-selection {
  display: flex;
  align-items: center;
}

.year-arrow {
  margin: 0 15px;
  font-size: 18px;
  color: #409eff;
}

.audit-comment {
  margin-top: 8px;
  color: #606266;
}

.audit-label {
  font-weight: bold;
  margin-right: 8px;
}
</style> 