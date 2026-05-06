<template>
  <div class="ApprovalDetail">
    <!-- 页面头部 -->
    <div class="page-header">
      <el-button @click="handleBack" icon="ArrowLeft" circle class="back-btn" />
      <div class="header-content">
        <h2 class="page-title">审批详情</h2>
      </div>
      <el-tag v-if="rowData.aid" :type="getStatusTagType(rowData.approval)" size="large" class="status-badge">
        {{ getApprovalStatusText(rowData.approval) }}
      </el-tag>
    </div>

    <template v-if="rowData.aid">
      <!-- 基础信息卡片 -->
      <div class="Card">
        <div class="card-section-title">
          <el-icon class="section-icon"><Document /></el-icon>
          基础信息
        </div>
        <el-descriptions border :column="3">
          <el-descriptions-item label="审批单号">
            <span class="mono-text">{{ rowData.aid }}</span>
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
          <el-descriptions-item label="申请人">{{ rowData.user_name ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="申请账号">
            <span class="mono-text">{{ rowData.apply_account ?? '-' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="申请时间">
            <span class="time-text">{{ rowData.apply_time || '-' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="最近审批" v-if="rowData.approved_time">
            <span class="time-text">{{ rowData.approved_time }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 审批内容卡片 -->
      <div class="Card">
        <div class="card-section-title">
          <el-icon class="section-icon"><InfoFilled /></el-icon>
          审批内容
        </div>
        <el-descriptions border :column="2">
          <el-descriptions-item label="用户邮箱">{{ rowData.user_email || '-' }}</el-descriptions-item>
          <el-descriptions-item label="完成状态">
            <el-tag :type="getCompletedStatus().type" size="small">{{ getCompletedStatus().text }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="原因" :span="2">
            <span class="reason-text">{{ rowData.reason || '-' }}</span>
          </el-descriptions-item>
          <el-descriptions-item label="处理状态">
            <el-tag :type="getWorkingStatus().type" size="small">{{ getWorkingStatus().text }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="处理人">{{ rowData.worker_name ?? '-' }}</el-descriptions-item>
          <el-descriptions-item label="处理人ID">
            <span class="mono-text">{{ rowData.worker ?? '-' }}</span>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </template>

    <!-- 空数据提示 -->
    <div v-else class="empty-card">
      <el-empty description="暂无审批详情数据">
        <el-button type="primary" @click="handleBack">返回列表</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, InfoFilled } from '@element-plus/icons-vue'
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
    padding: 24px;
    background: linear-gradient(135deg, #f5f7fa 0%, #e8ecf4 100%);
    min-height: 100vh;
    animation: fadeIn 0.5s ease-in;
}

@keyframes fadeIn {
    from { opacity: 0; transform: translateY(10px); }
    to { opacity: 1; transform: translateY(0); }
}

/* 页面头部 */
.page-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 28px;
    background: #ffffff;
    border-radius: 20px;
    padding: 20px 28px;
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.12);
    border: 1px solid rgba(102, 126, 234, 0.08);
    position: relative;
    overflow: hidden;
}

.page-header::before {
    content: '';
    position: absolute;
    top: 0; left: 0; right: 0;
    height: 3px;
    background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
}

.back-btn {
    border: none;
    background: linear-gradient(135deg, #f0f4ff 0%, #e8ecf4 100%);
    color: #667eea;
    transition: all 0.3s ease;
    flex-shrink: 0;
}

.back-btn:hover {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: #ffffff;
    transform: scale(1.1);
}

.header-content { flex: 1; }

.page-title {
    font-size: 24px;
    font-weight: 600;
    margin: 0 0 4px 0;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
}

.page-subtitle {
    font-size: 13px;
    color: #7f8c8d;
    margin: 0;
}

.status-badge {
    font-size: 14px;
    padding: 8px 20px;
    height: auto;
    border-radius: 20px;
    font-weight: 500;
}

/* 卡片 */
.Card {
    background: #ffffff;
    border-radius: 20px;
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.12);
    padding: 28px 32px;
    margin-bottom: 20px;
    border: 1px solid rgba(102, 126, 234, 0.08);
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    animation: slideUp 0.6s ease-out;
}

@keyframes slideUp {
    from { opacity: 0; transform: translateY(16px); }
    to { opacity: 1; transform: translateY(0); }
}

.Card:hover {
    box-shadow: 0 12px 40px rgba(102, 126, 234, 0.18);
    transform: translateY(-2px);
}

.card-section-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 17px;
    font-weight: 600;
    color: #2c3e50;
    margin-bottom: 20px;
    padding-bottom: 14px;
    border-bottom: 2px solid rgba(102, 126, 234, 0.1);
}

.section-icon {
    color: #667eea;
    font-size: 18px;
}

/* descriptions 样式 */
:deep(.el-descriptions__label) {
    background: linear-gradient(135deg, #f0f4ff 0%, #e8ecf4 100%) !important;
    color: #667eea !important;
    font-weight: 500;
    width: 110px;
}

:deep(.el-descriptions__content) {
    background: #ffffff !important;
    color: #2c3e50;
}

:deep(.el-descriptions__cell) {
    padding: 14px 18px !important;
}

/* 文本样式 */
.mono-text {
    font-family: 'Courier New', monospace;
    background: rgba(102, 126, 234, 0.08);
    padding: 2px 8px;
    border-radius: 4px;
    font-size: 13px;
    color: #667eea;
}

.time-text {
    color: #7f8c8d;
    font-size: 13px;
}

.reason-text {
    color: #2c3e50;
    line-height: 1.6;
}

/* 空状态 */
.empty-card {
    background: #ffffff;
    border-radius: 20px;
    padding: 60px 32px;
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.12);
    border: 1px solid rgba(102, 126, 234, 0.08);
    text-align: center;
}

/* 按钮 */
.el-button {
    border-radius: 8px;
    font-weight: 500;
    transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.el-button:not(.is-circle):hover { transform: translateY(-2px); }
</style>