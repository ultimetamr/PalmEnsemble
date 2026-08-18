package com.pico.swan.palmensemble.domain.usecase

import com.pico.swan.palmensemble.domain.model.Atmosphere
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class QuantizedAtmosphereTest {
    @Test
    fun paused_selection_applies_immediately() {
        val atmosphere = QuantizedAtmosphere()

        atmosphere.select(Atmosphere.ELECTRONIC, playing = false)

        assertEquals(Atmosphere.ELECTRONIC, atmosphere.snapshot.active)
        assertNull(atmosphere.snapshot.pending)
    }

    @Test
    fun playing_selection_waits_for_seven_to_zero_boundary() {
        val atmosphere = QuantizedAtmosphere()

        atmosphere.select(Atmosphere.LIGHT_ROCK, playing = true)
        repeat(7) { step ->
            assertNull(atmosphere.onStep(step + 1))
            assertEquals(Atmosphere.LO_FI, atmosphere.snapshot.active)
        }

        assertEquals(Atmosphere.LIGHT_ROCK, atmosphere.onStep(0))
        assertEquals(Atmosphere.LIGHT_ROCK, atmosphere.snapshot.active)
        assertNull(atmosphere.snapshot.pending)
    }

    @Test
    fun latest_playing_selection_replaces_pending_atmosphere() {
        val atmosphere = QuantizedAtmosphere()

        atmosphere.select(Atmosphere.ELECTRONIC, playing = true)
        atmosphere.select(Atmosphere.LIGHT_ROCK, playing = true)

        assertEquals(Atmosphere.LIGHT_ROCK, atmosphere.snapshot.pending)
        assertEquals(Atmosphere.LIGHT_ROCK, atmosphere.onStep(0))
    }
}
