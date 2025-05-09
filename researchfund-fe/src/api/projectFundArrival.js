import request from '@/utils/request'

/**
 * 获取经费到账记录列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getFundArrivalList(params) {
  return request({
    url: '/api/project-fund-arrivals',
    method: 'get',
    params
  })
}

/**
 * 获取项目的经费到账记录列表
 * @param {String|Number} projectId - 项目ID
 * @returns {Promise}
 */
export function getProjectFundArrivals(projectId) {
  return request({
    url: `/api/project-fund-arrivals/project/${projectId}`,
    method: 'get'
  })
}

/**
 * 获取项目已到账总金额
 * @param {String|Number} projectId - 项目ID
 * @returns {Promise}
 */
export function getProjectTotalArrivedAmount(projectId) {
  return request({
    url: `/api/project-fund-arrivals/project/${projectId}/total-amount`,
    method: 'get'
  })
}

/**
 * 获取经费到账记录详情
 * @param {String|Number} id - 记录ID
 * @returns {Promise}
 */
export function getFundArrivalDetail(id) {
  return request({
    url: `/api/project-fund-arrivals/${id}`,
    method: 'get'
  })
}

/**
 * 创建经费到账记录
 * @param {Object} data - 经费到账数据
 * @returns {Promise}
 */
export function createFundArrival(data) {
  // Ensure voucherPath is a string if it's an object
  const processedData = { ...data };
  if (processedData.voucherPath && typeof processedData.voucherPath === 'object') {
    processedData.voucherPath = processedData.voucherPath.url || '';
  }
  
  return request({
    url: '/api/project-fund-arrivals',
    method: 'post',
    data: processedData
  });
}

/**
 * 更新经费到账记录
 * @param {String|Number} id - 记录ID
 * @param {Object} data - 经费到账数据
 * @returns {Promise}
 */
export function updateFundArrival(id, data) {
  // Ensure voucherPath is a string if it's an object
  const processedData = { ...data };
  if (processedData.voucherPath && typeof processedData.voucherPath === 'object') {
    processedData.voucherPath = processedData.voucherPath.url || '';
  }
  
  return request({
    url: `/api/project-fund-arrivals/${id}`,
    method: 'put',
    data: processedData
  });
}

/**
 * 删除经费到账记录
 * @param {String|Number} id - 记录ID
 * @returns {Promise}
 */
export function deleteFundArrival(id) {
  return request({
    url: `/api/project-fund-arrivals/${id}`,
    method: 'delete'
  })
}

/**
 * 获取可用于添加经费到账的项目列表（进行中的项目）
 * @returns {Promise}
 */
export function getAvailableProjectsForFundArrival() {
  return request({
    url: '/api/project-fund-arrivals/available-projects',
    method: 'get'
  })
}

/**
 * 获取项目各经费来源的到账金额统计
 * @param {String|Number} projectId - 项目ID
 * @returns {Promise}
 */
export function getProjectFundArrivalBySource(projectId) {
  return request({
    url: `/api/project-fund-arrivals/project/${projectId}/by-source`,
    method: 'get'
  })
} 