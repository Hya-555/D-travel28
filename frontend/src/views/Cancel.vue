<template>
  <div>
    <el-card shadow="never">
      <template #header>取消/变更管理</template>

      <!-- 查询申请 -->
      <el-form :inline="true" :model="query" style="margin-bottom: 20px;">
        <el-form-item label="旅游团代码"><el-input v-model="query.groupCode" /></el-form-item>
        <el-form-item label="出发日期"><el-date-picker v-model="query.departureDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="责任人"><el-input v-model="query.contactName" /></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button></el-form-item>
      </el-form>

      <template v-if="app">
        <el-descriptions :column="2" border style="margin-bottom: 20px;">
          <el-descriptions-item label="申请编号">{{ app.applicationId }}</el-descriptions-item>
          <el-descriptions-item label="责任人">{{ app.contactName }}</el-descriptions-item>
          <el-descriptions-item label="已付金额">￥{{ app.paidAmount }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="app.status === 'CANCELLED' ? 'danger' : 'success'">{{ app.status }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <!-- 参加者列表（可逐个取消） -->
        <el-table v-if="participants.length" :data="participants" stripe>
          <el-table-column prop="name" label="姓名" />
          <el-table-column prop="isContactPerson" label="责任人" width="80">
            <template #default="{ row }">{{ row.isContactPerson ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column prop="status" label="当前状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="{ row }">
              <el-button v-if="row.status === 'ACTIVE'" size="small" type="danger" @click="removeParticipant(row)">取消该参加者</el-button>
              <el-button v-if="row.status === 'ACTIVE' && !row.isContactPerson" size="small" type="warning" @click="setAsContact(row)">设为责任人</el-button>
            </template>
          </el-table-column>
        </el-table>
      </template>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { findApplication, getParticipants, cancelApplication } from '../api'

const query = reactive({ groupCode: '', departureDate: '', contactName: '' })
const app = ref(null)
const participants = ref([])
const newContactId = ref(null)

const search = async () => {
  try {
    const appRes = await findApplication(query)
    app.value = appRes.data.data
    if (app.value) {
      const pRes = await getParticipants(app.value.applicationId)
      participants.value = pRes.data.data || []
    } else {
      ElMessage.warning('未找到匹配的申请')
    }
  } catch (e) { ElMessage.error('查询失败') }
}

const removeParticipant = async (row) => {
  const data = { applicationId: app.value.applicationId, participantId: row.participantId, cancelType: 'PARTICIPANT_REMOVE', employeeId: 1 }
  if (row.isContactPerson) {
    try {
      const { value } = await ElMessageBox.prompt('该参加者是申请责任人，请输入新责任人的参加者编号', '选定新责任人', { confirmButtonText: '确定', cancelButtonText: '取消' })
      if (!value) return
      data.newContactParticipantId = parseInt(value, 10)
    } catch { return }
  }
  try {
    const res = await cancelApplication(data)
    const result = res.data.data
    ElMessage.success(`取消成功，退款: ￥${result.refundAmount}，手续费: ￥${result.handlingFee}`)
    search()
  } catch (e) { ElMessage.error(e.response?.data?.msg || '操作失败') }
}

const setAsContact = async () => {
  ElMessage.info('请联系管理员变更责任人')
}
</script>
