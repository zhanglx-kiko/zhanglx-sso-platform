package com.zhanglx.sso.xss.integration;

import com.zhanglx.sso.xss.annotation.XssPolicy;
import com.zhanglx.sso.xss.support.XssAuditMetrics;
import com.zhanglx.sso.xss.support.XssPolicyMode;
import jakarta.servlet.Filter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 使用 MockMvc 跑完整 MVC 请求链路，验证 starter 自动装配后 Query、Form、Path、JSON、Multipart 都能生效。
 */
@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        classes = XssMockMvcIntegrationTest.TestApplication.class,
        properties = {
                "sso.xss.enabled=true",
                "sso.xss.global-enabled=true",
                "sso.xss.whitelist-paths[0]=/public/**"
        }
)
class XssMockMvcIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private XssAuditMetrics xssAuditMetrics;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {
        Filter[] filters = webApplicationContext.getBeansOfType(Filter.class)
                .values()
                .toArray(Filter[]::new);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .addFilters(filters)
                .build();
    }

    @Test
    void shouldSanitizeQueryAndHeaderThroughFullRequestChain() throws Exception {
        mockMvc.perform(get("/test/xss/query")
                        .param("remark", "<script>alert(1)</script>正常备注")
                        .param("searchKey", "张三<script>alert(1)</script>")
                        .param("password", "<script>alert(1)</script>123456")
                        .header("User-Agent", "<img src=x onerror=alert(1)>Mozilla"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remark").value("正常备注"))
                .andExpect(jsonPath("$.searchKey").value("张三"))
                .andExpect(jsonPath("$.password").value("<script>alert(1)</script>123456"))
                .andExpect(jsonPath("$.userAgent").value("Mozilla"));
    }

    @Test
    void shouldSanitizeFormThroughFullRequestChain() throws Exception {
        mockMvc.perform(post("/test/xss/form")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("remark", "<img src=x onerror=alert(1)>表单备注")
                        .param("keyword", "权限<script>alert(1)</script>")
                        .param("password", "<script>alert(1)</script>123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remark").value("表单备注"))
                .andExpect(jsonPath("$.keyword").value("权限"))
                .andExpect(jsonPath("$.password").value("<script>alert(1)</script>123456"));
    }

    @Test
    void shouldSanitizePathVariableThroughInterceptor() throws Exception {
        mockMvc.perform(get("/test/xss/path/{value}", "<img src=x onerror=alert(1)>路径值"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value("路径值"));
    }

    @Test
    void shouldSanitizeJsonBodyThroughRequestBodyAdvice() throws Exception {
        String jsonBody = """
                {
                  "remark": "<script>alert(1)</script>JSON备注",
                  "password": "<script>alert(1)</script>123456",
                  "content": "<p>保留段落</p><script>alert(1)</script>",
                  "child": {
                    "remark": "<img src=x onerror=alert(1)>子级备注"
                  },
                  "ext": {
                    "memo": "<svg onload=alert(1)></svg>扩展信息"
                  }
                }
                """;

        mockMvc.perform(post("/test/xss/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remark").value("JSON备注"))
                .andExpect(jsonPath("$.password").value("<script>alert(1)</script>123456"))
                .andExpect(jsonPath("$.content").value("<p>保留段落</p>"))
                .andExpect(jsonPath("$.child.remark").value("子级备注"))
                .andExpect(jsonPath("$.ext.memo").value("扩展信息"));
    }

    @Test
    void shouldSanitizeMultipartRequestParams() throws Exception {
        mockMvc.perform(multipart("/test/xss/multipart/param")
                        .param("remark", "<img src=x onerror=alert(1)>上传备注")
                        .param("searchKey", "张三<script>alert(1)</script>")
                        .param("password", "<script>alert(1)</script>123456"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remark").value("上传备注"))
                .andExpect(jsonPath("$.searchKey").value("张三"))
                .andExpect(jsonPath("$.password").value("<script>alert(1)</script>123456"));
    }

    @Test
    void shouldSanitizeMultipartStringPart() throws Exception {
        MockMultipartFile remarkPart = new MockMultipartFile(
                "remark",
                "",
                MediaType.TEXT_PLAIN_VALUE,
                "<script>alert(1)</script>分片备注".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile searchKeyPart = new MockMultipartFile(
                "searchKey",
                "",
                MediaType.TEXT_PLAIN_VALUE,
                "权限<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile passwordPart = new MockMultipartFile(
                "password",
                "",
                MediaType.TEXT_PLAIN_VALUE,
                "<script>alert(1)</script>123456".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/test/xss/multipart/string-part")
                        .file(remarkPart)
                        .file(searchKeyPart)
                        .file(passwordPart))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remark").value("分片备注"))
                .andExpect(jsonPath("$.searchKey").value("权限"))
                .andExpect(jsonPath("$.password").value("<script>alert(1)</script>123456"));
    }

    @Test
    void shouldSanitizeMultipartJsonPartAndKeepBinaryFileUntouched() throws Exception {
        MockMultipartFile payload = new MockMultipartFile(
                "payload",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                """
                        {
                          "remark": "<script>alert(1)</script>分片JSON备注",
                          "password": "<script>alert(1)</script>123456",
                          "content": "<p>富文本段落</p><script>alert(1)</script>",
                          "child": {
                            "remark": "<img src=x onerror=alert(1)>分片子级备注"
                          }
                        }
                        """.getBytes(StandardCharsets.UTF_8)
        );
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "demo.txt",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                "binary-<script>alert(1)</script>".getBytes(StandardCharsets.UTF_8)
        );

        mockMvc.perform(multipart("/test/xss/multipart/json-part")
                        .file(payload)
                        .file(file))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.payload.remark").value("分片JSON备注"))
                .andExpect(jsonPath("$.payload.password").value("<script>alert(1)</script>123456"))
                .andExpect(jsonPath("$.payload.content").value("<p>富文本段落</p>"))
                .andExpect(jsonPath("$.payload.child.remark").value("分片子级备注"))
                .andExpect(jsonPath("$.fileName").value("demo.txt"))
                .andExpect(jsonPath("$.fileSize").value(32));
    }

    @Test
    void shouldSkipWhitelistPathThroughFullRequestChain() throws Exception {
        String jsonBody = """
                {
                  "remark": "<script>alert(1)</script>白名单正文"
                }
                """;

        mockMvc.perform(post("/public/xss/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.remark").value("<script>alert(1)</script>白名单正文"));
    }

    @Test
    void shouldRecordAuditMetricsWithControlledEndpointTags() throws Exception {
        long requestHitBefore = xssAuditMetrics.getRequestHitCount().get();
        long hitBefore = xssAuditMetrics.getHitCount().get();
        AtomicLong endpointBefore = xssAuditMetrics.getEndpointHitDistribution()
                .computeIfAbsent("/test/xss/query", key -> new AtomicLong());
        long endpointHitBefore = endpointBefore.get();

        mockMvc.perform(get("/test/xss/query")
                        .param("remark", "<script>alert(1)</script>指标备注")
                        .param("searchKey", "指标<script>alert(1)</script>")
                        .param("password", "<script>alert(1)</script>123456")
                        .header("User-Agent", "<img src=x onerror=alert(1)>Chrome"))
                .andExpect(status().isOk());

        long requestHitDelta = xssAuditMetrics.getRequestHitCount().get() - requestHitBefore;
        long hitDelta = xssAuditMetrics.getHitCount().get() - hitBefore;
        long endpointHitDelta = xssAuditMetrics.getEndpointHitDistribution()
                .get("/test/xss/query")
                .get() - endpointHitBefore;

        org.assertj.core.api.Assertions.assertThat(requestHitDelta).isEqualTo(1L);
        org.assertj.core.api.Assertions.assertThat(hitDelta).isGreaterThanOrEqualTo(3L);
        org.assertj.core.api.Assertions.assertThat(endpointHitDelta).isEqualTo(1L);
    }

    @RestController
    @RequestMapping
    static class XssTestController {

        @GetMapping("/test/xss/query")
        public Map<String, Object> query(@RequestParam String remark,
                                         @RequestParam String searchKey,
                                         @RequestParam String password,
                                         @RequestHeader(value = "User-Agent", required = false) String userAgent) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("remark", remark);
            result.put("searchKey", searchKey);
            result.put("password", password);
            result.put("userAgent", userAgent);
            return result;
        }

        @PostMapping(path = "/test/xss/form", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        public Map<String, Object> form(@RequestParam String remark,
                                        @RequestParam String keyword,
                                        @RequestParam String password) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("remark", remark);
            result.put("keyword", keyword);
            result.put("password", password);
            return result;
        }

        @GetMapping("/test/xss/path/{value}")
        public Map<String, Object> path(@PathVariable String value) {
            return Map.of("value", value);
        }

        @PostMapping(path = "/test/xss/json", consumes = MediaType.APPLICATION_JSON_VALUE)
        public JsonPayload json(@RequestBody JsonPayload payload) {
            return payload;
        }

        @PostMapping(path = "/public/xss/json", consumes = MediaType.APPLICATION_JSON_VALUE)
        public JsonPayload publicJson(@RequestBody JsonPayload payload) {
            return payload;
        }

        @PostMapping(path = "/test/xss/multipart/param", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public Map<String, Object> multipartParam(@RequestParam String remark,
                                                  @RequestParam String searchKey,
                                                  @RequestParam String password) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("remark", remark);
            result.put("searchKey", searchKey);
            result.put("password", password);
            return result;
        }

        @PostMapping(path = "/test/xss/multipart/string-part", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public Map<String, Object> multipartStringPart(@RequestPart String remark,
                                                       @RequestPart String searchKey,
                                                       @RequestPart String password) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("remark", remark);
            result.put("searchKey", searchKey);
            result.put("password", password);
            return result;
        }

        @PostMapping(path = "/test/xss/multipart/json-part", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        public Map<String, Object> multipartJsonPart(@RequestPart JsonPayload payload,
                                                     @RequestPart MultipartFile file) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("payload", payload);
            result.put("fileName", file.getOriginalFilename());
            result.put("fileSize", file.getSize());
            return result;
        }
    }

    static class JsonPayload {
        public String remark;
        @XssPolicy(XssPolicyMode.NONE)
        public String password;
        @XssPolicy(XssPolicyMode.RICH_TEXT)
        public String content;
        public JsonChild child;
        public Map<String, Object> ext;
    }

    static class JsonChild {
        public String remark;
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @Import(XssTestController.class)
    static class TestApplication {
    }
}
