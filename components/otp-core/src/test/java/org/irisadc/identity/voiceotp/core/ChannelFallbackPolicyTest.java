package org.irisadc.identity.voiceotp.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;

class ChannelFallbackPolicyTest {

    private static final Instant NOW = Instant.parse("2026-11-07T08:00:00Z");
    private ChannelFallbackPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new ChannelFallbackPolicy(Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void smsFailureOffersVoice() {
        FallbackDecision result = policy.evaluate(session(OtpChannel.SMS,
                OtpDeliveryStatus.DELIVERY_FAILED, 0, 1), DeliveryFailure.PROVIDER_REJECTED);

        assertTrue(result.allowed());
        assertEquals(OtpChannel.VOICE, result.targetChannel());
    }

    @Test
    void voiceFailureOffersSms() {
        FallbackDecision result = policy.evaluate(session(OtpChannel.VOICE,
                OtpDeliveryStatus.DELIVERY_FAILED, 0, 1), DeliveryFailure.NO_ANSWER);

        assertTrue(result.allowed());
        assertEquals(OtpChannel.SMS, result.targetChannel());
    }

    @Test
    void acceptedDeliveryCanBeSwitchedWhenUserReportsNonReceipt() {
        FallbackDecision result = policy.evaluate(session(OtpChannel.SMS,
                OtpDeliveryStatus.DELIVERY_ACCEPTED, 0, 1),
                DeliveryFailure.USER_REPORTED_NON_RECEIPT);

        assertTrue(result.allowed());
        assertEquals(OtpChannel.VOICE, result.targetChannel());
    }

    @Test
    void wrongOtpNeverTriggersFallback() {
        FallbackDecision result = policy.evaluate(session(OtpChannel.SMS,
                OtpDeliveryStatus.OTP_VERIFICATION_FAILED, 0, 1), DeliveryFailure.INVALID_OTP);

        assertFalse(result.allowed());
        assertEquals("otp-verification-is-not-delivery-failure", result.reason());
    }

    @Test
    void expiredChallengeCannotSwitchChannel() {
        FallbackDecision result = policy.evaluate(session(OtpChannel.VOICE,
                OtpDeliveryStatus.DELIVERY_FAILED, 0, 1, NOW), DeliveryFailure.NO_ANSWER);

        assertFalse(result.allowed());
        assertEquals("challenge-expired", result.reason());
    }

    @Test
    void switchLimitIsEnforced() {
        FallbackDecision result = policy.evaluate(session(OtpChannel.SMS,
                OtpDeliveryStatus.DELIVERY_FAILED, 1, 1), DeliveryFailure.PROVIDER_TIMEOUT);

        assertFalse(result.allowed());
        assertEquals("channel-switch-limit-reached", result.reason());
    }

    @Test
    void verifiedChallengeCannotSwitch() {
        FallbackDecision result = policy.evaluate(session(OtpChannel.VOICE,
                OtpDeliveryStatus.VERIFIED, 0, 1), DeliveryFailure.USER_REPORTED_NON_RECEIPT);

        assertFalse(result.allowed());
        assertEquals("challenge-already-verified", result.reason());
    }

    private OtpDeliverySession session(OtpChannel channel, OtpDeliveryStatus status,
                                       int switchCount, int maxSwitches) {
        return session(channel, status, switchCount, maxSwitches, NOW.plusSeconds(300));
    }

    private OtpDeliverySession session(OtpChannel channel, OtpDeliveryStatus status,
                                       int switchCount, int maxSwitches, Instant expiry) {
        return new OtpDeliverySession("opaque-flow-id", "opaque-challenge-id",
                OtpFlowPurpose.LOGIN_MFA, channel, status, switchCount, maxSwitches, expiry);
    }
}
