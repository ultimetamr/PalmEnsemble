package com.pico.swan.palmensemble.content

internal object StagePanelDepths {
    const val TRACK = 0f
    const val DRAG_OVERLAY = .10f
    const val MODAL = .24f

    fun modalIsFrontmost(): Boolean = MODAL > DRAG_OVERLAY && MODAL > TRACK
}
