package com.warrantywise.enums;

import lombok.Getter;

@Getter
public enum WarrantyStatus {
    ACTIVE("Active"),
    EXPIRING_SOON("Expiring Soon"),
    EXPIRED("Expired");

    private final String displayName;

    WarrantyStatus(String displayName) {
        this.displayName = displayName;
    }
}
