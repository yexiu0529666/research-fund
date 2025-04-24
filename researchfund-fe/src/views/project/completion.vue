<template>
  <div class="project-completion-container">
    <div class="page-header">
      <div class="page-title">提交结题报告</div>
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
        </el-descriptions>
      </div>
      
      <div class="upload-section">
        <h3>上传结题报告</h3>
        <p class="instruction">请上传项目结题报告，格式支持PDF、Word或PPT，大小不超过20MB。</p>
        
        <el-upload
          class="upload-container"
          action="#"
          :http-request="handleFileUpload"
          :on-change="handleFileChange"
          :on-remove="handleFileRemove"
          :before-upload="beforeUpload"
          :file-list="fileList"
          :limit="1"
          :accept="'.pdf,.doc,.docx,.ppt,.pptx'"
          drag
        >
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>
          <template #tip>
            <div class="el-upload__tip">
              支持PDF、Word或PPT格式文件，不超过20MB
              <div v-if="uploadStatus === 'uploading'" class="upload-status uploading">
                <el-icon><loading /></el-icon> 正在上传...
              </div>
              <div v-else-if="uploadStatus === 'success'" class="upload-status success">
                <el-icon><check /></el-icon> 上传成功
              </div>
              <div v-else-if="uploadStatus === 'error'" class="upload-status error">
                <el-icon><close /></el-icon> 上传失败
              </div>
            </div>
          </template>
        </el-upload>
      </div>
      
      <div class="button-container">
        <el-button type="primary" @click="handleSubmitCompletionReport" :loading="submitLoading" :disabled="!reportPath">提交结题报告</el-button>
        <el-button @click="goBack">返回</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getProjectDetail, submitCompletionReport } from '@/api/project'
import { uploadProjectFile } from '@/api/upload'
import { UploadFilled, Check, Close, Loading } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const projectId = ref(route.params.id)
const loading = ref(false)
const submitLoading = ref(false)
const fileList = ref([])
const projectInfo = ref({})
const uploadStatus = ref('')
const reportPath = ref('')

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
      
      // 检查项目状态是否为待结题
      if (projectInfo.value.status !== 'pending_completion') {
        ElMessage.warning('当前项目状态不是待结题状态，无法提交结题报告')
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

// 处理文件更改
const handleFileChange = (file) => {
  if (file.status === 'ready') {
    uploadStatus.value = 'uploading'
  }
}

// 处理文件移除
const handleFileRemove = () => {
  reportPath.value = ''
  uploadStatus.value = ''
}

// 上传前验证
const beforeUpload = (file) => {
  // 验证文件类型
  const validTypes = ['application/pdf', 'application/msword', 'application/vnd.openxmlformats-officedocument.wordprocessingml.document', 'application/vnd.ms-powerpoint', 'application/vnd.openxmlformats-officedocument.presentationml.presentation']
  const isValidType = validTypes.includes(file.type)
  
  if (!isValidType) {
    ElMessage.error('只能上传PDF、Word或PPT格式的文件!')
    return false
  }
  
  // 验证文件大小 (20MB = 20 * 1024 * 1024)
  const maxSize = 20 * 1024 * 1024
  const isLtMaxSize = file.size <= maxSize
  
  if (!isLtMaxSize) {
    ElMessage.error(`文件大小不能超过 20MB!`)
    return false
  }
  
  return true
}

// 处理文件上传
const handleFileUpload = async (options) => {
  const file = options.file
  uploadStatus.value = 'uploading'
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await uploadProjectFile(formData)
    
    if (response.code === 200) {
      reportPath.value = response.data
      uploadStatus.value = 'success'
      ElMessage.success('文件上传成功')
      
      // 确保展示已上传文件
      fileList.value = [{
        name: file.name,
        url: reportPath.value
      }]
    } else {
      uploadStatus.value = 'error'
      ElMessage.error('文件上传失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('文件上传失败:', error)
    uploadStatus.value = 'error'
    ElMessage.error('文件上传失败: ' + (error.message || '未知错误'))
  }
}

// 提交结题报告
const handleSubmitCompletionReport = async () => {
  if (!reportPath.value) {
    ElMessage.warning('请先上传结题报告')
    return
  }
  
  submitLoading.value = true
  
  try {
    const response = await submitCompletionReport(projectId.value, reportPath.value)
    
    if (response.code === 200) {
      ElMessage.success('结题报告提交成功')
      router.push('/project/list')
    } else {
      ElMessage.error('提交结题报告失败: ' + (response.message || '未知错误'))
    }
  } catch (error) {
    console.error('提交结题报告失败:', error)
    ElMessage.error('提交结题报告失败: ' + (error.message || '未知错误'))
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchProjectDetail()
})
</script>

<style scoped>
.project-completion-container {
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

.project-info {
  margin-bottom: 30px;
}

.project-info h3, .upload-section h3 {
  font-size: 18px;
  margin-bottom: 20px;
  font-weight: bold;
}

.instruction {
  color: #606266;
  margin-bottom: 20px;
}

.upload-section {
  margin-bottom: 30px;
}

.upload-container {
  width: 100%;
}

.button-container {
  display: flex;
  justify-content: center;
  margin-top: 30px;
}

.upload-status {
  margin-top: 10px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.uploading {
  color: #409eff;
}

.success {
  color: #67c23a;
}

.error {
  color: #f56c6c;
}
</style> 