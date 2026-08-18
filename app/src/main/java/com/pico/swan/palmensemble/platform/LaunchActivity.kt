package com.pico.swan.palmensemble.platform

import android.os.Bundle
import com.pico.spatial.ui.platform.stub.SpatialLaunchActivity

object LaunchOptions { @Volatile var captureMode: String? = null }

class LaunchActivity : SpatialLaunchActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        LaunchOptions.captureMode = intent.getStringExtra("captureMode")
        super.onCreate(savedInstanceState)
    }
}
