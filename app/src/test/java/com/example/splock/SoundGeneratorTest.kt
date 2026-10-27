package com.example.splock

import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.PI
import kotlin.math.sin

class SoundGeneratorTest {

    @Test
    fun generateAlarmSounds() {
        val rawDir = File("src/main/res/raw")
        if (!rawDir.exists()) {
            rawDir.mkdirs()
        }

        generateWav(File(rawDir, "f1_pit_siren.wav")) { t ->
            val freq = 600.0 + 600.0 * sin(2.0 * PI * 1.5 * t)
            sin(2.0 * PI * freq * t)
        }

        generateWav(File(rawDir, "motogp_revving.wav")) { t ->
            val modT = t % 1.0
            val freq = 200.0 + 1200.0 * (modT * modT)
            sin(2.0 * PI * freq * t) * (1.0 - modT)
        }

        generateWav(File(rawDir, "classic_buzzer.wav")) { t ->
            val sine = sin(2.0 * PI * 350.0 * t)
            if (sine > 0) 1.0 else -1.0
        }

        generateWav(File(rawDir, "digital_beep.wav")) { t ->
            val env = if (t < 0.3) 1.0 - (t / 0.3) else 0.0
            sin(2.0 * PI * 1000.0 * t) * env
        }

        generateWav(File(rawDir, "racing_airhorn.wav")) { t ->
            (sin(2.0 * PI * 440.0 * t) + sin(2.0 * PI * 554.0 * t) + sin(2.0 * PI * 659.0 * t)) / 3.0
        }
    }

    private fun generateWav(file: File, durationSec: Double = 1.5, sampleRate: Int = 16000, waveFunc: (Double) -> Double) {
        val numSamples = (sampleRate * durationSec).toInt()
        val dataSize = numSamples * 2
        val totalLen = 36 + dataSize

        val buffer = ByteArray(44 + dataSize)
        val out = ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN)

        out.put("RIFF".toByteArray())
        out.putInt(totalLen)
        out.put("WAVE".toByteArray())

        out.put("fmt ".toByteArray())
        out.putInt(16)
        out.putShort(1.toShort())
        out.putShort(1.toShort())
        out.putInt(sampleRate)
        out.putInt(sampleRate * 2)
        out.putShort(2.toShort())
        out.putShort(16.toShort())

        out.put("data".toByteArray())
        out.putInt(dataSize)

        for (i in 0 until numSamples) {
            val t = i.toDouble() / sampleRate
            val sample = waveFunc(t)
            val pcmVal = (sample * 32767.0).toInt().coerceIn(-32768, 32767)
            out.putShort(pcmVal.toShort())
        }

        FileOutputStream(file).use {
            it.write(buffer)
        }
    }
}
