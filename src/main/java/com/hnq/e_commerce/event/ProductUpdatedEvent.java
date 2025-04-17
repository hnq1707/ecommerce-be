package com.hnq.e_commerce.event;

import com.hnq.e_commerce.entities.Product;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProductUpdatedEvent {
    private final Product product;
}
