package com.zeroscam.coredomain.model

import com.zeroscam.coredomain.enums.DetectionChannel
import com.zeroscam.coredomain.enums.FeedbackLabel
import com.zeroscam.coredomain.value.UserId
import java.time.Instant

/**
 * Origine du feedback remonté à ZeroScam.
 */
enum class FeedbackOrigin {
    /**
     * Feedback fourni directement par l'utilisateur final (UI mobile par ex.).
     */
    END_USER,

    /**
     * Feedback fourni par un analyste / opérateur humain (back-office).
     */
    ANALYST,

    /**
     * Feedback dérivé automatiquement par ZeroScam-Research
     * (ex. auto-labeling ou heuristiques offline).
     */
    AUTO_INFERRED,
}

/**
 * Feedback sur une détection donnée (message / appel / paiement / device).
 *
 * Ce modèle est la "vérité métier" côté core-domain et sert aussi de base
 * au contrat REST avec ZeroScam-Research.
 */
data class DetectionFeedback(
    val detectionId: String,
    val userId: UserId,
    val channel: DetectionChannel,
    /**
     * Vision binaire de l'utilisateur / analyste :
     *  - true  → il considère que c'est frauduleux / scam.
     *  - false → il considère que c'est légitime.
     *  - null  → il ne sait pas / ne se prononce pas.
     *
     * Combiné avec [label] pour affiner l'interprétation.
     */
    val isScam: Boolean?,
    /**
     * Label métier plus riche (TP/FP/FN/NEW_SCAM_PATTERN, etc.).
     */
    val label: FeedbackLabel,
    /**
     * Commentaire libre, éventuellement nettoyé / tronqué par le use case.
     */
    val comment: String?,
    /**
     * Horodatage du feedback (instant exact).
     */
    val createdAt: Instant,
    /**
     * Horodatage en secondes epoch, redondant mais pratique
     * pour le stockage indexé / analytics.
     */
    val createdAtEpochSeconds: Long,
    /**
     * Origine du feedback (end user, analyst, auto-inferred, ...).
     * Valeur par défaut : END_USER pour compat ascendante.
     */
    val origin: FeedbackOrigin = FeedbackOrigin.END_USER,
    /**
     * Métadonnées additionnelles (clé/valeur) pour ZeroScam-Research :
     *  - appVersion
     *  - osVersion
     *  - deviceCountry
     *  - experimentVariant, etc.
     */
    val metadata: Map<String, String>? = null,
)
