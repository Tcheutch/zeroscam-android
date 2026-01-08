package com.zeroscam.app.di

import com.zeroscam.coredomain.usecase.AnalyzeIncomingCallUseCase
import com.zeroscam.coredomain.usecase.AnalyzeIncomingMessageUseCase
import com.zeroscam.coredomain.usecase.EvaluateDeviceSecurityStateUseCase
import com.zeroscam.coredomain.usecase.EvaluatePaymentIntentUseCase

object AppDiModule {
    @Volatile private var bootstrapped = false

    @Synchronized
    fun bootstrap(
        analyzeIncomingMessageUseCase: AnalyzeIncomingMessageUseCase,
        analyzeIncomingCallUseCase: AnalyzeIncomingCallUseCase,
        evaluatePaymentIntentUseCase: EvaluatePaymentIntentUseCase,
        evaluateDeviceSecurityStateUseCase: EvaluateDeviceSecurityStateUseCase,
    ) {
        if (bootstrapped) return

        AppGraph.init(
            analyzeIncomingMessageUseCase = analyzeIncomingMessageUseCase,
            analyzeIncomingCallUseCase = analyzeIncomingCallUseCase,
            evaluatePaymentIntentUseCase = evaluatePaymentIntentUseCase,
            evaluateDeviceSecurityStateUseCase = evaluateDeviceSecurityStateUseCase,
        )

        ZeroScamCompositionRoot.wireCoreDomainUseCases(
            analyzeIncomingMessageUseCase = AppGraph.analyzeIncomingMessageUseCase,
            analyzeIncomingCallUseCase = AppGraph.analyzeIncomingCallUseCase,
            evaluatePaymentIntentUseCase = AppGraph.evaluatePaymentIntentUseCase,
            evaluateDeviceSecurityStateUseCase = AppGraph.evaluateDeviceSecurityStateUseCase,
        )

        bootstrapped = true
    }
}
