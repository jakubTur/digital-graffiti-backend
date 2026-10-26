package edu.w04ist.si041g.backend.auth;

import edu.w04ist.si041g.backend.auth.dto.LoginRequest;
import edu.w04ist.si041g.backend.auth.dto.LoginResponse;
import edu.w04ist.si041g.backend.config.PasswordConfig;
import edu.w04ist.si041g.backend.credential.Credential;
import edu.w04ist.si041g.backend.credential.CredentialRepository;
import edu.w04ist.si041g.backend.user.User;
import edu.w04ist.si041g.backend.user.UserService;
import io.jsonwebtoken.Claims;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {
    public final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;
    public AuthService(CredentialRepository credentialRepository, PasswordEncoder passwordEncoder, JwtService jwtService, UserService userService) {
        this.credentialRepository = credentialRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userService = userService;
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
        String accessToken = jwtService.createAccessToken(user.getId(),credential.getLogin(), user.getRole().name());
        String refreshToken = jwtService.createRefreshToken(user.getId());
        return new LoginResponse(accessToken, refreshToken, user.getId(), user.getNickname(), user.getEmail(), user.getRole().name());
    }
    public LoginResponse refresh(String refreshToken) {
        try {
            Claims claims = jwtService.parseToken(refreshToken);

            if (!jwtService.isRefreshToken(claims)) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "A refresh token is required"
                );
            }

            Long userId = Long.valueOf(claims.getSubject());
            User user = userService.getUserById(userId);

            if (!user.isActive()) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "User account is inactive"
                );
            }

            String accessToken = jwtService.createAccessToken(
                    user.getId(),
                    user.getEmail(),
                    user.getRole().name()
            );

            String newRefreshToken = jwtService.createRefreshToken(user.getId());

            return new LoginResponse(
                    accessToken,
                    newRefreshToken,
                    user.getId(),
                    user.getNickname(),
                    user.getEmail(),
                    user.getRole().name()
            );
        } catch (ResponseStatusException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired refresh token"
            );
        }
    }
}
