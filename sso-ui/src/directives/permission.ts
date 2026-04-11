import type { App, DirectiveBinding } from 'vue'
import { usePermissionStore } from '@/stores/permission'

type PermissionBinding =
  | string
  | string[]
  | {
      all?: string[]
      any?: string[]
    }

const isAllowed = (binding: PermissionBinding): boolean => {
  const permissionStore = usePermissionStore()

  if (typeof binding === 'string') {
    return permissionStore.hasPermission(binding)
  }

  if (Array.isArray(binding)) {
    return permissionStore.hasAnyPermission(binding)
  }

  if (binding && typeof binding === 'object') {
    const allMatched = binding.all ? binding.all.every((item) => permissionStore.hasPermission(item)) : true
    const anyMatched = binding.any ? permissionStore.hasAnyPermission(binding.any) : true
    return allMatched && anyMatched
  }

  return true
}

const updateVisibility = (el: HTMLElement, binding: DirectiveBinding<PermissionBinding>) => {
  const allowed = isAllowed(binding.value)
  el.style.display = allowed ? '' : 'none'
}

export const setupPermissionDirective = (app: App) => {
  app.directive('permission', {
    mounted(el, binding) {
      updateVisibility(el as HTMLElement, binding)
    },
    updated(el, binding) {
      updateVisibility(el as HTMLElement, binding)
    },
  })
}
