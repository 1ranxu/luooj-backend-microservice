package com.luoying.luoojbackendcommon.config;

import com.sun.istack.internal.NotNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

@Configuration
public class ThreadPoolExecutorConfig {
    private static final int coreThreads = 2 * Runtime.getRuntime().availableProcessors();
    private static final int maxThreads = 2 * coreThreads;
    private static final long keepAliveTime = 0L;
    private static final TimeUnit unit = TimeUnit.SECONDS;
    private static final LinkedBlockingQueue queue = new LinkedBlockingQueue(1000);

    private static final String threadName = "pool-thread-";

    private static final ThreadFactory threadFactory = new ThreadFactory() {
        private int count = 1;

        @Override
        public Thread newThread(@NotNull Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(threadName + count);
            count++;
            return thread;
        }
    };

    private static final RejectedExecutionHandler policy = new ThreadPoolExecutor.CallerRunsPolicy();


    @Bean
    public ThreadPoolExecutor threadPoolExecutor() {
        return new ThreadPoolExecutor(coreThreads, maxThreads, keepAliveTime, unit,
                queue, threadFactory, policy);
    }
}