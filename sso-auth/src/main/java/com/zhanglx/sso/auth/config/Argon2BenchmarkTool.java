package com.zhanglx.sso.auth.config;

import com.password4j.Argon2Function;
import com.password4j.BenchmarkResult;
import com.password4j.SystemChecker;
import com.password4j.types.Argon2;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/24 17:20
 * 类名：Argon2BenchmarkTool
 * 说明：Argon2 硬件性能基准测试工具（独立运行）
 * 用途：在与生产环境同等规格的机器（或容器）上运行此工具，
 * 获取最适合当前硬件的加密参数，并填入 application-prod.yml。
 */
public class Argon2BenchmarkTool {

    static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("        Argon2 硬件性能基准测试工具启动           ");
        System.out.println("==================================================");
        System.out.println("【⚠️ 警告】请务必在与生产环境配置完全一致的服务器/容器规格下运行此工具！\n");

        // ------------------ 目标参数设定 ------------------
        // 你希望单次密码校验最多消耗多少毫秒？（通常 SSO 登录接口建议 300ms - 500ms）
        long targetMaxHashTimeMs = 500L;

        // 初始内存探索基线 (单位：KiB)。
        // OWASP 推荐最小 15 MiB (15360 KiB)。这里设为 64 MiB (65536 KiB) 起步。
        int initialMemory = 65536;

        // 并行度：建议等于分配给该容器的 CPU 核心数，或核心数的一半
        int parallelism = Runtime.getRuntime().availableProcessors() > 2 ?
                Runtime.getRuntime().availableProcessors() / 2 : 2;

        int outputLength = 32;

        System.out.printf("\uD83D\uDC49 目标最大耗时: %d ms%n", targetMaxHashTimeMs);
        System.out.printf("\uD83D\uDC49 初始内存基线: %d KiB (约 %d MB)%n", initialMemory, initialMemory / 1024);
        System.out.printf("\uD83D\uDC49 测试并行度: %d%n", parallelism);

        // ------------------ 1. 预热 JIT 编译器 ------------------
        System.out.println("\n[1/2] 正在预热 JIT 编译器 (模拟服务冷启动结束后的真实满血性能)...");
        Argon2Function warmupFunc = Argon2Function.getInstance(4096, 3, 2, 32, Argon2.ID);
        for (int i = 0; i < 50; i++) {
            warmupFunc.hash("warmup_password_test", "warmup_salt_123456");
        }
        System.out.println("✅ JIT 预热完成！");

        // ------------------ 2. 正式基准测试 ------------------
        System.out.println("\n[2/2] 正在施压寻找最优参数组合 (这可能需要几十秒时间，请耐心等待)...");

        long startTime = System.currentTimeMillis();
        BenchmarkResult<Argon2Function> result = SystemChecker.benchmarkForArgon2(
                targetMaxHashTimeMs,
                initialMemory,
                parallelism,
                outputLength,
                Argon2.ID
        );
        long costTime = System.currentTimeMillis() - startTime;

        Argon2Function optimalParams = result.getPrototype();

        if (optimalParams != null) {
            System.out.println("\n🎉 测试完成！总耗时: " + costTime + " ms");
            System.out.println("⏱️ 测出的单次加密真实耗时估算: " + result.getElapsed() + " ms");

            System.out.println("\n👇 请将以下 YAML 配置复制到您的 application-prod.yml 或 Nacos 配置中心 👇");
            System.out.println("--------------------------------------------------");
            System.out.println("security:");
            System.out.println("  argon2:");
            System.out.println("    memory: " + optimalParams.getMemory() + "       # 内存消耗 (KiB)");
            System.out.println("    iterations: " + optimalParams.getIterations() + "   # 迭代次数 (控制 CPU 消耗)");
            System.out.println("    parallelism: " + optimalParams.getParallelism() + "  # 并行度");
            System.out.println("    output-length: 32");
            System.out.println("    salt-length: 16");
            System.out.println("--------------------------------------------------");
        } else {
            System.out.println("\n❌ 基准测试失败！请检查系统资源是否被完全占满。");
        }
    }

}