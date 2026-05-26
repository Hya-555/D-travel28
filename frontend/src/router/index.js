import { createRouter, createWebHistory } from 'vue-router'
import MainLayout from '../layout/MainLayout.vue'

const routes = [
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '首页' } },
      { path: 'apply', name: 'Apply', component: () => import('../views/Apply.vue'), meta: { title: '旅游申请' } },
      { path: 'participants', name: 'Participants', component: () => import('../views/Participants.vue'), meta: { title: '参加者录入' } },
      { path: 'payment', name: 'Payment', component: () => import('../views/Payment.vue'), meta: { title: '收款管理' } },
      { path: 'cancel', name: 'Cancel', component: () => import('../views/Cancel.vue'), meta: { title: '取消管理' } },
      { path: 'receipt', name: 'Receipt', component: () => import('../views/Receipt.vue'), meta: { title: '收据打印' } },
      { path: 'tours', name: 'Tours', component: () => import('../views/Tours.vue'), meta: { title: '旅游团管理' } },
      { path: 'routes', name: 'Routes', component: () => import('../views/Routes.vue'), meta: { title: '路线管理' } },
      { path: 'prices', name: 'Prices', component: () => import('../views/Prices.vue'), meta: { title: '价格管理' } },
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
