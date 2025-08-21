package insight.shinanai.distributed_scheduled_task_demo.service.impl;

import insight.shinanai.distributed_scheduled_task_demo.dto.UserLoginDTO;
import insight.shinanai.distributed_scheduled_task_demo.dto.UserRegistryDTO;
import insight.shinanai.distributed_scheduled_task_demo.service.AuthService;
import insight.shinanai.distributed_scheduled_task_demo.service.UserService;
import insight.shinanai.distributed_scheduled_task_demo.utils.JwtUtils;
import insight.shinanai.distributed_scheduled_task_demo.utils.ResponseUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    public AuthServiceImpl(AuthenticationManager authenticationManager, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.userService = userService;
    }

    @Override
    public ResponseEntity<?> login(UserLoginDTO userLoginDTO) {
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userLoginDTO.getUsername(),
                                                                                                          userLoginDTO.getPassword()
        );
        try {
            authenticationManager.authenticate(authenticationToken);
        } catch (AuthenticationException e) {
            return ResponseUtils.error("Authentication failed: Username or password is incorrect.",
                                       HttpStatus.UNAUTHORIZED
            );
        }
        final UserDetails userDetails = userService.loadUserByUsername(userLoginDTO.getUsername());
        final String token = JwtUtils.generateToken(userDetails);

        return ResponseUtils.success(Map.of("token", token, "message", "Login successful"));
    }

    @Override
    public ResponseEntity<?> register(UserRegistryDTO userRegistryDTO) {
        if (userService.existsByUsername(userRegistryDTO.getUsername())) {
            return ResponseUtils.error("Username is already taken.", HttpStatus.BAD_REQUEST);
        }
        userService.saveUser(userRegistryDTO);
        return ResponseUtils.success("User registered successfully.");
    }
}
