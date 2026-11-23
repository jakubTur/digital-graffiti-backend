package edu.w04ist.si041g.backend.credential;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CredentialChangeRepository extends JpaRepository<CredentialChangeRequest, Long> {
    Optional<CredentialChangeRequest> findByToken(String token);
}
