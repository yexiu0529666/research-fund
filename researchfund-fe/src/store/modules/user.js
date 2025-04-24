import { defineStore } from 'pinia'
import { login, logout, getUserInfo } from '@/api/user'
import { startTokenExpiryTimer, clearTokenExpiryTimer } from '@/utils/token-expiry'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('token'),
    userId: '',
    name: '',
    roles: [],
    permissions: [],
    department: {}
  }),
  
  getters: {
    // 是否有管理员权限
    isAdmin: (state) => {
      return state.roles.includes('ROLE_ADMIN')
    },
    
  },
  
  actions: {
    // 登录
    login(userInfo) {
      const { username, password, rememberMe } = userInfo
      return new Promise((resolve, reject) => {
        login({ username: username.trim(), password })
          .then(response => {
            console.log('登录响应:', response)
            if (response.code === 200) {
              const { token, user, roles } = response.data
              
              // 保存token到状态和本地存储
              this.token = token
              localStorage.setItem('token', token)
              
              // 保存基本用户信息
              this.userId = user.id
              this.name = user.realName || user.username
              this.roles = roles || []
              
              // 如果选择记住我，保存用户名到本地存储
              if (rememberMe) {
                localStorage.setItem('rememberedUser', JSON.stringify({ 
                  username: username 
                }))
              } else {
                localStorage.removeItem('rememberedUser')
              }
              
              // 启动Token过期定时器
              startTokenExpiryTimer()
              
              resolve()
            } else {
              console.error('登录失败，响应状态码不是200:', response)
              reject(new Error(response.message || '登录失败'))
            }
          })
          .catch(error => {
            console.error('登录请求异常:', error)
            if (error.response) {
              console.error('错误响应数据:', error.response.data)
              reject(new Error(error.response.data.message || '登录失败，服务器错误'))
            } else if (error.message) {
              reject(new Error('登录失败: ' + error.message))
            } else {
              reject(new Error('登录失败，网络错误'))
            }
          })
      })
    },
    
    // 获取用户信息
    getUserInfo() {
      return new Promise((resolve, reject) => {
        getUserInfo()
          .then(response => {
            if (response.code === 200) {
              const { user, roles, permissions, department } = response.data
              
              // 更新用户信息
              this.userId = user.id
              this.name = user.realName || user.username
              this.roles = roles || []
              this.permissions = permissions || []
              this.department = department || {}
              
              resolve(response.data)
            } else {
              reject(new Error(response.message || '获取用户信息失败'))
            }
          })
          .catch(error => {
            reject(error)
          })
      })
    },
    
    // 登出
    logout() {
      return new Promise((resolve, reject) => {
        logout()
          .then(() => {
            // 清除状态和本地存储中的token
            this.resetState()
            localStorage.removeItem('token')
            
            // 清除Token过期定时器
            clearTokenExpiryTimer()
            
            resolve()
          })
          .catch(error => {
            // 即使后端登出接口失败，也要清除前端的登录状态
            this.resetState()
            localStorage.removeItem('token')
            
            // 清除Token过期定时器
            clearTokenExpiryTimer()
            
            reject(error)
          })
      })
    },
    
    // 重置状态
    resetState() {
      this.token = ''
      this.userId = ''
      this.name = ''
      this.roles = []
      this.permissions = []
      this.department = {}
    }
  }
}) 