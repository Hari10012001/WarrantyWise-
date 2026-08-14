package com.warrantywise.enums;

import lombok.Getter;

@Getter
public enum AttachmentType {
    PRIMARY_IMAGE("Primary Image"),
    BILL("Bill"),
    INVOICE("Invoice"),
    DOCUMENT("Document");

    private final String displayName;

    AttachmentType(String displayName) {
        this.displayName = displayName;
    }
}
