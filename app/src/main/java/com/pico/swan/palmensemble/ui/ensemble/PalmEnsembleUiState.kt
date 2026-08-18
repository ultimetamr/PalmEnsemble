package com.pico.swan.palmensemble.ui.ensemble

import com.pico.swan.palmensemble.domain.model.*

data class PalmEnsembleUiState(
    val active: Pattern = Pattern.EMPTY, val pending: Pattern? = null, val currentStep: Int = 0,
    val playing: Boolean = false, val selected: SoundId? = null, val atmosphere: Atmosphere = Atmosphere.LO_FI,
    val pendingAtmosphere: Atmosphere? = null,
    val demo: Boolean = true, val clearConfirm: Boolean = false, val recording: Boolean = false,
    val recordRemainingMs: Long = 30_000, val status: String = "按住球拖进格子，或点击球再点格号", val lastSaved: String? = null,
    val tutorialVisible: Boolean = false, val autoMode: Boolean = false,
)

sealed interface PalmEnsembleEvent {
    data class SelectSound(val sound: SoundId): PalmEnsembleEvent
    data class Place(val index: Int): PalmEnsembleEvent
    data class Remove(val index: Int, val family: SoundFamily): PalmEnsembleEvent
    data class Preset(val value: Atmosphere): PalmEnsembleEvent
    data object TogglePlay: PalmEnsembleEvent
    data object LoadExample: PalmEnsembleEvent
    data object Randomize: PalmEnsembleEvent
    data object ToggleAuto: PalmEnsembleEvent
    data object OpenTutorial: PalmEnsembleEvent
    data object CloseTutorial: PalmEnsembleEvent
    data object RequestClear: PalmEnsembleEvent
    data object CancelClear: PalmEnsembleEvent
    data object ConfirmClear: PalmEnsembleEvent
    data object ToggleRecord: PalmEnsembleEvent
    data object StartOwnLoop: PalmEnsembleEvent
    data object CancelSelection: PalmEnsembleEvent
}
