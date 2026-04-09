<template>
  <section class="panel panel--search">
    <div class="panel-header">
      <div>
        <h2 class="panel-title">{{ title }}</h2>
        <p class="panel-subtitle">{{ description }}</p>
      </div>
      <slot name="header-extra" />
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
  title: string
  description: string
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
