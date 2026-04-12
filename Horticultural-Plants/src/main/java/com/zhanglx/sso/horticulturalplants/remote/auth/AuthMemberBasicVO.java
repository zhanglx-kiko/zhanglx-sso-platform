package com.zhanglx.sso.horticulturalplants.remote.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthMemberBasicVO {

    private Long id;

    private String phoneNumber;

    private String nickname;

    private String avatar;

    private Boolean phoneBound;

    private Integer memberType;

    private Integer realNameStatus;

    private Integer status;
}
