package com.zeroscam.app.di

import com.zeroscam.app.BuildConfig
import com.zeroscam.app.infra.research.ZeroScamResearchPortsProvider
import com.zeroscam.coredomain.ports.ResearchExportPort
import com.zeroscam.coredomain.ports.UserFeedbackRepository
import com.zeroscam.coredomain.usecase.AggregateUserRiskUseCase
import com.zeroscam.coredomain.usecase.AnalyzeIncomingCallUseCase
import com.zeroscam.coredomain.usecase.AnalyzeIncomingMessageUseCase
import com.zeroscam.coredomain.usecase.EvaluateDeviceSecurityStateUseCase
import com.zeroscam.coredomain.usecase.EvaluatePaymentIntentUseCase
import com.zeroscam.coredomain.usecase.RecordUserFeedbackUseCase
import com.zeroscam.coredomain.usecase.ZeroScamOrchestratorUseCase

object ZeroScamCompositionRoot {
    private const val BASE_URL: String = BuildConfig.RESEARCH_BASE_URL

    // ✅ type explicite => plus d'erreurs "Cannot infer T"
    private val researchPorts: ZeroScamResearchPortsProvider.ResearchPorts by lazy {
        ZeroScamResearchPortsProvider.createPorts(BASE_URL)
    }

    private val researchExportPort: ResearchExportPort by lazy { researchPorts.researchExportPort }
    private val userFeedbackRepository: UserFeedbackRepository by lazy {
        researchPorts.userFeedbackRepository
    }

    val recordUserFeedbackUseCase: RecordUserFeedbackUseCase by lazy {
        RecordUserFeedbackUseCase(userFeedbackRepository)
    }

    @Volatile
    private var wired = false

    private lateinit var messageUseCase: AnalyzeIncomingMessageUseCase
    private lateinit var callUseCase: AnalyzeIncomingCallUseCase
    private lateinit var paymentUseCase: EvaluatePaymentIntentUseCase
    private lateinit var deviceUseCase: EvaluateDeviceSecurityStateUseCase

    private val aggregateUserRiskUseCase: AggregateUserRiskUseCase by lazy {
        AggregateUserRiskUseCase()
    }

    fun wireCoreDomainUseCases(
        analyzeIncomingMessageUseCase: AnalyzeIncomingMessageUseCase,
        analyzeIncomingCallUseCase: AnalyzeIncomingCallUseCase,
        evaluatePaymentIntentUseCase: EvaluatePaymentIntentUseCase,
        evaluateDeviceSecurityStateUseCase: EvaluateDeviceSecurityStateUseCase,
    ) {
        messageUseCase = analyzeIncomingMessageUseCase
        callUseCase = analyzeIncomingCallUseCase
        paymentUseCase = evaluatePaymentIntentUseCase
        deviceUseCase = evaluateDeviceSecurityStateUseCase
        wired = true
    }

    val orchestrator: ZeroScamOrchestratorUseCase by lazy {
        check(wired) {
            "ZeroScamCompositionRoot non initialisé : appelle " +
                "wireCoreDomainUseCases(...) au démarrage (Application/DI)."
        }
        ZeroScamOrchestratorUseCase(
            analyzeIncomingMessageUseCase = messageUseCase,
            analyzeIncomingCallUseCase = callUseCase,
            evaluatePaymentIntentUseCase = paymentUseCase,
            evaluateDeviceSecurityStateUseCase = deviceUseCase,
            aggregateUserRiskUseCase = aggregateUserRiskUseCase,
            researchExportPort = researchExportPort,
        )
    }
}
