# Changelog – ZeroScam Core Domain

## v1.0.0-core-domain-zeroscam – 2025-12-14

Première release stable du **core-domain ZeroScam**.  
Objectif : fournir une base propre, testée et extensible pour le moteur de détection multi-canal (appels, messages, paiements, posture device) côté mobile/app.

Cette version est taguée sur `main` sous le nom :

`v1.0.0-core-domain-zeroscam`

---

### 1. Portée fonctionnelle de cette V1

Cette release couvre le cœur métier suivant :

- **Détection de messages entrants**  
  via `AnalyzeIncomingMessageUseCase`
- **Évaluation de paiements avant exécution**  
  via `EvaluatePaymentIntentUseCase`
- **Boucle de feedback utilisateur structurée**  
  via `RecordUserFeedbackUseCase`
- **Évaluation de l’état de sécurité du device**  
  via `EvaluateDeviceSecurityStateUseCase`
- **Interfaces de ports stables** pour la couche data / infra :
  - `ThreatIntelRepository`
  - `DetectionLogRepository`
  - `MessageScamDetector`
  - `PaymentRiskEngine`
  - `DeviceThreatDetector`
  - `UserFeedbackRepository`
  - `UserProfileRepository`
  - `SubscriptionRepository`
  - etc.

Tous les use cases sont **couverts par des tests unitaires** et passent :

- `./gradlew :core-domain:compileKotlin`
- `./gradlew :core-domain:test`
- `./gradlew :core-domain:ktlintCheck :app:ktlintCheck`

---

### 2. Modèle fonctionnel & enums du domaine

#### 2.1. Modèles principaux

Les principaux agrégats métiers introduits dans cette V1 :

- `DetectionResult`  
  Représente le résultat standardisé d’une détection :
  - `riskLevel` (`RiskLevel`)
  - `confidenceScore` (`Double`)
  - `scamType` (`ScamType?`)
  - `attackVectors` (`List<AttackVector>`)
  - `reasons` (`List<String>`)
  - `channel` (`DetectionChannel`)
  - `createdAt` (`Instant`)

- `Message`  
  Message entrant (SMS, WhatsApp, email, etc.) :
  - `id`, `userId`
  - `channel` (`DetectionChannel`)
  - `content`
  - `receivedAt`

- `PhoneCall`  
  Appel entrant/sortant avec :
  - direction (`CallDirection`)
  - numéro, heure, etc.

- `PaymentIntent`  
  Intention de paiement avant exécution :
  - montant, devise, canal (`PaymentChannel`)
  - destination (IBAN, wallet, etc.)

- `DeviceSecuritySnapshot`  
  Instantané de posture device :
  - `isRootedOrJailbroken`
  - `isEmulator`
  - `hasDebuggableBuild`
  - `hasSuspiciousApps`
  - `integrityCheckPassed`
  - `capturedAt`, `userId`

- `DetectionFeedback`  
  Retour utilisateur sur une détection :
  - `detectionId`, `userId`
  - `channel` (`DetectionChannel`)
  - `isScam` (scam confirmé ou non)
  - `label` (`FeedbackLabel`)
  - `comment` (optionnel, nettoyé / tronqué)
  - `createdAt`, `createdAtEpochSeconds`

- `Subscription` / `UserProfile`  
  Profils utilisateur et plans d’abonnement, utilisés notamment pour adapter la politique de risque.

#### 2.2. Enums clés

- `RiskLevel` : `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- `ScamType` : familles d’attaques (phishing, vishing, spoofing, etc.)
- `DetectionChannel` : `CALL`, `MESSAGE`, `PAYMENT`, `DEVICE_SECURITY`, …
- `PaymentChannel` : MoMo, wallet, card, bank transfer, crypto, etc. (via noms de variants + heuristique).
- `AttackVector` : listes de vecteurs techniques (QR code, hash-jacking, device, etc.)
- `FeedbackLabel` : `TRUE_POSITIVE`, `FALSE_POSITIVE`, etc.
- `SubscriptionPlan`, `SubscriptionStatus` : base pour politique de risque liée au plan.

---

### 3. Use cases livrés en V1

#### 3.1. AnalyzeIncomingMessageUseCase (Message/SMS/WhatsApp/email)

Pipeline **“béton”** pour les messages entrants :

1. **Score brut via `MessageScamDetector`**  
   - Modèle ML + règles (patterns de liens, mots-clés “urgent”, OTP, etc.)
   - Retourne un `DetectionResult` initial.

2. **Enrichissement threat-intel par URLs**
   - Extraction des URLs dans le contenu (`URL_REGEX`).
   - Vérification via `ThreatIntelRepository.isKnownScamUrl`.
   - Ajout de la raison `url_known_scam` si nécessaire.

3. **Ajustement global via `ThreatIntelRepository.adjustMessageResult`**
   - Prise en compte d’IOC, listes dynamiques, signaux back-end.

4. **Escalades déterministes**
   - Si sender ou URL marqués scam :
     - `sender_known_scam` / `url_known_scam`
     - escalades → `HIGH` ou `CRITICAL` selon combinaison.
   - Signaux de contenu :
     - `suspicious_link_pattern`
     - `message_content_anomaly`
   - QR codes :
     - `qr_code_present` / `qr_code_payment`
     - QR générique → bump modéré.
     - QR + sémantique paiement (“scan to pay”, “MoMo”, crypto, etc.) → escalade agressive jusqu’à `HIGH`/`CRITICAL`.

5. **Journalisation via `DetectionLogRepository.logMessageDetection`**
   - Traçabilité complète pour audit & feedback loop.

✅ Tests unitaires :  
`AnalyzeIncomingMessageUseCaseTest` couvre notamment :

- combinaison sender + URL scam → `CRITICAL` + forte confiance,
- scénarios QR code paiement vs QR générique,
- absence de signaux → résultat intact.

---

#### 3.2. EvaluatePaymentIntentUseCase (Paiements)

Use case d’évaluation de risque sur une intention de paiement avant exécution :

1. **Score brut via `PaymentRiskEngine.analyze(paymentIntent)`**
   - Signaux transactionnels + éventuels features device/hash.

2. **Ajustement via `ThreatIntelRepository.adjustPaymentResult`**
   - Connaissance des destinations scam, blacklists, etc.

3. **Escalades déterministes**
   - Destination connue scam :
     - Ajout de raison dédiée + escalade min. `HIGH`.
   - Signaux techniques forts :
     - `hash_jacking_suspected`
     - `device_compromised`
     - `device_jailbroken`
   - Combinaisons de signaux → escalades plus agressives.

4. **Politique Afrique / canal / plan**
   - Classification régionale par devise :
     - `XAF`, `XOF` → CEMAC / UEMOA
     - `NGN`, `GHS`, `KES`, `ZAR`, etc. → AFRICA_OTHER
     - `USD`, `EUR`, `GBP` → NON_AFRICA
   - Heuristique de risque par canal via `PaymentChannel` (`toString().lowercase()`):
     - `mobile`, `momo`, `wallet`, `ussd`, `crypto` → HIGH
     - `card`, `debit`, `credit`, `transfer`, `bank` → MEDIUM
     - sinon → LOW
   - Politique par plan (`SubscriptionPlan`) via `toString()` :
     - plans contenant “free” ou “trial” → low touch,
     - tout le reste → high touch (politique plus agressive pour minimiser les faux négatifs).

5. **Journalisation via `DetectionLogRepository.logPaymentDetection`**

✅ Tests unitaires :  
`EvaluatePaymentIntentUseCaseTest` vérifie :

- escalade sur destination connue scam,
- comportement différent FREE vs plan payant,
- prise en compte contextuelle Afrique / non-Afrique + type de canal.

---

#### 3.3. EvaluateDeviceSecurityStateUseCase (Posture device – V1)

V1 volontairement simple, stable, prête pour une V2 agressive :

1. **Analyse brute via `DeviceThreatDetector.analyze(snapshot)`**
2. **Ajustement via `ThreatIntelRepository.adjustDeviceSecurityResult(snapshot, initialResult)`**
3. **Journalisation via `DetectionLogRepository.logDeviceSecurityDetection(snapshot, result)`**

➡️ V1 définit le contrat et la chaîne d’appel.  
La **V2** pourra durcir la politique (root/jailbreak → `CRITICAL`, `integrityCheckPassed = false` → `CRITICAL`, etc.) sans casser l’interface ni les autres use cases.

---

#### 3.4. RecordUserFeedbackUseCase (feedback loop)

Use case dédié à la boucle de feedback utilisateur :

- Crée un `DetectionFeedback` propre à partir des paramètres :
  - `detectionId`, `userId`, `channel`, `isScam`, `label`, `comment`, `createdAt?`
- **Nettoyage / validation du commentaire** :
  - `trim()`
  - chaîne blanche → `null`
  - tronquage à une longueur maximale (1_000 caractères) pour éviter les payloads énormes.
- **Cohérence temporelle** :
  - `createdAt` injecté ou auto-généré.
  - `createdAtEpochSeconds` calculé systématiquement à partir de `createdAt`.
- Persistance via `UserFeedbackRepository.saveFeedback`.

✅ Tests unitaires :  
`RecordUserFeedbackUseCaseTest` vérifie :

- nettoyage et tronquage d’un long commentaire,
- conversion automatique des commentaires blancs en `null`,
- cohérence `createdAt` / `createdAtEpochSeconds`,
- identité entre l’instance retournée et l’instance sauvegardée.

---

### 4. Qualité, conventions et outillage

- **Style Kotlin** : conformes aux règles `ktlint` définies dans le projet :
  - imports ordonnés,
  - pas de blancs en début de corps de classe,
  - pas d’espaces incorrects dans les listes d’arguments, etc.
- **Build health** (au moment du tag) :

```bash
./gradlew :core-domain:compileKotlin
./gradlew :core-domain:test
./gradlew :core-domain:ktlintCheck :app:ktlintCheck
