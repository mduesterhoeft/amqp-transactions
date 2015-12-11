package com.epages;

import static org.apache.commons.lang.exception.ExceptionUtils.getStackTrace;
import static org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer.X_EXCEPTION_MESSAGE;
import static org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer.X_EXCEPTION_STACKTRACE;

import java.util.Map;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.retry.RejectAndDontRequeueRecoverer;

public class RejectWithExceptionInfoAndDontRequeueRevoverer extends RejectAndDontRequeueRecoverer {
    @Override
    public void recover(Message message, Throwable cause) {
        Map<String, Object> headers = message.getMessageProperties().getHeaders();
        headers.put(X_EXCEPTION_STACKTRACE, getStackTrace(cause));
        headers.put(X_EXCEPTION_MESSAGE, cause.getCause() != null ? cause.getCause().getMessage() : cause.getMessage());

        super.recover(message, cause);
    }
}
