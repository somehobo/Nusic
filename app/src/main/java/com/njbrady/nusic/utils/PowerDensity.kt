import android.content.Context
import android.os.Handler
import android.os.Looper
import be.tarsos.dsp.AudioDispatcher
import be.tarsos.dsp.AudioEvent
import be.tarsos.dsp.AudioProcessor
import be.tarsos.dsp.io.jvm.AudioDispatcherFactory
import be.tarsos.dsp.util.fft.FFT
import be.tarsos.dsp.util.fft.HammingWindow
import java.io.File
import java.io.IOException

/*
    courtesy of GPT-4, prompt: Write me a function in kotlin for android that takes in an audio file and calculates the power spectrum density with a resolution of 1 second

    After prompt took a lot of massaging to get working
 */

fun calculatePSD(context: Context, audioFile: File, resolutionInSeconds: Int = 1, callback: (List<FloatArray>) -> Unit) {
    try {
        val sampleRate = 44100
        val bufferSize = sampleRate * resolutionInSeconds
        val bufferOverlap = 0

        val dispatcher: AudioDispatcher = AudioDispatcherFactory.fromPipe(audioFile.absolutePath, sampleRate, bufferSize, bufferOverlap)
        val fft = FFT(bufferSize, HammingWindow())
        val psdList = mutableListOf<FloatArray>()

        val processor = object : AudioProcessor {
            override fun process(audioEvent: AudioEvent): Boolean {
                val buffer = audioEvent.floatBuffer
                val transformBuffer = FloatArray(bufferSize)
                val powerSpectrum = FloatArray(bufferSize / 2)
                fft.forwardTransform(buffer)
                for (i in 0 until bufferSize / 2) {
                    val real = buffer[i * 2]
                    val imag = buffer[i * 2 + 1]
                    powerSpectrum[i] = real * real + imag * imag
                }
                psdList.add(powerSpectrum)
                return true
            }

            override fun processingFinished() {
                // Do nothing
            }
        }

        dispatcher.addAudioProcessor(processor)

        Thread(Runnable {
            dispatcher.run()
            Handler(Looper.getMainLooper()).post { callback(psdList) }
        }).start()
    } catch (e: IOException) {
        e.printStackTrace()
    }
}
