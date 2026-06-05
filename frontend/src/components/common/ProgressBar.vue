<template>
  <div class="progress-wrapper">
    <el-progress
      :percentage="percentage"
      :status="progressStatus"
      :stroke-width="16"
    />
    <p class="step-text" v-if="step">{{ step }}</p>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  percentage: { type: Number, default: 0 },
  step: { type: String, default: '' },
  status: { type: String, default: '' } // '', 'success', 'exception'
})

const progressStatus = computed(() => props.status || undefined)
</script>

<style scoped>
.progress-wrapper {
  width: 100%;
  max-width: 500px;
  margin: 0 auto;
}
.step-text {
  margin-top: 12px;
  text-align: center;
  color: #909399;
  font-size: 14px;
}
</style>
