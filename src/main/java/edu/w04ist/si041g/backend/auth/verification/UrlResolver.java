package edu.w04ist.si041g.backend.auth.verification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service
public class UrlResolver {
    private final int serverPort;
    private final String configuredBaseUrl;

    public UrlResolver(
            @Value("${server.port:8080}") int serverPort,
            @Value("${app.backend-url:}") String configuredBaseUrl
    ) {
        this.serverPort = serverPort;
        this.configuredBaseUrl = configuredBaseUrl;
    }

    public String getBackendBaseUrl() {
        if (configuredBaseUrl != null && !configuredBaseUrl.isBlank()) {
            return configuredBaseUrl.replaceAll("/$", "");
        }

        String ec2PublicIp = getEc2PublicIp();
        if (ec2PublicIp != null) {
            return "http://" + ec2PublicIp + ":" + serverPort;
        }

        try {
            InetAddress address = InetAddress.getLocalHost();
            if (address.isLoopbackAddress()) {
                return "http://localhost:" + serverPort;
            }
            return "http://" + address.getHostAddress() + ":" + serverPort;
        } catch (UnknownHostException exception) {
            return "http://localhost:" + serverPort;
        }
    }

    private String getEc2PublicIp() {
        try {
            HttpURLConnection tokenRequest =
                    (HttpURLConnection) URI.create(
                            "http://169.254.169.254/latest/api/token"
                    ).toURL().openConnection();

            tokenRequest.setRequestMethod("PUT");
            tokenRequest.setRequestProperty(
                    "X-aws-ec2-metadata-token-ttl-seconds",
                    "60"
            );
            tokenRequest.setConnectTimeout(500);
            tokenRequest.setReadTimeout(500);

            String token;
            try (var input = tokenRequest.getInputStream()) {
                token = new String(input.readAllBytes(), UTF_8).trim();
            }

            HttpURLConnection ipRequest =
                    (HttpURLConnection) URI.create(
                            "http://169.254.169.254/latest/meta-data/public-ipv4"
                    ).toURL().openConnection();

            ipRequest.setRequestProperty(
                    "X-aws-ec2-metadata-token",
                    token
            );
            ipRequest.setConnectTimeout(500);
            ipRequest.setReadTimeout(500);

            try (var input = ipRequest.getInputStream()) {
                String ip = new String(input.readAllBytes(), UTF_8).trim();
                return ip.isBlank() ? null : ip;
            }
        } catch (Exception exception) {
            return null;
        }
    }
}
