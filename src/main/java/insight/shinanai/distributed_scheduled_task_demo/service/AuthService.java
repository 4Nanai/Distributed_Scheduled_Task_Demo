package insight.shinanai.distributed_scheduled_task_demo.service;

import insight.shinanai.distributed_scheduled_task_demo.dto.UserLoginDTO;
import insight.shinanai.distributed_scheduled_task_demo.dto.UserRegistryDTO;
import org.springframework.http.ResponseEntity;

public interface AuthService {
    ResponseEntity<?> login(UserLoginDTO userLoginDTO);

    ResponseEntity<?> register(UserRegistryDTO userRegistryDTO);
}
