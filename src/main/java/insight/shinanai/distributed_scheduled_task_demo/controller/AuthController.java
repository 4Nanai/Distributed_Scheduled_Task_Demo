package insight.shinanai.distributed_scheduled_task_demo.controller;

import insight.shinanai.distributed_scheduled_task_demo.dto.UserLoginDTO;
import insight.shinanai.distributed_scheduled_task_demo.dto.UserRegistryDTO;
import insight.shinanai.distributed_scheduled_task_demo.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestParam String username, @RequestParam String password) {
        UserLoginDTO userLoginDTO = new UserLoginDTO(username, password);
        return authService.login(userLoginDTO);
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestParam String username,
                                      @RequestParam String password,
                                      @RequestParam String email) {
        UserRegistryDTO userRegistryDTO = new UserRegistryDTO(username, password, email);
        return authService.register(userRegistryDTO);
    }
}
