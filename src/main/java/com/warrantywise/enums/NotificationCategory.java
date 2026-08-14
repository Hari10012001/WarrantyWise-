package com.warrantywise.enums;

import lombok.Getter;

@Getter
public enum NotificationCategory {
    WARRANTY_EXPIRY("Warranty Expiry"),
    SERVICE_DUE("Service Due"),
    SYSTEM("System"),
    INFO("Information");

    private final String displayName;

    NotificationCategory(String displayName) {
        this.displayName = displayName;
    }
}
