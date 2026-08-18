package com.pico.swan.palmensemble.domain.model

import com.pico.swan.palmensemble.domain.usecase.QuantizedAtmosphere
import com.pico.swan.palmensemble.domain.usecase.QuantizedSequencer
import com.pico.swan.palmensemble.domain.usecase.selectAtmospherePreservingPattern
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AtmospherePresetPatternTest {
    @Test
    fun every_atmosphere_replaces_the_complete_eight_step_arrangement() {
        val patterns = Atmosphere.entries.associateWith(Pattern::forAtmosphere)

        patterns.values.forEach { pattern ->
            assertEquals(STEP_COUNT, pattern.steps.size)
            assertTrue(pattern.steps.any { it.drum != null })
            assertTrue(pattern.steps.any { it.melody != null })
        }
        assertNotEquals(patterns.getValue(Atmosphere.LO_FI), patterns.getValue(Atmosphere.ELECTRONIC))
        assertNotEquals(patterns.getValue(Atmosphere.LO_FI), patterns.getValue(Atmosphere.LIGHT_ROCK))
        assertNotEquals(patterns.getValue(Atmosphere.ELECTRONIC), patterns.getValue(Atmosphere.LIGHT_ROCK))
    }

    @Test
    fun playing_style_switch_preserves_user_pattern_and_quantizes_only_timbre() {
        val userPattern = Pattern.EMPTY
            .place(0, SoundId.KICK)
            .place(3, SoundId.BASS)
            .place(7, SoundId.CLAP)
        val sequencer = QuantizedSequencer(userPattern)
        val atmosphere = QuantizedAtmosphere(Atmosphere.LO_FI)
        sequencer.setPlaying(true)

        selectAtmospherePreservingPattern(sequencer, atmosphere, Atmosphere.LIGHT_ROCK)

        repeat(7) {
            sequencer.tick()
            atmosphere.onStep(sequencer.snapshot.currentStep)
            assertEquals(userPattern, sequencer.snapshot.active)
            assertEquals(null, sequencer.snapshot.pending)
            assertEquals(Atmosphere.LO_FI, atmosphere.snapshot.active)
        }

        sequencer.tick()
        atmosphere.onStep(sequencer.snapshot.currentStep)
        assertEquals(userPattern, sequencer.snapshot.active)
        assertEquals(null, sequencer.snapshot.pending)
        assertEquals(Atmosphere.LIGHT_ROCK, atmosphere.snapshot.active)
    }

    @Test
    fun paused_style_switch_preserves_user_pattern_and_changes_timbre_immediately() {
        val userPattern = Pattern.EMPTY.place(2, SoundId.HAT).place(6, SoundId.CHORD)
        val sequencer = QuantizedSequencer(userPattern)
        val atmosphere = QuantizedAtmosphere(Atmosphere.LO_FI)

        selectAtmospherePreservingPattern(sequencer, atmosphere, Atmosphere.ELECTRONIC)

        assertEquals(userPattern, sequencer.snapshot.active)
        assertEquals(null, sequencer.snapshot.pending)
        assertEquals(Atmosphere.ELECTRONIC, atmosphere.snapshot.active)
    }
}
