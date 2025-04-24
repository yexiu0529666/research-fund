import request from '@/utils/request'

/**
 * 获取仪表盘待办事项
 * @param {Boolean} isAdmin - 是否为管理员
 * @returns {Promise}
 */
export function getDashboardTasks(isAdmin = false) {
  return request({
    url: '/api/dashboard/tasks',
    method: 'get',
    params: { isAdmin }
  })
}

/**
 * 获取仪表盘项目概览
 * @param {Boolean} isAdmin - 是否为管理员
 * @returns {Promise}
 */
export function getDashboardProjects(isAdmin = false) {
  return request({
    url: '/api/dashboard/projects',
    method: 'get',
    params: { isAdmin }
  })
}

/**
 * 获取仪表盘统计数据
 * @param {Boolean} isAdmin - 是否为管理员
 * @returns {Promise}
 */
export function getDashboardStats(isAdmin = false) {
  return request({
    url: '/api/dashboard/stats',
    method: 'get',
    params: { isAdmin }
  })
}

/**
 * 获取项目类型统计
 * @param {Boolean} isAdmin - 是否为管理员
 * @returns {Promise}
 */
export function getProjectTypeStats(isAdmin = false) {
  return request({
    url: '/api/dashboard/project-types',
    method: 'get',
    params: { isAdmin }
  })
}

/**
 * 获取研究成果统计
 * @param {Boolean} isAdmin - 是否为管理员
 * @returns {Promise}
 */
export function getResearchResults(isAdmin = false) {
  return request({
    url: '/api/dashboard/research-results',
    method: 'get',
    params: { isAdmin }
  })
} 