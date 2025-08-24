package insight.shinanai.distributed_scheduled_task_demo.listeners;

import com.fasterxml.jackson.databind.ObjectMapper;
import insight.shinanai.distributed_scheduled_task_demo.dto.JobRegistryDTO;
import insight.shinanai.distributed_scheduled_task_demo.service.JobInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class JobRegistryMessageListener implements MessageListener {

    private final ObjectMapper objectMapper;
    private final JobInfoService jobInfoService;

    public JobRegistryMessageListener(ObjectMapper objectMapper, JobInfoService jobInfoService) {
        this.objectMapper = objectMapper;
        this.jobInfoService = jobInfoService;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            log.info("Received job registry message: {}", new String(message.getBody()));
            JobRegistryDTO jobRegistryDTO = objectMapper.readValue(message.getBody(), JobRegistryDTO.class);
            jobInfoService.scheduleScriptJob(jobRegistryDTO);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
