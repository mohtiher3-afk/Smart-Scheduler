package com.example.services

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.media.RingtoneManager
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin
import kotlin.math.PI

object AlertSoundPlayer {
    private const val SAMPLE_RATE = 44100
    private var currentAudioTrack: AudioTrack? = null

    fun playSound(context: Context, soundType: String) {
        // Stop any currently playing synthesized sound
        stopSound()

        if (soundType == "default" || soundType.isEmpty()) {
            try {
                val notification: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                val r = RingtoneManager.getRingtone(context, notification)
                r.play()
            } catch (e: Exception) {
                Log.e("AlertSoundPlayer", "Error playing default sound", e)
            }
            return
        }

        // Play synthesized sound in background coroutine to avoid blocking UI thread
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val audioData = when (soundType) {
                    "digital_beep" -> generateDigitalBeep()
                    "soft_chime" -> generateSoftChime()
                    "classic_bell" -> generateClassicBell()
                    "tech_alert" -> generateTechAlert()
                    else -> generateDigitalBeep()
                }

                val bufferSize = audioData.size * 2 // 16-bit PCM has 2 bytes per sample
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(audioData, 0, audioData.size)
                currentAudioTrack = audioTrack
                audioTrack.play()
            } catch (e: Exception) {
                Log.e("AlertSoundPlayer", "Error synthesizing sound: ${e.message}", e)
            }
        }
    }

    fun stopSound() {
        try {
            currentAudioTrack?.let {
                if (it.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    it.stop()
                }
                it.release()
            }
            currentAudioTrack = null
        } catch (e: Exception) {
            Log.e("AlertSoundPlayer", "Error stopping sound", e)
        }
    }

    private fun generateDigitalBeep(): ShortArray {
        // Two 100ms pulses of 900Hz, with a 50ms pause
        val pulseLen = (SAMPLE_RATE * 0.1).toInt()
        val pauseLen = (SAMPLE_RATE * 0.05).toInt()
        val totalSamples = pulseLen * 2 + pauseLen
        val samples = ShortArray(totalSamples)

        val freq = 900.0
        // Pulse 1
        for (i in 0 until pulseLen) {
            val angle = 2.0 * PI * i * freq / SAMPLE_RATE
            samples[i] = (sin(angle) * Short.MAX_VALUE * 0.7).toInt().toShort()
        }
        // Pulse 2
        for (i in 0 until pulseLen) {
            val angle = 2.0 * PI * i * freq / SAMPLE_RATE
            samples[pulseLen + pauseLen + i] = (sin(angle) * Short.MAX_VALUE * 0.7).toInt().toShort()
        }
        return samples
    }

    private fun generateSoftChime(): ShortArray {
        // Dual chord 523Hz (C5) + 659Hz (E5) fading out smoothly over 800ms
        val duration = 0.8
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(totalSamples)

        val freq1 = 523.25
        val freq2 = 659.25

        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val envelope = Math.exp(-4.0 * progress) // Fades out

            val angle1 = 2.0 * PI * i * freq1 / SAMPLE_RATE
            val angle2 = 2.0 * PI * i * freq2 / SAMPLE_RATE
            
            val wave = (sin(angle1) + sin(angle2)) * 0.5
            samples[i] = (wave * Short.MAX_VALUE * 0.8 * envelope).toInt().toShort()
        }
        return samples
    }

    private fun generateClassicBell(): ShortArray {
        // 440Hz base frequency with amplitude modulation fading over 1.2s
        val duration = 1.2
        val totalSamples = (SAMPLE_RATE * duration).toInt()
        val samples = ShortArray(totalSamples)

        val baseFreq = 440.0
        val modFreq = 12.0 // Ringing tremolo modulation

        for (i in 0 until totalSamples) {
            val progress = i.toDouble() / totalSamples
            val envelope = Math.exp(-3.0 * progress)

            val tremolo = 0.7 + 0.3 * sin(2.0 * PI * i * modFreq / SAMPLE_RATE)

            val angle = 2.0 * PI * i * baseFreq / SAMPLE_RATE
            val wave = sin(angle) * tremolo
            samples[i] = (wave * Short.MAX_VALUE * 0.7 * envelope).toInt().toShort()
        }
        return samples
    }

    private fun generateTechAlert(): ShortArray {
        // Ascending melody/arpeggio: C5 -> E5 -> G5 -> C6, 120ms each
        val noteDuration = 0.12
        val samplesPerNote = (SAMPLE_RATE * noteDuration).toInt()
        val freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)
        val totalSamples = samplesPerNote * freqs.size
        val samples = ShortArray(totalSamples)

        for (noteIdx in freqs.indices) {
            val freq = freqs[noteIdx]
            val offset = noteIdx * samplesPerNote
            for (i in 0 until samplesPerNote) {
                val noteProgress = i.toDouble() / samplesPerNote
                val envelope = if (noteProgress > 0.8) (1.0 - noteProgress) / 0.2 else 1.0

                val angle = 2.0 * PI * i * freq / SAMPLE_RATE
                val wave = sin(angle) * envelope
                samples[offset + i] = (wave * Short.MAX_VALUE * 0.6).toInt().toShort()
            }
        }
        return samples
    }
}
