<template>
  <div class="upload-page">
    <ImageUploader
      v-if="!slideSetResult"
      @uploaded="onUploaded"
    />
    <UploadProgress
      v-else
      :slide-set-id="slideSetResult.id"
      :title="slideSetResult.title"
      @done="onDone"
      @error="onError"
    />
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import ImageUploader from '@/components/upload/ImageUploader.vue'
import UploadProgress from '@/components/upload/UploadProgress.vue'

const router = useRouter()
const slideSetResult = ref(null)

function onUploaded(result) {
  slideSetResult.value = result
}

function onDone(noteId) {
  router.push(`/note/${noteId}`)
}

function onError() {
  // Error is shown inside UploadProgress
}
</script>

<style scoped>
.upload-page {
  max-width: 700px;
  margin: 40px auto;
}
</style>
