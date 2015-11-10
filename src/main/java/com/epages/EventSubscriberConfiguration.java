package com.epages;

import org.aopalliance.aop.Advice;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.interceptor.StatefulRetryOperationsInterceptor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class EventSubscriberConfiguration {

    @Bean
    Queue testEventSubscriberQueue() {
        final boolean durable = true;
        return new Queue("testEventSubscriberQueue", durable, false, false);
    }

    @Bean
    Binding binding(TopicExchange topicExchange) {
        return BindingBuilder.bind(testEventSubscriberQueue()).to(topicExchange).with("payload.event-create");
    }

    @Bean
    SimpleMessageListenerContainer messageListenerContainer(ConnectionFactory connectionFactory,
                                                            MessageListenerAdapter listenerAdapter,
                                                            PlatformTransactionManager transactionManager) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(testEventSubscriberQueue().getName());
        container.setMessageListener(listenerAdapter);
        container.setChannelTransacted(true);
        container.setTransactionManager(transactionManager);
        container.setAdviceChain(new Advice[]{retryOperationsInterceptor()});
        return container;
    }

    @Bean
    MessageListenerAdapter listenerAdapter(MessageConverter messageConverter, SubscriberHandler subscriberHandler) {
        MessageListenerAdapter listenerAdapter = new MessageListenerAdapter(subscriberHandler);
        listenerAdapter.setMessageConverter(messageConverter);
        return listenerAdapter;
    }

    @Bean
    public StatefulRetryOperationsInterceptor retryOperationsInterceptor() {
        return RetryInterceptorBuilder.stateful()
                .maxAttempts(3)
                .backOffOptions(1000, 2.0, 10000)
                .build();
    }
}
