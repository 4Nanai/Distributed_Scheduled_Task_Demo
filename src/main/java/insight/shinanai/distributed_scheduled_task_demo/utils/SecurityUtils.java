package insight.shinanai.distributed_scheduled_task_demo.utils;

import insight.shinanai.distributed_scheduled_task_demo.domain.User;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtils {
    public static User getCurrentUser() {
        UsernamePasswordAuthenticationToken authentication = (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user;
        }
        throw new BadCredentialsException("Authentication does not exist or is expired");
    }

    public static Long getCurrentUserId() {
        User user = getCurrentUser();
        return user.getId();
    }

    public static String getCurrentUsername() {
        User user = getCurrentUser();
        return user.getUsername();
    }
}
