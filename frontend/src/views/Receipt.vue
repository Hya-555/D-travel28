<template>
  <div>
    <el-card shadow="never">
      <template #header>收据打印与每日催款</template>

      <!-- 单个打印订金收据 -->
      <el-card shadow="hover" style="margin-bottom: 20px;">
        <template #header>打印订金收据</template>
        <el-form :inline="true">
          <el-form-item label="申请编号"><el-input-number v-model="singleAppId" :min="1" /></el-form-item>
          <el-form-item><el-button type="primary" @click="printSingle">打印订金收据</el-button></el-form-item>
        </el-form>
      </el-card>

      <!-- 每日批量打印 -->
      <el-card shadow="hover">
        <template #header>每日批量打印（前一天已完成的申请）</template>
        <p style="color: #909399; margin-bottom: 16px;">
          系统将打印旅游确认书。全款已付的只打印确认书；未付余款的打印确认书 + 余额交款单。
        </p>
        <el-button type="primary" @click="printDailyDocs">执行每日打印</el-button>

        <el-divider />

        <el-table :data="history" stripe v-if="history.length">
          <el-table-column prop="receiptNo" label="编号" />
          <el-table-column label="类型">
            <template #default="{ row }">
              <el-tag v-if="row.receiptType === 'DEPOSIT_RECEIPT'" type="info">订金收据</el-tag>
              <el-tag v-else-if="row.receiptType === 'CONFIRMATION'" type="success">旅游确认书</el-tag>
              <el-tag v-else-if="row.receiptType === 'PAYMENT_SLIP'" type="warning">余额交款单</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="printTime" label="打印时间" />
        </el-table>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { printDepositReceipt, printDaily, getPrintFormData } from '../api'
import { generateTravelApplicationForm } from '../utils/printTemplates'

const singleAppId = ref(null)
const history = ref([])

const printSingle = async () => {
  if (!singleAppId.value) { ElMessage.warning('请输入申请编号'); return }
  try {
    // 1. 获取打印所需完整数据
    const formRes = await getPrintFormData(singleAppId.value)
    const formData = formRes.data.data

    // 2. 生成旅游申请书 HTML 并打开打印窗口
    const html = generateTravelApplicationForm(formData)
    const printWindow = window.open('', '_blank', 'width=800,height=600')
    if (printWindow) {
      printWindow.document.write(html)
      printWindow.document.close()
    } else {
      ElMessage.warning('弹窗被浏览器拦截，请允许弹窗后重试')
    }

    // 3. 记录打印到数据库（保留原有行为）
    const res = await printDepositReceipt(singleAppId.value, 1)
    history.value.unshift(res.data.data)
    ElMessage.success('订金收据打印成功')
  } catch (e) { ElMessage.error(e.response?.data?.msg || '打印失败') }
}

const printDailyDocs = async () => {
  try {
    const res = await printDaily(1)
    const docs = res.data.data || []
    history.value = [...docs, ...history.value]
    const confirmCnt = docs.filter(d => d.receiptType === 'CONFIRMATION').length
    const slipCnt = docs.filter(d => d.receiptType === 'PAYMENT_SLIP').length
    ElMessage.success(`打印完成：确认书 ${confirmCnt} 份，交款单 ${slipCnt} 份`)
  } catch (e) { ElMessage.error(e.response?.data?.msg || '打印失败') }
}
</script>
