package com.pico.swan.palmensemble.domain.model

import com.pico.swan.palmensemble.domain.usecase.QuantizedSequencer
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RandomPatternTest {
    @Test
    fun random_pattern_has_eight_steps_and_allows_empty_slots() {
        val random = Random(20260814)
        val patterns = List(64) { Pattern.random(random) }

        patterns.forEach { pattern ->
            assertEquals(STEP_COUNT, pattern.steps.size)
            assertTrue(pattern.steps.all { it.drum == null || it.drum.family == SoundFamily.DRUM })
            assertTrue(pattern.steps.all { it.melody == null || it.melody.family == SoundFamily.MELODY })
        }
        assertTrue(patterns.any { pattern -> pattern.steps.any { it.drum == null } })
        assertTrue(patterns.any { pattern -> pattern.steps.any { it.melody == null } })
        assertTrue(patterns.any(Pattern::hasContent))
        assertNotEquals(patterns.first(), patterns.last())
    }

    @Test
    fun random_pattern_waits_for_the_next_bar_while_playing() {
        val original = Pattern.EMPTY.place(0, SoundId.KICK)
        val generated = Pattern.random(Random(42))
        val sequencer = QuantizedSequencer(original)
        sequencer.setPlaying(true)

        sequencer.edit { generated }

        assertEquals(original, sequencer.snapshot.active)
        assertEquals(generated, sequencer.snapshot.pending)
        repeat(7) { sequencer.tick() }
        assertEquals(original, sequencer.snapshot.active)
        sequencer.tick()
        assertEquals(generated, sequencer.snapshot.active)
    }
}
