/**
 * 日期时间格式化工具
 */

/**
 * 格式化日期时间
 * @param {string|Date} date 日期对象或日期字符串
 * @param {string} format 格式化模式，默认为 'YYYY-MM-DD HH:mm:ss'
 * @returns {string} 格式化后的日期字符串
 */
export function formatDateTime(date, format = 'YYYY-MM-DD HH:mm:ss') {
  if (!date) return '';
  
  const d = typeof date === 'string' ? new Date(date) : date;
  
  if (isNaN(d.getTime())) {
    return '';
  }
  
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  const seconds = String(d.getSeconds()).padStart(2, '0');
  
  return format
    .replace('YYYY', year)
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds);
}

/**
 * 格式化日期（不含时间）
 * @param {string|Date} date 日期对象或日期字符串
 * @returns {string} 格式化后的日期字符串 YYYY-MM-DD
 */
export function formatDate(date) {
  return formatDateTime(date, 'YYYY-MM-DD');
}

/**
 * 计算两个日期之间的差值（天数）
 * @param {string|Date} startDate 开始日期
 * @param {string|Date} endDate 结束日期
 * @returns {number} 相差天数
 */
export function daysBetween(startDate, endDate) {
  const start = new Date(startDate);
  const end = new Date(endDate);
  
  // 确保日期有效
  if (isNaN(start.getTime()) || isNaN(end.getTime())) {
    return 0;
  }
  
  // 设置时间为0点以计算整天
  start.setHours(0, 0, 0, 0);
  end.setHours(0, 0, 0, 0);
  
  // 计算毫秒差并转换为天数
  const diffTime = Math.abs(end - start);
  const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
  
  return diffDays;
}

/**
 * 获取当前日期（YYYY-MM-DD格式）
 * @returns {string} 当前日期字符串
 */
export function getCurrentDate() {
  return formatDate(new Date());
}

/**
 * 判断一个日期是否过期（早于当前日期）
 * @param {string|Date} date 需要判断的日期
 * @returns {boolean} 是否已过期
 */
export function isExpired(date) {
  if (!date) return false;
  
  const checkDate = new Date(date);
  const today = new Date();
  
  // 设置为0点进行比较
  checkDate.setHours(0, 0, 0, 0);
  today.setHours(0, 0, 0, 0);
  
  return checkDate < today;
} 