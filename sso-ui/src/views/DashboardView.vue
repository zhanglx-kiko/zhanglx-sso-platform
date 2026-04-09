<template>
  <div class="page-shell">
    <AppPageHeader
      eyebrow="数据总览"
      title="仪表盘"
      description="从统一入口查看账号体系、权限结构与当前工作台的运行状态。"
      :stats="overviewStats"
    >
      <template #actions>
        <el-button plain @click="router.push('/system/auth/user')">查看管理员</el-button>
        <el-button type="primary" @click="router.push('/system/auth/permission')">
          进入权限中心
        </el-button>
      </template>
    </AppPageHeader>

    <section class="metric-grid">
      <article v-for="item in metricCards" :key="item.label" class="metric-card dashboard-metric">
        <div class="dashboard-metric__top">
          <span class="dashboard-metric__label">{{ item.label }}</span>
          <div class="dashboard-metric__icon" :class="item.tone">
            <el-icon :size="18">
              <component :is="item.icon" />
            </el-icon>
          </div>
        </div>
        <strong class="metric-card__value">{{ item.value }}</strong>
        <p class="metric-card__hint">{{ item.hint }}</p>
      </article>
    </section>

    <div class="dashboard-grid">
      <section class="panel">
        <div class="panel-header">
          <div>
            <h2 class="panel-title">今日重点</h2>
            <p class="panel-subtitle">先处理权限流转，再推进业务模块接入，会让整个后台更稳定。</p>
          </div>
        </div>

        <div class="focus-list">
          <article v-for="item in focusItems" :key="item.title" class="focus-item">
            <span class="focus-item__index">{{ item.index }}</span>
            <div class="focus-item__copy">
              <h3>{{ item.title }}</h3>
              <p>{{ item.description }}</p>
            </div>
          </article>
        </div>
      </section>

      <section class="panel">
        <div class="panel-header">
          <div>
            <h2 class="panel-title">快捷入口</h2>
            <p class="panel-subtitle">用统一样式快速进入最常用的管理页面。</p>
          </div>
        </div>

        <div class="action-grid">
          <button
            v-for="action in quickActions"
            :key="action.title"
            type="button"
            class="action-card"
            @click="router.push(action.path)"
          >
            <div class="action-card__icon">
              <el-icon :size="18">
                <component :is="action.icon" />
              </el-icon>
            </div>
            <div class="action-card__copy">
              <strong>{{ action.title }}</strong>
              <span>{{ action.description }}</span>
            </div>
          </button>
        </div>
      </section>
    </div>

    <section class="panel">
      <div class="panel-header">
        <div>
          <h2 class="panel-title">接入建议</h2>
          <p class="panel-subtitle">图表和真实业务指标接入这里后，整个首页就能完整承担运营总览角色。</p>
        </div>
        <span class="soft-note">当前先展示结构示意，方便后续继续扩展。</span>
      </div>

      <div class="dashboard-board">
        <article v-for="item in boardCards" :key="item.title" class="dashboard-board__card">
          <span>{{ item.tag }}</span>
          <h3>{{ item.title }}</h3>
          <p>{{ item.description }}</p>
        </article>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { Connection, Document, Folder, Grid, Menu, Setting, User, UserFilled } from '@element-plus/icons-vue'
import AppPageHeader from '@/components/AppPageHeader.vue'
import { getDeptTreeApi } from '@/api/dept'
import { getPermissionTreeApi } from '@/api/permission'
import { getRolePageApi } from '@/api/role'
import { getUserPageApi } from '@/api/user'
import { countTreeNodes } from '@/utils/admin'

const router = useRouter()

const userTotal = ref('--')
const roleTotal = ref('--')
const permissionTotal = ref('--')
const deptTotal = ref('--')

const overviewStats = computed(() => [
  { label: '后台账号', value: userTotal.value, hint: '已纳入当前管理体系' },
  { label: '角色模型', value: roleTotal.value, hint: '覆盖权限分工层级' },
  { label: '权限节点', value: permissionTotal.value, hint: '菜单、按钮与接口统一管理' },
  { label: '部门层级', value: deptTotal.value, hint: '支撑用户归属和数据范围' },
])

const metricCards = computed(() => [
  {
    label: '用户总数',
    value: userTotal.value,
    hint: '来源于用户分页接口 total',
    icon: User,
    tone: 'tone-emerald',
  },
  {
    label: '角色规模',
    value: roleTotal.value,
    hint: '来源于角色分页接口 total',
    icon: UserFilled,
    tone: 'tone-amber',
  },
  {
    label: '权限节点',
    value: permissionTotal.value,
    hint: '来源于权限树节点统计',
    icon: Menu,
    tone: 'tone-slate',
  },
  {
    label: '部门层级',
    value: deptTotal.value,
    hint: '来源于部门树节点统计',
    icon: Connection,
    tone: 'tone-ocean',
  },
])

const focusItems = [
  {
    index: '01',
    title: '整理用户与角色关系',
    description: '先把账号归属和角色模型固化下来，后面的权限分配会更清晰。',
  },
  {
    index: '02',
    title: '校准菜单权限树',
    description: '把系统管理、业务中心和后续模块统一挂在同一条导航逻辑上。',
  },
  {
    index: '03',
    title: '补齐业务概览指标',
    description: '商品、订单和营销数据一旦接入，仪表盘就能直接承担运营总览角色。',
  },
]

const quickActions = [
  {
    title: '用户管理',
    description: '维护账号信息与登录状态',
    path: '/system/auth/user',
    icon: User,
  },
  {
    title: '角色管理',
    description: '统一整理权限分配模型',
    path: '/system/auth/role',
    icon: UserFilled,
  },
  {
    title: '权限管理',
    description: '维护路由与可见权限节点',
    path: '/system/auth/permission',
    icon: Menu,
  },
  {
    title: '部门管理',
    description: '整理组织树与归属关系',
    path: '/system/auth/dept',
    icon: Folder,
  },
  {
    title: '系统参数',
    description: '查看默认密码等运行参数',
    path: '/system/auth/config',
    icon: Setting,
  },
  {
    title: '字典管理',
    description: '维护状态值与通用选项',
    path: '/system/auth/dict',
    icon: Document,
  },
  {
    title: '应用管理',
    description: '维护系统应用和会员应用',
    path: '/system/auth/app',
    icon: Grid,
  },
]

const boardCards = [
  {
    tag: '趋势图',
    title: '访问趋势面板',
    description: '接入 ECharts 后可展示登录量、活跃人数和权限调用趋势。',
  },
  {
    tag: '通知流',
    title: '待处理事项',
    description: '适合挂载权限申请、账号变更和异常提醒等运营信息。',
  },
  {
    tag: '业务扩展',
    title: '商城与订单概览',
    description: '后续可把商品、订单和营销模块汇总到同一块总览视图。',
  },
  {
    tag: '审计记录',
    title: '操作留痕',
    description: '适合展示关键管理动作与最近一次高风险操作记录。',
  },
]

const loadOverview = async () => {
  try {
    const [userPage, rolePage, permissionTree, deptTree] = await Promise.all([
      getUserPageApi({
        pageNum: 1,
        pageSize: 1,
        searchKey: '',
      }),
      getRolePageApi({
        pageNum: 1,
        pageSize: 1,
        searchKey: '',
      }),
      getPermissionTreeApi(),
      getDeptTreeApi(),
    ])

    userTotal.value = String(userPage.total ?? 0)
    roleTotal.value = String(rolePage.total ?? 0)
    permissionTotal.value = String(countTreeNodes(permissionTree))
    deptTotal.value = String(countTreeNodes(deptTree))
  } catch {
    userTotal.value = '--'
    roleTotal.value = '--'
    permissionTotal.value = '--'
    deptTotal.value = '--'
  }
}

onMounted(() => {
  loadOverview()
})
</script>

<style scoped>
.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.15fr) minmax(320px, 0.85fr);
  gap: 16px;
}

.dashboard-metric {
  gap: 10px;
}

.dashboard-metric__top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.dashboard-metric__label {
  color: var(--app-muted);
  font-size: 12px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

.dashboard-metric__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border-radius: 12px;
}

.tone-emerald {
  background: #e6f4ea;
  color: #137333;
}

.tone-amber {
  background: #fef7e0;
  color: #b06000;
}

.tone-slate {
  background: #f1f3f4;
  color: var(--app-muted-strong);
}

.tone-ocean {
  background: #e8f0fe;
  color: var(--app-accent-strong);
}

.focus-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.focus-item {
  display: flex;
  gap: 12px;
  padding: 14px;
  border: 1px solid var(--app-border);
  border-radius: 16px;
  background: var(--app-surface-muted);
}

.focus-item__index {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  flex-shrink: 0;
  border-radius: 12px;
  background: #e8f0fe;
  color: var(--app-accent-strong);
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 0.02em;
}

.focus-item__copy h3,
.dashboard-board__card h3 {
  margin: 0;
  color: var(--app-title);
  font-size: 16px;
  font-weight: 600;
}

.focus-item__copy p,
.dashboard-board__card p {
  margin: 6px 0 0;
  color: var(--app-muted);
  font-size: 12px;
  line-height: 1.6;
}

.action-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.action-card {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 14px;
  border: 1px solid var(--app-border);
  border-radius: 16px;
  background: var(--app-surface-muted);
  cursor: pointer;
  transition:
    border-color 0.2s ease,
    background-color 0.2s ease;
}

.action-card:hover {
  border-color: #c6d4e5;
  background: #ffffff;
}

.action-card__icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 38px;
  height: 38px;
  flex-shrink: 0;
  border-radius: 12px;
  background: #e8f0fe;
  color: var(--app-accent-strong);
}

.action-card__copy {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  text-align: left;
}

.action-card__copy strong {
  color: var(--app-title);
  font-size: 15px;
  font-weight: 600;
}

.action-card__copy span,
.dashboard-board__card span {
  color: var(--app-muted);
  font-size: 12px;
}

.dashboard-board {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.dashboard-board__card {
  padding: 14px;
  border: 1px solid var(--app-border);
  border-radius: 16px;
  background: var(--app-surface-muted);
}

.dashboard-board__card span {
  display: inline-flex;
  margin-bottom: 8px;
  font-weight: 500;
  letter-spacing: 0.02em;
}

@media (max-width: 1200px) {
  .dashboard-grid,
  .dashboard-board {
    grid-template-columns: 1fr;
  }

  .action-grid {
    grid-template-columns: 1fr;
  }
}
</style>
