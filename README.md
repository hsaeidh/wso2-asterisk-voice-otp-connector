# WSO2 Asterisk Voice OTP Connector

WSO2 Identity Server 7.3.0 integration for dual-channel OTP delivery using SMS and Asterisk voice calls.

- Login MFA and password recovery.
- User-selectable SMS / Voice OTP.
- Bidirectional fallback after delivery failure or explicit non-receipt.
- Asterisk built-in dialplan SayDigits() only; no separate TTS engine.
- WSO2 remains authoritative for OTP and recovery lifecycle.

See [Architecture](docs/ARCHITECTURE.md) and [Dual-channel fallback](docs/DUAL_CHANNEL_FALLBACK.md).

The project is under development. Do not deploy until the target-version integration and security tests pass.
