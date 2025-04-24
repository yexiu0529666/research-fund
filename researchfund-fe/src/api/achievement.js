import request from '@/utils/request'

/**
 * 获取成果奖励列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getAchievementList(params) {
  return request({
    url: '/api/achievements',
    method: 'get',
    params
  })
}

/**
 * 获取成果奖励详情
 * @param {String|Number} id - 成果奖励ID
 * @returns {Promise}
 */
export function getAchievementById(id) {
  return request({
    url: `/api/achievements/${id}`,
    method: 'get'
  })
}

/**
 * 创建成果奖励
 * @param {Object} data - 成果奖励数据
 * @returns {Promise}
 */
export function createAchievement(data) {
  return request({
    url: '/api/achievements',
    method: 'post',
    data
  })
}

/**
 * 更新成果奖励
 * @param {String|Number} id - 成果奖励ID
 * @param {Object} data - 成果奖励数据
 * @returns {Promise}
 */
export function updateAchievement(id, data) {
  return request({
    url: `/api/achievements/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除成果奖励
 * @param {String|Number} id - 成果奖励ID
 * @returns {Promise}
 */
export function deleteAchievement(id) {
  return request({
    url: `/api/achievements/${id}`,
    method: 'delete'
  })
}

/**
 * 审核成果奖励
 * @param {String|Number} id - 成果奖励ID
 * @param {Object} data - 审核数据
 * @returns {Promise}
 */
export function auditAchievement(id, data) {
  return request({
    url: `/api/achievements/${id}/audit`,
    method: 'post',
    data
  })
}

/**
 * 支付成果奖励
 * @param {String|Number} id - 成果奖励ID
 * @returns {Promise}
 */
export function payAchievement(id) {
  return request({
    url: `/api/achievements/${id}/pay`,
    method: 'post'
  })
} 