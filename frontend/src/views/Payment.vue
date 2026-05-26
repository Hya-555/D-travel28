<template>
  <div>
    <el-card shadow="never">
      <template #header>余款收款管理</template>

      <!-- 查询 -->
      <el-form :inline="true" :model="query" style="margin-bottom: 20px;">
        <el-form-item label="旅游团代码"><el-input v-model="query.groupCode" /></el-form-item>
        <el-form-item label="出发日期"><el-date-picker v-model="query.departureDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
        <el-form-item label="责任人"><el-input v-model="query.contactName" /></el-form-item>
        <el-form-item><el-button type="primary" @click="search">查询</el-button></el-form-item>
      </el-form>

      <!-- 申请详情 -->
      <el-descriptions v-if="app" :column="2" border style="margin-bottom: 20px;">
        <el-descriptions-item label="申请编号">{{ app.applicationId }}</el-descriptions-item>
        <el-descriptions-item label="总金额">￥{{ app.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="已付金额">￥{{ app.paidAmount }}</el-descriptions-item>
        <el-descriptions-item label="待付余款">￥{{ (app.totalAmount - app.paidAmount).toFixed(2) }}</el-descriptions-item>
      </el-descriptions>

      <!-- 支付记录 -->
      <el-table v-if="app" :data="payments" stripe style="margin-bottom: 20px;">
        <el-table-column prop="paymentNo" label="交款单编号" />
        <el-table-column prop="paymentType" label="类型">
          <template #default="{ row }">{{ row.paymentType === 'DEPOSIT' ? '订金' : '余款' }}</template>
        </el-table-column>
        <el-table-column prop="amount" label="金额(元)" />
        <el-table-column prop="payTime" label="支付时间" />
      </el-table>

      <!-- 支付余款 -->
      <div v-if="app && app.paidAmount < app.totalAmount" style="margin-top: 20px;">
        <el-form :inline="true">
          <el-form-item label="交款单编号"><el-input v-model="paymentNo" placeholder="输入交款单编号" /></el-form-item>
          <el-form-item><el-button type="success" @click="confirmPay">确认收款</el-button></el-form-item>
        </el-form>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { findApplication, getPayments, payBalance } from '../api'

const query = reactive({ groupCode: '', departureDate: '', contactName: '' })
const app = ref(null)
const payments = ref([])
const paymentNo = ref('')

const search = async () => {
  try {
    const res = await findApplication(query)
    app.value = res.data.data
    if (app.value) {
      const pRes = await getPayments(app.value.applicationId)
      payments.value = pRes.data.data || []
    } else {
      ElMessage.warning('未找到匹配的申请')
    }
  } catch (e) { ElMessage.error('查询失败') }
}

const confirmPay = async () => {
  if (!paymentNo.value) { ElMessage.warning('请输入交款单编号'); return }
  try {
    await payBalance({ applicationId: app.value.applicationId, paymentNo: paymentNo.value, employeeId: 1 })
    ElMessage.success('余款支付完成')
    app.value.paidAmount = app.value.totalAmount
    paymentNo.value = ''
    search()
  } catch (e) { ElMessage.error(e.response?.data?.msg || '支付失败') }
}
</script>
