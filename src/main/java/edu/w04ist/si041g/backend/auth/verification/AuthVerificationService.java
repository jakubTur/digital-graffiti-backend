package edu.w04ist.si041g.backend.auth.verification;

import edu.w04ist.si041g.backend.user.User;
import edu.w04ist.si041g.backend.user.UserRegisterEvent;
import edu.w04ist.si041g.backend.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthVerificationService {
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;

    @Value("${app.verification-token-expiration-minutes:30}")
    private int tokenExpirationMinutes;

    public AuthVerificationService(
            UserRepository userRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            EmailService emailService
    ) {
        this.userRepository = userRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.emailService = emailService;
    }

    @Transactional
    public void createAndSendVerificationToken(User user){
        emailVerificationTokenRepository.deleteByUser(user);
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(tokenExpirationMinutes));
        emailVerificationTokenRepository.save(token);
        emailService.sendVerificationEmail(user.getEmail(), token.getToken());
    }

    @Transactional
    public String verifyEmail(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Verification token has expired");
        }

        User user = verificationToken.getUser();
        user.setVerified(true);
        userRepository.save(user);
        emailVerificationTokenRepository.delete(verificationToken);

        return "Email verified successfully";
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserCreatedEvent(UserRegisterEvent event) {
        System.out.println("Handling UserRegisterEvent for userId: " + event.userId());
        try {
            User user = userRepository.findById(event.userId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            createAndSendVerificationToken(user);
        } catch (RuntimeException e) {
            System.err.println("Error during user registration: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
