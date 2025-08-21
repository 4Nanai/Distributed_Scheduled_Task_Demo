package insight.shinanai.distributed_scheduled_task_demo.controller;

import insight.shinanai.distributed_scheduled_task_demo.dto.UserLoginDTO;
import insight.shinanai.distributed_scheduled_task_demo.dto.UserRegistryDTO;
import insight.shinanai.distributed_scheduled_task_demo.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO userLoginDTO) {
        return authService.login(userLoginDTO);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegistryDTO userRegistryDTO) {
        return authService.register(userRegistryDTO);
    }
}
