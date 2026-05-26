<template>
  <div>
    <el-card shadow="never">
      <template #header>价格管理</template>

      <!-- 选择旅游团 -->
      <el-form :inline="true" style="margin-bottom: 20px;">
        <el-form-item label="选择旅游团">
          <el-select v-model="selectedGroup" placeholder="选择旅游团" @change="loadPrices">
            <el-option v-for="g in groups" :key="g.groupCode" :label="`${g.groupCode} - ${g.routeCode}`" :value="g.groupCode" />
          </el-select>
        </el-form-item>
      </el-form>

      <!-- 价格历史 -->
      <el-table v-if="prices.length" :data="prices" stripe style="margin-bottom: 20px;">
        <el-table-column prop="adultPrice" label="大人价格(元)" />
        <el-table-column prop="childPrice" label="小孩价格(元)" />
        <el-table-column prop="discountDesc" label="优惠措施" />
        <el-table-column label="公开状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.isPublished ? 'success' : 'warning'">{{ row.isPublished ? '已公开' : '未公开' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="setTime" label="设定时间" width="170" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button v-if="!row.isPublished" size="small" type="success" @click="handlePublish(row.id)">公开</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 设定新价格 -->
      <el-card shadow="hover">
        <template #header>设定新价格</template>
        <el-form :model="form" label-width="120px" style="max-width: 400px;">
          <el-form-item label="旅游团代码" required><el-input v-model="form.groupCode" /></el-form-item>
          <el-form-item label="大人价格" required><el-input-number v-model="form.adultPrice" :min="0" :precision="2" /></el-form-item>
          <el-form-item label="小孩价格" required><el-input-number v-model="form.childPrice" :min="0" :precision="2" /></el-form-item>
          <el-form-item label="优惠措施"><el-input v-model="form.discountDesc" placeholder="如：早鸟优惠、团体折扣等" /></el-form-item>
          <el-form-item><el-button type="primary" @click="savePrice">设定价格</el-button></el-form-item>
        </el-form>
      </el-card>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAvailableGroups, getPrices, setPrice, publishPrice } from '../api'

const groups = ref([])
const selectedGroup = ref('')
const prices = ref([])
const form = reactive({ groupCode: '', adultPrice: 0, childPrice: 0, discountDesc: '', employeeId: 1 })

onMounted(async () => {
  try { const res = await getAvailableGroups(); groups.value = res.data.data || [] } catch (e) { /* ignore */ }
})

const loadPrices = async () => {
  if (!selectedGroup.value) return
  try { const res = await getPrices(selectedGroup.value); prices.value = res.data.data || [] } catch (e) { /* ignore */ }
}

const savePrice = async () => {
  if (!form.groupCode || form.adultPrice <= 0 || form.childPrice <= 0) {
    ElMessage.warning('请填写完整信息'); return
  }
  try {
    const res = await setPrice({ ...form })
    ElMessage.success('价格设定成功（尚未公开）')
    selectedGroup.value = form.groupCode
    loadPrices()
  } catch (e) { ElMessage.error(e.response?.data?.msg || '操作失败') }
}

const handlePublish = async (priceId) => {
  try {
    await publishPrice(priceId)
    ElMessage.success('价格已公开，不可再变更')
    loadPrices()
  } catch (e) { ElMessage.error(e.response?.data?.msg || '操作失败') }
}
</script>
