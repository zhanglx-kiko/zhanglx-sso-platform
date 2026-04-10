package com.zhanglx.sso.auth.domain.dto;

import com.zhanglx.sso.auth.enums.EnableStatusEnum;
import com.zhanglx.sso.auth.enums.UserTypeEnum;
import com.zhanglx.sso.core.domain.page.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * 应用分页查询参数对象。
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Schema(name = "AppQueryDTO", description = "应用分页查询参数")
public class AppQueryDTO extends PageQuery {

    /**
     * 应用编码。
     */
    @Schema(description = "应用编码")
    private String appCode;

    /**
     * 应用名称。
     */
    @Schema(description = "应用名称")
    private String appName;

    /**
     * 状态：1-启用，0-停用。
     */
    @Schema(description = "状态：1-启用，0-停用")
    private EnableStatusEnum status;

    /**
     * 用户类型：1-系统用户，2-会员用户。
     */
    @Schema(description = "用户类型：1-系统用户，2-会员用户")
    private UserTypeEnum userType;
}
