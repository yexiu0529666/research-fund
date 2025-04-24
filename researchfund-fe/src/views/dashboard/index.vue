<template>
  <div class="dashboard-container">
    <!-- 欢迎信息 -->
    <div class="welcome-section">
      <div class="welcome-info">

        <div class="welcome-text">
          <h1>您好，{{ userInfo.name }}</h1>
          <p>{{ welcomeMessage }}</p>
        </div>
      </div>
      <div class="date-info">
        <p class="current-time">{{ currentTime }}</p>
        <p class="current-date">{{ currentDate }}</p>
      </div>
    </div>
    

    
    <!-- 主要内容区域 -->
    <el-row :gutter="20" class="dashboard-content">
      <!-- 项目概览 -->
      <el-col :xs="24" :md="24">
        <el-card class="project-overview">
          <template #header>
            <div class="card-header">
              <span>项目概览</span>
              <el-button text @click="$router.push('/project/list')">查看全部</el-button>
            </div>
          </template>
          <div v-if="projectList.length > 0" class="project-list">
            <div v-for="(project, index) in projectList" :key="index" class="project-item">
              <div class="project-info">
                <span class="project-name">{{ project.name }}</span>
                <el-tag size="small" :type="getProjectStatusType(project.status)">{{ getProjectStatusLabel(project.status) }}</el-tag>
              </div>
              <div class="project-progress">
                <span class="progress-text">预算使用：{{ project.budgetUsed }} / {{ project.budgetTotal }}</span>
                <el-progress :percentage="project.budgetPercentage" :status="getProgressStatus(project.budgetPercentage)" />
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无项目数据" />
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-section">
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>各类型项目占比</span>
            </div>
          </template>
          <div class="chart-container">
            <div ref="projectTypeChartRef" class="chart-inner"></div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :lg="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>科研成果数量</span>
            </div>
          </template>
          <div class="chart-container">
            <div ref="researchResultsChartRef" class="chart-inner"></div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick, onUnmounted } from 'vue'
import { Document, Money, CreditCard, PieChart } from '@element-plus/icons-vue'
import { useUserStore } from '@/store/modules/user'
import * as echarts from 'echarts'
import { getDashboardProjects, getDashboardStats, getProjectTypeStats, getResearchResults } from '@/api/dashboard'
import { ElMessage } from 'element-plus'

const userStore = useUserStore()
const projectTypeChartRef = ref(null)
let projectTypeChart = null
const researchResultsChartRef = ref(null)
let researchResultsChart = null

// 判断是否为管理员
const isAdmin = computed(() => userStore.isAdmin)

// 用户信息
const userInfo = computed(() => {
  return {
    name: userStore.name || '用户',
  }
})

// 生成欢迎语
const welcomeMessage = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) {
    return '夜深了，注意休息！'
  } else if (hour < 9) {
    return '早上好，祝您有个愉快的一天！'
  } else if (hour < 12) {
    return '上午好，工作顺利！'
  } else if (hour < 14) {
    return '中午好，别忘了休息一会儿！'
  } else if (hour < 18) {
    return '下午好，加油！'
  } else if (hour < 22) {
    return '晚上好，辛苦了！'
  } else {
    return '夜深了，注意休息！'
  }
})

// 当前日期和时间
const currentTime = ref('')
const currentDate = ref('')

// 更新时间
const updateTime = () => {
  const now = new Date()
  const hour = String(now.getHours()).padStart(2, '0')
  const minute = String(now.getMinutes()).padStart(2, '0')
  currentTime.value = `${hour}:${minute}`
  
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const weekDays = ['星期日', '星期一', '星期二', '星期三', '星期四', '星期五', '星期六']
  const weekDay = weekDays[now.getDay()]
  currentDate.value = `${year}年${month}月${day}日 ${weekDay}`
}

// 统计数据
const statisticsData = ref({
  projectCount: 0,
  pendingExpenseCount: 0,
  pendingPaymentCount: 0,
  budgetUsageRate: 0
})

// 待办任务列表
const taskList = ref([])

// 项目列表
const projectList = ref([])

// 项目类型数据
const projectTypeData = ref([])

// 科研成果数据
const researchResultsData = ref([])

// 定时器
let timeInterval = null

// 加载仪表盘数据
const loadDashboardData = async () => {
  try {
    // 加载项目概览
    const projectsResponse = await getDashboardProjects(isAdmin.value)
    if (projectsResponse.code === 200) {
      projectList.value = projectsResponse.data || []
      console.log('获取项目概览成功:', projectList.value)
    } else {
      console.error('获取项目概览失败:', projectsResponse.message)
    }
    
    // 加载统计数据
    const statsResponse = await getDashboardStats(isAdmin.value)
    if (statsResponse.code === 200) {
      statisticsData.value = statsResponse.data || {
        projectCount: 0,
        pendingExpenseCount: 0,
        pendingPaymentCount: 0,
        budgetUsageRate: 0
      }
      console.log('获取统计数据成功:', statisticsData.value)
    } else {
      console.error('获取统计数据失败:', statsResponse.message)
    }
    
    // 加载项目类型统计
    const projectTypeResponse = await getProjectTypeStats(isAdmin.value)
    if (projectTypeResponse.code === 200) {
      projectTypeData.value = projectTypeResponse.data || []
      console.log('获取项目类型统计成功:', projectTypeData.value)
      // 更新项目类型图表
      nextTick(() => {
        initProjectTypeChart()
      })
    } else {
      console.error('获取项目类型统计失败:', projectTypeResponse.message)
    }
    
    // 加载研究成果统计
    const resultsResponse = await getResearchResults(isAdmin.value)
    if (resultsResponse.code === 200) {
      researchResultsData.value = resultsResponse.data || []
      console.log('获取研究成果统计成功:', researchResultsData.value)
      // 更新研究成果图表
      nextTick(() => {
        initResearchResultsChart()
      })
    } else {
      console.error('获取研究成果统计失败:', resultsResponse.message)
    }
  } catch (error) {
    console.error('加载仪表盘数据出错:', error)
    ElMessage.error('获取仪表盘数据失败，请稍后再试')
  }
}


// 获取项目状态类型
const getProjectStatusType = (status) => {
  const statusMap = {
    active: 'success',
    planning: 'info',
    completed: 'info',
    suspended: 'warning'
  }
  return statusMap[status] || 'info'
}

// 获取项目状态标签
const getProjectStatusLabel = (status) => {
  const statusMap = {
    active: '进行中',
    planning: '筹划中',
    completed: '已完成',
    suspended: '已暂停'
  }
  return statusMap[status] || '未知'
}

// 获取进度条状态
const getProgressStatus = (percentage) => {
  if (percentage >= 90) {
    return 'exception'
  } else if (percentage >= 80) {
    return 'warning'
  } else {
    return ''
  }
}

// 初始化项目类型饼图
const initProjectTypeChart = () => {
  if (!projectTypeChartRef.value) return
  
  // 如果已经存在图表实例，先销毁
  if (projectTypeChart) {
    projectTypeChart.dispose()
  }
  
  // 创建新的图表实例
  projectTypeChart = echarts.init(projectTypeChartRef.value)
  
  // 检查所有值是否为零或数据为空
  const noData = !projectTypeData.value.length || projectTypeData.value.every(item => item.value === 0)
  
  // 如果没有数据，显示暂无数据提示
  if (noData) {
    projectTypeChart.setOption({
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: {
          color: '#999',
          fontSize: 16,
          fontWeight: 'normal'
        }
      }
    })
    return
  }
  
  // 图表配置项
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c}个 ({d}%)'
    },
    legend: {
      orient: 'horizontal',
      bottom: 10,
      data: projectTypeData.value.map(item => item.name)
    },
    series: [
      {
        name: '项目类型',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
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
        data: projectTypeData.value,
        color: ['#91cc75', '#fac858', '#5470c6']
      }
    ]
  }
  
  // 使用配置项显示图表
  projectTypeChart.setOption(option)
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    projectTypeChart && projectTypeChart.resize()
  })
}

// 初始化科研成果柱状图
const initResearchResultsChart = () => {
  if (!researchResultsChartRef.value) return
  
  // 如果已经存在图表实例，先销毁
  if (researchResultsChart) {
    researchResultsChart.dispose()
  }
  
  // 创建新的图表实例
  researchResultsChart = echarts.init(researchResultsChartRef.value)
  
  // 检查是否所有值都为零或者没有数据
  const noData = !researchResultsData.value.length || researchResultsData.value.every(item => item.value === 0)
  
  // 如果没有数据，显示暂无数据提示
  if (noData) {
    researchResultsChart.setOption({
      title: {
        text: '暂无数据',
        left: 'center',
        top: 'center',
        textStyle: {
          color: '#999',
          fontSize: 16,
          fontWeight: 'normal'
        }
      }
    })
    return
  }
  
  // 图表配置项
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      },
      formatter: '{b}: {c}个'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '10%',
      top: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: researchResultsData.value.map(item => item.name),
      axisTick: {
        alignWithLabel: true
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '{value}个'
      }
    },
    series: [
      {
        name: '成果数量',
        type: 'bar',
        barWidth: '60%',
        data: researchResultsData.value.map(item => item.value),
        itemStyle: {
          color: function(params) {
            const colorList = ['#5470c6', '#91cc75', '#fac858', '#ee6666', '#73c0de'];
            return colorList[params.dataIndex % colorList.length];
          }
        }
      }
    ]
  }
  
  // 使用配置项显示图表
  researchResultsChart.setOption(option)
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    researchResultsChart && researchResultsChart.resize()
  })
}

// 初始化
onMounted(() => {
  // 更新时间
  updateTime()
  // 设置定时器，每分钟更新一次时间
  timeInterval = setInterval(updateTime, 60000)
  
  // 加载仪表盘数据
  loadDashboardData()
  
  // 初始化图表
  nextTick(() => {
    initProjectTypeChart()
    initResearchResultsChart()
  })
})

// 组件卸载时清除定时器和图表实例
onUnmounted(() => {
  // 清除定时器
  if (timeInterval) {
    clearInterval(timeInterval)
  }
  
  // 清除图表实例
  if (projectTypeChart) {
    projectTypeChart.dispose()
    projectTypeChart = null
  }
  
  if (researchResultsChart) {
    researchResultsChart.dispose()
    researchResultsChart = null
  }
  
  // 移除窗口大小变化监听
  window.removeEventListener('resize', () => {
    projectTypeChart && projectTypeChart.resize()
    researchResultsChart && researchResultsChart.resize()
  })
})
</script>

<style scoped>
.dashboard-container {
  padding: 10px;
}

.welcome-section {
  background-color: #fff;
  border-radius: 4px;
  padding: 20px;
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.welcome-info {
  display: flex;
  align-items: center;
}

.user-avatar {
  margin-right: 20px;
}

.welcome-text h1 {
  font-size: 24px;
  color: #303133;
  margin: 0 0 10px 0;
}

.welcome-text p {
  font-size: 14px;
  color: #606266;
  margin: 0;
}

.date-info {
  text-align: right;
}

.current-time {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.current-date {
  font-size: 14px;
  color: #606266;
  margin: 5px 0 0 0;
}

.statistics-cards {
  margin-bottom: 20px;
}

.statistics-card {
  height: 100%;
}

.card-body {
  display: flex;
  align-items: center;
  padding-bottom: 15px;
}

.card-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 15px;
}

.card-icon .el-icon {
  font-size: 24px;
  color: #fff;
}

.card-icon.blue {
  background-color: #409EFF;
}

.card-icon.green {
  background-color: #67C23A;
}

.card-icon.orange {
  background-color: #E6A23C;
}

.card-icon.purple {
  background-color: #8E44AD;
}

.card-info {
  flex: 1;
}

.card-title {
  font-size: 14px;
  color: #909399;
  margin: 0 0 5px 0;
}

.card-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin: 0;
}

.card-footer {
  border-top: 1px solid #EBEEF5;
  padding-top: 10px;
  text-align: right;
}

.dashboard-content {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-list, .project-list {
  margin-bottom: 10px;
}

.task-item, .project-item {
  padding: 12px 0;
  border-bottom: 1px solid #EBEEF5;
}

.task-item:last-child, .project-item:last-child {
  border-bottom: none;
}

.task-info {
  display: flex;
  align-items: center;
  margin-bottom: 5px;
}

.task-title {
  margin-left: 10px;
  font-size: 14px;
}

.task-meta {
  display: flex;
  justify-content: space-between;
  font-size: 12px;
  color: #909399;
}

.project-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.project-name {
  font-size: 14px;
  font-weight: bold;
}

.project-progress {
  margin-top: 5px;
}

.progress-text {
  display: block;
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.chart-section {
  margin-bottom: 20px;
}

.chart-card {
  margin-bottom: 20px;
  height: 350px;
}

.chart-container {
  height: 280px;
  display: flex;
  justify-content: center;
  align-items: center;
}

.chart-inner {
  width: 100%;
  height: 100%;
}

.chart-placeholder {
  width: 100%;
  padding: 20px;
}
</style> 