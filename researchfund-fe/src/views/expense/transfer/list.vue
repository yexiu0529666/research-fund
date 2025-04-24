<template>
  <div class="app-container">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-title">
        <h3>经费结转申请</h3>
      </div>
      <div class="header-actions">
        <el-button type="primary" @click="handleCreate">
          新增结转
        </el-button>
      </div>
    </div>

    <!-- 搜索区域 -->
    <el-card class="search-card">
      <el-form :model="queryParams" ref="queryForm" :inline="true" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="queryParams.title"
            placeholder="请输入标题"
            clearable
            style="width: 220px"
            @keyup.enter="handleSearch"
          />
        </el-form-item>
        <el-form-item label="项目" prop="projectId">
          <el-select
            v-model="queryParams.projectId"
            placeholder="全部项目"
            clearable
            style="width: 220px"
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
          <el-select
            v-model="queryParams.status"
            placeholder="全部状态"
            clearable
            style="width: 180px"
          >
            <el-option
              v-for="(label, value) in statusOptions"
              :key="value"
              :label="label"
              :value="value"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon> 搜索
          </el-button>
          <el-button @click="resetSearch">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 列表区域 -->
    <el-card class="table-card">
      <el-table
        v-loading="loading"
        :data="transferList"
        border
        style="width: 100%"
        @selection-change="handleSelectionChange"
        table-layout="auto"
        :fit="true"
        :header-cell-style="{ 'text-align': 'center' }"
      >
        
        <el-table-column label="标题" min-width="180" prop="title" :show-overflow-tooltip="true" />
        <el-table-column label="项目名称" min-width="160" prop="projectName" :show-overflow-tooltip="true" />
        <el-table-column label="结转金额" align="right" width="120">
          <template #default="scope">
            <span class="amount">{{ formatCurrency(scope.row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="结转年度" width="120">
          <template #default="scope">
            <span v-if="scope.row.fromYear && scope.row.toYear">
              {{ scope.row.fromYear }}-{{ scope.row.toYear }}
            </span>
            <span v-else>未设置</span>
          </template>
        </el-table-column>
        <el-table-column label="申请日期" width="120" prop="applyDate" />
        <el-table-column label="申请人" width="100" prop="applyUserName" />
        <el-table-column label="状态" width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusLabel(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" align="center">
          <template #default="scope">
            <el-button
              type="primary"
              link
              @click="handleView(scope.row)"
            >查看</el-button>
            <el-button
              v-if="canEdit(scope.row)"
              type="success"
              link
              @click="handleEdit(scope.row)"
            >编辑</el-button>
            <el-button
              v-if="canAudit(scope.row)"
              type="warning"
              link
              @click="handleAudit(scope.row)"
            >审核</el-button>
            <el-button
              v-if="canDelete(scope.row)"
              type="danger"
              link
              @click="handleDelete(scope.row)"
            >删除</el-button>
          </template>
        </el-table-column>
      </el-table>


      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :page-sizes="[10, 20, 50, 100]"
          :page-size="queryParams.pageSize"
          :current-page="queryParams.pageNum"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 审核对话框 -->
    <el-dialog
      title="审核结转申请"
      v-model="auditDialogVisible"
      width="500px"
      destroy-on-close
      :close-on-click-modal="false"
    >
      <el-form
        ref="auditFormRef"
        :model="auditForm"
        :rules="auditRules"
        label-width="80px"
      >
        <el-form-item label="申请标题" class="review-item">
          <div>{{ currentTransfer.title }}</div>
        </el-form-item>
        <el-form-item label="结转金额" class="review-item">
          <div class="amount">{{ formatCurrency(currentTransfer.amount) }}</div>
        </el-form-item>
        <el-form-item label="结转年度" class="review-item">
          <div>{{ currentTransfer.transferYears }}</div>
        </el-form-item>
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
import { reactive, ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getTransferList, getMyTransferList, getPendingTransferList, auditTransfer, deleteTransfer } from '@/api/transfer'
import { getMyProjects, getProjectList } from '@/api/project'
import { useUserStore } from '@/store/modules/user'

export default {
  name: 'ExpenseTransferList',
  components: {
    Search,
    Refresh
  },
  setup() {
    const router = useRouter()
    const userStore = useUserStore()
    
    // 响应式数据
    const loading = ref(false)
    const submitting = ref(false)
    const transferList = ref([])
    const total = ref(0)
    const projectOptions = ref([])
    const dateRange = ref([])
    const auditDialogVisible = ref(false)
    const auditFormRef = ref(null)
    const selectedRows = ref([])
    const currentTransfer = ref({})

    // 查询参数
    const queryParams = reactive({
      title: '',
      projectId: '',
      status: '',
      beginDate: '',
      endDate: '',
      pageNum: 1,
      pageSize: 10
    })

    // 状态选项
    const statusOptions = {
      pending: '待审核',
      approved: '已批准',
      rejected: '已拒绝'
    }

    // 审核表单
    const auditForm = reactive({
      status: 'approved',
      comment: ''
    })

    // 审核表单校验规则
    const auditRules = {
      status: [{ required: true, message: '请选择审核结果', trigger: 'change' }],
      comment: [{ required: true, message: '请输入审核意见', trigger: 'blur' }]
    }

    // 加载项目选项
    const loadProjectOptions = async () => {
      try {
        let response
        
        // 分页参数，设置大一点的页码确保获取所有项目
        const pageParams = {
          pageNum: 1,
          pageSize: 1000 // 设置足够大的每页数量
        }
        
        // 根据用户角色决定调用哪个接口获取项目列表
        if (userStore.isAdmin) {
          // 管理员获取所有项目
          console.log('以管理员身份加载所有项目')
          response = await getProjectList(pageParams)
        } else {
          // 普通用户获取自己的项目
          console.log('加载我的项目')
          response = await getMyProjects(pageParams)
        }
        
        if (response.code === 200) {
          console.log('获取项目列表成功:', response.data)
          projectOptions.value = response.data.list || []
        } else {
          console.error('获取项目选项失败，响应码:', response.code)
          projectOptions.value = []
        }
      } catch (error) {
        console.error('获取项目选项失败:', error)
        projectOptions.value = []
      }
    }

    // 加载结转申请列表
    const loadTransferList = async () => {
      loading.value = true
      try {
        console.log('加载经费结转申请列表，查询参数:', queryParams)
        console.log('当前用户角色:', userStore.roles, '是否管理员:', userStore.isAdmin)
        
        // 设置日期范围
        if (dateRange.value && dateRange.value.length === 2) {
          queryParams.beginDate = dateRange.value[0]
          queryParams.endDate = dateRange.value[1]
        } else {
          queryParams.beginDate = undefined
          queryParams.endDate = undefined
        }

        // 根据用户角色决定调用哪个接口
        let response
        if (userStore.isAdmin) {
          console.log('以管理员身份加载经费结转列表')
          response = await getTransferList(queryParams)
        } else {
          console.log('加载我的经费结转列表')
          response = await getMyTransferList(queryParams)
        }
        
        console.log('获取经费结转列表成功，数据:', response)
        
        // 详细记录响应结构
        if (response && response.data) {
          console.log('响应数据结构:', {
            code: response.code,
            message: response.message,
            dataType: typeof response.data,
            dataKeys: Object.keys(response.data),
            listType: Array.isArray(response.data.list) ? 'Array' : typeof response.data.list,
            listLength: Array.isArray(response.data.list) ? response.data.list.length : 'N/A'
          })
        }
        
        // 修改这里：从 response.data.list 中获取数据而不是 response.data.rows
        transferList.value = response.data.list || []
        total.value = response.data.total || 0
        
        // 添加数据格式检查和处理结转年度字段
        if (transferList.value.length > 0) {
          console.log('原始列表第一项:', JSON.stringify(transferList.value[0], null, 2))
          
          transferList.value = transferList.value.map(item => {
            // 添加结转年度显示字段
            if (item.fromYear && item.toYear) {
              item.transferYears = `${item.fromYear}-${item.toYear}`
            } else {
              item.transferYears = '未设置'
            }
            return item
          })
        } else {
          console.log('列表为空')
        }
        
        console.log('处理后的列表数据:', transferList.value)
      } catch (error) {
        console.error('获取结转申请列表失败', error)
        ElMessage.error('获取结转申请列表失败: ' + (error.message || '未知错误'))
      } finally {
        loading.value = false
      }
    }

    // 搜索
    const handleSearch = () => {
      queryParams.pageNum = 1
      loadTransferList()
    }

    // 重置搜索
    const resetSearch = () => {
      queryParams.title = ''
      queryParams.projectId = ''
      queryParams.status = ''
      dateRange.value = []
      queryParams.beginDate = undefined
      queryParams.endDate = undefined
      queryParams.pageNum = 1
      loadTransferList()
    }

    // 选择项变更
    const handleSelectionChange = (selection) => {
      selectedRows.value = selection
    }

    // 新增
    const handleCreate = () => {
      router.push('/expense/transfer/create')
    }

    // 查看
    const handleView = (row) => {
      router.push(`/expense/transfer/detail/${row.id}`)
    }

    // 编辑
    const handleEdit = (row) => {
      router.push(`/expense/transfer/edit/${row.id}`)
    }

    // 判断是否可编辑
    const canEdit = (row) => {
      // 只有待审核的申请且是申请人才可以编辑
      return row.status === 'pending' && String(row.applyUserId) === String(userStore.userId)
    }

    // 判断是否可审核
    const canAudit = (row) => {
      // 只有待审核的申请且不是申请人自己且是管理员才可以审核
      return row.status === 'pending' && 
             String(row.applyUserId) !== String(userStore.userId) &&
             userStore.isAdmin
    }

    // 审核
    const handleAudit = (row) => {
      currentTransfer.value = row
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
            currentTransfer.value.id,
            auditForm.status,
            auditForm.comment
          )
          
          ElMessage.success('审核提交成功')
          auditDialogVisible.value = false
          
          // 重新加载列表
          await loadTransferList()
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

    // 分页大小变更
    const handleSizeChange = (size) => {
      queryParams.pageSize = size
      loadTransferList()
    }

    // 页码变更
    const handleCurrentChange = (page) => {
      queryParams.pageNum = page
      loadTransferList()
    }

    // 获取状态标签类型
    const getStatusType = (status) => {
      const typeMap = {
        pending: 'warning',
        approved: 'success',
        rejected: 'danger'
      }
      return typeMap[status] || 'info'
    }

    // 获取状态标签文本
    const getStatusLabel = (status) => {
      return statusOptions[status] || status
    }

    // 格式化货币
    const formatCurrency = (value) => {
      if (!value && value !== 0) return '¥0.00'
      return '¥' + Number(value).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
    }

    // 判断是否可删除
    const canDelete = (row) => {
      // 只有待审核的申请且是申请人才可以删除
      return row.status === 'pending' && String(row.applyUserId) === String(userStore.userId)
    }

    // 删除
    const handleDelete = (row) => {
      ElMessageBox.confirm(
        `确认删除经费结转申请"${row.title}"吗？
        项目：${row.projectName}
        结转金额：${formatCurrency(row.amount)}
        
        删除后无法恢复！`,
        '确认删除',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: true
        }
      ).then(async () => {
        try {
          await deleteTransfer(row.id)
          ElMessage.success('删除成功')
          await loadTransferList()
        } catch (error) {
          console.error('删除失败', error)
          ElMessage.error('删除失败: ' + (error.message || '未知错误'))
        }
      }).catch(() => {
        // 用户取消删除操作
      })
    }

    // 初始化
    onMounted(() => {
      loadProjectOptions()
      loadTransferList()
    })

    return {
      loading,
      submitting,
      transferList,
      total,
      queryParams,
      statusOptions,
      projectOptions,
      dateRange,
      auditDialogVisible,
      auditForm,
      auditRules,
      auditFormRef,
      currentTransfer,
      selectedRows,
      handleSearch,
      resetSearch,
      handleSelectionChange,
      handleCreate,
      handleView,
      handleEdit,
      handleAudit,
      submitAudit,
      canEdit,
      canAudit,
      handleSizeChange,
      handleCurrentChange,
      getStatusType,
      getStatusLabel,
      formatCurrency,
      canDelete,
      handleDelete
    }
  }
}
</script>

<style scoped>
.app-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-title h3 {
  margin: 0;
  font-size: 20px;
}

.search-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
  overflow-x: auto; /* 使表格容器在小屏幕时可横向滚动 */
}

/* 表格响应式样式 */
.el-table {
  width: 100% !important;
}

/* 在小屏幕设备上调整表格样式 */
@media screen and (max-width: 768px) {
  .table-card {
    overflow-x: auto;
  }
  
  .el-table {
    width: 100%;
    min-width: 800px; /* 设置最小宽度以保证所有内容可见 */
  }
}

.pagination-container {
  margin-top: 15px;
  text-align: right;
}

.amount {
  font-weight: bold;
  color: #f56c6c;
}

.dialog-footer {
  text-align: right;
}

.review-item {
  margin-bottom: 10px;
}
</style> 