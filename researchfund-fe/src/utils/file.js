/**
 * 格式化文件URL，确保包含正确的基础路径
 * @param {String} path - 文件路径
 * @returns {String} 完整的文件URL
 */
export function formatFileUrl(path) {
  if (!path) return '';
  
  // 如果已经是完整URL则直接返回
  if (path.startsWith('http://') || path.startsWith('https://')) {
    return path;
  }
  
  // 确保路径以/开头
  const normalizedPath = path.startsWith('/') ? path : `/${path}`;
  
  // 获取基础URL（去掉/api前缀）
  const baseUrl = import.meta.env.VITE_APP_BASE_API || '';
  const serverUrl = baseUrl.replace(/\/api\/?$/, '');
  
  console.log('原始路径:', path);
  console.log('格式化后的路径:', `${serverUrl}${normalizedPath}`);
  
  // 添加服务器URL
  return `${serverUrl}${normalizedPath}`;
} 