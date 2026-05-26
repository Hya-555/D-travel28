<template>
  <div>
    <el-card shadow="never">
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>路线管理</span>
          <el-button type="primary" @click="dialogVisible = true">新建路线</el-button>
        </div>
      </template>

      <el-table :data="routes" stripe v-loading="loading">
        <el-table-column prop="routeCode" label="路线代码" width="120" />
        <el-table-column prop="routeName" label="路线名称" />
        <el-table-column prop="description" label="描述" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'info'">{{ row.status === 'ACTIVE' ? '启用' : '停用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="170" />
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button size="small" @click="editRoute(row)">变更</el-button>
            <el-button v-if="row.status === 'ACTIVE'" size="small" type="danger" @click="handleCancel(row.routeCode)">取消</el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-dialog v-model="dialogVisible" :title="editing ? '变更路线' : '新建路线'" width="500px">
        <el-form :model="form" label-width="100px">
          <el-form-item label="路线代码" required><el-input v-model="form.routeCode" :disabled="editing" /></el-form-item>
          <el-form-item label="路线名称" required><el-input v-model="form.routeName" /></el-form-item>
          <el-form-item label="描述"><el-input v-model="form.description" type="textarea" :rows="3" /></el-form-item>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import { getRoutes, createRoute, updateRoute, cancelRoute } from '../api'

const loading = ref(false)
const routes = ref([])
const dialogVisible = ref(false)
const editing = ref(false)
const form = reactive({ routeCode: '', routeName: '', description: '', employeeId: 1 })

const load = async () => {
  loading.value = true
  try { const res = await getRoutes(); routes.value = res.data.data || [] } catch (e) { /* ignore */ }
  loading.value = false
}

const editRoute = (row) => {
  editing.value = true
  form.routeCode = row.routeCode
  form.routeName = row.routeName
  form.description = row.description
  dialogVisible.value = true
}

const save = async () => {
  if (!form.routeCode || !form.routeName) { ElMessage.warning('请填写必填项'); return }
  try {
    if (editing.value) {
      await updateRoute(form.routeCode, { ...form })
      ElMessage.success('路线变更成功（历史已保留）')
    } else {
      await createRoute({ ...form })
      ElMessage.success('路线创建成功')
    }
    dialogVisible.value = false
    editing.value = false
    Object.assign(form, { routeCode: '', routeName: '', description: '', employeeId: 1 })
    load()
  } catch (e) { ElMessage.error(e.response?.data?.msg || '操作失败') }
}

const handleCancel = async (routeCode) => {
  try {
    await ElMessageBox.confirm('确认取消该路线？路线信息不会被删除，仅变更为停用状态。', '确认', { type: 'warning' })
    await cancelRoute(routeCode)
    ElMessage.success('路线已取消')
    load()
  } catch { /* cancelled */ }
}

onMounted(load)
</script>
