package com.zeroscam.app.infra.research

import com.zeroscam.coredomain.ports.ResearchExportPort
import com.zeroscam.coredomain.ports.UserFeedbackRepository

/**
 * Fournit les implémentations infra (HTTP) des ports du core-domain
 * pour ZeroScam-Research.
 */
object ZeroScamResearchPortsProvider {
    data class ResearchPorts(
        val researchExportPort: ResearchExportPort,
        val userFeedbackRepository: UserFeedbackRepository,
    )

    fun createPorts(baseUrl: String): ResearchPorts {
        val api: ZeroScamResearchApi =
            ZeroScamResearchClientFactory.create(baseUrl)

        val researchExportPort: ResearchExportPort =
            HttpResearchExportPort(api)

        val userFeedbackRepository: UserFeedbackRepository =
            HttpUserFeedbackRepository(api)

        return ResearchPorts(
            researchExportPort = researchExportPort,
            userFeedbackRepository = userFeedbackRepository,
        )
    }
}
