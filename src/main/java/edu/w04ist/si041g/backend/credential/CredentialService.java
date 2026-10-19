
package edu.w04ist.si041g.backend.credential;

import edu.w04ist.si041g.backend.credential.CredentialRequestBody;
import edu.w04ist.si041g.backend.credential.CredentialResponseBody;
import edu.w04ist.si041g.backend.user.User;
import edu.w04ist.si041g.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CredentialService {

    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;

    public CredentialService(
            CredentialRepository credentialRepository,
            UserRepository userRepository
    ) {
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public CredentialResponseBody createCredential(
            CredentialRequestBody request
    ) {
        User user = userRepository.findById(request.getUserId())
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
        credential.setPasswordHash(request.getPasswordHash());

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

        if (request.getPasswordHash() != null) {
            credential.setPasswordHash(request.getPasswordHash());
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

        if (request.getPasswordHash() == null
                || request.getPasswordHash().isBlank()) {
            throw new IllegalArgumentException("Password hash is required");
        }
    }

    private CredentialResponseBody toResponse(Credential credential) {
        return new CredentialResponseBody(
                credential.getId(),
                credential.getUser().getId(),
                credential.getLogin()
        );
    }
}
