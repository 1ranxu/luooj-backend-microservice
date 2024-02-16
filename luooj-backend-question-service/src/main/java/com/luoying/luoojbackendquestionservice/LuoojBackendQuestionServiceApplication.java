package com.luoying.luoojbackendquestionservice;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author 落樱的悔恨
 */
@SpringBootApplication
@MapperScan("com.luoying.luoojbackendquestionservice.mapper")
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.luoying")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.luoying.luoojbackendserviceclient.service"})
public class LuoojBackendQuestionServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(LuoojBackendQuestionServiceApplication.class, args);
    }

}
