<template>
  <div class="expense-audit-list-container">
    <div class="page-header">
      <div class="page-title">经费审核列表</div>
      <div class="right">
        <el-button type="primary" @click="refreshList">刷新列表</el-button>
      </div>
    </div>
    
    <!-- 搜索表单 -->
    <el-card class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="申请标题">
          <el-input v-model="searchForm.title" placeholder="请输入申请标题" clearable />
        </el-form-item>
        <el-form-item label="申请类型">
          <el-select v-model="searchForm.type" placeholder="全部类型" clearable style="width: 150px;">
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
        <el-form-item label="申请状态">
          <el-select v-model="searchForm.status" placeholder="全部状态" clearable style="width: 120px;">
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
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 数据表格 -->
    <el-card class="table-card" v-loading="tableLoading">
      <el-table :data="expenseList" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="title" label="申请标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="projectName" label="所属项目" min-width="180" show-overflow-tooltip />
        <el-table-column label="申请类型" align="center" width="120">
          <template #default="scope">
            <el-tag :type="getExpenseTypeTag(scope.row.type)">{{ getExpenseTypeLabel(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="申请金额" width="120" align="right">
          <template #default="scope">
            {{ formatCurrency(scope.row.amount) }}
          </template>
        </el-table-column>
        <el-table-column prop="applyUserName" label="申请人" width="120" />
        <el-table-column prop="applyDate" label="申请日期" width="120" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="scope">
            <el-button 
              type="primary" 
              link 
              @click="handleView(scope.row)"
            >查看</el-button>
            <el-button 
              type="success" 
              link 
              @click="handleAudit(scope.row)"
              v-if="scope.row.status === 'pending'"
            >审核</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.pageNum"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
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
        <el-form-item label="所属项目">
          <el-text>{{ auditForm.projectName }}</el-text>
        </el-form-item>
        <el-form-item label="申请类型">
          <el-tag :type="getExpenseTypeTag(auditForm.type)">{{ getExpenseTypeLabel(auditForm.type) }}</el-tag>
        </el-form-item>
        <el-form-item label="申请金额">
          <el-text>{{ formatCurrency(auditForm.amount) }}</el-text>
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
          <el-button type="primary" @click="submitAudit" :loading="submitLoading">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { getExpenseList, auditExpense } from '@/api/expense'

const router = useRouter()
const tableLoading = ref(false)
const submitLoading = ref(false)
const expenseList = ref([])

// 搜索表单
const searchForm = reactive({
  title: '',
  type: '',
  status: ''
})

// 分页
const pagination = reactive({
  pageNum: 1,
  pageSize: 10,
  total: 0
})

// 审核对话框
const auditDialogVisible = ref(false)
const auditForm = reactive({
  id: '',
  title: '',
  projectName: '',
  type: '',
  amount: 0,
  result: 'approved',
  comment: ''
})

// 获取经费申请列表
const getExpenseListData = async () => {
  tableLoading.value = true
  
  try {
    const params = {
      pageNum: pagination.pageNum,
      pageSize: pagination.pageSize,
      title: searchForm.title || undefined,
      type: searchForm.type || undefined,
      status: searchForm.status || undefined
    }
    
    const response = await getExpenseList(params)
    
    if (response.code === 200) {
      expenseList.value = response.data.list || []
      pagination.total = response.data.total || 0
    } else {
      ElMessage.error(response.msg || '获取经费申请列表失败')
    }
  } catch (error) {
    console.error('获取经费申请列表失败:', error)
    ElMessage.error('获取经费申请列表失败，请刷新页面重试')
  } finally {
    tableLoading.value = false
  }
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
    'repayment_pending': '待还款',
    'completed': '已完成',
    'repaid': '已还款'
  }
  return statuses[status] || '未知状态'
}

// 搜索
const handleSearch = () => {
  pagination.pageNum = 1
  getExpenseListData()
}

// 重置搜索
const resetSearch = () => {
  // 重置表单
  searchForm.title = ''
  searchForm.type = ''
  searchForm.status = ''
  
  // 重新加载数据
  handleSearch()
}

// 查看详情
const handleView = (row) => {
  console.log('审计页面点击查看经费申请详情，ID:', row.id, '，详情:', row)
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

// 审核
const handleAudit = (row) => {
  auditForm.id = row.id
  auditForm.title = row.title
  auditForm.projectName = row.projectName
  auditForm.type = row.type
  auditForm.amount = row.amount
  auditForm.result = 'approved'
  auditForm.comment = ''
  
  auditDialogVisible.value = true
}

// 提交审核
const submitAudit = async () => {
  if (!auditForm.comment) {
    ElMessage.warning('请输入审核意见')
    return
  }
  
  submitLoading.value = true
  
  try {
    const response = await auditExpense(
      auditForm.id, 
      auditForm.result, 
      auditForm.comment
    )
    
    if (response.code === 200) {
      ElMessage.success('审核提交成功')
      auditDialogVisible.value = false
      
      // 刷新经费申请列表
      getExpenseListData()
    } else {
      ElMessage.error(response.msg || '审核失败')
    }
  } catch (error) {
    console.error('提交审核失败:', error)
    ElMessage.error('审核失败: ' + (error.message || '未知错误'))
  } finally {
    submitLoading.value = false
  }
}

// 刷新列表
const refreshList = () => {
  getExpenseListData()
}

// 处理每页大小变化
const handleSizeChange = (size) => {
  pagination.pageSize = size
  getExpenseListData()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  pagination.pageNum = page
  getExpenseListData()
}

// 页面加载时获取数据
onMounted(() => {
  getExpenseListData()
})
</script>

<style scoped>
.expense-audit-list-container {
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

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 