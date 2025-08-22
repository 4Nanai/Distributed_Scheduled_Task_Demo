package insight.shinanai.distributed_scheduled_task_demo.controller;

import insight.shinanai.distributed_scheduled_task_demo.utils.ResponseUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisTestController {
    private final RedisTemplate<Object, Object> redisTemplate;

    public RedisTestController(RedisTemplate<Object, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/redis/test")
    public ResponseEntity<?> testRedisConnection() {
        redisTemplate.opsForValue().set("test", "Hello, Redis!");
        return ResponseUtils.success(redisTemplate.opsForValue()
                                             .get("test"));
    }
}
