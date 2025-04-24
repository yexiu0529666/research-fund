import request from '@/utils/request'

/**
 * 获取经费结转申请列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getTransferList(params) {
  console.log('调用获取经费结转列表API，参数:', params)
  return request({
    url: '/api/transfers',
    method: 'get',
    params
  }).then(response => {
    console.log('获取经费结转列表成功，原始返回数据:', response)
    // 检查数据结构并输出
    if (response && response.data) {
      console.log('数据结构:', {
        hasTotal: 'total' in response.data,
        hasList: 'list' in response.data,
        hasRows: 'rows' in response.data,
        dataKeys: Object.keys(response.data)
      })
    }
    return response
  }).catch(error => {
    console.error('获取经费结转列表失败:', error)
    throw error
  })
}

/**
 * 获取我的经费结转申请列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getMyTransferList(params) {
  console.log('调用获取我的经费结转列表API，参数:', params)
  return request({
    url: '/api/transfers/my',
    method: 'get',
    params
  }).then(response => {
    console.log('获取我的经费结转列表成功，原始返回数据:', response)
    // 检查数据结构并输出
    if (response && response.data) {
      console.log('数据结构:', {
        hasTotal: 'total' in response.data,
        hasList: 'list' in response.data,
        hasRows: 'rows' in response.data,
        dataKeys: Object.keys(response.data)
      })
    }
    return response
  }).catch(error => {
    console.error('获取我的经费结转列表失败:', error)
    throw error
  })
}

/**
 * 获取待审核的经费结转申请列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getPendingTransferList(params) {
  return request({
    url: '/api/transfers',
    method: 'get',
    params: { ...params, status: 'pending' }
  })
}

/**
 * 获取经费结转申请详情
 * @param {String|Number} id - 经费结转申请ID
 * @returns {Promise}
 */
export function getTransferDetail(id) {
  return request({
    url: `/api/transfers/${id}`,
    method: 'get'
  })
}

/**
 * 创建经费结转申请
 * @param {Object} data - 经费结转申请数据
 * @returns {Promise}
 */
export function createTransfer(data) {
  return request({
    url: '/api/transfers',
    method: 'post',
    data
  })
}

/**
 * 更新经费结转申请
 * @param {String|Number} id - 经费结转申请ID
 * @param {Object} data - 经费结转申请数据
 * @returns {Promise}
 */
export function updateTransfer(id, data) {
  return request({
    url: `/api/transfers/${id}`,
    method: 'put',
    data
  })
}

/**
 * 审核经费结转申请
 * @param {String|Number} id - 经费结转申请ID
 * @param {String} status - 审核状态: approved(批准) / rejected(拒绝)
 * @param {String} comment - 审核意见
 * @returns {Promise}
 */
export function auditTransfer(id, status, comment) {
  return request({
    url: `/api/transfers/${id}/audit`,
    method: 'post',
    params: {
      status,
      comment
    }
  })
}

/**
 * 删除经费结转申请
 * @param {String|Number} id - 经费结转申请ID
 * @returns {Promise}
 */
export function deleteTransfer(id) {
  return request({
    url: `/api/transfers/${id}`,
    method: 'delete'
  })
} 