package com.bookstore.common.enums;

/**
 * Enum for user roles in the system
 */
public enum UserRole {
    DEV_ROLE("Developer"),
    USER_ROLE("User"),
    ADMIN_ROLE("Administrator");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}

// Made with Bob