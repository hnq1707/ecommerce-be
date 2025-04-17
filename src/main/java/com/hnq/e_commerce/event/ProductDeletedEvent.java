package com.hnq.e_commerce.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ProductDeletedEvent {
    private final String productId;
}
