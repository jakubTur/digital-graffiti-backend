package edu.w04ist.si041g.backend.auth;

import edu.w04ist.si041g.backend.auth.dto.LoginRequest;
import edu.w04ist.si041g.backend.auth.dto.LoginResponse;
import edu.w04ist.si041g.backend.auth.dto.RefreshTokenRequest;
import edu.w04ist.si041g.backend.auth.verification.AuthVerificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final AuthService authService;
    private final AuthVerificationService verificationService;
    public AuthController(AuthService authService, AuthVerificationService verificationService) {
        this.authService = authService;
        this.verificationService = verificationService;
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

    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(@RequestParam("token") String token) {
        String message = verificationService.verifyEmail(token);
        return ResponseEntity.ok(message);
    }
}
