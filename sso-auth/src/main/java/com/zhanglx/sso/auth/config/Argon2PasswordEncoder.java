package com.zhanglx.sso.auth.config;

import com.password4j.Argon2Function;
import com.password4j.Hash;
import com.password4j.SaltGenerator;
import com.password4j.types.Argon2;
import com.zhanglx.sso.core.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author: Zhang L X
 * @Description: Argon2加密工具类（企业多节点容器化版本）
 * 核心特性：
 * 1. 静态参数注入（保障多节点加密强度绝对一致）
 * 2. 验证时解析参数，仅不一致时创建临时实例（兼容历史弱密码）
 * 3. 支持密码升级判断
 * 4. 舱壁模式（Bulkhead）：使用独立的平台线程池隔离 CPU 密集型计算，防止虚拟线程/主业务线程被拖垮
 */
@Component
public class Argon2PasswordEncoder {
    private static final Logger log = LoggerFactory.getLogger(Argon2PasswordEncoder.class);

    // 核心常量
    private static final Argon2 ARGON2_TYPE = Argon2.ID;
    private static final long ASYNC_TIMEOUT_MS = 3000;
    private static final int MIN_SALT_LENGTH = 16;
    // Argon2参数解析正则（精准匹配密文中的m/t/p）
    private static final Pattern ARGON2_PARAM_PATTERN = Pattern.compile("m=(\\d+),t=(\\d+),p=(\\d+)");

    // 全局静态参数实例
    /**
     * 全局复用的 Argon2 计算实例。
     */
    private Argon2Function argon2Function;

    /**
     * 当前节点加载后的全局 Argon2 参数快照。
     */
    private Argon2Parameters globalParams;

    // CPU 密集型任务专用的受限平台线程池
    /**
     * 密码计算专用线程池。
     */
    private ExecutorService cryptoExecutor;

    // 配置项（从配置中心或 application.yml 读取，确保集群一致）
    /**
     * Argon2 内存成本参数。
     */
    @Value("${security.argon2.memory:65536}")
    private int memory;

    /**
     * Argon2 迭代次数。
     */
    @Value("${security.argon2.iterations:3}")
    private int iterations;

    /**
     * Argon2 并行度参数。
     */
    @Value("${security.argon2.parallelism:4}")
    private int parallelism;

    /**
     * Argon2 输出长度。
     */
    @Value("${security.argon2.output-length:32}")
    private int outputLength;

    /**
     * Salt 长度。
     */
    @Value("${security.argon2.salt-length:16}")
    private int saltLength;

    /**
     * 额外的 Pepper 配置。
     */
    @Value("${security.argon2.pepper:}")
    private String pepper;

    /**
     * 初始化：加载静态配置并初始化舱壁线程池
     */
    @PostConstruct
    public void init() {
        // 1. 校验并调整 Salt 长度
        if (saltLength < MIN_SALT_LENGTH) {
            log.warn("salt长度({}字节)低于推荐值，自动调整为16字节", saltLength);
            saltLength = MIN_SALT_LENGTH;
        }

        // 2. 初始化全局 Argon2 实例（多节点参数完全一致）
        argon2Function = Argon2Function.getInstance(memory, iterations, parallelism, outputLength, ARGON2_TYPE);
        globalParams = new Argon2Parameters(memory, iterations, parallelism);
        log.info("Argon2 初始化完成，全局统一参数：内存={}, 迭代={}, 并行={}", memory, iterations, parallelism);

        // 3. 初始化专用的平台线程池（舱壁模式隔离）
        // 核心：最大线程数设为 CPU 核心数的一半（最少2个），防止恶意并发打满所有 CPU
        int cpuCores = Runtime.getRuntime().availableProcessors();
        int poolSize = Math.max(2, cpuCores / 2);

        cryptoExecutor = Executors.newFixedThreadPool(poolSize, runnable -> {
            Thread thread = new Thread(runnable);
            thread.setName("Argon2-Crypto-Thread-" + thread.getId());
            // 必须是平台守护线程，决不能用虚拟线程
            thread.setDaemon(true);
            return thread;
        });
        log.info("Argon2 专属 CPU 计算隔离线程池初始化完成，池大小: {}", poolSize);
    }

    /**
     * 加密密码（同步）
     */
    /**
     * 加密密码（同步）
     */
    public String encode(CharSequence rawPassword) {
        Assert.notNull(rawPassword, "original.password.cannot.be.blank");
        try {
            // 1. 生成纯净的随机二进制盐
            byte[] saltBytes = SaltGenerator.generate(saltLength);

            // 2. 将密码也转为安全的 byte[]，避免 String 常驻内存池
            byte[] passwordBytes = rawPassword.toString().getBytes(StandardCharsets.UTF_8);

            // 3. 直接调用全 byte[] 参数的底层重载方法！
            Hash hashResult = argon2Function.hash(passwordBytes, saltBytes, pepper);

            // 4. 用完立刻清空内存中的密码字节数组（增强安全性）
            Arrays.fill(passwordBytes, (byte) 0);

            return hashResult.getResult();
        } catch (Exception e) {
            log.error("Argon2加密失败", e);
            throw BusinessException.internalError("password.encryption.failed");
        }
    }

    /**
     * 验证密码（同步）
     */
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        Assert.notNull(rawPassword, "original.password.cannot.be.blank");
        Assert.notNull(encodedPassword, "encrypted.password.cannot.be.blank");

        try {
            Argon2Parameters cipherParams = parseArgon2Parameters(encodedPassword);
            if (cipherParams == null || globalParams.equals(cipherParams)) {
                // 参数一致或解析失败：直接用全局实例验证
                return argon2Function.check(rawPassword, encodedPassword, null, pepper);
            } else {
                // 参数不一致：临时创建实例验证（平滑兼容历史旧参数生成的密码）
                Argon2Function tempFunction = createTempArgon2Function(cipherParams);
                return tempFunction.check(rawPassword, encodedPassword, null, pepper);
            }
        } catch (Exception e) {
            log.error("密码验证异常", e);
            return false;
        }
    }

    /**
     * 检查密码是否需要升级（例如历史旧系统迁移过来的弱哈希）
     */
    public boolean needUpgrade(String encodedPassword) {
        Argon2Parameters cipherParams = parseArgon2Parameters(encodedPassword);
        return cipherParams != null && !globalParams.equals(cipherParams);
    }

    /**
     * 异步加密（提交给隔离的平台线程池）
     */
    public Future<String> encodeAsync(CharSequence rawPassword) {
        return cryptoExecutor.submit(() -> encode(rawPassword));
    }

    /**
     * 异步验证（提交给隔离的平台线程池）
     */
    public Future<Boolean> matchesAsync(CharSequence rawPassword, String encodedPassword) {
        return cryptoExecutor.submit(() -> matches(rawPassword, encodedPassword));
    }

    /**
     * 带超时的异步加密
     */
    public String encodeAsyncWithTimeout(CharSequence rawPassword) {
        try {
            return encodeAsync(rawPassword).get(ASYNC_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw BusinessException.internalError("password.encryption.timed.out");
        } catch (Exception e) {
            throw BusinessException.internalError("password.encryption.failed");
        }
    }

    /**
     * 带超时的异步验证
     */
    public boolean matchesAsyncWithTimeout(CharSequence rawPassword, String encodedPassword) {
        try {
            return matchesAsync(rawPassword, encodedPassword).get(ASYNC_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw BusinessException.internalError("password.verification.timed.out");
        } catch (Exception e) {
            return false;
        }
    }

    // ========== 私有核心方法 ==========

/**
 * 解析 Argon2 编码结果中的参数。
 */
    private Argon2Parameters parseArgon2Parameters(String encodedPassword) {
        try {
            String[] parts = encodedPassword.split("\\$");
            if (parts.length < 4) {
                return null;
            }
            Matcher matcher = ARGON2_PARAM_PATTERN.matcher(parts[3]);
            if (!matcher.find()) {
                return null;
            }
            return new Argon2Parameters(
                    Integer.parseInt(matcher.group(1)),
                    Integer.parseInt(matcher.group(2)),
                    Integer.parseInt(matcher.group(3))
            );
        } catch (Exception e) {
            log.error("解析Argon2参数异常", e);
            return null;
        }
    }

/**
 * 根据参数创建临时 Argon2 计算器。
 */
    private Argon2Function createTempArgon2Function(Argon2Parameters params) {
        try {
            return Argon2Function.getInstance(params.memory(), params.iterations(), params.parallelism(), outputLength, ARGON2_TYPE);
        } catch (Exception e) {
            log.error("创建临时Argon2实例失败，使用全局参数兜底", e);
            return argon2Function;
        }
    }

    /**
     * 优雅关闭资源
     */
    @PreDestroy
    public void destroy() {
        log.info("关闭 Argon2 加密工具类平台线程池");
        if (cryptoExecutor != null) {
            cryptoExecutor.shutdown();
            try {
                if (!cryptoExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    cryptoExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                cryptoExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    // ========== 内部参数封装类 ==========
    private record Argon2Parameters(int memory, int iterations, int parallelism) {
        @Override
        public String toString() {
            return String.format("m=%d,t=%d,p=%d", memory, iterations, parallelism);
        }
    }
}
