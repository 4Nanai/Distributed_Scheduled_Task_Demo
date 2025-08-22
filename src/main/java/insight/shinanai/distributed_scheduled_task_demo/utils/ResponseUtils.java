package insight.shinanai.distributed_scheduled_task_demo.utils;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public final class ResponseUtils {
    public static <T> ResponseEntity<?> success(T data) {
        return ResponseEntity.ok(Map.of("data", data));
    }

    public static ResponseEntity<?> success(String message) {
        return ResponseEntity.ok(Map.of("message", message));
    }

    public static <T> ResponseEntity<?> success(String message, T data) {
        return ResponseEntity.ok(Map.of("message", message, "data", data));
    }

    public static ResponseEntity<?> error(String message, int status) {
        return ResponseEntity.status(status)
                .body(Map.of("error", message));
    }

    public static ResponseEntity<?> error(String message, HttpStatus status) {
        return ResponseEntity.status(status)
                .body(Map.of("error", message));
    }

    public static ResponseEntity<?> error(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", message));
    }
}
