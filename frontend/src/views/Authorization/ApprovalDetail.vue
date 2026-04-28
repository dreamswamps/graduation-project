<template>
  <div class="ApprovalDetail">
    <div class="page-header">
      <el-button @click="handleBack" icon="ArrowLeft" circle class="back-btn"></el-button>
      <h2 class="page-title">审批详情</h2>
    </div>

    <!-- 基础信息卡片 -->
    <el-card class="detail-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">基础信息</span>
        </div>
      </template>

      <el-descriptions border :column="3" class="detail-descriptions">
        <el-descriptions-item label="审批单号">
          {{ rowData.aid ?? '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="审批类型">
          <el-tag :type="getTypeTagType(rowData.type)" size="small">
            {{ getApprovalTypeText(rowData.type) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag :type="getStatusTagType(rowData.approval)" size="small">
            {{ getApprovalStatusText(rowData.approval) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="申请人">
          {{ rowData.user_name ?? '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="申请账号">
          {{ rowData.apply_account ?? '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="申请时间">
          {{ rowData.apply_time || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="最近审批" v-if="rowData.approved_time">
          {{ rowData.approved_time || '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 审批内容卡片 -->
    <el-card class="detail-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <span class="card-title">审批内容</span>
        </div>
      </template>

      <el-descriptions border :column="2" class="detail-descriptions">
        <el-descriptions-item label="用户邮箱">
          {{ rowData.user_email || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="完成状态">
          <el-tag :type="getCompletedStatus().type">
            {{ getCompletedStatus().text }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="原因">
          {{ rowData.reason || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="处理状态">
          <el-tag :type="getWorkingStatus().type">
            {{ getWorkingStatus().text }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理人">
          {{ rowData.worker_name ?? '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="处理人ID">
          {{ rowData.worker ?? '-' }}
        </el-descriptions-item>
      </el-descriptions>
    </el-card>

    <!-- 空数据提示 -->
    <el-empty
      v-if="!rowData.aid"
      description="暂无审批详情数据"
      class="empty-tip"
    >
      <el-button type="primary" @click="handleBack">返回列表</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { 
  optionTypeMap, 
  approvalStatusMap, 
  completedStatusMap, 
  workingStatusMap,
  typeTagMap,
} from './RelationMap'

// 路由实例
const router = useRouter()
const rowData = ref({})

// 返回上一页
const handleBack = () => {
  router.back()
}

// 审批类型转文字
const getApprovalTypeText = (type) => {
  return optionTypeMap[type] || type || '未知类型'
}

// 审批类型标签样式
const getTypeTagType = (type) => {
  return typeTagMap[type] || 'info'
}

// 审批状态转文字
const getApprovalStatusText = (status) => {
  return approvalStatusMap[status]?.text || '未知状态'
}

// 审批状态标签样式
const getStatusTagType = (status) => {
  return approvalStatusMap[status]?.type || 'default'
}

// 获取完成状态
const getCompletedStatus = () => {
  const status = rowData.value.has_completed
  return {
    text: completedStatusMap[status]?.text || '未知',
    type: completedStatusMap[status]?.type || 'info'
  }
}

// 获取处理状态，如果已经完成审批，则处理状态为特殊状态，且和status值无关
const getWorkingStatus = () => {
  let status = rowData.value.working
  if (rowData.value.has_completed == 1) {
   status = 100; 
  }
  return {
    text: workingStatusMap[status]?.text || '未知',
    type: workingStatusMap[status]?.type || 'info'
  }
}

// 页面初始化
onMounted(() => {
  const storage = sessionStorage.getItem('approvalDetail')
  if (storage) {
    rowData.value = JSON.parse(storage)
    sessionStorage.removeItem('approvalDetail')
    console.log('审批详情数据:', rowData.value)
  } else {
    ElMessage.warning('未获取到审批详情数据')
  }
})
</script>

<style scoped>
.ApprovalDetail {
  padding: 20px;
  background-color: #f5f7fa;
}

/* 页面头部样式 */
.page-header {
  display: flex;
  align-items: center;
  margin-bottom: 24px;
  padding: 8px 0;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.back-btn {
  margin: 0 16px;
  border: none;
  background-color: #f0f7ff;
  color: #409eff;
  transition: all 0.3s ease;
}

.back-btn:hover {
  background-color: #409eff;
  color: #ffffff;
  transform: scale(1.1);
}

.page-title {
  margin: 0;
  font-size: 24px;
  font-weight: 600;
  color: #1f2937;
  position: relative;
  padding-left: 16px;
}

.page-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 50%;
  transform: translateY(-45%);
  width: 4px;
  height: 24px;
  background: linear-gradient(to bottom, #409eff, #79bbff);
  border-radius: 2px;
}

/* 卡片样式 - 统一风格 */
.detail-card {
  margin-bottom: 20px;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  box-shadow: 2px 2px 6px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  transition: all 0.3s ease;
}

.detail-card:hover {
  box-shadow: 4px 4px 12px rgba(0, 0, 0, 0.3);
}

/* 卡片头部样式 */
:deep(.detail-card .el-card__header) {
  background-color: #f0f7ff;
  border-bottom: 1px solid #e4e7ed;
  padding: 15px 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: #409eff;
}

/* 描述列表样式 - 统一风格 */
.detail-descriptions {
  margin: 10px 0;
}

:deep(.detail-descriptions .el-descriptions__header) {
  margin-bottom: 0;
}

:deep(.detail-descriptions .el-descriptions__body) {
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #e4e7ed;
}

:deep(.detail-descriptions .el-descriptions__cell) {
  padding: 12px 16px;
  font-size: 14px;
}

:deep(.detail-descriptions .el-descriptions__label) {
  background-color: #f0f7ff;
  color: #409eff;
  font-weight: 500;
  width: 120px;
}

:deep(.detail-descriptions .el-descriptions__content) {
  background-color: #ffffff;
  color: #333;
}

/* 标签样式 - 统一风格 */
:deep(.el-tag) {
  border-radius: 4px;
  padding: 0 8px;
  height: 24px;
  line-height: 22px;
  font-size: 12px;
  border: none;
  font-weight: normal;
}

:deep(.el-tag--warning) {
  background-color: #fdf6ec;
  color: #e6a23c;
}

:deep(.el-tag--success) {
  background-color: #f0f9eb;
  color: #67c23a;
}

:deep(.el-tag--danger) {
  background-color: #fef0f0;
  color: #f56c6c;
}

:deep(.el-tag--info) {
  background-color: #f4f4f5;
  color: #909399;
}

:deep(.el-tag--primary) {
  background-color: #ecf5ff;
  color: #409eff;
}

/* 空数据提示 */
.empty-tip {
  padding: 60px 0;
  background-color: #ffffff;
  border-radius: 8px;
  border: 1px solid #e4e7ed;
  box-shadow: 2px 2px 6px rgba(0, 0, 0, 0.1);
  margin-top: 20px;
}

:deep(.empty-tip .el-empty__description) {
  color: #909399;
}

</style>