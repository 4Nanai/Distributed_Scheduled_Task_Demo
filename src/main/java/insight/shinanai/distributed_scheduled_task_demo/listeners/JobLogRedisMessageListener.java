package insight.shinanai.distributed_scheduled_task_demo.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import insight.shinanai.distributed_scheduled_task_demo.dto.JobLogDTO;
import insight.shinanai.distributed_scheduled_task_demo.handler.JobLogWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class JobLogRedisMessageListener implements MessageListener {
    private final JobLogWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    public JobLogRedisMessageListener(JobLogWebSocketHandler webSocketHandler, ObjectMapper objectMapper) {
        this.webSocketHandler = webSocketHandler;
        this.objectMapper = objectMapper;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(message.getChannel());
            String jobId = channel.substring("job-logs:".length());

            JobLogDTO jobLog = objectMapper.readValue(message.getBody(), JobLogDTO.class);
            webSocketHandler.sendLogToSessions(jobId, jobLog.getLogVO());
        } catch (Exception e) {
            log.error("Error processing Redis message", e);
        }
    }
}
