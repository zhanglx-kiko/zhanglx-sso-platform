<template>
  <AppModuleShowcase
    eyebrow="商品工作区"
    title="商品中心"
    description="把商品相关能力收敛到同一个业务入口，后续扩展类目、库存和上下架流程会更自然。"
    :stats="stats"
    :cards="cards"
    note="当前先以统一页面骨架承接视觉风格，后续只需把真实业务组件接入这套容器。"
  />
</template>

<script setup lang="ts">
import AppModuleShowcase from '@/components/AppModuleShowcase.vue'

const stats = [
  { label: '模块状态', value: '已就绪', hint: '已切换到统一视觉风格' },
  { label: '规划方向', value: '商品域', hint: '适合承接商品概览与入口聚合' },
  { label: '后续重点', value: 'SKU', hint: '可继续接库存与上下架能力' },
]

const cards = [
  { label: '总览', title: '商品总览', description: '适合展示商品数量、在售状态与最近变更。' },
  { label: '分类', title: '类目层级', description: '可以继续把分类树和检索区接到同一页内。' },
  { label: '流程', title: '生命周期', description: '上架、下架、审核等节点都适合沉淀为统一流程。' },
  { label: '分析', title: '经营视角', description: '后续可接商品动销、库存预警和转化数据。' },
]
</script>
