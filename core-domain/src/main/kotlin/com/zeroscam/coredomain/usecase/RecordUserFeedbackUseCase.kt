package com.zeroscam.coredomain.usecase

import com.zeroscam.coredomain.enums.DetectionChannel
import com.zeroscam.coredomain.enums.FeedbackLabel
import com.zeroscam.coredomain.model.DetectionFeedback
import com.zeroscam.coredomain.ports.UserFeedbackRepository
import com.zeroscam.coredomain.value.UserId
import java.time.Instant

/**
 * Use case : enregistrement d'un feedback utilisateur sur une détection.
 *
 * Objectifs :
 *  - normaliser les feedbacks (sanitization du commentaire, horodatage),
 *  - préparer les données pour la boucle de réentraînement Vigilis-Research,
 *  - rester 100% pur domaine (pas de dépendance Android/infra).
 */
class RecordUserFeedbackUseCase(
    private val userFeedbackRepository: UserFeedbackRepository,
) {
    /**
     * Enregistre un feedback utilisateur.
     *
     * @param detectionId   id de la détection (DetectionResult.id côté domaine).
     * @param userId        id fonctionnel de l'utilisateur.
     * @param channel       canal de détection (CALL, MESSAGE, PAYMENT, DEVICE_SECURITY, etc.).
     * @param isScam        verdict utilisateur : c'était bien un scam ou pas.
     * @param label         label de vérité (TP / FP / TN / FN).
     * @param comment       commentaire optionnel, libre.
     * @param createdAt     horodatage du feedback (par défaut maintenant, en UTC).
     */
    operator fun invoke(
        detectionId: String,
        userId: UserId,
        channel: DetectionChannel,
        isScam: Boolean,
        label: FeedbackLabel,
        comment: String?,
        createdAt: Instant = Instant.now(),
    ): DetectionFeedback {
        val sanitizedComment =
            comment
                ?.takeIf { it.isNotBlank() }
                ?.take(MAX_COMMENT_LENGTH)

        val feedback =
            DetectionFeedback(
                detectionId = detectionId,
                userId = userId,
                channel = channel,
                isScam = isScam,
                label = label,
                comment = sanitizedComment,
                createdAt = createdAt,
                createdAtEpochSeconds = createdAt.epochSecond,
            )

        userFeedbackRepository.saveFeedback(feedback)

        return feedback
    }

    companion object {
        // On limite la taille du commentaire pour éviter les payloads monstrueux.
        private const val MAX_COMMENT_LENGTH = 1_000
    }
}
