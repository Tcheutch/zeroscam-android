package com.zeroscam.app.debug

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.zeroscam.app.di.ZeroScamCompositionRoot
import com.zeroscam.coredomain.enums.DetectionChannel
import com.zeroscam.coredomain.enums.FeedbackLabel
import com.zeroscam.coredomain.model.DeviceSecuritySnapshot
import com.zeroscam.coredomain.model.Message
import com.zeroscam.coredomain.model.PhoneCall
import com.zeroscam.coredomain.usecase.ZeroScamOrchestrationRequest
import com.zeroscam.coredomain.value.UserId
import java.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class DebugResearchViewModel : ViewModel() {
    private val orchestrator = ZeroScamCompositionRoot.orchestrator
    private val recordUserFeedbackUseCase = ZeroScamCompositionRoot.recordUserFeedbackUseCase

    fun runTestScenario() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val now = Instant.now()
                val userId = UserId("user-orchestrator-debug")

                val message =
                    Message(
                        id = "msg-debug-1",
                        userId = userId,
                        channel = DetectionChannel.MESSAGE,
                        content = "Super promo crypto !!!",
                        receivedAt = now.minusSeconds(30),
                    )

                val call =
                    PhoneCall(
                        id = "call-debug-1",
                        userId = userId,
                        phoneNumber = "+33123456789",
                        startedAt = now.minusSeconds(10),
                        countryIso = "FR",
                        isInContacts = false,
                        isFromUnknownNumber = true,
                    )

                val deviceSnapshot =
                    DeviceSecuritySnapshot(
                        id = "dev-debug-1",
                        userId = userId,
                        capturedAt = now,
                        isRootedOrJailbroken = false,
                        isEmulator = true,
                        hasDebuggableBuild = true,
                        hasSuspiciousApps = false,
                        integrityCheckPassed = true,
                    )

                val request =
                    ZeroScamOrchestrationRequest(
                        message = message,
                        call = call,
                        paymentIntent = null,
                        deviceSnapshot = deviceSnapshot,
                    )

                val result = orchestrator(request)
                val callDetection = result.callDetection

                if (callDetection == null) {
                    Log.w("DebugResearchVM", "Pas de callDetection dans le résultat")
                    return@launch
                }

                // ✅ Signature EXACTE du RecordUserFeedbackUseCase (ne pas ajouter origin/metadata ici)
                val feedback =
                    recordUserFeedbackUseCase(
                        detectionId = callDetection.id,
                        userId = userId,
                        channel = callDetection.channel,
                        isScam = true,
                        label = FeedbackLabel.NEW_SCAM_PATTERN,
                        comment = "Cas debug : appel très suspect",
                        createdAt = Instant.now(),
                    )

                Log.i("DebugResearchVM", "OK snapshot+feedback detectionId=${feedback.detectionId}")
            } catch (e: Exception) {
                Log.e("DebugResearchVM", "Erreur dans le scénario de test", e)
            }
        }
    }
}
