<template>
  <div class="fund-arrival-detail-container">
    <div class="page-header">
      <div class="page-title">经费到账详情</div>
      <el-button type="primary" @click="goBack">返回列表</el-button>
    </div>
    
    <el-card v-loading="loading">
      <template v-if="fundArrival">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="项目名称">{{ fundArrival.projectName }}</el-descriptions-item>
          <el-descriptions-item label="经费来源">
            <el-tag>{{ formatFundingSource(fundArrival.fundingSource) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="到账金额">¥{{ fundArrival.amount?.toFixed(2) }}</el-descriptions-item>
          <el-descriptions-item label="到账日期">{{ fundArrival.arrivalDate }}</el-descriptions-item>
          <el-descriptions-item label="到账凭证" :span="2">
            <div class="file-section" v-if="fundArrival.voucherPath">
              <el-button type="primary" link @click="downloadFile(fundArrival.voucherPath)">
                <el-icon><Download /></el-icon> 下载到账凭证
              </el-button>
            </div>
            <span v-else>无</span>
          </el-descriptions-item>
          <el-descriptions-item label="备注" :span="2">{{ fundArrival.remark || '无' }}</el-descriptions-item>
        </el-descriptions>
      </template>
      <el-empty v-else description="未找到经费到账记录" />
    </el-card>
    
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { getFundArrivalDetail } from '@/api/projectFundArrival';
import { getFileDownloadUrl } from '@/api/upload'

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const fundArrival = ref(null);
const imageObjectUrl = ref(''); // 用于存储对象URL


// 下载项目申请书
const downloadFile = (filePath) => {
  if (!filePath) {
    ElMessage.warning('到账凭证不存在');
    return;
  }

  try {
    // 构建下载文件名
    const fileName = filePath.match(/\/([^\/]+)$/)[1];  
    
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


// 格式化经费来源
const formatFundingSource = (source) => {
  const sourceMap = {
    'fiscal': '财政经费',
    'school': '校配套经费',
    'other': '其他经费'
  };
  return sourceMap[source] || source;
};




// 获取详情
const getDetail = async () => {
  const id = route.params.id;
  if (!id) {
    ElMessage.error('缺少ID参数');
    return;
  }
  
  loading.value = true;
  try {
    const res = await getFundArrivalDetail(id);
    if (res.code === 200) {
      fundArrival.value = res.data;
    } else {
      ElMessage.error(res.message || '获取经费到账详情失败');
    }
  } catch (error) {
    console.error('获取经费到账详情失败:', error);
    ElMessage.error('获取经费到账详情失败');
  } finally {
    loading.value = false;
  }
};

// 返回列表
const goBack = () => {
  router.push('/project/fund-arrival');
};

// 初始化
onMounted(() => {
  getDetail();
});

// 处理对话框关闭
const handleDialogClose = () => {
  imageObjectUrl.value = ''; // 清理对象URL
};
</script>

<style scoped>
.fund-arrival-detail-container {
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

.file-preview-container {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  min-height: 200px;
}

.image-preview, .pdf-preview {
  display: flex;
  flex-direction: column;
  align-items: center;
  width: 100%;
}

.preview-actions {
  margin-top: 15px;
  text-align: center;
}

.other-file {
  text-align: center;
  padding: 30px;
}
</style> 