<template>
  <div class="project-edit-container">
    <div class="page-header">
      <div class="page-title">编辑项目</div>
    </div>
    
    <el-card class="form-container" v-loading="loading">
      <el-form 
        ref="projectFormRef" 
        :model="projectForm" 
        :rules="rules" 
        label-width="100px"
        :disabled="submitLoading"
      >
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="projectForm.name" placeholder="请输入项目名称" />
        </el-form-item>
        
        <el-form-item label="项目类型" prop="type">
          <el-select v-model="projectForm.type" placeholder="请选择项目类型">
            <el-option label="校级项目" value="school" />
            <el-option label="横向项目" value="horizontal" />
            <el-option label="纵向项目" value="vertical" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="经费来源" prop="fundingSources">
          <el-select v-model="projectForm.fundingSources" multiple placeholder="请选择经费来源（可多选）">
            <el-option label="财政经费" value="fiscal" />
            <el-option label="校配套经费" value="school" />
            <el-option label="其他经费" value="other" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="负责人" prop="leaderName">
          <el-input v-model="projectForm.leaderName" placeholder="请输入负责人姓名" />
        </el-form-item>
        
        <el-form-item label="项目周期" prop="projectPeriod">
          <el-date-picker
            v-model="projectForm.projectPeriod"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            :disabledDate="disabledDate"
          />
        </el-form-item>
        
        <!-- 预算科目 -->
        <el-form-item label="预算科目" prop="budgetItems">
          <div class="budget-container">
            <div v-for="(item, index) in projectForm.budgetItems" :key="index" class="budget-item">
              <el-row :gutter="10">
                <el-col :span="10">
                  <el-select
                    v-model="item.category"
                    placeholder="选择科目名称"
                    style="width: 100%;"
                  >
                    <el-option label="设备费" value="设备费" />
                    <el-option label="材料费" value="材料费" />
                    <el-option label="测试化验费" value="测试化验费" />
                    <el-option label="差旅费" value="差旅费" />
                    <el-option label="会议费" value="会议费" />
                    <el-option label="劳务费" value="劳务费" />
                    <el-option label="专家咨询费" value="专家咨询费" />
                    <el-option label="其他费用" value="其他费用" />
                  </el-select>
                </el-col>
                <el-col :span="10">
                  <el-input-number
                    v-model="item.amount"
                    :min="0"
                    :precision="2"
                    :step="1000"
                    :controls="false"
                    style="width: 100%"
                    placeholder="金额"
                  />
                </el-col>
                <el-col :span="4">
                  <el-button 
                    type="danger" 
                    circle 
                    icon="Delete" 
                    size="small"
                    @click="removeBudgetItem(index)"
                  />
                </el-col>
              </el-row>
            </div>
            <div class="budget-actions">
              <el-button type="primary" plain size="small" @click="addBudgetItem">
                <el-icon><Plus /></el-icon>添加科目
              </el-button>
            </div>
            <div class="budget-total">
              总预算：{{ formatCurrency(totalBudget) }}
            </div>
          </div>
        </el-form-item>

        <!-- 项目团队成员 -->
        <el-form-item label="项目团队" prop="team">
          <div class="team-container">
            <div v-for="(member, index) in projectForm.team" :key="index" class="team-member">
              <el-row :gutter="10">
                <el-col :span="9">
                  <el-input
                    v-model="member.name"
                    placeholder="姓名"
                    @change="validateTeamMembers"
                  />
                </el-col>
                <el-col :span="9">
                  <el-input
                    v-model="member.role"
                    placeholder="角色职责"
                    @change="validateTeamMembers"
                  />
                </el-col>
                <el-col :span="6">
                  <el-button 
                    type="danger" 
                    circle 
                    icon="Delete" 
                    size="small"
                    @click="removeMember(index)"
                  />
                </el-col>
              </el-row>
            </div>
            <div class="team-actions">
              <el-button type="primary" plain size="small" @click="addMember">
                <el-icon><Plus /></el-icon>添加成员
              </el-button>
            </div>
            <div v-if="teamError" class="error-message">{{ teamError }}</div>
          </div>
        </el-form-item>
        
        <!-- 项目申请书 -->
        <el-form-item label="项目申请书" prop="filePath">
          <div class="upload-container">
            <div v-if="projectForm.filePath" class="file-info">
              <el-link type="primary" >{{ originalFileName || getFileNameFromPath(projectForm.filePath) }}</el-link>
              <el-button type="primary" link @click="replaceFile">替换文件</el-button>
            </div>
            <el-upload
              v-else
              ref="upload"
              action="#"
              :http-request="handleFileUpload"
              :show-file-list="true"
              :limit="1"
              :file-list="fileList"
              :on-change="handleFileChange"
              :on-remove="handleFileRemove"
              :auto-upload="true"
            >
              <template #trigger>
                <el-button type="primary">选择文件</el-button>
              </template>
              <template #tip>
                <div class="el-upload__tip">请上传DOCX、PDF或PPT格式的项目申请书</div>
              </template>
              <div v-if="uploadStatus === 'uploading'" class="upload-status">
                <el-icon class="loading"><Loading /></el-icon> 正在上传...
              </div>
              <div v-else-if="uploadStatus === 'success'" class="upload-status success">
                <el-icon><Check /></el-icon> 上传成功
              </div>
              <div v-else-if="uploadStatus === 'error'" class="upload-status error">
                <el-icon><Close /></el-icon> 上传失败
              </div>
            </el-upload>
          </div>
        </el-form-item>
        
        <el-form-item label="项目简介" prop="description">
          <el-input
            v-model="projectForm.description"
            type="textarea"
            :rows="4"
            placeholder="请输入项目简介"
          />
        </el-form-item>

        <el-form-item label="研究内容" prop="researchContent">
          <el-input
            v-model="projectForm.researchContent"
            type="textarea"
            :rows="4"
            placeholder="请输入研究内容"
          />
        </el-form-item>

        <el-form-item label="预期成果" prop="expectedResults">
          <el-input
            v-model="projectForm.expectedResults"
            type="textarea"
            :rows="4"
            placeholder="请输入预期成果"
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitLoading">保存修改</el-button>
          <el-button @click="goBack">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter, useRoute } from 'vue-router'
import { getProjectDetail, updateProject } from '@/api/project'
import { uploadProjectFile } from '@/api/upload'
import { useUserStore } from '@/store/modules/user'
import { Plus, Check, Close, Loading } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const projectFormRef = ref(null)
const submitLoading = ref(false)
const loading = ref(false)
const teamError = ref('')
const fileList = ref([])
const projectFile = ref(null)
const uploadStatus = ref('')
const fileSize = ref(null)
const projectId = ref(null)
const originalFileName = ref('') // 保存原始文件名

// 表单数据
const projectForm = reactive({
  name: '',
  type: '',
  fundingSources: [],
  leaderName: '',
  projectPeriod: null,
  budget: 0,
  usedBudget: 0, // 添加已用预算字段
  status: 'planning', // 添加状态字段，默认为planning
  auditStatus: 'pending', // 添加审核状态字段，默认为pending
  description: '',
  researchContent: '',
  expectedResults: '',
  team: [], // 项目团队成员
  filePath: '', // 文件路径
  budgetItems: [{ category: '', amount: 0 }] // 预算科目
})

// 计算总预算
const totalBudget = computed(() => {
  return projectForm.budgetItems.reduce((sum, item) => sum + (Number(item.amount) || 0), 0)
})

// 格式化货币
const formatCurrency = (value) => {
  if (!value && value !== 0) return '¥0.00'
  return '¥' + Number(value).toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

// 添加预算科目
function addBudgetItem() {
  projectForm.budgetItems.push({
    category: '',
    amount: 0
  })
}

// 移除预算科目
function removeBudgetItem(index) {
  projectForm.budgetItems.splice(index, 1)
  if (projectForm.budgetItems.length === 0) {
    addBudgetItem()
  }
}

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择项目类型', trigger: 'change' }
  ],
  fundingSources: [
    { required: true, message: '请选择经费来源', trigger: 'change' }
  ],
  leaderName: [
    { required: true, message: '请输入负责人姓名', trigger: 'blur' }
  ],
  projectPeriod: [
    { required: true, message: '请选择项目周期', trigger: 'change' }
  ],
  budgetItems: [
    { 
      required: true,
      validator: (rule, value, callback) => {
        if (!value || value.length === 0) {
          callback(new Error('请至少添加一个预算科目'))
          return
        }
        
        for (let i = 0; i < value.length; i++) {
          const item = value[i]
          if (!item.category) {
            callback(new Error('请填写所有预算科目的名称'))
            return
          }
          if (!item.amount) {
            callback(new Error('请填写所有预算科目的金额'))
            return
          }
        }
        
        callback()
      }, 
      trigger: 'change' 
    }
  ],
  description: [
    { required: true, message: '请输入项目简介', trigger: 'blur' }
  ],
  researchContent: [
    { required: true, message: '请输入研究内容', trigger: 'blur' }
  ],
  expectedResults: [
    { required: true, message: '请输入预期成果', trigger: 'blur' }
  ],
  filePath: [
    { required: true, message: '请上传项目申请书', trigger: 'change' }
  ]
}

// 添加团队成员
function addMember() {
  projectForm.team.push({
    name: '',
    role: ''
  });
  validateTeamMembers();
}

// 移除团队成员
function removeMember(index) {
  projectForm.team.splice(index, 1);
  validateTeamMembers();
}

// 验证团队成员
function validateTeamMembers() {
  teamError.value = '';
  
  // 至少需要一个团队成员
  if (projectForm.team.length === 0) {
    teamError.value = '请至少添加一个团队成员';
    return false;
  }
  
  // 检查每个成员的姓名和角色是否都已填写
  for (let i = 0; i < projectForm.team.length; i++) {
    const member = projectForm.team[i];
    if (!member.name || !member.role) {
      teamError.value = '请填写所有团队成员的姓名和角色';
      return false;
    }
  }
  
  return true;
}

// 处理文件变更
const handleFileChange = (file) => {
  if (file.raw) {
    const isValidType = ['application/vnd.openxmlformats-officedocument.wordprocessingml.document', 
                          'application/pdf', 
                          'application/vnd.ms-powerpoint',
                          'application/vnd.openxmlformats-officedocument.presentationml.presentation'].includes(file.raw.type);
    
    if (!isValidType) {
      ElMessage.error('请上传Word、PDF或PPT格式的文件!');
      fileList.value = [];
      return false;
    }
    
    // 10MB 限制
    const isLt10M = file.size / 1024 / 1024 < 10;
    if (!isLt10M) {
      ElMessage.error('上传文件大小不能超过 10MB!');
      fileList.value = [];
      return false;
    }
    
    projectFile.value = file.raw;
    fileList.value = [file];
    fileSize.value = (file.size / 1024 / 1024).toFixed(2) + 'MB';
    originalFileName.value = file.raw.name;
    
    // 通过el-upload的自动上传功能会自动调用handleFileUpload
    return true;
  }
  return false;
}

// 处理文件删除
const handleFileRemove = () => {
  projectFile.value = null;
  fileList.value = [];
  fileSize.value = null;
}

// 处理文件上传
const handleFileUpload = async (options) => {
  // 如果是el-upload通过http-request调用，则从options中获取文件
  const fileToUpload = options && options.file ? options.file : projectFile.value;
  
  if (!fileToUpload) {
    ElMessage.warning('请先选择文件');
    return;
  }
  
  // 保存原始文件名
  if (options && options.file && options.file.name) {
    originalFileName.value = options.file.name;
  }
  
  uploadStatus.value = 'uploading';
  
  try {
    const formData = new FormData();
    formData.append('file', fileToUpload);
    
    const response = await uploadProjectFile(formData);
    
    if (response.code === 200 && response.data) {
      // 上传成功后，更新filePath会触发视图切换到显示文件信息
      projectForm.filePath = response.data;
      uploadStatus.value = 'success';
      
      // 清空fileList，不再需要在上传控件中显示
      fileList.value = [];
      
      // 判断是否文件已存在
      if (response.message && response.message.includes('已存在')) {
        ElMessage.success('文件已存在，无需重新上传');
      } else {
        ElMessage.success('文件上传成功');
      }
      
      // 确保表单验证状态更新
      projectFormRef.value?.validateField('filePath');
      
      return response.data;
    } else {
      uploadStatus.value = 'error';
      ElMessage.error('文件上传失败: ' + (response.message || '未知错误'));
      return null;
    }
  } catch (error) {
    console.error('文件上传异常:', error);
    uploadStatus.value = 'error';
    ElMessage.error('文件上传异常: ' + (error.message || '未知错误'));
    return null;
  }
}

// 替换文件
const replaceFile = () => {
  projectForm.filePath = '';
  fileList.value = [];
  projectFile.value = null;
  originalFileName.value = '';
}

// 从文件路径获取文件名
const getFileNameFromPath = (path) => {
  if (!path) return '';
  const parts = path.split('/');
  return parts[parts.length - 1];
}

// 下载文件
const downloadFile = () => {
  if (!projectForm.filePath) {
    ElMessage.warning('没有可下载的文件');
    return;
  }
  
  window.open(`/api/upload/download?filePath=${encodeURIComponent(projectForm.filePath)}`, '_blank');
}

// 日期禁用函数
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7; // 禁用今天之前的日期
}

// 提交表单
const submitForm = async () => {
  if (!projectFormRef.value) return;
  
  projectFormRef.value.validate(async (valid) => {
    if (!valid) {
      ElMessage.error('请完善表单信息');
      return;
    }
    
    submitLoading.value = true;
    
    try {
      // 准备提交数据
      const submitData = {
        id: projectId.value,
        name: projectForm.name,
        type: projectForm.type,
        leaderName: projectForm.leaderName,
        startDate: projectForm.projectPeriod[0],
        endDate: projectForm.projectPeriod[1],
        budget: totalBudget.value, // 使用计算的总预算
        usedBudget: projectForm.usedBudget,
        status: projectForm.status,
        auditStatus: projectForm.auditStatus,
        description: projectForm.description,
        researchContent: projectForm.researchContent,
        expectedResults: projectForm.expectedResults,
        filePath: projectForm.filePath,
        team: projectForm.team, // 团队成员
        fundingSources: projectForm.fundingSources,
        budgetItems: projectForm.budgetItems // 添加预算科目数据
      };
      
      // 调用更新API
      await updateProject(projectId.value, submitData);
      
      ElMessage.success('项目更新成功');
      router.push(`/project/detail/${projectId.value}`);
    } catch (error) {
      console.error('更新项目失败:', error);
      ElMessage.error('保存失败: ' + (error.message || '未知错误'));
    } finally {
      submitLoading.value = false;
    }
  });
}

// 返回
const goBack = () => {
  router.push(`/project/detail/${projectId.value}`);
}

// 获取项目详情
const getProjectInfo = async (id) => {
  loading.value = true;
  try {
    const response = await getProjectDetail(id);
    
    if (response.code === 200 && response.data) {
      const projectData = response.data;
      
      // 填充表单数据
      projectForm.name = projectData.name;
      projectForm.type = projectData.type;
      
      // 处理经费来源，兼容新旧版本
      if (projectData.fundingSources && projectData.fundingSources.length > 0) {
        projectForm.fundingSources = projectData.fundingSources;
      } else if (projectData.fundingSource) {
        projectForm.fundingSources = [projectData.fundingSource];
      } else {
        projectForm.fundingSources = [];
      }
      
      projectForm.leaderName = projectData.leaderName;
      projectForm.projectPeriod = [projectData.startDate, projectData.endDate];
      projectForm.budget = projectData.budget;
      projectForm.usedBudget = projectData.usedBudget;
      projectForm.status = projectData.status;
      projectForm.auditStatus = projectData.auditStatus;
      projectForm.description = projectData.description;
      projectForm.researchContent = projectData.researchContent;
      projectForm.expectedResults = projectData.expectedResults;
      projectForm.filePath = projectData.filePath;
      
      // 清空原始文件名，使用后端返回的文件名
      originalFileName.value = '';
      
      // 设置团队成员
      if (projectData.team && projectData.team.length > 0) {
        projectForm.team = projectData.team;
      } else {
        // 如果没有团队成员，初始添加一个空成员
        addMember();
      }
      
      // 设置预算科目
      if (projectData.budgetItems && projectData.budgetItems.length > 0) {
        projectForm.budgetItems = projectData.budgetItems;
      } else {
        // 如果没有预算科目数据，根据总预算创建一个默认的"其他费用"科目
        projectForm.budgetItems = [{
          category: '其他费用',
          amount: projectData.budget
        }];
      }
    } else {
      ElMessage.error('获取项目信息失败: ' + (response.message || '未知错误'));
    }
  } catch (error) {
    console.error('获取项目详情异常:', error);
    ElMessage.error('获取项目信息异常: ' + (error.message || '未知错误'));
  } finally {
    loading.value = false;
  }
}

// 页面加载时获取数据
onMounted(() => {
  // 从路由参数中获取项目ID
  projectId.value = route.params.id;
  
  // 检查用户权限
  if (userStore.isAdmin) {
    ElMessage.error('管理员不允许编辑项目');
    router.push('/project/list');
    return;
  }
  
  if (projectId.value) {
    getProjectInfo(projectId.value);
  } else {
    ElMessage.error('缺少项目ID参数');
    router.push('/project/list');
  }
});
</script>

<style scoped>
.project-edit-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 20px;
  font-weight: bold;
}

.form-container {
  margin-bottom: 20px;
}

.form-tips {
  color: #909399;
  margin-left: 10px;
  font-size: 14px;
}

.team-container {
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  padding: 10px;
  background-color: #F5F7FA;
}

.team-member {
  margin-bottom: 10px;
}

.team-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-start;
}

.error-message {
  color: #F56C6C;
  font-size: 14px;
  margin-top: 5px;
}

.upload-container {
  display: flex;
  flex-direction: column;
}

.file-info {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

.file-info .el-link {
  margin-right: 10px;
}

.upload-status {
  margin-top: 10px;
  color: #909399;
  display: flex;
  align-items: center;
}

.upload-status.success {
  color: #67C23A;
}

.upload-status.error {
  color: #F56C6C;
}

.upload-status .el-icon {
  margin-right: 5px;
}

.budget-container {
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  padding: 10px;
  background-color: #F5F7FA;
}

.budget-item {
  margin-bottom: 10px;
}

.budget-actions {
  margin-top: 10px;
  display: flex;
  justify-content: flex-start;
}

.budget-total {
  margin-top: 15px;
  font-weight: bold;
  text-align: right;
  color: #409EFF;
}
</style> 