package com.epages;

import static java.util.Collections.singletonMap;

import java.util.Arrays;
import java.util.List;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.retry.interceptor.StatefulRetryOperationsInterceptor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class EventSubscriberConfiguration {

    public static final String EXCHANGE_NAME = "vertical.exchange";
    public static final String RETRY_EXCHANGE_NAME = "vertical.exchange.rx";
    public static final String DEAD_LETTER_EXCHANGE_NAME = "vertical.exchange.dlx";

    public static final String SUBSCRIBER_QUEUE_NAME = "vertical.test-queue";
    public static final String RETRY_QUEUE_NAME = "vertical.test-rq";
    public static final String DEAD_LETTER_QUEUE_NAME = "vertical.dlq";

    public static final String DEAD_LETTER_EXCHANGE_HEADER_NAME = "x-dead-letter-exchange";
    private static final boolean DURABLE = true;
    private static final boolean AUTO_DELETE = false;
    private static final boolean EXCLUSIVE = false;
    @Bean
    @Primary
    TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME, DURABLE, AUTO_DELETE);
    }

    @Bean
    TopicExchange retryExchange() {
        return new TopicExchange(RETRY_EXCHANGE_NAME, DURABLE, AUTO_DELETE);
    }

    @Bean
    TopicExchange deadLetterExchange() {
        return new TopicExchange(DEAD_LETTER_EXCHANGE_NAME, DURABLE, AUTO_DELETE);
    }

    @Bean
    List<Declarable> queuesAndBindings() {
        Queue queue = new Queue(SUBSCRIBER_QUEUE_NAME, DURABLE, EXCLUSIVE, AUTO_DELETE,
                singletonMap(DEAD_LETTER_EXCHANGE_HEADER_NAME, RETRY_EXCHANGE_NAME));

        Queue retryQueue = new Queue(RETRY_QUEUE_NAME, DURABLE, EXCLUSIVE, AUTO_DELETE,
                singletonMap(DEAD_LETTER_EXCHANGE_HEADER_NAME, DEAD_LETTER_EXCHANGE_NAME));

        Queue deadLetterQueue = new Queue(DEAD_LETTER_QUEUE_NAME, DURABLE, EXCLUSIVE, AUTO_DELETE);

        return Arrays.asList(
                queue, retryQueue, deadLetterQueue,
                BindingBuilder.bind(queue).to(exchange()).with("payload.event-create"),
                BindingBuilder.bind(retryQueue).to(retryExchange()).with("#"),
                BindingBuilder.bind(deadLetterQueue).to(deadLetterExchange()).with("#")
        );
    }

    @Bean
    SimpleMessageListenerContainer subscriberListenerContainer(ConnectionFactory connectionFactory,
                                                              MessageListenerAdapter listenerAdapter,
                                                              PlatformTransactionManager transactionManager) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(SUBSCRIBER_QUEUE_NAME);
        container.setMessageListener(listenerAdapter);
        container.setChannelTransacted(true);
        container.setTransactionManager(transactionManager);
        container.setDefaultRequeueRejected(false);
        container.setConcurrentConsumers(1);
        return container;
    }

    @Bean
    SimpleMessageListenerContainer retrySubscriberListenerContainer(ConnectionFactory connectionFactory,
                                                              MessageListenerAdapter listenerAdapter,
                                                              PlatformTransactionManager transactionManager,
                                                              StatefulRetryOperationsInterceptor retryOperationsInterceptor) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(RETRY_QUEUE_NAME);
        container.setMessageListener(listenerAdapter);
        container.setChannelTransacted(true);
        container.setTransactionManager(transactionManager);
        container.setDefaultRequeueRejected(true);
        container.setConcurrentConsumers(1);
        container.setAdviceChain(new Advice[]{retryOperationsInterceptor});
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(MessageConverter messageConverter, SubscriberHandler subscriberHandler) {
        MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(subscriberHandler);
        listenerAdapter.setMessageConverter(messageConverter);
        return listenerAdapter;
    }

    @Bean
    public StatefulRetryOperationsInterceptor retryOperationsInterceptor(AmqpTemplate amqpTemplate) {
        return RetryInterceptorBuilder.stateful()
                .maxAttempts(5)
                .recoverer(new RejectAndDontRequeueRecoverer())
                //.backOffOptions(1000, 5, 10000)
                .build();
    }
}
