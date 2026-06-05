<template>
  <div class="settings-page">
    <h2>Settings</h2>
    <p class="desc">Configure your API keys for Baidu OCR and Alibaba Bailian.</p>
    <el-form
      v-if="config"
      :model="form"
      label-width="180px"
      class="settings-form"
      @submit.prevent="saveSettings"
    >
      <el-form-item label="Baidu OCR API Key">
        <el-input
          v-model="form['baidu.ocr.apiKey']"
          show-password
          placeholder="Enter Baidu OCR API Key"
        />
      </el-form-item>
      <el-form-item label="Baidu OCR Secret Key">
        <el-input
          v-model="form['baidu.ocr.secretKey']"
          show-password
          placeholder="Enter Baidu OCR Secret Key"
        />
      </el-form-item>
      <el-form-item label="Bailian API Key">
        <el-input
          v-model="form['bailian.apiKey']"
          show-password
          placeholder="sk-..."
        />
      </el-form-item>
      <el-form-item label="LLM Model">
        <el-select v-model="form['llm.model']" placeholder="Select model">
          <el-option label="Qwen Plus" value="qwen-plus" />
          <el-option label="Qwen Max" value="qwen-max" />
          <el-option label="Qwen Turbo" value="qwen-turbo" />
        </el-select>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" @click="saveSettings" :loading="saving">
          Save Settings
        </el-button>
      </el-form-item>
    </el-form>
    <el-skeleton v-else :rows="5" animated />
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { getConfig, updateConfig } from '@/api/config'

const config = ref(null)
const form = reactive({})
const saving = ref(false)

onMounted(async () => {
  try {
    const data = await getConfig()
    config.value = data
    Object.assign(form, data)
  } catch (e) {
    ElMessage.error(e.userMessage || 'Failed to load settings')
  }
})

async function saveSettings() {
  saving.value = true
  try {
    const updated = await updateConfig(form)
    config.value = updated
    ElMessage.success('Settings saved!')
  } catch (e) {
    ElMessage.error(e.userMessage || 'Failed to save settings')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.settings-page {
  max-width: 650px;
  margin: 0 auto;
}
.settings-page h2 {
  margin-bottom: 8px;
}
.desc {
  color: #909399;
  margin-bottom: 30px;
}
.settings-form {
  background: #fff;
  padding: 30px;
  border-radius: 8px;
}
</style>
