package com.medibook.common.util;

public final class RoleUtils {

    private RoleUtils() {
    }

    /** SUPER_ADMIN and CLINIC_ADMIN see everything. Null (guest) is not an admin. */
    public static boolean isAdmin(Role role) {
        return role == Role.SUPER_ADMIN || role == Role.CLINIC_ADMIN;
    }
}