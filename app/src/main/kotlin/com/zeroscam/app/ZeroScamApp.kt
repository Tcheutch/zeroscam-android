package com.zeroscam.app

import android.app.Application
import android.util.Log
import com.zeroscam.app.di.AppGraph
import com.zeroscam.app.di.ZeroScamCompositionRoot

class ZeroScamApp : Application() {
    override fun onCreate() {
        super.onCreate()

        AppGraph.initIfNeeded(this)

        ZeroScamCompositionRoot.wireCoreDomainUseCases(
            analyzeIncomingMessageUseCase = AppGraph.analyzeIncomingMessageUseCase,
            analyzeIncomingCallUseCase = AppGraph.analyzeIncomingCallUseCase,
            evaluatePaymentIntentUseCase = AppGraph.evaluatePaymentIntentUseCase,
            evaluateDeviceSecurityStateUseCase = AppGraph.evaluateDeviceSecurityStateUseCase,
        )

        Log.i(TAG, "ZeroScamApp initialized (AppGraph + CompositionRoot wired)")
    }

    companion object {
        private const val TAG = "ZeroScamApp"
    }
}
