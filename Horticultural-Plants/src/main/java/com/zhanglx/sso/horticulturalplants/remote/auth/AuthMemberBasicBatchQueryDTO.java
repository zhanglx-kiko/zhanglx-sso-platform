package com.zhanglx.sso.horticulturalplants.remote.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthMemberBasicBatchQueryDTO {

    private List<Long> memberIds;
}
