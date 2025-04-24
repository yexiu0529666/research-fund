<template>
  <div class="completion-audit-container">
    <div class="page-header">
      <div class="page-title">结题审核</div>
    </div>
    
    <el-card class="form-container" v-loading="loading">
      <div class="project-info">
        <h3>项目信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="项目名称" :span="2">{{ projectInfo.name }}</el-descriptions-item>
          <el-descriptions-item label="项目类型">
            <el-tag :type="getTypeTag(projectInfo.type)">{{ getTypeLabel(projectInfo.type) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="负责人">{{ projectInfo.leaderName }}</el-descriptions-item>
          <el-descriptions-item label="开始日期">{{ projectInfo.startDate }}</el-descriptions-item>
          <el-descriptions-item label="结束日期">{{ projectInfo.endDate }}</el-descriptions-item>
          <el-descriptions-item label="总预算">{{ formatCurrency(projectInfo.budget) }}</el-descriptions-item>
          <el-descriptions-item label="已用预算">{{ formatCurrency(projectInfo.usedBudget) }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="getStatusTag(projectInfo.status)">{{ getStatusLabel(projectInfo.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="结题报告提交时间">
            {{ projectInfo.completionReportSubmitTime || '无' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <div class="report-section">
        <h3>结题报告</h3>
        <div v-if="projectInfo.completionReportPath" class="report-download">
          <el-button type="primary" @click="downloadCompletionReport">
            <el-icon><download /></el-icon> 下载结题报告
          </el-button>
        </div>
        <div v-else class="no-report">
          暂无结题报告
        </div>
      </div>
      
      <div class="audit-section">
        <h3>审核结果</h3>
        <el-form :model="auditForm" label-width="120px">
          <el-form-item label="审核结果" required>
            <el-radio-group v-model="auditForm.auditStatus">
              <el-radio label="approved">通过</el-radio>
              <el-radio label="rejected">不通过</el-radio>
            </el-radio-group>
          </el-form-item>
          
          <el-form-item label="审核意见" required>
            <el-input
              v-model="auditForm.comment"
              type="textarea"
              :rows="4"
              placeholder="请输入审核意见"
            />
          </el-form-item>
        </el-form>
      </div>
      
      <div class="button-container">
        <el-button type="primary" @click="submitAudit" :loading="submitLoading">提交审核</el-button>
        <el-button @click="goBack">返回</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProjectDetail, auditProjectCompletion } from '@/api/project'
import { getFileDownloadUrl } from '@/api/upload'
import { Download } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const projectId = ref(route.params.id)
const loading = ref(false)
const submitLoading = ref(false)
const projectInfo = ref({})

// 审核表单
const auditForm = ref({
  auditStatus: 'approved',
  comment: ''
})

// 获取项目类型标签样式
const getTypeTag = (type) => {
  const typeMap = {
    'school': '',
    'horizontal': 'success',
    'vertical': 'primary'
  }
  return typeMap[type] || 'info'
}

// 获取项目类型显示文本
const getTypeLabel = (type) => {
  const typeMap = {
    'school': '校级项目',
    'horizontal': '横向项目',
    'vertical': '纵向项目'
  }
  return typeMap[type] || '其他'
}

// 获取状态标签样式
const getStatusTag = (status) => {
  const statusMap = {
    'planning': 'info',
    'active': 'success',
    'completed': '',
    'suspended': 'warning',
    'pending_completion': 'warning',
    'completion_review': 'primary',
    'archived': 'success'
  }
  return statusMap[status] || 'info'
}

// 获取状态显示文本
const getStatusLabel = (status) => {
  const statusMap = {
    'planning': '筹划中',
    'active': '进行中',
    'completed': '已完成',
    'suspended': '已暂停',
    'pending_completion': '待结题',
    'completion_review': '待结题审核',
    'archived': '已归档'
  }
  return statusMap[status] || '未知状态'
}

// 格式化金额
const formatCurrency = (value) => {
  if (!value && value !== 0) return '¥0.00'
  return `¥${Number(value).toFixed(2)}`
}

// 返回
const goBack = () => {
  router.push('/project/list')
}

// 获取项目详情
const fetchProjectDetail = async () => {
  if (!projectId.value) {
    ElMessage.error('项目ID无效')
    return
  }
  
  loading.value = true
  
  try {
    const response = await getProjectDetail(projectId.value)
    
    if (response.code === 200 && response.data) {
      projectInfo.value = response.data
      
      // 检查项目状态是否为待结题审核
      if (projectInfo.value.status !== 'completion_review') {
        ElMessage.warning('当前项目状态不是待结题审核状态，无法进行结题审核')
      }
    } else {
      ElMessage.error('获取项目详情失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('获取项目详情失败:', error)
    ElMessage.error('获取项目详情失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 下载结题报告
const downloadCompletionReport = () => {
  if (!projectInfo.value.completionReportPath) {
    ElMessage.warning('结题报告不存在')
    return
  }
  
  try {
    // 构建下载文件名
    const fileName = `结题报告_${projectInfo.value.name}.pdf`
    
    // 获取下载链接
    const filePath = '/uploads/project-files/' + projectInfo.value.completionReportPath
    const downloadUrl = getFileDownloadUrl(filePath, fileName)
    
    // 通过创建a标签下载文件
    const link = document.createElement('a')
    link.href = downloadUrl
    link.target = '_blank'
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    
    ElMessage.success('开始下载文件')
  } catch (error) {
    console.error('下载文件失败:', error)
    ElMessage.error('下载文件失败')
  }
}

// 提交审核
const submitAudit = async () => {
  // 表单验证
  if (!auditForm.value.comment) {
    ElMessage.warning('请输入审核意见')
    return
  }
  
  submitLoading.value = true
  
  try {
    const response = await auditProjectCompletion(
      projectId.value,
      auditForm.value.auditStatus,
      auditForm.value.comment
    )
    
    if (response.code === 200) {
      ElMessage.success('结题审核提交成功')
      router.push('/project/list')
    } else {
      ElMessage.error('提交结题审核失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('提交结题审核失败:', error)
    ElMessage.error('提交结题审核失败: ' + (error.message || '未知错误'))
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchProjectDetail()
})
</script>

<style scoped>
.completion-audit-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: bold;
}

.form-container {
  max-width: 800px;
  margin: 0 auto;
}

.project-info,
.report-section,
.audit-section {
  margin-bottom: 30px;
}

.project-info h3,
.report-section h3,
.audit-section h3 {
  font-size: 18px;
  margin-bottom: 20px;
  font-weight: bold;
}

.report-download {
  margin-top: 15px;
}

.no-report {
  color: #909399;
  font-style: italic;
}

.button-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}
</style> 