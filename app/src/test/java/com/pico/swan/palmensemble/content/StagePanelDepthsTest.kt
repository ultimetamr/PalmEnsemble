package com.pico.swan.palmensemble.content

import org.junit.Assert.assertTrue
import org.junit.Test

class StagePanelDepthsTest {
    @Test
    fun clear_modal_is_in_front_of_track_and_drag_overlay() {
        assertTrue(StagePanelDepths.modalIsFrontmost())
    }
}
