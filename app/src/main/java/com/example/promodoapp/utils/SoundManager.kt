package com.example.promodoapp.utils


import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import com.example.promodoapp.R

object SoundManager {
    private lateinit var soundPool: SoundPool
    private var loaded = false
    private var coinSoundId = 0
    private var notifySoundId = 0

    fun init(context: Context) {
        if (::soundPool.isInitialized) return // chỉ khởi tạo một lần duy nhất

        val attrs = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(2) // có thể phát đồng thời 2 tiếng
            .setAudioAttributes(attrs)
            .build()

        // Load các file âm thanh vào bộ nhớ
        coinSoundId = soundPool.load(context, R.raw.coin_recieve, 1)

        soundPool.setOnLoadCompleteListener { _, _, _ ->
            loaded = true
        }
    }

    fun playCoin() {
        if (loaded) {
            soundPool.play(coinSoundId, 1f, 1f, 1, 0, 1f)
        }
    }

}
