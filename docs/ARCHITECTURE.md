# Architecture

## Scope

This repository provides WSO2 Identity Server 7.3.0 integrations for OTP delivery over SMS and Asterisk voice, for both password recovery and login MFA (verification step).

The user can choose SMS OTP or Voice OTP. If the selected delivery channel reports a send failure, or the user explicitly requests another channel because the message/call did not arrive, the same flow must offer the alternate channel.

## Delivery and fallback policy

| Initial channel | Failure / user request | Available fallback |
|---|---|---|
| SMS | Provider rejects/fails the send, or user reports no SMS | Voice via Asterisk |
| Voice | ARI/origination/dialplan failure, busy, no-answer, or user reports no call | SMS |

A channel fallback is a delivery operation, not a new authentication factor. The flow must not require the user to complete both SMS and voice.

Delivery failure and OTP verification failure are distinct states. A wrong or expired OTP must never trigger a channel switch automatically. A user may request a resend or switch channel only while the WSO2 challenge/recovery transaction remains valid and policy permits it.

### OTP authority

- WSO2 remains authoritative for OTP generation, validation, expiry, retry limits, and recovery policy.
- Channel adapters deliver a WSO2-managed challenge; they do not independently validate OTPs.
- When WSO2's supported APIs permit it, a channel switch re-delivers the still-valid challenge. If a new challenge is required, WSO2 must issue it and invalidate the prior challenge atomically.
- The implementation must not invent a parallel OTP store or bypass WSO2's recovery-token lifecycle.
- Delivery acknowledgements must be interpreted honestly: an SMS provider acceptance or Asterisk call completion does not prove that the user received/heard the code. The UI must allow an explicit channel switch even when end-device delivery cannot be confirmed.

## Components

1. **WSO2 integration layer** — separate MFA and password-recovery adapters, using supported WSO2 7.3.0 extension points and preserving native flow state.
2. **Channel orchestration** — a shared state machine for channel selection, delivery attempts, fallback eligibility, resend throttling, and idempotency.
3. **SMS adapter** — delegates to the configured WSO2 SMS provider path.
4. **Asterisk ARI adapter** — originates and correlates calls; transfers the channel to a restricted dialplan context that invokes the built-in SayDigits() application.
5. **Configuration layer** — externalized ARI endpoint, credentials, timeouts, language-to-Asterisk-channel-language mapping, and claim URIs.

## Asterisk playback

The only OTP speech mechanism is Asterisk's built-in dialplan SayDigits() application. No external or separately installed TTS engine is part of this project.

Important: SayDigits() is a dialplan application, not an ARI play operation. ARI controls call lifecycle and must hand off to a tightly restricted dialplan extension/context to execute SayDigits(). The OTP must not be placed in a Stasis application argument, URL, log, exception, or metric label. The implementation must prove a secure OTP handoff to the dialplan before enabling voice delivery; if the chosen mechanism exposes the OTP in ARI requests/events or logs, it is unacceptable.

Asterisk uses the channel's configured language and matching sound prompts. Availability of Persian prompt assets must be checked separately; SayDigits() being present does not imply that Persian prompts are installed.

## Security invariants

- OTP values must never appear in URLs, ARI application arguments, logs, exception messages, or metrics labels.
- ARI credentials are external configuration/secrets, never source-controlled.
- Calls are correlated using opaque, short-lived delivery identifiers.
- The destination phone number is resolved from a trusted user claim and validated before dialing.
- WSO2 remains authoritative for OTP generation, validation, expiration, retry limits, and recovery policy.
- Fallback must not reset or bypass account lockout, OTP attempt, resend, or recovery limits.
- A delivery retry must be idempotent and protected against duplicate calls/messages.
- ARI network access must be restricted; TLS is required outside a trusted local network.

## Compatibility target

- WSO2 Identity Server 7.3.0
- Java 21
- Asterisk 20+ ARI and dialplan SayDigits()

## Implementation status

The repository is being implemented incrementally. A component is complete only after unit, integration, packaging, and target-version compatibility checks pass.
