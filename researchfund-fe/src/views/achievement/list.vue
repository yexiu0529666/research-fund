<template>
  <div class="achievement-list-container">
    <!-- 搜索条件区域 -->
    <el-card class="search-card">
      <el-form :model="searchForm" ref="searchFormRef" :inline="true" size="default">
        <el-form-item label="成果名称" prop="title">
          <el-input v-model="searchForm.title" placeholder="请输入成果名称" clearable />
        </el-form-item>
        <el-form-item label="成果类型" prop="type">
          <el-select v-model="searchForm.type" placeholder="请选择成果类型" clearable style="width: 180px;">
            <el-option label="期刊论文" value="journal" />
            <el-option label="会议论文" value="conference" />
            <el-option label="专利" value="patent" />
            <el-option label="著作" value="book" />
            <el-option label="软件著作权" value="software" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        <el-form-item label="相关项目" prop="projectId">
          <el-select 
            v-model="searchForm.projectId" 
            placeholder="请选择相关项目" 
            clearable 
            filterable
            style="width: 180px;"
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
        <el-form-item label="状态" prop="status">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable style="width: 150px;">
            <el-option label="待审核" value="pending" />
            <el-option label="已通过" value="approved" />
            <el-option label="已拒绝" value="rejected" />
            <el-option label="已支付" value="paid" />
          </el-select>
        </el-form-item>
        <el-form-item label="获奖日期" prop="awardDate">
          <el-date-picker 
            v-model="searchForm.awardDate" 
            type="daterange" 
            range-separator="至" 
            start-placeholder="开始日期" 
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 操作按钮栏 -->
    <div class="action-bar">
      <el-button type="primary" @click="handleCreate" v-if="!userStore.isAdmin">
        <el-icon><Plus /></el-icon> 新增成果
      </el-button>
    </div>

    <!-- 列表区域 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="achievementList"
        stripe
        border
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column prop="title" label="成果名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="type" label="成果类型" min-width="120">
          <template #default="{ row }">
            {{ getTypeLabel(row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="level" label="成果级别" min-width="100">
          <template #default="{ row }">
            {{ getLevelLabel(row.level) }}
          </template>
        </el-table-column>
        <el-table-column prop="authors" label="作者" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ formatAuthors(row.authors) }}
          </template>
        </el-table-column>
        <el-table-column prop="projectName" label="相关项目" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.projectName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="awardDate" label="获奖日期" min-width="120" />
        <el-table-column prop="awardAmount" label="奖励金额" min-width="120">
          <template #default="{ row }">
            {{ formatCurrency(row.awardAmount) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="110">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="提交时间" min-width="150">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" fixed="right">
          <template #default="{ row }">
            <el-button 
              link 
              type="primary" 
              size="small" 
              @click="handleView(row)"
            >
              查看
            </el-button>
            <el-button 
              link 
              type="primary"
              size="small" 
              @click="handleEdit(row)"
              v-if="canEdit(row)"
            >
              编辑
            </el-button>
            <el-button 
              link 
              type="primary" 
              size="small" 
              @click="handleAudit(row)"
              v-if="canAudit(row)"
            >
              审核
            </el-button>
            <el-button 
              link 
              type="success" 
              size="small" 
              @click="handlePay(row)"
              v-if="canPay(row)"
            >
              支付
            </el-button>
            <el-button 
              link 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
              v-if="canDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页控件 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="queryParams.pageNum"
          v-model:page-size="queryParams.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

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
import { Plus, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAchievementList, deleteAchievement, payAchievement } from '@/api/achievement'
import { getProjectList, getMyProjects } from '@/api/project'
import { useUserStore } from '@/store/modules/user'
import { useRouter } from 'vue-router'
import { formatDateTime } from '@/utils/date'

const router = useRouter()
const userStore = useUserStore()

// 列表加载状态
const loading = ref(false)
// 成果奖励列表数据
const achievementList = ref([])
// 总数据条数
const total = ref(0)
// 待删除的ID
const deleteId = ref(null)
// 删除对话框显示控制
const deleteDialogVisible = ref(false)
// 删除加载状态
const deleteLoading = ref(false)

// 项目列表数据
const projectOptions = ref([])
const projectsLoading = ref(false)

// 搜索表单
const searchFormRef = ref(null)
const searchForm = reactive({
  title: '',
  type: '',
  projectId: '',
  status: '',
  awardDate: []
})

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  title: '',
  type: '',
  projectId: '',
  status: '',
  startDate: '',
  endDate: ''
})

// 初始化加载数据
onMounted(() => {
  loadAchievementList()
  loadProjectOptions()
})

// 加载项目列表
const loadProjectOptions = async () => {
  projectsLoading.value = true
  try {
    // 根据用户角色决定获取项目的范围
    let response;
    if (userStore.isAdmin) {
      // 管理员获取所有项目
      response = await getProjectList({ pageSize: 100 });
    } else {
      // 普通用户获取自己的项目
      response = await getMyProjects({ pageSize: 100 });
    }

    if (response.code === 200) {
      projectOptions.value = response.data.list || [];
    } else {
      ElMessage.error(response.message || '获取项目列表失败');
    }
  } catch (error) {
    console.error('获取项目列表出错:', error);
  } finally {
    projectsLoading.value = false;
  }
}

// 加载成果奖励列表
const loadAchievementList = async () => {
  loading.value = true
  try {
    const response = await getAchievementList(queryParams)
    if (response.code === 200) {
      achievementList.value = response.data.list || []
      total.value = response.data.total || 0
    } else {
      ElMessage.error(response.message || '获取成果奖励列表失败')
    }
  } catch (error) {
    console.error('获取成果奖励列表出错:', error)
    ElMessage.error('获取成果奖励列表失败，请稍后再试')
  } finally {
    loading.value = false
  }
}

// 查询
const handleSearch = () => {
  queryParams.pageNum = 1
  queryParams.title = searchForm.title
  queryParams.type = searchForm.type
  queryParams.projectId = searchForm.projectId
  queryParams.status = searchForm.status
  
  if (searchForm.awardDate && searchForm.awardDate.length === 2) {
    queryParams.startDate = searchForm.awardDate[0]
    queryParams.endDate = searchForm.awardDate[1]
  } else {
    queryParams.startDate = ''
    queryParams.endDate = ''
  }
  
  loadAchievementList()
}

// 重置搜索
const resetSearch = () => {
  searchFormRef.value.resetFields()
  queryParams.pageNum = 1
  queryParams.title = ''
  queryParams.type = ''
  queryParams.projectId = ''
  queryParams.status = ''
  queryParams.startDate = ''
  queryParams.endDate = ''
  loadAchievementList()
}

// 分页大小变化
const handleSizeChange = (size) => {
  queryParams.pageSize = size
  loadAchievementList()
}

// 当前页变化
const handleCurrentChange = (page) => {
  queryParams.pageNum = page
  loadAchievementList()
}

// 新增成果奖励
const handleCreate = () => {
  router.push('/achievement/create')
}

// 查看成果奖励
const handleView = (row) => {
  router.push(`/achievement/detail/${row.id}`)
}

// 编辑成果奖励
const handleEdit = (row) => {
  router.push(`/achievement/edit/${row.id}`)
}

// 审核成果奖励
const handleAudit = (row) => {
  router.push(`/achievement/audit/${row.id}`)
}

// 导出成果奖励
const handleExport = () => {
  ElMessage.success('导出功能开发中')
}

// 删除成果奖励
const handleDelete = (row) => {
  deleteId.value = row.id
  deleteDialogVisible.value = true
}

// 确认删除
const confirmDelete = async () => {
  if (!deleteId.value) return
  
  deleteLoading.value = true
  try {
    const response = await deleteAchievement(deleteId.value)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadAchievementList()
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

// 获取类型标签
const getTypeLabel = (type) => {
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
  const levelMap = {
    national: '国家级',
    provincial: '省部级',
    departmental: '厅局级',
    school: '校级',
    other: '其他'
  }
  return levelMap[level] || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  const statusMap = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger',
    paid: 'success'
  }
  return statusMap[status] || 'info'
}

// 获取状态标签
const getStatusLabel = (status) => {
  const statusMap = {
    pending: '待审核',
    approved: '已通过',
    rejected: '已拒绝',
    paid: '已支付'
  }
  return statusMap[status] || '未知'
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

// 判断是否能够编辑
const canEdit = (row) => {
  // 只有用户自己可以编辑自己提交的且状态为待审核的成果
  // 管理员不能编辑
  return !userStore.isAdmin && row.status === 'pending'
}

// 判断是否能够审核
const canAudit = (row) => {
  // 只有管理员可以审核待审核的成果
  return userStore.isAdmin && row.status === 'pending'
}

// 判断是否能够删除
const canDelete = (row) => {
  // 只有用户自己可以删除自己提交的且状态为待审核的成果
  // 管理员不能删除
  return !userStore.isAdmin  && row.status === 'pending'
}

// 判断是否能够支付
const canPay = (row) => {
  // 只有管理员可以支付已通过的成果
  return userStore.isAdmin && row.status === 'approved'
}

// 处理支付
const handlePay = (row) => {
  ElMessageBox.confirm('确定要支付该成果奖励吗？支付后状态将更新为已支付。', '支付确认', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    try {
      const response = await payAchievement(row.id)
      if (response.code === 200) {
        ElMessage.success('支付成功')
        // 重新加载列表，刷新状态
        loadAchievementList()
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
</script>

<style scoped>
.achievement-list-container {
  padding: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.search-card :deep(.el-form-item) {
  margin-bottom: 18px;
  margin-right: 15px;
}

.search-card :deep(.el-select) {
  min-width: 120px;
}

.action-bar {
  margin-bottom: 20px;
  display: flex;
  justify-content: flex-start;
}

.table-card {
  margin-bottom: 20px;
  overflow-x: auto;
}

.table-card :deep(.el-table) {
  min-width: 800px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 确保下拉菜单宽度足够 */
:deep(.el-select__popper) {
  min-width: 180px !important;
}
</style> 