package com.zhanglx.sso.auth.domain.dto;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TreeDtoDeserializationTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldDeserializeDeptDtoWithoutIdSetterConflict() throws Exception {
        String json = """
                {
                  \"id\": \"100\",
                  \"parentId\": \"10\",
                  \"deptName\": \"dev-dept\",
                  \"sortNum\": 3
                }
                """;

        DeptDTO deptDTO = objectMapper.readValue(json, DeptDTO.class);

        assertEquals(100L, deptDTO.getId());
        assertEquals(10L, deptDTO.getParentId());
        assertEquals("dev-dept", deptDTO.getDeptName());
        assertEquals(3, deptDTO.getSortNum());
    }

    @Test
    void shouldDeserializePermissionDtoWithoutIdSetterConflict() throws Exception {
        String json = """
                {
                  \"id\": \"200\",
                  \"parentId\": \"0\",
                  \"name\": \"user-menu\",
                  \"identification\": \"system:user:list\",
                  \"displayNo\": 1,
                  \"type\": 1
                }
                """;

        PermissionDTO permissionDTO = objectMapper.readValue(json, PermissionDTO.class);

        assertEquals(200L, permissionDTO.getId());
        assertEquals(0L, permissionDTO.getParentId());
        assertEquals("user-menu", permissionDTO.getName());
        assertEquals("system:user:list", permissionDTO.getIdentification());
        assertEquals(1, permissionDTO.getDisplayNo());
        assertEquals(1, permissionDTO.getType().getCode());
        assertNotNull(permissionDTO.getChildren());
        assertTrue(permissionDTO.getChildren().isEmpty());
    }

    @Test
    void shouldSerializeDeptDtoWithIdAndParentId() throws Exception {
        DeptDTO deptDTO = DeptDTO.builder()
                .id(100L)
                .parentId(10L)
                .deptName("dev-dept")
                .sortNum(3)
                .build();

        String json = objectMapper.writeValueAsString(deptDTO);

        assertTrue(json.contains("\"id\":\"100\""));
        assertTrue(json.contains("\"parentId\":\"10\""));
        assertTrue(json.contains("\"deptName\":\"dev-dept\""));
    }
}