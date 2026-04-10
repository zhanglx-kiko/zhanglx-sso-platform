package com.zhanglx.sso.mybatis;

import com.zhanglx.sso.mybatis.config.MybatisAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * 作者：Zhang L X
 * 创建时间：2026/2/26 15:46
 * 类名：MybatisAutoConfig
 * 说明：
 */
@Configuration
@Import(MybatisAutoConfiguration.class)
public class MybatisAutoConfig {
}