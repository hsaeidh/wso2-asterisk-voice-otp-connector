# Dual-channel OTP selection and fallback

## User journeys

### Login MFA

1. The user completes the preceding authentication step.
2. WSO2 presents the configured OTP delivery choices: SMS OTP and Voice OTP. Policy may select a default, but the alternate remains available where enabled.
3. The selected channel attempts delivery.
4. If the sender returns a definite failure, show a safe delivery error and offer the other channel.
5. If the provider reports acceptance but the user does not receive the SMS/call, provide an explicit “Use voice instead” / “Use SMS instead” action. Do not claim delivery based solely on provider acceptance or call answer.
6. The user enters the OTP. WSO2 performs validation and applies its expiry and attempt limits.

### Password recovery

1. User identification and anti-enumeration behavior remain controlled by WSO2.
2. The recovery flow presents SMS OTP and Voice OTP as available methods, subject to configured policy and verified claims.
3. The chosen sender delivers the WSO2-managed recovery challenge.
4. On send failure or an explicit user switch request, offer the other channel without creating a second independent recovery transaction.
5. Password reset proceeds only after WSO2 validates the recovery OTP and its existing recovery policy permits the operation.

## State model

- CHALLENGE_CREATED
- CHANNEL_SELECTED
- DELIVERY_PENDING
- DELIVERY_ACCEPTED (provider accepted; not proof of end-user receipt)
- DELIVERY_FAILED
- AWAITING_CHANNEL_SWITCH
- OTP_VALIDATION_PENDING
- OTP_VERIFIED
- OTP_REJECTED
- CHALLENGE_EXPIRED
- LOCKED

Allowed transitions include:
- DELIVERY_FAILED -> AWAITING_CHANNEL_SWITCH -> CHANNEL_SELECTED
- DELIVERY_ACCEPTED -> AWAITING_CHANNEL_SWITCH when the user reports non-receipt
- OTP_VALIDATION_PENDING -> OTP_REJECTED without changing channel
- Any active state -> CHALLENGE_EXPIRED or LOCKED when WSO2 policy requires it

The shared orchestrator must reject stale flow IDs, duplicate switch requests, cross-user challenge reuse, and transitions after expiry/lockout.

## Failure classification

SMS:
- Definite: provider connection failure, timeout, invalid provider response, explicit rejection.
- Ambiguous: provider accepted the request but handset delivery is unknown.
- The user may manually switch in either case.

Voice:
- Definite: ARI authentication/connectivity error, originate rejection, dialplan failure, channel failure, busy, or no-answer after configured timeout.
- Ambiguous: call answered but audio completion/recipient identity cannot be proven.
- The user may manually switch in either case.

Wrong OTP, expired OTP, and exceeded verification attempts are not delivery failures and must not automatically trigger fallback.

## OTP lifecycle and retry rules

- WSO2 owns challenge creation, OTP validation, expiry, and attempt counters.
- Prefer re-delivering the active WSO2 challenge through the alternate channel if the WSO2 7.3.0 extension API supports secure retrieval/reuse.
- Otherwise request WSO2 to issue a replacement challenge and invalidate the prior challenge through the supported lifecycle API. Never generate a code locally.
- Channel switches and resends share a bounded policy counter to prevent SMS/voice cost abuse.
- A successful delivery attempt must not reset verification failures or lockout counters.

## UI and accessibility

- Channel names are explicit: “SMS” and “Voice call”.
- After a failure, display a concise generic delivery message and the alternate action.
- Do not reveal provider internals, phone numbers, user existence, or sensitive account state.
- Mask destination details consistently across both channels.
- Preserve the current flow/session correlation and return the user to the same OTP entry step after switching.

## Acceptance criteria

1. SMS selected, provider rejects send: Voice fallback is offered and can complete the same MFA/recovery journey.
2. SMS accepted but not received: user can explicitly switch to Voice.
3. Voice selected, ARI/originate/dialplan fails: SMS fallback is offered.
4. Voice call is unanswered: SMS fallback is offered after the configured timeout.
5. Wrong OTP: no automatic channel switch; WSO2 attempt counter is incremented.
6. Expired OTP: no delivery fallback; WSO2 expiry behavior is preserved.
7. Repeated switch/resend attempts cannot bypass configured limits.
8. Concurrent or replayed switch requests cannot send duplicate deliveries.
9. MFA success is granted only after WSO2 OTP validation.
10. Password reset is allowed only after WSO2 recovery validation.
11. OTP is absent from URLs, ARI app args, logs, exceptions, and metrics.
12. Both journeys are verified on WSO2 IS 7.3.0 / Java 21; voice is verified on Asterisk 20+ with the selected channel language.
13. Tests cover SMS provider rejection, ambiguous acceptance, ARI timeout, busy/no-answer, user-initiated fallback, expiry, wrong OTP, lockout, replay, and idempotency.

## Integration gate

Do not assume that the built-in SMS OTP authenticator exposes its OTP for a second delivery channel, or that the legacy password-recovery notification hook covers the current WSO2 Flow Builder journey. Before implementing those adapters, verify the exact 7.3.0 extension contracts and preserve native WSO2 OTP/recovery state. If a supported extension point cannot expose a required operation, use a supported custom flow/authenticator rather than reflection, private APIs, or a fork of WSO2 internals.
