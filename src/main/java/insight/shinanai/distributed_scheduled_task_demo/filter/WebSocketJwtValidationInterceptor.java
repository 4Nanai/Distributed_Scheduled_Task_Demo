package insight.shinanai.distributed_scheduled_task_demo.filter;

import insight.shinanai.distributed_scheduled_task_demo.service.UserService;
import insight.shinanai.distributed_scheduled_task_demo.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class WebSocketJwtValidationInterceptor implements HandshakeInterceptor {
    private final UserService userService;

    public WebSocketJwtValidationInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {
        Optional<String> token = getQueryParam(request, "token");
        if (token.isPresent()) {
            Authentication authentication = validateTokenAndCreateAuth(token.get());
            if (authentication != null) {
                attributes.put("simpUser", authentication);
                return true; // 允许连接
            }
        }

        log.warn("WebSocket connection rejected due to missing or invalid token");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {
        // No action needed after handshake
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
            if (JwtUtils.isTokenExpired(token)) {
                log.warn("Token is expired: {}", token);
                return null;
            }
            String username = JwtUtils.getUsernameFromToken(token);
            UserDetails userDetails = userService.loadUserByUsername(username);
            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage(), e);
            return null;
        }
    }
}
