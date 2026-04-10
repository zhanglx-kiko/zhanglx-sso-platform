package com.zhanglx.sso.auth.domain.dto;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

class UserDTOSerializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldSerializeDeptNameAndStillHideSensitiveFields() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUsername("admin");
        userDTO.setDeptName("dept-a");
        userDTO.setPassword("secret");
        userDTO.setOpenId("wx-open-id");

        String json = objectMapper.writeValueAsString(userDTO);

        assertThat(json).contains("\"deptName\":\"dept-a\"");
        assertThat(json).doesNotContain("password");
        assertThat(json).doesNotContain("openId");
    }
}