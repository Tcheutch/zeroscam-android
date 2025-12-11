package com.zeroscam.core_domain.ports

import com.zeroscam.core_domain.model.DetectionFeedback

interface UserFeedbackRepository {
    fun save(feedback: DetectionFeedback)
}
