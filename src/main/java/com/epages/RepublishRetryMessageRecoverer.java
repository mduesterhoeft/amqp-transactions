package com.epages;

import static org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer.X_ORIGINAL_EXCHANGE;
import static org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer.X_ORIGINAL_ROUTING_KEY;
import static org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer.X_EXCEPTION_MESSAGE;

import java.util.Map;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.MessageRecoverer;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class RepublishRetryMessageRecoverer implements MessageRecoverer {

    private final AmqpTemplate amqpTemplate;
    private final RejectAndDontRequeueRecoverer rejectAndDontRequeueRecoverer;

    private static final String RETRY_HEADER_NAME = "x-retry-count";

    private final int retries;

    public RepublishRetryMessageRecoverer(AmqpTemplate amqpTemplate, int retries) {
        this.retries = retries;
        this.amqpTemplate = amqpTemplate;
        this.rejectAndDontRequeueRecoverer = new RejectAndDontRequeueRecoverer();
    }

    public RepublishRetryMessageRecoverer(AmqpTemplate amqpTemplate) {
        this(amqpTemplate, 3);
    }

    @Override
    public void recover(Message message, Throwable cause) {
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        int retryCount = (int) headers.getOrDefault(RETRY_HEADER_NAME, 0);
        log.info("retry count {} for message {}", retryCount, message.getMessageProperties().getMessageId());
        if (retryCount > retries) {
            log.info("rejecting message {}", message.getMessageProperties().getMessageId());

            //message.getMessageProperties().setReceivedRoutingKey((String) headers.get(X_ORIGINAL_ROUTING_KEY));
            rejectAndDontRequeueRecoverer.recover(message, cause);
        } else {
            log.info("retrying message {}", message.getMessageProperties().getMessageId());

            headers.putIfAbsent(X_ORIGINAL_EXCHANGE, message.getMessageProperties().getReceivedExchange());
            headers.putIfAbsent(X_ORIGINAL_ROUTING_KEY, message.getMessageProperties().getReceivedRoutingKey());
            headers.put(RETRY_HEADER_NAME, retryCount + 1);
            headers.put(X_EXCEPTION_MESSAGE,
                    cause.getCause() != null ? cause.getCause().getMessage() : cause.getMessage());
            this.amqpTemplate.send("", message.getMessageProperties().getConsumerQueue(), message);
        }

    }
}
