package org.irisadc.identity.voiceotp.core;

/** Failure categories used to decide whether a channel switch is appropriate. */
public enum DeliveryFailure {
    NONE,
    PROVIDER_REJECTED,
    PROVIDER_TIMEOUT,
    ARI_UNAVAILABLE,
    CALL_REJECTED,
    BUSY,
    NO_ANSWER,
    DIALPLAN_FAILURE,
    USER_REPORTED_NON_RECEIPT,
    INVALID_OTP,
    OTP_EXPIRED,
    ATTEMPTS_EXCEEDED
}
