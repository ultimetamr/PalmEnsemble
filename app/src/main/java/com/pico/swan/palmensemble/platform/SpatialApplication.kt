package com.pico.swan.palmensemble.platform

import android.app.Application
import com.pico.spatial.ui.foundation.dsl.launch
import com.pico.swan.palmensemble.mainApp

class SpatialApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        launch(::mainApp)
    }
}
