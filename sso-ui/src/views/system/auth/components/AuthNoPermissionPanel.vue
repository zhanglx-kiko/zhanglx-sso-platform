<template>
  <section v-if="showPanel" class="panel panel--empty">
    <el-empty :description="description" />
  </section>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useUserStore } from '@/stores/user'

withDefaults(
  defineProps<{
    description?: string
  }>(),
  {
    description: '当前账号暂无页面访问权限',
  },
)

const userStore = useUserStore()
const showPanel = computed(() => userStore.hasSession())
</script>

<style scoped>
.panel--empty {
  min-height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
