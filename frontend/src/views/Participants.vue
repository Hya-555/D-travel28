<template>
  <div>
    <el-card shadow="never">
      <template #header>参加者录入</template>

      <!-- 查询申请 -->
      <el-form :inline="true" :model="query" style="margin-bottom: 20px;">
        <el-form-item label="旅游团代码">
          <el-input v-model="query.groupCode" placeholder="旅游团代码" />
        </el-form-item>
        <el-form-item label="出发日期">
          <el-date-picker v-model="query.departureDate" type="date" placeholder="出发日期" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="责任人姓名">
          <el-input v-model="query.contactName" placeholder="责任人姓名" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">查询</el-button>
        </el-form-item>
      </el-form>

      <!-- 申请信息 -->
      <el-descriptions v-if="app" :column="2" border style="margin-bottom: 20px;">
        <el-descriptions-item label="申请编号">{{ app.applicationId }}</el-descriptions-item>
        <el-descriptions-item label="责任人">{{ app.contactName }} / {{ app.contactPhone }}</el-descriptions-item>
        <el-descriptions-item label="旅游团">{{ app.groupCode }}</el-descriptions-item>
        <el-descriptions-item label="出发日期">{{ app.departureDate }}</el-descriptions-item>
        <el-descriptions-item label="总人数">大人{{ app.adultCount }} 小孩{{ app.childCount }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="app.status === 'COMPLETED' ? 'success' : 'warning'">{{ app.status }}</el-tag>
        </el-descriptions-item>
      </el-descriptions>

      <!-- 录入参加者 -->
      <el-dialog v-model="dialogVisible" title="录入参加者信息" width="600px">
        <el-form :model="participant" label-width="140px">
          <el-form-item label="姓名" required><el-input v-model="participant.name" /></el-form-item>
          <el-form-item label="性别"><el-select v-model="participant.gender"><el-option label="男" value="男" /><el-option label="女" value="女" /></el-select></el-form-item>
          <el-form-item label="出生日期"><el-date-picker v-model="participant.birthDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
          <el-form-item label="电话"><el-input v-model="participant.phone" /></el-form-item>
          <el-form-item label="联系地址"><el-input v-model="participant.address" /></el-form-item>
          <el-form-item label="邮政编码"><el-input v-model="participant.zipCode" /></el-form-item>
          <el-form-item label="Email"><el-input v-model="participant.email" /></el-form-item>
          <el-form-item label="是否责任人"><el-switch v-model="participant.isContactPerson" /></el-form-item>
          <el-divider content-position="left">旅途中紧急联络（可选）</el-divider>
          <el-form-item label="联络人姓名"><el-input v-model="participant.emergencyContact" /></el-form-item>
          <el-form-item label="联络地址"><el-input v-model="participant.emergencyAddress" /></el-form-item>
          <el-form-item label="联络电话"><el-input v-model="participant.emergencyPhone" /></el-form-item>
          <el-form-item label="与本人关系"><el-input v-model="participant.relationship" /></el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="saveParticipant">保存</el-button>
        </template>
      </el-dialog>

      <!-- 已录入参加者 -->
      <div v-if="app">
        <div style="display: flex; justify-content: space-between; margin-bottom: 10px;">
          <span>参加者列表 ({{ participants.length }})</span>
          <div>
            <el-button type="primary" @click="dialogVisible = true" :disabled="app.status === 'COMPLETED'">录入参加者</el-button>
            <el-button type="success" @click="completeApp" :disabled="app.status === 'COMPLETED' || participants.length === 0">完成申请</el-button>
          </div>
        </div>
        <el-table :data="participants" stripe>
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column prop="gender" label="性别" width="60" />
          <el-table-column prop="phone" label="电话" width="130" />
          <el-table-column prop="address" label="地址" />
          <el-table-column prop="isContactPerson" label="责任人" width="80">
            <template #default="{ row }">{{ row.isContactPerson ? '是' : '否' }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="80" />
        </el-table>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { findApplication, getParticipants, addParticipant, completeApplication } from '../api'

const query = reactive({ groupCode: '', departureDate: '', contactName: '' })
const app = ref(null)
const participants = ref([])
const dialogVisible = ref(false)
const participant = reactive({
  name: '', gender: '男', birthDate: '', phone: '', address: '', zipCode: '',
  email: '', emergencyContact: '', emergencyAddress: '', emergencyPhone: '',
  relationship: '', isContactPerson: false
})

const search = async () => {
  try {
    const res = await findApplication(query)
    app.value = res.data.data
    if (app.value) {
      loadParticipants()
    } else {
      ElMessage.warning('未找到匹配的申请')
    }
  } catch (e) { ElMessage.error('查询失败') }
}

const loadParticipants = async () => {
  try {
    const res = await getParticipants(app.value.applicationId)
    participants.value = res.data.data || []
  } catch (e) { /* ignore */ }
}

const saveParticipant = async () => {
  if (!participant.name) { ElMessage.warning('请填写姓名'); return }
  try {
    await addParticipant(app.value.applicationId, { ...participant })
    ElMessage.success('参加者录入成功')
    dialogVisible.value = false
    Object.assign(participant, { name: '', gender: '男', birthDate: '', phone: '', address: '', zipCode: '', email: '', emergencyContact: '', emergencyAddress: '', emergencyPhone: '', relationship: '', isContactPerson: false })
    loadParticipants()
  } catch (e) { ElMessage.error(e.response?.data?.msg || '录入失败') }
}

const completeApp = async () => {
  try {
    await completeApplication(app.value.applicationId)
    ElMessage.success('申请已完成')
    app.value.status = 'COMPLETED'
  } catch (e) { ElMessage.error(e.response?.data?.msg || '操作失败') }
}
</script>
