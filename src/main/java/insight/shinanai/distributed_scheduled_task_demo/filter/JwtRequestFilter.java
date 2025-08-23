package insight.shinanai.distributed_scheduled_task_demo.filter;

import insight.shinanai.distributed_scheduled_task_demo.service.UserService;
import insight.shinanai.distributed_scheduled_task_demo.utils.JwtUtils;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@Slf4j
public class JwtRequestFilter extends OncePerRequestFilter {
    private final RedisTemplate<Object, Object> redisTemplate;
    private final UserService userService;
    private final UserDetailsService userDetailsService;

    public JwtRequestFilter(RedisTemplate<Object, Object> redisTemplate, UserService userService,
                            @Qualifier("userServiceImpl") UserDetailsService userDetailsService) {
        this.redisTemplate = redisTemplate;
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        final String requestHeader = request.getHeader("token");

        String username = null;
        String jwt = null;

        // get token header
        if (requestHeader != null) {
            jwt = requestHeader;
            try {
                username = JwtUtils.getUsernameFromToken(jwt);
            } catch (IllegalArgumentException e) {
                log.warn("Unable to get JWT Token", e);
            } catch (ExpiredJwtException e) {
                log.warn("JWT Token has expired", e);
            }
        } else {
            log.warn("JWT Token does not exist");
        }

        // get user details and set authentication
        if (username != null && SecurityContextHolder.getContext()
                .getAuthentication() == null) {
            UserDetails userDetails = getUserDetailFromRedis(username);
            // user details not in redis
            if (userDetails == null) {
                userDetails = userDetailsService.loadUserByUsername(username);
                cacheUserDetailsToRedis(userDetails);
            }
            // validate token
            if (JwtUtils.validateToken(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
                usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext()
                        .setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }

    private void cacheUserDetailsToRedis(UserDetails userDetails) {
        try {
            redisTemplate.opsForValue()
                    .set("user:" + userDetails.getUsername(), userDetails, Duration.ofMinutes(30));
        } catch (Exception e) {
            log.error("Failed to cache user details to Redis", e);
        }
    }

    private UserDetails getUserDetailFromRedis(String username) {
        try {
            return (UserDetails) redisTemplate.opsForValue()
                    .get("user:" + username);
        } catch (Exception e) {
            return null;
        }
    }
}
