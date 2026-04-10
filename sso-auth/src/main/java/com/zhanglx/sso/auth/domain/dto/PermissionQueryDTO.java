package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.auth.enums.PermissionTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @Author: Zhang L X
 * @Create: 2026/4/1 09:42
 * @ClassName: PermissionQueryDTO
 * @Description:
 */
@Data
public class PermissionQueryDTO {

    /**
     * 账号。
     */
    @Schema(description = "账号")
    private String username;

    /**
     * 权限项标识列表。
     */
    @Schema(description = "权限项标识列表")
    private List<String> identifications;

    /**
     * 权限项类型列表。
     */
    @Schema(description = "权限项类型列表")
    private List<PermissionTypeEnum> permissionTypes;

}
