import request from '@/utils/request'

/**
 * 获取经费申请列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getExpenseList(params) {
  return request({
    url: '/api/expenses',
    method: 'get',
    params
  })
}

/**
 * 获取当前用户的经费申请列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getMyExpenseList(params) {
  return request({
    url: '/api/expenses/my',
    method: 'get',
    params
  })
}

/**
 * 获取经费申请详情
 * @param {String|Number} id - 经费申请ID
 * @returns {Promise}
 */
export function getExpenseDetail(id) {
  return request({
    url: `/api/expenses/${id}`,
    method: 'get'
  })
}

/**
 * 创建经费申请
 * @param {Object} data - 经费申请数据
 * @returns {Promise}
 */
export function createExpense(data) {
  return request({
    url: '/api/expenses',
    method: 'post',
    data
  })
}

/**
 * 更新经费申请
 * @param {String|Number} id - 经费申请ID
 * @param {Object} data - 经费申请数据
 * @returns {Promise}
 */
export function updateExpense(id, data) {
  return request({
    url: `/api/expenses/${id}`,
    method: 'put',
    data
  })
}

/**
 * 审核经费申请
 * @param {String|Number} id - 经费申请ID
 * @param {String} status - 审核状态：approved-已批准,rejected-已拒绝
 * @param {String} comment - 审核意见
 * @returns {Promise}
 */
export function auditExpense(id, status, comment) {
  return request({
    url: `/api/expenses/${id}/audit`,
    method: 'post',
    params: {
      status,
      comment
    }
  })
}

/**
 * 删除经费申请
 * @param {String|Number} id - 经费申请ID
 * @returns {Promise}
 */
export function deleteExpense(id) {
  return request({
    url: `/api/expenses/${id}`,
    method: 'delete'
  })
}

/**
 * 获取项目的经费支出列表
 * @param {String|Number} projectId - 项目ID
 * @returns {Promise}
 */
export function getExpenseByProject(projectId) {
  return request({
    url: '/api/expenses/project',
    method: 'get',
    params: {
      projectId
    }
  })
}

/**
 * 上传附件
 * @param {File} file - 文件对象
 * @returns {Promise}
 */
export function uploadAttachment(file) {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/api/expenses/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 支付经费申请
 * @param {String|Number} id - 经费申请ID
 * @returns {Promise}
 */
export function payExpense(id) {
  return request({
    url: `/api/expenses/${id}/pay`,
    method: 'post'
  })
}

/**
 * 提交报销凭证
 * @param {Number} id - 经费申请ID 
 * @param {Array} attachments - 报销凭证附件列表
 * @returns {Promise}
 */
export function submitReceipt(id, attachments) {
  return request({
    url: `/api/expenses/${id}/submit-receipt`,
    method: 'post',
    data: attachments
  })
}

/**
 * 负责人自行还款
 * @param {Number} id - 经费申请ID
 * @returns {Promise}
 */
export function repayExpense(id) {
  return request({
    url: `/api/expenses/${id}/repay`,
    method: 'post'
  })
}

/**
 * 审核报销凭证
 * @param {Number} id - 经费申请ID
 * @param {String} status - 审核状态：approved-批准,rejected-拒绝
 * @param {String} comment - 审核意见
 * @returns {Promise}
 */
export function auditReceipt(id, status, comment) {
  return request({
    url: `/api/expenses/${id}/audit-receipt`,
    method: 'post',
    params: {
      status,
      comment
    }
  })
} 