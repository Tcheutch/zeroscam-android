package com.zeroscam.app.debug

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.zeroscam.app.R

/**
 * Écran très simple avec un bouton "Run scenario"
 * pour déclencher le flux complet vers ZeroScam-Research.
 */
class DebugResearchFragment : Fragment() {
    private val vm: DebugResearchViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return inflater.inflate(R.layout.fragment_debug_research, container, false)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        val runButton: Button = view.findViewById(R.id.btnRunScenario)
        runButton.setOnClickListener {
            vm.runTestScenario()
        }
    }
}
