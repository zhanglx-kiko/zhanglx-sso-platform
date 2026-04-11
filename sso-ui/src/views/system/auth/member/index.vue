<template>
  <div class="page-shell">
    <template v-if="canListMember">
      <AuthSearchSection :model="queryForm" :compact="false">
      <template #toolbar>
        <el-button plain @click="loadMembers">刷新列表</el-button>
      </template>

      <el-form-item label="关键字">
        <el-input
          v-model="queryForm.searchKey"
          placeholder="手机号 / 昵称 / 邮箱"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="会员标识">
        <el-input
          v-model="queryForm.memberId"
          placeholder="请输入会员标识"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="会员状态">
        <el-select v-model="queryForm.status" placeholder="全部状态" clearable>
          <el-option
            v-for="item in MEMBER_STATUS_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="会员类型">
        <el-select v-model="queryForm.memberType" placeholder="全部类型" clearable>
          <el-option
            v-for="item in MEMBER_TYPE_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="实名状态">
        <el-select v-model="queryForm.realNameStatus" placeholder="全部实名状态" clearable>
          <el-option
            v-for="item in MEMBER_REAL_NAME_STATUS_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="手机绑定">
        <el-select v-model="queryForm.phoneBound" placeholder="全部" clearable>
          <el-option
            v-for="item in YES_NO_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="微信绑定">
        <el-select v-model="queryForm.hasWechatBind" placeholder="全部" clearable>
          <el-option
            v-for="item in YES_NO_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="注册时间">
        <el-date-picker
          v-model="registerTimeRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          unlink-panels
        />
      </el-form-item>
      <el-form-item label="最后登录">
        <el-date-picker
          v-model="lastLoginTimeRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          range-separator="至"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          unlink-panels
        />
      </el-form-item>
      <el-form-item label="注册IP">
        <el-input
          v-model="queryForm.registerIp"
          placeholder="请输入注册IP"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>
      <el-form-item label="最后登录IP">
        <el-input
          v-model="queryForm.lastLoginIp"
          placeholder="请输入最后登录IP"
          clearable
          @keyup.enter="handleSearch"
        />
      </el-form-item>

      <template #actions>
        <el-button type="primary" @click="handleSearch">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </template>
    </AuthSearchSection>

    <section class="panel panel--table">
      <div class="panel-header">
        <div>
          <h2 class="panel-title">会员列表</h2>
        </div>
      </div>

      <el-table v-loading="loading" :data="memberList" row-key="id">
        <el-table-column prop="phoneNumber" label="手机号" min-width="148">
          <template #default="{ row }">
            {{ row.phoneNumber || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="会员信息" min-width="220">
          <template #default="{ row }">
            <div class="member-identity">
              <el-avatar :size="38" :src="row.avatar || undefined">
                {{ row.nickname?.slice(0, 1) || '会' }}
              </el-avatar>
              <div class="member-identity__meta">
                <strong>{{ row.nickname || '未设置昵称' }}</strong>
                <span>会员标识：{{ row.id }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getMemberStatusTagType(row.status)">
              {{ getMemberStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="会员类型" width="110" align="center">
          <template #default="{ row }">
            {{ getMemberTypeLabel(row.memberType) }}
          </template>
        </el-table-column>
        <el-table-column label="实名状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getRealNameStatusTagType(row.realNameStatus)" effect="plain">
              {{ getRealNameStatusLabel(row.realNameStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="等级/积分" min-width="120" align="center">
          <template #default="{ row }">
            <div class="metric-stack">
              <span>Lv.{{ row.userLevel ?? 1 }}</span>
              <small>{{ row.points ?? 0 }} 积分</small>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="绑定情况" min-width="136" align="center">
          <template #default="{ row }">
            <div class="binding-tags">
              <el-tag size="small" :type="row.phoneBound ? 'success' : 'info'">
                {{ row.phoneBound ? '已绑手机' : '未绑手机' }}
              </el-tag>
              <el-tag size="small" :type="row.wechatBound ? 'success' : 'info'">
                {{ row.wechatBound ? '已绑微信' : '未绑微信' }}
              </el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="170" />
        <el-table-column prop="lastLoginTime" label="最后登录" min-width="170">
          <template #default="{ row }">
            {{ row.lastLoginTime || '--' }}
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginIp" label="最后登录IP" min-width="150">
          <template #default="{ row }">
            {{ row.lastLoginIp || '--' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <el-space :size="10" wrap>
              <el-button v-if="canViewMember" link type="primary" @click="openDetailDrawer(row)">详情</el-button>
              <el-dropdown v-if="getRowActions(row).length" @command="handleRowCommand($event, row)">
                <el-button link type="primary">
                  更多
                  <el-icon class="el-icon--right"><ArrowDown /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item
                      v-for="action in getRowActions(row)"
                      :key="action.command"
                      :command="action.command"
                    >
                      {{ action.label }}
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <div class="panel-footer">
        <el-pagination
          v-model:current-page="queryForm.pageNum"
          v-model:page-size="queryForm.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          background
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadMembers"
          @current-change="loadMembers"
        />
      </div>
      </section>
    </template>
    <el-empty v-else description="当前账号没有会员查询权限" />
    <el-drawer
      v-model="detailVisible"
      title="会员详情"
      size="72%"
      destroy-on-close
      @closed="resetDetailState"
    >
      <div v-loading="detailLoading" class="detail-drawer">
        <el-empty v-if="!detailLoading && !detailData" description="暂无会员详情" />
        <template v-else-if="detailData">
          <div class="detail-hero">
            <div class="member-identity">
              <el-avatar :size="56" :src="detailData.avatar || undefined">
                {{ detailData.nickname?.slice(0, 1) || '会' }}
              </el-avatar>
              <div class="member-identity__meta member-identity__meta--detail">
                <strong>{{ detailData.nickname || '未设置昵称' }}</strong>
                <span>{{ detailData.phoneNumber || '未绑定手机号' }}</span>
              </div>
            </div>
            <div class="detail-hero__tags">
              <el-tag :type="getMemberStatusTagType(detailData.status)">
                {{ getMemberStatusLabel(detailData.status) }}
              </el-tag>
              <el-tag effect="plain">
                {{ getMemberTypeLabel(detailData.memberType) }}
              </el-tag>
              <el-tag effect="plain" :type="getRealNameStatusTagType(detailData.realNameStatus)">
                {{ getRealNameStatusLabel(detailData.realNameStatus) }}
              </el-tag>
            </div>
          </div>

          <el-tabs v-model="activeDetailTab" @tab-change="handleDetailTabChange">
            <el-tab-pane label="基础信息" name="profile">
              <el-descriptions :column="2" border class="detail-descriptions">
                <el-descriptions-item label="会员标识">{{ detailData.id }}</el-descriptions-item>
                <el-descriptions-item label="邮箱">{{ detailData.email || '--' }}</el-descriptions-item>
                <el-descriptions-item label="性别">{{ getGenderLabel(detailData.sex) }}</el-descriptions-item>
                <el-descriptions-item label="生日">{{ detailData.birthday || '--' }}</el-descriptions-item>
                <el-descriptions-item label="手机绑定">
                  {{ detailData.phoneBound ? '已绑定' : '未绑定' }}
                </el-descriptions-item>
                <el-descriptions-item label="微信绑定">
                  {{ detailData.wechatBound ? '已绑定' : '未绑定' }}
                </el-descriptions-item>
                <el-descriptions-item label="用户等级">
                  Lv.{{ detailData.userLevel ?? 1 }}
                </el-descriptions-item>
                <el-descriptions-item label="积分">
                  {{ detailData.points ?? 0 }}
                </el-descriptions-item>
                <el-descriptions-item label="状态原因">
                  {{ detailData.statusReason || '--' }}
                </el-descriptions-item>
                <el-descriptions-item label="状态到期">
                  {{ detailData.statusExpireTime || '--' }}
                </el-descriptions-item>
                <el-descriptions-item label="注册来源">
                  {{ detailData.registerSource || '--' }}
                </el-descriptions-item>
                <el-descriptions-item label="注册设备">
                  {{ detailData.registerDevice || '--' }}
                </el-descriptions-item>
                <el-descriptions-item label="注册IP">
                  {{ detailData.registerIp || '--' }}
                </el-descriptions-item>
                <el-descriptions-item label="最后登录IP">
                  {{ detailData.lastLoginIp || '--' }}
                </el-descriptions-item>
                <el-descriptions-item label="最后登录">
                  {{ detailData.lastLoginTime || '--' }}
                </el-descriptions-item>
                <el-descriptions-item label="风险等级">
                  {{ detailData.riskLevel ?? 0 }}
                </el-descriptions-item>
                <el-descriptions-item label="黑名单">
                  {{ getYesNoLabel(detailData.blacklistFlag) }}
                </el-descriptions-item>
                <el-descriptions-item label="已注销">
                  {{ detailData.cancelled ? '是' : '否' }}
                </el-descriptions-item>
                <el-descriptions-item label="注销时间">
                  {{ detailData.cancelTime || '--' }}
                </el-descriptions-item>
                <el-descriptions-item label="禁用时间">
                  {{ detailData.disabledTime || '--' }}
                </el-descriptions-item>
                <el-descriptions-item label="创建时间">
                  {{ detailData.createTime || '--' }}
                </el-descriptions-item>
                <el-descriptions-item label="更新时间">
                  {{ detailData.updateTime || '--' }}
                </el-descriptions-item>
                <el-descriptions-item label="扩展资料" :span="2">
                  <pre class="profile-extra">{{ detailData.profileExtra || '--' }}</pre>
                </el-descriptions-item>
              </el-descriptions>

              <div v-if="showProfileSummary" class="summary-grid">
                <section v-if="canViewSocialBindings" class="summary-card">
                  <h3>社交绑定摘要</h3>
                  <el-tag
                    v-for="item in detailData.socialBindings || []"
                    :key="item.id"
                    class="summary-tag"
                    effect="plain"
                  >
                    {{ getSocialTypeLabel(item.identityType) }}
                  </el-tag>
                  <span v-if="!(detailData.socialBindings || []).length" class="soft-note">暂无社交绑定</span>
                </section>
                <section v-if="canViewManageRecords" class="summary-card">
                  <h3>最近管理动作</h3>
                  <div v-if="detailData.manageRecordSummary?.length" class="summary-list">
                    <div v-for="item in detailData.manageRecordSummary" :key="item.id" class="summary-list__item">
                      <strong>{{ getManageActionLabel(item.actionType) }}</strong>
                      <span>{{ item.createTime || '--' }}</span>
                    </div>
                  </div>
                  <span v-else class="soft-note">暂无管理记录</span>
                </section>
                <section v-if="canViewLoginAudits" class="summary-card">
                  <h3>最近登录记录</h3>
                  <div v-if="detailData.loginAuditSummary?.length" class="summary-list">
                    <div v-for="item in detailData.loginAuditSummary" :key="item.id" class="summary-list__item">
                      <strong>{{ getLoginEventLabel(item.eventType, item.loginResult) }}</strong>
                      <span>{{ item.createTime || '--' }}</span>
                    </div>
                  </div>
                  <span v-else class="soft-note">暂无登录记录</span>
                </section>
              </div>
            </el-tab-pane>

            <el-tab-pane v-if="canViewSocialBindings" label="社交绑定" name="social">
              <el-table v-loading="socialBindingsLoading" :data="socialBindings" class="inner-table">
                <el-table-column prop="identityType" label="绑定类型" min-width="140">
                  <template #default="{ row }">
                    {{ getSocialTypeLabel(row.identityType) }}
                  </template>
                </el-table-column>
                <el-table-column prop="identifier" label="第三方标识" min-width="220" show-overflow-tooltip />
                <el-table-column prop="unionId" label="联合标识" min-width="220" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{ row.unionId || '--' }}
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="绑定时间" min-width="170" />
              </el-table>
              <el-empty v-if="!socialBindingsLoading && !socialBindings.length" description="暂无社交绑定" />
            </el-tab-pane>

            <el-tab-pane v-if="canViewLoginAudits" label="登录审计" name="login">
              <el-table v-loading="loginAuditState.loading" :data="loginAuditState.records" class="inner-table">
                <el-table-column prop="eventType" label="事件" width="110">
                  <template #default="{ row }">
                    {{ getLoginEventLabel(row.eventType, row.loginResult) }}
                  </template>
                </el-table-column>
                <el-table-column prop="displayName" label="展示名称" min-width="130">
                  <template #default="{ row }">
                    {{ row.displayName || row.username || '--' }}
                  </template>
                </el-table-column>
                <el-table-column prop="loginIp" label="登录IP" min-width="140">
                  <template #default="{ row }">
                    {{ row.loginIp || '--' }}
                  </template>
                </el-table-column>
                <el-table-column prop="deviceType" label="设备" min-width="120">
                  <template #default="{ row }">
                    {{ row.deviceType || '--' }}
                  </template>
                </el-table-column>
                <el-table-column prop="failReason" label="失败原因" min-width="180" show-overflow-tooltip>
                  <template #default="{ row }">
                    {{ row.failReason || '--' }}
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="记录时间" min-width="170" />
              </el-table>
              <div class="inner-pagination">
                <el-pagination
                  v-model:current-page="loginAuditState.pageNum"
                  v-model:page-size="loginAuditState.pageSize"
                  :page-sizes="[5, 10, 20, 50]"
                  :total="loginAuditState.total"
                  background
                  layout="total, sizes, prev, pager, next"
                  @size-change="loadMemberLoginAudits"
                  @current-change="loadMemberLoginAudits"
                />
              </div>
            </el-tab-pane>

            <el-tab-pane v-if="canViewManageRecords" label="管理记录" name="manage">
              <el-table v-loading="manageRecordState.loading" :data="manageRecordState.records" class="inner-table">
                <el-table-column prop="actionType" label="动作" width="130">
                  <template #default="{ row }">
                    <el-tag effect="plain">{{ getManageActionLabel(row.actionType) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column label="状态变化" min-width="170">
                  <template #default="{ row }">
                    {{ getMemberStatusLabel(row.beforeStatus) }} → {{ getMemberStatusLabel(row.afterStatus) }}
                  </template>
                </el-table-column>
                <el-table-column prop="operatorName" label="操作人" min-width="120">
                  <template #default="{ row }">
                    {{ row.operatorName || '--' }}
                  </template>
                </el-table-column>
                <el-table-column prop="reason" label="原因" min-width="200" show-overflow-tooltip />
                <el-table-column prop="expireTime" label="到期时间" min-width="170">
                  <template #default="{ row }">
                    {{ row.expireTime || '--' }}
                  </template>
                </el-table-column>
                <el-table-column prop="createTime" label="创建时间" min-width="170" />
              </el-table>
              <div class="inner-pagination">
                <el-pagination
                  v-model:current-page="manageRecordState.pageNum"
                  v-model:page-size="manageRecordState.pageSize"
                  :page-sizes="[5, 10, 20, 50]"
                  :total="manageRecordState.total"
                  background
                  layout="total, sizes, prev, pager, next"
                  @size-change="loadMemberManageRecords"
                  @current-change="loadMemberManageRecords"
                />
              </div>
            </el-tab-pane>
          </el-tabs>
        </template>
      </div>
    </el-drawer>

    <el-dialog
      v-model="actionDialog.visible"
      :title="actionDialogTitle"
      width="560px"
      @closed="resetActionDialog"
    >
      <el-form ref="actionFormRef" :model="actionForm" :rules="actionFormRules" label-width="92px">
        <el-form-item label="会员">
          <div class="action-member-meta">
            <strong>{{ actionDialog.member?.nickname || '未设置昵称' }}</strong>
            <span>{{ actionDialog.member?.phoneNumber || '未绑定手机号' }}</span>
          </div>
        </el-form-item>
        <el-form-item label="处理原因" prop="reason">
          <el-input
            v-model="actionForm.reason"
            type="textarea"
            :rows="3"
            maxlength="255"
            show-word-limit
            :placeholder="`请输入${actionDialogTitle}原因`"
          />
        </el-form-item>
        <el-form-item v-if="showExpireTimeField" label="到期时间" prop="expireTime">
          <el-date-picker
            v-model="actionForm.expireTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="可选：设置状态失效时间"
          />
        </el-form-item>
        <el-form-item label="补充备注" prop="remark">
          <el-input
            v-model="actionForm.remark"
            type="textarea"
            :rows="2"
            maxlength="255"
            show-word-limit
            placeholder="可选：补充说明"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="actionDialog.visible = false">取消</el-button>
        <el-button type="primary" :loading="actionDialog.submitting" @click="submitAction">
          确认{{ actionDialogTitle }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>
<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ArrowDown } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import AuthSearchSection from '@/views/system/auth/components/AuthSearchSection.vue'
import {
  DEFAULT_PAGE_SIZE,
  MEMBER_PERMISSION_KEYS,
  MEMBER_MANAGE_ACTION_OPTIONS,
  MEMBER_REAL_NAME_STATUS_OPTIONS,
  MEMBER_STATUS_OPTIONS,
  MEMBER_TYPE_OPTIONS,
  YES_NO_OPTIONS,
} from '@/constants/admin'
import {
  disableMemberApi,
  enableMemberApi,
  forceLogoutMemberApi,
  freezeMemberApi,
  getMemberDetailApi,
  getMemberLoginAuditsApi,
  getMemberManageRecordsApi,
  getMemberPageApi,
  getMemberSocialBindingsApi,
  unfreezeMemberApi,
} from '@/api/member'
import { showGlobalError } from '@/stores/globalError'
import { usePermissionStore } from '@/stores/permission'
import { toPageResult } from '@/utils/admin'
import type {
  AdminMemberDetailVO,
  AdminMemberForceLogoutDTO,
  AdminMemberListVO,
  AdminMemberQueryDTO,
  AdminMemberStatusUpdateDTO,
  MemberLoginAuditVO,
  MemberManageRecordVO,
  MemberSocialBindingVO,
} from '@/types/system'

type MemberActionCommand = 'disable' | 'enable' | 'freeze' | 'unfreeze' | 'forceLogout'

interface MemberActionOption {
  command: MemberActionCommand
  label: string
}

interface DetailPageState<T> {
  loading: boolean
  pageNum: number
  pageSize: number
  total: number
  records: T[]
}

const MEMBER_STATUS_LABEL_MAP = Object.fromEntries(
  MEMBER_STATUS_OPTIONS.map((item) => [item.value, item.label]),
) as Record<number, string>
const MEMBER_REAL_NAME_LABEL_MAP = Object.fromEntries(
  MEMBER_REAL_NAME_STATUS_OPTIONS.map((item) => [item.value, item.label]),
) as Record<number, string>
const MEMBER_TYPE_LABEL_MAP = Object.fromEntries(
  MEMBER_TYPE_OPTIONS.map((item) => [item.value, item.label]),
) as Record<number, string>
const MEMBER_MANAGE_ACTION_LABEL_MAP = Object.fromEntries(
  MEMBER_MANAGE_ACTION_OPTIONS.map((item) => [item.value, item.label]),
) as Record<number, string>
const permissionStore = usePermissionStore()

const loading = ref(false)
const total = ref(0)
const memberList = ref<AdminMemberListVO[]>([])

const queryForm = reactive<AdminMemberQueryDTO>({
  pageNum: 1,
  pageSize: DEFAULT_PAGE_SIZE,
  searchKey: '',
  memberId: '',
  status: undefined,
  memberType: undefined,
  realNameStatus: undefined,
  phoneBound: undefined,
  hasWechatBind: undefined,
  registerIp: '',
  lastLoginIp: '',
})

const registerTimeRange = ref<string[]>([])
const lastLoginTimeRange = ref<string[]>([])

const detailVisible = ref(false)
const detailLoading = ref(false)
const activeDetailTab = ref('profile')
const currentDetailMemberId = ref('')
const detailData = ref<AdminMemberDetailVO | null>(null)

const socialBindingsLoading = ref(false)
const socialBindingsLoaded = ref(false)
const socialBindings = ref<MemberSocialBindingVO[]>([])

const loginAuditsLoaded = ref(false)
const manageRecordsLoaded = ref(false)

const loginAuditState = reactive<DetailPageState<MemberLoginAuditVO>>({
  loading: false,
  pageNum: 1,
  pageSize: 10,
  total: 0,
  records: [],
})

const manageRecordState = reactive<DetailPageState<MemberManageRecordVO>>({
  loading: false,
  pageNum: 1,
  pageSize: 10,
  total: 0,
  records: [],
})

const actionDialog = reactive<{
  visible: boolean
  command: MemberActionCommand
  member: AdminMemberListVO | null
  submitting: boolean
}>({
  visible: false,
  command: 'disable',
  member: null,
  submitting: false,
})

const actionFormRef = ref<FormInstance>()
const actionForm = reactive<AdminMemberStatusUpdateDTO & AdminMemberForceLogoutDTO>({
  reason: '',
  expireTime: '',
  remark: '',
})

const actionFormRules: FormRules = {
  reason: [
    { required: true, message: '请输入处理原因', trigger: 'blur' },
    { max: 255, message: '处理原因不能超过 255 个字符', trigger: 'blur' },
  ],
  remark: [{ max: 255, message: '补充备注不能超过 255 个字符', trigger: 'blur' }],
}

const actionDialogTitle = computed(() => {
  const titleMap: Record<MemberActionCommand, string> = {
    disable: '禁用会员',
    enable: '启用会员',
    freeze: '冻结会员',
    unfreeze: '解冻会员',
    forceLogout: '强制下线',
  }
  return titleMap[actionDialog.command]
})

const showExpireTimeField = computed(() => {
  return actionDialog.command === 'disable' || actionDialog.command === 'freeze'
})

const canListMember = computed(() => permissionStore.hasPermission(MEMBER_PERMISSION_KEYS.LIST))
const canViewMember = computed(() => permissionStore.hasPermission(MEMBER_PERMISSION_KEYS.VIEW))
const canViewSocialBindings = computed(() =>
  permissionStore.hasPermission(MEMBER_PERMISSION_KEYS.SOCIAL_LIST),
)
const canViewLoginAudits = computed(() =>
  permissionStore.hasPermission(MEMBER_PERMISSION_KEYS.LOGIN_LOG_LIST),
)
const canViewManageRecords = computed(() =>
  permissionStore.hasPermission(MEMBER_PERMISSION_KEYS.MANAGE_RECORD_LIST),
)
const canDisableMember = computed(() => permissionStore.hasPermission(MEMBER_PERMISSION_KEYS.DISABLE))
const canEnableMember = computed(() => permissionStore.hasPermission(MEMBER_PERMISSION_KEYS.ENABLE))
const canFreezeMember = computed(() => permissionStore.hasPermission(MEMBER_PERMISSION_KEYS.FREEZE))
const canUnfreezeMember = computed(() => permissionStore.hasPermission(MEMBER_PERMISSION_KEYS.UNFREEZE))
const canForceLogoutMember = computed(() =>
  permissionStore.hasPermission(MEMBER_PERMISSION_KEYS.FORCE_LOGOUT),
)
const showProfileSummary = computed(() => {
  return (
    canViewSocialBindings.value || canViewManageRecords.value || canViewLoginAudits.value
  )
})

const getMemberStatusLabel = (value?: number) => {
  return value === undefined || value === null ? '--' : (MEMBER_STATUS_LABEL_MAP[value] || `状态${value}`)
}

const getMemberStatusTagType = (value?: number) => {
  switch (value) {
    case 1:
      return 'success'
    case 0:
      return 'danger'
    case 2:
      return 'warning'
    case 3:
      return 'info'
    case 4:
      return ''
    default:
      return 'info'
  }
}

const getRealNameStatusLabel = (value?: number) => {
  return value === undefined || value === null ? '--' : (MEMBER_REAL_NAME_LABEL_MAP[value] || `状态${value}`)
}

const getRealNameStatusTagType = (value?: number) => {
  switch (value) {
    case 2:
      return 'success'
    case 1:
      return 'warning'
    case 3:
      return 'danger'
    default:
      return 'info'
  }
}

const getMemberTypeLabel = (value?: number) => {
  return value === undefined || value === null ? '--' : (MEMBER_TYPE_LABEL_MAP[value] || `类型${value}`)
}

const getManageActionLabel = (value?: number) => {
  return value === undefined || value === null ? '--' : (MEMBER_MANAGE_ACTION_LABEL_MAP[value] || `动作${value}`)
}

const getGenderLabel = (value?: number) => {
  switch (value) {
    case 1:
      return '男'
    case 2:
      return '女'
    default:
      return '未知'
  }
}

const getYesNoLabel = (value?: number | boolean) => {
  if (value === true || value === 1) return '是'
  if (value === false || value === 0) return '否'
  return '--'
}

const getSocialTypeLabel = (value?: string) => {
  if (!value) return '--'
  if (value === 'WECHAT_OPEN') return '微信公众号'
  if (value === 'WX_MINI') return '微信小程序'
  return value
}

const getLoginEventLabel = (eventType?: string, loginResult?: string) => {
  const eventLabel = eventType === 'LOGOUT' ? '登出' : '登录'
  if (loginResult === 'FAILURE') {
    return `${eventLabel}失败`
  }
  return `${eventLabel}成功`
}

const buildQueryPayload = (): AdminMemberQueryDTO => {
  return {
    ...queryForm,
    memberId: queryForm.memberId?.trim() || undefined,
    registerIp: queryForm.registerIp?.trim() || undefined,
    lastLoginIp: queryForm.lastLoginIp?.trim() || undefined,
    registerStartTime: registerTimeRange.value[0],
    registerEndTime: registerTimeRange.value[1],
    lastLoginStartTime: lastLoginTimeRange.value[0],
    lastLoginEndTime: lastLoginTimeRange.value[1],
  }
}

const getRowActions = (row: AdminMemberListVO): MemberActionOption[] => {
  const status = Number(row.status ?? 1)
  const actions: MemberActionOption[] = []

  if (status === 1) {
    if (canFreezeMember.value) {
      actions.push({ command: 'freeze', label: '鍐荤粨浼氬憳' })
    }
    if (canDisableMember.value) {
      actions.push({ command: 'disable', label: '绂佺敤浼氬憳' })
    }
    if (canForceLogoutMember.value) {
      actions.push({ command: 'forceLogout', label: '寮哄埗涓嬬嚎' })
    }
    return actions
  }

  if (status === 2) {
    if (canUnfreezeMember.value) {
      actions.push({ command: 'unfreeze', label: '瑙ｅ喕浼氬憳' })
    }
    if (canDisableMember.value) {
      actions.push({ command: 'disable', label: '绂佺敤浼氬憳' })
    }
    if (canForceLogoutMember.value) {
      actions.push({ command: 'forceLogout', label: '寮哄埗涓嬬嚎' })
    }
    return actions
  }

  if (status === 0) {
    if (canEnableMember.value) {
      actions.push({ command: 'enable', label: '鍚敤浼氬憳' })
    }
    if (canForceLogoutMember.value) {
      actions.push({ command: 'forceLogout', label: '寮哄埗涓嬬嚎' })
    }
    return actions
  }

  return actions
}

const loadMembers = async () => {
  if (!canListMember.value) {
    memberList.value = []
    total.value = 0
    return
  }
  loading.value = true
  try {
    const page = toPageResult(await getMemberPageApi(buildQueryPayload()))
    memberList.value = page.records
    total.value = page.total
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '鍔犺浇浼氬憳鍒楄〃澶辫触' })
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  queryForm.pageNum = 1
  void loadMembers()
}

const resetQuery = () => {
  queryForm.pageNum = 1
  queryForm.pageSize = DEFAULT_PAGE_SIZE
  queryForm.searchKey = ''
  queryForm.memberId = ''
  queryForm.status = undefined
  queryForm.memberType = undefined
  queryForm.realNameStatus = undefined
  queryForm.phoneBound = undefined
  queryForm.hasWechatBind = undefined
  queryForm.registerIp = ''
  queryForm.lastLoginIp = ''
  registerTimeRange.value = []
  lastLoginTimeRange.value = []
  void loadMembers()
}

const openDetailDrawer = async (row: AdminMemberListVO) => {
  if (!canViewMember.value) return

  detailVisible.value = true
  detailLoading.value = true
  currentDetailMemberId.value = row.id
  activeDetailTab.value = 'profile'
  socialBindingsLoaded.value = false
  loginAuditsLoaded.value = false
  manageRecordsLoaded.value = false

  try {
    const detail = await getMemberDetailApi(row.id)
    detailData.value = detail
    socialBindings.value = detail.socialBindings || []
    loginAuditState.records = detail.loginAuditSummary || []
    loginAuditState.total = detail.loginAuditSummary?.length || 0
    loginAuditState.pageNum = 1
    loginAuditState.pageSize = 10
    manageRecordState.records = detail.manageRecordSummary || []
    manageRecordState.total = detail.manageRecordSummary?.length || 0
    manageRecordState.pageNum = 1
    manageRecordState.pageSize = 10
  } catch (error) {
    detailVisible.value = false
    showGlobalError(error, { fallbackMessage: '鍔犺浇浼氬憳璇︽儏澶辫触' })
  } finally {
    detailLoading.value = false
  }
}

const loadMemberSocialBindings = async () => {
  if (!canViewSocialBindings.value || !currentDetailMemberId.value) return
  socialBindingsLoading.value = true
  try {
    socialBindings.value = await getMemberSocialBindingsApi(currentDetailMemberId.value)
    socialBindingsLoaded.value = true
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '鍔犺浇绀句氦缁戝畾澶辫触' })
  } finally {
    socialBindingsLoading.value = false
  }
}

const loadMemberLoginAudits = async () => {
  if (!canViewLoginAudits.value || !currentDetailMemberId.value) return
  loginAuditState.loading = true
  try {
    const page = toPageResult(
      await getMemberLoginAuditsApi(currentDetailMemberId.value, {
        pageNum: loginAuditState.pageNum,
        pageSize: loginAuditState.pageSize,
      }),
    )
    loginAuditState.records = page.records
    loginAuditState.total = page.total
    loginAuditsLoaded.value = true
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '鍔犺浇鐧诲綍瀹¤澶辫触' })
  } finally {
    loginAuditState.loading = false
  }
}

const loadMemberManageRecords = async () => {
  if (!canViewManageRecords.value || !currentDetailMemberId.value) return
  manageRecordState.loading = true
  try {
    const page = toPageResult(
      await getMemberManageRecordsApi(currentDetailMemberId.value, {
        pageNum: manageRecordState.pageNum,
        pageSize: manageRecordState.pageSize,
      }),
    )
    manageRecordState.records = page.records
    manageRecordState.total = page.total
    manageRecordsLoaded.value = true
  } catch (error) {
    showGlobalError(error, { fallbackMessage: '鍔犺浇绠＄悊璁板綍澶辫触' })
  } finally {
    manageRecordState.loading = false
  }
}

const handleDetailTabChange = (tabName: string | number) => {
  if (tabName === 'social' && canViewSocialBindings.value && !socialBindingsLoaded.value) {
    void loadMemberSocialBindings()
  }

  if (tabName === 'login' && canViewLoginAudits.value && !loginAuditsLoaded.value) {
    loginAuditState.pageNum = 1
    void loadMemberLoginAudits()
  }

  if (tabName === 'manage' && canViewManageRecords.value && !manageRecordsLoaded.value) {
    manageRecordState.pageNum = 1
    void loadMemberManageRecords()
  }
}

const resetDetailState = () => {
  detailData.value = null
  currentDetailMemberId.value = ''
  activeDetailTab.value = 'profile'
  socialBindings.value = []
  socialBindingsLoaded.value = false
  loginAuditsLoaded.value = false
  manageRecordsLoaded.value = false
  loginAuditState.loading = false
  loginAuditState.pageNum = 1
  loginAuditState.pageSize = 10
  loginAuditState.total = 0
  loginAuditState.records = []
  manageRecordState.loading = false
  manageRecordState.pageNum = 1
  manageRecordState.pageSize = 10
  manageRecordState.total = 0
  manageRecordState.records = []
}

const openActionDialog = (command: MemberActionCommand, row: AdminMemberListVO) => {
  actionDialog.visible = true
  actionDialog.command = command
  actionDialog.member = row
  actionForm.reason = ''
  actionForm.expireTime = ''
  actionForm.remark = ''
}

const resetActionDialog = () => {
  actionDialog.command = 'disable'
  actionDialog.member = null
  actionDialog.submitting = false
  actionForm.reason = ''
  actionForm.expireTime = ''
  actionForm.remark = ''
  actionFormRef.value?.clearValidate()
}

const handleRowCommand = (command: string, row: AdminMemberListVO) => {
  openActionDialog(command as MemberActionCommand, row)
}

const submitAction = async () => {
  if (!actionDialog.member) return

  const valid = await actionFormRef.value?.validate().catch(() => false)
  if (!valid) return

  const memberId = actionDialog.member.id
  actionDialog.submitting = true

  try {
    if (actionDialog.command === 'forceLogout') {
      await forceLogoutMemberApi(memberId, {
        reason: actionForm.reason,
        remark: actionForm.remark || undefined,
      })
    } else {
      const payload: AdminMemberStatusUpdateDTO = {
        reason: actionForm.reason,
        remark: actionForm.remark || undefined,
        expireTime: showExpireTimeField.value ? actionForm.expireTime || undefined : undefined,
      }

      if (actionDialog.command === 'disable') {
        await disableMemberApi(memberId, payload)
      } else if (actionDialog.command === 'enable') {
        await enableMemberApi(memberId, payload)
      } else if (actionDialog.command === 'freeze') {
        await freezeMemberApi(memberId, payload)
      } else if (actionDialog.command === 'unfreeze') {
        await unfreezeMemberApi(memberId, payload)
      }
    }

    ElMessage.success(`${actionDialogTitle.value}成功`)
    actionDialog.visible = false
    await loadMembers()

    if (detailVisible.value && currentDetailMemberId.value === memberId) {
      await openDetailDrawer(actionDialog.member)
    }
  } catch (error) {
    showGlobalError(error, { fallbackMessage: `${actionDialogTitle.value}失败` })
  } finally {
    actionDialog.submitting = false
  }
}

onMounted(() => {
  if (canListMember.value) {
    void loadMembers()
  }
})
</script>

<style scoped>
.member-identity {
  display: flex;
  align-items: center;
  gap: 12px;
}

.member-identity__meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.member-identity__meta strong {
  font-size: 14px;
  line-height: 1.2;
}

.member-identity__meta span {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.member-identity__meta--detail strong {
  font-size: 18px;
}

.metric-stack {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.metric-stack small {
  color: var(--el-text-color-secondary);
}

.binding-tags {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 6px;
}

.detail-drawer {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.detail-hero {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 4px 0 12px;
}

.detail-hero__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.detail-descriptions {
  margin-bottom: 18px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.summary-card {
  border: 1px solid var(--el-border-color-light);
  border-radius: 14px;
  padding: 16px;
  background: var(--el-fill-color-blank);
}

.summary-card h3 {
  margin: 0 0 12px;
  font-size: 14px;
}

.summary-tag {
  margin-right: 8px;
  margin-bottom: 8px;
}

.summary-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.summary-list__item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  font-size: 13px;
}

.summary-list__item span {
  color: var(--el-text-color-secondary);
}

.inner-table {
  margin-top: 4px;
}

.inner-pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.profile-extra {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
  font-family: inherit;
}

.action-member-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.action-member-meta span {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

@media (max-width: 1280px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
