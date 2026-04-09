<template>
  <section class="log-text-block">
    <header class="log-text-block__header">
      <strong class="log-text-block__title">{{ title }}</strong>
      <el-button v-if="resolvedText" link type="primary" @click="handleCopy">复制</el-button>
    </header>
    <pre class="log-text-block__body">{{ resolvedText || emptyText }}</pre>
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { copyText } from '@/utils/clipboard'

const props = withDefaults(
  defineProps<{
    title: string
    text?: string | null
    emptyText?: string
  }>(),
  {
    text: '',
    emptyText: '暂无内容',
  },
)

const resolvedText = computed(() => props.text?.trim() || '')

const handleCopy = async () => {
  if (!resolvedText.value) return

  try {
    await copyText(resolvedText.value)
    ElMessage.success('复制成功')
  } catch {
    ElMessage.warning('复制失败，请手动复制')
  }
}
</script>

<style scoped>
.log-text-block {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px;
  border: 1px solid var(--app-border);
  border-radius: 18px;
  background: var(--app-surface-muted);
}

.log-text-block__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.log-text-block__title {
  color: var(--app-title);
  font-size: 13px;
}

.log-text-block__body {
  overflow: auto;
  max-height: 240px;
  margin: 0;
  padding: 12px 14px;
  border-radius: 14px;
  background: #0f1720;
  color: #dbe7f5;
  font-family: 'JetBrains Mono', 'SFMono-Regular', 'Cascadia Mono', 'Consolas', monospace;
  font-size: 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
