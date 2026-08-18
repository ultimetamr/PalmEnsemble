package com.pico.swan.palmensemble.domain.usecase

import com.pico.swan.palmensemble.domain.model.Pattern
import com.pico.swan.palmensemble.domain.model.SoundId

class PatternEditUseCase { operator fun invoke(pattern: Pattern, step: Int, sound: SoundId): Pattern = pattern.place(step, sound) }
