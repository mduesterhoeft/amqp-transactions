package com.epages;

import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class EventPublisher {

    private RabbitTemplate rabbitTemplate;
    private Exchange exchange;

    @Autowired
    public EventPublisher(RabbitTemplate rabbitTemplate, Exchange exchange) {
        this.rabbitTemplate = rabbitTemplate;
        this.exchange = exchange;
    }

    public void publish(EventPayload payload) {
        rabbitTemplate.convertAndSend(exchange.getName(), "payload.create", payload);
        log.info("publish {}", payload);
    }
}
