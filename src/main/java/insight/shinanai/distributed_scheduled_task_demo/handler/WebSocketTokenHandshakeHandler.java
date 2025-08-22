package insight.shinanai.distributed_scheduled_task_demo.handler;

import insight.shinanai.distributed_scheduled_task_demo.domain.User;
import insight.shinanai.distributed_scheduled_task_demo.utils.JwtUtils;
import insight.shinanai.distributed_scheduled_task_demo.utils.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class WebSocketTokenHandshakeHandler extends DefaultHandshakeHandler {
    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        final Optional<String> token = getQueryParam(request, "token");
        if (token.isPresent()) {
            Authentication authentication = validateTokenAndCreateAuth(token.get());
            if (authentication != null) {
                attributes.put("simpUser", authentication);
                return authentication;
            }
        }
        log.warn("WebSocket connection rejected due to missing or invalid token");
        return null;
    }

    private Optional<String> getQueryParam(final ServerHttpRequest request, final String paramName) {
        URI uri = request.getURI();
        String query = uri.getQuery();

        if (query != null) {
            String[] params = query.split("&");
            Map<String, String> queryParams = Arrays.stream(params)
                    .map(param -> param.split("=", 2))
                    .collect(Collectors.toMap(p -> p[0], p -> p.length > 1 ? p[1] : ""));
            return Optional.ofNullable(queryParams.get(paramName));
        }

        return Optional.empty();
    }

    private Authentication validateTokenAndCreateAuth(String token) {
        try {
            User user = SecurityUtils.getCurrentUser();
            if (JwtUtils.validateToken(token, user)) {
                return SecurityUtils.getAuthentication();
            } else {
                log.warn("Invalid token for user: {}", user.getUsername());
                return null;
            }
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage(), e);
            return null;
        }
    }
}
