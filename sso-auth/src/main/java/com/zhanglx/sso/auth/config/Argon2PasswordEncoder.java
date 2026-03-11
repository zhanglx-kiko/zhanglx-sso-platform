package com.zhanglx.sso.auth.config;

import com.password4j.*;
import com.password4j.types.Argon2;
import com.zhanglx.sso.common.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Argon2加密工具类（极简版：删除无价值的缓存逻辑）
 * 核心特性：
 * 1. 基准测试动态参数（适配硬件性能）
 * 2. 验证时解析参数，仅不一致时创建临时实例（兼容历史密码）
 * 3. 支持密码升级判断
 * 4. 异步加密/验证（虚拟线程）
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

    // 成员变量
    private final ReentrantLock initLock = new ReentrantLock();
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();
    // 全局动态参数（基准测试生成）
    private volatile Argon2Function argon2Function;
    // 全局参数缓存（仅缓存自己的参数，避免重复调用getter）
    private volatile Argon2Parameters globalParams;

    // 配置项
    @Value("${security.argon2.max-hash-time-ms:500}")
    private long maxHashTimeMs;
    @Value("${security.argon2.initial-memory:16}")
    private int initialMemory;
    @Value("${security.argon2.parallelism:4}")
    private int parallelism;
    @Value("${security.argon2.output-length:32}")
    private int outputLength;
    @Value("${security.argon2.salt-length:16}")
    private int saltLength;
    @Value("${security.argon2.pepper:}")
    private String pepper;

    /**
     * 初始化：基准测试生成动态参数（适配硬件）
     */
    @PostConstruct
    public void init() {
        if (argon2Function != null) {
            return;
        }
        initLock.lock();
        try {
            if (argon2Function == null) {
                log.info("开始基准测试Argon2最优参数，最大哈希耗时: {}ms，初始内存: {}，并行度: {}，salt长度: {}",
                        maxHashTimeMs, initialMemory, parallelism, saltLength);

                // 校验salt长度
                if (saltLength < MIN_SALT_LENGTH) {
                    log.warn("salt长度({}字节)低于推荐值，自动调整为16字节", saltLength);
                    saltLength = MIN_SALT_LENGTH;
                }

                // 基准测试获取最优参数
                BenchmarkResult<Argon2Function> benchmarkResult = SystemChecker.benchmarkForArgon2(
                        maxHashTimeMs, initialMemory, parallelism, outputLength, ARGON2_TYPE);

                // 初始化全局实例
                if (benchmarkResult.getPrototype() == null) {
                    log.warn("基准测试失败，使用兜底固定参数");
                    argon2Function = Argon2Function.getInstance(16, 3, 4, 32, ARGON2_TYPE);
                } else {
                    argon2Function = benchmarkResult.getPrototype();
                }

                // 缓存全局参数（仅自己用，避免重复调用getter）
                globalParams = new Argon2Parameters(
                        argon2Function.getMemory(),
                        argon2Function.getIterations(),
                        argon2Function.getParallelism()
                );

                log.info("Argon2初始化完成，全局参数：内存={}, 迭代={}, 并行={}",
                        globalParams.memory(), globalParams.iterations(), globalParams.parallelism());
            }
        } catch (Exception e) {
            log.error("Argon2初始化异常，使用兜底参数", e);
            argon2Function = Argon2Function.getInstance(16, 3, 4, 32, ARGON2_TYPE);
            globalParams = new Argon2Parameters(16, 3, 4);
        } finally {
            initLock.unlock();
        }
    }

    /**
     * 核心：加密密码（使用全局动态参数）
     */
    public String encode(CharSequence rawPassword) {
        Assert.notNull(rawPassword, "原始密码不能为空");
        checkInit();

        try {
            // 生成安全salt
            byte[] saltBytes = SaltGenerator.generate(saltLength);
            String saltStr = new String(saltBytes, StandardCharsets.UTF_8);
            // 用全局参数加密
            Hash hashResult = argon2Function.hash(rawPassword, saltStr, pepper);
            return hashResult.getResult();
        } catch (Exception e) {
            log.error("Argon2加密失败", e);
            throw new BusinessException("密码加密失败");
        }
    }

    /**
     * 返回是否匹配
     */
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        Assert.notNull(rawPassword, "原始密码不能为空");
        Assert.notNull(encodedPassword, "加密密码不能为空");
        checkInit();

        try {
            // 直接解析参数（无缓存，耗时可忽略）
            Argon2Parameters cipherParams = parseArgon2Parameters(encodedPassword);
            if (cipherParams == null) {
                log.warn("解析密文参数失败，使用全局参数验证");
                return argon2Function.check(rawPassword, encodedPassword, null, pepper);
            }

            // 对比全局参数和密文参数
            if (globalParams.equals(cipherParams)) {
                // 参数一致：直接用全局实例验证
                return argon2Function.check(rawPassword, encodedPassword, null, pepper);
            } else {
                // 参数不一致：创建临时实例验证（兼容历史密码）
                Argon2Function tempFunction = createTempArgon2Function(cipherParams);
                return tempFunction.check(rawPassword, encodedPassword, null, pepper);
            }
        } catch (Exception e) {
            log.error("密码验证异常", e);
            return false;
        }
    }

    /**
     * 检查密码是否需要升级
     */
    public boolean needUpgrade(String encodedPassword) {
        checkInit();
        Argon2Parameters cipherParams = parseArgon2Parameters(encodedPassword);
        return cipherParams != null && !globalParams.equals(cipherParams);
    }

    /**
     * 异步加密（虚拟线程）
     */
    public Future<String> encodeAsync(CharSequence rawPassword) {
        return virtualThreadExecutor.submit(() -> encode(rawPassword));
    }

    /**
     * 异步验证（虚拟线程）
     */
    public Future<Boolean> matchesAsync(CharSequence rawPassword, String encodedPassword) {
        return virtualThreadExecutor.submit(() -> matches(rawPassword, encodedPassword));
    }

    /**
     * 带超时的异步加密
     */
    public String encodeAsyncWithTimeout(CharSequence rawPassword) {
        try {
            return encodeAsync(rawPassword).get(ASYNC_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new BusinessException("密码加密超时");
        } catch (Exception e) {
            throw new BusinessException("密码加密失败");
        }
    }

    /**
     * 带超时的异步验证
     */
    public boolean matchesAsyncWithTimeout(CharSequence rawPassword, String encodedPassword) {
        try {
            return matchesAsync(rawPassword, encodedPassword).get(ASYNC_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw new BusinessException("密码验证超时");
        } catch (Exception e) {
            return false;
        }
    }

    // ========== 私有核心方法 ==========
    /**
     * 解析密文中的Argon2参数（m/t/p）
     */
    private Argon2Parameters parseArgon2Parameters(String encodedPassword) {
        try {
            String[] parts = encodedPassword.split("\\$");
            if (parts.length < 4) {
                log.warn("密文格式错误：{}", encodedPassword);
                return null;
            }

            // 匹配参数部分（如：m=16,t=3,p=4）
            Matcher matcher = ARGON2_PARAM_PATTERN.matcher(parts[3]);
            if (!matcher.find()) {
                log.warn("未匹配到Argon2参数：{}", parts[3]);
                return null;
            }

            // 解析参数
            int memory = Integer.parseInt(matcher.group(1));
            int iterations = Integer.parseInt(matcher.group(2));
            int parallelism = Integer.parseInt(matcher.group(3));

            return new Argon2Parameters(memory, iterations, parallelism);
        } catch (Exception e) {
            log.error("解析Argon2参数异常", e);
            return null;
        }
    }

    /**
     * 创建临时Argon2实例（仅参数不一致时调用）
     */
    private Argon2Function createTempArgon2Function(Argon2Parameters params) {
        try {
            return Argon2Function.getInstance(
                    params.memory(),
                    params.iterations(),
                    params.parallelism(),
                    outputLength,
                    ARGON2_TYPE
            );
        } catch (Exception e) {
            log.error("创建临时Argon2实例失败，使用全局参数兜底", e);
            return argon2Function;
        }
    }

    /**
     * 检查初始化状态
     */
    private void checkInit() {
        if (argon2Function == null) {
            initLock.lock();
            try {
                if (argon2Function == null) {
                    throw new BusinessException("Argon2未初始化完成");
                }
            } finally {
                initLock.unlock();
            }
        }
    }

    /**
     * 优雅关闭资源
     */
    @PreDestroy
    public void destroy() {
        log.info("关闭Argon2加密工具类资源");

        // 关闭虚拟线程池
        virtualThreadExecutor.shutdown();
        try {
            if (!virtualThreadExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                virtualThreadExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            virtualThreadExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        log.info("Argon2加密工具类资源已全部关闭");
    }

    // ========== 内部参数封装类 ==========
    private record Argon2Parameters(int memory, int iterations, int parallelism) {
        @Override
        public String toString() {
            return String.format("m=%d,t=%d,p=%d", memory, iterations, parallelism);
        }
    }

}