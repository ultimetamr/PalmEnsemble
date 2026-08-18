package com.pico.swan.palmensemble.content

/**
 * Initial camera-relative placement only. The Stage root uses TrackingMode.ONCE,
 * so it starts centered in the user's view and then remains world-locked.
 */
internal object StageLaunchPlacement {
    const val ANCHOR_SETTLE_MS = 650L
    const val TRACKING_START_DELAY_MS = 300L
    const val CENTER_X = 0f
    const val CENTER_Y = -0.04f
    const val DISTANCE_Z = -1.35f

    fun isComfortablyCentered(): Boolean =
        CENTER_X == 0f && CENTER_Y in -0.15f..0.05f && DISTANCE_Z in -1.6f..-1.2f
}
