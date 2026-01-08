package com.zeroscam.app.infra.research.dto

import com.squareup.moshi.Json
import com.zeroscam.coredomain.model.DetectionFeedback
import com.zeroscam.coredomain.ports.UserRiskSnapshot
import com.zeroscam.coredomain.value.UserId
import java.time.Instant
import java.util.UUID

/**
 * DTOs REST pour ZeroScam-Research.
 *
 * Contrainte béton armé :
 * - core-domain a déjà évolué (id/createdAt parfois absents) => on sécurise via réflexion.
 */

data class UserRiskSnapshotDto(
    @Json(name = "snapshotId")
    val snapshotId: String,
    @Json(name = "userId")
    val userId: String,
    @Json(name = "createdAt")
    val createdAt: String,
    @Json(name = "globalRiskLevel")
    val globalRiskLevel: String,
    @Json(name = "globalConfidence")
    val globalConfidence: Double,
    @Json(name = "detections")
    val detections: List<DetectionResultDto>,
) {
    companion object {
        fun fromDomain(domain: UserRiskSnapshot): UserRiskSnapshotDto {
            val snapshotId =
                readAsString(domain, "id")
                    ?: readAsString(domain, "snapshotId")
                    ?: "snap-${UUID.randomUUID()}"

            val createdAt =
                read(domain, "createdAt")
                    ?.let { it.toString() }
                    ?: Instant.now().toString()

            val userId =
                when (val u = read(domain, "userId")) {
                    is UserId -> u.value
                    is String -> u
                    else -> u?.toString() ?: "unknown"
                }

            val globalRiskLevel =
                enumNameOrString(
                    read(domain, "globalRiskLevel") ?: read(domain, "riskLevel"),
                ) ?: "UNKNOWN"

            val globalConfidence =
                readAsDouble(domain, "globalConfidence")
                    ?: readAsDouble(domain, "confidenceScore")
                    ?: 0.0

            val detectionsAny =
                (read(domain, "detections") as? List<*>) ?: emptyList<Any?>()

            val detections =
                detectionsAny
                    .mapNotNull { it?.let { DetectionResultDto.fromAny(it) } }

            return UserRiskSnapshotDto(
                snapshotId = snapshotId,
                userId = userId,
                createdAt = createdAt,
                globalRiskLevel = globalRiskLevel,
                globalConfidence = globalConfidence,
                detections = detections,
            )
        }
    }
}

data class DetectionResultDto(
    @Json(name = "id")
    val id: String,
    @Json(name = "userId")
    val userId: String?,
    @Json(name = "createdAt")
    val createdAt: String?,
    @Json(name = "channel")
    val channel: String?,
    @Json(name = "riskLevel")
    val riskLevel: String?,
    @Json(name = "confidenceScore")
    val confidenceScore: Double?,
    @Json(name = "reasons")
    val reasons: List<String>?,
    @Json(name = "recommendation")
    val recommendation: String?,
) {
    companion object {
        fun fromAny(d: Any): DetectionResultDto {
            val id =
                readAsString(d, "id")
                    ?: readAsString(d, "detectionId")
                    ?: "det-${UUID.randomUUID()}"

            val userId =
                when (val u = read(d, "userId")) {
                    is UserId -> u.value
                    is String -> u
                    else -> u?.toString()
                }

            val createdAt = read(d, "createdAt")?.toString()
            val channel = enumNameOrString(read(d, "channel"))
            val riskLevel = enumNameOrString(read(d, "riskLevel"))
            val confidenceScore =
                readAsDouble(d, "confidenceScore") ?: readAsDouble(d, "confidence")

            val reasons =
                (read(d, "reasons") as? List<*>)?.mapNotNull { it?.toString() }

            val recommendation = readAsString(d, "recommendation")

            return DetectionResultDto(
                id = id,
                userId = userId,
                createdAt = createdAt,
                channel = channel,
                riskLevel = riskLevel,
                confidenceScore = confidenceScore,
                reasons = reasons,
                recommendation = recommendation,
            )
        }
    }
}

data class DetectionFeedbackDto(
    @Json(name = "detectionId")
    val detectionId: String,
    @Json(name = "userId")
    val userId: String,
    @Json(name = "channel")
    val channel: String,
    @Json(name = "isScam")
    val isScam: Boolean?,
    @Json(name = "label")
    val label: String,
    @Json(name = "comment")
    val comment: String?,
    @Json(name = "createdAt")
    val createdAt: String,
    @Json(name = "createdAtEpochSeconds")
    val createdAtEpochSeconds: Long,
    @Json(name = "origin")
    val origin: String?,
    @Json(name = "metadata")
    val metadata: Map<String, String>?,
) {
    companion object {
        fun fromDomain(f: DetectionFeedback): DetectionFeedbackDto =
            DetectionFeedbackDto(
                detectionId = f.detectionId,
                userId = f.userId.value,
                channel = f.channel.name,
                isScam = f.isScam,
                label = f.label.name,
                comment = f.comment,
                createdAt = f.createdAt.toString(),
                createdAtEpochSeconds = f.createdAtEpochSeconds,
                origin = runCatching { f.origin.name }.getOrNull(),
                metadata = f.metadata,
            )
    }
}

/** DTOs réponses backend */
data class ResearchSnapshotResponseDto(
    val status: String,
    val snapshotId: String?,
    val userId: String?,
)

data class FeedbackResponseDto(
    val status: String,
    val feedbackId: String?,
    val detectionId: String?,
    val label: String?,
)

data class HealthResponseDto(
    val status: String,
)

// ----------------- Helpers réflexion (béton armé) -----------------

private fun read(
    target: Any,
    prop: String,
): Any? {
    val cap = prop.replaceFirstChar { it.uppercaseChar() }
    // getter getX()
    target.javaClass.methods
        .firstOrNull { it.name == "get$cap" && it.parameterCount == 0 }
        ?.let { return runCatching { it.invoke(target) }.getOrNull() }

    // field x
    target.javaClass.declaredFields
        .firstOrNull { it.name == prop }
        ?.let {
            return runCatching {
                it.isAccessible = true
                it.get(target)
            }.getOrNull()
        }

    return null
}

private fun readAsString(
    target: Any,
    prop: String,
): String? = read(target, prop)?.toString()

private fun readAsDouble(
    target: Any,
    prop: String,
): Double? {
    val v = read(target, prop) ?: return null
    return when (v) {
        is Double -> v
        is Float -> v.toDouble()
        is Int -> v.toDouble()
        is Long -> v.toDouble()
        is Number -> v.toDouble()
        is String -> v.toDoubleOrNull()
        else -> null
    }
}

private fun enumNameOrString(v: Any?): String? =
    when (v) {
        null -> null
        is Enum<*> -> v.name
        else -> v.toString()
    }
