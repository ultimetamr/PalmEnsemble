package com.pico.swan.palmensemble.data.repository

import android.content.Context
import com.pico.swan.palmensemble.domain.model.RecordedEvent
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class EventSequenceRepository(private val context: Context) {
    fun save(events: List<RecordedEvent>): File {
        val payload = JSONObject().put("format", "PalmEnsemble event sequence (not audio)").put("bpm", 100).put("stepMs", 300)
            .put("events", JSONArray().apply { events.forEach { put(JSONObject().put("elapsedMs", it.elapsedMs).put("type", it.type).put("detail", it.detail)) } })
        return File(context.filesDir, "palmensemble-events-${System.currentTimeMillis()}.json").apply { writeText(payload.toString(2)) }
    }
}
