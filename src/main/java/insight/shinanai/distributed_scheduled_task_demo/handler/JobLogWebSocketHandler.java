package insight.shinanai.distributed_scheduled_task_demo.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import insight.shinanai.distributed_scheduled_task_demo.event.JobLogEvent;
import insight.shinanai.distributed_scheduled_task_demo.vo.LogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.net.URI;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Slf4j
@Component
public class JobLogWebSocketHandler extends TextWebSocketHandler {
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<WebSocketSession>> jobSessionMap = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String jobId = getJobIdFromSession(session);
        if (jobId != null) {
            jobSessionMap.computeIfAbsent(jobId, ignore -> new CopyOnWriteArrayList<>())
                    .add(session);
            log.info("WebSocket connection established for job ID: {}, Session ID: {}", jobId, session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String jobId = getJobIdFromSession(session);
        if (jobId != null) {
            CopyOnWriteArrayList<WebSocketSession> webSocketSessions = jobSessionMap.get(jobId);
            if (webSocketSessions != null) {
                webSocketSessions.remove(session);
                if (webSocketSessions.isEmpty()) {
                    jobSessionMap.remove(jobId);
                }
            }
            log.info("WebSocket connection closed for job ID: {}, Session ID: {}", jobId, session.getId());
        }
    }

    @EventListener
    public void handleJobLogEvent(JobLogEvent event) {
        sendLogToSessions(event.getJobId(), event.getLogVO());
    }

    public void sendLogToSessions(String jobId, LogVO logVO) {
        CopyOnWriteArrayList<WebSocketSession> webSocketSessions = jobSessionMap.get(jobId);
        if (webSocketSessions != null && !webSocketSessions.isEmpty()) {
            try {
                String message = objectMapper.writeValueAsString(logVO);
                TextMessage textMessage = new TextMessage(message);
                webSocketSessions.removeIf(session -> {
                    try {
                        if (session.isOpen()) {
                            session.sendMessage(textMessage);
                            return false;
                        } else {
                            return true;
                        }
                    } catch (IOException e) {
                        log.error("Error sending message to WebSocket session: {}", session.getId(), e);
                        return true; // Remove the session if sending fails
                    }
                });
            } catch (JsonProcessingException jsonProcessingException) {
                log.error("Error serializing LogVO to JSON", jsonProcessingException);
            }
        }
    }

    private String getJobIdFromSession(WebSocketSession session) {
        URI uri = session.getUri();
        if (uri == null) {
            return null;
        }
        String path = uri.getPath();
        if (path != null && path.contains("/jobs/") && path.endsWith("/logs")) {
            String[] split = path.split("/");
            for (int i = 0; i < split.length; i++) {
                if ("jobs".equals(split[i]) && i + 1 < split.length) {
                    return split[i + 1]; // Return the job ID
                }
            }
        }
        return null;
    }
}
