package com.subhash.apimonitoring.monitoring.thread;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Monitoring Thread Pool Configuration
 *
 * WHY - Single Responsibility:
 * Dedicated thread pool for monitoring tasks
 * Prevents monitoring from affecting other operations
 *
 * HOW:
 * - Configurable core/max pool sizes
 * - Custom thread naming for easy debugging
 * - Rejection policy for overload situations
 *
 * RESULT: Efficient, isolated monitoring execution
 */
@Slf4j
@Configuration
public class MonitoringThreadPool {

    @Value("${app.monitoring.thread-pool.core-size:20}")
    private int corePoolSize;

    @Value("${app.monitoring.thread-pool.max-size:50}")
    private int maxPoolSize;

    @Value("${app.monitoring.thread-pool.queue-capacity:100}")
    private int queueCapacity;

    @Value("${app.monitoring.thread-pool.keep-alive-seconds:60}")
    private int keepAliveSeconds;

    @Bean(name = "monitoringExecutor")
    public Executor monitoringExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAliveSeconds);
        executor.setThreadNamePrefix("monitoring-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();

        log.info("Monitoring thread pool initialized: core={}, max={}, queue={}",
                corePoolSize, maxPoolSize, queueCapacity);

        return executor;
    }
}
