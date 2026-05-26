<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>旅游团管理</span>
          <el-button type="primary" @click="dialogVisible = true">创建旅游团</el-button>
        </div>
      </template>

      <el-table :data="groups" stripe v-loading="loading">
        <el-table-column prop="groupCode" label="团代码" width="120" />
        <el-table-column prop="routeCode" label="路线代码" width="120" />
        <el-table-column prop="departureDate" label="出发日期" width="120" />
        <el-table-column prop="deadline" label="截止日期" width="120" />
        <el-table-column label="人数" width="150">
          <template #default="{ row }">{{ row.currentCount }} / {{ row.maxCapacity }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'AVAILABLE'" type="success">可报名</el-tag>
            <el-tag v-else-if="row.status === 'FULL'" type="danger">已满员</el-tag>
            <el-tag v-else-if="row.status === 'EXPIRED'" type="info">已截止</el-tag>
            <el-tag v-else>{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
      </el-table>

      <el-dialog v-model="dialogVisible" title="创建旅游团" width="500px">
        <el-form :model="form" label-width="110px">
          <el-form-item label="旅游团代码" required><el-input v-model="form.groupCode" /></el-form-item>
          <el-form-item label="所属路线代码" required><el-input v-model="form.routeCode" /></el-form-item>
          <el-form-item label="出发日期" required><el-date-picker v-model="form.departureDate" type="date" value-format="YYYY-MM-DD" /></el-form-item>
          <el-form-item label="截止日期" required><el-date-picker v-model="form.deadline" type="date" value-format="YYYY-MM-DD" /></el-form-item>
          <el-form-item label="人数上限" required><el-input-number v-model="form.maxCapacity" :min="1" /></el-form-item>
        </el-form>
        <template #footer>
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="save">保存</el-button>
        </template>
      </el-dialog>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getGroups, createGroup } from '../api'

const loading = ref(false)
const groups = ref([])
const dialogVisible = ref(false)
const form = reactive({ groupCode: '', routeCode: '', departureDate: '', deadline: '', maxCapacity: 30, employeeId: 1 })

const load = async () => {
  loading.value = true
  try { const res = await getGroups(); groups.value = res.data.data || [] } catch (e) { /* ignore */ }
  loading.value = false
}

const save = async () => {
  if (!form.groupCode || !form.routeCode || !form.departureDate || !form.deadline) {
    ElMessage.warning('请填写所有必填项'); return
  }
  try {
    await createGroup({ ...form })
    ElMessage.success('旅游团创建成功')
    dialogVisible.value = false
    Object.assign(form, { groupCode: '', routeCode: '', departureDate: '', deadline: '', maxCapacity: 30, employeeId: 1 })
    load()
  } catch (e) { ElMessage.error(e.response?.data?.msg || '操作失败') }
}

onMounted(load)
</script>
