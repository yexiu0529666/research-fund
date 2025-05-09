<template>
  <div class="project-apply-container">
    <div class="page-header">
      <div class="page-title">立项申请</div>
    </div>
    
    <el-card class="form-container">
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
        <el-form-item label="项目团队" prop="team" :error="teamError">
    <div class="team-container">
      <div v-for="(member, index) in projectForm.team" :key="index" class="team-member">
        <el-row :gutter="10">
          <el-col :span="9">
            <el-input
              v-model="member.name"
              placeholder="姓名"
              @change="projectFormRef.validateField('team')"
            />
          </el-col>
          <el-col :span="9">
            <el-input
              v-model="member.role"
              placeholder="角色职责"
              @change="projectFormRef.validateField('team')"
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
    </div>
  </el-form-item>
        
        <!-- 项目申请书上传 -->
        <el-form-item label="申请书" prop="filePath">
          <el-upload
            class="upload-container"
            action="#"
            :http-request="handleFileUpload"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :on-success="handleUploadSuccess"
            :on-error="handleUploadError"
            :file-list="fileList"
            :limit="1"
            :before-upload="beforeUpload"
            :accept="'.pdf'"
          >
            <el-button type="primary">选择PDF文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                请上传项目申请书PDF文件，不超过10MB
                <span v-if="fileSize" class="file-size-info">当前文件大小: {{ fileSize }}MB</span>
                <span v-if="uploadStatus === 'success'" class="upload-success">
                  <el-icon><Check /></el-icon> 上传成功
                </span>
                <span v-if="uploadStatus === 'error'" class="upload-error">
                  <el-icon><Close /></el-icon> 上传失败
                </span>
                <span v-if="uploadStatus === 'uploading'" class="uploading">
                  <el-icon><Loading /></el-icon> 上传中...
                </span>
              </div>
            </template>
          </el-upload>
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
            :rows="6"
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
          <el-button type="primary" @click="submitForm" :loading="submitLoading">提交申请</el-button>
          <el-button @click="resetForm">重置</el-button>
          <el-button @click="goBack">取消</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { createProject } from '@/api/project'
import { uploadProjectFile } from '@/api/upload'
import { useUserStore } from '@/store/modules/user'
import { Plus, Check, Close, Loading } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const projectFormRef = ref(null)
const submitLoading = ref(false)
const teamError = ref('')
const fileList = ref([])
const projectFile = ref(null)
const uploadStatus = ref('')
const fileSize = ref(null)

// 表单数据
const projectForm = reactive({
  name: '',
  type: '',
  fundingSources: [],
  leaderName: userStore.name || '', // 默认填充当前用户名
  projectPeriod: null,
  budgetItems: [{ category: '', amount: 0 }], // 预算科目
  description: '',
  researchContent: '',
  expectedResults: '',
  team: [{ name: '', role: '' }], // 项目团队成员
  filePath: '' // 文件路径
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
  team: [
    { validator: validateTeamMembers, trigger: 'change' }
  ],
  filePath: [
    { required: true, message: '请上传项目申请书', trigger: 'change' }
  ]
}

// 验证团队成员
function validateTeamMembers(rule, value, callback) {
  teamError.value = '';

  // 至少需要一个团队成员
  if (!projectForm.team || projectForm.team.length === 0) {
    teamError.value = '请至少添加一个团队成员';
    callback(new Error(teamError.value));
    return;
  }

  // 检查每个成员的姓名和角色是否都已填写
  for (let i = 0; i < projectForm.team.length; i++) {
    const member = projectForm.team[i];
    if (!member.name || !member.role) {
      teamError.value = '请填写所有团队成员的姓名和角色';
      callback(new Error(teamError.value));
      return;
    }
  }

  // 验证通过
  callback();
}

// 添加团队成员
function addMember() {
  projectForm.team.push({
    name: '',
    role: ''
  });
  projectFormRef.value.validateField('team');
}

// 移除团队成员
function removeMember(index) {
  projectForm.team.splice(index, 1);
  projectFormRef.value.validateField('team');
}

// 处理文件变更
function handleFileChange(file) {
  projectFile.value = file.raw;
  if (file.status === 'ready') {
    uploadStatus.value = 'uploading';
  }
  // 格式化文件大小显示，保留两位小数
  fileSize.value = (file.raw.size / 1024 / 1024).toFixed(2);
  
  // 如果文件大小超过限制，显示警告
  if (file.raw.size / 1024 / 1024 >= 10) {
    ElMessage.warning('文件大小超过10MB限制，上传可能会失败');
  }
}

// 处理文件移除
function handleFileRemove() {
  projectFile.value = null;
  projectForm.filePath = '';
  uploadStatus.value = '';
  fileSize.value = null;
  fileList.value = [];
  
  // 重新验证文件字段
  projectFormRef.value?.validateField('filePath');
}

// 文件上传前的验证
function beforeUpload(file) {
  // 验证文件类型
  const isPDF = file.type === 'application/pdf';
  if (!isPDF) {
    ElMessage.error('只能上传PDF文件!');
    return false;
  }
  
  // 验证文件大小
  const fileSizeMB = file.size / 1024 / 1024;
  const isLt10M = fileSizeMB < 10;
  
  if (!isLt10M) {
    ElMessage.error(`文件大小超出限制! 当前大小: ${fileSizeMB.toFixed(2)}MB，最大限制: 10MB`);
    return false;
  }
  
  // 如果文件接近限制大小，给出警告
  if (fileSizeMB > 9) {
    ElMessage.warning(`文件较大 (${fileSizeMB.toFixed(2)}MB)，接近10MB的限制，上传可能较慢`);
  }
  
  return true;
}

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // 禁用今天之前的日期
}

// 处理文件上传
function handleFileUpload(options) {
  const { file, onSuccess, onError } = options;
  
  // 再次验证文件大小
  const fileSizeMB = file.size / 1024 / 1024;
  if (fileSizeMB >= 10) {
    const errMsg = `文件大小超出限制! 当前大小: ${fileSizeMB.toFixed(2)}MB，最大限制: 10MB`;
    ElMessage.error(errMsg);
    uploadStatus.value = 'error';
    onError(new Error(errMsg));
    return;
  }
  
  uploadStatus.value = 'uploading';
  
  const formData = new FormData();
  formData.append('file', file);
  
  uploadProjectFile(formData)
    .then(response => {
      if (response.code === 200) {
        // 设置文件路径，这会触发表单验证
        projectForm.filePath = response.data;
        uploadStatus.value = 'success';
        
        // 保存文件信息到fileList以在UI中正确显示
        const fileName = file.name || 'project-file.pdf';
        fileList.value = [{
          name: fileName,
          url: response.data, // 这可能不是真实URL，但用于显示文件名
          status: 'success'
        }];
        
        // 验证表单，确保文件上传字段被标记为有效
        projectFormRef.value?.validateField('filePath');
        
        // 判断是否文件已存在
        if (response.message && response.message.includes('已存在')) {
          ElMessage.success('文件已存在，无需重新上传');
        } else {
          ElMessage.success('文件上传成功');
        }
        onSuccess(response);
      } else {
        uploadStatus.value = 'error';
        ElMessage.error(response.message || '文件上传失败');
        onError(new Error(response.message || '文件上传失败'));
      }
    })
    .catch(error => {
      uploadStatus.value = 'error';
      ElMessage.error(error.message || '文件上传失败');
      onError(error);
    });
}

// 处理文件上传成功
function handleUploadSuccess(response, file) {
  projectForm.filePath = response.data;
  uploadStatus.value = 'success';
  
  // 确保文件列表显示正确
  if (!fileList.value.some(f => f.status === 'success')) {
    fileList.value = [{
      name: file.name,
      url: response.data,
      status: 'success'
    }];
  }
  
  // 验证表单
  projectFormRef.value?.validateField('filePath');
}

// 处理文件上传错误
function handleUploadError(error) {
  uploadStatus.value = 'error';
}

// 提交表单
const submitForm = async () => {
  if (!projectFormRef.value) return;

  await projectFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      submitLoading.value = true;

      try {
        const startDate = projectForm.projectPeriod[0];
        const endDate = projectForm.projectPeriod[1];

        const submitData = {
          ...projectForm,
          startDate,
          endDate,
          budget: totalBudget.value, // 使用计算的总预算
          usedBudget: 0,
          status: 'planning',
          auditStatus: 'pending',
          leaderId: userStore.userId
        };

        delete submitData.projectPeriod;

        console.log('提交的数据:', submitData);

        const response = await createProject(submitData);

        ElMessage.success('项目申请提交成功');
        router.push('/project/list');
      } catch (error) {
        console.error('提交项目申请失败:', error);
        ElMessage.error(error.message || '提交失败，请稍后重试');
      } finally {
        submitLoading.value = false;
      }
    } else {
      console.error('表单验证失败:', fields);
      if (fields.team) {
        ElMessage.warning(teamError.value);
      }
    }
  });
};

// 重置表单
const resetForm = () => {
  projectFormRef.value?.resetFields()
  projectForm.team = []
  projectForm.budgetItems = [{ category: '', amount: 0 }]
  fileList.value = []
  projectFile.value = null
  projectForm.filePath = ''
  uploadStatus.value = ''
  fileSize.value = null
  addMember() // 添加一个默认成员
}

// 返回列表
const goBack = () => {
  router.push('/project/list')
}
</script>

<style scoped>
.project-apply-container {
  padding: 20px;
}

.page-header {
  margin-bottom: 20px;
}

.page-title {
  font-size: 22px;
  font-weight: bold;
}

.form-container {
  max-width: 800px;
  margin: 0 auto;
}

.form-tips {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}

.team-container {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  background-color: #f5f7fa;
}

.team-member {
  margin-bottom: 10px;
  padding: 10px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.team-actions {
  margin-top: 15px;
  display: flex;
  justify-content: center;
}

.error-message {
  color: #f56c6c;
  font-size: 12px;
  margin-top: 5px;
}

.upload-container {
  width: 100%;
}

.el-upload__tip {
  line-height: 1.2;
  margin-top: 8px;
  color: #909399;
}

.upload-success {
  color: #67C23A;
}

.upload-error {
  color: #F56C6C;
}

.uploading {
  color: #909399;
}

.file-size-info {
  color: #909399;
  font-size: 12px;
  margin-left: 10px;
}

.budget-container {
  border: 1px solid #ebeef5;
  border-radius: 4px;
  padding: 15px;
  background-color: #f5f7fa;
}

.budget-item {
  margin-bottom: 10px;
  padding: 10px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.1);
}

.budget-actions {
  margin-top: 15px;
  display: flex;
  justify-content: center;
}

.budget-total {
  margin-top: 15px;
  text-align: right;
  font-weight: bold;
  color: #409EFF;
}
</style> 