<template>
  <section class="panel panel--search">
    <div v-if="$slots.toolbar" class="search-toolbar">
      <div class="search-toolbar__actions">
        <slot name="toolbar" />
      </div>
    </div>

    <AuthSearchForm
      :model="model"
      :compact="compact"
      :form-class="formClass"
      :actions-item-class="actionsItemClass"
    >
      <slot />
      <template v-if="$slots.actions" #actions>
        <slot name="actions" />
      </template>
    </AuthSearchForm>
  </section>
</template>

<script setup lang="ts">
import AuthSearchForm from './AuthSearchForm.vue'

type FormClassValue = string | string[] | Record<string, boolean>

interface Props {
  model?: object
  compact?: boolean
  formClass?: FormClassValue
  actionsItemClass?: FormClassValue
}

withDefaults(defineProps<Props>(), {
  model: undefined,
  compact: false,
  formClass: '',
  actionsItemClass: '',
})
</script>

<style scoped>
.search-toolbar {
  margin-bottom: 14px;
}

.search-toolbar__actions {
  display: flex;
  flex-wrap: wrap;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .search-toolbar__actions {
    justify-content: flex-start;
  }
}
</style>
