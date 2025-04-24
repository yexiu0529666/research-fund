<template>
  <div class="fund-transfer-create-container">
    <div class="page-header">
      <div class="page-title">创建经费结转申请</div>
      <div class="right">
        <el-button @click="goBack">返回列表</el-button>
      </div>
    </div>
    
    <el-card class="form-card" v-loading="loading">
      <div class="info-alert" v-if="!hasEligibleProjects">
        <el-alert
          title="暂无可用于结转的项目"
          type="warning"
          description="只有待结题状态的项目才可以申请经费结转。如需结转经费，请等待项目进入待结题状态。"
          :closable="false"
          show-icon
        />
      </div>
      
      <el-form 
        ref="formRef"
        :model="formData"
        :rules="formRules"
        label-width="120px"
        class="transfer-form"
        v-if="hasEligibleProjects"
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
                  余额: ¥{{ (item.budget - item.usedBudget).toFixed(2) }}
                </span>
              </div>
            </el-option>
          </el-select>
          <div class="project-status-tag" v-if="selectedProject">
            <el-tag type="warning">待结题</el-tag>
          </div>
        </el-form-item>
        
        <el-form-item label="项目余额" v-if="projectBalance !== null">
          <el-tag type="success" size="large">¥{{ projectBalance.toFixed(2) }}</el-tag>
        </el-form-item>
        
        <el-form-item label="结转金额" prop="amount">
          <el-input-number
            v-model="formData.amount"
            :min="0.01"
            :max="projectBalance || 9999999"
            :precision="2"
            :step="100"
            style="width: 200px;"
          />
          <span class="form-tip">不能超过项目余额</span>
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
          <span class="form-tip">申请日期默认为今天，不可修改</span>
        </el-form-item>
        
        <el-form-item label="结转年度" required>
          <div class="year-selection">
            <el-form-item prop="fromYear" style="margin-bottom: 0">
              <el-select 
                v-model="formData.fromYear"
                placeholder="从哪一年度结转"
                style="width: 180px;"
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
          />
        </el-form-item>
      
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">提交申请</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getPendingCompletionProjects } from '@/api/project'
import { createTransfer } from '@/api/transfer'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const submitting = ref(false)

// 项目选项
const projectOptions = ref([])
const projectBalance = ref(null)
const selectedProject = ref(null)
const hasEligibleProjects = computed(() => projectOptions.value.length > 0)

// 表单数据
const formData = reactive({
  title: '',
  projectId: '',
  projectName: '',
  amount: '',
  applyDate: new Date().toISOString().split('T')[0], // 今天日期，格式：YYYY-MM-DD
  fromYear: new Date().getFullYear().toString(), // 默认为当前年份
  toYear: (new Date().getFullYear() + 1).toString(), // 默认为下一年
  reason: '',
  remark: ''
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
        if (value && projectBalance.value !== null && value > projectBalance.value) {
          callback(new Error(`结转金额不能超过项目余额(¥${projectBalance.value.toFixed(2)})`))
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

// 处理项目选择变化
const handleProjectChange = (projectId) => {
  const selected = projectOptions.value.find(p => p.id === projectId)
  if (selected) {
    selectedProject.value = selected
    formData.projectName = selected.name
    projectBalance.value = selected.budget - selected.usedBudget
    
    // 如果当前输入的金额超过项目余额，则重置为项目余额
    if (formData.amount > projectBalance.value) {
      formData.amount = projectBalance.value
    }
  } else {
    selectedProject.value = null
    formData.projectName = ''
    projectBalance.value = null
  }
}

// 获取项目列表
const fetchProjects = async () => {
  loading.value = true
  try {
    const response = await getPendingCompletionProjects()
    if (response.code === 200) {
      projectOptions.value = response.data || []
      if (projectOptions.value.length === 0) {
        ElMessage.warning('当前没有待结题状态的项目，无法创建经费结转申请')
      }
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
      const response = await createTransfer(formData)
      
      if (response.code === 200) {
        ElMessage.success('经费结转申请提交成功')
        router.push('/expense/transfer/list')
      } else {
        ElMessage.error(response.msg || '提交失败')
      }
    } catch (error) {
      console.error('提交经费结转申请失败:', error)
      ElMessage.error('提交经费结转申请失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields()
    projectBalance.value = null
    selectedProject.value = null
  }
}

// 返回列表页面
const goBack = () => {
  router.push('/expense/transfer/list')
}

// 页面加载时获取数据
onMounted(() => {
  fetchProjects()
})
</script>

<style scoped>
.fund-transfer-create-container {
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

.info-alert {
  margin-bottom: 20px;
}

.project-status-tag {
  display: inline-block;
  margin-left: 10px;
}
</style> 