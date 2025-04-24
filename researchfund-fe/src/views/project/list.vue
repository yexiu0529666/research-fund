<template>
  <div class="project-list-container">
    <div class="page-header">
      <div class="page-title">项目管理</div>
      <el-button type="primary" @click="handleAddProject" v-if="userStore.roles.includes('ROLE_RESEARCHER')">新建项目</el-button>
    </div>
    
    <!-- 搜索和筛选区域 -->
    <el-card class="filter-container">
      <el-form :inline="true" :model="queryParams" class="filter-form">
        <el-form-item label="项目名称">
          <el-input v-model="queryParams.name" placeholder="请输入项目名称" clearable style="min-width: 180px;" />
        </el-form-item>
        
        <el-form-item label="项目类型">
          <el-select v-model="queryParams.type" clearable placeholder="请选择项目类型" style="min-width: 180px;">
            <el-option label="校级项目" value="school" />
            <el-option label="横向项目" value="horizontal" />
            <el-option label="纵向项目" value="vertical" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" clearable placeholder="请选择项目状态" style="min-width: 180px;">
            <el-option label="筹划中" value="planning" />
            <el-option label="进行中" value="active" />
            <el-option label="已完成" value="completed" />
            <el-option label="已暂停" value="suspended" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="审核状态">
          <el-select v-model="queryParams.auditStatus" clearable placeholder="请选择审核状态" style="min-width: 180px;">
            <el-option label="草稿" value="audit" />
            <el-option label="审核中" value="pending" />
            <el-option label="审核通过" value="approved" />
            <el-option label="审核未通过" value="rejected" />
          </el-select>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 项目列表 -->
    <el-card class="table-container">
      <el-table
        v-loading="loading"
        :data="displayProjectList"
        style="width: 100%"
        stripe
        border
      >
        <el-table-column prop="name" label="项目名称" min-width="180" show-overflow-tooltip />
        <el-table-column prop="type" label="项目类型" min-width="120">
          <template #default="scope">
            <el-tag :type="getProjectTypeTag(scope.row.type)">{{ getProjectTypeLabel(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="leaderName" label="负责人" min-width="100" />
        <el-table-column prop="startDate" label="开始日期" min-width="110" />
        <el-table-column prop="endDate" label="结束日期" min-width="110" />
        <el-table-column prop="budget" label="总预算" min-width="110">
          <template #default="scope">
            {{ formatCurrency(scope.row.budget) }}
          </template>
        </el-table-column>
        <el-table-column prop="usedBudget" label="已用预算" min-width="110">
          <template #default="scope">
            {{ formatCurrency(scope.row.usedBudget) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getStatusTag(scope.row.status)">{{ getStatusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="auditStatus" label="审核状态" min-width="120">
          <template #default="scope">
            <el-tag :type="getAuditStatusTag(scope.row.auditStatus)">{{ getAuditStatusLabel(scope.row.auditStatus) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleViewDetail(scope.row)">查看</el-button>
            <el-button link type="primary" @click="handleEdit(scope.row)" 
            v-if="!userStore.roles.includes('ROLE_ADMIN') && scope.row.status === 'planning' && scope.row.auditStatus == 'audit'">编辑</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)"
            v-if="!userStore.roles.includes('ROLE_ADMIN') && scope.row.status === 'planning' && scope.row.auditStatus == 'audit'">删除</el-button>
            <el-button link type="success" @click="handleConfirmProject(scope.row)" 
              v-if="userStore.roles.includes('ROLE_RESEARCHER') && scope.row.status === 'planning' && scope.row.auditStatus == 'audit'">确认立项</el-button>
            <el-button link type="warning" @click="handleSubmitCompletion(scope.row)" 
              v-if="userStore.roles.includes('ROLE_RESEARCHER') && scope.row.status === 'pending_completion'">提交结题报告</el-button>
            <el-button link type="warning" @click="handleAuditCompletion(scope.row)" 
              v-if="userStore.roles.includes('ROLE_ADMIN') && scope.row.status === 'completion_review'">审核结题</el-button>
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
    title="项目审核"
    v-model="auditDialogVisible"
    width="500px"
  >
    <el-form :model="auditForm" label-width="100px">
      <el-form-item label="项目名称">
        <el-text>{{ auditForm.name }}</el-text>
      </el-form-item>
      <el-form-item label="项目类型">
        <el-tag :type="getProjectTypeTag(auditForm.type)">{{ getProjectTypeLabel(auditForm.type) }}</el-tag>
      </el-form-item>
      <el-form-item label="负责人">
        <el-text>{{ auditForm.leaderName }}</el-text>
      </el-form-item>
      <el-form-item label="总预算">
        <el-text>{{ formatCurrency(auditForm.budget) }}</el-text>
      </el-form-item>
      <el-form-item label="审核结果">
        <el-radio-group v-model="auditForm.result">
          <el-radio label="approved">通过</el-radio>
          <el-radio label="rejected">不通过</el-radio>
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
import { getProjectList, getMyProjects, deleteProject, auditProject, confirmProject } from '@/api/project'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const total = ref(0)

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  name: '',
  type: '',
  status: '',
  auditStatus: ''
})

// 项目列表数据
const projectList = ref([])

// 显示给表格的项目列表
const displayProjectList = ref([])

// 获取项目类型标签样式
const getProjectTypeTag = (type) => {
  const types = {
    school: 'success',
    horizontal: 'warning',
    vertical: 'primary'
  }
  return types[type] || 'info'
}

// 获取项目类型显示文本
const getProjectTypeLabel = (type) => {
  const types = {
    school: '校级项目',
    horizontal: '横向项目',
    vertical: '纵向项目'
  }
  return types[type] || '未知类型'
}

// 获取状态标签样式
const getStatusTag = (status) => {
  const statuses = {
    planning: 'info',
    active: 'success',
    completed: '',
    suspended: 'warning',
    pending_completion: 'warning',
    completion_review: 'info',
    archived: 'success'
  }
  return statuses[status] || 'info'
}

// 获取状态显示文本
const getStatusLabel = (status) => {
  const statuses = {
    planning: '筹划中',
    active: '进行中',
    completed: '已完成',
    suspended: '已暂停',
    pending_completion: '待结题',
    completion_review: '待结题审核',
    archived: '已归档'
  }
  return statuses[status] || '未知状态'
}

// 获取审核状态标签样式
const getAuditStatusTag = (status) => {
  const statuses = {
    audit: 'warning',
    pending: 'info',
    approved: 'success',
    rejected: 'danger'
  }
  return statuses[status] || 'info'
}

// 获取审核状态显示文本
const getAuditStatusLabel = (status) => {
  const statuses = {
    audit: '草稿',
    pending: '审核中',
    approved: '审核通过',
    rejected: '审核未通过'
  }
  return statuses[status] || '未知审核状态'
}

// 格式化金额
const formatCurrency = (value) => {
  if (!value) return '0.00'
  return new Intl.NumberFormat('zh-CN', {
    style: 'currency',
    currency: 'CNY',
    minimumFractionDigits: 2
  }).format(value).replace('CN¥', '¥')
}

// 执行查询
const handleQuery = () => {
  getProjects()
}

// 重置查询
const resetQuery = () => {
  Object.assign(queryParams, {
    pageNum: 1,
    pageSize: 10,
    name: '',
    type: '',
    status: '',
    auditStatus: ''
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

// 查看项目详情
const handleViewDetail = (row) => {
  console.log('查看项目详情:', row)
  router.push(`/project/detail/${row.id}`)
}

// 编辑项目
const handleEdit = (row) => {
  router.push(`/project/edit/${row.id}`)
}

// 删除项目
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除项目 "${row.name}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteProject(row.id);
      ElMessage.success('删除成功');
      // 刷新项目列表
      getProjects();
    } catch (error) {
      console.error('删除项目失败:', error);
      ElMessage.error('删除失败: ' + (error.message || '未知错误'));
    }
  }).catch(() => {})
}

// 新建项目
const handleAddProject = () => {
  router.push('/project/apply')
}

// 审核对话框
const auditDialogVisible = ref(false)
const auditForm = ref({
  id: null,
  name: '',
  type: '',
  leaderName: '',
  budget: 0,
  result: 'approved',
  comment: ''
})

// 审核项目
const handleAudit = (row) => {
  auditForm.value = {
    id: row.id,
    name: row.name,
    type: row.type,
    leaderName: row.leaderName,
    budget: row.budget,
    result: 'approved',
    comment: ''
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
    await auditProject(
      auditForm.value.id, 
      auditForm.value.result, 
      auditForm.value.comment
    );
    
    ElMessage.success('审核提交成功');
    auditDialogVisible.value = false;
    
    // 刷新项目列表
    getProjects();
  } catch (error) {
    console.error('提交审核失败:', error);
    ElMessage.error('审核失败: ' + (error.message || '未知错误'));
  }
}

// 页面加载时获取数据
onMounted(() => {
  getProjects()
})

// 获取项目列表
const getProjects = async () => {
  loading.value = true
  
  try {
    let response;
    
    // 根据用户角色决定获取全部项目还是当前用户的项目
    if (userStore.roles.includes('ROLE_ADMIN')) {
      // 管理员角色获取所有项目
      const params = {
        name: queryParams.name || undefined,
        type: queryParams.type || undefined,
        status: queryParams.status || undefined,
        auditStatus: queryParams.auditStatus || undefined,
        pageNum: queryParams.pageNum,
        pageSize: queryParams.pageSize
      };
      response = await getProjectList(params);
    } else {
      // 普通用户只获取自己的项目
      const params = {
        name: queryParams.name || undefined,
        type: queryParams.type || undefined,
        status: queryParams.status || undefined,
        pageNum: queryParams.pageNum,
        pageSize: queryParams.pageSize
      };
      response = await getMyProjects(params);
    }
    
    // 检查响应数据的格式
    if (response.code === 200 && response.data) {
      projectList.value = response.data.list || [];
      displayProjectList.value = projectList.value;
      total.value = response.data.total || 0;
    } else {
      ElMessage.warning('获取项目数据格式不正确');
      projectList.value = [];
      displayProjectList.value = [];
      total.value = 0;
    }
  } catch (error) {
    console.error('获取项目列表失败:', error);
    ElMessage.error('获取项目列表失败: ' + (error.message || '未知错误'));
    projectList.value = [];
    displayProjectList.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

// 确认立项
const handleConfirmProject = async (row) => {
  ElMessageBox.confirm(
    '确认要将项目提交立项审核吗？确认立项后将无法修改项目内容和经费申请。',
    '确认立项',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await confirmProject(row.id);
      ElMessage.success('确认立项成功，项目已提交审核');
      // 刷新项目列表
      getProjects();
    } catch (error) {
      console.error('确认立项失败:', error);
      ElMessage.error('确认立项失败: ' + (error.message || '未知错误'));
    }
  }).catch(() => {
    ElMessage.info('已取消操作');
  });
}

// 提交结题报告
const handleSubmitCompletion = (row) => {
  router.push(`/project/completion/${row.id}`);
}

// 审核结题
const handleAuditCompletion = (row) => {
  router.push(`/project/completion-audit/${row.id}`);
}
</script>

<style scoped>
.project-list-container {
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