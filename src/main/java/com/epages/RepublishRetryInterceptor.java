package com.epages;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.amqp.core.Message;

public class RepublishRetryInterceptor implements MethodInterceptor {

    private final RepublishRetryMessageRecoverer republishRetryMessageRecoverer;

    public RepublishRetryInterceptor(RepublishRetryMessageRecoverer republishRetryMessageRecoverer) {
        this.republishRetryMessageRecoverer = republishRetryMessageRecoverer;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
       try {
           return invocation.proceed();
       } catch (Throwable t) {
           Message message = (Message) invocation.getArguments()[1];
           republishRetryMessageRecoverer.recover(message, t);
           return null;
       }
    }
}
