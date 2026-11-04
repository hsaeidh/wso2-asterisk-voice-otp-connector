# Architecture

## Scope

This repository provides WSO2 Identity Server 7.3.0 integrations for voice-delivered one-time passwords through Asterisk.

The implementation is split into three layers:

1. **Asterisk ARI client** — originates outbound calls and controls playback.
2. **WSO2 OIDC MFA authenticator** — delegates OTP generation, validation, expiry, and attempt policies to WSO2's OTP framework, while delivering the generated OTP through the voice transport.
3. **Password-recovery delivery adapter** — integrates with the supported WSO2 recovery notification extension point without replacing WSO2's recovery-token lifecycle.

## Security invariants

- OTP values must never appear in URLs, ARI application arguments, logs, exception messages, or metrics labels.
- ARI credentials are external configuration/secrets, never source-controlled.
- Calls are correlated using opaque, short-lived delivery identifiers.
- The destination phone number is resolved from a trusted user claim and validated before dialing.
- WSO2 remains authoritative for OTP generation, validation, expiration, retry limits, and recovery policy.
- Network access to ARI must be restricted; TLS is required outside a trusted local network.

## Compatibility target

- WSO2 Identity Server 7.3.0
- Java 21
- Asterisk ARI (Stasis application)

## Implementation status

This repository is being built incrementally. A component is considered complete only after its tests and compatibility checks pass.