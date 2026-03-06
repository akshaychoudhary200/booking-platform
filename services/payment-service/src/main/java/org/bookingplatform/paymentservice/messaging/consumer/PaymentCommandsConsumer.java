package org.bookingplatform.paymentservice.messaging.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.bookingplatform.paymentservice.messaging.dto.AuthorizePaymentCommand;
import org.bookingplatform.paymentservice.payment.application.PaymentAuthService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
public class PaymentCommandsConsumer {

    private final ObjectMapper objectMapper;
    private final PaymentAuthService paymentAuthService;

    public PaymentCommandsConsumer(ObjectMapper objectMapper, PaymentAuthService paymentAuthService) {
        this.objectMapper = objectMapper;
        this.paymentAuthService = paymentAuthService;
    }

    @KafkaListener(topics = "payment.commands")
    public void onMessage(String message, Acknowledgment ack) throws Exception {
        AuthorizePaymentCommand cmd = objectMapper.readValue(message, AuthorizePaymentCommand.class);

        if (!"AuthorizePayment".equals(cmd.type())) {
            ack.acknowledge();
            return;
        }

        // If this throws, no ack => redelivery
        paymentAuthService.handleAuthorize(cmd);

        ack.acknowledge();
    }
}