package insight.shinanai.distributed_scheduled_task_demo.config;

import insight.shinanai.distributed_scheduled_task_demo.listeners.JobLogRedisMessageListener;
import insight.shinanai.distributed_scheduled_task_demo.listeners.JobRegistryMessageListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            JobLogRedisMessageListener jobLogMessageListener,
            JobRegistryMessageListener jobRegistryMessageListener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(jobLogMessageListener, new PatternTopic("job-logs:*"));
        container.addMessageListener(jobRegistryMessageListener, new PatternTopic("job-registry:*"));
        return container;
    }
}
