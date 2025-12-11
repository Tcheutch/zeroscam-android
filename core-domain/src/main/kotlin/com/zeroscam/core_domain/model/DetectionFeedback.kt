package com.zeroscam.core_domain.model

import com.zeroscam.core_domain.value.UserId
import java.time.Instant

data class DetectionFeedback(
    val detectionId: String,
    val userId: UserId,
    val label: FeedbackLabel,
    val comment: String?,
    val createdAt: Instant,
)

enum class FeedbackLabel {
    TRUE_POSITIVE,
    FALSE_POSITIVE,
    TRUE_NEGATIVE,
    FALSE_NEGATIVE,
}
