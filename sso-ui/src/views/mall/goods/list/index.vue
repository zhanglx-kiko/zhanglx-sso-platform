<template>
  <AppModuleShowcase
    eyebrow="商品列表"
    title="商品列表"
    description="统一承载筛选、表格、批量操作和状态切换，让商品运营页保持一致体验。"
    :stats="stats"
    :cards="cards"
  />
</template>

<script setup lang="ts">
import AppModuleShowcase from '@/components/AppModuleShowcase.vue'

const stats = [
  { label: '列表模式', value: '表格', hint: '适合承载批量管理场景' },
  { label: '运营动作', value: '批量', hint: '支持上下架、推荐与分组操作' },
  { label: '信息密度', value: '灵活', hint: '后续可扩展更多字段与筛选器' },
]

const cards = [
  { label: '检索', title: '筛选检索', description: '适合接商品名、编码、状态和类目等条件。' },
  { label: '操作', title: '批量操作', description: '可以收敛成统一工具条，减少页面噪音。' },
  { label: '状态', title: '状态切换', description: '适合结合上架、草稿和审核流统一呈现。' },
  { label: '详情', title: '详情抽屉', description: '后续可以侧边抽屉承接商品详情与快速编辑。' },
]
</script>
