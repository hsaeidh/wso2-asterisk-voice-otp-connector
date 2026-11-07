# WSO2 IS 7.3.0 integration findings

## Verified implementation constraints

### Login MFA

The upstream WSO2 SMS OTP authenticator is a complete application authenticator. Its implementation owns OTP dispatch, retry/resend handling, and verification flow. The reviewed source does not expose a supported public operation that lets an unrelated voice adapter retrieve the active SMS OTP and deliver it through Asterisk.

Consequently, the connector must not bolt a second sender onto the native SMS OTP UI by reading its private context or internal OTP state. The likely supported direction is a single custom verification authenticator / custom login-flow component that owns channel selection while delegating challenge creation and validation to a supported WSO2 OTP API. The exact 7.3.0 challenge API and OSGi package versions must be confirmed before implementation.

### Password recovery

The reviewed WSO2 recovery implementation stores a hashed confirmation secret and records the selected notification channel in the recovery data. The legacy notification recovery manager explicitly recognizes EMAIL, SMS, and EXTERNAL as supported channels. Voice is not a built-in channel that can simply be selected by sending a new channel string.

The modern Flow Builder password-recovery journey is a separate integration surface. The connector must verify the supported custom action/component contract for WSO2 IS 7.3.0 before implementing a Voice OTP step. It must not replace or bypass the native recovery token lifecycle, use reflection to access private recovery data, or patch WSO2 internals as an undocumented dependency.

### Delivery acknowledgement semantics

A provider acceptance response is not proof that an SMS reached the handset. Likewise, an answered call is not proof that the intended user heard the digits. Therefore both journeys must offer an explicit alternate-channel action after the user reports non-receipt, even if the provider did not return a hard failure.

## Required implementation decision before WSO2 adapters

1. Pin the exact WSO2 IS 7.3.0 dependency/BOM versions and Java 21 build target.
2. Confirm the public OTP challenge generation/validation contract usable by a custom MFA authenticator.
3. Confirm the Flow Builder extension point that can add a Voice OTP delivery action to password recovery while retaining WSO2's recovery validation.
4. Confirm whether the same active challenge can be delivered over a second channel. If not, define a supported challenge replacement operation that invalidates the previous challenge atomically.
5. Confirm how the OTP can be made available to Asterisk's dialplan without exposing it in ARI arguments, URLs, logs, or events.

## Upstream references reviewed

- WSO2 SMS OTP authenticator source: https://github.com/wso2-extensions/identity-outbound-auth-sms-otp/blob/master/component/authenticator/src/main/java/org/wso2/carbon/identity/authenticator/smsotp/SMSOTPAuthenticator.java
- WSO2 password recovery manager source: https://github.com/wso2-extensions/identity-governance/blob/master/components/org.wso2.carbon.identity.recovery/src/main/java/org/wso2/carbon/identity/recovery/password/NotificationPasswordRecoveryManager.java
- WSO2 IS 7.3.0 SMS OTP guide: https://is.docs.wso2.com/en/7.3.0/guides/authentication/mfa/add-smsotp-login/
- WSO2 password recovery Flow Builder guide: https://is.docs.wso2.com/en/next/guides/flows/password-recovery/
- Asterisk SayDigits documentation: https://docs.asterisk.org/Certified-Asterisk_20.7_Documentation/API_Documentation/Dialplan_Applications/SayDigits/

This document records the integration boundary; it is not a claim that the WSO2 adapters are complete.
