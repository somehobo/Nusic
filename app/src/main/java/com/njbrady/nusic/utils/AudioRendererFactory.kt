package com.njbrady.nusic.utils

import SpectrumAudioProcessor
import android.content.Context
import android.os.Handler
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.Renderer
import com.google.android.exoplayer2.audio.AudioCapabilities
import com.google.android.exoplayer2.audio.AudioRendererEventListener
import com.google.android.exoplayer2.audio.AudioSink
import com.google.android.exoplayer2.audio.DefaultAudioSink
import com.google.android.exoplayer2.audio.DefaultAudioSink.OFFLOAD_MODE_DISABLED
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector
import com.google.android.exoplayer2.metadata.MetadataOutput
import com.google.android.exoplayer2.text.TextOutput
import com.google.android.exoplayer2.video.VideoRendererEventListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


class AudioRendererFactory(private val context: Context) : DefaultRenderersFactory(context) {
    private val _spectrumAudioProcessor = SpectrumAudioProcessor()
    private val _psd = MutableStateFlow(floatArrayOf())

    val psd: StateFlow<FloatArray> = _psd

    init {
        _spectrumAudioProcessor.setOnSpectrumAvailableListener { amplitudes ->
            _psd.value = amplitudes
        }
    }

    override fun createRenderers(
        eventHandler: Handler,
        videoRendererEventListener: VideoRendererEventListener,
        audioRendererEventListener: AudioRendererEventListener,
        textRendererOutput: TextOutput,
        metadataRendererOutput: MetadataOutput
    ): Array<Renderer> {
        val renderersList = ArrayList<Renderer>()
        val defaultAudioSink: AudioSink =
            DefaultAudioSink.Builder()
                .setAudioProcessors(arrayOf( _spectrumAudioProcessor))
                .setAudioCapabilities(AudioCapabilities.getCapabilities(context))
                .setEnableFloatOutput(false)
                .setEnableAudioTrackPlaybackParams(true)
                .setOffloadMode(
                   OFFLOAD_MODE_DISABLED).build()
        buildAudioRenderers(
            context,
            EXTENSION_RENDERER_MODE_OFF,
            MediaCodecSelector.DEFAULT,
            false,
            defaultAudioSink!!,
            eventHandler,
            audioRendererEventListener,
            renderersList
        )

        return renderersList.toTypedArray()
    }
}