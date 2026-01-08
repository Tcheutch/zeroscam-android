package com.zeroscam.app.infra.research

import android.util.Log
import com.zeroscam.app.infra.research.dto.UserRiskSnapshotDto
import com.zeroscam.coredomain.ports.ResearchExportPort
import com.zeroscam.coredomain.ports.UserRiskSnapshot

/**
 * Implémentation HTTP de ResearchExportPort qui parle au backend ZeroScam-Research.
 *
 * Important : appel synchrone (execute()) -> à appeler hors main thread (IO dispatcher).
 */
class HttpResearchExportPort(
    private val api: ZeroScamResearchApi,
) : ResearchExportPort {
    override fun publishUserRiskSnapshot(snapshot: UserRiskSnapshot) {
        try {
            val dto = UserRiskSnapshotDto.fromDomain(snapshot)
            val response = api.postUserRiskSnapshot(dto).execute()

            if (!response.isSuccessful) {
                Log.e(
                    TAG,
                    "Failed to publish UserRiskSnapshot: code=${response.code()}, " +
                        "message=${response.message()}",
                )
            } else {
                Log.d(
                    TAG,
                    "UserRiskSnapshot published: snapshotId=${dto.snapshotId}, " +
                        "userId=${dto.userId}, detections=${dto.detections.size}",
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while publishing UserRiskSnapshot", e)
            // on ne propage pas au domaine : snapshot perdu mais pas de crash
        }
    }

    companion object {
        private const val TAG = "HttpResearchExportPort"
    }
}
