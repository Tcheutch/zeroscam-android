package com.zeroscam.app

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.zeroscam.app.di.AppGraph
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppGraphWiringInstrumentedTest {
    @Test
    fun appGraph_should_provide_usecases_in_debug() {
        val ctx = InstrumentationRegistry.getInstrumentation().targetContext
        AppGraph.initIfNeeded(ctx)

        assertNotNull(AppGraph.analyzeIncomingMessageUseCase)
        assertNotNull(AppGraph.analyzeIncomingCallUseCase)
        assertNotNull(AppGraph.evaluatePaymentIntentUseCase)
        assertNotNull(AppGraph.evaluateDeviceSecurityStateUseCase)
    }
}
