<template>
  <el-header class="app-header">
    <div class="brand">AI 课程笔记助手</div>
    <el-menu
      mode="horizontal"
      :default-active="activeRoute"
      router
      class="nav-menu"
    >
      <el-menu-item index="/">
        <el-icon><Upload /></el-icon>
        <span>上传</span>
      </el-menu-item>
      <el-menu-item index="/history">
        <el-icon><Document /></el-icon>
        <span>历史</span>
      </el-menu-item>
      <el-menu-item index="/settings">
        <el-icon><Setting /></el-icon>
        <span>设置</span>
      </el-menu-item>
    </el-menu>
  </el-header>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { Upload, Document, Setting } from '@element-plus/icons-vue'

const route = useRoute()
const activeRoute = computed(() => '/' + route.path.split('/')[1])
</script>

<style scoped>
.app-header {
  display: flex;
  align-items: center;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
}
.brand {
  font-size: 18px;
  font-weight: 600;
  color: #409eff;
  margin-right: 40px;
  white-space: nowrap;
}
.nav-menu {
  flex: 1;
  border-bottom: none !important;
}
</style>
