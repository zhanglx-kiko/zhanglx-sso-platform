package com.zhanglx.sso.mybatis;

import com.zhanglx.sso.mybatis.config.MybatisAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/26 15:46
 * @ClassName: MybatisAutoConfig
 * @Description:
 */
@Configuration
@Import(MybatisAutoConfiguration.class)
public class MybatisAutoConfig {
}
