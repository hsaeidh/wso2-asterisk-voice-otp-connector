package org.irisadc.identity.voiceotp.core;

import java.time.Instant;
import java.util.Objects;

/**
 * Non-secret snapshot of an OTP delivery flow.
 *
 * challengeId and flowId are opaque references. This object must never contain
 * the OTP itself, provider credentials, or unmasked destination data.
 */
public record OtpDeliverySession(
        String flowId,
        String challengeId,
        OtpFlowPurpose purpose,
        OtpChannel selectedChannel,
        OtpDeliveryStatus status,
        int channelSwitchCount,
        int maximumChannelSwitches,
        Instant expiresAt) {

    public OtpDeliverySession {
        requireText(flowId, "flowId");
        requireText(challengeId, "challengeId");
        Objects.requireNonNull(purpose, "purpose");
        Objects.requireNonNull(selectedChannel, "selectedChannel");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(expiresAt, "expiresAt");
        if (channelSwitchCount < 0 || maximumChannelSwitches < 0) {
            throw new IllegalArgumentException("Switch counters cannot be negative");
        }
        if (channelSwitchCount > maximumChannelSwitches) {
            throw new IllegalArgumentException("Switch count exceeds configured maximum");
        }
    }

    private static void requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " must not be blank");
        }
    }
}
