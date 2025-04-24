<template>
  <div class="expense-apply-list-container">
    <div class="page-header">
      <div class="page-title">经费申请列表</div>
      <el-button type="primary" @click="handleAddExpenseApply" v-if="userStore.roles.includes('ROLE_RESEARCHER')">新建经费申请</el-button>
    </div>
    
    <!-- 搜索和筛选区域 -->
    <el-card class="filter-container">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="申请标题">
          <el-input v-model="queryParams.title" placeholder="请输入申请标题" clearable style="min-width: 180px;" />
        </el-form-item>
        
        <el-form-item label="申请类别">
          <el-select v-model="queryParams.category" clearable placeholder="请选择申请类别" style="min-width: 180px;">
            <el-option label="经费借款" value="advance" />
            <el-option label="报销" value="reimbursement" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="所属项目">
          <el-select v-model="queryParams.projectId" clearable placeholder="请选择所属项目" style="min-width: 180px;">
            <el-option 
              v-for="item in projectOptions" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="申请类型">
          <el-select v-model="queryParams.type" clearable placeholder="请选择申请类型" style="min-width: 180px;">
            <el-option label="设备费" value="equipment" />
            <el-option label="材料费" value="material" />
            <el-option label="测试化验费" value="test" />
            <el-option label="差旅费" value="travel" />
            <el-option label="会议费" value="meeting" />
            <el-option label="劳务费" value="labor" />
            <el-option label="专家咨询费" value="consultation" />
            <el-option label="其他费用" value="other" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="审核状态">
          <el-select v-model="queryParams.status" clearable placeholder="请选择审核状态" style="min-width: 180px;">
            <el-option label="待审核" value="pending" />
            <el-option label="已批准" value="approved" />
            <el-option label="已拒绝" value="rejected" />
            <el-option label="已支付" value="paid" />
            <el-option label="待提交凭证" value="receipt_pending" />
            <el-option label="待还款" value="repayment_pending" />
            <el-option label="已完成" value="completed" />
            <el-option label="已还款" value="repaid" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 经费申请列表 -->
    <el-card class="table-container">
      <el-table
        v-loading="loading"
        :data="expenseApplyList"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column prop="title" label="申请标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="category" label="申请类别" min-width="100">
          <template #default="scope">
            <el-tag :type="getCategoryTag(scope.row.category)">{{ getCategoryLabel(scope.row.category) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="projectName" label="所属项目" min-width="180" show-overflow-tooltip />
        <el-table-column prop="type" label="申请类型" min-width="120">
          <template #default="scope">
            <el-tag :type="getExpenseTypeTag(scope.row.type)">{{ getExpenseTypeLabel(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="申请金额" min-width="120">
          <template #default="scope">
            {{ formatCurrency(scope.row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="applyDate" label="申请日期" min-width="120" />
        <el-table-column prop="status" label="审核状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleViewDetail(scope.row)">查看</el-button>
            <el-button link type="primary" @click="handleEdit(scope.row)" 
              v-if="scope.row.status === 'pending' && scope.row.applyUserId === userStore.userId">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)" 
              v-if="scope.row.status === 'pending' && scope.row.applyUserId === userStore.userId">删除</el-button>
            <el-button link type="success" @click="handleAudit(scope.row)" 
              v-if="userStore.roles.includes('ROLE_ADMIN') && scope.row.status === 'pending'">审核</el-button>
            <el-button link type="warning" @click="handleSubmitReceipt(scope.row)" 
              v-if="scope.row.status === 'receipt_pending' && scope.row.applyUserId === userStore.userId">提交凭证</el-button>
            <el-button link type="danger" @click="handleRepay(scope.row)" 
              v-if="scope.row.status === 'repayment_pending' && scope.row.applyUserId === userStore.userId">还款</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
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
      <el-form-item label="审核意见">
        <el-input
          v-model="auditForm.comment"
          type="textarea"
          :rows="4"
          placeholder="请输入审核意见"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <span class="dialog-footer">
        <el-button @click="auditDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAudit">提交</el-button>
      </span>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { getMyProjects } from '@/api/project'
import { getExpenseList, getMyExpenseList, deleteExpense, auditExpense } from '@/api/expense'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const total = ref(0)
const projectOptions = ref([]) // 项目选项列表

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
  projectId: '',
  type: '',
  status: '',
  category: ''
})

// 经费申请列表数据
const expenseApplyList = ref([])

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
    'receipt_pending': 'warning',
    'approved': 'success',
    'rejected': 'danger',
    'paid': 'success',
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

// 格式化金额
const formatCurrency = (value) => {
  if (!value) return '¥0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2
  }).format(value).replace('CN¥', '¥')
}

// 执行查询
const handleQuery = () => {
  getExpenseApplies()
}

// 重置查询
const resetQuery = () => {
  Object.assign(queryParams, {
    pageNum: 1,
    pageSize: 10,
    title: '',
    projectId: '',
    type: '',
    status: '',
    category: ''
  })
  handleQuery()
}

// 处理分页
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  handleQuery()
}

const handleCurrentChange = (page) => {
  queryParams.pageNum = page
  handleQuery()
}

// 查看经费申请详情
const handleViewDetail = (row) => {
  console.log('点击查看经费申请详情，ID:', row.id, '，详情:', row)
  if (!row.id) {
    ElMessage.error('无效的经费申请ID，无法查看详情')
    return
  }
  try {
    router.push(`/expense/apply/detail/${row.id}`)
  } catch (error) {
    console.error('路由跳转失败:', error)
    ElMessage.error('页面跳转失败，请重试')
  }
}

// 编辑经费申请
const handleEdit = (row) => {
  router.push(`/expense/apply/edit/${row.id}`)
}

// 删除经费申请
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除申请 "${row.title}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteExpense(row.id);
      // 刷新列表
      getExpenseApplies()
    } catch (error) {
      console.error('删除经费申请失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }).catch(() => {})
}

// 新建经费申请
const handleAddExpenseApply = () => {
  router.push('/expense/apply/create')
}

// 审核对话框
const auditDialogVisible = ref(false)
const auditForm = ref({
  id: null,
  title: '',
  projectName: '',
  type: '',
  amount: 0,
  reason: '',
  result: 'approved',
  comment: '',
  category: ''
})

// 审核经费申请
const handleAudit = (row) => {
  auditForm.value = {
    id: row.id,
    title: row.title,
    projectName: row.projectName,
    type: row.type,
    amount: row.amount,
    reason: row.reason,
    result: 'approved',
    comment: '',
    category: row.category
  }
  auditDialogVisible.value = true
}

// 提交审核
const submitAudit = async () => {
  if (!auditForm.value.comment) {
    ElMessage.warning('请输入审核意见')
    return
  }
  
  try {
    await auditExpense(
      auditForm.value.id, 
      auditForm.value.result, 
      auditForm.value.comment
    );
    
    // 刷新列表
    getExpenseApplies()
  } catch (error) {
    console.error('提交审核失败:', error)
    ElMessage.error('审核失败: ' + (error.message || '未知错误'))
  }
}

// 获取项目选项
const getProjectList = async () => {
  try {
    // 分页参数，设置大一点的页码确保获取所有项目
    const pageParams = {
          pageNum: 1,
          pageSize: 1000 // 设置足够大的每页数量
        }
    const response = await getMyProjects(pageParams);
    if (response.code === 200) {
      projectOptions.value = response.data.list || [];
    } else {
      ElMessage.warning('获取项目列表失败: ' + response.msg);
      projectOptions.value = [];
    }
  } catch (error) {
    console.error('获取项目列表失败:', error);
    ElMessage.error('获取项目列表失败，请刷新页面重试');
    projectOptions.value = [];
  }
}

// 获取经费申请列表
const getExpenseApplies = async () => {
  loading.value = true
  
  try {
    // 构造查询参数
    const params = {
      title: queryParams.title || undefined,
      projectId: queryParams.projectId || undefined,
      type: queryParams.type || undefined,
      status: queryParams.status || undefined,
      pageNum: queryParams.pageNum,
      pageSize: queryParams.pageSize
    }
    
    // 根据用户角色调用不同API
    let response
    if (userStore.roles.includes('ROLE_ADMIN')) {
      response = await getExpenseList(params)
    } else {
      response = await getMyExpenseList(params)
    }
    
    // 处理响应数据
    if (response.code === 200 && response.data) {
      expenseApplyList.value = response.data.list || []
      total.value = response.data.total || 0
    } else {
      ElMessage.warning('获取经费申请数据格式不正确')
      expenseApplyList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取经费申请列表失败:', error)
    ElMessage.error('获取经费申请列表失败: ' + (error.message || '未知错误'))
    expenseApplyList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 提交报销凭证
const handleSubmitReceipt = (row) => {
  router.push(`/expense/apply/detail/${row.id}`)
}

// 处理还款
const handleRepay = (row) => {
  router.push(`/expense/apply/detail/${row.id}`)
}

// 页面加载时获取数据
onMounted(() => {
  getProjectList() // 获取项目选项
  getExpenseApplies() // 获取经费申请列表
})
</script>

<style scoped>
.expense-apply-list-container {
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

.filter-container {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
}

.filter-form :deep(.el-form-item) {
  margin-right: 20px;
  margin-bottom: 15px;
}

.filter-form :deep(.el-select) {
  width: 100%;
}

.table-container {
  margin-bottom: 20px;
}

.pagination-container {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
}
</style> 