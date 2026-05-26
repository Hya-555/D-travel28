<template>
  <div>
    <el-row :gutter="20">
      <el-col :span="6" v-for="card in cards" :key="card.title">
        <el-card shadow="hover" style="margin-bottom: 20px;">
          <div style="display: flex; align-items: center; justify-content: space-between;">
            <div>
              <div style="color: #909399; font-size: 14px;">{{ card.title }}</div>
              <div style="font-size: 28px; font-weight: bold; margin-top: 8px;">{{ card.value }}</div>
            </div>
            <el-icon :size="48" :color="card.color"><component :is="card.icon" /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 10px;">
      <template #header>最近申请</template>
      <el-table :data="applications" stripe v-loading="loading">
        <el-table-column prop="applicationId" label="申请编号" width="100" />
        <el-table-column prop="contactName" label="责任人" width="120" />
        <el-table-column prop="contactPhone" label="电话" width="140" />
        <el-table-column prop="groupCode" label="旅游团代码" width="120" />
        <el-table-column prop="departureDate" label="出发日期" width="120" />
        <el-table-column label="已付/总金额" width="180">
          <template #default="{ row }">
            ￥{{ row.paidAmount }} / ￥{{ row.totalAmount }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="150">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'DRAFT'" type="info">草稿</el-tag>
            <el-tag v-else-if="row.status === 'DEPOSIT_PAID'" type="warning">已付订金</el-tag>
            <el-tag v-else-if="row.status === 'COMPLETED'" type="success">已完成</el-tag>
            <el-tag v-else-if="row.status === 'CANCELLED'" type="danger">已取消</el-tag>
            <el-tag v-else>{{ row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getApplications } from '../api'

const applications = ref([])
const loading = ref(false)
const cards = ref([
  { title: '总申请数', value: 0, icon: 'Document', color: '#409EFF' },
  { title: '进行中', value: 0, icon: 'Clock', color: '#E6A23C' },
  { title: '已完成', value: 0, icon: 'CircleCheck', color: '#67C23A' },
  { title: '已取消', value: 0, icon: 'CircleClose', color: '#F56C6C' },
])

onMounted(async () => {
  loading.value = true
  try {
    const res = await getApplications()
    applications.value = res.data.data || []
    cards.value[0].value = applications.value.length
    cards.value[1].value = applications.value.filter(a => ['DRAFT', 'DEPOSIT_PAID'].includes(a.status)).length
    cards.value[2].value = applications.value.filter(a => a.status === 'COMPLETED').length
    cards.value[3].value = applications.value.filter(a => a.status === 'CANCELLED').length
  } catch (e) {
    // ignore - no data yet
  }
  loading.value = false
})
</script>
