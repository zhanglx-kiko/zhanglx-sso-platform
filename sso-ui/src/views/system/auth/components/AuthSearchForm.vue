<template>
  <el-form :inline="true" :model="model" :class="formClasses">
    <slot />
    <el-form-item v-if="$slots.actions" :class="actionsItemClasses">
      <div class="filters-actions">
        <slot name="actions" />
      </div>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type FormClassValue = string | string[] | Record<string, boolean>

interface Props {
  model?: object
  compact?: boolean
  formClass?: FormClassValue
  actionsItemClass?: FormClassValue
}

const props = withDefaults(defineProps<Props>(), {
  model: undefined,
  compact: false,
  formClass: '',
  actionsItemClass: '',
})

const formClasses = computed(() => [
  'filters-form',
  props.compact && 'filters-form--compact',
  props.formClass,
])

const actionsItemClasses = computed(() => [
  'filters-form__actions',
  props.actionsItemClass,
])
</script>
