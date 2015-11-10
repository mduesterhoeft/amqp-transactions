package com.epages;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

import java.net.URI;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "shops", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
public class ShopController {

    private ShopService shopService;

    @Autowired
    public ShopController(ShopService shopService) {
        this.shopService = shopService;
    }

    @RequestMapping
    public ResponseEntity<List<Shop>> getAll() {
        return ResponseEntity.ok(shopService.getAll());
    }

    @RequestMapping(path = "/{id}")
    public ResponseEntity<Shop> getOne(@PathVariable Long id) {
        return ResponseEntity.ok(shopService.get(id));
    }

    @RequestMapping(method = POST)
    public ResponseEntity<Void> create(@RequestBody Shop shop) {
        Shop shopSaved = shopService.create(shop);
        return ResponseEntity.created(URI.create("http://localhost/shops/" + shopSaved.getId())).build();
    }

    @RequestMapping(method = POST, path = "/save-first")
    public ResponseEntity<Void> createSaveFirst(@RequestBody Shop shop) {
        Shop shopSaved = shopService.createSaveFirst(shop);
        return ResponseEntity.created(URI.create("http://localhost/shops/" + shopSaved.getId())).build();
    }
}
