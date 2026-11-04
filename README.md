# WSO2 Asterisk Voice OTP Connector

Voice OTP delivery integrations for **WSO2 Identity Server 7.3.0** using **Asterisk ARI** and **SayDigits()**.

## Supported journeys
- OIDC multi-factor authentication (MFA)
- Passwordless authentication
- Password recovery

## Delivery model
WSO2 owns OTP generation, validation, expiration, retry limits, and recovery policy. The connector delivers the generated OTP through an outbound Asterisk call. Asterisk reads digits with `SayDigits()` (not a TTS engine). When voice delivery fails under policy, SMS fallback uses WSO2's existing notification flow and the **same OTP**.

## Runtime target
- WSO2 Identity Server 7.3.0
- Java 21
- Asterisk ARI (Stasis)
- Maven multi-module / OSGi

## Status
This branch adds architecture and Maven/OSGi scaffolding only. It does **not** yet implement authenticators, ARI operations, Dialplan, or end-to-end tests. Runtime compatibility and installation must be verified against actual WSO2 IS 7.3.0 and Asterisk.

See [Architecture](docs/ARCHITECTURE.md).
