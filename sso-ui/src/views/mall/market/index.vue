<template>
  <AppModuleShowcase
    eyebrow="营销中心"
    title="营销中心"
    description="统一承接活动、优惠券和投放策略，让营销模块也延续这套简洁大方的后台风格。"
    :stats="stats"
    :cards="cards"
  />
</template>

<script setup lang="ts">
import AppModuleShowcase from '@/components/AppModuleShowcase.vue'

const stats = [
  { label: '活动引擎', value: '活动', hint: '适合挂载满减、秒杀和专题活动' },
  { label: '触达方式', value: '优惠券', hint: '可继续扩展券包和推送策略' },
  { label: '运营复盘', value: '分析', hint: '适合整合投放与转化数据' },
]

const cards = [
  { label: '活动', title: '活动编排', description: '后续可以展示活动列表、状态和时间轴信息。' },
  { label: '优惠', title: '优惠体系', description: '适合统一维护优惠券模板、领取规则和库存。' },
  { label: '人群', title: '人群策略', description: '可结合用户分层和标签做定向投放。' },
  { label: '复盘', title: '投放复盘', description: '方便后续接入活动成效、投入产出和转化漏斗。' },
]
</script>
