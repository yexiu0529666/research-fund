<template>
  <div class="expense-apply-detail-container">
    <div class="page-header">
      <div class="left">
        <el-button icon="Back" @click="goBack">返回</el-button>
        <div class="page-title">经费申请详情</div>
      </div>
      <div class="operate-buttons">
        <el-button 
          type="primary" 
          @click="handleEdit" 
          v-if="expenseDetail.status === 'pending' && expenseDetail.applyUserId === userStore.userId"
        >编辑申请</el-button>
        <el-button 
          type="success" 
          @click="handleAudit" 
          v-if="userStore.roles.includes('ROLE_ADMIN') && expenseDetail.status === 'pending'"
        >审核申请</el-button>
        <el-button 
          type="warning" 
          @click="handleSubmitReceipt" 
          v-if="expenseDetail.status === 'receipt_pending' && expenseDetail.applyUserId === userStore.userId"
        >提交报销凭证</el-button>
        <el-button 
          type="danger" 
          @click="handleRepay" 
          v-if="expenseDetail.status === 'repayment_pending' && expenseDetail.applyUserId === userStore.userId"
        >确认还款</el-button>
      </div>
    </div>
    
    <el-row :gutter="20" v-loading="loading">
      <el-col :xs="24" :sm="24" :md="16" :lg="18">
        <!-- 基本信息 -->
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <span>申请信息</span>
              <el-tag :type="getStatusTag(expenseDetail.status)">{{ getStatusLabel(expenseDetail.status) }}</el-tag>
            </div>
          </template>
          
          <el-descriptions :column="2" border>
            <el-descriptions-item label="申请标题" :span="2">{{ expenseDetail.title }}</el-descriptions-item>
            <el-descriptions-item label="申请类别">
              <el-tag :type="getCategoryTag(expenseDetail.category)">{{ getCategoryLabel(expenseDetail.category) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="所属项目">
              {{ expenseDetail.projectName }}
            </el-descriptions-item>
            <el-descriptions-item v-if="projectInfo" label="项目预算">
              {{ formatCurrency(projectInfo.budget) }}
            </el-descriptions-item>
            <el-descriptions-item v-if="projectInfo" label="已用预算">
              {{ formatCurrency(projectInfo.usedBudget) }}
            </el-descriptions-item>
            <el-descriptions-item v-if="projectInfo" label="剩余预算">
              {{ calculateRemainingBudget() }}
            </el-descriptions-item>
            <el-descriptions-item v-if="projectInfo" label="预算使用进度" :span="2">
              <el-progress 
                :percentage="budgetProgressPercentage" 
                :status="getBudgetProgressStatus(budgetProgressPercentage)"
                :format="percentFormat"
              />
            </el-descriptions-item>
            <el-descriptions-item label="申请类型">
              <el-tag :type="getExpenseTypeTag(expenseDetail.type)">{{ getExpenseTypeLabel(expenseDetail.type) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="申请金额">{{ formatCurrency(expenseDetail.amount) }}</el-descriptions-item>
            <el-descriptions-item label="申请日期">{{ expenseDetail.applyDate }}</el-descriptions-item>
            <el-descriptions-item label="申请人">{{ expenseDetail.applyUserName }}</el-descriptions-item>
            <el-descriptions-item label="当前状态">
              <el-tag :type="getStatusTag(expenseDetail.status)">{{ getStatusLabel(expenseDetail.status) }}</el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="预计用途" :span="2">{{ expenseDetail.purpose }}</el-descriptions-item>
            <el-descriptions-item label="申请理由" :span="2">{{ expenseDetail.reason }}</el-descriptions-item>
          </el-descriptions>
        </el-card>
        
        <!-- 附件信息 -->
        <el-card class="detail-card" v-if="(expenseDetail.category === 'reimbursement' || (expenseDetail.attachments && expenseDetail.attachments.length > 0))">
          <template #header>
            <div class="card-header">
              <span>申请附件</span>
            </div>
          </template>
          
          <div class="attachments-container">
            <div v-if="expenseDetail.attachments && expenseDetail.attachments.length > 0">
              <div class="attachment-item" v-for="(item, index) in expenseDetail.attachments" :key="index">
                <div class="attachment-icon">
                  <el-icon v-if="isImage(item.name)"><Picture /></el-icon>
                  <el-icon v-else-if="isPDF(item.name)"><Document /></el-icon>
                  <el-icon v-else-if="isExcel(item.name)"><Grid /></el-icon>
                  <el-icon v-else-if="isWord(item.name)"><Document /></el-icon>
                  <el-icon v-else><Folder /></el-icon>
                </div>
                <div class="attachment-info">
                  <div class="attachment-name">{{ item.name }}</div>
                  <div class="attachment-actions">
                    <el-button type="primary" link @click="downloadFile(item)">
                      <el-icon><Download /></el-icon> 下载
                    </el-button>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="no-attachments">
              <el-empty description="没有附件" :image-size="100"></el-empty>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="24" :md="8" :lg="6">
        <!-- 审核信息 -->
        <el-card class="detail-card" v-if="expenseDetail.status !== 'pending'">
          <template #header>
            <div class="card-header">
              <span>审核信息</span>
            </div>
          </template>
          
          <div class="audit-info">
            <div class="info-item">
              <div class="info-label">审核结果</div>
              <div class="info-value">
                <el-tag :type="getStatusTag(expenseDetail.status)">{{ getStatusLabel(expenseDetail.status) }}</el-tag>
              </div>
            </div>
            <div class="info-item">
              <div class="info-label">审核人</div>
              <div class="info-value">{{ expenseDetail.auditUserName || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">审核时间</div>
              <div class="info-value">{{ formatDateTime(expenseDetail.auditTime) || '-' }}</div>
            </div>
            <div class="info-item">
              <div class="info-label">审核意见</div>
              <div class="info-value comment">{{ expenseDetail.auditComment || '无' }}</div>
            </div>
          </div>
        </el-card>
        
        <!-- 流程记录 -->
        <el-card class="detail-card">
          <template #header>
            <div class="card-header">
              <span>流程记录</span>
            </div>
          </template>
          
          <el-timeline>
            <el-timeline-item
              v-for="(activity, index) in activities"
              :key="index"
              :timestamp="activity.timestamp"
              :type="activity.type"
              :color="activity.color"
            >
              {{ activity.content }}
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </el-col>
    </el-row>
  </div>
  
  <!-- 审核对话框 -->
  <el-dialog
    title="经费申请审核"
    v-model="auditDialogVisible"
    width="500px"
  >
    <el-form :model="auditForm" label-width="100px">
      <el-form-item label="申请标题">
        <el-text>{{ auditForm.title }}</el-text>
      </el-form-item>
      <el-form-item label="申请类别">
        <el-tag :type="getCategoryTag(auditForm.category)">{{ getCategoryLabel(auditForm.category) }}</el-tag>
      </el-form-item>
      <el-form-item label="所属项目">
        <el-text>{{ auditForm.projectName }}</el-text>
      </el-form-item>
      <el-form-item label="申请类型">
        <el-tag :type="getExpenseTypeTag(auditForm.type)">{{ getExpenseTypeLabel(auditForm.type) }}</el-tag>
      </el-form-item>
      <el-form-item label="申请金额">
        <el-text>{{ formatCurrency(auditForm.amount) }}</el-text>
      </el-form-item>
      <el-form-item label="申请理由">
        <el-text>{{ auditForm.reason }}</el-text>
      </el-form-item>
      <el-form-item label="审核结果">
        <el-radio-group v-model="auditForm.result">
          <el-radio label="approved">批准</el-radio>
          <el-radio label="rejected">拒绝</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">提交</el-button>
      </span>
    </template>
  </el-dialog>
  
  <!-- 添加上传报销凭证对话框 -->
  <el-dialog
    title="上传报销凭证"
    v-model="uploadDialogVisible"
    width="500px"
  >
    <div class="upload-container">
      <el-upload
        class="upload-drop"
        drag
        multiple
        :auto-upload="false"
        :on-change="handleFileChange"
        :file-list="fileList"
      >
        <el-icon class="el-icon--upload"><upload-filled /></el-icon>
        <div class="el-upload__text">
          拖拽文件到此处或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip">
            请上传报销凭证相关的文件，支持常见文档和图片格式。
          </div>
        </template>
      </el-upload>
    </div>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="uploadDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmUploadReceipt">提交</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted, watchEffect, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { Document, Download, Picture, Grid, Folder, UploadFilled } from '@element-plus/icons-vue'
import { getFileDownloadUrl } from '@/api/upload'
import { getExpenseDetail, auditExpense, submitReceipt, repayExpense, uploadAttachment } from '@/api/expense'
import { getProjectDetail } from '@/api/project'
import { formatDateTime } from '@/utils/date'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)

// 从路由参数获取经费申请ID
const expenseId = ref(null)

// 经费申请详情数据
const expenseDetail = ref({
  id: '',
  title: '',
  projectId: '',
  projectName: '',
  type: '',
  amount: 0,
  applyDate: '',
  purpose: '',
  reason: '',
  attachments: [],
  applyUserId: '',
  applyUserName: '',
  status: 'pending',
  auditUserId: '',
  auditUserName: '',
  auditTime: '',
  auditComment: '',
  category: ''
})

// 项目信息
const projectInfo = ref(null)

// 活动记录
const activities = ref([])

// 获取经费申请详情
const getExpenseData = async () => {
  if (!expenseId.value) {
    console.error('尝试获取经费详情时ID为空')
    ElMessage.error('无法获取经费申请详情：ID无效')
    return
  }

  loading.value = true
  console.log('开始加载经费申请详情，ID:', expenseId.value)
  
  try {
    const response = await getExpenseDetail(expenseId.value)
    
    if (response.code === 200) {
      console.log('成功获取经费申请详情:', response.data)
      expenseDetail.value = response.data
      
      // 获取项目信息
      if (expenseDetail.value.projectId) {
        try {
          const projectResponse = await getProjectDetail(expenseDetail.value.projectId)
          if (projectResponse.code === 200) {
            projectInfo.value = projectResponse.data
            console.log('项目预算信息:', {
              budget: projectInfo.value.budget,
              usedBudget: projectInfo.value.usedBudget,
              budgetType: typeof projectInfo.value.budget,
              usedBudgetType: typeof projectInfo.value.usedBudget
            })
          }
        } catch (error) {
          console.error('获取项目信息失败:', error)
        }
      }
      
      // 生成活动记录
      generateActivities()
    } else {
      console.error('获取经费申请详情失败:', response.msg)
      ElMessage.error(response.msg || '获取经费申请详情失败')
    }
  } catch (error) {
    console.error('获取经费申请详情失败:', error)
    ElMessage.error('获取经费申请详情失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 生成活动记录
const generateActivities = () => {
  const status = expenseDetail.value.status
  const category = expenseDetail.value.category
  
  activities.value = [
    {
      content: `${expenseDetail.value.applyUserName} 提交了${getCategoryLabel(category)}申请`,
      timestamp: expenseDetail.value.applyDate,
      type: 'primary',
      color: '#0bbd87'
    }
  ]
  
  if (status === 'approved' || status === 'paid' || status === 'receipt_pending' || 
      status === 'repayment_pending' || status === 'completed' || status === 'repaid') {
    activities.value.push({
      content: `${expenseDetail.value.auditUserName || '管理员'} 批准了${getCategoryLabel(category)}申请`,
      timestamp: formatDateTime(expenseDetail.value.auditTime) || formatDateTime(new Date()),
      type: 'success',
      color: '#0bbd87'
    })
  } else if (status === 'rejected') {
    activities.value.push({
      content: `${expenseDetail.value.auditUserName || '管理员'} 拒绝了${getCategoryLabel(category)}申请`,
      timestamp: formatDateTime(expenseDetail.value.auditTime) || formatDateTime(new Date()),
      type: 'danger',
      color: '#ff4949'
    })
  }
}

// 监听路由参数变化
watchEffect(() => {
  if (route.params && route.params.id) {
    console.log('获取路由参数ID:', route.params.id)
    expenseId.value = route.params.id
    // 当ID变化时自动加载数据
    if (expenseId.value) {
      getExpenseData()
    }
  } else {
    console.error('未能获取到有效的经费申请ID')
    ElMessage.error('无法获取经费申请ID，请返回列表重试')
  }
})

// 审核对话框
const auditDialogVisible = ref(false)
const auditForm = reactive({
  id: '',
  title: '',
  projectName: '',
  type: '',
  amount: 0,
  reason: '',
  result: 'approved',
  comment: '',
  category: ''
})

// 上传报销凭证相关变量
const uploadDialogVisible = ref(false)
const fileList = ref([])
const uploadedFiles = ref([])

// 处理文件变更
const handleFileChange = (uploadFile, uploadFiles) => {
  console.log('文件变更:', uploadFile, uploadFiles)
  fileList.value = uploadFiles
}

// 刷新详情
const refreshDetail = () => {
  getExpenseData()
}

// 获取经费申请类型标签样式
const getExpenseTypeTag = (type) => {
  const types = {
    equipment: 'success',
    material: 'info',
    test: 'warning',
    travel: 'danger',
    meeting: 'primary',
    labor: '',
    consultation: 'info',
    other: 'info'
  }
  return types[type] || 'info'
}

// 获取经费申请类型显示文本
const getExpenseTypeLabel = (type) => {
  const types = {
    equipment: '设备费',
    material: '材料费',
    test: '测试化验费',
    travel: '差旅费',
    meeting: '会议费',
    labor: '劳务费',
    consultation: '专家咨询费',
    other: '其他费用'
  }
  return types[type] || '未知类型'
}

// 获取状态标签样式
const getStatusTag = (status) => {
  const statuses = {
    'pending': 'info',
    'approved': 'success',
    'rejected': 'danger',
    'paid': 'warning',
    'receipt_pending': 'warning',
    'receipt_audit': 'info',
    'repayment_pending': 'danger',
    'completed': 'success',
    'repaid': 'success'
  }
  return statuses[status] || 'info'
}

// 获取状态显示文本
const getStatusLabel = (status) => {
  const statuses = {
    'pending': '待审核',
    'approved': '已批准',
    'rejected': '已拒绝',
    'paid': '已支付',
    'receipt_pending': '待提交报销凭证',
    'receipt_audit': '报销凭证待审核',
    'repayment_pending': '待还款',
    'completed': '已完成',
    'repaid': '已还款'
  }
  return statuses[status] || '未知状态'
}

// 获取申请类别标签样式
const getCategoryTag = (category) => {
  const categories = {
    advance: 'primary',
    reimbursement: 'success'
  }
  return categories[category] || 'info'
}

// 获取申请类别显示文本
const getCategoryLabel = (category) => {
  const categories = {
    advance: '经费借款',
    reimbursement: '报销'
  }
  return categories[category] || '未知类别'
}

// 格式化金额
const formatCurrency = (value) => {
  if (!value) return '¥0.00'
  
  // 确保数值是数字
  let numValue = value;
  if (typeof value === 'string') {
    numValue = Number(value.replace(/[^\d.-]/g, ''));
  }
  
  // 检查是否为有效数字
  if (isNaN(numValue)) {
    console.warn('formatCurrency: 无效的数值', value);
    return '¥0.00';
  }
  
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2
  }).format(numValue).replace('CN¥', '¥')
}

// 判断文件类型
const isImage = (filename) => {
  return /\.(jpg|jpeg|png|gif|bmp)$/i.test(filename)
}

const isPDF = (filename) => {
  return /\.pdf$/i.test(filename)
}

const isExcel = (filename) => {
  return /\.(xls|xlsx|csv)$/i.test(filename)
}

const isWord = (filename) => {
  return /\.(doc|docx)$/i.test(filename)
}

// 下载文件
const downloadFile = (file) => {
  if (!file.url) {
    ElMessage.warning('文件路径不存在，无法下载')
    return
  }
  
  try {
    // 构建下载文件名
    const fileName = file.name
    
    // 获取下载链接
    const downloadUrl = getFileDownloadUrl(file.url, fileName);
    
    // 通过创建a标签下载文件
    const link = document.createElement('a');
    link.href = downloadUrl;
    link.target = '_blank'; // 可以改为 '_self' 以在当前窗口下载
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    
    ElMessage.success('开始下载文件');
  } catch (error) {
    console.error('下载文件失败:', error);
    ElMessage.error('下载文件失败');
  }
}

// 编辑申请
const handleEdit = () => {
  router.push(`/expense/apply/edit/${expenseId.value}`)
}

// 返回列表
const goBack = () => {
  router.back()
}

// 审核申请
const handleAudit = () => {
  auditForm.id = expenseDetail.value.id
  auditForm.title = expenseDetail.value.title
  auditForm.projectName = expenseDetail.value.projectName
  auditForm.type = expenseDetail.value.type
  auditForm.amount = expenseDetail.value.amount
  auditForm.reason = expenseDetail.value.reason
  auditForm.result = 'approved'
  auditForm.comment = ''
  auditForm.category = expenseDetail.value.category
  
  auditDialogVisible.value = true
}

// 提交审核
const submitAudit = async () => {
  
  try {
    const response = await auditExpense(
      auditForm.id, 
      auditForm.result, 
      auditForm.comment
    )
    
    if (response.code === 200) {
      ElMessage.success('审核提交成功')
      auditDialogVisible.value = false
      
      // 刷新经费申请详情
      getExpenseData()
    } else {
      ElMessage.error(response.msg || '审核失败')
    }
  } catch (error) {
    console.error('提交审核失败:', error)
    ElMessage.error('审核失败: ' + (error.message || '未知错误'))
  }
}

// 页面加载时获取数据
onMounted(() => {
  console.log('经费申请详情页面已挂载，路由参数:', route.params)
  // 如果watchEffect未触发（在某些情况下可能发生），则在这里确保数据加载
  if (expenseId.value) {
    console.log('从onMounted调用数据加载')
    getExpenseData()
  }
})

// 添加预算进度相关方法和计算属性
// 计算预算使用百分比
const budgetProgressPercentage = computed(() => {
  if (!projectInfo.value) return 0;
  console.log('预算计算原始数据:', projectInfo.value.budget, projectInfo.value.usedBudget);
  
  // 使用Number确保将字符串转换为数字
  let budget = Number(projectInfo.value.budget);
  let usedBudget = Number(projectInfo.value.usedBudget);
  
  console.log('预算计算处理后数据 (Number):', budget, usedBudget);
  
  // 如果 Number 转换失败，尝试去掉逗号等格式
  if (isNaN(budget) && typeof projectInfo.value.budget === 'string') {
    budget = Number(projectInfo.value.budget.replace(/[^\d.-]/g, ''));
  }
  
  if (isNaN(usedBudget) && typeof projectInfo.value.usedBudget === 'string') {
    usedBudget = Number(projectInfo.value.usedBudget.replace(/[^\d.-]/g, ''));
  }
  
  console.log('预算计算清理后数据:', budget, usedBudget);
  
  if (isNaN(budget) || budget <= 0 || isNaN(usedBudget)) {
    console.warn('预算计算失败，返回0');
    return 0;
  }
  
  // 计算百分比并限制最大值为100
  const percentage = (usedBudget / budget) * 100;
  console.log('计算出的预算使用百分比:', percentage);
  const result = Math.min(Math.round(percentage * 10) / 10, 100);
  console.log('格式化后的预算使用百分比:', result);
  return result;
});

// 获取进度条状态
const getBudgetProgressStatus = (percentage) => {
  if (percentage < 80) return '';
  if (percentage < 100) return 'warning';
  return 'success';
};

// 格式化百分比显示
const percentFormat = (val) => {
  return `已使用 ${parseFloat(val).toFixed(1)}%`;
};

// 提交报销凭证
const handleSubmitReceipt = async () => {
  try {
    uploadDialogVisible.value = true
    fileList.value = []
    uploadedFiles.value = []
  } catch (error) {
    console.error('提交报销凭证失败:', error)
    ElMessage.error('提交报销凭证失败，请稍后重试')
  }
}

// 确认上传报销凭证
const confirmUploadReceipt = async () => {
  try {
    if (!fileList.value || fileList.value.length === 0) {
      ElMessage.warning('请先上传报销凭证文件')
      return
    }
    
    // 此处应将文件上传到服务器，获取文件URL
    // 简化处理，假设uploadAttachment已在expense.js中定义
    const attachments = []
    for (const file of fileList.value) {
      try {
        // 上传文件
        const uploadRes = await uploadAttachment(file.raw)
        if (uploadRes.code === 200) {
          attachments.push({
            name: file.name,
            url: uploadRes.data.url,
            size: file.size,
            type: file.type
          })
        } else {
          ElMessage.error(`文件${file.name}上传失败: ${uploadRes.msg}`)
        }
      } catch (uploadErr) {
        console.error('文件上传失败:', uploadErr)
        ElMessage.error(`文件${file.name}上传失败`)
      }
    }
    
    if (attachments.length === 0) {
      ElMessage.error('没有成功上传的文件，无法提交报销凭证')
      return
    }
    
    // 提交报销凭证
    const result = await submitReceipt(expenseDetail.value.id, attachments)
    if (result.code === 200) {
      ElMessage.success('报销凭证提交成功')
      uploadDialogVisible.value = false
      fileList.value = []
      refreshDetail()
    } else {
      ElMessage.error(result.msg || '报销凭证提交失败')
    }
  } catch (error) {
    console.error('提交报销凭证失败:', error)
    ElMessage.error('提交报销凭证失败，请稍后重试')
  }
}

// 执行还款操作
const handleRepay = async () => {
  try {
    await ElMessageBox.confirm(
      '确认已经完成还款操作?',
      '还款确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    const result = await repayExpense(expenseDetail.value.id)
    if (result.code === 200) {
      ElMessage.success('还款操作已确认')
      refreshDetail()
    } else {
      ElMessage.error(result.msg || '还款确认失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('还款确认失败:', error)
      ElMessage.error('还款确认失败，请稍后重试')
    }
  }
}

// 计算剩余预算
const calculateRemainingBudget = () => {
  if (!projectInfo.value) return '¥0.00'
  
  // 确保获取到数字
  const budget = Number(projectInfo.value.budget) || 0;
  const usedBudget = Number(projectInfo.value.usedBudget) || 0;
  
  console.log('剩余预算计算:', budget, usedBudget, budget - usedBudget);
  
  const remainingBudget = budget - usedBudget;
  return formatCurrency(remainingBudget);
}
</script>

<style scoped>
.expense-apply-detail-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header .left {
  display: flex;
  align-items: center;
}

.page-title {
  font-size: 22px;
  font-weight: bold;
  margin-left: 15px;
}

.detail-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.attachments-container {
  padding: 10px 0;
}

.attachment-item {
  display: flex;
  padding: 10px;
  border-bottom: 1px solid #ebeef5;
}

.attachment-item:last-child {
  border-bottom: none;
}

.attachment-icon {
  font-size: 24px;
  margin-right: 10px;
  color: #909399;
}

.attachment-info {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.attachment-name {
  font-size: 14px;
  color: #606266;
}

.audit-info {
  padding: 10px 0;
}

.info-item {
  margin-bottom: 15px;
}

.info-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.info-value {
  font-size: 14px;
  color: #606266;
}

.info-value.comment {
  white-space: pre-wrap;
  line-height: 1.5;
}

.el-timeline {
  padding-left: 0;
}

.upload-container {
  padding: 20px 0;
}

.upload-drop {
  width: 100%;
}
</style> 