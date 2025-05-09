/**
 * 测试多个经费来源功能
 * 
 * 该文件仅用于测试，演示如何处理多个经费来源
 */

// 示例数据
const exampleProject = {
  id: 1,
  name: '测试项目',
  type: 'school',
  // 单个经费来源（兼容旧代码）
  fundingSource: 'fiscal',
  // 多个经费来源（新功能）
  fundingSources: ['fiscal', 'school', 'other'],
  leaderName: '张三',
  startDate: '2023-01-01',
  endDate: '2023-12-31',
  budget: 100000,
  usedBudget: 30000,
  status: 'active'
};

// 测试展示多个经费来源
function showMultipleFundingSources(project) {
  // 优先使用fundingSources字段
  if (project.fundingSources && project.fundingSources.length > 0) {
    return project.fundingSources.map(source => getFundingSourceLabel(source)).join(', ');
  } 
  // 兼容旧版本，使用单个fundingSource字段
  else if (project.fundingSource) {
    return getFundingSourceLabel(project.fundingSource);
  }
  return '未知来源';
}

// 获取经费来源标签
function getFundingSourceLabel(source) {
  const sourceMap = {
    'fiscal': '财政经费',
    'school': '校配套经费',
    'other': '其他经费'
  };
  return sourceMap[source] || '未知来源';
}

// 测试多选表单
function createMultipleSelectionForm() {
  // 表单结构示例
  const form = {
    fundingSources: ['fiscal', 'school'] // 默认选中两个经费来源
  };
  
  return form;
}

// 测试打印结果
console.log('项目经费来源展示:', showMultipleFundingSources(exampleProject));
console.log('表单多选示例:', createMultipleSelectionForm());

export {
  showMultipleFundingSources,
  getFundingSourceLabel,
  createMultipleSelectionForm
}; 