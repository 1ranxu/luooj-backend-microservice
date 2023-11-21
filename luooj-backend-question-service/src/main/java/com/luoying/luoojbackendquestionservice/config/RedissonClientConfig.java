package com.luoying.luoojbackendquestionservice.config;

import org.redisson.Redisson;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonClientConfig {
    @Bean
    public RRateLimiter rateLimiter() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://127.0.0.1:6379").setDatabase(1).setPassword("123");
        RedissonClient redissonClient = Redisson.create(config);

        String key = "myRateLimiter";
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        // 每五秒产生三个令牌
        rateLimiter.setRate(RateType.OVERALL, 3, 5, RateIntervalUnit.SECONDS);
        return rateLimiter;
    }
}
