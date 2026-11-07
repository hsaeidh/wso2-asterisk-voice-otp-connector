package org.irisadc.identity.voiceotp.core;

import java.time.Clock;
import java.util.Objects;

/**
 * Pure policy for switching between SMS and Voice without changing the
 * WSO2-owned OTP challenge or weakening verification policy.
 */
public final class ChannelFallbackPolicy {

    private final Clock clock;

    public ChannelFallbackPolicy(Clock clock) {
        this.clock = Objects.requireNonNull(clock, "clock");
    }

    public FallbackDecision evaluate(OtpDeliverySession session, DeliveryFailure failure) {
        Objects.requireNonNull(session, "session");
        Objects.requireNonNull(failure, "failure");

        if (!clock.instant().isBefore(session.expiresAt())) {
            return FallbackDecision.deny("challenge-expired");
        }

        if (session.status() == OtpDeliveryStatus.EXPIRED) {
            return FallbackDecision.deny("challenge-expired");
        }
        if (session.status() == OtpDeliveryStatus.LOCKED
                || failure == DeliveryFailure.ATTEMPTS_EXCEEDED) {
            return FallbackDecision.deny("verification-locked");
        }
        if (session.status() == OtpDeliveryStatus.VERIFIED) {
            return FallbackDecision.deny("challenge-already-verified");
        }
        if (session.status() == OtpDeliveryStatus.OTP_VERIFICATION_PENDING
                || session.status() == OtpDeliveryStatus.OTP_VERIFICATION_FAILED
                || failure == DeliveryFailure.INVALID_OTP
                || failure == DeliveryFailure.OTP_EXPIRED) {
            return FallbackDecision.deny("otp-verification-is-not-delivery-failure");
        }
        if (session.channelSwitchCount() >= session.maximumChannelSwitches()) {
            return FallbackDecision.deny("channel-switch-limit-reached");
        }

        boolean deliveryFailure = switch (failure) {
            case PROVIDER_REJECTED, PROVIDER_TIMEOUT, ARI_UNAVAILABLE, CALL_REJECTED,
                    BUSY, NO_ANSWER, DIALPLAN_FAILURE -> true;
            default -> false;
        };

        boolean userReportedNonReceipt =
                failure == DeliveryFailure.USER_REPORTED_NON_RECEIPT
                        && session.status() == OtpDeliveryStatus.DELIVERY_ACCEPTED;

        if (!deliveryFailure && !userReportedNonReceipt) {
            return FallbackDecision.deny("no-eligible-delivery-failure");
        }

        if (session.status() != OtpDeliveryStatus.DELIVERY_FAILED
                && session.status() != OtpDeliveryStatus.DELIVERY_ACCEPTED
                && session.status() != OtpDeliveryStatus.CHALLENGE_ACTIVE) {
            return FallbackDecision.deny("session-state-does-not-allow-switch");
        }

        return FallbackDecision.allow(
                session.selectedChannel().alternate(), "alternate-channel-available");
    }
}
