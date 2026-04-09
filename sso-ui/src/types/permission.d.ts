/**
 * 权限项 DTO 对象 (对应后端 PermissionDTO)
 */
export interface PermissionDTO {
  /** 继承自 BaseDTO 的主键 ID */
  id: string
  /** 权限项名称 */
  name: string
  /** 权限项标识 */
  identification: string
  /** 父ID */
  parentId: string
  /** 标识血缘 */
  identityLineage?: string
  /** 组件地址 */
  comPath?: string
  /** 路由地址 */
  path?: string
  /** 菜单图标 */
  iconStr?: string
  /** 显示顺序 */
  displayNo: number
  /** 是否为外链(1是 0否) */
  isFrame?: number
  /** 类型(-1平台 0模块 1菜单 2按钮 3接口) */
  type: number
  /** 备注 */
  remark?: string
  /** 子权限项 */
  children?: PermissionDTO[]
}

/**
 * 权限查询 DTO 对象 (对应后端 PermissionQueryDTO)
 */
export interface PermissionQueryDTO {
  /** 账号 */
  username?: string
  /** 权限项标识列表 */
  identifications?: string[]
  /** 权限项类型列表 */
  permissionTypes?: string[]
}

/**
 * 权限导入 Excel 接收对象 (对应后端 PermissionExcelVO)
 */
export interface PermissionExcelVO {
  /** 类型(-1平台 0模块 1菜单 2按钮 3接口) */
  type: number
  /** 上级权限标识 */
  parentIdentification?: string
  /** 权限项名称 */
  name: string
  /** 权限项标识 */
  identification: string
  /** 路由地址 */
  path?: string
  /** 组件地址 */
  comPath?: string
  /** 是否为外链(0是 1否) */
  isFrame?: number
  /** 菜单图标 */
  iconStr?: string
  /** 显示序号 */
  displayNo: number
  /** 备注 */
  remark?: string
  /** 导入失败原因 */
  errorMessage?: string
}

/**
 * 权限视图展示对象 (对应后端 PermissionVO)
 */
export interface PermissionVO {
  /** 继承自 BaseVO 的主键 ID */
  id: string
  /** 权限项名称 */
  name: string
  /** 权限项标识 */
  identification: string
  /** 父ID */
  parentId: string
  /** 标识血缘 */
  identityLineage?: string
  /** 组件地址 */
  comPath?: string
  /** 路由地址 */
  path?: string
  /** 菜单图标 */
  iconStr?: string
  /** 显示顺序 (注: 后端该字段定义为 String) */
  displayNo: number
  /** 是否为外链(0是 1否) */
  isFrame?: number
  /** 类型(-1平台 0模块 1菜单 2按钮 3接口) */
  type: number
  /** 备注 */
  remark?: string
  /** 子权限项（前端树形结构渲染时常用，虽然VO里没显式写，但如果后端返回树结构会用到） */
  children?: PermissionVO[]
}
