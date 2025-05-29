package org.kuraterut.service;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.kuraterut.config.JwtTokenProvider;
import org.kuraterut.exceptions.exceptions.InvalidConfirmationCodeException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Getter
public class AuthService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final Map<String, String> pendingConfirmations = new ConcurrentHashMap<>();

    @Value("${spring.kafka.topic}")
    private String topic;


    public void registerUser(String email) {
        String confirmationCode = generateConfirmationCode();
        pendingConfirmations.put(email, confirmationCode);

        kafkaTemplate.send(topic, email, confirmationCode);
    }

    public String confirmUser(String email, String code) {
        String storedCode = pendingConfirmations.get(email);

        if (storedCode == null || !storedCode.equals(code)) {
            throw new InvalidConfirmationCodeException("Invalid confirmation code");
        }

        pendingConfirmations.remove(email);
        return jwtTokenProvider.generateToken(email);
    }

    private String generateConfirmationCode() {
        return String.format("%06d", new Random().nextInt(999999));
    }
}