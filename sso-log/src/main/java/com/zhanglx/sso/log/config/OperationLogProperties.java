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

    private String appCode = "sso";

    private String appName = "SSO认证中心";

    private String platformCode = "auth-admin";

    private String platformName = "认证后台";

    private String sourceSystem = "sso-auth";

    private int requestBodyMaxLength = 2048;

    private int responseSummaryMaxLength = 2048;

    private int errorMessageMaxLength = 1024;

    private int exceptionStackMaxLength = 4096;

    private int requestQueryMaxLength = 1024;

    private int userAgentMaxLength = 512;

    private int extMaxEntries = 16;

    private int extKeyMaxLength = 64;

    private int extValueMaxLength = 256;

    private int collectionPreviewSize = 10;

    private int beanPropertyPreviewSize = 20;

    private int searchDefaultDays = 7;

    private int searchMaxWindow = 10000;

    private ElasticsearchProperties elasticsearch = new ElasticsearchProperties();

    private AsyncProperties async = new AsyncProperties();

    @Data
    public static class ElasticsearchProperties {

        private List<String> hosts = new ArrayList<>(List.of("127.0.0.1:9200"));

        private String schema = "http";

        private String username;

        private String password;

        private Duration connectTimeout = Duration.ofSeconds(2);

        private Duration socketTimeout = Duration.ofSeconds(5);

        private String indexPrefix = "operation-log";

        private int bulkBatchSize = 200;

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

        private int corePoolSize = 1;

        private int maxPoolSize = 2;

        /**
         * 业务线程投递到日志系统的有界队列容量。
         */
        private int queueCapacity = 5000;

        private int keepAliveSeconds = 60;

        /**
         * 队列打满时的处理策略：
         * DROP_CURRENT 直接丢当前日志；
         * DROP_OLDEST 先淘汰最旧的一条，再尝试写入当前日志。
         */
        private String rejectionPolicy = "DROP_CURRENT";
    }
}
