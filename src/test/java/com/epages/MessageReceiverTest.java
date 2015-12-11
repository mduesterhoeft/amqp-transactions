package com.epages;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.assertj.core.api.BDDAssertions.then;
import static org.hamcrest.Matchers.hasSize;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.epages.util.MessageListener;
import com.epages.util.MessageSender;

public class MessageReceiverTest {

    private URI shopsUri = URI.create("http://localhost:8080/shops");


    private MessageSender messageSender = new MessageSender();
    private MessageListener messageListener;

    @Before
    public void setup() {
        messageListener = new MessageListener();
        messageListener.initListener();
    }

    @After
    public void cleanup() {
        try {
            messageListener.cancel();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void should_send_message_only() {
        String name = "test_me1122";
        for (int i = 0; i < 100; i++) {
            String currentName = name + i;
            messageSender.sendMessage(currentName);
            messageSender.sendMessage(currentName);
        }

    }

    @Test
    public void should_send_message() {

        messageListener.resetCounter();

        List<String> responseList =  given().contentType(JSON).when().get(shopsUri).andReturn().jsonPath().get("");
        int count = responseList.size();

        String name = RandomStringUtils.random(10);
        messageSender.sendMessage(name);

        given()
                .contentType(JSON)
        .when().get(shopsUri)
        .then()
                .statusCode(200)
                .body("", hasSize(count + 1));

        then(messageListener.countReceivedMessages()).isEqualTo(1);
    }

    @Test
    public void should_discard_Message() {

        messageListener.resetCounter();

        List<String> responseList =  given().contentType(JSON).when().get(shopsUri).andReturn().jsonPath().get("");
        int count = responseList.size();

        String name = RandomStringUtils.random(10);
        messageSender.sendMessage(name);
        messageSender.sendMessage(name);

        given()
                .contentType(JSON)
        .when()
                .get(shopsUri)
        .then()
                .statusCode(200)
                .body("", hasSize(count + 1));

        then(messageListener.countReceivedMessages()).isEqualTo(1);
    }
}
