package com.warrantywise.enums;

import lombok.Getter;

@Getter
public enum ProductCondition {
    NEW("New"),
    REFURBISHED("Refurbished"),
    USED("Used"),
    OPEN_BOX("Open Box");

    private final String displayName;

    ProductCondition(String displayName) {
        this.displayName = displayName;
    }
}
