package com.pico.swan.palmensemble.domain.model

import kotlin.random.Random

enum class SoundFamily { DRUM, MELODY }

enum class SoundId(val label: String, val icon: String, val family: SoundFamily, val color: Long) {
    KICK("底鼓", "■", SoundFamily.DRUM, 0xFFFF9B73), SNARE("军鼓", "▲", SoundFamily.DRUM, 0xFFFFCF6B),
    HAT("踩镲", "◆", SoundFamily.DRUM, 0xFF87D7FF), CLAP("拍手", "●", SoundFamily.DRUM, 0xFFD7A0FF),
    CHORD("暖和弦", "■", SoundFamily.MELODY, 0xFFFF9B73), BASS("圆润贝斯", "▲", SoundFamily.MELODY, 0xFFFFCF6B),
    BELL("微光铃", "◆", SoundFamily.MELODY, 0xFF87D7FF), LEAD("柔和主音", "●", SoundFamily.MELODY, 0xFFD7A0FF),
}
enum class Atmosphere(val label: String) { LO_FI("Lo-fi"), ELECTRONIC("电子"), LIGHT_ROCK("轻摇滚") }
data class Step(val drum: SoundId? = null, val melody: SoundId? = null) {
    fun with(sound: SoundId) = if (sound.family == SoundFamily.DRUM) copy(drum = sound) else copy(melody = sound)
    fun remove(family: SoundFamily) = if (family == SoundFamily.DRUM) copy(drum = null) else copy(melody = null)
    val isEmpty get() = drum == null && melody == null
}
data class Pattern(val steps: List<Step> = List(STEP_COUNT) { Step() }) {
    init { require(steps.size == STEP_COUNT) }
    fun place(index: Int, sound: SoundId) = copy(steps = steps.toMutableList().also { it[index] = it[index].with(sound) })
    fun remove(index: Int, family: SoundFamily) = copy(steps = steps.toMutableList().also { it[index] = it[index].remove(family) })
    val hasContent get() = steps.any { !it.isEmpty }
    companion object {
        val EMPTY = Pattern()
        private val LO_FI_PRESET = Pattern(listOf(
            Step(SoundId.KICK, SoundId.CHORD), Step(),
            Step(SoundId.HAT, SoundId.BASS), Step(),
            Step(SoundId.SNARE, SoundId.CHORD), Step(),
            Step(SoundId.HAT, SoundId.BELL), Step(SoundId.CLAP),
        ))
        private val ELECTRONIC_PRESET = Pattern(listOf(
            Step(SoundId.KICK, SoundId.BASS), Step(SoundId.HAT, SoundId.LEAD),
            Step(SoundId.KICK, SoundId.BELL), Step(SoundId.HAT, SoundId.LEAD),
            Step(SoundId.KICK, SoundId.BASS), Step(SoundId.HAT, SoundId.LEAD),
            Step(SoundId.SNARE, SoundId.BELL), Step(SoundId.HAT, SoundId.LEAD),
        ))
        private val LIGHT_ROCK_PRESET = Pattern(listOf(
            Step(SoundId.KICK, SoundId.CHORD), Step(SoundId.HAT),
            Step(SoundId.SNARE, SoundId.LEAD), Step(SoundId.KICK),
            Step(SoundId.HAT, SoundId.CHORD), Step(SoundId.KICK, SoundId.BASS),
            Step(SoundId.SNARE, SoundId.LEAD), Step(SoundId.CLAP, SoundId.BASS),
        ))

        val EXAMPLE = LO_FI_PRESET

        fun forAtmosphere(atmosphere: Atmosphere): Pattern = when (atmosphere) {
            Atmosphere.LO_FI -> LO_FI_PRESET
            Atmosphere.ELECTRONIC -> ELECTRONIC_PRESET
            Atmosphere.LIGHT_ROCK -> LIGHT_ROCK_PRESET
        }

        fun random(random: Random = Random.Default): Pattern {
            val drums = SoundId.entries.filter { it.family == SoundFamily.DRUM }
            val melodies = SoundId.entries.filter { it.family == SoundFamily.MELODY }
            fun optional(sounds: List<SoundId>, fillPercent: Int): SoundId? =
                if (random.nextInt(100) < fillPercent) sounds[random.nextInt(sounds.size)] else null

            return Pattern(List(STEP_COUNT) {
                Step(
                    drum = optional(drums, fillPercent = 55),
                    melody = optional(melodies, fillPercent = 40),
                )
            })
        }
    }
}
data class RecordedEvent(val elapsedMs: Long, val type: String, val detail: String)
const val BPM = 100
const val STEP_MS = 300L
const val BAR_MS = 2_400L
const val STEP_COUNT = 8
