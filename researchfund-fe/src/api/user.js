import request from '@/utils/request'

/**
 * 用户登录
 * @param {Object} data - 登录信息 {username, password}
 * @returns {Promise}
 */
export function login(data) {
  return request({
    url: '/api/user/login',
    method: 'post',
    data
  })
}

/**
 * 获取用户信息
 * @returns {Promise}
 */
export function getUserInfo() {
  return request({
    url: '/api/user/info',
    method: 'get'
  })
}

/**
 * 用户登出
 * @returns {Promise}
 */
export function logout() {
  return request({
    url: '/api/user/logout',
    method: 'post'
  })
}

/**
 * 获取所有用户列表
 * @param {Object} query 查询参数
 * @returns {Promise}
 */
export function getAllUsers(query) {
  return request({
    url: '/api/user/list',
    method: 'get',
    params: query
  })
}

/**
 * 获取用户详情
 * @param {number} id 用户ID
 * @returns {Promise}
 */
export function getUserDetail(id) {
  return request({
    url: `/api/user/${id}`,
    method: 'get'
  })
}

/**
 * 更新用户信息
 * @param {number} id 用户ID
 * @param {Object} data 用户数据
 * @returns {Promise}
 */
export function updateUser(id, data) {
  return request({
    url: `/api/user/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除用户
 * @param {number} id 用户ID
 * @returns {Promise}
 */
export function deleteUser(id) {
  return request({
    url: `/api/user/${id}`,
    method: 'delete'
  })
}

/**
 * 获取所有角色
 * @returns {Promise}
 */
export function getAllRoles() {
  return request({
    url: '/api/role/all',
    method: 'get'
  })
}

/**
 * 获取所有部门
 * @returns {Promise}
 */
export function getAllDepartments() {
  return request({
    url: '/api/department/all',
    method: 'get'
  })
}

/**
 * 更新用户密码
 * @param {Object} data - 包含旧密码和新密码的对象 {oldPassword, newPassword}
 * @returns {Promise}
 */
export function updatePassword(data) {
  return request({
    url: '/api/user/changePassword',
    method: 'post',
    data
  })
}

/**
 * 重置用户密码
 * @param {number} userId 用户ID
 * @param {string} password 新密码
 * @returns {Promise}
 */
export function resetPassword(userId, password) {
  return request({
    url: '/api/user/resetPassword',
    method: 'post',
    data: {
      userId,
      password
    }
  })
}

/**
 * 获取指定部门的用户列表
 * @param {String|Number} departmentId - 部门ID
 * @returns {Promise}
 */
export function getUsersByDepartment(departmentId) {
  return request({
    url: `/api/user/department/${departmentId}`,
    method: 'get'
  })
}

/**
 * 更新用户头像
 * @param {FormData} formData - 包含头像文件的表单数据
 * @returns {Promise}
 */
export function updateAvatar(formData) {
  return request({
    url: '/api/user/avatar',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 用户注册
 * @param {Object} data - 注册信息
 * @returns {Promise}
 */
export function register(data) {
  return request({
    url: '/api/user/register',
    method: 'post',
    data
  })
} 