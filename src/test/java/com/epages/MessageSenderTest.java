package com.epages;

import static com.jayway.restassured.RestAssured.given;
import static com.jayway.restassured.http.ContentType.JSON;
import static org.assertj.core.api.BDDAssertions.then;

import java.net.URI;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;

import com.epages.util.MessageListener;

public class MessageSenderTest {

    private MessageListener messageListener =  new MessageListener();

    private URI shopsUri = URI.create("http://localhost:8080/shops");
    private Shop shop;

//    @Before
//    public void setup() {
//        messageListener = new MessageListener();
//        messageListener.initListener();
//    }
//
//    @After
//    public void cleanup() {
//        try {
//            messageListener.cancel();
//        } catch (IOException | TimeoutException e) {
//            e.printStackTrace();
//        }
//    }

    @Test
    public void should_send_message() {
        messageListener.resetCounter();

        givenShop();

        given()
                .contentType(JSON)
                .body(shop)
        .when()
                .post(shopsUri)
        .then()
                .statusCode(201);

        then(messageListener.countReceivedMessages()).isEqualTo(1);
    }

    @Test
    public void should_send_message_save_first() {
        //messageListener.resetCounter();

        int countBefore = getCount();

        givenShop();

        given()
                .contentType(JSON)
                .body(shop)
                .when()
                .post(shopsUri.toString() + "/save-first")
                .then()
                .statusCode(201);

        then(messageListener.countReceivedMessages()).isEqualTo(1);
        then(getCount()).isEqualTo(countBefore + 1);
    }

    private int getCount() {
        List<String> responseList =  given().contentType(JSON).when().get(shopsUri).andReturn().jsonPath().get("");
        return responseList.size();
    }

    @Test
    public void should_not_send_message_on_db_exception() {
        messageListener.resetCounter();

        givenShop();

        givenShopExists();

        given()
                .contentType(JSON)
                .body(shop)
        .when()
                .post(shopsUri)
        .then()
                .statusCode(500);

        then(messageListener.countReceivedMessages()).isEqualTo(1);
    }


    private void givenShopExists() {
        given().contentType(JSON).body(shop)
        .when().post(shopsUri);
    }

    private void givenShop() {
        shop = new Shop();
        shop.setName(RandomStringUtils.random(10));
    }
}
