package com.zhanglx.sso.auth.domain.dto;


import com.zhanglx.sso.core.domain.page.BasePageQuery;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/11 16:27
 * @ClassName: UserQueryDTO
 * @Description:
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UserQueryDTO extends BasePageQuery {

    private String username;
    private String deptId;

}
