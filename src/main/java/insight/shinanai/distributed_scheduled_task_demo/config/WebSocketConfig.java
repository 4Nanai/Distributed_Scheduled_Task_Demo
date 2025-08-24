package insight.shinanai.distributed_scheduled_task_demo.config;

import insight.shinanai.distributed_scheduled_task_demo.filter.WebSocketJwtValidationInterceptor;
import insight.shinanai.distributed_scheduled_task_demo.handler.JobLogWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    private final JobLogWebSocketHandler jobLogWebSocketHandler;
    private final WebSocketJwtValidationInterceptor webSocketJwtValidationInterceptor;

    public WebSocketConfig(JobLogWebSocketHandler jobLogWebSocketHandler,
                           WebSocketJwtValidationInterceptor webSocketJwtValidationInterceptor) {
        this.jobLogWebSocketHandler = jobLogWebSocketHandler;
        this.webSocketJwtValidationInterceptor = webSocketJwtValidationInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(jobLogWebSocketHandler, "/ws/jobs/{jobId}/logs")
                .setAllowedOriginPatterns("*")
                .addInterceptors(webSocketJwtValidationInterceptor);
    }
}
