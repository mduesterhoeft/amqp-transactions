package com.epages;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class EventBusPublisherConfiguration {

    public static final String EXCHANGE_NAME = "test.exchange";

    /**
     * This bean gets registered as a new exchange on the RabbitMQ server.
     */
    @Bean
    @Primary
    public TopicExchange testExchange() {
        final boolean durable = true;
        final boolean autoDelete = false;
        return new TopicExchange(EXCHANGE_NAME, durable, autoDelete);
    }
}
