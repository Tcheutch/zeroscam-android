package com.zeroscam.coredomain.ports

import com.zeroscam.coredomain.model.DetectionFeedback

interface UserFeedbackRepository {
    fun save(feedback: DetectionFeedback)
}
