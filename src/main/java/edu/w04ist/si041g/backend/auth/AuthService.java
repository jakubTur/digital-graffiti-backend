package edu.w04ist.si041g.backend.auth;

import edu.w04ist.si041g.backend.auth.dto.LoginRequest;
import edu.w04ist.si041g.backend.auth.dto.LoginResponse;
import edu.w04ist.si041g.backend.config.PasswordConfig;
import edu.w04ist.si041g.backend.credential.Credential;
import edu.w04ist.si041g.backend.credential.CredentialRepository;
import edu.w04ist.si041g.backend.user.User;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    public final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    public AuthService(CredentialRepository credentialRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }
    public LoginResponse login(LoginRequest loginRequest) {
        if (loginRequest.getEmail() == null || loginRequest.getEmail().isBlank() || loginRequest.getPassword() == null || loginRequest.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Login and password must be provided");

        }
        Credential credential = credentialRepository.findByLogin(loginRequest.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid login or password"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), credential.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid login or password");
        }
        User user = credential.getUser();
        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User account is inactive");
        }
        String token = jwtService.createToken(user.getId(), credential.getLogin(), user.getRole().name());
        return new LoginResponse(token, jwtService.getTokenLifetime().toSeconds(), user.getId(), credential.getLogin(), user.getRole().name());
    }
}
