<template>
  <span class="copyable-text">
    <el-tooltip :content="text || emptyText" placement="top">
      <span class="copyable-text__value" :class="{ 'copyable-text__value--mono': monospace }">
        {{ text || emptyText }}
      </span>
    </el-tooltip>
    <el-button
      v-if="text && showCopy"
      link
      type="primary"
      class="copyable-text__button"
      @click="handleCopy"
    >
      复制
    </el-button>
  </span>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus'
import { copyText } from '@/utils/clipboard'

const props = withDefaults(
  defineProps<{
    text?: string | null
    emptyText?: string
    monospace?: boolean
    showCopy?: boolean
  }>(),
  {
    text: '',
    emptyText: '--',
    monospace: false,
    showCopy: true,
  },
)

const handleCopy = async () => {
  if (!props.text) return

  try {
    await copyText(props.text)
    ElMessage.success('复制成功')
  } catch {
    ElMessage.warning('复制失败，请手动复制')
  }
}
</script>

<style scoped>
.copyable-text {
  display: inline-flex;
  min-width: 0;
  align-items: center;
  gap: 8px;
}

.copyable-text__value {
  min-width: 0;
  overflow: hidden;
  color: var(--app-text);
  text-overflow: ellipsis;
  white-space: nowrap;
}

.copyable-text__value--mono {
  font-family: 'JetBrains Mono', 'SFMono-Regular', 'Cascadia Mono', 'Consolas', monospace;
  font-size: 12px;
}

.copyable-text__button {
  flex-shrink: 0;
}
</style>
