package com.epages.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

import lombok.SneakyThrows;

public class MessageListener {

    static String EXCHANGE_NAME = "test.exchange";
    static String QUEUE_NAME = "testQueue";

    private List<String> messagesReceived = new ArrayList<>();
    private Channel channel;

    @SneakyThrows
    public void initListener()  {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.99.100");
        Connection connection = factory.newConnection();
        channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "topic", true);
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "payload.create");

        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope,
                                       AMQP.BasicProperties properties, byte[] body) throws IOException {
                String message = new String(body, "UTF-8");
                System.out.println(" [x] Received '" + message + "'");
                messagesReceived.add(message);
            }
        };
        channel.basicConsume(QUEUE_NAME, true, consumer);
    }

    public int countReceivedMessages() {
        return messagesReceived.size();
    }

    public void resetCounter() {
        messagesReceived.clear();
    }

    public void cancel() throws IOException, TimeoutException {
        channel.close();
    }
}
