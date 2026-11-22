package edu.w04ist.si041g.backend.auth.verification;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final UrlResolver urlResolver;
    private final String fromEmail;

    public EmailService(
            JavaMailSender mailSender,
            UrlResolver urlResolver,
            @Value("${spring.mail.username}") String fromEmail
    ) {
        this.mailSender = mailSender;
        this.urlResolver = urlResolver;
        this.fromEmail = fromEmail;
    }

    public void sendVerificationEmail(String toEmail, String token) {
        String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
        String verificationUrl = urlResolver.getBackendBaseUrl()
                + "/auth/verify-email?token=" + encodedToken;

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    false,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify your email address");
            helper.setText("""
                    Please verify your email address by opening this link:

                    %s

                    This link may only be used once.
                    """.formatted(verificationUrl));

            mailSender.send(message);
        } catch (MessagingException | MailException exception) {
            throw new IllegalStateException(
                    "Unable to send verification email to " + toEmail,
                    exception
            );
        }
    }
}
