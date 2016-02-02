package com.epages;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.REQUIRED)
@Component
public class ShopService {

    private ShopRepository shopRepository;
    private EventPublisher eventPublisher;

    @Autowired
    public ShopService(ShopRepository shopRepository, EventPublisher eventPublisher) {
        this.shopRepository = shopRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<Shop> getAll() {
        return  shopRepository.findAll();
    }

    public Shop get(Long id) {
        return shopRepository.findOne(id);
    }

    public Shop create(Shop shop) {
        eventPublisher.publish(new EventPayload(shop.getName()));
        return shopRepository.save(shop);
    }

    public Shop createSaveFirst(Shop shop) {
        Shop savedShop = shopRepository.save(shop);
        eventPublisher.publish(new EventPayload(savedShop.getName()));
        return savedShop;
    }
}
