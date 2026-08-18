package com.pico.swan.palmensemble

import com.pico.swan.palmensemble.ui.ensemble.PalmEnsembleScreen
import com.pico.spatial.ui.design.PicoTheme
import com.pico.spatial.ui.foundation.dsl.DefaultStage
import com.pico.spatial.ui.foundation.dsl.SpatialAppScope

fun mainApp(scope: SpatialAppScope) =
    with(scope) {
        DefaultStage {
            PicoTheme {
                PalmEnsembleScreen()
            }
        }
    }
