package com.pico.swan.palmensemble.domain.usecase

import com.pico.swan.palmensemble.domain.model.*

data class SequencerSnapshot(val active: Pattern = Pattern.EMPTY, val pending: Pattern? = null, val currentStep: Int = 0, val playing: Boolean = false) { val hasPending get() = pending != null }

class QuantizedSequencer(initial: Pattern = Pattern.EMPTY) {
    var snapshot = SequencerSnapshot(active = initial); private set
    fun setPlaying(value: Boolean) { snapshot = snapshot.copy(playing = value) }
    fun edit(transform: (Pattern) -> Pattern) {
        val source = snapshot.pending ?: snapshot.active
        snapshot = if (snapshot.playing) snapshot.copy(pending = transform(source)) else snapshot.copy(active = transform(source), pending = null)
    }
    fun tick(): List<SoundId> {
        if (!snapshot.playing) return emptyList()
        val next = (snapshot.currentStep + 1) % STEP_COUNT
        val committed = if (next == 0) snapshot.pending ?: snapshot.active else snapshot.active
        snapshot = snapshot.copy(active = committed, pending = if (next == 0) null else snapshot.pending, currentStep = next)
        return snapshot.active.steps[next].let { listOfNotNull(it.drum, it.melody) }
    }
    fun soundsAtCurrentStep(): List<SoundId> = snapshot.active.steps[snapshot.currentStep].let { listOfNotNull(it.drum, it.melody) }
}
