import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.kuraterut.NotificationServiceApplication;
import org.kuraterut.listener.ConfirmationCodeListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Objects;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@Testcontainers
@SpringBootTest(classes = NotificationServiceApplication.class)
public class ConfirmationCodeListenerIntegrationTest {

    @Container
    private static final KafkaContainer KAFKA =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.3.3"));

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @SpyBean
    private ConfirmationCodeListener confirmationCodeListener;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @DynamicPropertySource
    static void kafkaProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.kafka.bootstrap-servers", KAFKA::getBootstrapServers);
        registry.add("spring.kafka.consumer.topic", () -> "confirmation-codes");
        registry.add("spring.kafka.consumer.group-id", () -> "notification-group");
        registry.add("spring.kafka.consumer.auto-offset-reset", () -> "earliest");
    }

    @BeforeAll
    static void setup() {
        await().until(KAFKA::isRunning);
    }

    @Test
    void shouldSuccessfullyProcessConfirmationCode(){
        ContainerTestUtils.waitForAssignment(
                Objects.requireNonNull(kafkaListenerEndpointRegistry.getListenerContainer("listener-id")),
                1
        );

        final String email = "user@example.com";
        final String code = "123456";

        kafkaTemplate.send("confirmation-codes", email, code);
        kafkaTemplate.flush();

        verify(confirmationCodeListener, timeout(15000).times(1))
                .listenConfirmationCode(eq(code), eq(email));
    }
}