
package edu.w04ist.si041g.backend.credential;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CredentialRepository
        extends JpaRepository<Credential, Long> {

    Optional<Credential> findByLogin(String login);

    Optional<Credential> findByUser_Id(Long userId);

    boolean existsByLogin(String login);

    boolean existsByUser_Id(Long userId);
}
