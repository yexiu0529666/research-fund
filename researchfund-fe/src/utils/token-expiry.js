/**
 * Token强制过期管理模块
 * 此模块确保所有用户（包括管理员）在登录后10分钟内强制登出系统
 */

import router from '@/router'
import { ElMessage } from 'element-plus'

// Token过期时间（毫秒）
const TOKEN_EXPIRY_TIME = 10 * 60 * 1000 // 10分钟

let tokenExpiryTimer = null

/**
 * 启动token过期计时器
 */
export function startTokenExpiryTimer() {
  // 先清除可能存在的旧计时器
  clearTokenExpiryTimer()
  
  console.log('[TokenExpiry] 启动Token过期计时器，将在10分钟后自动登出')
  
  // 设置新的计时器
  tokenExpiryTimer = setTimeout(() => {
    // 清除token
    localStorage.removeItem('token')
    sessionStorage.removeItem('token') // 同时清理sessionStorage，以防将来改用它
    
    console.log('[TokenExpiry] Token已过期，系统强制登出')
    
    // 显示提示消息
    ElMessage({
      message: '您的登录已过期，请重新登录',
      type: 'warning',
      duration: 5000
    })
    
    // 跳转到登录页
    router.push('/login')
  }, TOKEN_EXPIRY_TIME)
}

/**
 * 清除token过期计时器
 */
export function clearTokenExpiryTimer() {
  if (tokenExpiryTimer) {
    clearTimeout(tokenExpiryTimer)
    tokenExpiryTimer = null
    console.log('[TokenExpiry] 清除Token过期计时器')
  }
}

/**
 * 重置token过期计时器（用于用户活动时延长过期时间）
 * 注意：此函数当前未使用，保留供将来扩展
 */
export function resetTokenExpiryTimer() {
  // 只有当已经登录时才重置计时器
  if (localStorage.getItem('token')) {
    clearTokenExpiryTimer()
    startTokenExpiryTimer()
    console.log('[TokenExpiry] 重置Token过期计时器')
  }
}

/**
 * 检查token是否已经存在，如果存在则启动计时器
 */
export function checkExistingToken() {
  if (localStorage.getItem('token')) {
    console.log('[TokenExpiry] 检测到已有Token，启动过期计时器')
    startTokenExpiryTimer()
  }
} 