package com.pico.swan.palmensemble.platform

import com.pico.swan.palmensemble.domain.model.Atmosphere
import com.pico.swan.palmensemble.domain.model.SoundId
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sqrt

class SynthToneGeneratorTest {
    @Test
    fun every_sound_has_three_distinct_atmosphere_buffers() {
        SoundId.entries.forEach { sound ->
            val loFi = SynthToneGenerator.render(sound, Atmosphere.LO_FI)
            val electronic = SynthToneGenerator.render(sound, Atmosphere.ELECTRONIC)
            val rock = SynthToneGenerator.render(sound, Atmosphere.LIGHT_ROCK)

            assertFalse("$sound Lo-fi and electronic must differ", loFi.contentEquals(electronic))
            assertFalse("$sound Lo-fi and light rock must differ", loFi.contentEquals(rock))
            assertFalse("$sound electronic and light rock must differ", electronic.contentEquals(rock))
            assertTrue("$sound Lo-fi must be audible", loFi.any { it != 0.toShort() })
            assertTrue("$sound electronic must be audible", electronic.any { it != 0.toShort() })
            assertTrue("$sound light rock must be audible", rock.any { it != 0.toShort() })
        }
    }

    @Test
    fun atmosphere_waveforms_are_not_merely_small_variations_of_each_other() {
        SoundId.entries.forEach { sound ->
            val rendered = Atmosphere.entries.associateWith { SynthToneGenerator.render(sound, it) }
            Atmosphere.entries.forEachIndexed { index, first ->
                Atmosphere.entries.drop(index + 1).forEach { second ->
                    val similarity = cosineSimilarity(rendered.getValue(first), rendered.getValue(second))
                    assertTrue("$sound $first/$second similarity=$similarity", abs(similarity) < 0.72)
                }
            }
        }
    }

    private fun cosineSimilarity(first: ShortArray, second: ShortArray): Double {
        val size = minOf(first.size, second.size)
        var dot = 0.0
        var firstEnergy = 0.0
        var secondEnergy = 0.0
        repeat(size) { index ->
            val a = first[index].toDouble()
            val b = second[index].toDouble()
            dot += a * b
            firstEnergy += a * a
            secondEnergy += b * b
        }
        return dot / sqrt(firstEnergy * secondEnergy)
    }
}
