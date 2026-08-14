package com.warrantywise.enums;

import lombok.Getter;

@Getter
public enum ActionType {
    CREATE("Create"),
    UPDATE("Update"),
    DELETE("Delete"),
    LOGIN("Login"),
    LOGOUT("Logout"),
    UPLOAD("Upload");

    private final String displayName;

    ActionType(String displayName) {
        this.displayName = displayName;
    }
}
