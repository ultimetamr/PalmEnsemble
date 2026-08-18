package com.pico.swan.palmensemble.domain.usecase

import com.pico.swan.palmensemble.domain.model.Pattern
import kotlin.random.Random

class AutoBarGenerator(private val random: Random = Random.Default) {
    fun next(): Pattern = Pattern.random(random)
}
