package com.zeroscam.core_domain.usecase

import com.zeroscam.core_domain.model.DetectionFeedback
import com.zeroscam.core_domain.ports.UserFeedbackRepository

class RecordUserFeedbackUseCase(
    private val repository: UserFeedbackRepository,
) {
    operator fun invoke(feedback: DetectionFeedback) {
        repository.save(feedback)
    }
}
