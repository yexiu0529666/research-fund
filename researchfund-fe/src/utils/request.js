import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '@/router'

// 创建axios实例
const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '', // API基础URL
  timeout: 15000 // 请求超时时间
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从本地存储中获取token
    const token = localStorage.getItem('token')
    
    // 如果有token，将其添加到请求头中
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    
    // 记录请求信息
    console.log('API请求:', {
      url: config.url,
      method: config.method,
      data: config.data,
      params: config.params,
      headers: config.headers
    })
    
    return config
  },
  error => {
    // 处理请求错误
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 记录响应信息
    console.log('API响应:', {
      url: response.config.url,
      status: response.status,
      data: res
    })
    
    // 如果是文件下载，直接返回response
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 检查响应状态码
    if (res.code === 200) {
      return res
    } else if (res.code === 401) {
      // 未授权，清除token并重定向到登录页
      console.error('未授权错误:', res)
      ElMessageBox.confirm('您的登录已过期，请重新登录', '提示', {
        confirmButtonText: '重新登录',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        localStorage.removeItem('token')
        router.push('/login')
      })
      return Promise.reject(new Error(res.message || '未授权'))
    } else if (res.code === 403) {
      // 没有权限
      console.error('权限不足错误:', res)
      return Promise.reject(new Error(res.message || '权限不足'))
    } else {
      // 其他错误
      console.error('API错误响应:', res)
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  error => {
    // 处理响应错误
    console.error('API响应错误:', error)
    if (error.response) {
      const { status, data } = error.response
      console.error('错误响应详情:', { status, data })
      let message = '请求失败'
      
      if (status === 401) {
        message = '未授权，请重新登录'
        // 清除token并重定向到登录页
        localStorage.removeItem('token')
        router.push('/login')
      } else if (status === 403) {
        // 详细记录403错误
        console.warn('权限错误:', {
          url: error.config?.url,
          method: error.config?.method,
          user: localStorage.getItem('token') ? '已登录' : '未登录',
          message: data?.message || '权限不足'
        })
        message = data?.message || '您没有操作权限'
      } else if (status === 404) {
        message = '请求的资源不存在'
      } else if (status === 500) {
        message = '服务器内部错误'
      }
      
    } else if (error.message.includes('timeout')) {
      console.error('请求超时:', error)
    } else {
      console.error('请求失败:', error)
    }
    
    return Promise.reject(error)
  }
)

export default service 