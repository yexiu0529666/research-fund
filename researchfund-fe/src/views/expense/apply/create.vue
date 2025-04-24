<template>
  <div class="expense-apply-create-container">
    <div class="page-header">
      <div class="page-title">新建经费申请</div>
    </div>
    
    <el-card class="form-container">
      <el-form 
        ref="expenseFormRef" 
        :model="expenseForm" 
        :rules="rules" 
        label-width="100px"
        :disabled="submitLoading"
      >
        <el-form-item label="申请标题" prop="title">
          <el-input v-model="expenseForm.title" placeholder="请输入申请标题" />
        </el-form-item>
        
        <el-form-item label="申请类别" prop="category">
          <el-radio-group v-model="expenseForm.category">
            <el-radio label="advance">经费借款</el-radio>
            <el-radio label="reimbursement">报销</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="expenseForm.projectId" placeholder="请选择所属项目" style="width: 100%;">
            <el-option 
              v-for="item in projectOptions" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="申请类型" prop="type">
          <el-select v-model="expenseForm.type" placeholder="请选择申请类型" style="width: 100%;">
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
        
        <el-form-item label="申请金额" prop="amount">
          <el-input-number
            v-model="expenseForm.amount"
            :min="0"
            :precision="2"
            :step="500"
            :controls="false"
            style="width: 200px"
          />
          <span class="form-tips">单位：元</span>
        </el-form-item>
        
        <el-form-item label="申请日期" prop="applyDate">
          <el-date-picker
            v-model="expenseForm.applyDate"
            type="date"
            placeholder="选择申请日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%;"
            :disabled="true"
          />
        </el-form-item>
        
        <el-form-item label="预计用途" prop="purpose">
          <el-input
            v-model="expenseForm.purpose"
            type="textarea"
            :rows="3"
            placeholder="请简要描述资金用途"
          />
        </el-form-item>
        
        <el-form-item label="申请理由" prop="reason">
          <el-input
            v-model="expenseForm.reason"
            type="textarea"
            :rows="4"
            placeholder="请详细说明申请理由及必要性"
          />
        </el-form-item>
        
        <el-form-item label="附件" prop="attachments" v-if="expenseForm.category === 'reimbursement'">
          <el-upload
            class="upload-container"
            action="#"
            :http-request="handleFileUpload"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :file-list="fileList"
            :limit="3"
            :before-upload="beforeUpload"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                <span style="color: #f56c6c; font-weight: bold;">必传项：</span>
                请上传附件作为申请依据（如采购清单、发票、收据等）。支持PDF、Word、Excel、图片等格式文件，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
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
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { getAvailableProjectsForExpense } from '@/api/project'
import { createExpense, uploadAttachment } from '@/api/expense'
// 这里需要导入实际的API
// import { createExpenseApply } from '@/api/expense'
// import { uploadFile } from '@/api/upload'
// import { getProjectOptions } from '@/api/project'

const router = useRouter()
const userStore = useUserStore()
const expenseFormRef = ref(null)
const submitLoading = ref(false)
const fileList = ref([])
const projectOptions = ref([]) // 项目选项列表

// 表单数据
const expenseForm = reactive({
  title: '',
  projectId: '',
  type: '',
  amount: 0,
  applyDate: new Date().toISOString().slice(0, 10), // 始终设置为当天日期
  purpose: '',
  reason: '',
  attachments: [],
  category: 'advance'
})

// 表单验证规则
const rules = {
  title: [
    { required: true, message: '请输入申请标题', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择申请类别', trigger: 'change' }
  ],
  projectId: [
    { required: true, message: '请选择所属项目', trigger: 'change' }
  ],
  type: [
    { required: true, message: '请选择申请类型', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '请输入申请金额', trigger: 'blur' },
    { type: 'number', min: 1, message: '金额必须大于0', trigger: 'blur' }
  ],
  applyDate: [
    { required: true, message: '请选择申请日期', trigger: 'change' }
  ],
  purpose: [
    { required: true, message: '请输入预计用途', trigger: 'blur' },
    { min: 10, max: 500, message: '长度在 10 到 500 个字符', trigger: 'blur' }
  ],
  reason: [
    { required: true, message: '请输入申请理由', trigger: 'blur' },
    { min: 10, max: 1000, message: '长度在 10 到 1000 个字符', trigger: 'blur' }
  ],
  attachments: [
    { 
      type: 'array', 
      required: true, 
      message: '请上传附件', 
      trigger: 'change',
      validator: (rule, value, callback) => {
        if (expenseForm.category === 'reimbursement' && (!value || value.length === 0)) {
          callback(new Error('报销申请需要上传至少一个附件'));
        } else {
          callback();
        }
      }
    }
  ]
}

// 文件上传前检查
const beforeUpload = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10
  
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!')
    return false
  }
  return true
}

// 处理文件变更
const handleFileChange = (file) => {
  console.log('File changed:', file)
}

// 处理文件移除
const handleFileRemove = (file) => {
  console.log('File removed:', file)
}

// 处理文件上传
const handleFileUpload = async (options) => {
  const { file } = options;
  
  try {
    // 调用API上传文件
    const response = await uploadAttachment(file);
    
    if (response.code === 200 && response.data) {
      // 添加到附件列表
      expenseForm.attachments.push(response.data);
      
      // 文件上传成功
      options.onSuccess();
      ElMessage.success(`${file.name} 上传成功`);
    } else {
      options.onError();
      ElMessage.error(`${file.name} 上传失败: ${response.msg || '未知错误'}`);
    }
  } catch (error) {
    console.error('文件上传失败:', error);
    options.onError();
    ElMessage.error(`${file.name} 上传失败: ${error.message || '未知错误'}`);
  }
}

// 提交表单
const submitForm = async () => {
  // 只有报销类型才需要检查附件
  if (expenseForm.category === 'reimbursement' && expenseForm.attachments.length === 0) {
    ElMessage.warning('报销申请需要上传至少一个附件');
    return;
  }
  
  await expenseFormRef.value.validate(async (valid, fields) => {
    if (valid) {
      submitLoading.value = true;
      
      try {
        // 构建提交数据
        const submitData = {
          ...expenseForm,
          applyUserId: userStore.userId,
          applyUserName: userStore.name,
          status: 'pending' // 初始状态为待审核
        };
        
        console.log('提交的数据:', submitData);
        
        // 调用API创建经费申请
        const response = await createExpense(submitData);
        
        if (response.code === 200) {
          ElMessage.success('经费申请提交成功');
          // 跳转到经费申请列表页面
          router.push('/expense/apply/list');
        }
      } catch (error) {
        console.error('提交经费申请失败:', error);
        ElMessage.error(error.message || '提交失败，请稍后重试');
      } finally {
        submitLoading.value = false;
      }
    } else {
      console.error('表单验证失败:', fields);
    }
  });
}

// 重置表单
const resetForm = () => {
  expenseFormRef.value?.resetFields()
  fileList.value = []
  expenseForm.attachments = []
  // 确保重置后申请日期仍然是当天
  expenseForm.applyDate = new Date().toISOString().slice(0, 10)
}

// 返回列表
const goBack = () => {
  router.push('/expense/apply/list')
}

// 获取项目列表
const getProjects = async () => {
  try {
    // 调用API获取项目列表
    const response = await getAvailableProjectsForExpense();
    if (response.code === 200) {
      projectOptions.value = response.data || [];
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

// 页面加载时获取数据
onMounted(() => {
  getProjects()
})
</script>

<style scoped>
.expense-apply-create-container {
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

.upload-container {
  width: 100%;
}
</style> 