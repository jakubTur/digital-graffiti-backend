package edu.w04ist.si041g.backend.auth.email;

import edu.w04ist.si041g.backend.credential.Credential;
import edu.w04ist.si041g.backend.credential.CredentialRepository;
import edu.w04ist.si041g.backend.credential.CredentialService;
import edu.w04ist.si041g.backend.user.User;
import edu.w04ist.si041g.backend.user.UserRegisterEvent;
import edu.w04ist.si041g.backend.user.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuthVerificationService {
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final CredentialRepository credentialRepository;
    private final EmailService emailService;
    private final CredentialService credentialService;

    @Value("${app.verification-token-expiration-minutes:30}")
    private int tokenExpirationMinutes;

    public AuthVerificationService(
            UserRepository userRepository,
            EmailVerificationTokenRepository emailVerificationTokenRepository,
            CredentialRepository credentialRepository,
            EmailService emailService,
            CredentialService credentialService) {
        this.userRepository = userRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.credentialRepository = credentialRepository;
        this.emailService = emailService;
        this.credentialService = credentialService;
    }

    @Transactional
    public void createAndSendVerificationToken(User user, String address, String template_message, String subject) {
        emailVerificationTokenRepository.deleteByUser(user);
        EmailVerificationToken token = new EmailVerificationToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(tokenExpirationMinutes));
        emailVerificationTokenRepository.save(token);
        emailService.sendVerificationEmail(user.getEmail(), token.getToken(), address, template_message, subject);
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
    @Transactional
    public void sendResetPasswordMail(String email) {
        System.out.println("Sending reset password mail to " + email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        createAndSendVerificationToken(user, "/auth/reset-password/confirm?token=", """
                Please reset your password by opening this link:

                %s

                This link may only be used once.
                """, "Reset your password");
        System.out.println("Reset password mail sent to " + user.getEmail());
    }
    @Transactional
    public String verifyResetPasswordToken(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid reset password token"));

        if (verificationToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Reset password token has expired");
        }

        User user = verificationToken.getUser();
        Credential credential = credentialRepository.findByUser_Id(user.getId())
                .orElseThrow(() -> new RuntimeException("Credential not found for user ID: " + user.getId()));
        String newPassword = generateStrongPassword();
        credential.setPasswordHash(credentialService.encodePassword(newPassword));
        credentialRepository.save(credential);
        emailService.sendResetPasswordEmail(user.getEmail(), newPassword);
        emailVerificationTokenRepository.delete(verificationToken);

        return "Reset password token verified successfully for user: " + user.getEmail() + ". A new password has been sent to the user's email.";
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleUserCreatedEvent(UserRegisterEvent event) {
        System.out.println("Handling UserRegisterEvent for userId: " + event.userId());
        try {
            User user = userRepository.findById(event.userId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            createAndSendVerificationToken(user, "/auth/verify-email?token=", """
                    Please verify your email address by opening this link:

                    %s

                    This link may only be used once.
                    """, "Verify your email address");
        } catch (RuntimeException e) {
            System.err.println("Error during user registration: " + e.getMessage());
            e.printStackTrace();
        }
    }
    private String generateStrongPassword() {
        String UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String LOWER = "abcdefghijklmnopqrstuvwxyz";
        String DIGITS = "0123456789";
        String SYMBOLS = "!?@#$%^&*()-_=+";
        String ALL = UPPER + LOWER + DIGITS + SYMBOLS;
        SecureRandom RANDOM = new SecureRandom();
        List<Character> passwordChars = new java.util.ArrayList<>(16);
        passwordChars.add(LOWER.charAt(RANDOM.nextInt(LOWER.length())));
        passwordChars.add(UPPER.charAt(RANDOM.nextInt(UPPER.length())));
        passwordChars.add(DIGITS.charAt(RANDOM.nextInt(DIGITS.length())));
        passwordChars.add(SYMBOLS.charAt(RANDOM.nextInt(SYMBOLS.length())));
        for (int i = 4; i < 12; i++) {
            passwordChars.add(ALL.charAt(RANDOM.nextInt(ALL.length())));
        }
        java.util.Collections.shuffle(passwordChars, RANDOM);
        StringBuilder password = new StringBuilder(16);
        for (char c : passwordChars) {
            password.append(c);
        }
        return password.toString();
    }
}
