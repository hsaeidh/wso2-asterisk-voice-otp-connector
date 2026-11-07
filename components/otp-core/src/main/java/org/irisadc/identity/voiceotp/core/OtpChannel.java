package org.irisadc.identity.voiceotp.core;

/** Delivery channels supported by the shared OTP flow. */
public enum OtpChannel {
    SMS,
    VOICE;

    public OtpChannel alternate() {
        return this == SMS ? VOICE : SMS;
    }
}
