package com.pico.swan.palmensemble.content

import org.junit.Assert.assertTrue
import org.junit.Test

class StageLaunchPlacementTest {
    @Test
    fun initial_panel_group_is_centered_at_a_comfortable_distance() {
        assertTrue(StageLaunchPlacement.isComfortablyCentered())
        assertTrue(StageLaunchPlacement.ANCHOR_SETTLE_MS >= 500L)
    }
}
