package com.pico.swan.palmensemble.ui.ensemble

import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import com.pico.swan.palmensemble.content.HomeStage

@Composable
fun PalmEnsembleScreen() {
    val viewModel: PalmEnsembleViewModel = viewModel()
    val state by viewModel.state.collectAsStateWithLifecycle()
    HomeStage(state, viewModel::onEvent)
}
