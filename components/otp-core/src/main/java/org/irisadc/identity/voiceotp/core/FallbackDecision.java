package org.irisadc.identity.voiceotp.core;

import java.util.Optional;

public record FallbackDecision(boolean allowed, OtpChannel targetChannel, String reason) {

    public FallbackDecision {
        if (allowed && targetChannel == null) {
            throw new IllegalArgumentException("An allowed fallback requires a target channel");
        }
        if (!allowed && targetChannel != null) {
            throw new IllegalArgumentException("A denied fallback cannot have a target channel");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("A decision reason is required");
        }
    }

    public Optional<OtpChannel> target() {
        return Optional.ofNullable(targetChannel);
    }

    public static FallbackDecision allow(OtpChannel target, String reason) {
        return new FallbackDecision(true, target, reason);
    }

    public static FallbackDecision deny(String reason) {
        return new FallbackDecision(false, null, reason);
    }
}
