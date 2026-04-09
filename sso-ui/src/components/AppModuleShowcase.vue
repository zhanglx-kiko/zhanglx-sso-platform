<template>
  <div class="page-shell">
    <AppPageHeader
      :eyebrow="eyebrow"
      :title="title"
      :description="description"
      :stats="stats"
      compact
    />

    <section class="panel module-showcase">
      <div class="module-showcase__layout">
        <div class="module-showcase__copy">
          <h2 class="module-showcase__title">{{ panelTitle }}</h2>
          <p class="module-showcase__description">{{ panelDescription }}</p>
          <p v-if="note" class="module-showcase__note">{{ note }}</p>
        </div>

        <div class="module-showcase__cards">
          <article v-for="card in cards" :key="card.title" class="module-showcase__card">
            <span class="module-showcase__card-label">{{ card.label }}</span>
            <h3 class="module-showcase__card-title">{{ card.title }}</h3>
            <p class="module-showcase__card-description">{{ card.description }}</p>
          </article>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import AppPageHeader from '@/components/AppPageHeader.vue'

interface ShowcaseStat {
  label: string
  value: string | number
  hint?: string
}

interface ShowcaseCard {
  label: string
  title: string
  description: string
}

withDefaults(
  defineProps<{
    eyebrow: string
    title: string
    description: string
    panelTitle?: string
    panelDescription?: string
    note?: string
    stats?: ShowcaseStat[]
    cards?: ShowcaseCard[]
  }>(),
  {
    panelTitle: '这个模块已经切换到新的统一页面骨架',
    panelDescription: '后续只需要把真实业务组件接入当前面板结构，就能自然延续整个系统的视觉语言。',
    note: '',
    stats: () => [],
    cards: () => [],
  },
)
</script>

<style scoped>
.module-showcase__layout {
  display: grid;
  grid-template-columns: minmax(0, 0.95fr) minmax(0, 1.05fr);
  gap: 12px;
  align-items: stretch;
}

.module-showcase__copy {
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 2px 0;
}

.module-showcase__title {
  margin: 0;
  color: var(--app-title);
  font-size: 21px;
  font-weight: 600;
  line-height: 1.24;
  letter-spacing: -0.04em;
}

.module-showcase__description,
.module-showcase__note {
  margin: 8px 0 0;
  color: var(--app-muted);
  font-size: 12px;
  line-height: 1.6;
}

.module-showcase__note {
  color: var(--app-muted);
}

.module-showcase__cards {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.module-showcase__card {
  display: flex;
  min-height: 118px;
  flex-direction: column;
  gap: 8px;
  padding: 14px;
  border: 1px solid var(--app-border);
  border-radius: 14px;
  background: var(--app-surface-muted);
}

.module-showcase__card-label {
  color: var(--app-muted);
  font-size: 11px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.module-showcase__card-title {
  margin: 0;
  color: var(--app-title);
  font-size: 15px;
  font-weight: 600;
}

.module-showcase__card-description {
  margin: 0;
  color: var(--app-muted);
  font-size: 12px;
  line-height: 1.55;
}

@media (max-width: 1024px) {
  .module-showcase__layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .module-showcase__cards {
    grid-template-columns: 1fr;
  }

  .module-showcase__card {
    min-height: auto;
  }
}
</style>
