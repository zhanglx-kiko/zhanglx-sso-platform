<template>
  <div class="not-found">
    <section class="not-found__card">
      <span class="not-found__tag">页面不存在</span>
      <h1 class="not-found__code">404</h1>
      <h2 class="not-found__title">页面暂时找不到了</h2>
      <p class="not-found__description">
        可能是路由地址已经变更，或者当前入口还没有接入对应模块。你可以先返回首页继续操作。
      </p>
      <div class="not-found__actions">
        <el-button plain @click="router.back()">返回上一页</el-button>
        <el-button type="primary" @click="goHome">回到首页</el-button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style scoped>
.not-found {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 24px;
}

.not-found__card {
  width: min(680px, 100%);
  padding: 42px;
  border: 1px solid var(--app-border);
  border-radius: 28px;
  background: var(--app-surface);
  box-shadow: var(--app-shadow);
  text-align: center;
}

.not-found__tag {
  display: inline-flex;
  align-items: center;
  min-height: 32px;
  padding: 0 12px;
  border-radius: 999px;
  background: #eef3fd;
  color: var(--app-accent-strong);
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.not-found__code {
  margin: 18px 0 0;
  color: var(--app-title);
  font-size: clamp(92px, 15vw, 160px);
  font-weight: 600;
  line-height: 0.9;
  letter-spacing: -0.08em;
}

.not-found__title {
  margin: 18px 0 0;
  color: var(--app-title);
  font-size: 32px;
  font-weight: 600;
}

.not-found__description {
  max-width: 480px;
  margin: 16px auto 0;
  color: var(--app-muted);
  font-size: 15px;
  line-height: 1.9;
}

.not-found__actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  margin-top: 28px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .not-found {
    padding: 12px;
  }

  .not-found__card {
    padding: 26px 20px;
    border-radius: 24px;
  }

  .not-found__title {
    font-size: 26px;
  }
}
</style>
