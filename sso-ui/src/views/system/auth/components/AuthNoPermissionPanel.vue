<template>
  <section class="panel panel--empty">
    <el-empty :description="description" />
  </section>
</template>

<script setup lang="ts">
withDefaults(
  defineProps<{
    description?: string
  }>(),
  {
    description: '当前账号暂无页面访问权限',
  },
)
</script>

<style scoped>
.panel--empty {
  min-height: 260px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>
