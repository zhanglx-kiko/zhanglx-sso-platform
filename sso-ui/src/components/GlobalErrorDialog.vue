<template>
  <teleport to="body">
    <transition name="global-error-fade" appear>
      <div v-if="errorState.visible" class="global-error-overlay" @click="closeGlobalError">
        <section class="global-error-card" role="alertdialog" aria-modal="true" @click.stop>
          <button
            class="global-error-card__close"
            type="button"
            aria-label="关闭错误提示"
            @click="closeGlobalError"
          >
            <el-icon><CloseBold /></el-icon>
          </button>

          <div class="global-error-card__content">
            <span class="global-error-card__icon">
              <el-icon><CircleCloseFilled /></el-icon>
            </span>
            <p class="global-error-card__message">{{ errorState.message }}</p>
          </div>
        </section>
      </div>
    </transition>
  </teleport>
</template>

<script setup lang="ts">
import { CircleCloseFilled, CloseBold } from '@element-plus/icons-vue'
import { closeGlobalError, useGlobalErrorState } from '@/stores/globalError'

const errorState = useGlobalErrorState()
</script>

<style scoped>
.global-error-overlay {
  position: fixed;
  inset: 0;
  z-index: 4000;
  display: flex;
  align-items: flex-start;
  justify-content: center;
  padding: 76px 16px 16px;
  background: transparent;
}

.global-error-card {
  position: relative;
  width: min(360px, 100%);
  border: 1px solid rgba(217, 48, 37, 0.12);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.96);
  box-shadow:
    0 14px 32px rgba(15, 23, 42, 0.12),
    0 4px 12px rgba(217, 48, 37, 0.08);
  backdrop-filter: blur(10px);
}

.global-error-card__content {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 14px 44px 14px 14px;
}

.global-error-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  min-width: 20px;
  height: 20px;
  margin-top: 1px;
  color: #d93025;
  font-size: 18px;
}

.global-error-card__message {
  margin: 0;
  color: var(--app-text);
  font-size: 13px;
  font-weight: 500;
  line-height: 1.6;
}

.global-error-card__close {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  padding: 0;
  border: none;
  border-radius: 50%;
  background: transparent;
  color: #8b5a5a;
  cursor: pointer;
  transition:
    background-color 0.2s ease,
    color 0.2s ease,
    opacity 0.2s ease;
}

.global-error-card__close:hover {
  background: rgba(217, 48, 37, 0.08);
  color: #b3261e;
}

.global-error-fade-enter-active,
.global-error-fade-leave-active {
  transition: opacity 0.22s ease;
}

.global-error-fade-enter-active .global-error-card,
.global-error-fade-leave-active .global-error-card {
  transition:
    transform 0.22s ease,
    opacity 0.22s ease;
}

.global-error-fade-enter-from,
.global-error-fade-leave-to {
  opacity: 0;
}

.global-error-fade-enter-from .global-error-card,
.global-error-fade-leave-to .global-error-card {
  opacity: 0;
  transform: translateY(-10px);
}

@media (max-width: 768px) {
  .global-error-overlay {
    padding: 68px 12px 12px;
  }

  .global-error-card {
    width: min(100%, 340px);
    border-radius: 16px;
  }
}
</style>
