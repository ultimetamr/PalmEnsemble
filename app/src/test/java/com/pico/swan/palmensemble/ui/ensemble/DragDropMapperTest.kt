package com.pico.swan.palmensemble.ui.ensemble

import com.pico.swan.palmensemble.domain.model.SoundFamily
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DragDropMapperTest {
    private val metrics = DragDropMetrics(
        bankCenterFromTrackCenterPx = 650f,
        firstStepCenterFromTrackCenterPx = -406f,
        stepPitchPx = 116f,
        snapRadiusPx = 54f,
    )

    @Test
    fun left_drum_bank_snaps_to_each_step() {
        repeat(8) { step ->
            val dragX = 244f + step * 116f
            assertEquals(step, DragDropMapper.targetStep(SoundFamily.DRUM, dragX, metrics))
        }
    }

    @Test
    fun right_melody_bank_snaps_to_each_step() {
        repeat(8) { step ->
            val dragX = -1056f + step * 116f
            assertEquals(step, DragDropMapper.targetStep(SoundFamily.MELODY, dragX, metrics))
        }
    }

    @Test
    fun release_outside_track_does_not_place() {
        assertNull(DragDropMapper.targetStep(SoundFamily.DRUM, 40f, metrics))
        assertNull(DragDropMapper.targetStep(SoundFamily.MELODY, -40f, metrics))
        assertNull(DragDropMapper.targetStep(SoundFamily.DRUM, 1_400f, metrics))
    }

    @Test
    fun placed_ball_only_discards_after_leaving_the_track_threshold() {
        assertFalse(DragDropMapper.shouldDiscard(95f, 0f, 0f, 96f))
        assertTrue(DragDropMapper.shouldDiscard(97f, 0f, 0f, 96f))
        assertTrue(DragDropMapper.shouldDiscard(70f, 70f, 0f, 96f))
        assertTrue(DragDropMapper.shouldDiscard(0f, 0f, 97f, 96f))
    }
}
