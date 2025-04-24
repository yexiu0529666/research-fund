<template>
  <div class="app-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-page-header @back="goBack" :title="loading ? '加载中...' : '返回列表'">
        <template #extra>
          <el-button 
            v-if="canEdit" 
            type="primary" 
            size="small" 
            @click="handleEdit"
            :loading="loading"
          >编辑</el-button>
          <el-button 
            v-if="canAudit" 
            type="success" 
            size="small" 
            @click="handleAudit"
            :loading="loading"
          >审核</el-button>
        </template>
      </el-page-header>
    </div>

    <!-- 内容区域 -->
    <el-row :gutter="20" v-loading="loading">
      <el-col :span="16">
        <!-- 申请详情卡片 -->
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>申请详情</span>
            </div>
          </template>
          <div v-if="transferDetail.id">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="标题">{{ transferDetail.title }}</el-descriptions-item>
              <el-descriptions-item label="项目名称">{{ transferDetail.projectName }}</el-descriptions-item>
              <el-descriptions-item label="结转金额">{{ formatCurrency(transferDetail.amount) }}</el-descriptions-item>
              <el-descriptions-item label="申请日期">{{ formatDateTime(transferDetail.applyDate) }}</el-descriptions-item>
              <el-descriptions-item label="申请人">{{ transferDetail.applyUserName }}</el-descriptions-item>
              <el-descriptions-item label="结转年度">
                <span v-if="transferDetail.fromYear && transferDetail.toYear">
                  {{ transferDetail.fromYear }} → {{ transferDetail.toYear }}
                </span>
                <span v-else>未设置</span>
              </el-descriptions-item>
              <el-descriptions-item label="结转原因" :span="2">{{ transferDetail.reason }}</el-descriptions-item>
              <el-descriptions-item label="备注" :span="2">{{ transferDetail.remark || '无' }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <div v-else class="empty-data">暂无申请详情</div>
        </el-card>

        <!-- 审核信息卡片 -->
        <el-card class="box-card" v-if="transferDetail.status !== 'pending'">
          <template #header>
            <div class="card-header">
              <span>审核信息</span>
            </div>
          </template>
          <div v-if="transferDetail.auditUserId">
            <el-descriptions :column="2" border>
              <el-descriptions-item label="审核状态">
                <el-tag :type="getStatusType(transferDetail.status)">{{ getStatusLabel(transferDetail.status) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="审核时间">{{ formatDateTime(transferDetail.auditTime) }}</el-descriptions-item>
              <el-descriptions-item label="审核人">{{ transferDetail.auditUserName }}</el-descriptions-item>
              <el-descriptions-item label="审核意见" :span="2">{{ transferDetail.auditComment || '无' }}</el-descriptions-item>
            </el-descriptions>
          </div>
          <div v-else class="empty-data">暂无审核信息</div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <!-- 流程时间线卡片 -->
        <el-card class="box-card">
          <template #header>
            <div class="card-header">
              <span>申请流程</span>
            </div>
          </template>
          <div class="timeline-container">
            <el-timeline>
              <el-timeline-item
                v-for="(activity, index) in activities"
                :key="index"
                :type="activity.type"
                :color="activity.color"
                :timestamp="activity.timestamp"
              >
                {{ activity.content }}
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 审核对话框 -->
    <el-dialog
      title="审核申请"
      v-model="auditDialogVisible"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="auditFormRef"
        :model="auditForm"
        :rules="auditRules"
        label-width="80px"
      >
        <el-form-item label="审核结果" prop="status">
          <el-radio-group v-model="auditForm.status">
            <el-radio label="approved">批准</el-radio>
            <el-radio label="rejected">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="审核意见" prop="comment">
          <el-input
            v-model="auditForm.comment"
            type="textarea"
            rows="4"
            placeholder="请输入审核意见"
          ></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="auditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitAudit" :loading="submitting">
            提交
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { getTransferDetail, auditTransfer } from '@/api/transfer'
import { formatDateTime } from '@/utils/date'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { reactive, ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/store/modules/user'

export default {
  name: 'TransferDetail',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const userStore = useUserStore()

    // 响应式数据
    const loading = ref(false)
    const submitting = ref(false)
    const auditDialogVisible = ref(false)
    const auditFormRef = ref(null)
    const transferDetail = ref({})
    
    // 审核表单
    const auditForm = reactive({
      status: 'approved',
      comment: ''
    })
    
    // 表单验证规则
    const auditRules = {
      status: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
      comment: [{ required: true, message: '请输入审核意见', trigger: 'blur' }]
    }
    
    // 流程活动
    const activities = ref([])

    // 计算属性: 是否可以编辑
    const canEdit = computed(() => {
      if (!transferDetail.value.id) return false
      if (!userStore) return false // 处理userStore可能未注入的情况
      const userId = userStore.userId || userStore.id // 兼容不同的用户ID字段名
      if (!userId) return false
      // 只有申请人且状态为待审核时可以编辑
      return transferDetail.value.status === 'pending' && 
             transferDetail.value.applyUserId === userId
    })

    // 计算属性: 是否可以审核
    const canAudit = computed(() => {
      return transferDetail.value.status === 'pending' && userStore.isAdmin
    })

    // 获取详情
    const getDetail = async () => {
      const id = route.params.id
      if (!id) {
        ElMessage.error('参数错误')
        return
      }
      
      loading.value = true
      try {
        const { data } = await getTransferDetail(id)
        console.log('获取结转详情成功:', data)
        
        // 处理结转年度显示
        if (data && data.fromYear && data.toYear) {
          data.transferYears = `${data.fromYear}-${data.toYear}`
        } else {
          data.transferYears = '未设置'
        }
        
        transferDetail.value = data
        generateActivities()
      } catch (error) {
        console.error('获取结转申请详情失败', error)
        ElMessage.error('获取结转申请详情失败')
      } finally {
        loading.value = false
      }
    }

    // 生成活动时间线
    const generateActivities = () => {
      activities.value = []
      
      // 申请创建
      activities.value.push({
        content: `${transferDetail.value.applyUserName} 创建了结转申请`,
        timestamp: formatDateTime(transferDetail.value.createTime),
        type: 'primary',
        color: '#0bbd87'
      })
      
      // 审核记录
      if (transferDetail.value.auditUserId) {
        const type = transferDetail.value.status === 'approved' ? 'success' : 'danger'
        const color = transferDetail.value.status === 'approved' ? '#0bbd87' : '#ff4949'
        const statusText = getStatusLabel(transferDetail.value.status)
        
        activities.value.push({
          content: `${transferDetail.value.auditUserName} ${statusText}了申请`,
          timestamp: formatDateTime(transferDetail.value.auditTime),
          type,
          color
        })
      }
    }

    // 获取状态标签
    const getStatusLabel = (status) => {
      const statusMap = {
        pending: '待审核',
        approved: '已批准',
        rejected: '已拒绝'
      }
      return statusMap[status] || status
    }

    // 获取状态类型
    const getStatusType = (status) => {
      const typeMap = {
        pending: 'warning',
        approved: 'success',
        rejected: 'danger'
      }
      return typeMap[status] || 'info'
    }

    // 返回列表
    const goBack = () => {
      if (userStore.isAdmin) {
        router.back()
      } else {
        router.push('/expense/transfer/list')
      }
    }

    // 编辑操作
    const handleEdit = () => {
      router.push(`/expense/transfer/edit/${transferDetail.value.id}`)
    }

    // 审核操作
    const handleAudit = () => {
      auditForm.status = 'approved'
      auditForm.comment = ''
      auditDialogVisible.value = true
    }

    // 提交审核
    const submitAudit = () => {
      auditFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        submitting.value = true
        try {
          await auditTransfer(
            transferDetail.value.id,
            auditForm.status,
            auditForm.comment
          )
          
          ElMessage.success('审核提交成功')
          auditDialogVisible.value = false
          
          // 重新获取详情
          await getDetail()
        } catch (error) {
          console.error('审核提交失败', error)
          // 检查是否为剩余经费不足的错误
          if (error.message && error.message.includes('剩余经费不足')) {
            ElMessage.error({
              message: error.message,
              duration: 5000,
              showClose: true
            })
          } else {
            ElMessage.error('审核提交失败: ' + (error.message || '未知错误'))
          }
        } finally {
          submitting.value = false
        }
      })
    }

    // 格式化货币
    const formatCurrency = (value) => {
      if (!value && value !== 0) return '¥0.00'
      return '¥' + Number(value).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    }

    // 初始化
    onMounted(() => {
      getDetail()
    })

    return {
      loading,
      transferDetail,
      activities,
      auditDialogVisible,
      auditForm,
      auditRules,
      auditFormRef,
      submitting,
      canEdit,
      canAudit,
      goBack,
      handleEdit,
      handleAudit,
      submitAudit,
      getStatusLabel,
      getStatusType,
      formatDateTime,
      formatCurrency
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  background: #fff;
  padding: 15px;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-data {
  text-align: center;
  color: #909399;
  padding: 30px 0;
}

.timeline-container {
  padding: 10px;
  max-height: 500px;
  overflow-y: auto;
}

.dialog-footer {
  text-align: right;
}
</style> 