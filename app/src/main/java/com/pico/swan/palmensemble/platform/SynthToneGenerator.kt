package com.pico.swan.palmensemble.platform

import com.pico.swan.palmensemble.domain.model.Atmosphere
import com.pico.swan.palmensemble.domain.model.SoundFamily
import com.pico.swan.palmensemble.domain.model.SoundId
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.round
import kotlin.math.sin
import kotlin.math.tanh

object SynthToneGenerator {
    const val SAMPLE_RATE = 24_000

    fun render(
        sound: SoundId,
        atmosphere: Atmosphere,
        sampleRate: Int = SAMPLE_RATE,
    ): ShortArray {
        val durationSeconds = when (atmosphere) {
            Atmosphere.LO_FI -> if (sound.family == SoundFamily.DRUM) .27 else .64
            Atmosphere.ELECTRONIC -> if (sound.family == SoundFamily.DRUM) .20 else .43
            Atmosphere.LIGHT_ROCK -> if (sound.family == SoundFamily.DRUM) .32 else .58
        }
        val count = (sampleRate * durationSeconds).toInt()
        var lowPass = 0.0
        return ShortArray(count) { index ->
            val time = index.toDouble() / sampleRate
            val base = when (atmosphere) {
                Atmosphere.LO_FI -> loFiVoice(sound, index, time)
                Atmosphere.ELECTRONIC -> electronicVoice(sound, index, time)
                Atmosphere.LIGHT_ROCK -> rockVoice(sound, index, time)
            }
            val styled = when (atmosphere) {
                Atmosphere.LO_FI -> {
                    lowPass += .09 * (base - lowPass)
                    val dusty = lowPass + pseudoNoise(index / 5 + 101) * .018
                    round(dusty * 20.0) / 20.0 * .44
                }
                Atmosphere.ELECTRONIC -> {
                    val gate = if (sound.family == SoundFamily.MELODY && (time * 16).toInt() % 2 == 1) .28 else 1.0
                    tanh(base * 1.45) * gate * .58
                }
                Atmosphere.LIGHT_ROCK -> {
                    tanh(base * 2.7) * .52
                }
            }
            (styled.coerceIn(-1.0, 1.0) * Short.MAX_VALUE).toInt().toShort()
        }
    }

    private fun loFiVoice(sound: SoundId, index: Int, time: Double): Double {
        val wobble = 1.0 + .009 * sin(2 * PI * .55 * time)
        return when (sound) {
            SoundId.KICK -> sin(2 * PI * (58 - 22 * time) * time) * exp(-time * 10)
            SoundId.SNARE -> (pseudoNoise(index / 3 + 17) * .62 + sin(2 * PI * 112 * time) * .30) * exp(-time * 12)
            SoundId.HAT -> pseudoNoise(index / 2 + 31) * exp(-time * 24)
            SoundId.CLAP -> pseudoNoise(index / 4 + 47) * (if ((time * 38).toInt() % 3 == 0) 1.0 else .32) * exp(-time * 13)
            SoundId.CHORD -> triad(196.0, 246.94, 293.66, time, wobble) * exp(-time * 3.3)
            SoundId.BASS -> sin(2 * PI * 82.41 * wobble * time) * exp(-time * 4.2)
            SoundId.BELL -> (sin(2 * PI * 493.88 * wobble * time) + .25 * sin(2 * PI * 987.76 * time)) * exp(-time * 5.7)
            SoundId.LEAD -> (sin(2 * PI * 329.63 * wobble * time) + .16 * sin(2 * PI * 659.25 * time)) * exp(-time * 4.6)
        }
    }

    private fun electronicVoice(sound: SoundId, index: Int, time: Double): Double = when (sound) {
        SoundId.KICK -> (sin(2 * PI * (155 - 112 * time) * time) + .34 * sin(2 * PI * 2_400 * time) * exp(-time * 90)) * exp(-time * 17)
        SoundId.SNARE -> (pseudoNoise(index * 17 + 71) * .78 + square(238.0, time) * .22) * exp(-time * 23)
        SoundId.HAT -> (pseudoNoise(index * 31 + 89) * .72 + square(6_200.0, time) * .28) * exp(-time * 48)
        SoundId.CLAP -> pseudoNoise(index * 23 + 113) * (if ((time * 72).toInt() % 4 == 0) 1.0 else .18) * exp(-time * 20)
        SoundId.CHORD -> (saw(220.0, time) + saw(277.18, time) + saw(329.63, time)) / 3 * exp(-time * 2.6)
        SoundId.BASS -> (square(55.0, time) * .72 + sin(2 * PI * 110.0 * time) * .28) * exp(-time * 4.8)
        SoundId.BELL -> sin(2 * PI * 880.0 * time + 4.8 * sin(2 * PI * 176.0 * time)) * exp(-time * 7.5)
        SoundId.LEAD -> (saw(659.25, time) * .72 + square(1_318.5, time) * .28) * exp(-time * 4.5)
    }

    private fun rockVoice(sound: SoundId, index: Int, time: Double): Double = when (sound) {
        SoundId.KICK -> (sin(2 * PI * (92 - 30 * time) * time) * .82 + pseudoNoise(index * 7 + 131) * .18) * exp(-time * 12)
        SoundId.SNARE -> (pseudoNoise(index * 11 + 149) * .80 + sin(2 * PI * 196.0 * time) * .20) * exp(-time * 15)
        SoundId.HAT -> (pseudoNoise(index * 13 + 167) * .72 + sin(2 * PI * 4_300.0 * time) * .28) * exp(-time * 31)
        SoundId.CLAP -> pseudoNoise(index * 19 + 181) * (if ((time * 48).toInt() % 3 == 0) 1.0 else .40) * exp(-time * 14)
        SoundId.CHORD -> triad(82.41, 123.47, 164.81, time, 1.0) * exp(-time * 3.0)
        SoundId.BASS -> (sin(2 * PI * 73.42 * time) + .34 * sin(2 * PI * 146.84 * time)) * exp(-time * 4.0)
        SoundId.BELL -> (sin(2 * PI * 493.88 * time) + .55 * sin(2 * PI * 740.82 * time)) * exp(-time * 6.2)
        SoundId.LEAD -> (saw(392.0, time) * .62 + sin(2 * PI * 588.0 * time) * .38) * exp(-time * 3.8)
    }

    private fun triad(first: Double, second: Double, third: Double, time: Double, pitch: Double): Double =
        (sin(2 * PI * first * pitch * time) + sin(2 * PI * second * pitch * time) + sin(2 * PI * third * pitch * time)) / 3

    private fun square(frequency: Double, time: Double): Double = if (sin(2 * PI * frequency * time) >= 0) 1.0 else -1.0

    private fun saw(frequency: Double, time: Double): Double = 2.0 * ((time * frequency) % 1.0) - 1.0

    private fun pseudoNoise(index: Int): Double =
        (((index * 1_103_515_245L + 12_345) ushr 16) and 0x7fff).toDouble() / 16_384 - 1
}
