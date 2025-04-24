<template>
  <div class="achievement-create-container">
    <el-card class="form-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">新增成果奖励</span>
          <el-button @click="goBack" link>返回列表</el-button>
        </div>
      </template>

      <el-form 
        ref="formRef" 
        :model="formData" 
        :rules="rules" 
        label-width="120px" 
        label-position="right"
        status-icon
      >
        <el-form-item label="成果名称" prop="title">
          <el-input v-model="formData.title" placeholder="请输入成果名称" />
        </el-form-item>
        
        <el-form-item label="成果类型" prop="type">
          <el-select v-model="formData.type" placeholder="请选择成果类型" style="width: 100%">
            <el-option label="期刊论文" value="journal" />
            <el-option label="会议论文" value="conference" />
            <el-option label="专利" value="patent" />
            <el-option label="著作" value="book" />
            <el-option label="软件著作权" value="software" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>

        <el-form-item label="成果级别" prop="level">
          <el-select v-model="formData.level" placeholder="请选择成果级别" style="width: 100%">
            <el-option label="国家级" value="national" />
            <el-option label="省部级" value="provincial" />
            <el-option label="厅局级" value="departmental" />
            <el-option label="校级" value="school" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="作者" prop="authors">
          <el-input v-model="formData.authors" placeholder="请输入作者，多位作者用逗号分隔" />
        </el-form-item>
        
        <el-form-item label="获奖日期" prop="awardDate">
          <el-date-picker 
            v-model="formData.awardDate" 
            type="date" 
            placeholder="选择日期" 
            style="width: 100%"
            value-format="YYYY-MM-DD" />
        </el-form-item>
        
        <el-form-item label="相关项目" prop="projectId">
          <el-select 
            v-model="formData.projectId" 
            placeholder="请选择相关项目" 
            filterable 
            style="width: 100%"
            :loading="projectsLoading"
          >
            <el-option 
              v-for="item in projectOptions" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="成果简介" prop="description">
          <el-input 
            v-model="formData.description" 
            type="textarea" 
            rows="4" 
            placeholder="请输入成果简介"
          />
        </el-form-item>
        
        <el-form-item label="成果附件" prop="attachments">
          <el-upload
            class="attachment-uploader"
            action="#"
            :http-request="handleFileUpload"
            :on-success="handleAttachmentSuccess"
            :on-error="handleAttachmentError"
            :on-remove="handleAttachmentRemove"
            :file-list="fileList"
            multiple
          >
            <el-button type="primary">上传附件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                可上传证书、论文原文等相关材料，支持PDF、Word、JPG等格式文件。<span style="color: #F56C6C;">（必传）</span>
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="备注" prop="remarks">
          <el-input 
            v-model="formData.remarks" 
            type="textarea" 
            rows="3" 
            placeholder="请输入备注信息（选填）" 
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">提交申请</el-button>
          <el-button @click="resetForm">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { createAchievement } from '@/api/achievement'
import { getProjectList } from '@/api/project'
import { uploadProjectFile } from '@/api/upload'

const router = useRouter()
const formRef = ref(null)
const submitLoading = ref(false)
const projectsLoading = ref(false)
const projectOptions = ref([])
const fileList = ref([])

// 表单数据
const formData = reactive({
  title: '',
  type: '',
  level: '',
  authors: '',
  awardDate: '',
  projectId: '',
  description: '',
  attachments: [],
  remarks: ''
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入成果名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择成果类型', trigger: 'change' }
  ],
  level: [
    { required: true, message: '请选择成果级别', trigger: 'change' }
  ],
  authors: [
    { required: true, message: '请输入作者', trigger: 'blur' }
  ],
  awardDate: [
    { required: true, message: '请选择获奖日期', trigger: 'change' }
  ],
  projectId: [
    { required: true, message: '请选择相关项目', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入成果简介', trigger: 'blur' },
    { min: 10, max: 2000, message: '长度在 10 到 2000 个字符', trigger: 'blur' }
  ],
  attachments: [
    { 
      required: true, 
      validator: (rule, value, callback) => {
        if (value.length === 0) {
          callback(new Error('请上传至少一个成果附件'));
        } else {
          callback();
        }
      }, 
      trigger: 'change' 
    }
  ]
}

// 上传文件的请求头
const uploadHeaders = computed(() => {
  return {
    Authorization: 'Bearer ' + localStorage.getItem('token')
  }
})

// 加载项目列表
const loadProjects = async () => {
  projectsLoading.value = true
  try {
    const response = await getProjectList({ pageSize: 100, status: 'archived' })
    if (response.code === 200) {
      projectOptions.value = response.data.list || []
    }
  } catch (error) {
    console.error('获取项目列表失败:', error)
  } finally {
    projectsLoading.value = false
  }
}

// 处理附件上传成功
const handleAttachmentSuccess = (response, file) => {
  if (response.code === 200) {
    formData.attachments.push({
      id: response.data.id,
      name: file.name,
      path: response.data
    })
    ElMessage.success('附件上传成功')
  } else {
    ElMessage.error(response.message || '附件上传失败')
  }
}

// 处理附件上传失败
const handleAttachmentError = (error) => {
  console.error('附件上传失败:', error)
  ElMessage.error('附件上传失败，请重试')
}

// 处理附件移除
const handleAttachmentRemove = (file) => {
  const index = fileList.value.indexOf(file)
  if (index !== -1) {
    fileList.value.splice(index, 1)
  }
  
  const attachmentIndex = formData.attachments.findIndex(item => item.name === file.name)
  if (attachmentIndex !== -1) {
    formData.attachments.splice(attachmentIndex, 1)
  }
}

// 处理文件上传
const handleFileUpload = async (options) => {
  const file = options.file
  
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    const response = await uploadProjectFile(formData)
    
    if (response.code === 200) {
      // 上传成功回调
      options.onSuccess && options.onSuccess(response)
    } else {
      // 上传失败回调
      options.onError && options.onError(new Error(response.message || '文件上传失败'))
    }
  } catch (error) {
    console.error('文件上传失败:', error)
    // 上传失败回调
    options.onError && options.onError(error)
  }
}

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return
  
  // 检查附件是否已上传
  if (formData.attachments.length === 0) {
    ElMessage.error('请上传至少一个成果附件')
    return
  }
  
  await formRef.value.validate(async (valid, fields) => {
    if (valid) {
      submitLoading.value = true
      try {
        // 处理作者，转换为数组
        const authorsArray = formData.authors.split(',').map(item => item.trim())
        
        // 构建提交的数据
        const submitData = {
          ...formData,
          authors: authorsArray,
          awardAmount: 0 // 默认设置为0，由管理员审核时填写
        }
        
        const response = await createAchievement(submitData)
        if (response.code === 200) {
          ElMessage.success('成果奖励申请提交成功')
          // 跳转回列表页
          router.push('/achievement/list')
        } else {
          ElMessage.error(response.message || '提交失败')
        }
      } catch (error) {
        console.error('提交成果奖励申请出错:', error)
        ElMessage.error('提交失败，请稍后再试')
      } finally {
        submitLoading.value = false
      }
    } else {
      // 表单验证失败
      console.error('表单验证失败:', fields)
      ElMessage.error('请完善表单信息')
    }
  })
}

// 重置表单
const resetForm = () => {
  if (!formRef.value) return
  
  ElMessageBox.confirm('确定要重置表单吗？所有已填写的内容将被清空。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    formRef.value.resetFields()
    formData.attachments = []
    fileList.value = []
  }).catch(() => {})
}

// 返回列表页
const goBack = () => {
  router.push('/achievement/list')
}

// 初始化
onMounted(() => {
  // 加载项目列表
  loadProjects()
})
</script>

<style scoped>
.achievement-create-container {
  padding: 20px;
}

.form-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
}

.attachment-uploader {
  width: 100%;
}

.el-upload__tip {
  margin-top: 5px;
  color: #909399;
  font-size: 12px;
}
</style> 