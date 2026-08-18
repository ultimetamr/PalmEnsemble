package com.pico.swan.palmensemble.ui.ensemble

import com.pico.swan.palmensemble.domain.model.SoundFamily
import com.pico.swan.palmensemble.domain.model.SoundId
import kotlin.math.abs
import kotlin.math.roundToInt

internal data class DragDropMetrics(
    val bankCenterFromTrackCenterPx: Float,
    val firstStepCenterFromTrackCenterPx: Float,
    val stepPitchPx: Float,
    val snapRadiusPx: Float,
)

internal object DragDropMapper {
    fun targetStep(
        family: SoundFamily,
        dragX: Float,
        metrics: DragDropMetrics,
    ): Int? {
        val bankCenter = when (family) {
            SoundFamily.DRUM -> -metrics.bankCenterFromTrackCenterPx
            SoundFamily.MELODY -> metrics.bankCenterFromTrackCenterPx
        }
        val releaseX = bankCenter + dragX
        val step = ((releaseX - metrics.firstStepCenterFromTrackCenterPx) / metrics.stepPitchPx).roundToInt()
        if (step !in 0 until 8) return null
        val stepCenter = metrics.firstStepCenterFromTrackCenterPx + step * metrics.stepPitchPx
        return step.takeIf { abs(releaseX - stepCenter) <= metrics.snapRadiusPx }
    }

    fun shouldDiscard(
        dragX: Float,
        dragY: Float,
        dragZ: Float,
        thresholdPx: Float,
    ): Boolean = dragX * dragX + dragY * dragY + dragZ * dragZ >= thresholdPx * thresholdPx
}

internal data class DragUiState(
    val sound: SoundId? = null,
    val offsetX: Float = 0f,
    val offsetY: Float = 0f,
    val offsetZ: Float = 0f,
    val targetStep: Int? = null,
    val originStep: Int? = null,
    val discardArmed: Boolean = false,
)
