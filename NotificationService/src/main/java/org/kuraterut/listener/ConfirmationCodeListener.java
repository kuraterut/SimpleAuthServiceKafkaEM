package org.kuraterut.listener;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class ConfirmationCodeListener {

    @KafkaListener(id = "listener-id", topics = "${spring.kafka.consumer.topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void listenConfirmationCode(@Payload String code,
                                       @Header(KafkaHeaders.RECEIVED_KEY) String email) {
        System.out.printf("Sending confirmation code %s to email %s%n", code, email);
    }

}