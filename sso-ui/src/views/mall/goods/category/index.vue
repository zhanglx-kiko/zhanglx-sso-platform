<template>
  <AppModuleShowcase
    eyebrow="分类树"
    title="商品分类"
    description="用统一的容器与信息层次承载分类树、筛选面板和类目属性配置。"
    :stats="stats"
    :cards="cards"
  />
</template>

<script setup lang="ts">
import AppModuleShowcase from '@/components/AppModuleShowcase.vue'

const stats = [
  { label: '层级组织', value: '树形', hint: '适合维护多层分类结构' },
  { label: '属性扩展', value: '规格', hint: '类目可继续挂载属性模板' },
  { label: '统一风格', value: '完成', hint: '已经切到全局设计语言' },
]

const cards = [
  { label: '层级', title: '分类树', description: '适合展示父子级结构与拖拽排序能力。' },
  { label: '属性', title: '属性模板', description: '可继续接入品牌、规格和展示属性配置。' },
  { label: '展示', title: '前台展示', description: '适合关联导航曝光、搜索权重和推荐配置。' },
  { label: '记录', title: '维护记录', description: '可以补充分类调整的操作记录与责任留痕。' },
]
</script>
