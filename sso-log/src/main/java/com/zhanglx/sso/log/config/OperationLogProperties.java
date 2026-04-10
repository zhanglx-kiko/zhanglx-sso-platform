package com.zhanglx.sso.log.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * 操作日志配置。
 * 这里把高并发场景下需要调优的阈值都显式暴露出来，避免写死后线上难以止血。
 */
@Data
@ConfigurationProperties(prefix = "sso.log.operation")
public class OperationLogProperties {

    /**
     * 是否开启操作日志能力。
     * 关闭后切面仍可加载，但会直接短路，避免 ES 故障时影响应用启动。
     */
    private boolean enableOperationLog = false;
    /**
     * 应用编码。
     */
    private String appCode = "sso";
    /**
     * 应用名称。
     */
    private String appName = "SSO认证中心";
    /**
     * 平台编码。
     */
    private String platformCode = "auth-admin";
    /**
     * 平台名称。
     */
    private String platformName = "认证后台";
    /**
     * 来源系统。
     */
    private String sourceSystem = "sso-auth";
    /**
     * 请求体摘要最大长度。
     */
    private int requestBodyMaxLength = 2048;
    /**
     * 响应摘要最大长度。
     */
    private int responseSummaryMaxLength = 2048;
    /**
     * 错误信息最大长度。
     */
    private int errorMessageMaxLength = 1024;
    /**
     * 异常堆栈最大长度。
     */
    private int exceptionStackMaxLength = 4096;
    /**
     * 请求查询串最大长度。
     */
    private int requestQueryMaxLength = 1024;
    /**
     * 用户代理最大长度。
     */
    private int userAgentMaxLength = 512;
    /**
     * 扩展字段最大条数。
     */
    private int extMaxEntries = 16;
    /**
     * 扩展字段键最大长度。
     */
    private int extKeyMaxLength = 64;
    /**
     * 扩展字段值最大长度。
     */
    private int extValueMaxLength = 256;
    /**
     * 集合预览数量。
     */
    private int collectionPreviewSize = 10;
    /**
     * Bean 属性预览数量。
     */
    private int beanPropertyPreviewSize = 20;
    /**
     * 默认搜索天数。
     */
    private int searchDefaultDays = 7;
    /**
     * 搜索最大窗口。
     */
    private int searchMaxWindow = 10000;
    /**
     * Elasticsearch 连接配置。
     */
    private ElasticsearchProperties elasticsearch = new ElasticsearchProperties();
    /**
     * 异步投递配置。
     */
    private AsyncProperties async = new AsyncProperties();

    @Data
    public static class ElasticsearchProperties {
        /**
         * Elasticsearch 地址列表。
         */
        private List<String> hosts = new ArrayList<>(List.of("127.0.0.1:9200"));
        /**
         * 访问协议。
         */
        private String schema = "http";
        /**
         * 用户名。
         */
        private String username;
        /**
         * 密码。
         */
        private String password;
        /**
         * 连接超时时间。
         */
        private Duration connectTimeout = Duration.ofSeconds(2);
        /**
         * 套接字超时时间。
         */
        private Duration socketTimeout = Duration.ofSeconds(5);
        /**
         * 索引前缀。
         */
        private String indexPrefix = "operation-log";
        /**
         * 单批写入数量。
         */
        private int bulkBatchSize = 200;
        /**
         * 批量刷新间隔毫秒数。
         */
        private long bulkFlushIntervalMs = 1000L;

        /**
         * 单次 bulk 请求的估算上限，避免把一个超大的批次一次性打到 ES。
         */
        private int bulkMaxBytes = 1024 * 1024;

        /**
         * 有限重试次数，超过后直接丢弃并打内部告警日志，禁止无限堆积。
         */
        private int maxRetryCount = 1;
    }

    @Data
    public static class AsyncProperties {
        /**
         * 核心线程数。
         */
        private int corePoolSize = 1;
        /**
         * 最大线程数。
         */
        private int maxPoolSize = 2;

        /**
         * 业务线程投递到日志系统的有界队列容量。
         */
        private int queueCapacity = 5000;
        /**
         * 线程空闲存活秒数。
         */
        private int keepAliveSeconds = 60;

        /**
         * 队列打满时的处理策略：
         * DROP_CURRENT 直接丢当前日志；
         * DROP_OLDEST 先淘汰最旧的一条，再尝试写入当前日志。
         */
        private String rejectionPolicy = "DROP_CURRENT";
    }
}
