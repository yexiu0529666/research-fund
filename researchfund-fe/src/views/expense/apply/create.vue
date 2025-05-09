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
          <el-select 
            v-model="expenseForm.projectId" 
            placeholder="请选择所属项目" 
            style="width: 100%;"
            @change="handleProjectChange"
          >
            <el-option 
              v-for="item in projectOptions" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="申请类型" prop="type">
          <el-select 
            v-model="expenseForm.type" 
            placeholder="请选择申请类型" 
            style="width: 100%;"
            :disabled="!expenseForm.projectId || budgetItems.length === 0"
          >
            <el-option 
              v-for="item in budgetItems" 
              :key="item.type" 
              :label="`${item.category} - 剩余预算: ¥${Number(item.remainingAmount).toFixed(2)}`"
              :value="item.type" 
              :disabled="item.remainingAmount <= 0"
            />
          </el-select>
          <div v-if="expenseForm.projectId && budgetItems.length === 0" class="error-message">
            该项目没有可用的预算科目，请选择其他项目或联系管理员
          </div>
          <div v-if="budgetItemSelected" class="budget-info">
            该科目总预算: <strong>¥{{ selectedBudgetItem.budgetAmount.toFixed(2) }}</strong>，
            已使用: <strong>¥{{ selectedBudgetItem.usedAmount.toFixed(2) }}</strong>，
            剩余: <strong>¥{{ selectedBudgetItem.remainingAmount.toFixed(2) }}</strong>
          </div>
        </el-form-item>
        
        <el-form-item label="申请金额" prop="amount">
          <el-input-number
            v-model="expenseForm.amount"
            :min="0"
            :max="maxApplicableAmount"
            :precision="2"
            :step="500"
            :controls="false"
            style="width: 200px"
            :disabled="!budgetItemSelected || projectTotalArrivedAmount <= 0"
          />
          <span class="form-tips">单位：元</span>
          <div v-if="budgetItemSelected && projectTotalArrivedAmount > 0" class="max-amount-tip">
            可申请最大金额: ¥{{ maxApplicableAmount.toFixed(2) }}
            <div class="amount-detail">
              (项目预算余额: ¥{{ selectedBudgetItem.remainingAmount.toFixed(2) }},
              项目已到账金额: ¥{{ projectTotalArrivedAmount.toFixed(2) }})
            </div>
          </div>
          <div v-else-if="budgetItemSelected && projectTotalArrivedAmount <= 0" class="error-message">
            该项目尚未有资金到账，无法申请经费
          </div>
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
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/modules/user'
import { getAvailableProjectsForExpense, getProjectBudgetItems } from '@/api/project'
import { createExpense, uploadAttachment } from '@/api/expense'
import { getProjectTotalArrivedAmount } from '@/api/projectFundArrival'
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
const budgetItems = ref([]) // 项目预算科目列表
const projectTotalArrivedAmount = ref(0) // 项目已到账总金额

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

// 选中的预算科目信息
const selectedBudgetItem = computed(() => {
  if (!expenseForm.type || budgetItems.value.length === 0) return null;
  return budgetItems.value.find(item => item.type === expenseForm.type) || null;
});

// 是否选中了预算科目
const budgetItemSelected = computed(() => selectedBudgetItem.value !== null);

// 可申请的最大金额（取已到账金额和预算科目剩余额度的较小值）
const maxApplicableAmount = computed(() => {
  if (!budgetItemSelected.value) return 0;
  
  // 如果已到账金额为0，显示警告
  if (projectTotalArrivedAmount.value <= 0) {
    return 0;
  }
  
  // 取预算科目剩余金额和已到账金额中的较小值
  const budgetRemaining = selectedBudgetItem.value.remainingAmount || 0;
  return Math.min(budgetRemaining, projectTotalArrivedAmount.value);
});

// 处理项目选择变更
const handleProjectChange = async (projectId) => {
  // 清空之前的数据
  expenseForm.type = '';
  expenseForm.amount = 0;
  budgetItems.value = [];
  projectTotalArrivedAmount.value = 0;
  
  if (!projectId) return;
  
  try {
    // 获取项目已到账总金额
    const totalAmountRes = await getProjectTotalArrivedAmount(projectId);
    if (totalAmountRes.code === 200) {
      projectTotalArrivedAmount.value = totalAmountRes.data || 0;
      console.log('项目已到账金额:', projectTotalArrivedAmount.value);
    } else {
      ElMessage.warning('获取项目已到账金额失败, 将使用0作为默认值');
      projectTotalArrivedAmount.value = 0;
    }
    
    // 获取项目可用预算科目
    const response = await getProjectBudgetItems(projectId);
    if (response.code === 200) {
      budgetItems.value = response.data || [];
      if (budgetItems.value.length === 0) {
        ElMessage.warning('该项目没有可用的预算科目，请选择其他项目或联系管理员');
      }
    } else {
      ElMessage.error('获取项目预算科目失败: ' + (response.message || '未知错误'));
    }
  } catch (error) {
    console.error('获取项目信息失败:', error);
    ElMessage.error('获取项目信息失败: ' + (error.message || '未知错误'));
  }
}

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
    { type: 'number', min: 1, message: '金额必须大于0', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!budgetItemSelected.value) {
          callback();
          return;
        }
        
        // 检查是否有到账金额
        if (projectTotalArrivedAmount.value <= 0) {
          callback(new Error('该项目尚未有资金到账，无法申请经费'));
          return;
        }
        
        // 检查申请金额是否超过了可用预算
        if (value > selectedBudgetItem.value.remainingAmount) {
          callback(new Error(`金额不能超过可用预算 ¥${selectedBudgetItem.value.remainingAmount.toFixed(2)}`));
          return;
        }
        
        // 检查申请金额是否超过了已到账金额
        if (value > projectTotalArrivedAmount.value) {
          callback(new Error(`金额不能超过项目已到账金额 ¥${projectTotalArrivedAmount.value.toFixed(2)}`));
          return;
        }
        
        // 通过验证
        callback();
      }, 
      trigger: 'blur' 
    }
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
  
  // 检查项目是否有资金到账
  if (projectTotalArrivedAmount.value <= 0) {
    ElMessage.error('该项目尚未有资金到账，无法申请经费');
    return;
  }
  
  // 检查申请金额是否超过已到账金额
  if (expenseForm.amount > projectTotalArrivedAmount.value) {
    ElMessage.error(`申请金额不能超过项目已到账金额 ¥${projectTotalArrivedAmount.value.toFixed(2)}`);
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

.error-message {
  color: #F56C6C;
  font-size: 13px;
  margin-top: 5px;
}

.budget-info {
  margin-top: 8px;
  color: #409EFF;
  font-size: 13px;
}

.max-amount-tip {
  color: #67C23A;
  font-size: 13px;
  margin-top: 5px;
  font-weight: bold;
}

.amount-detail {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style> 