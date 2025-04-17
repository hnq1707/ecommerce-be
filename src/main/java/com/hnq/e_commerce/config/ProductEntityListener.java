package com.hnq.e_commerce.config;

import com.hnq.e_commerce.entities.Product;

import com.hnq.e_commerce.event.ProductCreatedEvent;
import com.hnq.e_commerce.event.ProductDeletedEvent;
import com.hnq.e_commerce.event.ProductUpdatedEvent;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class ProductEntityListener {

    private static ApplicationEventPublisher publisher;

    @Autowired
    public void setPublisher(ApplicationEventPublisher publisher) {
        ProductEntityListener.publisher = publisher;
    }

    @PostPersist
    public void postPersist(Product product) {
        if (publisher != null) {
            publisher.publishEvent(new ProductCreatedEvent(product));
        }
    }

    @PostUpdate
    public void postUpdate(Product product) {
        if (publisher != null) {
            publisher.publishEvent(new ProductUpdatedEvent(product));
        }
    }

    @PostRemove
    public void postRemove(Product product) {
        if (publisher != null) {
            publisher.publishEvent(new ProductDeletedEvent(product.getId()));
        }
    }
}