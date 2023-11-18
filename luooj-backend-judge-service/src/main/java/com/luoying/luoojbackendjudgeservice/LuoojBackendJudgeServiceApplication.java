package com.luoying.luoojbackendjudgeservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableAspectJAutoProxy(proxyTargetClass = true, exposeProxy = true)
@ComponentScan("com.luoying")
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {"com.luoying.luoojbackendserviceclient.service"})
public class LuoojBackendJudgeServiceApplication {

    public static void main(String[] args) {
        // 初始化交换机和消息队列
        InitRabbitMq.doInit();
        SpringApplication.run(LuoojBackendJudgeServiceApplication.class, args);
    }

}
