<template>
  <div class="history-page">
    <h2>Notes History</h2>
    <el-table
      v-if="store.notes.length > 0"
      :data="store.notes"
      stripe
      @row-click="goToNote"
      style="cursor: pointer"
      v-loading="store.loading"
    >
      <el-table-column prop="id" label="#" width="60" />
      <el-table-column prop="title" label="Title" min-width="200" />
      <el-table-column prop="imageCount" label="Images" width="100" />
      <el-table-column label="Created" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="Actions" width="120" @click.stop>
        <template #default="{ row }">
          <el-popconfirm
            title="Delete this note?"
            @confirm="handleDelete(row.id)"
          >
            <template #reference>
              <el-button type="danger" size="small" text>
                Delete
              </el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>
    <el-empty v-else-if="!store.loading" description="No notes yet">
      <el-button type="primary" @click="$router.push('/')">
        Create your first note
      </el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useNoteStore } from '@/stores/note'

const router = useRouter()
const store = useNoteStore()

onMounted(() => {
  store.fetchNotes()
})

function goToNote(row) {
  router.push(`/note/${row.id}`)
}

async function handleDelete(id) {
  try {
    await store.removeNote(id)
    ElMessage.success('Note deleted')
  } catch {
    ElMessage.error('Failed to delete note')
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleString('zh-CN')
}
</script>

<style scoped>
.history-page {
  max-width: 900px;
  margin: 0 auto;
}
.history-page h2 {
  margin-bottom: 20px;
}
</style>
