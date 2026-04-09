<template>
  <section class="page-header" :class="{ 'page-header--compact': compact }">
    <div class="page-header__body">
      <div class="page-header__copy">
        <h1 class="page-header__title">{{ title }}</h1>
        <p v-if="description" class="page-header__description">{{ description }}</p>
      </div>

      <div v-if="$slots.actions" class="page-header__actions">
        <slot name="actions" />
      </div>
    </div>

    <div v-if="stats.length" class="page-header__stats">
      <article v-for="stat in stats" :key="stat.label" class="page-header__stat">
        <span class="page-header__stat-label">{{ stat.label }}</span>
        <strong class="page-header__stat-value">{{ stat.value }}</strong>
        <span v-if="stat.hint" class="page-header__stat-hint">{{ stat.hint }}</span>
      </article>
    </div>
  </section>
</template>

<script setup lang="ts">
interface HeaderStat {
  label: string
  value: number | string
  hint?: string
}

withDefaults(
  defineProps<{
    eyebrow?: string
    title: string
    description?: string
    stats?: HeaderStat[]
    compact?: boolean
  }>(),
  {
    eyebrow: '',
    description: '',
    stats: () => [],
    compact: false,
  },
)
</script>

<style scoped>
.page-header {
  padding: 14px 16px;
  border: 1px solid var(--app-border);
  border-radius: 16px;
  background: var(--app-surface);
  box-shadow: var(--app-shadow-soft);
}

.page-header__body {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 14px;
}

.page-header__copy {
  min-width: 0;
}

.page-header__title {
  margin: 0;
  color: var(--app-title);
  font-size: clamp(21px, 2.8vw, 28px);
  font-weight: 600;
  line-height: 1.15;
  letter-spacing: -0.04em;
}

.page-header__description {
  max-width: 640px;
  margin: 5px 0 0;
  color: var(--app-muted);
  font-size: 12px;
  line-height: 1.55;
}

.page-header__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
  flex-shrink: 0;
}

.page-header__stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(160px, 1fr));
  gap: 10px;
  margin-top: 12px;
}

.page-header__stat {
  display: flex;
  min-height: 68px;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  padding: 11px 13px;
  border: 1px solid var(--app-border);
  border-radius: 14px;
  background: var(--app-surface-muted);
}

.page-header__stat-label {
  color: var(--app-muted);
  font-size: 11px;
  letter-spacing: 0.02em;
}

.page-header__stat-value {
  color: var(--app-title);
  font-size: clamp(18px, 2.2vw, 24px);
  font-weight: 600;
  line-height: 1;
  letter-spacing: -0.05em;
}

.page-header__stat-hint {
  color: var(--app-muted);
  font-size: 10px;
  line-height: 1.45;
}

.page-header--compact {
  padding: 13px 15px;
}

.page-header--compact .page-header__title {
  font-size: clamp(20px, 2.4vw, 24px);
}

@media (max-width: 900px) {
  .page-header {
    padding: 14px 16px;
  }

  .page-header__body {
    flex-direction: column;
  }

  .page-header__actions {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .page-header {
    border-radius: 16px;
  }

  .page-header__stats {
    grid-template-columns: 1fr;
  }

  .page-header__stat {
    min-height: auto;
  }
}
</style>
