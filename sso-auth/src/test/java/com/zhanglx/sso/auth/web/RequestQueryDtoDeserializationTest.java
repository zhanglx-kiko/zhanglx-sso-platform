package com.zhanglx.sso.auth.web;

import com.zhanglx.sso.auth.domain.dto.AuthLoginLogQueryDTO;
import com.zhanglx.sso.log.domain.query.OperationLogQueryDTO;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RequestQueryDtoDeserializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeAuthLoginLogQueryDtoWithBlankUserIdAndTimeRange() throws Exception {
        String json = """
                {
                  "pageNum": 1,
                  "pageSize": 20,
                  "userId": "",
                  "username": "",
                  "loginIp": "",
                  "startTime": "2026-04-04 00:00:00",
                  "endTime": "2026-04-10 23:59:59"
                }
                """;

        AuthLoginLogQueryDTO queryDTO = objectMapper.readValue(json, AuthLoginLogQueryDTO.class);

        assertThat(queryDTO.getUserId()).isNull();
        assertThat(queryDTO.getStartTime()).isEqualTo(LocalDateTime.of(2026, 4, 4, 0, 0, 0));
        assertThat(queryDTO.getEndTime()).isEqualTo(LocalDateTime.of(2026, 4, 10, 23, 59, 59));
    }

    @Test
    void shouldDeserializeOperationLogQueryDtoWithTimeRange() throws Exception {
        String json = """
                {
                  "pageNum": 1,
                  "pageSize": 20,
                  "userId": "",
                  "traceId": "",
                  "startTime": "2026-04-04 00:00:00",
                  "endTime": "2026-04-10 23:59:59"
                }
                """;

        OperationLogQueryDTO queryDTO = objectMapper.readValue(json, OperationLogQueryDTO.class);

        assertThat(queryDTO.getUserId()).isEmpty();
        assertThat(queryDTO.getStartTime()).isEqualTo(LocalDateTime.of(2026, 4, 4, 0, 0, 0));
        assertThat(queryDTO.getEndTime()).isEqualTo(LocalDateTime.of(2026, 4, 10, 23, 59, 59));
    }
}
