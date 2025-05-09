import request from '@/utils/request'

/**
 * 获取项目列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getProjectList(params) {
  return request({
    url: '/api/projects',
    method: 'get',
    params
  })
}

/**
 * 获取当前用户的项目列表
 * @param {Object} params - 查询参数
 * @returns {Promise}
 */
export function getMyProjects(params) {
  return request({
    url: '/api/projects/my',
    method: 'get',
    params
  })
}

/**
 * 获取当前用户可用于报销申请的项目列表
 * @returns {Promise}
 */
export function getAvailableProjectsForExpense() {
  return request({
    url: '/api/projects/available-for-expense',
    method: 'get'
  })
}

/**
 * 获取项目详情
 * @param {String|Number} id - 项目ID
 * @returns {Promise}
 */
export function getProjectDetail(id) {
  return request({
    url: `/api/projects/${id}`,
    method: 'get'
  })
}

/**
 * 创建项目
 * @param {Object} data - 项目数据
 * @returns {Promise}
 */
export function createProject(data) {
  console.log('调用创建项目API，提交数据:', data);
  return request({
    url: '/api/projects',
    method: 'post',
    data,
    headers: {
      'Content-Type': 'application/json'
    }
  }).then(response => {
    console.log('创建项目成功，服务器响应:', response);
    return response;
  }).catch(error => {
    console.error('创建项目失败:', error);
    throw error;
  });
}

/**
 * 更新项目
 * @param {String|Number} id - 项目ID
 * @param {Object} data - 项目数据
 * @returns {Promise}
 */
export function updateProject(id, data) {
  return request({
    url: `/api/projects/${id}`,
    method: 'put',
    data
  })
}

/**
 * 删除项目
 * @param {String|Number} id - 项目ID
 * @returns {Promise}
 */
export function deleteProject(id) {
  return request({
    url: `/api/projects/${id}`,
    method: 'delete'
  })
}

/**
 * 审核项目
 * @param {String|Number} id - 项目ID
 * @param {String} auditStatus - 审核状态
 * @param {String} comment - 审核意见
 * @returns {Promise}
 */
export function auditProject(id, auditStatus, comment) {
  return request({
    url: `/api/projects/${id}/audit`,
    method: 'post',
    params: {
      auditStatus,
      comment
    }
  })
}

/**
 * 确认立项申请
 * @param {String|Number} id - 项目ID
 * @returns {Promise}
 */
export function confirmProject(id) {
  return request({
    url: `/api/projects/${id}/confirm`,
    method: 'post'
  })
}

/**
 * 获取项目经费使用统计
 * @param {String|Number} id - 项目ID
 * @returns {Promise}
 */
export function getProjectExpenseStats(id) {
  return request({
    url: `/api/projects/${id}/expense-stats`,
    method: 'get'
  })
}

/**
 * 获取项目选项列表（用于下拉选择）
 * @returns {Promise}
 */
export function getProjectOptions() {
  return request({
    url: '/api/projects/options',
    method: 'get'
  })
}

/**
 * 获取待结题项目列表（用于经费结转）
 * @returns {Promise}
 */
export function getPendingCompletionProjects() {
  return request({
    url: '/api/projects/pending-completion',
    method: 'get'
  })
}

/**
 * 提交项目结题报告
 * @param {String|Number} id - 项目ID
 * @param {String} reportPath - 报告文件路径
 * @returns {Promise}
 */
export function submitCompletionReport(id, reportPath) {
  return request({
    url: `/api/projects/${id}/completion-report`,
    method: 'post',
    params: {
      reportPath
    }
  })
}

/**
 * 审核项目结题
 * @param {String|Number} id - 项目ID
 * @param {String} auditStatus - 审核状态 (approved 或 rejected)
 * @param {String} comment - 审核意见
 * @returns {Promise}
 */
export function auditProjectCompletion(id, auditStatus, comment) {
  return request({
    url: `/api/projects/${id}/completion-audit`,
    method: 'post',
    params: {
      auditStatus,
      comment
    }
  })
}

/**
 * 获取项目可用预算科目列表
 * @param {Number} projectId - 项目ID
 * @returns {Promise}
 */
export function getProjectBudgetItems(projectId) {
  return request({
    url: `/api/projects/${projectId}/budget-items`,
    method: 'get'
  })
} 