package com.zhanglx.sso.auth.constants;

import java.util.Set;

public final class MemberVerificationCodeScenes {

    public static final String REGISTER = "REGISTER";
    public static final String FORGOT_PASSWORD = "FORGOT_PASSWORD";
    public static final String BIND_PHONE = "BIND_PHONE";

    private static final Set<String> SUPPORTED = Set.of(REGISTER, FORGOT_PASSWORD, BIND_PHONE);

    private MemberVerificationCodeScenes() {
    }

    public static boolean isSupported(String scene) {
        return SUPPORTED.contains(scene);
    }
}
