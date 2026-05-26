<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" style="background: #304156">
      <div style="color: #fff; text-align: center; padding: 20px 0; font-size: 18px; font-weight: bold;">
        旅游业务管理系统
      </div>
      <el-menu
        :default-active="currentRoute"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409EFF"
      >
        <el-menu-item index="/dashboard">
          <el-icon><DataAnalysis /></el-icon>
          <span>首页概览</span>
        </el-menu-item>

        <el-sub-menu index="biz">
          <template #title>
            <el-icon><Document /></el-icon>
            <span>业务办理</span>
          </template>
          <el-menu-item index="/apply">旅游申请</el-menu-item>
          <el-menu-item index="/participants">参加者录入</el-menu-item>
          <el-menu-item index="/payment">收款管理</el-menu-item>
          <el-menu-item index="/cancel">取消管理</el-menu-item>
          <el-menu-item index="/receipt">收据打印</el-menu-item>
        </el-sub-menu>

        <el-sub-menu index="mgmt">
          <template #title>
            <el-icon><Setting /></el-icon>
            <span>基础管理</span>
          </template>
          <el-menu-item index="/routes">路线管理</el-menu-item>
          <el-menu-item index="/tours">旅游团管理</el-menu-item>
          <el-menu-item index="/prices">价格管理</el-menu-item>
        </el-sub-menu>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header style="background: #fff; border-bottom: 1px solid #e6e6e6; display: flex; align-items: center; padding: 0 20px;">
        <h3 style="margin: 0;">{{ $route.meta.title }}</h3>
      </el-header>
      <el-main style="background: #f0f2f5">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
const route = useRoute()
const currentRoute = computed(() => route.path)
</script>
