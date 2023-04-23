import be.tarsos.dsp.util.fft.FFT
import com.google.android.exoplayer2.audio.AudioProcessor
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.log10
import kotlin.math.pow

class SpectrumAudioProcessor : AudioProcessor {
    private var fftSize = 2048
    private var fft = FFT(fftSize / 2)
    private var amplitudes = FloatArray(fftSize / 2)
    private var pcmBuffer = FloatArray(fftSize)
    private var bufferIndex = 0
    private lateinit var onSpectrumAvailableListener: (FloatArray) -> Unit
    private val hammingWindowCoefficients = hammingWindow(fftSize)
    val frequencyBands = arrayOf(
        Pair(20f, 60f),
        Pair(60f, 250f),
        Pair(250f, 500f),
        Pair(500f, 2000f),
        Pair(2000f, 4000f),
        Pair(4000f, 6000f)
    )
    private val previousAmplitudes = FloatArray(frequencyBands.size * 6) { 0f }

    private var pendingOutput: ByteBuffer? = null
    private var inputEnded: Boolean = false

    fun setOnSpectrumAvailableListener(listener: (FloatArray) -> Unit) {
        onSpectrumAvailableListener = listener
    }

    override fun configure(inputAudioFormat: AudioProcessor.AudioFormat): AudioProcessor.AudioFormat {
        return inputAudioFormat
    }

    override fun isActive(): Boolean {
        return true
    }

    override fun queueInput(inputBuffer: ByteBuffer){
        val inputSize = inputBuffer.remaining()
        val outputBuffer = ByteBuffer.allocate(inputSize).order(ByteOrder.nativeOrder())

        while (inputBuffer.hasRemaining()) {
            pcmBuffer[bufferIndex++] = inputBuffer.short / Short.MAX_VALUE.toFloat()
            outputBuffer.putShort(inputBuffer.getShort(inputBuffer.position() - 2))
            if (bufferIndex == fftSize) {
                processAudioFrame()
                bufferIndex = 0
            }
        }

        outputBuffer.flip()
        pendingOutput = outputBuffer
    }

    private fun hammingWindow(size: Int): FloatArray {
        val coefficients = FloatArray(size)
        for (i in 0 until size) {
            coefficients[i] = 0.54f - 0.46f * cos(2f * PI.toFloat() * i.toFloat() / (size - 1).toFloat())
        }
        return coefficients
    }

    private fun processAudioFrame() {
        for (i in 0 until fftSize) {
            pcmBuffer[i] *= hammingWindowCoefficients[i]
        }
        fft.forwardTransform(pcmBuffer)
        fft.modulus(pcmBuffer, amplitudes)

        val logFrequencyBands = ArrayList<Pair<Float, Float>>()
        for (i in 0 until frequencyBands.size - 1) {
            val startLogFreq = log10(frequencyBands[i].first.toDouble()).toFloat()
            val endLogFreq = log10(frequencyBands[i].second.toDouble()).toFloat()
            val step = (endLogFreq - startLogFreq) / 7
            for (j in 0 until 7) {
                val start = 10.0.pow((startLogFreq + step * j).toDouble()).toFloat()
                val end = 10.0.pow((startLogFreq + step * (j + 1)).toDouble()).toFloat()
                logFrequencyBands.add(Pair(start, end))
            }
        }

        val sampledAmplitudes = FloatArray(logFrequencyBands.size)
        val sampleRate = 44100f // You should replace this value with the actual sample rate of the audio
        val binSize = sampleRate / fftSize

        for ((i, band) in logFrequencyBands.withIndex()) {
            val startBin = (band.first / binSize).toInt().coerceIn(0, amplitudes.size - 1)
            val endBin = (band.second / binSize).toInt().coerceIn(0, amplitudes.size - 1)

            var sum = 0f
            for (bin in startBin..endBin) {
                sum += amplitudes[bin] * amplitudes[bin]
            }

            val avg = sum / (endBin - startBin + 1)
            sampledAmplitudes[i] = avg
        }


        val maxAmplitude = sampledAmplitudes.maxOrNull() ?: 1f
        for (i in sampledAmplitudes.indices) {
            val normalizedValue = if (maxAmplitude == 0f) 0f else sampledAmplitudes[i] / maxAmplitude
            sampledAmplitudes[i] = if (normalizedValue.isNaN()) 0f else normalizedValue
        }

        val alpha = 0.6f // Smoothing factor (between 0 and 1)

        for (i in sampledAmplitudes.indices) {
            // Apply the exponential moving average filter
            val smoothedValue = alpha * sampledAmplitudes[i] + (1 - alpha) * previousAmplitudes[i]

            // Check for NaN values and replace them with 0
            val finalValue = if (smoothedValue.isNaN()) 0f else smoothedValue

            sampledAmplitudes[i] = finalValue
            previousAmplitudes[i] = finalValue
        }

        onSpectrumAvailableListener.invoke(sampledAmplitudes)
    }


    override fun queueEndOfStream() {
        inputEnded = true
    }

    override fun isEnded(): Boolean {
        return inputEnded
    }

    override fun getOutput(): ByteBuffer {
        if (pendingOutput == null) {
            pendingOutput = ByteBuffer.allocate(0)
        }
        val output = pendingOutput
        pendingOutput = null
        return output!!
    }


    override fun flush() {
        pendingOutput = null
        inputEnded = false
    }

    override fun reset() {
        flush()
    }
}