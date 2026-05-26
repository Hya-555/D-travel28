import axios from 'axios'

const api = axios.create({
  baseURL: '/api',
  timeout: 10000
})

// ====== 申请相关 ======
export const checkAvailable = (groupCode) => api.get(`/application/check/${groupCode}`)
export const apply = (data) => api.post('/application/apply', data)
export const payDeposit = (applicationId, employeeId) => api.post(`/application/deposit/${applicationId}?employeeId=${employeeId || 1}`)
export const addParticipant = (applicationId, data) => api.post(`/application/${applicationId}/participant`, data)
export const completeApplication = (applicationId) => api.post(`/application/${applicationId}/complete`)
export const getApplications = () => api.get('/application/list')
export const getApplicationDetail = (id) => api.get(`/application/${id}`)
export const findApplication = (params) => api.get('/application/find', { params })
export const getParticipants = (applicationId) => api.get(`/application/${applicationId}/participants`)

// ====== 支付相关 ======
export const payBalance = (data) => api.post('/payment/balance', data)
export const getPayments = (applicationId) => api.get(`/payment/list/${applicationId}`)

// ====== 取消相关 ======
export const cancelApplication = (data) => api.post('/cancel', data)

// ====== 收据相关 ======
export const printDepositReceipt = (applicationId, employeeId) => api.post(`/receipt/deposit/${applicationId}?employeeId=${employeeId || 1}`)
export const printDaily = (employeeId) => api.post(`/receipt/daily?employeeId=${employeeId || 1}`)

// ====== 路线相关 ======
export const createRoute = (data) => api.post('/tour/route', data)
export const updateRoute = (routeCode, data) => api.put(`/tour/route/${routeCode}`, data)
export const cancelRoute = (routeCode) => api.post(`/tour/route/${routeCode}/cancel`)
export const getRoutes = () => api.get('/tour/routes')

// ====== 旅游团相关 ======
export const createGroup = (data) => api.post('/tour/group', data)
export const getGroups = () => api.get('/tour/groups')
export const getAvailableGroups = () => api.get('/tour/groups/available')

// ====== 价格相关 ======
export const setPrice = (data) => api.post('/tour/price', data)
export const publishPrice = (priceId) => api.post(`/tour/price/${priceId}/publish`)
export const getPrices = (groupCode) => api.get(`/tour/prices/${groupCode}`)

// ====== 财务相关 ======
export const triggerExport = () => api.post('/financial/export')

export default api
