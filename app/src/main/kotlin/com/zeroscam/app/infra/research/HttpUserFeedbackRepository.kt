package com.zeroscam.app.infra.research

import android.util.Log
import com.zeroscam.app.infra.research.dto.DetectionFeedbackDto
import com.zeroscam.coredomain.model.DetectionFeedback
import com.zeroscam.coredomain.ports.UserFeedbackRepository

/**
 * Implémentation HTTP de UserFeedbackRepository qui envoie les feedbacks
 * vers ZeroScam-Research (/v1/research/feedback).
 */
class HttpUserFeedbackRepository(
    private val api: ZeroScamResearchApi,
) : UserFeedbackRepository {
    override fun saveFeedback(feedback: DetectionFeedback) {
        try {
            val dto = DetectionFeedbackDto.fromDomain(feedback)
            val response = api.postDetectionFeedback(dto).execute()

            if (!response.isSuccessful) {
                Log.e(
                    TAG,
                    "Failed to post DetectionFeedback: code=${response.code()}, " +
                        "message=${response.message()}",
                )
            } else {
                Log.d(
                    TAG,
                    "DetectionFeedback posted: detectionId=${dto.detectionId}, " +
                        "userId=${dto.userId}, channel=${dto.channel}",
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while posting DetectionFeedback", e)
            // on évite de faire planter le flot métier
        }
    }

    companion object {
        private const val TAG = "HttpUserFeedbackRepo"
    }
}
