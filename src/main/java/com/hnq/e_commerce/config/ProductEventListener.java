package com.hnq.e_commerce.listeners;


import com.hnq.e_commerce.event.ProductCreatedEvent;
import com.hnq.e_commerce.event.ProductDeletedEvent;
import com.hnq.e_commerce.event.ProductUpdatedEvent;
import com.hnq.e_commerce.services.ProductSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProductEventListener {

    private final ProductSearchService productSearchService;

    @EventListener
    public void handleProductCreatedEvent(ProductCreatedEvent event) {
        productSearchService.indexProduct(event.getProduct());
    }

    @EventListener
    public void handleProductUpdatedEvent(ProductUpdatedEvent event) {
        productSearchService.indexProduct(event.getProduct());
    }

    @EventListener
    public void handleProductDeletedEvent(ProductDeletedEvent event) {
        productSearchService.removeProduct(event.getProductId());
    }
}