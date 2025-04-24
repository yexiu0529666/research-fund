<template>
  <div class="achievement-detail-container">
    <!-- 操作按钮 -->
    <div class="operation-bar">
      <el-button @click="goBack">
        <el-icon><Back /></el-icon> 返回列表
      </el-button>
      <div class="action-buttons">
        <el-button 
          type="primary" 
          @click="handleEdit"
          v-if="canEdit"
        >
          <el-icon><Edit /></el-icon> 编辑
        </el-button>
        <el-button
          type="success"
          @click="handlePay"
          v-if="canPay"
        >
          <el-icon><Money /></el-icon> 支付
        </el-button>
      </div>
    </div>

    <el-row :gutter="20">
      <!-- 基本信息 -->
      <el-col :span="16">
        <el-card shadow="hover" class="detail-card">
          <template #header>
            <div class="card-header">
              <span class="title">成果基本信息</span>
              <el-tag :type="statusType" size="small">{{ statusLabel }}</el-tag>
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

      <!-- 审核信息和创建信息 -->
      <el-col :span="8">
        <el-card shadow="hover" class="detail-card">
          <template #header>
            <div class="card-header">
              <span class="title">状态信息</span>
            </div>
          </template>
          <div class="detail-content" v-loading="loading">
            <div class="detail-item">
              <span class="label">创建人:</span>
              <span class="value">{{ achievement.creatorName || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">创建时间:</span>
              <span class="value">{{ formatDateTime(achievement.createTime) || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">审核人:</span>
              <span class="value">{{ achievement.auditUserName || '-' }}</span>
            </div>
            <div class="detail-item">
              <span class="label">审核时间:</span>
              <span class="value">{{ formatDateTime(achievement.auditTime) || '-' }}</span>
            </div>
            <div class="detail-item full-width">
              <span class="label">审核意见:</span>
              <div class="audit-comment">{{ achievement.auditComment || '-' }}</div>
            </div>
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

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="30%"
      :close-on-click-modal="false"
    >
      <span>确定要删除该条成果奖励记录吗？此操作不可撤销。</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete" :loading="deleteLoading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { Back, Edit, Check, Delete, Document, Money } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAchievementById, deleteAchievement, payAchievement } from '@/api/achievement'
import { useUserStore } from '@/store/modules/user'
import { useRouter, useRoute } from 'vue-router'
import { formatDateTime } from '@/utils/date'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const achievementId = route.params.id

// 加载状态
const loading = ref(false)
// 成果奖励数据
const achievement = ref({})
// 删除对话框控制
const deleteDialogVisible = ref(false)
const deleteLoading = ref(false)

// 计算状态标签类型
const statusType = computed(() => {
  const statusMap = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    paid: 'success'
  }
  return statusMap[achievement.value.status] || 'info'
})

// 计算状态标签文本
const statusLabel = computed(() => {
  const statusMap = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝',
    paid: '已支付'
  }
  return statusMap[achievement.value.status] || '未知'
})

// 计算是否可以编辑
const canEdit = computed(() => {
  return !userStore.isAdmin && achievement.value.status === 'pending'
})

// 计算是否可以支付
const canPay = computed(() => {
  return userStore.isAdmin && achievement.value.status === 'approved'
})

// 获取成果奖励详情
const getAchievementDetail = async () => {
  loading.value = true
  try {
    const response = await getAchievementById(achievementId)
    if (response.code === 200) {
      achievement.value = response.data || {}
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

// 返回列表页
const goBack = () => {
  router.push('/achievement/list')
}

// 编辑成果奖励
const handleEdit = () => {
  router.push(`/achievement/edit/${achievementId}`)
}

// 审核成果奖励
const handleAudit = () => {
  router.push(`/achievement/audit/${achievementId}`)
}

// 删除成果奖励
const handleDelete = () => {
  deleteDialogVisible.value = true
}

// 确认删除
const confirmDelete = async () => {
  deleteLoading.value = true
  try {
    const response = await deleteAchievement(achievementId)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      router.push('/achievement/list')
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    console.error('删除成果奖励出错:', error)
    ElMessage.error('删除失败，请稍后再试')
  } finally {
    deleteLoading.value = false
    deleteDialogVisible.value = false
  }
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

// 处理支付
const handlePay = () => {
  ElMessageBox.confirm('确定要支付该成果奖励吗？支付后状态将更新为已支付。', '支付确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      const response = await payAchievement(achievementId)
      if (response.code === 200) {
        ElMessage.success('支付成功')
        // 重新获取详情，刷新状态
        getAchievementDetail()
      } else {
        ElMessage.error(response.message || '支付失败')
      }
    } catch (error) {
      console.error('支付成果奖励出错:', error)
      ElMessage.error('支付失败，请稍后再试')
    }
  }).catch(() => {
    // 用户取消操作
  })
}

// 初始化
onMounted(() => {
  getAchievementDetail()
})
</script>

<style scoped>
.achievement-detail-container {
  padding: 20px;
}

.operation-bar {
  display: flex;
  justify-content: space-between;
  margin-bottom: 20px;
}

.action-buttons {
  display: flex;
  gap: 10px;
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

.description, .remarks, .audit-comment {
  background-color: #f8f8f8;
  padding: 10px;
  border-radius: 4px;
  margin-top: 8px;
  color: #303133;
  line-height: 1.5;
  white-space: pre-wrap;
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 