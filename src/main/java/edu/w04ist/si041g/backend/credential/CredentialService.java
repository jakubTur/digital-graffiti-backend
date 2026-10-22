package edu.w04ist.si041g.backend.credential;

import edu.w04ist.si041g.backend.credential.dto.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import edu.w04ist.si041g.backend.user.User;
import edu.w04ist.si041g.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CredentialService {


    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public CredentialService(
            CredentialRepository credentialRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Transactional
    public CredentialResponseBody createCredential(
            RegisterCredentialRequest request
    ) {
        User user = userRepository.findByNickname(request.getLogin())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        if (credentialRepository.existsByUser_Id(user.getId())) {
            throw new IllegalArgumentException(
                    "User already has credentials");
        }

        if (credentialRepository.existsByLogin(request.getLogin())) {
            throw new IllegalArgumentException(
                    "Login already exists");
        }

        validateRequest(request);

        Credential credential = new Credential();
        credential.setUser(user);
        credential.setLogin(request.getLogin());
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

        if (request.getLogin() != null) {
            if (!request.getLogin().equals(credential.getLogin())
                    && credentialRepository.existsByLogin(request.getLogin())) {
                throw new IllegalArgumentException(
                        "Login already exists");
            }

            credential.setLogin(request.getLogin());
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

    private Credential findCredential(Long id) {
        return credentialRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Credential not found"));
    }

    private void validateRequest(CredentialRequestBody request) {
        if (request.getLogin() == null || request.getLogin().isBlank()) {
            throw new IllegalArgumentException("Login is required");
        }

        if (request.getPassword() == null
                || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }
    }

    private void validateRequest(RegisterCredentialRequest request) {
        if (request.getLogin() == null || request.getLogin().isBlank()) {
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