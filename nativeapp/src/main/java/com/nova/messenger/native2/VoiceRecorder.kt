package com.nova.messenger.native2

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import java.io.File
import kotlin.math.roundToInt

data class VoiceClip(
    val file: File,
    val durationMs: Long,
    val waveform: List<Int>
)

class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var startedAt = 0L
    private val samples = mutableListOf<Int>()
    private val handler = Handler(Looper.getMainLooper())

    val isRecording: Boolean
        get() = recorder != null

    val elapsedMs: Long
        get() = if (isRecording) {
            (SystemClock.elapsedRealtime() - startedAt).coerceAtLeast(0L)
        } else {
            0L
        }

    private val sampler = object : Runnable {
        override fun run() {
            val active = recorder ?: return
            val amplitude = runCatching { active.maxAmplitude }.getOrDefault(0)
            val normalized = ((amplitude / 32767f) * 100f)
                .roundToInt()
                .coerceIn(3, 100)
            samples += normalized
            handler.postDelayed(this, 80L)
        }
    }

    fun start() {
        if (isRecording) return

        val file = File.createTempFile(
            "nova_voice_",
            ".m4a",
            context.cacheDir
        )

        val next = createRecorder()
        next.setAudioSource(MediaRecorder.AudioSource.MIC)
        next.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        next.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        next.setAudioEncodingBitRate(96_000)
        next.setAudioSamplingRate(44_100)
        next.setOutputFile(file.absolutePath)
        next.prepare()
        next.start()

        outputFile = file
        recorder = next
        samples.clear()
        startedAt = SystemClock.elapsedRealtime()
        handler.post(sampler)
    }

    fun stop(): VoiceClip? {
        val active = recorder ?: return null
        val file = outputFile ?: return null
        val duration = elapsedMs

        handler.removeCallbacks(sampler)

        val valid = runCatching {
            active.stop()
            true
        }.getOrElse { false }

        runCatching { active.release() }
        recorder = null
        outputFile = null
        startedAt = 0L

        if (!valid || duration < 350L || !file.exists() || file.length() <= 0L) {
            file.delete()
            samples.clear()
            return null
        }

        val wave = compactWaveform(samples)
        samples.clear()
        return VoiceClip(
            file = file,
            durationMs = duration,
            waveform = wave
        )
    }

    fun cancel() {
        val active = recorder
        handler.removeCallbacks(sampler)
        if (active != null) {
            runCatching { active.stop() }
            runCatching { active.release() }
        }
        recorder = null
        startedAt = 0L
        outputFile?.delete()
        outputFile = null
        samples.clear()
    }

    @Suppress("DEPRECATION")
    private fun createRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            MediaRecorder()
        }

    private fun compactWaveform(source: List<Int>): List<Int> {
        if (source.isEmpty()) return emptyList()
        val target = 48
        if (source.size <= target) return source.map { it.coerceIn(3, 100) }

        val step = source.size.toFloat() / target.toFloat()
        return (0 until target).map { index ->
            val from = (index * step).toInt().coerceIn(0, source.lastIndex)
            val to = (((index + 1) * step).toInt())
                .coerceIn(from + 1, source.size)
            source.subList(from, to)
                .average()
                .roundToInt()
                .coerceIn(3, 100)
        }
    }
}
