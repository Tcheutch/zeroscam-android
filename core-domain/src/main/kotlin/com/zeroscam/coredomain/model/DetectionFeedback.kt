package com.zeroscam.coredomain.model

import com.zeroscam.coredomain.enums.DetectionChannel
import com.zeroscam.coredomain.enums.FeedbackLabel
import com.zeroscam.coredomain.value.UserId
import java.time.Instant

/**
 * Feedback utilisateur sur une détection donnée.
 *
 * Ce modèle est pensé pour être :
 *  - stockable côté data (DB / backend),
 *  - exploitable pour la boucle de réentraînement Vigilis-Research.
 */
data class DetectionFeedback(
    val detectionId: String,
    val userId: UserId,
    val channel: DetectionChannel,
    val isScam: Boolean,
    val label: FeedbackLabel,
    val comment: String?,
    val createdAt: Instant,
    val createdAtEpochSeconds: Long,
)
