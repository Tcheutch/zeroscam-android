package com.zeroscam.coredomain.ports

import com.zeroscam.coredomain.model.DetectionFeedback

/**
 * Port de persistance des feedbacks utilisateurs.
 *
 * L'implémentation concrète vivra côté core-data / data (Room, Retrofit, etc.).
 */
interface UserFeedbackRepository {
    /**
     * Sauvegarde un feedback utilisateur.
     *
     * Implémentation typique :
     *  - écriture en base locale et/ou remote,
     *  - éventuelle mise en file pour synchro offline.
     */
    fun saveFeedback(feedback: DetectionFeedback)
}
