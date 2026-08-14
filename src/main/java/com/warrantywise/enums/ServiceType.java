package com.warrantywise.enums;

import lombok.Getter;

@Getter
public enum ServiceType {
    REPAIR("Repair"),
    MAINTENANCE("Maintenance"),
    INSPECTION("Inspection"),
    REPLACEMENT("Replacement"),
    OTHER("Other");

    private final String displayName;

    ServiceType(String displayName) {
        this.displayName = displayName;
    }
}
