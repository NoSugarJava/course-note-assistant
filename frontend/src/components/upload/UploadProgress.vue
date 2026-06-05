<template>
  <div class="progress-container">
    <h3>{{ title }}</h3>
    <ProgressBar
      :percentage="progress"
      :step="step"
      :status="errorMessage ? 'exception' : ''"
    />
    <el-alert
      v-if="errorMessage"
      :title="'Processing failed'"
      :description="errorMessage"
      type="error"
      show-icon
      :closable="false"
    />
    <div v-if="progress === 100" class="success-tip">
      <el-icon color="#67c23a" :size="24"><CircleCheckFilled /></el-icon>
      <span>Notes generated successfully! Redirecting...</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import { createProgressStream } from '@/utils/sse'
import ProgressBar from '@/components/common/ProgressBar.vue'

const props = defineProps({
  slideSetId: { type: Number, required: true },
  title: { type: String, default: '' }
})

const emit = defineEmits(['done', 'error'])

const progress = ref(0)
const step = ref('Initializing...')
const errorMessage = ref('')
let eventSource = null

onMounted(() => {
  const url = `/api/slidesets/${props.slideSetId}/progress`
  eventSource = createProgressStream(url, {
    onProgress: (data) => {
      progress.value = data.progress
      step.value = data.step
      if (data.status === 'FAILED') {
        errorMessage.value = data.errorMessage || 'Processing failed'
        emit('error', errorMessage.value)
      }
    },
    onComplete: (noteId) => {
      progress.value = 100
      setTimeout(() => emit('done', noteId), 1500)
    },
    onError: (msg) => {
      errorMessage.value = msg
      emit('error', msg)
    }
  })
})

onUnmounted(() => {
  if (eventSource) {
    eventSource.close()
  }
})
</script>

<style scoped>
.progress-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 40px;
}
.progress-container h3 {
  margin: 0;
  font-size: 18px;
}
.success-tip {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #67c23a;
  font-size: 15px;
}
</style>
