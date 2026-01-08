# Changelog – ZeroScam Core Domain

## v1.0.0-core-domain-zeroscam – 2025-12-14

First stable release of the **ZeroScam core domain**.  
Goal: provide a clean, tested, and extensible foundation for the multi-channel detection engine (calls, messages, payments, device posture) on the app/mobile side.

This version is tagged on `main` as:

`v1.0.0-core-domain-zeroscam`

---

### 1. Functional scope of this V1

This release covers the following core business logic:

- **Incoming message detection**  
  via `AnalyzeIncomingMessageUseCase`
- **Pre-execution payment risk evaluation**  
  via `EvaluatePaymentIntentUseCase`
- **Structured user feedback loop**  
  via `RecordUserFeedbackUseCase`
- **Device security posture evaluation**  
  via `EvaluateDeviceSecurityStateUseCase`
- **Stable port interfaces** for data/infra layer:
  - `ThreatIntelRepository`
  - `DetectionLogRepository`
  - `MessageScamDetector`
  - `PaymentRiskEngine`
  - `DeviceThreatDetector`
  - `UserFeedbackRepository`
  - `UserProfileRepository`
  - `SubscriptionRepository`
  - etc.

All use cases are **covered by unit tests** and pass:

- `./gradlew :core-domain:compileKotlin`
- `./gradlew :core-domain:test`
- `./gradlew :core-domain:ktlintCheck :app:ktlintCheck`

---

### 2. Domain model & enums

#### 2.1. Main models

The main domain aggregates introduced in this V1:

- `DetectionResult`  
  Standardized detection output:
  - `riskLevel` (`RiskLevel`)
  - `confidenceScore` (`Double`)
  - `scamType` (`ScamType?`)
  - `attackVectors` (`List<AttackVector>`)
  - `reasons` (`List<String>`)
  - `channel` (`DetectionChannel`)
  - `createdAt` (`Instant`)

- `Message`  
  Incoming message (SMS, WhatsApp, email, etc.):
  - `id`, `userId`
  - `channel` (`DetectionChannel`)
  - `content`
  - `receivedAt`

- `PhoneCall`  
  Incoming / outgoing call:
  - call direction (`CallDirection`)
  - phone number, timestamp, etc.

- `PaymentIntent`  
  Payment intent evaluated before execution:
  - amount, currency, channel (`PaymentChannel`)
  - destination (IBAN, wallet, etc.)

- `DeviceSecuritySnapshot`  
  Device security posture snapshot:
  - `isRootedOrJailbroken`
  - `isEmulator`
  - `hasDebuggableBuild`
  - `hasSuspiciousApps`
  - `integrityCheckPassed`
  - `capturedAt`, `userId`

- `DetectionFeedback`  
  User feedback on a detection:
  - `detectionId`, `userId`
  - `channel` (`DetectionChannel`)
  - `isScam` (confirmed scam or not)
  - `label` (`FeedbackLabel`)
  - `comment` (optional, cleaned / truncated)
  - `createdAt`, `createdAtEpochSeconds`

- `Subscription` / `UserProfile`  
  User profiles and subscription plans, used to adjust risk policy.

#### 2.2. Key enums

- `RiskLevel`: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- `ScamType`: high-level attack families (phishing, vishing, spoofing, etc.)
- `DetectionChannel`: `CALL`, `MESSAGE`, `PAYMENT`, `DEVICE_SECURITY`, …
- `PaymentChannel`: MoMo, wallet, card, bank transfer, crypto, etc.  
  (interpreted via variant names + heuristics).
- `AttackVector`: list of technical vectors (QR code, hash-jacking, device, etc.)
- `FeedbackLabel`: `TRUE_POSITIVE`, `FALSE_POSITIVE`, etc.
- `SubscriptionPlan`, `SubscriptionStatus`: used to drive risk policy by plan.

---

### 3. Use cases shipped in V1

#### 3.1. AnalyzeIncomingMessageUseCase (Message/SMS/WhatsApp/email)

**“Concrete” / production-grade** pipeline for incoming messages:

1. **Base scoring via `MessageScamDetector`**  
   - ML model + rules (link patterns, “urgent”, OTP, “account locked”, etc.)
   - Returns an initial `DetectionResult`.

2. **Threat intel enrichment via URLs**
   - Extracts URLs from content (`URL_REGEX`).
   - Checks each URL via `ThreatIntelRepository.isKnownScamUrl`.
   - Adds reason `url_known_scam` when at least one URL is known as scam.

3. **Global adjustment via `ThreatIntelRepository.adjustMessageResult`**
   - External IOC, dynamic lists, backend signals, etc.

4. **Deterministic escalations**
   - If sender or URL is marked as scam:
     - `sender_known_scam` / `url_known_scam`
     - escalates to at least `HIGH`, up to `CRITICAL` when combined.
   - Suspicious content signals:
     - `suspicious_link_pattern`
     - `message_content_anomaly`
   - QR-code signals:
     - `qr_code_present` / `qr_code_payment`
     - generic QR → moderate bump,
     - QR + payment semantics (“scan to pay”, “MoMo”, crypto, etc.) → aggressive escalation to `HIGH` / `CRITICAL`.

5. **Logging via `DetectionLogRepository.logMessageDetection`**
   - Full traceability for audit and feedback loop.

✅ Unit tests:  
`AnalyzeIncomingMessageUseCaseTest` covers in particular:

- sender + URL both scam → `CRITICAL` + high confidence,
- QR payment vs generic QR scenarios,
- no signal → result unchanged.

---

#### 3.2. EvaluatePaymentIntentUseCase (Payments)

Use case to evaluate payment risk before execution:

1. **Base scoring via `PaymentRiskEngine.analyze(paymentIntent)`**
   - Transactional features + potential device/hash features.

2. **Threat intel adjustment via `ThreatIntelRepository.adjustPaymentResult`**
   - Knowledge of scam destinations, blacklists, etc.

3. **Deterministic escalations**
   - Known scam destination:
     - dedicated reason + minimum `HIGH` escalation.
   - Strong technical signals:
     - `hash_jacking_suspected`
     - `device_compromised`
     - `device_jailbroken`
   - Combination of signals → more aggressive escalations.

4. **Africa / channel / subscription plan policy**
   - Region classification by currency:
     - `XAF`, `XOF` → CEMAC / UEMOA
     - `NGN`, `GHS`, `KES`, `ZAR`, etc. → AFRICA_OTHER
     - `USD`, `EUR`, `GBP` → NON_AFRICA
   - Channel risk classification via `PaymentChannel.toString().lowercase()`:
     - `mobile`, `momo`, `wallet`, `ussd`, `crypto` → HIGH
     - `card`, `debit`, `credit`, `transfer`, `bank` → MEDIUM
     - otherwise → LOW
   - Plan-based behavior (`SubscriptionPlan` via `toString()`):
     - names containing “free” or “trial” → low-touch,
     - everything else → high-touch (more aggressive policy to reduce false negatives).

5. **Logging via `DetectionLogRepository.logPaymentDetection`**

✅ Unit tests:  
`EvaluatePaymentIntentUseCaseTest` verifies:

- escalation on known scam destination,
- different behavior for FREE vs paid plans,
- contextual handling for Africa vs non-Africa and channel type.

---

#### 3.3. EvaluateDeviceSecurityStateUseCase (Device posture – V1)

V1 is intentionally simple, stable, and ready for a stricter V2:

1. **Base analysis via `DeviceThreatDetector.analyze(snapshot)`**
2. **Adjustment via `ThreatIntelRepository.adjustDeviceSecurityResult(snapshot, initialResult)`**
3. **Logging via `DetectionLogRepository.logDeviceSecurityDetection(snapshot, result)`**

➡️ V1 sets the contract and call chain.  
**V2** will be able to harden policy (root/jailbreak → `CRITICAL`, `integrityCheckPassed = false` → `CRITICAL`, etc.) without breaking interfaces or other use cases.

---

#### 3.4. RecordUserFeedbackUseCase (Feedback loop)

Use case dedicated to the user feedback loop:

- Creates a clean `DetectionFeedback` instance from parameters:
  - `detectionId`, `userId`, `channel`, `isScam`, `label`, `comment`, `createdAt?`
- **Comment sanitization**:
  - `trim()` whitespace,
  - blank strings → `null`,
  - max length enforced (1_000 chars) to avoid huge payloads.
- **Time consistency**:
  - `createdAt` injected or auto-generated,
  - `createdAtEpochSeconds` always derived from `createdAt`.
- Persists via `UserFeedbackRepository.saveFeedback`.

✅ Unit tests:  
`RecordUserFeedbackUseCaseTest` verifies:

- cleaning and truncation of long comments,
- conversion of blank comments to `null`,
- consistency between `createdAt` and `createdAtEpochSeconds`,
- returned instance equals the persisted instance.

---

### 4. Quality, conventions, toolchain

- **Kotlin style**: compliant with the project’s `ktlint` rules:
  - ordered imports,
  - no leading blank lines in class bodies,
  - no invalid spaces in argument lists, etc.
- **Build health** (at tag time):

```bash
./gradlew :core-domain:compileKotlin
./gradlew :core-domain:test
./gradlew :core-domain:ktlintCheck :app:ktlintCheck


sequenceDiagram
    participant App as ZeroScam App (mobile)
    participant Core as ZeroScam Core-Domain
    participant API as ZeroScam-API (backend)
    participant Research as ZeroScam-Research

    Note over App,Core: Décision initiale

    App->>Core: ZeroScamOrchestratorUseCase(request)
    Core-->>App: ZeroScamOrchestrationResult(aggregatedUserRisk, detections)

    App->>API: POST /v1/research/user-risk-snapshots (UserRiskSnapshot)
    API->>Research: ingest snapshot
    Research-->>API: 202 Accepted
    API-->>App: 202 Accepted

    Note over App,Research: Feedback utilisateur/analyste

    App->>API: POST /v1/research/feedback (DetectionFeedback)
    API->>Research: store DetectionFeedback
    Research-->>API: 201 Created
    API-->>App: 201 Created

    Note over Research: Offline training & tuning

    Research->>Research: Analyse snapshots + feedbacks\n(recall/precision, FN/FP, NEW_SCAM_PATTERN, etc.)
    Research->>Research: Compute new thresholds & channelWeights\n(AggregateUserRiskConfig vN+1)

    Note over API,Research: Publication de config

    Research->>API: POST /v1/research/core-config (AggregateUserRiskConfig vN+1)
    API-->>Research: 200 OK

    Note over App,API: Récupération côté clients

    App->>API: GET /v1/config/core-domain
    API-->>App: AggregateUserRiskConfig vN+1 (JSON)
    App->>Core: Inject new AggregateUserRiskConfig(...)
    Core-->>App: Next decisions use updated thresholds
