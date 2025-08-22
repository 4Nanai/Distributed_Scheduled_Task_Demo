package insight.shinanai.distributed_scheduled_task_demo.handler;

import insight.shinanai.distributed_scheduled_task_demo.utils.ResponseUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> exceptionHandler(Exception e) {
        log.error("Global Exception Handler: ", e);
        return ResponseUtils.error("Internal Server Error");
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<?> badCredentialsHandler(BadCredentialsException e) {
        log.error("Bad Credentials Exception: ", e);
        return ResponseUtils.error("Bad Credentials", HttpStatus.UNAUTHORIZED);
    }
}
