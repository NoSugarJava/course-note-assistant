<template>
  <div class="uploader">
    <el-upload
      ref="uploadRef"
      v-model:file-list="fileList"
      drag
      multiple
      :limit="20"
      :auto-upload="false"
      :before-upload="validateFile"
      :on-exceed="onExceed"
      accept="image/*"
      list-type="picture-card"
    >
      <el-icon class="el-icon--upload"><upload-filled /></el-icon>
      <div class="el-upload__text">
        Drag images here or <em>click to upload</em>
      </div>
      <template #tip>
        <div class="el-upload__tip">
          Supports JPG, PNG, WebP. Max 20 files, 20MB each.
        </div>
      </template>
    </el-upload>

    <div class="upload-actions">
      <el-input
        v-model="title"
        placeholder="Notes title (optional)"
        maxlength="100"
        show-word-limit
        style="width: 300px"
      />
      <el-button
        type="primary"
        :disabled="fileList.length === 0"
        :loading="uploading"
        @click="doUpload"
      >
        {{ uploading ? 'Uploading...' : 'Start Processing' }}
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import { uploadSlideSet } from '@/api/slideSet'

const emit = defineEmits(['uploaded'])

const uploadRef = ref(null)
const fileList = ref([])
const title = ref('')
const uploading = ref(false)

function validateFile(file) {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    ElMessage.error('Only image files are supported.')
    return false
  }
  const isLt20M = file.size / 1024 / 1024 < 20
  if (!isLt20M) {
    ElMessage.error('File size must be less than 20MB.')
    return false
  }
  return false // Don't auto-upload, we'll do it manually
}

function onExceed() {
  ElMessage.warning('Maximum 20 images allowed.')
}

async function doUpload() {
  if (fileList.value.length === 0) {
    ElMessage.warning('Please select at least one image.')
    return
  }
  uploading.value = true
  try {
    const rawFiles = fileList.value.map((f) => f.raw)
    const result = await uploadSlideSet(rawFiles, title.value)
    emit('uploaded', result)
  } catch (e) {
    ElMessage.error(e.userMessage || 'Upload failed')
  } finally {
    uploading.value = false
  }
}
</script>

<style scoped>
.uploader {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}
.upload-actions {
  display: flex;
  gap: 16px;
  align-items: center;
}
</style>
