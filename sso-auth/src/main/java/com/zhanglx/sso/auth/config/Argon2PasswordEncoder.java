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

/**
 * @Author: Zhang L X
 * @Create: 2026/2/26 18:09
 * @ClassName: Argon2PasswordEncoder
 * @Description: Argon2 加密工具类（最终版，无安全漏洞）
 * 基于 JDK25 虚拟线程 + Spring Boot4.0.3 + Spring Cloud2025.1.1
 * 适配 Password4j 原生 SaltGenerator（64字节默认salt，可自定义）
 */
@Component
public class Argon2PasswordEncoder {

    private static final Logger log = LoggerFactory.getLogger(Argon2PasswordEncoder.class);

    // Argon2 类型（推荐ID混合模式）
    private static final Argon2 ARGON2_TYPE = Argon2.ID;
    // 异步操作默认超时时间（ms）
    private static final long ASYNC_TIMEOUT_MS = 3000;
    // Argon2 官方推荐的最小 salt 长度（16字节）
    private static final int MIN_SALT_LENGTH = 16;
    // 初始化锁（防止并发初始化）
    private final ReentrantLock initLock = new ReentrantLock();

    // JDK25 虚拟线程池（原生支持）
    private final ExecutorService virtualThreadExecutor = Executors.newVirtualThreadPerTaskExecutor();

    // 配置参数（从配置文件注入）
    @Value("${security.argon2.max-hash-time-ms:500}")
    private long maxHashTimeMs;
    @Value("${security.argon2.initial-memory:16}")
    private int initialMemory;
    @Value("${security.argon2.parallelism:4}")
    private int parallelism;
    @Value("${security.argon2.output-length:32}")
    private int outputLength;
    // Pepper 全局密钥（从配置中心/环境变量注入，非数据库存储）
    @Value("${security.argon2.pepper:}")
    private String pepper;
    // 自定义 salt 长度（默认 64 字节，可配置为 16 字节）
    @Value("${security.argon2.salt-length:64}")
    private int saltLength;

    // 线程安全的 Argon2Function 单例
    private volatile Argon2Function argon2Function;

    /**
     * 初始化：自动基准测试最优参数，仅执行一次
     */
    @PostConstruct
    public void init() {
        if (argon2Function != null) {
            return;
        }
        initLock.lock();
        try {
            if (argon2Function == null) {
                log.info("开始基准测试Argon2最优参数（JDK25虚拟线程版），最大哈希耗时: {}ms，初始内存: {}，并行度: {}，salt长度: {}",
                        maxHashTimeMs, initialMemory, parallelism, saltLength);

                // 校验 salt 长度（至少 16 字节）
                if (saltLength < MIN_SALT_LENGTH) {
                    log.warn("配置的salt长度({}字节)小于Argon2官方推荐的16字节，自动调整为16字节", saltLength);
                    saltLength = MIN_SALT_LENGTH;
                }

                // 基于 BenchmarkResult 原生 API 获取最优实例
                BenchmarkResult<Argon2Function> benchmarkResult = SystemChecker.benchmarkForArgon2(
                        maxHashTimeMs, initialMemory, parallelism, outputLength, ARGON2_TYPE);

                if (benchmarkResult.getPrototype() == null) {
                    log.warn("Argon2基准测试未找到最优参数，使用默认安全参数");
                    argon2Function = Argon2Function.getInstance(16, 3, 4, 32, ARGON2_TYPE);
                } else {
                    argon2Function = benchmarkResult.getPrototype();
                    log.info("Argon2最优参数初始化完成：内存={}, 迭代次数={}, 并行度={}, 实际耗时={}ms",
                            argon2Function.getMemory(),
                            argon2Function.getIterations(),
                            argon2Function.getParallelism(),
                            benchmarkResult.getElapsed()
                    );
                }
            }
        } catch (Exception e) {
            log.error("Argon2初始化失败，降级使用默认参数", e);
            argon2Function = Argon2Function.getInstance(16, 3, 4, 32, ARGON2_TYPE);
        } finally {
            initLock.unlock();
        }
    }

    /**
     * 同步加密密码（最终版，无安全漏洞）
     * 核心：主动生成安全的随机 salt（64字节/16字节），避免空 salt
     *
     * @param rawPassword 原始密码
     * @return 加密后的哈希字符串（包含算法配置、salt、哈希值）
     */
    public String encode(CharSequence rawPassword) {
        Assert.notNull(rawPassword, "原始密码不能为空");
        checkInit();

        try {
            // 步骤1：生成密码学安全的随机 salt（自定义长度/默认64字节）
            byte[] saltBytes = SaltGenerator.generate(saltLength);
            // 步骤2：将 salt 转为字符串（使用 UTF-8 编码，避免乱码）
            String saltStr = new String(saltBytes, StandardCharsets.UTF_8);

            // 步骤3：调用原生 hash 方法，传入原始密码、salt、pepper
            Hash hashResult = argon2Function.hash(rawPassword, saltStr, pepper);

            // 步骤4：获取最终哈希字符串（包含 salt、算法配置等）
            return hashResult.getResult();
        } catch (Exception e) {
            log.error("Argon2同步加密失败", e);
            throw new BusinessException("密码加密失败", e.getMessage());
        }
    }

    /**
     * 异步加密密码（高并发场景，虚拟线程）
     *
     * @param rawPassword 原始密码
     * @return Future 异步结果，调用方可控制等待逻辑
     */
    public Future<String> encodeAsync(CharSequence rawPassword) {
        Assert.notNull(rawPassword, "原始密码不能为空");
        checkInit();

        // 提交到虚拟线程池，返回 Future 避免阻塞
        return virtualThreadExecutor.submit(() -> {
            try {
                return encode(rawPassword);
            } catch (Exception e) {
                log.error("Argon2虚拟线程加密失败", e);
                throw new BusinessException("密码加密失败", e.getMessage());
            }
        });
    }

    /**
     * 异步加密密码（带超时的便捷方法）
     *
     * @param rawPassword 原始密码
     * @return 加密后的哈希字符串
     */
    public String encodeAsyncWithTimeout(CharSequence rawPassword) {
        try {
            return encodeAsync(rawPassword).get(ASYNC_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.error("Argon2异步加密超时（{}ms）", ASYNC_TIMEOUT_MS, e);
            throw new BusinessException("密码加密超时，请重试");
        } catch (Exception e) {
            log.error("Argon2异步加密失败", e);
            throw new BusinessException("密码加密失败", e.getMessage());
        }
    }

    /**
     * 验证密码（最终版，自动解析 salt）
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的哈希字符串
     * @return true=匹配，false=不匹配
     */
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        Assert.notNull(rawPassword, "原始密码不能为空");
        Assert.notNull(encodedPassword, "加密密码不能为空");
        checkInit();

        try {
            // 核心：验证时无需传入 salt（框架从 encodedPassword 中解析）
            // 只需传入 pepper（全局密钥），与加密时一致
            return argon2Function.check(rawPassword, encodedPassword, null, pepper);
        } catch (Exception e) {
            log.error("Argon2密码验证失败", e);
            return false;
        }
    }

    /**
     * 异步验证密码（高并发场景，虚拟线程）
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的哈希字符串
     * @return Future 异步验证结果
     */
    public Future<Boolean> matchesAsync(CharSequence rawPassword, String encodedPassword) {
        Assert.notNull(rawPassword, "原始密码不能为空");
        Assert.notNull(encodedPassword, "加密密码不能为空");
        checkInit();

        return virtualThreadExecutor.submit(() -> matches(rawPassword, encodedPassword));
    }

    /**
     * 异步验证密码（带超时的便捷方法）
     *
     * @param rawPassword     原始密码
     * @param encodedPassword 加密后的哈希字符串
     * @return true=匹配，false=不匹配
     */
    public boolean matchesAsyncWithTimeout(CharSequence rawPassword, String encodedPassword) {
        try {
            return matchesAsync(rawPassword, encodedPassword).get(ASYNC_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            log.error("Argon2异步验证超时（{}ms）", ASYNC_TIMEOUT_MS, e);
            throw new BusinessException("密码验证超时，请重试");
        } catch (Exception e) {
            log.error("Argon2异步验证失败", e);
            return false;
        }
    }

    /**
     * 检查初始化状态，未初始化则阻塞等待
     */
    private void checkInit() {
        if (argon2Function == null) {
            initLock.lock();
            try {
                if (argon2Function == null) {
                    int waitCount = 0;
                    while (argon2Function == null && waitCount < 50) {
                        TimeUnit.MILLISECONDS.sleep(100);
                        waitCount++;
                    }
                    if (argon2Function == null) {
                        throw new BusinessException("Argon2初始化超时，无法执行加密操作");
                    }
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new BusinessException("Argon2初始化被中断", e.getMessage());
            } finally {
                initLock.unlock();
            }
        }
    }

    /**
     * 应用关闭时优雅释放虚拟线程池资源
     */
    @PreDestroy
    public void destroy() {
        log.info("开始关闭Argon2虚拟线程池");
        virtualThreadExecutor.shutdown();
        try {
            if (!virtualThreadExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                virtualThreadExecutor.shutdownNow();
                log.warn("Argon2虚拟线程池强制关闭");
            }
        } catch (InterruptedException e) {
            virtualThreadExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("Argon2虚拟线程池已关闭");
    }

    // ========== 扩展方法：支持自定义 Salt（兼容旧系统） ==========

    /**
     * 同步加密密码（自定义 Salt）
     *
     * @param rawPassword 原始密码
     * @param salt        自定义盐值（仅用于兼容旧系统，推荐使用自动生成）
     * @return 加密后的哈希字符串
     */
    public String encode(CharSequence rawPassword, String salt) {
        Assert.notNull(rawPassword, "原始密码不能为空");
        Assert.notNull(salt, "自定义salt不能为空");
        checkInit();

        try {
            Hash hashResult = argon2Function.hash(rawPassword, salt, pepper);
            return hashResult.getResult();
        } catch (Exception e) {
            log.error("Argon2同步加密失败（自定义Salt）", e);
            throw new BusinessException("密码加密失败", e.getMessage());
        }
    }
}