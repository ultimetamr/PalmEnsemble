package com.pico.swan.palmensemble.domain.usecase

import com.pico.swan.palmensemble.domain.model.Atmosphere

data class AtmosphereSnapshot(
    val active: Atmosphere = Atmosphere.LO_FI,
    val pending: Atmosphere? = null,
)

class QuantizedAtmosphere(initial: Atmosphere = Atmosphere.LO_FI) {
    var snapshot = AtmosphereSnapshot(active = initial)
        private set

    fun select(value: Atmosphere, playing: Boolean) {
        snapshot = if (playing) {
            snapshot.copy(pending = value)
        } else {
            snapshot.copy(active = value, pending = null)
        }
    }

    fun onStep(step: Int): Atmosphere? {
        if (step != 0) return null
        val next = snapshot.pending ?: return null
        snapshot = AtmosphereSnapshot(active = next)
        return next
    }
}

/** Changes the complete sound palette without ever editing the user's step pattern. */
fun selectAtmospherePreservingPattern(
    sequencer: QuantizedSequencer,
    atmosphere: QuantizedAtmosphere,
    value: Atmosphere,
) {
    atmosphere.select(value, playing = sequencer.snapshot.playing)
}
