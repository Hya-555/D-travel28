<template>
  <div>
    <el-card shadow="never">
      <template #header>旅游申请办理</template>

      <!-- 步骤条 -->
      <el-steps :active="step" finish-status="success" style="margin-bottom: 30px;">
        <el-step title="查询旅游团" />
        <el-step title="录入申请信息" />
        <el-step title="支付订金" />
        <el-step title="打印收据" />
      </el-steps>

      <!-- 步骤1: 查询旅游团 -->
      <div v-if="step === 0">
        <el-table :data="groups" stripe v-loading="loading" @row-click="selectGroup" highlight-current-row>
          <el-table-column prop="groupCode" label="旅游团代码" width="140" />
          <el-table-column prop="routeCode" label="路线代码" width="120" />
          <el-table-column prop="departureDate" label="出发日期" width="120" />
          <el-table-column prop="deadline" label="截止日期" width="120" />
          <el-table-column label="人数" width="150">
            <template #default="{ row }">
              {{ row.currentCount }} / {{ row.maxCapacity }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态">
            <template #default="{ row }">
              <el-tag :type="row.status === 'AVAILABLE' ? 'success' : 'info'">{{ row.status }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 步骤2: 录入申请信息 -->
      <div v-if="step === 1">
        <el-alert title="已选旅游团" :description="`${selectedGroup.groupCode} - 出发日期: ${selectedGroup.departureDate}`" type="success" show-icon style="margin-bottom: 20px;" />
        <el-form :model="form" label-width="120px" style="max-width: 500px;">
          <el-form-item label="责任人姓名" required>
            <el-input v-model="form.contactName" placeholder="申请责任人姓名" />
          </el-form-item>
          <el-form-item label="责任人电话" required>
            <el-input v-model="form.contactPhone" placeholder="电话号码" />
          </el-form-item>
          <el-form-item label="大人人数" required>
            <el-input-number v-model="form.adultCount" :min="0" />
          </el-form-item>
          <el-form-item label="小孩人数" required>
            <el-input-number v-model="form.childCount" :min="0" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitApply">提交申请</el-button>
            <el-button @click="step = 0">返回</el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤3: 支付订金 -->
      <div v-if="step === 2">
        <el-descriptions :column="2" border style="margin-bottom: 20px;">
          <el-descriptions-item label="申请编号">{{ currentApp.applicationId }}</el-descriptions-item>
          <el-descriptions-item label="责任人">{{ currentApp.contactName }}</el-descriptions-item>
          <el-descriptions-item label="大人/小孩">{{ currentApp.adultCount }} / {{ currentApp.childCount }}</el-descriptions-item>
          <el-descriptions-item label="订金金额">￥{{ currentApp.depositAmount }}</el-descriptions-item>
        </el-descriptions>
        <el-button type="primary" @click="confirmDeposit">确认支付订金</el-button>
      </div>

      <!-- 步骤4: 打印收据 -->
      <div v-if="step === 3">
        <el-result icon="success" title="订金支付完成" sub-title="请打印收据和旅游申请书交给顾客">
          <template #extra>
            <el-button type="primary" @click="printReceipt">打印收据</el-button>
            <el-button @click="resetForm">办理新申请</el-button>
          </template>
        </el-result>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAvailableGroups, apply, payDeposit, printDepositReceipt } from '../api'

const step = ref(0)
const loading = ref(false)
const groups = ref([])
const selectedGroup = ref({})
const form = ref({ contactName: '', contactPhone: '', adultCount: 1, childCount: 0, employeeId: 1 })
const currentApp = ref({})

onMounted(async () => {
  loading.value = true
  try {
    const res = await getAvailableGroups()
    groups.value = res.data.data || []
  } catch (e) { /* ignore */ }
  loading.value = false
})

const selectGroup = (row) => {
  if (row.status !== 'AVAILABLE') return
  selectedGroup.value = row
  step.value = 1
}

const submitApply = async () => {
  if (!form.value.contactName || !form.value.contactPhone) {
    ElMessage.warning('请填写责任人信息')
    return
  }
  try {
    const res = await apply({ ...form.value, groupCode: selectedGroup.value.groupCode, employeeId: 1 })
    currentApp.value = res.data.data
    step.value = 2
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '申请失败')
  }
}

const confirmDeposit = async () => {
  try {
    await payDeposit(currentApp.value.applicationId, 1)
    step.value = 3
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '支付失败')
  }
}

const printReceipt = async () => {
  try {
    await printDepositReceipt(currentApp.value.applicationId, 1)
    ElMessage.success('收据打印成功')
  } catch (e) {
    ElMessage.error(e.response?.data?.msg || '打印失败')
  }
}

const resetForm = () => {
  form.value = { contactName: '', contactPhone: '', adultCount: 1, childCount: 0, employeeId: 1 }
  currentApp.value = {}
  step.value = 0
}

</script>
