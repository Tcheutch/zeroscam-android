package com.zeroscam.coredomain.usecase

import com.zeroscam.coredomain.model.DetectionFeedback
import com.zeroscam.coredomain.ports.UserFeedbackRepository

class RecordUserFeedbackUseCase(
    private val repository: UserFeedbackRepository,
) {
    operator fun invoke(feedback: DetectionFeedback) {
        repository.save(feedback)
    }
}
