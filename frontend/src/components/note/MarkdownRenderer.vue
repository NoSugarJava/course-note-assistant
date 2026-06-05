<template>
  <div class="markdown-body" v-html="renderedHtml"></div>
</template>

<script setup>
import { computed } from 'vue'
import { marked } from 'marked'
import hljs from 'highlight.js'
import 'highlight.js/styles/github.css'

marked.setOptions({
  highlight: function (code, lang) {
    if (lang && hljs.getLanguage(lang)) {
      return hljs.highlight(code, { language: lang }).value
    }
    return hljs.highlightAuto(code).value
  },
  breaks: true
})

const props = defineProps({
  content: { type: String, required: true }
})

const renderedHtml = computed(() => {
  if (!props.content) return ''
  return marked.parse(props.content)
})
</script>

<style>
.markdown-body {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  line-height: 1.8;
  color: #303133;
}
.markdown-body h1 {
  font-size: 28px;
  border-bottom: 2px solid #e4e7ed;
  padding-bottom: 10px;
  margin-bottom: 20px;
}
.markdown-body h2 {
  font-size: 22px;
  margin-top: 30px;
  margin-bottom: 12px;
  color: #409eff;
}
.markdown-body h3 {
  font-size: 18px;
  margin-top: 20px;
  color: #606266;
}
.markdown-body ul, .markdown-body ol {
  padding-left: 24px;
}
.markdown-body li {
  margin-bottom: 6px;
}
.markdown-body pre {
  background: #f5f7fa;
  border-radius: 6px;
  padding: 16px;
  overflow-x: auto;
}
.markdown-body code {
  font-family: 'Consolas', 'Monaco', monospace;
  font-size: 14px;
}
.markdown-body table {
  border-collapse: collapse;
  width: 100%;
  margin: 16px 0;
}
.markdown-body th, .markdown-body td {
  border: 1px solid #e4e7ed;
  padding: 8px 12px;
  text-align: left;
}
.markdown-body th {
  background: #f5f7fa;
  font-weight: 600;
}
.markdown-body blockquote {
  border-left: 4px solid #409eff;
  padding: 8px 16px;
  margin: 16px 0;
  background: #ecf5ff;
  color: #606266;
}
</style>
