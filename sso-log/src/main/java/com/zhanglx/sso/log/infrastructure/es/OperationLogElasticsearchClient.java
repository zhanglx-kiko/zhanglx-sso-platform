package com.zhanglx.sso.log.infrastructure.es;

import com.zhanglx.sso.log.config.OperationLogProperties;
import com.zhanglx.sso.log.domain.model.OperationLogDocument;
import com.zhanglx.sso.log.domain.query.OperationLogQueryDTO;
import com.zhanglx.sso.log.domain.vo.OperationLogPageVO;
import com.zhanglx.sso.log.domain.vo.OperationLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Elasticsearch REST 封装。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OperationLogElasticsearchClient {

    private static final DateTimeFormatter INDEX_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final MediaType NDJSON_MEDIA_TYPE = MediaType.parseMediaType("application/x-ndjson");

    private final OperationLogProperties properties;
    private final ObjectMapper objectMapper;

    private final AtomicInteger clientCursor = new AtomicInteger();
    private final Object templateLock = new Object();

    private volatile boolean templateReady;
    private volatile long lastTemplateCheckAt;
    private volatile List<HostClient> clients;

    public String resolveIndexName(OperationLogDocument document) {
        LocalDate date = document.getEndTime() != null
                ? document.getEndTime().toLocalDate()
                : LocalDate.now();
        return properties.getElasticsearch().getIndexPrefix() + "-" + INDEX_DATE_FORMATTER.format(date);
    }

    public BulkWriteResult bulkWrite(List<OperationLogDocument> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return BulkWriteResult.success();
        }

        List<PreparedBulkItem> preparedItems = prepareBulkItems(documents);
        if (preparedItems.isEmpty()) {
            return BulkWriteResult.success();
        }

        Set<String> indices = new LinkedHashSet<>();
        preparedItems.forEach(item -> indices.add(item.indexName()));
        ensureTemplateAndIndices(indices);

        StringBuilder payload = new StringBuilder(preparedItems.size() * 256);
        for (PreparedBulkItem item : preparedItems) {
            payload.append(item.metaJson()).append('\n');
            payload.append(item.sourceJson()).append('\n');
        }

        String responseBody = executeWithFailover(client -> client.restClient.post()
                .uri("/_bulk?filter_path=errors,items.*.index.status,items.*.index.error")
                .contentType(NDJSON_MEDIA_TYPE)
                .body(payload.toString())
                .retrieve()
                .body(String.class));

        return parseBulkResult(responseBody, preparedItems);
    }

    public OperationLogPageVO pageQuery(OperationLogQueryDTO queryDTO) {
        List<String> indices = resolveSearchIndices(queryDTO);
        if (indices.isEmpty()) {
            return OperationLogPageVO.builder()
                    .records(List.of())
                    .total(0)
                    .current(queryDTO.getPageNum())
                    .size(queryDTO.getPageSize())
                    .build();
        }

        ObjectNode requestBody = buildSearchBody(queryDTO);
        String responseBody = executeWithFailover(client -> client.restClient.post()
                .uri("/" + String.join(",", indices) + "/_search?ignore_unavailable=true&allow_no_indices=true")
                .contentType(MediaType.APPLICATION_JSON)
                .body(writeJson(requestBody))
                .retrieve()
                .body(String.class));
        return parsePageResult(responseBody, queryDTO);
    }

    public OperationLogVO getDetail(String logId, OperationLogQueryDTO queryDTO) {
        if (!StringUtils.hasText(logId)) {
            return null;
        }

        List<String> indices = resolveDetailIndices(queryDTO);
        if (indices.isEmpty()) {
            return null;
        }

        ObjectNode requestBody = objectMapper.createObjectNode();
        requestBody.put("size", 1);
        requestBody.put("track_total_hits", false);
        ArrayNode sort = requestBody.putArray("sort");
        sort.addObject().putObject("endTime").put("order", "desc");
        sort.addObject().putObject("logId").put("order", "desc");
        ObjectNode bool = requestBody.putObject("query").putObject("bool");
        bool.putArray("filter")
                .add(objectMapper.createObjectNode()
                        .set("term", objectMapper.createObjectNode()
                                .set("logId", objectMapper.createObjectNode().put("value", logId))));

        String responseBody = executeWithFailover(client -> client.restClient.post()
                .uri("/" + String.join(",", indices) + "/_search?ignore_unavailable=true&allow_no_indices=true")
                .contentType(MediaType.APPLICATION_JSON)
                .body(writeJson(requestBody))
                .retrieve()
                .body(String.class));

        JsonNode root = readTree(responseBody);
        JsonNode hits = root.path("hits").path("hits");
        if (!hits.isArray() || hits.isEmpty()) {
            return null;
        }
        return convertHit(hits.get(0));
    }

    public void ensureTemplateAndIndices(Set<String> indices) {
        ensureTemplate();
        if (CollectionUtils.isEmpty(indices)) {
            return;
        }
        for (String index : indices) {
            if (!indexExists(index)) {
                createIndex(index);
            }
        }
    }

    private void ensureTemplate() {
        long now = System.currentTimeMillis();
        if (templateReady && now - lastTemplateCheckAt < 300_000L) {
            return;
        }

        synchronized (templateLock) {
            now = System.currentTimeMillis();
            if (templateReady && now - lastTemplateCheckAt < 300_000L) {
                return;
            }
            String templateName = properties.getElasticsearch().getIndexPrefix() + "-template";
            String templateJson = loadTemplateJson().replace("__INDEX_PREFIX__", properties.getElasticsearch().getIndexPrefix());
            executeWithFailover(client -> client.restClient.put()
                    .uri("/_index_template/" + templateName)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(templateJson)
                    .retrieve()
                    .toBodilessEntity());
            templateReady = true;
            lastTemplateCheckAt = now;
        }
    }

    private boolean indexExists(String index) {
        Integer status = executeWithFailover(client -> client.restClient.method(HttpMethod.HEAD)
                .uri("/" + index)
                .exchange((request, response) -> response.getStatusCode().value()));
        return status != null && status == 200;
    }

    private void createIndex(String index) {
        try {
            executeWithFailover(client -> client.restClient.put()
                    .uri("/" + index)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{}")
                    .retrieve()
                    .toBodilessEntity());
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() != 400) {
                throw e;
            }
        }
    }

    private List<PreparedBulkItem> prepareBulkItems(List<OperationLogDocument> documents) {
        List<PreparedBulkItem> preparedItems = new ArrayList<>();
        for (OperationLogDocument document : documents) {
            String indexName = resolveIndexName(document);
            String sourceJson = writeJson(document);
            String metaJson = writeJson(Map.of("index", Map.of("_index", indexName, "_id", document.getLogId())));
            preparedItems.add(new PreparedBulkItem(document, indexName, metaJson, sourceJson));
        }
        return preparedItems;
    }

    private BulkWriteResult parseBulkResult(String responseBody, List<PreparedBulkItem> preparedItems) {
        JsonNode root = readTree(responseBody);
        if (!root.path("errors").asBoolean(false)) {
            return BulkWriteResult.success();
        }

        JsonNode items = root.path("items");
        List<OperationLogDocument> failedDocuments = new ArrayList<>();
        List<String> failureMessages = new ArrayList<>();
        for (int i = 0; i < items.size() && i < preparedItems.size(); i++) {
            JsonNode item = items.get(i).path("index");
            int status = item.path("status").asInt(200);
            if (status >= 300) {
                failedDocuments.add(preparedItems.get(i).document());
                JsonNode error = item.path("error");
                if (!error.isMissingNode()) {
                    failureMessages.add(error.toString());
                }
            }
        }
        return new BulkWriteResult(failedDocuments, String.join(" | ", failureMessages));
    }

    private OperationLogPageVO parsePageResult(String responseBody, OperationLogQueryDTO queryDTO) {
        JsonNode root = readTree(responseBody);
        JsonNode hitsNode = root.path("hits");
        long total = hitsNode.path("total").path("value").asLong(0L);
        List<OperationLogVO> records = new ArrayList<>();
        String nextSearchAfterToken = null;

        JsonNode hits = hitsNode.path("hits");
        if (hits.isArray()) {
            for (JsonNode hit : hits) {
                records.add(convertHit(hit));
            }
            if (!hits.isEmpty()) {
                nextSearchAfterToken = encodeSearchAfter(hits.get(hits.size() - 1).path("sort"));
            }
        }

        return OperationLogPageVO.builder()
                .records(records)
                .total(total)
                .current(queryDTO.getPageNum())
                .size(queryDTO.getPageSize())
                .nextSearchAfterToken(nextSearchAfterToken)
                .build();
    }

    private OperationLogVO convertHit(JsonNode hit) {
        JsonNode source = hit.path("_source");
        return OperationLogVO.builder()
                .logId(text(source, "logId"))
                .appCode(text(source, "appCode"))
                .appName(text(source, "appName"))
                .platformCode(text(source, "platformCode"))
                .platformName(text(source, "platformName"))
                .module(text(source, "module"))
                .feature(text(source, "feature"))
                .operationType(text(source, "operationType"))
                .operationName(text(source, "operationName"))
                .operationDesc(text(source, "operationDesc"))
                .userId(text(source, "userId"))
                .username(text(source, "username"))
                .displayName(text(source, "displayName"))
                .tenantId(text(source, "tenantId"))
                .requestMethod(text(source, "requestMethod"))
                .requestPath(text(source, "requestPath"))
                .requestQuery(text(source, "requestQuery"))
                .requestBodySummary(text(source, "requestBodySummary"))
                .responseSummary(text(source, "responseSummary"))
                .resultStatus(text(source, "resultStatus"))
                .errorCode(text(source, "errorCode"))
                .errorMessageSummary(text(source, "errorMessageSummary"))
                .exceptionType(text(source, "exceptionType"))
                .exceptionStackSummary(text(source, "exceptionStackSummary"))
                .clientIp(text(source, "clientIp"))
                .userAgent(text(source, "userAgent"))
                .traceId(text(source, "traceId"))
                .requestId(text(source, "requestId"))
                .startTime(localDateTime(source, "startTime"))
                .endTime(localDateTime(source, "endTime"))
                .durationMs(longValue(source, "durationMs"))
                .sourceSystem(text(source, "sourceSystem"))
                .ext(convertExt(source.path("ext")))
                .ingestTime(localDateTime(source, "ingestTime"))
                .build();
    }

    private ObjectNode buildSearchBody(OperationLogQueryDTO queryDTO) {
        ObjectNode body = objectMapper.createObjectNode();
        body.put("track_total_hits", true);
        body.put("size", queryDTO.getPageSize());
        String order = "asc".equalsIgnoreCase(queryDTO.getSortOrder()) ? "asc" : "desc";
        if (!StringUtils.hasText(queryDTO.getSearchAfterToken())) {
            int from = Math.max(0, (queryDTO.getPageNum() - 1) * queryDTO.getPageSize());
            body.put("from", from);
        }

        ArrayNode sort = body.putArray("sort");
        sort.addObject().putObject("endTime").put("order", order);
        sort.addObject().putObject("logId").put("order", order);

        if (StringUtils.hasText(queryDTO.getSearchAfterToken())) {
            body.set("search_after", decodeSearchAfter(queryDTO.getSearchAfterToken()));
        }

        ObjectNode bool = body.putObject("query").putObject("bool");
        ArrayNode filter = bool.putArray("filter");
        ArrayNode must = bool.putArray("must");

        addTermFilter(filter, "appCode", queryDTO.getAppCode());
        addTermFilter(filter, "platformCode", queryDTO.getPlatformCode());
        addTermFilter(filter, "module", queryDTO.getModule());
        addTermFilter(filter, "feature", queryDTO.getFeature());
        addTermFilter(filter, "userId", queryDTO.getUserId());
        addTermFilter(filter, "username", queryDTO.getUsername());
        addTermFilter(filter, "operationType", queryDTO.getOperationType());
        addTermFilter(filter, "resultStatus", queryDTO.getResultStatus());
        addTermFilter(filter, "traceId", queryDTO.getTraceId());
        addRangeFilter(filter, "endTime", queryDTO.getStartTime(), queryDTO.getEndTime());

        if (StringUtils.hasText(queryDTO.getKeyword())) {
            ObjectNode multiMatch = objectMapper.createObjectNode();
            multiMatch.put("query", queryDTO.getKeyword().trim());
            ArrayNode fields = multiMatch.putArray("fields");
            fields.add("operationDesc");
            fields.add("requestPath.text");
            must.add(objectMapper.createObjectNode().set("multi_match", multiMatch));
        }
        return body;
    }

    private List<String> resolveSearchIndices(OperationLogQueryDTO queryDTO) {
        LocalDateTime endTime = queryDTO.getEndTime() != null ? queryDTO.getEndTime() : LocalDateTime.now();
        LocalDateTime startTime = queryDTO.getStartTime() != null
                ? queryDTO.getStartTime()
                : endTime.minusDays(Math.max(1, properties.getSearchDefaultDays()));
        if (startTime.isAfter(endTime)) {
            LocalDateTime tmp = startTime;
            startTime = endTime;
            endTime = tmp;
        }

        List<String> indices = new ArrayList<>();
        LocalDate start = startTime.toLocalDate();
        LocalDate end = endTime.toLocalDate();
        for (LocalDate current = start; !current.isAfter(end); current = current.plusDays(1)) {
            indices.add(properties.getElasticsearch().getIndexPrefix() + "-" + INDEX_DATE_FORMATTER.format(current));
        }
        return indices;
    }

    private List<String> resolveDetailIndices(OperationLogQueryDTO queryDTO) {
        if (queryDTO == null || (queryDTO.getStartTime() == null && queryDTO.getEndTime() == null)) {
            return List.of(properties.getElasticsearch().getIndexPrefix() + "-*");
        }
        return resolveSearchIndices(queryDTO);
    }

    private void addTermFilter(ArrayNode filter, String field, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        ObjectNode term = objectMapper.createObjectNode();
        term.put("value", value.trim());
        filter.add(objectMapper.createObjectNode().set("term", objectMapper.createObjectNode().set(field, term)));
    }

    private void addRangeFilter(ArrayNode filter, String field, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null && endTime == null) {
            return;
        }
        ObjectNode range = objectMapper.createObjectNode();
        ObjectNode detail = range.putObject(field);
        if (startTime != null) {
            detail.put("gte", DATE_TIME_FORMATTER.format(startTime));
        }
        if (endTime != null) {
            detail.put("lte", DATE_TIME_FORMATTER.format(endTime));
        }
        filter.add(objectMapper.createObjectNode().set("range", range));
    }

    private String encodeSearchAfter(JsonNode sortNode) {
        if (sortNode == null || !sortNode.isArray() || sortNode.isEmpty()) {
            return null;
        }
        return Base64.getUrlEncoder().encodeToString(writeJson(sortNode).getBytes(StandardCharsets.UTF_8));
    }

    private JsonNode decodeSearchAfter(String token) {
        if (!StringUtils.hasText(token)) {
            return objectMapper.createArrayNode();
        }
        try {
            String json = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalArgumentException("非法 searchAfterToken", e);
        }
    }

    private <T> T executeWithFailover(EsRestCall<T> call) {
        List<HostClient> hostClients = getClients();
        RuntimeException lastException = null;
        int startIndex = Math.floorMod(clientCursor.getAndIncrement(), hostClients.size());
        for (int i = 0; i < hostClients.size(); i++) {
            HostClient hostClient = hostClients.get((startIndex + i) % hostClients.size());
            try {
                return call.execute(hostClient);
            } catch (RestClientResponseException e) {
                int statusCode = e.getStatusCode().value();
                if (statusCode >= 400 && statusCode < 500 && statusCode != 429) {
                    throw e;
                }
                lastException = e;
            } catch (RuntimeException e) {
                lastException = e;
            }
        }
        throw lastException == null ? new IllegalStateException("ES 调用失败") : lastException;
    }

    private List<HostClient> getClients() {
        List<HostClient> current = clients;
        if (!CollectionUtils.isEmpty(current)) {
            return current;
        }
        synchronized (this) {
            current = clients;
            if (!CollectionUtils.isEmpty(current)) {
                return current;
            }
            List<HostClient> initialized = new ArrayList<>();
            for (String host : properties.getElasticsearch().getHosts()) {
                String normalizedHost = normalizeHost(host);
                RestClient restClient = buildRestClient(normalizedHost);
                initialized.add(new HostClient(normalizedHost, restClient));
            }
            if (initialized.isEmpty()) {
                throw new IllegalStateException("未配置 Elasticsearch hosts");
            }
            clients = initialized;
            return initialized;
        }
    }

    private RestClient buildRestClient(String host) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(properties.getElasticsearch().getConnectTimeout())
                .build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.getElasticsearch().getSocketTimeout());

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(URI.create(properties.getElasticsearch().getSchema() + "://" + host).toString())
                .requestFactory(requestFactory)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        if (StringUtils.hasText(properties.getElasticsearch().getUsername())) {
            String token = properties.getElasticsearch().getUsername() + ":" + Optional.ofNullable(properties.getElasticsearch().getPassword()).orElse("");
            String encoded = Base64.getEncoder().encodeToString(token.getBytes(StandardCharsets.UTF_8));
            builder.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encoded);
        }
        return builder.build();
    }

    private String normalizeHost(String host) {
        if (!StringUtils.hasText(host)) {
            throw new IllegalArgumentException("Elasticsearch host 不能为空");
        }
        return host.trim().replace("http://", "").replace("https://", "");
    }

    private String loadTemplateJson() {
        try {
            ClassPathResource resource = new ClassPathResource("es/operation-log-index-template.json");
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException("读取操作日志索引模板失败", e);
        }
    }

    private JsonNode readTree(String json) {
        try {
            return objectMapper.readTree(json);
        } catch (Exception e) {
            throw new IllegalStateException("解析 ES 响应失败", e);
        }
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception e) {
            throw new IllegalStateException("序列化 ES 请求失败", e);
        }
    }

    private String text(JsonNode source, String fieldName) {
        JsonNode node = source.path(fieldName);
        return node.isMissingNode() || node.isNull() ? null : node.asText();
    }

    private Long longValue(JsonNode source, String fieldName) {
        JsonNode node = source.path(fieldName);
        return node.isMissingNode() || node.isNull() ? null : node.asLong();
    }

    private LocalDateTime localDateTime(JsonNode source, String fieldName) {
        String text = text(source, fieldName);
        if (!StringUtils.hasText(text)) {
            return null;
        }
        return LocalDateTime.parse(text, DATE_TIME_FORMATTER);
    }

    private Map<String, String> convertExt(JsonNode extNode) {
        if (extNode == null || extNode.isMissingNode() || extNode.isNull() || !extNode.isObject()) {
            return Collections.emptyMap();
        }
        Map<String, String> ext = new LinkedHashMap<>();
        for (Map.Entry<String, JsonNode> entry : extNode.properties()) {
            ext.put(entry.getKey(), entry.getValue().asText());
        }
        return ext;
    }

    @FunctionalInterface
    private interface EsRestCall<T> {
        T execute(HostClient client);
    }

    private record HostClient(String host, RestClient restClient) {
    }

    private record PreparedBulkItem(OperationLogDocument document, String indexName, String metaJson, String sourceJson) {
    }

    public record BulkWriteResult(List<OperationLogDocument> failedDocuments, String failureSummary) {

        public static BulkWriteResult success() {
            return new BulkWriteResult(List.of(), null);
        }
    }
}
