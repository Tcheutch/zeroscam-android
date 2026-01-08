package com.zeroscam.app.infra.research

import com.zeroscam.app.infra.research.dto.DetectionFeedbackDto
import com.zeroscam.app.infra.research.dto.FeedbackResponseDto
import com.zeroscam.app.infra.research.dto.HealthResponseDto
import com.zeroscam.app.infra.research.dto.ResearchSnapshotResponseDto
import com.zeroscam.app.infra.research.dto.UserRiskSnapshotDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ZeroScamResearchApi {
    @GET("/health/db")
    fun healthDb(): Call<HealthResponseDto>

    @POST("/v1/research/user-risk-snapshots")
    fun postUserRiskSnapshot(
        @Body snapshot: UserRiskSnapshotDto,
    ): Call<ResearchSnapshotResponseDto>

    @POST("/v1/research/feedback")
    fun postDetectionFeedback(
        @Body feedback: DetectionFeedbackDto,
    ): Call<FeedbackResponseDto>
}
