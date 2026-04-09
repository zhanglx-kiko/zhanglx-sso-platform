<template>
  <AppModuleShowcase
    eyebrow="退款服务"
    title="退款维权"
    description="用统一布局承接退款、退货和申诉处理流程，让售后页面也保持简洁有序。"
    :stats="stats"
    :cards="cards"
  />
</template>

<script setup lang="ts">
import AppModuleShowcase from '@/components/AppModuleShowcase.vue'

const stats = [
  { label: '售后场景', value: '退款', hint: '适合处理退款与退货流程' },
  { label: '协同对象', value: '服务', hint: '可联动客服与履约处理节点' },
  { label: '处理节奏', value: '队列', hint: '后续可挂载优先级和超时提醒' },
]

const cards = [
  { label: '队列', title: '待处理队列', description: '适合展示待审核、待确认和争议单据。' },
  { label: '决策', title: '处理决策', description: '可继续接入同意、驳回和补充证据流程。' },
  { label: '记录', title: '协商记录', description: '适合用时间轴承接买卖双方的处理过程。' },
  { label: '风险', title: '风险识别', description: '后续可以补充异常售后和重复退款预警。' },
]
</script>
