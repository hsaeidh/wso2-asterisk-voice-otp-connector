package org.irisadc.identity.voiceotp.core;

/**
 * Delivery and verification states are deliberately separate. A wrong OTP is
 * never interpreted as a delivery failure and must not trigger channel fallback.
 */
public enum OtpDeliveryStatus {
    CHALLENGE_ACTIVE,
    DELIVERY_PENDING,
    DELIVERY_ACCEPTED,
    DELIVERY_FAILED,
    OTP_VERIFICATION_PENDING,
    OTP_VERIFICATION_FAILED,
    VERIFIED,
    EXPIRED,
    LOCKED
}
