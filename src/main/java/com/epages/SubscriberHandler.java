package com.epages;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SubscriberHandler {


    private final ShopService shopService;

    @Autowired
    public SubscriberHandler(ShopService shopService) {
        this.shopService = shopService;
    }

    @Transactional
    public void handleMessage(EventPayload event) {
        Shop shop = new Shop();
        shop.setName(event.getName());

        Shop savedShop = shopService.create(shop);

        log.info("Created shop {} from event {}", shop, event);
    }
}
