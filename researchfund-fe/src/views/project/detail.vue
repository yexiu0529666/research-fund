<template>
  <div class="project-detail-container">
    <div class="page-header">
      <div class="title-actions">
        <div class="page-title">项目详情</div>
        <div class="action-buttons">
          <el-button 
            type="primary" 
            @click="handleEdit" 
            :disabled="loading"
            v-if="canEdit">编辑项目</el-button>
          <el-button @click="goBack">返回列表</el-button>
        </div>
      </div>
    </div>
    
    <div v-loading="loading">
      <el-row :gutter="20">
        <el-col :xs="24" :lg="16">
          <!-- 项目基本信息 -->
          <el-card class="detail-card">
            <template #header>
              <div class="card-header">
                <span>基本信息</span>
                <el-tag :type="getStatusTag(projectInfo.status)">{{ getStatusLabel(projectInfo.status) }}</el-tag>
              </div>
            </template>
            
            <el-descriptions :column="2" border>
              <el-descriptions-item label="项目名称" :span="2">{{ projectInfo.name }}</el-descriptions-item>
              <el-descriptions-item label="项目类型">
                <el-tag :type="getTypeTag(projectInfo.type)">{{ getTypeLabel(projectInfo.type) }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="负责人">{{ projectInfo.leaderName }}</el-descriptions-item>
              <el-descriptions-item label="开始日期">{{ projectInfo.startDate }}</el-descriptions-item>
              <el-descriptions-item label="结束日期">{{ projectInfo.endDate }}</el-descriptions-item>
              <el-descriptions-item label="总预算">{{ formatCurrency(projectInfo.budget) }}</el-descriptions-item>
              <el-descriptions-item label="已用预算">{{ formatCurrency(projectInfo.usedBudget) }}</el-descriptions-item>
            </el-descriptions>
            
            <div class="status-tips" v-if="projectInfo.status === 'pending_completion'">
              <el-alert type="warning" :closable="false">
                <div class="alert-content">
                  <el-icon><warning /></el-icon>
                  <span>项目已进入待结题状态，请前往"项目列表"页面提交结题报告。</span>
                </div>
              </el-alert>
            </div>
            
            <div class="status-tips" v-if="projectInfo.status === 'completion_review'">
              <el-alert type="info" :closable="false">
                <div class="alert-content">
                  <el-icon><info-filled /></el-icon>
                  <span>结题报告已提交，正在等待审核。</span>
                </div>
              </el-alert>
            </div>
            
            <div class="status-tips" v-if="projectInfo.status === 'archived'">
              <el-alert type="success" :closable="false">
                <div class="alert-content">
                  <el-icon><circle-check /></el-icon>
                  <span>项目已结题归档。</span>
                </div>
              </el-alert>
            </div>
            
            <div class="progress-section">
              <div class="progress-title">经费使用进度</div>
              <el-progress 
                :percentage="budgetPercentage" 
                :status="getProgressStatus(budgetPercentage)"
                :format="percentFormat"
              />
            </div>
            
            <!-- 项目申请书下载链接 -->
            <div class="file-section" v-if="projectInfo.filePath">
              <div class="section-title">项目申请书</div>
              <el-button type="primary" link @click="downloadProjectFile">
                <el-icon><Download /></el-icon> 下载项目申请书
              </el-button>
            </div>

            <!-- 结题报告下载链接 -->
            <div class="file-section" v-if="projectInfo.completionReportPath">
              <div class="section-title">结题报告</div>
              <el-button type="primary" link @click="downloadCompletionReport">
                <el-icon><Download /></el-icon> 下载结题报告
              </el-button>
              <div v-if="projectInfo.completionAuditStatus" class="completion-audit-info">
                <div class="audit-status">
                  结题审核状态: 
                  <el-tag :type="getCompletionAuditStatusTag(projectInfo.completionAuditStatus)">
                    {{ getCompletionAuditStatusLabel(projectInfo.completionAuditStatus) }}
                  </el-tag>
                </div>
                <div v-if="projectInfo.completionAuditComment" class="audit-comment">
                  审核意见: {{ projectInfo.completionAuditComment }}
                </div>
              </div>
            </div>
          </el-card>
          
          <!-- 项目内容 -->
          <el-card class="detail-card">
            <template #header>
              <div class="card-header">
                <span>项目内容</span>
              </div>
            </template>
            
            <div class="content-section">
              <div class="section-title">项目简介</div>
              <div class="section-content">{{ projectInfo.description || '暂无内容' }}</div>
            </div>
            
            <div class="content-section">
              <div class="section-title">研究内容</div>
              <div class="section-content">{{ projectInfo.researchContent || '暂无内容' }}</div>
            </div>
            
            <div class="content-section">
              <div class="section-title">预期成果</div>
              <div class="section-content">{{ projectInfo.expectedResults || '暂无内容' }}</div>
            </div>
          </el-card>
        </el-col>
        
        <el-col :xs="24" :lg="8">
          <!-- 项目团队 -->
          <el-card class="detail-card">
            <template #header>
              <div class="card-header">
                <span>项目团队</span>
              </div>
            </template>
            
            <div class="team-members" v-if="projectInfo.team && projectInfo.team.length">
              <div v-for="(member, index) in projectInfo.team" :key="index" class="team-member">
                <div class="member-info">
                  <div class="member-name">{{ member.name }}</div>
                  <div class="member-role">{{ member.role }}</div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无团队成员" :image-size="100"></el-empty>
          </el-card>
          
          <!-- 项目里程碑 -->
          <el-card class="detail-card">
            <template #header>
              <div class="card-header">
                <span>经费使用占比</span>
              </div>
            </template>
            
            <div class="chart-container" v-loading="chartLoading">
              <div ref="budgetUsageChartRef" class="chart" style="height: 300px;"></div>
              <div v-if="showEmptyBudgetData" class="empty-data">
                <el-empty description="暂无经费使用数据" :image-size="100"></el-empty>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import { getProjectDetail, getProjectExpenseStats } from '@/api/project'
import { getFileDownloadUrl } from '@/api/upload'
import { Download, Warning, InfoFilled, CircleCheck } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const projectId = computed(() => route.params.id)
const loading = ref(false)
const chartLoading = ref(false)
const budgetUsageChartRef = ref(null)
let budgetUsageChart = null

// 判断当前用户是否为管理员
const isAdmin = computed(() => userStore.isAdmin)

// 判断当前用户是否可以编辑项目
const canEdit = computed(() => {
  // 管理员不能编辑项目
  if (isAdmin.value) return false
  
  // 研究人员可以编辑项目
  return userStore.roles.includes('ROLE_RESEARCHER')
})

// 项目详情信息
const projectInfo = ref({
  id: null,
  name: '',
  type: '',
  leaderName: '',
  startDate: '',
  endDate: '',
  budget: 0,
  usedBudget: 0,
  status: '',
  description: '',
  researchContent: '',
  expectedResults: '',
  filePath: '',
  team: [],
  milestones: [],
  budgetItems: [],
  showEmptyBudget: false,
  completionReportPath: '',
  completionAuditStatus: '',
  completionAuditComment: '',
  completionReportSubmitTime: ''
})

// 项目经费使用统计数据
const expenseStats = ref([])

// 计算预算使用百分比
const budgetPercentage = computed(() => {
  if (!projectInfo.value.budget || projectInfo.value.budget === 0) return 0;
  return Math.round((projectInfo.value.usedBudget / projectInfo.value.budget) * 100);
})

// 判断是否显示空状态
const showEmptyBudgetData = computed(() => {
  // 在加载过程中不显示空状态
  if (chartLoading.value) return false;
  
  // 有实际数据，不显示空状态
  if (hasBudgetData.value) return false;
  
  // 特殊情况：如果用户明确设置了不使用默认数据，才显示空状态
  return projectInfo.value.showEmptyBudget === true;
})

// 判断是否有经费数据
const hasBudgetData = computed(() => {
  return projectInfo.value.budgetItems && projectInfo.value.budgetItems.length > 0;
})

// 格式化百分比显示
const percentFormat = (val) => {
  return `已使用 ${parseFloat(val).toFixed(1)}%`;
}

// 获取项目类型标签样式
const getTypeTag = (type) => {
  const typeMap = {
    'SCIENTIFIC': 'primary',
    'TECHNOLOGICAL': 'success',
    'EDUCATIONAL': 'warning',
    'CULTURAL': 'info',
    'OTHER': 'info'
  }
  return typeMap[type] || 'info'
}

// 获取项目类型显示文本
const getTypeLabel = (type) => {
  const typeMap = {
    'SCIENTIFIC': '科学研究',
    'TECHNOLOGICAL': '技术开发',
    'EDUCATIONAL': '教育项目',
    'CULTURAL': '文化项目',
    'OTHER': '其他'
  }
  return typeMap[type] || '其他'
}

// 获取状态标签样式
const getStatusTag = (status) => {
  const statusMap = {
    'DRAFT': 'info',
    'SUBMITTED': 'warning',
    'APPROVED': 'success',
    'REJECTED': 'danger',
    'IN_PROGRESS': 'primary',
    'COMPLETED': 'success',
    'CANCELLED': 'info',
    'planning': 'info',
    'active': 'success',
    'completed': 'success',
    'suspended': 'warning',
    'pending_completion': 'warning',
    'completion_review': 'primary',
    'archived': 'success'
  }
  return statusMap[status] || 'info'
}

// 获取状态显示文本
const getStatusLabel = (status) => {
  const statusMap = {
    'DRAFT': '草稿',
    'SUBMITTED': '已提交',
    'APPROVED': '已批准',
    'REJECTED': '已驳回',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成',
    'CANCELLED': '已取消',
    'planning': '筹划中',
    'active': '进行中',
    'completed': '已完成',
    'suspended': '已暂停',
    'pending_completion': '待结题',
    'completion_review': '待结题审核',
    'archived': '已归档'
  }
  return statusMap[status] || '未知状态'
}

// 获取进度条状态
const getProgressStatus = (percentage) => {
  if (percentage < 80) return ''
  if (percentage < 100) return 'warning'
  return 'success'
}

// 格式化金额
const formatCurrency = (value) => {
  if (!value && value !== 0) return '¥0.00'
  return `¥${Number(value).toFixed(2)}`
}

// 编辑项目
const handleEdit = () => {
  router.push(`/project/edit/${projectId.value}`)
}

// 返回项目列表
const goBack = () => {
  // 如果是管理员，返回上一级
  if (useUserStore().isAdmin) {
    router.go(-1)
  } else {
    // 如果不是管理员，跳转到项目列表
    router.push('/project/list')
  }
}

// 获取项目详情
const fetchProjectDetail = async () => {
  if (!projectId.value) return;
  
  console.log('获取项目详情，ID:', projectId.value);
  loading.value = true;
  chartLoading.value = true;
  
  try {
    // 调用API获取项目详情
    const response = await getProjectDetail(projectId.value);
    
    if (response.code === 200 && response.data) {
      projectInfo.value = {
        ...response.data,
        team: response.data.team || [],
        milestones: response.data.milestones || [],
        budgetItems: response.data.budgetItems || []
      };
      
      // 获取项目经费使用统计
      fetchExpenseStats();
      
    } else {
      ElMessage.error('获取项目详情失败: ' + (response.message || '未知错误'));
      loading.value = false;
      chartLoading.value = false;
    }
  } catch (error) {
    console.error('获取项目数据出错:', error);
    ElMessage.error('获取项目数据失败: ' + (error.message || '未知错误'));
    loading.value = false;
    chartLoading.value = false;
  }
}

// 获取项目经费使用统计
const fetchExpenseStats = async () => {
  try {
    const response = await getProjectExpenseStats(projectId.value);
    
    if (response.code === 200 && response.data) {
      expenseStats.value = response.data;
    } else {
      console.warn('获取项目经费使用统计失败:', response.message || '未知错误');
      expenseStats.value = [];
    }
  } catch (error) {
    console.error('获取项目经费使用统计失败:', error);
    expenseStats.value = [];
  } finally {
    // 等待DOM更新后初始化图表
    await nextTick();
    initBudgetUsageChart();
    loading.value = false;
    chartLoading.value = false;
  }
}

// 路由参数变化时重新获取项目详情
watch(() => route.params.id, (newId) => {
  if (newId) {
    fetchProjectDetail();
  }
}, { immediate: true });

// 监听预算数据变化，更新图表
watch(() => projectInfo.value.budgetItems, () => {
  if (budgetUsageChartRef.value) {
    nextTick(() => {
      initBudgetUsageChart();
    });
  }
}, { deep: true });

// 页面挂载时获取项目详情
onMounted(() => {
  fetchProjectDetail();
});

// 页面卸载时清理图表实例
onUnmounted(() => {
  if (budgetUsageChart) {
    budgetUsageChart.dispose();
    budgetUsageChart = null;
  }
  
  // 移除监听事件
  window.removeEventListener('resize', () => {
    budgetUsageChart && budgetUsageChart.resize();
  });
});

// 下载项目申请书
const downloadProjectFile = () => {
  if (!projectInfo.value.filePath) {
    ElMessage.warning('项目申请书不存在');
    return;
  }

  try {
    // 构建下载文件名
    const fileName = `项目申请书_${projectInfo.value.name}.pdf`;
    
    const filePath = '/uploads/project-files/' + projectInfo.value.filePath
    // 获取下载链接
    const downloadUrl = getFileDownloadUrl(filePath, fileName);
    
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

// 下载结题报告
const downloadCompletionReport = () => {
  if (!projectInfo.value.completionReportPath) {
    ElMessage.warning('结题报告不存在');
    return;
  }

  try {
    // 构建下载文件名
    const fileName = `结题报告_${projectInfo.value.name}.pdf`;
    
    const filePath = '/uploads/project-files/' + projectInfo.value.completionReportPath;
    // 获取下载链接
    const downloadUrl = getFileDownloadUrl(filePath, fileName);
    
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

// 初始化经费使用占比饼图
const initBudgetUsageChart = () => {
  if (!budgetUsageChartRef.value) return;
  
  // 如果已经存在图表实例，先销毁
  if (budgetUsageChart) {
    budgetUsageChart.dispose();
  }
  
  // 创建新的图表实例
  budgetUsageChart = echarts.init(budgetUsageChartRef.value);
  
  // 处理经费统计数据
  let budgetData = [];
  let noData = true;
  
  if (expenseStats.value && expenseStats.value.length > 0) {
    // 使用后端返回的真实经费使用数据
    budgetData = expenseStats.value.map(item => ({
      name: getExpenseTypeLabel(item.type),
      value: item.amount
    }));
    noData = false;
  }
  
  // 计算总金额
  const totalBudget = budgetData.reduce((sum, item) => sum + item.value, 0);
  
  if (totalBudget === 0) {
    // 如果没有数据，显示空状态
    budgetUsageChart.setOption({
      title: {
        text: '暂无经费使用数据',
        left: 'center',
        top: 'center',
        textStyle: {
          fontSize: 16,
          color: '#909399'
        }
      }
    });
    return;
  }
  
  // 饼图配置
  const option = {
    title: {
      text: `总计: ${formatCurrency(totalBudget)}`,
      left: 'center',
      top: '0%',
      textStyle: {
        fontSize: 14,
        color: '#666'
      }
    },
    tooltip: {
      trigger: 'item',
      formatter: function(params) {
        // 计算百分比
        const percent = ((params.value / totalBudget) * 100).toFixed(1);
        // 格式化金额
        const formattedValue = formatCurrency(params.value);
        return `${params.seriesName}<br/>${params.name}: ${formattedValue} (${percent}%)`;
      }
    },
    legend: {
      orient: 'horizontal',
      bottom: '0%',
      left: 'center',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: {
        fontSize: 12
      }
    },
    series: [
      {
        name: '经费使用',
        type: 'pie',
        radius: ['40%', '65%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 6,
          borderColor: '#fff',
          borderWidth: 1
        },
        label: {
          show: false
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '14',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: budgetData
      }
    ]
  };
  
  // 使用配置项显示图表
  budgetUsageChart.setOption(option);
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    budgetUsageChart && budgetUsageChart.resize();
  });
}

// 获取经费申请类型显示文本
const getExpenseTypeLabel = (type) => {
  const types = {
    'equipment': '设备费',
    'material': '材料费',
    'test': '测试化验费',
    'travel': '差旅费',
    'meeting': '会议费',
    'labor': '劳务费',
    'consultation': '专家咨询费',
    'other': '其他费用'
  }
  return types[type] || '未知类型'
}

// 获取结题审核状态标签样式
const getCompletionAuditStatusTag = (status) => {
  const statusMap = {
    'pending': 'info',
    'approved': 'success',
    'rejected': 'danger'
  }
  return statusMap[status] || 'info'
}

// 获取结题审核状态显示文本
const getCompletionAuditStatusLabel = (status) => {
  const statusMap = {
    'pending': '待审核',
    'approved': '已通过',
    'rejected': '未通过'
  }
  return statusMap[status] || '未知状态'
}
</script>

<style scoped>
.project-detail-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.title-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 22px;
  font-weight: bold;
}

.detail-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-section {
  margin-top: 20px;
}

.progress-title {
  margin-bottom: 10px;
  font-weight: bold;
}

.content-section {
  margin-bottom: 20px;
}

.section-title {
  font-weight: bold;
  margin-bottom: 10px;
  font-size: 16px;
}

.section-content {
  line-height: 1.6;
  white-space: pre-line;
}

.team-members {
  display: flex;
  flex-direction: column;
}

.team-member {
  display: flex;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #ebeef5;
}

.team-member:last-child {
  border-bottom: none;
}

.member-info {
  margin-left: 10px;
}

.member-name {
  font-weight: bold;
}

.member-role {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.completion-audit-info {
  margin-top: 15px;
  padding: 10px;
  border-radius: 4px;
  background-color: #f8f8f8;
}

.audit-status, .audit-comment, .submit-time {
  margin-bottom: 8px;
}

.audit-comment {
  white-space: pre-wrap;
  color: #606266;
}

.submit-time {
  color: #909399;
  font-size: 13px;
}

.milestone-desc {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.chart-container {
  height: 300px;
  position: relative;
}

.chart {
  height: 100%;
  width: 100%;
}

.empty-data {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 100%;
  text-align: center;
  z-index: 10;
  background-color: rgba(255, 255, 255, 0.8);
}

.status-tips {
  margin: 15px 0;
}

.alert-content {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style> 