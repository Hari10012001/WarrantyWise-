package com.warrantywise.enums;

import lombok.Getter;

@Getter
public enum PurchaseMode {
    ONLINE("Online"),
    IN_STORE("In Store"),
    GIFT("Gift"),
    SECOND_HAND("Second Hand");

    private final String displayName;

    PurchaseMode(String displayName) {
        this.displayName = displayName;
    }
}
