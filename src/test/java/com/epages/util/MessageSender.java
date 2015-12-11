package com.epages.util;

import java.util.Date;
import java.util.UUID;

import com.epages.EventSubscriberConfiguration;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import lombok.SneakyThrows;

public class MessageSender {

    private static final String EXCHANGE_NAME = EventSubscriberConfiguration.EXCHANGE_NAME;

    @SneakyThrows
    public void sendMessage(String name) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.99.100");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);

        String routingKey = "payload.event-create";
        String message = "{ \"name\":\"" + name + "\"}";
        BasicProperties basicProperties = MessageProperties.PERSISTENT_BASIC.builder()
                .contentType(org.springframework.amqp.core.MessageProperties.CONTENT_TYPE_JSON)
                .messageId(UUID.randomUUID().toString())
                .timestamp(new Date())
                .build();
        channel.basicPublish(EXCHANGE_NAME, routingKey, basicProperties, message.getBytes());
        System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");

        connection.close();
    }
}
