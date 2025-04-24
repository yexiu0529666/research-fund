<template>
  <div class="task-list-container">
    <div class="page-header">
      <div class="page-title">任务管理</div>
      <div class="right">
        <el-button type="primary" @click="refreshList">刷新列表</el-button>
      </div>
    </div>
    
    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stat-cards">
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card" @click="() => { activeTab = 'project' }">
          <div class="stat-value">{{ pendingProjectCount }}</div>
          <div class="stat-label">项目待审核</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card" @click="() => { activeTab = 'expense' }">
          <div class="stat-value">{{ pendingExpenseCount }}</div>
          <div class="stat-label">经费支出待审核</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover" class="stat-card" @click="() => { activeTab = 'transfer' }">
          <div class="stat-value">{{ pendingTransferCount }}</div>
          <div class="stat-label">经费结转待审核</div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 任务列表 -->
    <el-tabs v-model="activeTab" class="task-tabs">
      <el-tab-pane label="项目任务" name="project">
        <el-card class="task-card" v-loading="loading.project">
          <!-- 筛选条件 -->
          <div class="filter-container">
            <el-select v-model="statusFilter" placeholder="审核状态" clearable style="width: 150px; margin-right: 15px;">
              <el-option label="全部" value="" />
              <el-option label="待审核" value="pending" />
              <el-option label="已通过" value="approved" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
            <el-button type="primary" @click="fetchProjectTasks">筛选</el-button>
            <el-button @click="resetFilter('project')">重置</el-button>
          </div>
          
          <div v-if="projectTasks.length === 0" class="empty-tip">
            暂无相关项目任务
          </div>
          <el-table v-else :data="projectTasks" border stripe style="width: 100%">
            <el-table-column prop="id" label="ID" min-width="60" align="center" />
            <el-table-column prop="name" label="项目名称" min-width="180" show-overflow-tooltip />
            <el-table-column prop="type" label="项目类型" min-width="100">
              <template #default="scope">
                <el-tag :type="getProjectTypeTag(scope.row.type)">{{ getProjectTypeLabel(scope.row.type) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="leaderName" label="负责人" min-width="100" />
            <el-table-column prop="auditStatus" label="审核状态" min-width="100">
              <template #default="scope">
                <el-tag :type="getAuditStatusTag(scope.row.auditStatus)">
                  {{ getAuditStatusLabel(scope.row.auditStatus) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="createTime" label="提交时间" min-width="150" />
            <el-table-column label="操作" min-width="150" fixed="right" align="center">
              <template #default="scope">
                <el-button 
                  type="primary" 
                  link 
                  @click="handleAuditProject(scope.row)" 
                  v-if="scope.row.auditStatus === 'pending'"
                >审核</el-button>
                <el-button 
                  type="primary" 
                  link 
                  @click="handleAuditProjectCompletion(scope.row)" 
                  v-if="scope.row.status === 'completion_review'"
                >审核结题</el-button>
                <el-button type="info" link @click="handleViewProject(scope.row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 添加分页组件 -->
          <div class="pagination-container" v-if="projectTasks.length > 0">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="pagination.project.pageSize"
              :current-page="pagination.project.pageNum"
              :total="pagination.project.total"
              @size-change="(size) => handleSizeChange('project', size)"
              @current-change="(page) => handleCurrentChange('project', page)"
            />
          </div>
        </el-card>
      </el-tab-pane>
      
      <el-tab-pane label="经费支出任务" name="expense">
        <el-card class="task-card" v-loading="loading.expense">
          <!-- 筛选条件 -->
          <div class="filter-container">
            <el-select v-model="statusFilter" placeholder="审核状态" clearable style="width: 150px; margin-right: 15px;">
              <el-option label="全部" value="" />
              <el-option label="待审核" value="pending" />
              <el-option label="已批准" value="approved" />
              <el-option label="已拒绝" value="rejected" />
              <el-option label="已支付" value="paid" />
            </el-select>
            <el-select v-model="categoryFilter" placeholder="申请类别" clearable style="width: 150px; margin-right: 15px;">
              <el-option label="全部" value="" />
              <el-option label="经费借款" value="advance" />
              <el-option label="报销" value="reimbursement" />
            </el-select>
            <el-select v-model="projectFilter" placeholder="所属项目" clearable style="width: 150px; margin-right: 15px;">
              <el-option label="全部" value="" />
              <el-option v-for="project in projectOptions" :key="project.id" :label="project.name" :value="project.id" />
            </el-select>
            <el-button type="primary" @click="fetchExpenseTasks">筛选</el-button>
            <el-button @click="resetFilter('expense')">重置</el-button>
          </div>
          
          <div v-if="expenseTasks.length === 0" class="empty-tip">
            暂无相关经费支出任务
          </div>
          <el-table v-else :data="expenseTasks" border stripe style="width: 100%">
            <el-table-column prop="id" label="ID" min-width="60" align="center" />
            <el-table-column prop="title" label="申请标题" min-width="180" show-overflow-tooltip />
            <el-table-column prop="projectName" label="所属项目" min-width="160" show-overflow-tooltip />
            <el-table-column label="申请类别" min-width="90">
              <template #default="scope">
                <el-tag :type="getCategoryTag(scope.row.category)">{{ getCategoryLabel(scope.row.category) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="申请类型" min-width="100">
              <template #default="scope">
                <el-tag :type="getExpenseTypeTag(scope.row.type)">{{ getExpenseTypeLabel(scope.row.type) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="amount" label="申请金额" min-width="100" align="right">
              <template #default="scope">
                {{ formatCurrency(scope.row.amount) }}
              </template>
            </el-table-column>
            <el-table-column prop="applyUserName" label="申请人" min-width="90" />
            <el-table-column prop="status" label="状态" min-width="90">
              <template #default="scope">
                <el-tag :type="getStatusTag(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applyDate" label="申请日期" min-width="100" />
            <el-table-column label="操作" min-width="150" fixed="right" align="center">
              <template #default="scope">
                <el-button 
                  type="primary" 
                  link 
                  @click="handleAuditExpense(scope.row)"
                  v-if="scope.row.status === 'pending'"
                >审核</el-button>
                <el-button 
                  type="primary" 
                  link 
                  @click="handleAuditReceipt(scope.row)"
                  v-if="scope.row.status === 'receipt_audit'"
                >审核凭证</el-button>
                <el-button
                  type="success"
                  link
                  @click="handlePayExpense(scope.row)"
                  v-if="scope.row.status === 'approved'"
                >支付</el-button>
                <el-button type="info" link @click="handleViewExpense(scope.row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 经费支出分页 -->
          <div class="pagination-container" v-if="expenseTasks.length > 0">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="pagination.expense.pageSize"
              :current-page="pagination.expense.pageNum"
              :total="pagination.expense.total"
              @size-change="(size) => handleSizeChange('expense', size)"
              @current-change="(page) => handleCurrentChange('expense', page)"
            />
          </div>
        </el-card>
      </el-tab-pane>
      
      <el-tab-pane label="经费结转任务" name="transfer">
        <el-card class="task-card" v-loading="loading.transfer">
          <!-- 筛选条件 -->
          <div class="filter-container">
            <el-select v-model="statusFilter" placeholder="审核状态" clearable style="width: 150px; margin-right: 15px;">
              <el-option label="全部" value="" />
              <el-option label="待审核" value="pending" />
              <el-option label="已通过" value="approved" />
              <el-option label="已拒绝" value="rejected" />
            </el-select>
            <el-select v-model="projectFilter" placeholder="所属项目" clearable style="width: 150px; margin-right: 15px;">
              <el-option label="全部" value="" />
              <el-option v-for="project in projectOptions" :key="project.id" :label="project.name" :value="project.id" />
            </el-select>
            <el-button type="primary" @click="fetchTransferTasks">筛选</el-button>
            <el-button @click="resetFilter('transfer')">重置</el-button>
          </div>
          
          <div v-if="transferTasks.length === 0" class="empty-tip">
            暂无相关经费结转任务
          </div>
          <el-table v-else :data="transferTasks" border stripe style="width: 100%">
            <el-table-column prop="id" label="ID" min-width="60" align="center" />
            <el-table-column prop="title" label="结转标题" min-width="180" show-overflow-tooltip />
            <el-table-column prop="projectName" label="所属项目" min-width="160" show-overflow-tooltip />
            <el-table-column prop="amount" label="结转金额" min-width="100" align="right">
              <template #default="scope">
                {{ formatCurrency(scope.row.amount) }}
              </template>
            </el-table-column>
            <el-table-column label="结转年度" min-width="100">
              <template #default="scope">
                {{ scope.row.fromYear }}→{{ scope.row.toYear }}
              </template>
            </el-table-column>
            <el-table-column prop="applyUserName" label="申请人" min-width="90" />
            <el-table-column prop="status" label="状态" min-width="90">
              <template #default="scope">
                <el-tag :type="getStatusTag(scope.row.status || 'pending')">
                  {{ getStatusLabel(scope.row.status || 'pending') }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="applyDate" label="申请日期" min-width="100" />
            <el-table-column label="操作" min-width="150" fixed="right" align="center">
              <template #default="scope">
                <el-button 
                  type="primary" 
                  link 
                  @click="handleAuditTransfer(scope.row)"
                  v-if="!scope.row.status || scope.row.status === 'pending'"
                >审核</el-button>
                <el-button type="info" link @click="handleViewTransfer(scope.row)">查看详情</el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <!-- 经费结转分页 -->
          <div class="pagination-container" v-if="transferTasks.length > 0">
            <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :page-sizes="[10, 20, 50, 100]"
              :page-size="pagination.transfer.pageSize"
              :current-page="pagination.transfer.pageNum"
              :total="pagination.transfer.total"
              @size-change="(size) => handleSizeChange('transfer', size)"
              @current-change="(page) => handleCurrentChange('transfer', page)"
            />
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
    
    <!-- 项目审核弹窗 -->
    <el-dialog
      title="项目审核"
      v-model="projectAuditDialogVisible"
      width="550px"
    >
      <el-form :model="projectAuditForm" label-width="100px">
        <el-form-item label="项目名称">
          <el-text>{{ projectAuditForm.name }}</el-text>
        </el-form-item>
        <el-form-item label="项目类型">
          <el-tag :type="getProjectTypeTag(projectAuditForm.type)">
            {{ getProjectTypeLabel(projectAuditForm.type) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="负责人">
          <el-text>{{ projectAuditForm.leaderName }}</el-text>
        </el-form-item>
        <el-form-item label="审核结果" prop="auditStatus">
          <el-radio-group v-model="projectAuditForm.auditStatus">
            <el-radio label="approved">通过</el-radio>
            <el-radio label="rejected">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="projectAuditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitProjectAudit" :loading="submitLoading">提交</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 经费支出审核弹窗 -->
    <el-dialog
      title="经费支出审核"
      v-model="expenseAuditDialogVisible"
      width="550px"
    >
      <el-form :model="expenseAuditForm" label-width="100px">
        <el-form-item label="申请标题">
          <el-text>{{ expenseAuditForm.title }}</el-text>
        </el-form-item>
        <el-form-item label="所属项目">
          <el-text>{{ expenseAuditForm.projectName }}</el-text>
        </el-form-item>
        <el-form-item label="申请类型">
          <el-tag :type="getExpenseTypeTag(expenseAuditForm.type)">
            {{ getExpenseTypeLabel(expenseAuditForm.type) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="申请金额">
          <el-text>{{ formatCurrency(expenseAuditForm.amount) }}</el-text>
        </el-form-item>
        <el-form-item label="申请人">
          <el-text>{{ expenseAuditForm.applyUserName }}</el-text>
        </el-form-item>
        <el-form-item label="审核结果" prop="status">
          <el-radio-group v-model="expenseAuditForm.status">
            <el-radio label="approved">批准</el-radio>
            <el-radio label="rejected">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="expenseAuditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitExpenseAudit" :loading="submitLoading">提交</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 经费结转审核弹窗 -->
    <el-dialog
      title="经费结转审核"
      v-model="transferAuditDialogVisible"
      width="550px"
    >
      <el-form :model="transferAuditForm" label-width="100px">
        <el-form-item label="结转标题">
          <el-text>{{ transferAuditForm.title }}</el-text>
        </el-form-item>
        <el-form-item label="所属项目">
          <el-text>{{ transferAuditForm.projectName }}</el-text>
        </el-form-item>
        <el-form-item label="结转金额">
          <el-text>{{ formatCurrency(transferAuditForm.amount) }}</el-text>
        </el-form-item>
        <el-form-item label="结转年度">
          <el-text>{{ transferAuditForm.fromYear }} → {{ transferAuditForm.toYear }}</el-text>
        </el-form-item>
        <el-form-item label="申请人">
          <el-text>{{ transferAuditForm.applyUserName }}</el-text>
        </el-form-item>
        <el-form-item label="审核结果" prop="status">
          <el-radio-group v-model="transferAuditForm.status">
            <el-radio label="approved">批准</el-radio>
            <el-radio label="rejected">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="transferAuditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitTransferAudit" :loading="submitLoading">提交</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 添加报销凭证审核弹窗 -->
    <el-dialog
      title="报销凭证审核"
      v-model="receiptAuditDialogVisible"
      width="550px"
    >
      <el-form :model="receiptAuditForm" label-width="100px">
        <el-form-item label="申请标题">
          <el-text>{{ receiptAuditForm.title }}</el-text>
        </el-form-item>
        <el-form-item label="所属项目">
          <el-text>{{ receiptAuditForm.projectName }}</el-text>
        </el-form-item>
        <el-form-item label="申请类型">
          <el-tag :type="getExpenseTypeTag(receiptAuditForm.type)">
            {{ getExpenseTypeLabel(receiptAuditForm.type) }}
          </el-tag>
        </el-form-item>
        <el-form-item label="申请金额">
          <el-text>{{ formatCurrency(receiptAuditForm.amount) }}</el-text>
        </el-form-item>
        <el-form-item label="申请人">
          <el-text>{{ receiptAuditForm.applyUserName }}</el-text>
        </el-form-item>
        <el-form-item label="审核结果" prop="status">
          <el-radio-group v-model="receiptAuditForm.status">
            <el-radio label="approved">通过</el-radio>
            <el-radio label="rejected">拒绝</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="receiptAuditDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitReceiptAudit" :loading="submitLoading">提交</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getProjectList, auditProject, getProjectOptions } from '@/api/project'
import { getExpenseList, auditExpense, payExpense, auditReceipt } from '@/api/expense'
import { getTransferList, auditTransfer } from '@/api/transfer'

const router = useRouter()
const activeTab = ref('project')
const statusFilter = ref('') // 默认筛选全部任务
const categoryFilter = ref('') // 默认筛选全部类别
const projectFilter = ref('') // 新增项目筛选

// 任务列表数据
const projectTasks = ref([])
const expenseTasks = ref([])
const transferTasks = ref([])

// 加载状态
const loading = reactive({
  project: false,
  expense: false,
  transfer: false
})

const submitLoading = ref(false)

// 分页参数
const pagination = reactive({
  project: {
    total: 0,
    pageSize: 10,
    pageNum: 1
  },
  expense: {
    total: 0,
    pageSize: 10,
    pageNum: 1
  },
  transfer: {
    total: 0,
    pageSize: 10,
    pageNum: 1
  }
})

// 统计数据
const pendingProjectCount = computed(() => {
  return projectTasks.value.filter(task => task.auditStatus === 'pending').length
})

const pendingExpenseCount = computed(() => {
  return expenseTasks.value.filter(task => task.status === 'pending').length
})

const pendingTransferCount = computed(() => {
  return transferTasks.value.filter(task => !task.status || task.status === 'pending').length
})

// 格式化货币
const formatCurrency = (value) => {
  if (!value) return '¥0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2
  }).format(value).replace('CN¥', '¥')
}

// 获取项目类型标签样式
const getProjectTypeTag = (type) => {
  const types = {
    'school': '',
    'horizontal': 'success',
    'vertical': 'warning'
  }
  return types[type] || 'info'
}

// 获取项目类型显示文本
const getProjectTypeLabel = (type) => {
  const types = {
    'school': '校级项目',
    'horizontal': '横向项目',
    'vertical': '纵向项目'
  }
  return types[type] || '未知类型'
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

// 获取审核状态标签样式
const getAuditStatusTag = (status) => {
  const statuses = {
    'pending': 'info',
    'approved': 'success',
    'rejected': 'danger'
  }
  return statuses[status] || 'info'
}

// 获取审核状态显示文本
const getAuditStatusLabel = (status) => {
  const statuses = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝',
    'completion_pending': '待结题审核'
  }
  return statuses[status] || '未知状态'
}

// 获取申请类别标签样式
const getCategoryTag = (category) => {
  const categories = {
    'advance': 'primary',
    'reimbursement': 'success'
  }
  return categories[category] || 'info'
}

// 获取申请类别显示文本
const getCategoryLabel = (category) => {
  const categories = {
    'advance': '经费借款',
    'reimbursement': '报销'
  }
  return categories[category] || '未知类别'
}

// 获取项目待审核列表
const fetchProjectTasks = async () => {
  loading.project = true
  try {
    const params = {
      pageNum: pagination.project.pageNum,
      pageSize: pagination.project.pageSize
    }
    
    if (statusFilter.value) {
      params.auditStatus = statusFilter.value
    }
    
    const response = await getProjectList(params)
    
    if (response.code === 200) {
      // 过滤掉草稿状态(audit_status=audit)的项目
      projectTasks.value = (response.data.list || []).filter(task => task.auditStatus !== 'audit')
      pagination.project.total = response.data.total || 0
    } else {
      ElMessage.error(response.msg || '获取项目任务列表失败')
    }
  } catch (error) {
    console.error('获取项目任务列表失败:', error)
    ElMessage.error('获取项目任务列表失败，请稍后重试')
  } finally {
    loading.project = false
  }
}

// 获取经费支出待审核列表
const fetchExpenseTasks = async () => {
  loading.expense = true
  try {
    const params = {
      pageNum: pagination.expense.pageNum,
      pageSize: pagination.expense.pageSize
    }
    
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    if (categoryFilter.value) {
      params.category = categoryFilter.value
    }
    if (projectFilter.value) {
      params.project = projectFilter.value
    }
    
    const response = await getExpenseList(params)
    
    if (response.code === 200) {
      expenseTasks.value = response.data.list || []
      pagination.expense.total = response.data.total || 0
    } else {
      ElMessage.error(response.msg || '获取经费支出任务列表失败')
    }
  } catch (error) {
    console.error('获取经费支出任务列表失败:', error)
    ElMessage.error('获取经费支出任务列表失败，请稍后重试')
  } finally {
    loading.expense = false
  }
}

// 获取经费结转待审核列表
const fetchTransferTasks = async () => {
  loading.transfer = true
  try {
    const params = {
      pageNum: pagination.transfer.pageNum,
      pageSize: pagination.transfer.pageSize
    }
    
    if (statusFilter.value) {
      params.status = statusFilter.value
    }
    if (projectFilter.value) {
      params.project = projectFilter.value
    }
    
    const response = await getTransferList(params)
    
    if (response.code === 200) {
      transferTasks.value = response.data.list || []
      pagination.transfer.total = response.data.total || 0
    } else {
      ElMessage.error(response.msg || '获取经费结转任务列表失败')
    }
  } catch (error) {
    console.error('获取经费结转任务列表失败:', error)
    ElMessage.error('获取经费结转任务列表失败，请稍后重试')
  } finally {
    loading.transfer = false
  }
}

// 分页大小改变
const handleSizeChange = (type, size) => {
  pagination[type].pageSize = size;
  pagination[type].pageNum = 1; // 切换页大小时重置到第1页
  
  if (type === 'project') {
    fetchProjectTasks();
  } else if (type === 'expense') {
    fetchExpenseTasks();
  } else if (type === 'transfer') {
    fetchTransferTasks();
  }
}

// 页码改变
const handleCurrentChange = (type, page) => {
  pagination[type].pageNum = page;
  
  if (type === 'project') {
    fetchProjectTasks();
  } else if (type === 'expense') {
    fetchExpenseTasks();
  } else if (type === 'transfer') {
    fetchTransferTasks();
  }
}

// 重置筛选条件
const resetFilter = (tabName) => {
  statusFilter.value = ''
  categoryFilter.value = ''
  projectFilter.value = ''
  
  // 重置分页
  if (tabName === 'project') {
    pagination.project.pageNum = 1
    fetchProjectTasks()
  } else if (tabName === 'expense') {
    pagination.expense.pageNum = 1
    fetchExpenseTasks()
  } else if (tabName === 'transfer') {
    pagination.transfer.pageNum = 1
    fetchTransferTasks()
  }
}

// 项目审核弹窗
const projectAuditDialogVisible = ref(false)
const projectAuditForm = reactive({
  id: '',
  name: '',
  type: '',
  leaderName: '',
  auditStatus: 'approved',
  comment: ''
})

// 经费支出审核弹窗
const expenseAuditDialogVisible = ref(false)
const expenseAuditForm = reactive({
  id: '',
  title: '',
  projectName: '',
  type: '',
  amount: 0,
  applyUserName: '',
  status: 'approved',
  comment: ''
})

// 经费结转审核弹窗
const transferAuditDialogVisible = ref(false)
const transferAuditForm = reactive({
  id: '',
  title: '',
  projectName: '',
  amount: 0,
  fromYear: '',
  toYear: '',
  applyUserName: '',
  status: 'approved',
  comment: ''
})

// 报销凭证审核弹窗
const receiptAuditDialogVisible = ref(false)
const receiptAuditForm = reactive({
  id: '',
  title: '',
  projectName: '',
  type: '',
  amount: 0,
  applyUserName: '',
  status: 'approved',
  comment: ''
})

// 处理项目审核
const handleAuditProject = (project) => {
  projectAuditForm.id = project.id
  projectAuditForm.name = project.name
  projectAuditForm.type = project.type
  projectAuditForm.leaderName = project.leaderName
  projectAuditForm.auditStatus = 'approved'
  projectAuditForm.comment = ''
  
  projectAuditDialogVisible.value = true
}

// 处理项目结题审核
const handleAuditProjectCompletion = (project) => {
  router.push(`/project/completion-audit/${project.id}`)
}

// 提交项目审核
const submitProjectAudit = async () => {
  
  submitLoading.value = true
  
  try {
    const response = await auditProject(
      projectAuditForm.id,
      projectAuditForm.auditStatus,
      projectAuditForm.comment
    )
    
    if (response.code === 200) {
      ElMessage.success('项目审核提交成功')
      projectAuditDialogVisible.value = false
      
      // 刷新项目列表
      fetchProjectTasks()
    } else {
      ElMessage.error(response.msg || '审核失败')
    }
  } catch (error) {
    console.error('提交项目审核失败:', error)
    ElMessage.error('审核失败: ' + (error.message || '未知错误'))
  } finally {
    submitLoading.value = false
  }
}

// 查看项目详情
const handleViewProject = (project) => {
  router.push(`/project/detail/${project.id}`)
}

// 处理经费支出审核
const handleAuditExpense = (expense) => {
  expenseAuditForm.id = expense.id
  expenseAuditForm.title = expense.title
  expenseAuditForm.projectName = expense.projectName
  expenseAuditForm.type = expense.type
  expenseAuditForm.amount = expense.amount
  expenseAuditForm.applyUserName = expense.applyUserName
  expenseAuditForm.status = 'approved'
  expenseAuditForm.comment = ''
  
  expenseAuditDialogVisible.value = true
}

// 提交经费支出审核
const submitExpenseAudit = async () => {
  submitLoading.value = true
  
  try {
    const response = await auditExpense(
      expenseAuditForm.id,
      expenseAuditForm.status,
      expenseAuditForm.comment
    )
    
    if (response.code === 200) {
      ElMessage.success('经费支出审核提交成功')
      expenseAuditDialogVisible.value = false
      
      // 刷新经费支出列表
      fetchExpenseTasks()
    } else {
      ElMessage.error(response.msg || '审核失败')
    }
  } catch (error) {
    console.error('提交经费支出审核失败:', error)
    // 显示更具体的错误信息，尤其是预算不足的情况
    if (error.response && error.response.data && error.response.data.msg) {
      ElMessage.error(error.response.data.msg)
    } else {
      ElMessage.error('审核失败: ' + (error.message || '未知错误'))
    }
  } finally {
    submitLoading.value = false
  }
}

// 查看经费支出详情
const handleViewExpense = (expense) => {
  console.log('任务列表点击查看经费申请详情，ID:', expense.id, '，详情:', expense)
  if (!expense.id) {
    ElMessage.error('无效的经费申请ID，无法查看详情')
    return
  }
  try {
    router.push(`/expense/apply/detail/${expense.id}`)
  } catch (error) {
    console.error('路由跳转失败:', error)
    ElMessage.error('页面跳转失败，请重试')
  }
}

// 处理经费支出支付
const handlePayExpense = (expense) => {
  ElMessageBox.confirm(
    `确定要支付经费"${expense.title}"，金额为${formatCurrency(expense.amount)}吗？`,
    '支付确认',
    {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消',
      type: 'warning'
    }
  )
  .then(async () => {
    submitLoading.value = true
    try {
      const response = await payExpense(expense.id)
      
      if (response.code === 200) {
        ElMessage.success('经费支付成功')
        // 刷新经费支出列表
        fetchExpenseTasks()
      } else {
        ElMessage.error(response.msg || '支付失败')
      }
    } catch (error) {
      console.error('经费支付失败:', error)
      // 显示更具体的错误信息，尤其是预算不足的情况
      if (error.response && error.response.data && error.response.data.msg) {
        ElMessage.error(error.response.data.msg)
      } else {
        ElMessage.error('支付失败: ' + (error.message || '未知错误'))
      }
    } finally {
      submitLoading.value = false
    }
  })
  .catch(() => {
    // 用户取消操作
  })
}

// 处理经费结转审核
const handleAuditTransfer = (transfer) => {
  transferAuditForm.id = transfer.id
  transferAuditForm.title = transfer.title
  transferAuditForm.projectName = transfer.projectName
  transferAuditForm.amount = transfer.amount
  transferAuditForm.fromYear = transfer.fromYear
  transferAuditForm.toYear = transfer.toYear
  transferAuditForm.applyUserName = transfer.applyUserName
  transferAuditForm.status = 'approved'
  transferAuditForm.comment = ''
  
  transferAuditDialogVisible.value = true
}

// 提交经费结转审核
const submitTransferAudit = async () => {
  
  submitLoading.value = true
  
  try {
    const response = await auditTransfer(
      transferAuditForm.id,
      transferAuditForm.status,
      transferAuditForm.comment
    )
    
    if (response.code === 200) {
      ElMessage.success('经费结转审核提交成功')
      transferAuditDialogVisible.value = false
      
      // 刷新经费结转列表
      fetchTransferTasks()
    } else {
      ElMessage.error(response.msg || '审核失败')
    }
  } catch (error) {
    console.error('提交经费结转审核失败:', error)
    if (error.response.data.message.includes('项目剩余经费不足')) {
      ElMessage.error(error.response.data.message)
    } else {
      ElMessage.error('审核失败: ' + (error.message || '未知错误'))
    }
  } finally {
    submitLoading.value = false
  }
}

// 查看经费结转详情
const handleViewTransfer = (transfer) => {
  // 路由可能需要根据实际情况调整
  router.push(`/expense/transfer/detail/${transfer.id}`)
}

// 获取状态标签样式
const getStatusTag = (status) => {
  const statuses = {
    'pending': 'info',
    'approved': 'success',
    'rejected': 'danger',
    'paid': 'success',
    'receipt_pending': 'warning',
    'receipt_audit': 'info',
    'repayment_pending': 'warning',
    'completed': 'success',
    'repaid': 'success'
  }
  return statuses[status] || 'info'
}

// 获取状态显示文本
const getStatusLabel = (status) => {
  const statuses = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '已拒绝',
    'paid': '已支付',
    'receipt_pending': '待提交报销凭证',
    'receipt_audit': '报销凭证待审核',
    'repayment_pending': '负责人自行还款',
    'completed': '已完成',
    'repaid': '已还款',
    'pending_completion': '待结题',
    'completion_review': '待结题审核',
    'archived': '已归档'
  }
  return statuses[status] || '未知状态'
}

// 处理报销凭证审核
const handleAuditReceipt = (expense) => {
  receiptAuditForm.id = expense.id
  receiptAuditForm.title = expense.title
  receiptAuditForm.projectName = expense.projectName
  receiptAuditForm.type = expense.type
  receiptAuditForm.amount = expense.amount
  receiptAuditForm.applyUserName = expense.applyUserName
  receiptAuditForm.status = 'approved'
  receiptAuditForm.comment = ''
  
  receiptAuditDialogVisible.value = true
}

// 提交报销凭证审核
const submitReceiptAudit = async () => {
  submitLoading.value = true
  
  try {
    const response = await auditReceipt(
      receiptAuditForm.id,
      receiptAuditForm.status,
    )
    
    if (response.code === 200) {
      ElMessage.success('报销凭证审核提交成功')
      receiptAuditDialogVisible.value = false
      
      // 刷新经费支出列表
      fetchExpenseTasks()
    } else {
      ElMessage.error(response.msg || '审核失败')
    }
  } catch (error) {
    console.error('提交报销凭证审核失败:', error)
    // 显示更具体的错误信息，尤其是预算不足的情况
    if (error.response && error.response.data && error.response.data.msg) {
      ElMessage.error(error.response.data.msg)
    } else {
      ElMessage.error('审核失败: ' + (error.message || '未知错误'))
    }
  } finally {
    submitLoading.value = false
  }
}

// 刷新列表
const refreshList = () => {
  fetchProjectTasks()
  fetchExpenseTasks()
  fetchTransferTasks()
}

// 页面加载时获取数据
onMounted(() => {
  loadProjectOptions()
  fetchProjectTasks()
  fetchExpenseTasks()
  fetchTransferTasks()
})

// 新增项目选项
const projectOptions = ref([])

// 加载项目选项
const loadProjectOptions = async () => {
  try {
    // 调用项目列表API而不是options API，避免类型转换错误
    const response = await getProjectList({
      pageSize: 100, // 限制返回较多项目，但不至于过多
      pageNum: 1
    })
    
    if (response.code === 200 && response.data && response.data.list) {
      // 从项目列表中提取id和name用于下拉选择
      projectOptions.value = response.data.list.map(project => ({
        id: project.id,
        name: project.name
      }))
    } else {
      ElMessage.error(response.msg || '获取项目选项失败')
    }
  } catch (error) {
    console.error('获取项目选项失败:', error)
    ElMessage.error('获取项目选项失败，请稍后重试')
  }
}
</script>

<style scoped>
.task-list-container {
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

.stat-cards {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  cursor: pointer;
  transition: all 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 16px;
  color: #606266;
}

.task-tabs {
  margin-bottom: 20px;
}

.task-card {
  min-height: 400px;
}

.empty-tip {
  text-align: center;
  color: #909399;
  font-size: 16px;
  padding: 50px 0;
}

.filter-container {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
}

.pagination-container {
  text-align: right;
  padding: 10px;
}
</style> 