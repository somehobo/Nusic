package com.njbrady.nusic.utils

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.net.Uri
import java.nio.ByteBuffer

fun calculateAmplitudes(uri: Uri, framesPerSecond: Int = 1, context: Context, callback: (List<Float>) -> Unit) {
    // Set up a content resolver to read the audio data from the Uri
//    val contentResolver = context.contentResolver

    // Set up a media extractor to extract the audio data from the content resolver
    val mediaExtractor = MediaExtractor()
    mediaExtractor.setDataSource(context, uri, null)
    mediaExtractor.selectTrack(0)
    // Set up a format for the audio data
    val audioFormat = mediaExtractor.getTrackFormat(0)
    val sampleRate = audioFormat.getInteger(MediaFormat.KEY_SAMPLE_RATE)
    val channelCount = audioFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT)
    val audioBuffer = ByteBuffer.allocate(sampleRate * 2 / framesPerSecond)

    // Calculate the number of frames per second to sample at
    val framesPerInterval = sampleRate / framesPerSecond

    // Calculate the total number of frames in the audio file
    val numSeconds = (audioFormat.getLong(MediaFormat.KEY_DURATION)/1000000).toInt()
    val numFrames: Int = numSeconds * framesPerSecond


    // Allocate an array to hold the amplitudes
    val amplitudes = FloatArray(numFrames)
    var nextEntry = 0
    // Read and process audio frames at each sampling interval

    for (second in 0 until numSeconds) {
        for (frame in 0 until framesPerSecond) {
            val curTimeUs: Long = ((second.toFloat() + (frame.toFloat()/framesPerSecond.toFloat())) * 1000000).toLong()
            mediaExtractor.seekTo(curTimeUs, MediaExtractor.SEEK_TO_CLOSEST_SYNC)

            val bytesRead = mediaExtractor.readSampleData(audioBuffer, 0)
            var sum = 0f
            for (j in 0 until bytesRead) {
                sum += audioBuffer[j].toFloat() / Short.MAX_VALUE.toFloat()
            }
            amplitudes[nextEntry] = sum / bytesRead
            nextEntry++
        }
    }

    // Release the resources used by the media extractor
    mediaExtractor.release()
    val minAmplitude = amplitudes.minOrNull() ?: 0f
    val maxAmplitude = amplitudes.maxOrNull() ?: 0f
    callback(amplitudes.map { (it - minAmplitude) / (maxAmplitude - minAmplitude) })
}
