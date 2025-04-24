<template>
  <div class="achievement-audit-container">
    <!-- 操作按钮 -->
    <div class="operation-bar">
      <el-button @click="goBack">
        <el-icon><Back /></el-icon> 返回列表
      </el-button>
    </div>

    <el-row :gutter="20">
      <!-- 基本信息 -->
      <el-col :span="16">
        <el-card shadow="hover" class="detail-card">
          <template #header>
            <div class="card-header">
              <span class="title">成果基本信息</span>
              <el-tag type="warning" size="small">待审核</el-tag>
            </div>
          </template>
          <div class="detail-content" v-loading="loading">
            <div class="detail-item">
              <span class="label">成果名称:</span>
              <span class="value">{{ achievement.title }}</span>
            </div>
            <div class="detail-item">
              <span class="label">成果类型:</span>
              <span class="value">{{ getTypeLabel(achievement.type) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">成果级别:</span>
              <span class="value">{{ getLevelLabel(achievement.level) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">作者:</span>
              <span class="value">{{ formatAuthors(achievement.authors) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">获奖日期:</span>
              <span class="value">{{ achievement.awardDate || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">奖励金额:</span>
              <span class="value">{{ formatCurrency(achievement.awardAmount) }}</span>
            </div>
            <div class="detail-item">
              <span class="label">相关项目:</span>
              <span class="value">{{ achievement.projectName || '-' }}</span>
            </div>
            <div class="detail-item full-width">
              <span class="label">成果简介:</span>
              <div class="description">{{ achievement.description || '-' }}</div>
            </div>
            <div class="detail-item full-width">
              <span class="label">备注:</span>
              <div class="remarks">{{ achievement.remarks || '-' }}</div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 审核表单和附件 -->
      <el-col :span="8">
        <!-- 审核表单 -->
        <el-card shadow="hover" class="detail-card">
          <template #header>
            <div class="card-header">
              <span class="title">审核信息</span>
            </div>
          </template>
          <div class="audit-form" v-loading="loading">
            <el-form 
              ref="auditFormRef" 
              :model="auditForm" 
              :rules="rules" 
              label-position="top"
            >
              <el-form-item label="审核结果" prop="status">
                <el-radio-group v-model="auditForm.status">
                  <el-radio label="approved">通过</el-radio>
                  <el-radio label="rejected">拒绝</el-radio>
                </el-radio-group>
              </el-form-item>
              
              <el-form-item label="奖励金额（¥）" prop="awardAmount" v-if="auditForm.status === 'approved'">
                <el-input-number 
                  v-model="auditForm.awardAmount" 
                  :precision="2" 
                  :step="100" 
                  :min="0"
                  style="width: 100%"
                  placeholder="请输入奖励金额" />
              </el-form-item>
              
              <el-form-item label="审核意见" prop="comment">
                <el-input 
                  v-model="auditForm.comment" 
                  type="textarea" 
                  :rows="6" 
                  placeholder="请输入审核意见" 
                />
              </el-form-item>
              
              <el-form-item>
                <el-button 
                  type="primary" 
                  @click="submitAudit" 
                  :loading="submitLoading" 
                  :disabled="!canSubmit"
                >
                  提交审核
                </el-button>
              </el-form-item>
            </el-form>
          </div>
        </el-card>

        <!-- 附件信息 -->
        <el-card shadow="hover" class="detail-card mt-20">
          <template #header>
            <div class="card-header">
              <span class="title">附件信息</span>
            </div>
          </template>
          <div class="attachment-list" v-loading="loading">
            <el-empty v-if="!achievement.attachments || achievement.attachments.length === 0" description="暂无附件" />
            <div v-else class="attachment-item" v-for="(file, index) in achievement.attachments" :key="index">
              <el-icon><Document /></el-icon>
              <span class="attachment-name">{{ file.name }}</span>
              <el-button type="primary" link @click="downloadFile(file)">下载</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Back, Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAchievementById, auditAchievement } from '@/api/achievement'
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()
const achievementId = route.params.id

// 加载状态
const loading = ref(false)
// 提交加载状态
const submitLoading = ref(false)
// 成果奖励数据
const achievement = ref({})
// 审核表单
const auditFormRef = ref(null)
const auditForm = reactive({
  status: 'approved',
  comment: '',
  awardAmount: 0
})

// 表单校验规则
const rules = {
  status: [
    { required: true, message: '请选择审核结果', trigger: 'change' }
  ],
  awardAmount: [
    { 
      required: true, 
      validator: (rule, value, callback) => {
        if (auditForm.status === 'approved' && (!value || value <= 0)) {
          callback(new Error('请输入有效的奖励金额'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  comment: [
    { required: true, message: '请输入审核意见', trigger: 'blur' },
    { min: 5, max: 500, message: '审核意见长度应在5到500个字符之间', trigger: 'blur' }
  ]
}

// 是否可以提交审核
const canSubmit = computed(() => {
  return achievement.value && achievement.value.status === 'pending'
})

// 获取成果奖励详情
const getAchievementDetail = async () => {
  loading.value = true
  try {
    const response = await getAchievementById(achievementId)
    if (response.code === 200) {
      achievement.value = response.data || {}
      
      // 如果成果不是待审核状态，提示用户
      if (achievement.value.status !== 'pending') {
        ElMessage.warning('该成果已经审核过，无法再次审核')
      }
    } else {
      ElMessage.error(response.message || '获取成果奖励详情失败')
    }
  } catch (error) {
    console.error('获取成果奖励详情出错:', error)
    ElMessage.error('获取成果奖励详情失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 提交审核
const submitAudit = async () => {
  if (!auditFormRef.value) return
  
  await auditFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      if (achievement.value.status !== 'pending') {
        ElMessage.warning('该成果已经审核过，无法再次审核')
        return
      }
      
      // 如果是批准状态，检查奖励金额
      if (auditForm.status === 'approved' && (!auditForm.awardAmount || auditForm.awardAmount <= 0)) {
        ElMessage.error('批准成果时必须设置有效的奖励金额')
        return
      }
      
      submitLoading.value = true
      try {
        const response = await auditAchievement(achievementId, {
          status: auditForm.status,
          comment: auditForm.comment,
          awardAmount: auditForm.awardAmount
        })
        
        if (response.code === 200) {
          ElMessage.success('审核成功')
          // 跳转回详情页
          router.push(`/achievement/detail/${achievementId}`)
        } else {
          ElMessage.error(response.message || '审核失败')
        }
      } catch (error) {
        console.error('提交审核出错:', error)
        ElMessage.error('审核失败，请稍后再试')
      } finally {
        submitLoading.value = false
      }
    } else {
      ElMessage.error('请完善审核信息')
    }
  })
}

// 返回列表页
const goBack = () => {
  router.push('/achievement/list')
}

// 下载附件
const downloadFile = (file) => {
  if (!file.path) {
    ElMessage.warning('附件路径不存在')
    return
  }
  
  // 创建下载链接
  const link = document.createElement('a')
  link.href = file.path
  link.target = '_blank'
  link.download = file.name
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
}

// 获取类型标签
const getTypeLabel = (type) => {
  if (!type) return '-'
  const typeMap = {
    journal: '期刊论文',
    conference: '会议论文',
    patent: '专利',
    book: '著作',
    software: '软件著作权',
    other: '其他'
  }
  return typeMap[type] || '未知'
}

// 获取级别标签
const getLevelLabel = (level) => {
  if (!level) return '-'
  const levelMap = {
    national: '国家级',
    provincial: '省部级',
    departmental: '厅局级',
    school: '校级',
    other: '其他'
  }
  return levelMap[level] || '未知'
}

// 格式化作者
const formatAuthors = (authors) => {
  if (!authors) return '-'
  if (typeof authors === 'string') return authors
  
  if (Array.isArray(authors)) {
    return authors.join(', ')
  }
  
  return authors.toString()
}

// 格式化货币
const formatCurrency = (amount) => {
  if (!amount && amount !== 0) return '-'
  return `¥${Number(amount).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`
}

// 初始化
onMounted(() => {
  getAchievementDetail()
})
</script>

<style scoped>
.achievement-audit-container {
  padding: 20px;
}

.operation-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.detail-card {
  margin-bottom: 20px;
}

.mt-20 {
  margin-top: 20px;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.card-header .title {
  font-size: 16px;
  font-weight: bold;
}

.detail-content {
  display: flex;
  flex-wrap: wrap;
}

.detail-item {
  width: 50%;
  margin-bottom: 15px;
  display: flex;
}

.detail-item.full-width {
  width: 100%;
  flex-direction: column;
}

.detail-item .label {
  color: #606266;
  width: 80px;
  text-align: right;
  padding-right: 12px;
  font-weight: bold;
}

.detail-item .value {
  color: #303133;
  flex: 1;
}

.description, .remarks {
  background-color: #f8f8f8;
  padding: 10px;
  border-radius: 4px;
  margin-top: 8px;
  color: #303133;
  line-height: 1.5;
  white-space: pre-wrap;
}

.audit-form {
  padding: 10px 0;
}

.attachment-list {
  padding: 0 10px;
}

.attachment-item {
  display: flex;
  align-items: center;
  padding: 8px 0;
  border-bottom: 1px solid #ebeef5;
}

.attachment-item:last-child {
  border-bottom: none;
}

.attachment-item .el-icon {
  margin-right: 8px;
  color: #409EFF;
}

.attachment-name {
  flex: 1;
  font-size: 14px;
  color: #606266;
}
</style> 