package com.pico.swan.palmensemble.platform

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import com.pico.swan.palmensemble.domain.model.Atmosphere
import com.pico.swan.palmensemble.domain.model.SoundId
import kotlin.concurrent.thread

interface AudioEngine {
    fun setAtmosphere(atmosphere: Atmosphere)
    fun trigger(sound: SoundId)
    fun close()
}

class SynthAudioEngine : AudioEngine {
    private val sampleRate = SynthToneGenerator.SAMPLE_RATE
    private val buffers = Atmosphere.entries.associateWith { atmosphere ->
        SoundId.entries.associateWith { sound -> SynthToneGenerator.render(sound, atmosphere, sampleRate) }
    }
    private val live = mutableSetOf<AudioTrack>()
    @Volatile private var activeAtmosphere = Atmosphere.LO_FI

    override fun setAtmosphere(atmosphere: Atmosphere) {
        activeAtmosphere = atmosphere
    }

    override fun trigger(sound: SoundId) {
        val pcm = buffers.getValue(activeAtmosphere).getValue(sound)
        val track = AudioTrack.Builder().setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
            .setAudioFormat(AudioFormat.Builder().setEncoding(AudioFormat.ENCODING_PCM_16BIT).setSampleRate(sampleRate).setChannelMask(AudioFormat.CHANNEL_OUT_MONO).build())
            .setTransferMode(AudioTrack.MODE_STATIC).setBufferSizeInBytes(pcm.size * 2).build()
        synchronized(live) { live += track }
        track.write(pcm, 0, pcm.size); track.play()
        thread(name = "PalmEnsemble-${sound.name}") { Thread.sleep((pcm.size * 1000L / sampleRate) + 30); track.stop(); track.release(); synchronized(live) { live -= track } }
    }
    override fun close() { synchronized(live) { live.forEach { it.release() }; live.clear() } }
}
