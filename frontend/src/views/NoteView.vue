<template>
  <div class="note-page">
    <div v-if="store.loading" class="loading">
      <el-skeleton :rows="10" animated />
    </div>
    <div v-else-if="store.error" class="error">
      <el-result icon="error" :title="store.error">
        <template #extra>
          <el-button type="primary" @click="$router.push('/history')">
            Back to History
          </el-button>
        </template>
      </el-result>
    </div>
    <template v-else-if="store.currentNote">
      <NoteToolbar
        :note-id="store.currentNote.id"
        :content="store.currentNote.markdownContent"
      />
      <MarkdownRenderer :content="store.currentNote.markdownContent" />
    </template>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue'
import { useRoute } from 'vue-router'
import { useNoteStore } from '@/stores/note'
import NoteToolbar from '@/components/note/NoteToolbar.vue'
import MarkdownRenderer from '@/components/note/MarkdownRenderer.vue'

const route = useRoute()
const store = useNoteStore()

onMounted(() => {
  store.fetchNote(Number(route.params.id))
})

onUnmounted(() => {
  store.clearCurrentNote()
})
</script>

<style scoped>
.note-page {
  max-width: 900px;
  margin: 0 auto;
}
.loading {
  padding: 40px;
}
</style>
