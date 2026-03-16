package com.zhanglx.sso.mybatis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: Zhang L X
 * @Create: 2026/2/26 15:46
 * @ClassName: MybatisAutoConfig
 * @Description:
 */
@Configuration
@ComponentScan(basePackages = "com.zhanglx.sso.mybatis")
@MapperScan("com.baomidou.mybatisplus.samples.quickstart.mapper")
public class MybatisAutoConfig {
}
