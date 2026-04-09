<template>
  <AppModuleShowcase
    eyebrow="订单中心"
    title="订单中心"
    description="把订单相关入口聚合到同一块业务工作区，方便后续接入列表、退款和履约能力。"
    :stats="stats"
    :cards="cards"
  />
</template>

<script setup lang="ts">
import AppModuleShowcase from '@/components/AppModuleShowcase.vue'

const stats = [
  { label: '业务重心', value: '订单', hint: '适合承接订单主流程' },
  { label: '履约联动', value: '流程', hint: '可继续挂载发货、售后与退款' },
  { label: '统一承载', value: '完成', hint: '页面骨架已和全局保持一致' },
]

const cards = [
  { label: '总览', title: '订单总览', description: '适合汇总待付款、待发货和异常订单情况。' },
  { label: '履约', title: '履约链路', description: '后续可接发货节点、物流回传和签收状态。' },
  { label: '售后', title: '售后协同', description: '适合与退款、退货和客服处理联动呈现。' },
  { label: '分析', title: '经营分析', description: '可以进一步接入订单转化和履约效率指标。' },
]
</script>
