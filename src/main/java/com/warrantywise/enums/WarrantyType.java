package com.warrantywise.enums;

import lombok.Getter;

@Getter
public enum WarrantyType {
    MANUFACTURER("Manufacturer"),
    EXTENDED("Extended"),
    SELLER("Seller");

    private final String displayName;

    WarrantyType(String displayName) {
        this.displayName = displayName;
    }
}
