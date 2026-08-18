package com.pico.swan.palmensemble.domain.usecase

import com.pico.swan.palmensemble.domain.model.STEP_COUNT
import kotlin.random.Random
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoBarGeneratorTest {
    @Test
    fun every_auto_bar_has_eight_steps_and_can_leave_empty_steps() {
        val generator = AutoBarGenerator(Random(20260814))
        val patterns = List(32) { generator.next() }

        patterns.forEach { pattern ->
            assertEquals(STEP_COUNT, pattern.steps.size)
        }
        assertTrue(patterns.any { pattern -> pattern.steps.any { it.isEmpty } })
    }
}
