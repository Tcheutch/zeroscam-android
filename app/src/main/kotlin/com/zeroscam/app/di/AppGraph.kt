package com.zeroscam.app.di

import android.content.Context
import android.util.Log
import com.zeroscam.app.BuildConfig
import com.zeroscam.coredomain.usecase.AnalyzeIncomingCallUseCase
import com.zeroscam.coredomain.usecase.AnalyzeIncomingMessageUseCase
import com.zeroscam.coredomain.usecase.EvaluateDeviceSecurityStateUseCase
import com.zeroscam.coredomain.usecase.EvaluatePaymentIntentUseCase

object AppGraph {
    @Volatile
    private var initialized = false

    private lateinit var _analyzeIncomingMessageUseCase: AnalyzeIncomingMessageUseCase
    private lateinit var _analyzeIncomingCallUseCase: AnalyzeIncomingCallUseCase
    private lateinit var _evaluatePaymentIntentUseCase: EvaluatePaymentIntentUseCase
    private lateinit var _evaluateDeviceSecurityStateUseCase: EvaluateDeviceSecurityStateUseCase

    fun init(
        analyzeIncomingMessageUseCase: AnalyzeIncomingMessageUseCase,
        analyzeIncomingCallUseCase: AnalyzeIncomingCallUseCase,
        evaluatePaymentIntentUseCase: EvaluatePaymentIntentUseCase,
        evaluateDeviceSecurityStateUseCase: EvaluateDeviceSecurityStateUseCase,
    ) {
        _analyzeIncomingMessageUseCase = analyzeIncomingMessageUseCase
        _analyzeIncomingCallUseCase = analyzeIncomingCallUseCase
        _evaluatePaymentIntentUseCase = evaluatePaymentIntentUseCase
        _evaluateDeviceSecurityStateUseCase = evaluateDeviceSecurityStateUseCase
        initialized = true
        Log.i(TAG, "AppGraph initialized with real implementations")
    }

    fun initIfNeeded(context: Context) {
        if (initialized) return

        if (!BuildConfig.DEBUG) {
            error(
                "AppGraph must be initialized by real DI in RELEASE. " +
                    "Call AppGraph.init(...) at startup.",
            )
        }

        try {
            val clazz = Class.forName("com.zeroscam.app.di.DebugAppGraphWiring")
            val method = clazz.getDeclaredMethod("initIfNeeded", Context::class.java)
            method.invoke(null, context)
        } catch (t: Throwable) {
            val msg = t.message ?: "no message"
            error(
                "DEBUG fallback missing. Ensure DebugAppGraphWiring.kt exists in src/debug. " +
                    "Root cause: ${t::class.java.simpleName}: $msg",
            )
        }
    }

    val analyzeIncomingMessageUseCase: AnalyzeIncomingMessageUseCase
        get() =
            checkInit {
                _analyzeIncomingMessageUseCase
            }

    val analyzeIncomingCallUseCase: AnalyzeIncomingCallUseCase
        get() = checkInit { _analyzeIncomingCallUseCase }

    val evaluatePaymentIntentUseCase: EvaluatePaymentIntentUseCase
        get() = checkInit { _evaluatePaymentIntentUseCase }

    val evaluateDeviceSecurityStateUseCase: EvaluateDeviceSecurityStateUseCase
        get() = checkInit { _evaluateDeviceSecurityStateUseCase }

    private inline fun <T> checkInit(block: () -> T): T {
        check(initialized) {
            "AppGraph not initialized. " +
                "Call AppGraph.init(...) or (DEBUG only) initIfNeeded(...)."
        }
        return block()
    }

    private const val TAG = "AppGraph"
}
