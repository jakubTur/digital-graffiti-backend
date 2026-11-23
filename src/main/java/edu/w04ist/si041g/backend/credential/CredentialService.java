package edu.w04ist.si041g.backend.credential;

import edu.w04ist.si041g.backend.auth.verification.EmailService;
import edu.w04ist.si041g.backend.credential.dto.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import edu.w04ist.si041g.backend.user.User;
import edu.w04ist.si041g.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class CredentialService {


    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final CredentialChangeRepository credentialChangeRepository;

    public CredentialService(
            CredentialRepository credentialRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            CredentialChangeRepository credentialChangeRepository,
            EmailService emailService
    ) {
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.credentialChangeRepository = credentialChangeRepository;
        this.emailService = emailService;
    }


    @Transactional
    public CredentialResponseBody createCredential(
            RegisterCredentialRequest request
    ) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        if (credentialRepository.existsByUser_Id(user.getId())) {
            throw new IllegalArgumentException(
                    "User already has credentials");
        }

        if (credentialRepository.existsByLogin(request.getEmail())) {
            throw new IllegalArgumentException(
                    "Email is already used");
        }

        validateRequest(request);

        Credential credential = new Credential();
        credential.setUser(user);
        credential.setLogin(request.getEmail());
        credential.setPasswordHash(
                passwordEncoder.encode(request.getPassword())
        );

        return toResponse(credentialRepository.save(credential));
    }

    @Transactional(readOnly = true)
    public CredentialResponseBody getCredentialById(Long id) {
        return toResponse(findCredential(id));
    }

    @Transactional(readOnly = true)
    public List<CredentialResponseBody> getAllCredentials() {
        return credentialRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CredentialResponseBody updateCredential(
            Long id,
            CredentialRequestBody request
    ) {
        Credential credential = findCredential(id);

        if (request.getEmail() != null) {
            if (!request.getEmail().equals(credential.getLogin())
                    && credentialRepository.existsByLogin(request.getEmail())) {
                throw new IllegalArgumentException(
                        "Login already exists");
            }

            credential.setLogin(request.getEmail());
        }

        if (request.getPassword() != null) {
            credential.setPasswordHash(
                    passwordEncoder.encode(request.getPassword())
            );
        }

        return toResponse(credentialRepository.save(credential));
    }

    @Transactional
    public void deleteCredential(Long id) {
        Credential credential = findCredential(id);
        credentialRepository.delete(credential);
    }

    @Transactional
    public void updateEmail(Long userId, String newEmail) {
        Credential credential = credentialRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("Credential not found for user ID: " + userId));

        if (credentialRepository.existsByLogin(newEmail)) {
            throw new IllegalArgumentException("Email is already used");
        }

        credential.setLogin(newEmail);
        credentialRepository.save(credential);
    }

    @Transactional
    public void updatePassword(Long userId, String newPassword) {
        Credential credential = credentialRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("Credential not found for user ID: " + userId));

        credential.setPasswordHash(newPassword);
        credentialRepository.save(credential);
    }

    @Transactional
    public void requestPasswordUpdate(Long userId, String newPassword) {
        CredentialChangeRequest request = new CredentialChangeRequest();
        request.setPasswordChange(true);
        request.setUser(credentialRepository.findByUser_Id(userId).orElseThrow(() -> new IllegalArgumentException("User not found for user ID: " + userId)).getUser());
        request.setValue(passwordEncoder.encode(newPassword));
        request.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        request.setToken(UUID.randomUUID().toString());
        credentialChangeRepository.save(request);
        emailService.sendVerificationEmail(request.getUser().getEmail(), request.getToken(), "/api/credentials/verify-change?token=", """
                Please verify your password change request by opening this link:

                %s

                This link may only be used once.
                """, "Verify Password Change Request");
    }

    @Transactional
    public void requestEmailUpdate(Long userId, String newEmail) {
        if (credentialRepository.existsByLogin(newEmail)) {
            throw new IllegalArgumentException("Email is already used");
        }

        CredentialChangeRequest request = new CredentialChangeRequest();
        request.setPasswordChange(false);
        request.setUser(credentialRepository.findByUser_Id(userId).orElseThrow(() -> new IllegalArgumentException("User not found for user ID: " + userId)).getUser());
        request.setValue(newEmail);
        request.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        request.setToken(UUID.randomUUID().toString());
        credentialChangeRepository.save(request);
        emailService.sendVerificationEmail(
                request.getUser().getEmail(), request.getToken(), "/api/credentials/verify-change?token=", """
                Email for you Digital Graffiti account was changed to %s. If you did not request this change, contact US.
                """.formatted(request.getValue()), "Email Change"
        );
        emailService.sendVerificationEmail(request.getValue(), request.getToken(), "/api/credentials/verify-change?token=", """
                Please verify your email changed to (%s) by opening this link:

                %s

                This link may only be used once.
                """.formatted(newEmail, "%s"), "Verify Email Change Request");
    }

    @Transactional
    public String verifyChange(String token) {
        CredentialChangeRequest request = credentialChangeRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Invalid token"));

        if (request.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token has expired");
        }
        String message;
        if (request.isPasswordChange()) {
            message = "Password change verified successfully";
            updatePassword(request.getUser().getId(), request.getValue());
        } else {
            message = "Email change verified successfully";
            updateEmail(request.getUser().getId(), request.getValue());
        }
        credentialChangeRepository.delete(request);
        return message;
    }

    private Credential findCredential(Long id) {
        return credentialRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Credential not found"));
    }

    private void validateRequest(RegisterCredentialRequest request) {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Login is required");
        }

        if (request.getPassword() == null
                || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
    }

    private CredentialResponseBody toResponse(Credential credential) {
        return new CredentialResponseBody(
                credential.getId(),
                credential.getUser().getId(),
                credential.getLogin()
        );
    }

    public boolean verifyPassword(String login, String password) {
        if (password == null) {
            return false;
        }

        return credentialRepository.findByLogin(login)
                .map(credential -> passwordEncoder.matches(
                        password,
                        credential.getPasswordHash()
                ))
                .orElse(false);
    }

}