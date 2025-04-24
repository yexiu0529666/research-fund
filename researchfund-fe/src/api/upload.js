import request from '@/utils/request'

/**
 * 上传项目申请书PDF文件
 * @param {FormData} formData - 包含文件的FormData
 * @returns {Promise}
 */
export function uploadProjectFile(formData) {
  return request({
    url: '/api/upload/project-file',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    },
    timeout: 30000  // 上传文件可能需要更长的超时时间
  })
}

/**
 * 获取项目申请书文件下载链接
 * @param {string} filePath - 文件路径
 * @param {string} fileName - 自定义下载文件名（可选）
 * @returns {string} 下载链接
 */
export function getFileDownloadUrl(filePath, fileName) {
  const baseUrl = import.meta.env.VITE_API_BASE_URL || '';
  let url = `${baseUrl}/api/upload/download?filePath=${encodeURIComponent(filePath)}`;
  
  // 如果提供了文件名，添加到URL中
  if (fileName) {
    url += `&fileName=${encodeURIComponent(fileName)}`;
  }
  
  return url;
} 