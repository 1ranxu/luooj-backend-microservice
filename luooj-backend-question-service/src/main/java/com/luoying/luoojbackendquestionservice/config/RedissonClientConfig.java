package com.luoying.luoojbackendquestionservice.config;

import org.redisson.Redisson;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 落樱的悔恨
 * Redisson限流器
 */
@Configuration
public class RedissonClientConfig {
    @Value("${spring.redis.host}")
    private String host;

    @Value("${spring.redis.password}")
    private String password;

    @Bean
    public RRateLimiter rateLimiter() {
        Config config = new Config();
        config.useSingleServer().setAddress("redis://"+host+":6379").setDatabase(1).setPassword(password);
        RedissonClient redissonClient = Redisson.create(config);

        String key = "myRateLimiter";
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        // 每秒产生114个令牌
        rateLimiter.setRate(RateType.OVERALL, 114, 1, RateIntervalUnit.SECONDS);
        return rateLimiter;
    }
}
