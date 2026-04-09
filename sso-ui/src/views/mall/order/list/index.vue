<template>
  <AppModuleShowcase
    eyebrow="订单列表"
    title="所有订单"
    description="订单列表页已经切到统一面板风格，后续只需补上真实表格和筛选逻辑。"
    :stats="stats"
    :cards="cards"
  />
</template>

<script setup lang="ts">
import AppModuleShowcase from '@/components/AppModuleShowcase.vue'

const stats = [
  { label: '展示方式', value: '表格', hint: '适合承接高密度订单数据' },
  { label: '筛选范围', value: '多维', hint: '支持状态、时间和用户维度' },
  { label: '操作模式', value: '批量', hint: '方便统一批量处理异常订单' },
]

const cards = [
  { label: '筛选', title: '多维筛选', description: '可接入状态、订单号、时间区间和支付方式。' },
  { label: '处理', title: '单据处理', description: '适合挂载发货、备注、退款和详情查看。' },
  { label: '批量', title: '批量能力', description: '方便统一处理导出、标记和风控复核。' },
  { label: '追踪', title: '链路追踪', description: '可继续接支付、履约和售后全链路状态。' },
]
</script>
