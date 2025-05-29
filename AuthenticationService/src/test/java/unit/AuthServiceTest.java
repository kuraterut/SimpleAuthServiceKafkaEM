package unit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kuraterut.config.JwtTokenProvider;
import org.kuraterut.exceptions.exceptions.InvalidConfirmationCodeException;
import org.kuraterut.service.AuthService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthService authService;

    @Test
    void registerUser_ShouldSendConfirmationCode() {
        String email = "test@example.com";
        ReflectionTestUtils.setField(authService, "topic", "confirmation-codes");

        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(null);

        authService.registerUser(email);

        verify(kafkaTemplate).send(eq("confirmation-codes"), eq(email), anyString());
        assertNotNull(authService.getPendingConfirmations().get(email));
    }

    @Test
    void confirmUser_WithValidCode_ShouldReturnToken() {
        String email = "test@example.com";
        String code = "123456";
        String expectedToken = "jwt.token.here";

        authService.getPendingConfirmations().put(email, code);
        when(jwtTokenProvider.generateToken(email)).thenReturn(expectedToken);

        String token = authService.confirmUser(email, code);

        assertEquals(expectedToken, token);
        assertNull(authService.getPendingConfirmations().get(email));
    }

    @Test
    void confirmUser_WithInvalidCode_ShouldThrowException() {
        String email = "test@example.com";
        authService.getPendingConfirmations().put(email, "123456");

        assertThrows(InvalidConfirmationCodeException.class, () -> {
            authService.confirmUser(email, "wrong-code");
        });
    }
}