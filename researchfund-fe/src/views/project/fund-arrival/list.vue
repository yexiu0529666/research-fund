<template>
  <div class="fund-arrival-list-container" v-loading="projectsLoading">
    <div class="page-header">
      <div class="page-title">{{ isResearcher ? '项目经费到账记录' : '经费到账管理' }}</div>
      <div class="page-actions">
        <el-button type="primary" @click="handleCreateClick" v-if="userStore.roles.includes('ROLE_ADMIN')">新增到账记录</el-button>
      </div>
    </div>
    
    <el-card class="filter-container" >
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <el-form-item label="项目">
          <el-select v-model="filterForm.projectId" placeholder="选择项目" clearable style="width: 250px">
            <el-option
              v-for="project in projectOptions"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            >
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span>{{ project.name }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleFilter">查询</el-button>
          <el-button @click="resetFilter">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <el-card class="table-container">
      <div class="table-operations" v-if="filterForm.projectId">
        <div class="project-info">
          <div class="info-item">
            <span class="label">项目名称:</span>
            <span class="value">{{ selectedProject?.name }}</span>
          </div>
          <div class="info-item">
            <span class="label">项目预算:</span>
            <span class="value">¥{{ selectedProject?.budget?.toFixed(2) }}</span>
          </div>
          <div class="info-item">
            <span class="label">已到账金额:</span>
            <span class="value">¥{{ selectedProject?.totalArrivedAmount?.toFixed(2) }}</span>
          </div>
          <div class="info-item">
            <span class="label">待到账金额:</span>
            <span class="value">¥{{ selectedProject?.pendingAmount?.toFixed(2) }}</span>
          </div>
        </div>
      </div>
      
      <el-empty 
        v-if="isResearcher && projectOptions.length === 0" 
        description="您暂无可查看的项目经费到账记录" 
      ></el-empty>
      
      <div v-else>
        <el-table
          v-loading="tableLoading"
          :data="tableData"
          border
          style="width: 100%"
          :empty-text="isResearcher ? '暂无经费到账记录，请联系管理员' : '暂无经费到账记录'"
        >
          <el-table-column prop="projectName" label="项目名称" min-width="160" show-overflow-tooltip />
          <el-table-column prop="fundingSource" label="经费来源" min-width="120">
            <template #default="scope">
              <el-tag>{{ formatFundingSource(scope.row.fundingSource) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="到账金额" min-width="100">
            <template #default="scope">
              ¥{{ scope.row.amount.toFixed(2) }}
            </template>
          </el-table-column>
          <el-table-column prop="arrivalDate" label="到账日期" min-width="120" />
          <el-table-column prop="remark" label="备注" min-width="160" show-overflow-tooltip />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button link type="primary" @click="handleView(scope.row)">查看</el-button>
              <el-button link type="primary" @click="handleEdit(scope.row)" v-if="userStore.roles.includes('ROLE_ADMIN')">编辑</el-button>
              <el-button link type="danger" @click="handleDelete(scope.row)" v-if="userStore.roles.includes('ROLE_ADMIN')">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="pagination-container">
          <el-pagination
            v-model:currentPage="currentPage"
            v-model:page-size="pageSize"
            :page-sizes="[10, 20, 50]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogType === 'create' ? '新增到账记录' : '编辑到账记录'"
      width="650px"
    >
      <el-form 
        ref="formRef" 
        :model="form" 
        :rules="rules" 
        label-width="120px"
        :disabled="formLoading"
      >
        <el-form-item label="项目" prop="projectId">
          <el-select 
            v-model="form.projectId" 
            placeholder="选择项目" 
            style="width: 100%"
            :disabled="dialogType === 'edit'"
            @change="handleProjectChange"
          >
            <el-option
              v-for="project in availableProjects"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            >
              <div style="display: flex; justify-content: space-between; align-items: center">
                <span>{{ project.name }}</span>
                <span style="font-size: 12px; color: #909399">
                  预算: ¥{{ project.budget.toFixed(2) }} | 
                  已到账: ¥{{ project.totalArrivedAmount.toFixed(2) }} | 
                  待到账: ¥{{ project.pendingAmount.toFixed(2) }}
                </span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="经费来源" prop="fundingSource">
          <el-select v-model="form.fundingSource" placeholder="选择经费来源" style="width: 100%">
            <el-option
              v-for="option in fundingSourceOptions"
              :key="option.value"
              :label="option.label"
              :value="option.value"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="到账金额" prop="amount">
          <el-input-number
            v-model="form.amount"
            :min="0"
            :max="form.projectId ? getMaxAmount() : 999999999"
            :precision="2"
            :step="1000"
            style="width: 200px"
          />
          <span class="form-tips">单位：元</span>
          <div v-if="form.projectId" class="max-amount-tip">
            可添加最大金额: ¥{{ getMaxAmount().toFixed(2) }}
          </div>
        </el-form-item>
        
        <el-form-item label="到账日期" prop="arrivalDate">
          <el-date-picker
            v-model="form.arrivalDate"
            type="date"
            placeholder="选择到账日期"
            format="YYYY-MM-DD"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="到账凭证" prop="voucherPath">
          <el-upload
            class="upload-container"
            action="#"
            :http-request="handleFileUpload"
            :on-change="handleFileChange"
            :on-remove="handleFileRemove"
            :file-list="fileList"
            :limit="1"
            :before-upload="beforeUpload"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                请上传到账凭证文件，支持PDF、Word、图片等格式，单个文件不超过10MB
              </div>
            </template>
          </el-upload>
        </el-form-item>
        
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="form.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入备注信息"
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="formLoading">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';
import { useRouter } from 'vue-router';
import { useUserStore } from '@/store/modules/user';
import { 
  getFundArrivalList, 
  getAvailableProjectsForFundArrival, 
  createFundArrival, 
  updateFundArrival, 
  deleteFundArrival,
  getProjectTotalArrivedAmount
} from '@/api/projectFundArrival';
import { uploadAttachment } from '@/api/expense';
import { getProjectList, getMyProjects } from '@/api/project';
import { formatFileUrl } from '@/utils/file';

// 引入路由和用户store
const router = useRouter();
const userStore = useUserStore();

// 判断当前用户是否为研究人员
const isResearcher = computed(() => userStore.roles.includes('ROLE_RESEARCHER'));

// 状态变量
const tableLoading = ref(false);
const projectsLoading = ref(false);
const formLoading = ref(false);
const dialogVisible = ref(false);
const dialogType = ref('create'); // 'create' 或 'edit'
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const tableData = ref([]);
const projectOptions = ref([]);
const availableProjects = ref([]);
const fileList = ref([]);
const formRef = ref(null);

// 表单数据
const form = reactive({
  id: null,
  projectId: null,
  projectName: '',
  fundingSource: '',
  amount: 0,
  arrivalDate: new Date().toISOString().slice(0, 10),
  voucherPath: '',
  remark: ''
});

// 经费来源选项
const fundingSourceOptions = ref([]);

// 筛选表单
const filterForm = reactive({
  projectId: null
});

// 表单验证规则
const rules = {
  projectId: [
    { required: true, message: '请选择项目', trigger: 'change' }
  ],
  fundingSource: [
    { required: true, message: '请选择经费来源', trigger: 'change' }
  ],
  amount: [
    { required: true, message: '请输入到账金额', trigger: 'blur' },
    { type: 'number', min: 0.01, message: '金额必须大于0', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (!form.projectId) {
          callback();
          return;
        }
        const project = projectOptions.value.find(p => p.id === form.projectId);
        if (project && value > project.pendingAmount) {
          callback(new Error(`金额不能超过待到账金额 ¥${project.pendingAmount.toFixed(2)}`));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  arrivalDate: [
    { required: true, message: '请选择到账日期', trigger: 'change' }
  ],
  voucherPath: [
    { required: true, message: '请上传到账凭证', trigger: 'change' }
  ]
};

// 获取选中的项目信息
const selectedProject = computed(() => {
  if (!filterForm.projectId) return null;
  return projectOptions.value.find(p => p.id === filterForm.projectId);
});

// 获取可添加的最大金额
const getMaxAmount = () => {
  if (!form.projectId) return 0;
  const project = projectOptions.value.find(p => p.id === form.projectId);
  return project ? project.pendingAmount : 0;
};

// 格式化经费来源
const formatFundingSource = (source) => {
  const sourceMap = {
    'fiscal': '财政经费',
    'school': '校配套经费',
    'other': '其他经费'
  };
  return sourceMap[source] || source;
};

// 处理项目变更
const handleProjectChange = (projectId) => {
  if (!projectId) {
    fundingSourceOptions.value = [];
    form.fundingSource = '';
    return;
  }
  
  const project = availableProjects.value.find(p => p.id === projectId);
  if (project) {
    form.projectName = project.name;
    
    // 更新经费来源选项，根据项目的经费来源
    if (project.fundingSources && project.fundingSources.length > 0) {
      // 如果有多个经费来源
      fundingSourceOptions.value = project.fundingSources.map(source => ({
        label: formatFundingSource(source),
        value: source
      }));
    } else if (project.fundingSource) {
      // 兼容旧数据，使用单个经费来源
      fundingSourceOptions.value = [{
        label: formatFundingSource(project.fundingSource),
        value: project.fundingSource
      }];
    } else {
      // 如果项目没有设置经费来源，则使用默认选项
      fundingSourceOptions.value = [
        { label: '财政经费', value: 'fiscal' },
        { label: '校配套经费', value: 'school' },
        { label: '其他经费', value: 'other' }
      ];
    }
    
    // 清空当前选择的经费来源
    form.fundingSource = '';
  }
};

// 获取项目列表
const getProjects = async () => {
  projectsLoading.value = true;
  try {
    let res;
    // 如果是研究人员，只获取自己的项目
    if (isResearcher.value) {
      res = await getMyProjects();
    } else {
      // 管理员获取所有项目
      res = await getProjectList();
    }
    
    if (res.code === 200) {
      const projects = res.data.list || [];
      
      // 增强项目数据，增加到账信息
      const enhancedProjects = await Promise.all(projects.map(async (project) => {
        try {
          // 获取已到账金额
          const totalRes = await getProjectTotalArrivedAmount(project.id);
          const totalArrivedAmount = totalRes.code === 200 ? totalRes.data || 0 : 0;
          
          return {
            ...project,
            totalArrivedAmount,
            pendingAmount: project.budget - totalArrivedAmount
          };
        } catch (error) {
          console.error(`获取项目 ${project.id} 到账信息失败:`, error);
          return {
            ...project,
            totalArrivedAmount: 0,
            pendingAmount: project.budget
          };
        }
      }));
      
      projectOptions.value = enhancedProjects;
    }
  } catch (error) {
    console.error('获取项目列表失败:', error);
    ElMessage.error('获取项目列表失败');
  } finally {
    projectsLoading.value = false;
  }
};

// 获取可用项目列表
const getAvailableProjects = async () => {
  try {
    // 使用专门的API获取可用于经费到账的项目列表
    const res = await getAvailableProjectsForFundArrival();
    if (res.code === 200) {
      let projects = res.data || [];
      
      // 如果是研究人员，只保留自己的项目
      if (isResearcher.value) {
        const myProjectIds = projectOptions.value.map(p => p.id);
        projects = projects.filter(p => myProjectIds.includes(p.id));
      }
      
      // 增强项目数据，增加到账信息
      const enhancedProjects = await Promise.all(projects.map(async (project) => {
        try {
          // 获取已到账金额
          const totalRes = await getProjectTotalArrivedAmount(project.id);
          const totalArrivedAmount = totalRes.code === 200 ? totalRes.data || 0 : 0;
          
          return {
            ...project,
            totalArrivedAmount,
            pendingAmount: project.budget - totalArrivedAmount
          };
        } catch (error) {
          console.error(`获取项目 ${project.id} 到账信息失败:`, error);
          return {
            ...project,
            totalArrivedAmount: 0,
            pendingAmount: project.budget
          };
        }
      }));
      
      availableProjects.value = enhancedProjects;
    }
  } catch (error) {
    console.error('获取可用项目列表失败:', error);
    ElMessage.error('获取可用项目列表失败');
  }
};

// 获取经费到账记录列表
const getList = async () => {
  tableLoading.value = true;
  try {
    const params = {
      pageNum: currentPage.value,
      pageSize: pageSize.value
    };
    
    if (filterForm.projectId) {
      params.projectId = filterForm.projectId;
    }
    
    const res = await getFundArrivalList(params);
    if (res.code === 200) {
      let list = res.data.list || [];
      
      // 如果是研究人员且未指定项目ID过滤，需要在前端过滤掉不属于自己项目的记录
      if (isResearcher.value && !params.projectId) {
        const myProjectIds = projectOptions.value.map(p => p.id);
        list = list.filter(item => myProjectIds.includes(item.projectId));
      }
      
      tableData.value = list;
      total.value = isResearcher.value && !params.projectId 
        ? list.length  // 如果在前端过滤了，使用过滤后的长度
        : res.data.total || 0;
    }
  } catch (error) {
    console.error('获取经费到账记录列表失败:', error);
    ElMessage.error('获取经费到账记录列表失败');
  } finally {
    tableLoading.value = false;
  }
};

// 处理筛选
const handleFilter = () => {
  currentPage.value = 1;
  getList();
};

// 重置筛选
const resetFilter = () => {
  currentPage.value = 1;
  getList();
};

// 处理页面大小变更
const handleSizeChange = (val) => {
  pageSize.value = val;
  getList();
};

// 处理当前页变更
const handleCurrentChange = (val) => {
  currentPage.value = val;
  getList();
};

// 处理创建点击
const handleCreateClick = () => {
  resetForm();
  dialogType.value = 'create';
  dialogVisible.value = true;
};

// 处理编辑
const handleEdit = (row) => {
  resetForm();
  dialogType.value = 'edit';
  Object.keys(form).forEach(key => {
    if (row[key] !== undefined) {
      form[key] = row[key];
    }
  });
  
  // 处理文件列表
  if (form.voucherPath) {
    // Make sure voucherPath is a string
    if (typeof form.voucherPath === 'object') {
      form.voucherPath = form.voucherPath.url || form.voucherPath.toString();
    }
    
    fileList.value = [{
      name: '到账凭证',
      url: formatFileUrl(form.voucherPath)
    }];
  }
  
  dialogVisible.value = true;
};

// 处理删除
const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确定要删除该经费到账记录吗？',
    '提示',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const res = await deleteFundArrival(row.id);
      if (res.code === 200) {
        ElMessage.success('删除成功');
        getList();
      }
    } catch (error) {
      console.error('删除经费到账记录失败:', error);
      ElMessage.error('删除失败：' + (error.message || '未知错误'));
    }
  }).catch(() => {});
};

// 重置表单
const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }
  
  form.id = null;
  form.projectId = null;
  form.projectName = '';
  form.fundingSource = '';
  form.amount = 0;
  form.arrivalDate = new Date().toISOString().slice(0, 10);
  form.voucherPath = '';
  form.remark = '';
  
  fileList.value = [];
};

// 文件上传前检查
const beforeUpload = (file) => {
  const isLt10M = file.size / 1024 / 1024 < 10;
  
  if (!isLt10M) {
    ElMessage.error('文件大小不能超过 10MB!');
    return false;
  }
  return true;
};

// 处理文件变更
const handleFileChange = (file) => {
  console.log('File changed:', file);
};

// 处理文件移除
const handleFileRemove = (file) => {
  form.voucherPath = '';
  // 手动触发验证
  if (formRef.value) {
    formRef.value.validateField('voucherPath');
  }
};

// 处理文件上传
const handleFileUpload = async (options) => {
  const { file } = options;
  
  try {
    const res = await uploadAttachment(file);
    if (res.code === 200 && res.data) {
      // Store just the path string, not the full file object
      form.voucherPath = res.data; 
      options.onSuccess();
      ElMessage.success('文件上传成功');
      // 手动触发验证
      if (formRef.value) {
        formRef.value.validateField('voucherPath');
      }
    } else {
      options.onError();
      ElMessage.error('文件上传失败: ' + (res.message || '未知错误'));
    }
  } catch (error) {
    console.error('文件上传失败:', error);
    options.onError();
    ElMessage.error('文件上传失败: ' + (error.message || '未知错误'));
  }
};

// 提交表单
const submitForm = async () => {
  if (!formRef.value) return;
  
  formRef.value.validate(async (valid) => {
    if (valid) {
      formLoading.value = true;
      try {
        const submitData = { ...form };
        
        // Ensure voucherPath is a string, not an object
        if (typeof submitData.voucherPath === 'object' && submitData.voucherPath !== null) {
          submitData.voucherPath = submitData.voucherPath.url || '';
        }
        
        if (dialogType.value === 'create') {
          // 创建
          const res = await createFundArrival(submitData);
          if (res.code === 200) {
            ElMessage.success('创建成功');
            dialogVisible.value = false;
            getList();
            getProjects();
            getAvailableProjects(); // 刷新项目列表
          }
        } else {
          // 更新
          const res = await updateFundArrival(form.id, submitData);
          if (res.code === 200) {
            ElMessage.success('更新成功');
            dialogVisible.value = false;
            getList();
            getProjects(); 
            getAvailableProjects(); // 刷新项目列表
          }
        }
      } catch (error) {
        console.error('提交失败:', error);
        ElMessage.error('提交失败: ' + (error.message || '未知错误'));
      } finally {
        formLoading.value = false;
      }
    }
  });
};

// 处理查看
const handleView = (row) => {
  router.push(`/project/fund-arrival/detail/${row.id}`);
};

// 初始化
onMounted(() => {
  getProjects().then(() => {
    getList();
  });
  getAvailableProjects();
});
</script>

<style scoped>
.fund-arrival-list-container {
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
  align-items: center;
}

.table-container {
  margin-bottom: 20px;
}

.table-operations {
  margin-bottom: 15px;
  padding: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.project-info {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.info-item {
  display: flex;
  align-items: center;
}

.info-item .label {
  font-weight: bold;
  margin-right: 8px;
  color: #606266;
}

.info-item .value {
  color: #409EFF;
  font-weight: bold;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.form-tips {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}

.max-amount-tip {
  margin-top: 5px;
  color: #409EFF;
  font-size: 13px;
}

.upload-container {
  width: 100%;
}
</style> 