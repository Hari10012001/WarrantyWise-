package com.warrantywise.enums;

import lombok.Getter;

@Getter
public enum ProductStatus {
    IN_USE("In Use"),
    IN_STORAGE("In Storage"),
    UNDER_REPAIR("Under Repair"),
    SOLD("Sold"),
    DISPOSED("Disposed"),
    GIFTED("Gifted"),
    LOST("Lost");

    private final String displayName;

    ProductStatus(String displayName) {
        this.displayName = displayName;
    }
}
