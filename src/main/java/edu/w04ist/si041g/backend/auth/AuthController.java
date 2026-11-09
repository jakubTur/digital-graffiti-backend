package edu.w04ist.si041g.backend.auth;

import edu.w04ist.si041g.backend.auth.dto.LoginRequest;
import edu.w04ist.si041g.backend.auth.dto.LoginResponse;
import edu.w04ist.si041g.backend.auth.dto.RefreshTokenRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        System.out.println("LOGIN CONTROLLER REACHED");
        return ResponseEntity.ok(authService.login(loginRequest));
    }
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(@RequestBody RefreshTokenRequest refreshTokenRequest) {
        return ResponseEntity.ok(authService.refresh(refreshTokenRequest.getRefreshToken()));
    }
}
