<template>
  <div class="toolbar">
    <el-button @click="goBack">
      <el-icon><ArrowLeft /></el-icon>
      Back
    </el-button>
    <div class="toolbar-actions">
      <el-button @click="copyNote">
        <el-icon><CopyDocument /></el-icon>
        Copy
      </el-button>
      <el-button type="primary" @click="downloadNote">
        <el-icon><Download /></el-icon>
        Download
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'
import { ArrowLeft, CopyDocument, Download } from '@element-plus/icons-vue'
import { downloadNoteUrl } from '@/api/note'

const props = defineProps({
  noteId: { type: Number, required: true },
  content: { type: String, required: true }
})

const router = useRouter()

function goBack() {
  router.push('/history')
}

async function copyNote() {
  try {
    await navigator.clipboard.writeText(props.content)
    ElMessage.success('Copied to clipboard!')
  } catch {
    ElMessage.error('Failed to copy. Please try again.')
  }
}

function downloadNote() {
  window.open(downloadNoteUrl(props.noteId), '_blank')
}
</script>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  margin-bottom: 16px;
}
.toolbar-actions {
  display: flex;
  gap: 8px;
}
</style>
