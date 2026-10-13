package com.medibook.common.util;

public final class RoleUtils {

    private RoleUtils() {
    }

    /** Administrative roles. Null (guest) is not an admin. */
    public static boolean isAdmin(Role role) {
        return role == Role.SUPER_ADMIN || role == Role.CLINIC_ADMIN || role == Role.CLINIC_OWNER;
    }
}
