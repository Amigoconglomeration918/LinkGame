package com.example.linkgame.audio

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.linkgame.R
import com.example.linkgame.data.repository.SettingsRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicReference

object AudioManager : DefaultLifecycleObserver {
    @SuppressLint("StaticFieldLeak")
    private lateinit var appContext: Context
    private val bgmPlayer = AtomicReference<MediaPlayer?>(null)
    private var bgmJob: Job? = null
    private var isBgmEnabled = true
    private var currentBgmType = "classic"
    private var soundEnabled = true
    private var isInBackground = false   // 是否在后台

    fun init(context: Context) {
        appContext = context.applicationContext

        // 注册前后台生命周期监听
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        bgmJob?.cancel()
        bgmJob = CoroutineScope(Dispatchers.IO).launch {
            // 监听背景音乐开关
            launch {
                SettingsRepository.isBgmEnabled(appContext).collectLatest { enabled ->
                    isBgmEnabled = enabled
                    if (enabled && !isInBackground) {
                        startBgmInternal()
                    } else if (!enabled) {
                        stopBgmInternal()
                    }
                }
            }

            // 监听背景音乐类型
            launch {
                SettingsRepository.getBgmType(appContext).collectLatest { type ->
                    if (currentBgmType != type) {
                        currentBgmType = type
                        if (isBgmEnabled && !isInBackground) {
                            restartBgmInternal()
                        }
                    }
                }
            }

            // 监听音效开关
            launch {
                SettingsRepository.isSoundEnabled(appContext).collectLatest { enabled ->
                    soundEnabled = enabled
                }
            }
        }
    }

    // 实现生命周期回调
    override fun onStop(owner: LifecycleOwner) {
        isInBackground = true
        stopBgmInternal()  // 后台时暂停音乐
    }

    override fun onStart(owner: LifecycleOwner) {
        isInBackground = false
        if (isBgmEnabled) {
            startBgmInternal()  // 前台时恢复音乐
        }
    }

    // 兼容旧接口（空实现）
    @Deprecated("Use SettingsRepository instead", ReplaceWith(""))
    fun setBgmEnabled(enabled: Boolean) {}

    @Deprecated("Use SettingsRepository instead", ReplaceWith(""))
    fun setSoundEnabled(enabled: Boolean) {}

    fun playClick() {
        if (!soundEnabled) return
        playShortSound(R.raw.click)
    }

    fun playEliminate() {
        if (!soundEnabled) return
        playShortSound(R.raw.eliminate)
    }

    private fun playShortSound(rawId: Int) {
        if (!::appContext.isInitialized) return
        try {
            val mp = MediaPlayer.create(appContext, rawId)
            mp.setOnCompletionListener { it.release() }
            mp.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getRawIdForBgmType(type: String): Int {
        return when (type) {
            "beat" -> R.raw.beat
            "stimulate" -> R.raw.stimulate
            "china" -> R.raw.china
            else -> R.raw.classic
        }
    }

    private fun startBgmInternal() {
        if (!isBgmEnabled || isInBackground) return
        val rawId = getRawIdForBgmType(currentBgmType)
        createAndStartPlayer(rawId)
    }

    private fun stopBgmInternal() {
        synchronized(bgmPlayer) {
            bgmPlayer.get()?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
            }
            bgmPlayer.set(null)
        }
    }

    private fun restartBgmInternal() {
        stopBgmInternal()
        startBgmInternal()
    }

    private fun createAndStartPlayer(rawId: Int) {
        if (!::appContext.isInitialized) return
        synchronized(bgmPlayer) {
            bgmPlayer.get()?.let {
                if (it.isPlaying) it.stop()
                it.release()
            }
            val newPlayer = try {
                MediaPlayer.create(appContext, rawId).apply {
                    isLooping = true
                    setVolume(0.5f, 0.5f)
                    start()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            bgmPlayer.set(newPlayer)
        }
    }

    fun release() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        bgmJob?.cancel()
        stopBgmInternal()
    }
}