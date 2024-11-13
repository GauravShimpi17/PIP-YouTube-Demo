package com.neosofttech.pip_demo.viewmodel

import android.app.Application
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Rational
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class WebViewModel(application: Application) : AndroidViewModel(application) {

    private val _isPiPModeRequested = MutableLiveData(false)
    val isPiPModeRequested: LiveData<Boolean> get() = _isPiPModeRequested

    private val _hasOverlayPermission = MutableLiveData(false)
    val hasOverlayPermission: LiveData<Boolean> get() = _hasOverlayPermission

    private var floatingYouTubePlayer: YouTubePlayer? = null

    fun checkOverlayPermission(context: Context) {
        _hasOverlayPermission.value = Settings.canDrawOverlays(context)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 1001) {
            _hasOverlayPermission.value = Settings.canDrawOverlays(getApplication())
        }
    }

    fun createFloatingPlayer(floatingYouTubePlayerView: YouTubePlayerView) {
        floatingYouTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                floatingYouTubePlayer = youTubePlayer
                youTubePlayer.loadVideo("jxcnVDy3U3A", 0f)
                youTubePlayer.play()
            }
        })
    }

    fun stopVideo() {
        floatingYouTubePlayer?.pause()
        floatingYouTubePlayer = null
    }

    fun onUserLeaveHint() {
        _isPiPModeRequested.value = true
    }

    fun onPause() {
        _isPiPModeRequested.value = true
    }

    fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        if (isInPictureInPictureMode) {
            _isPiPModeRequested.value = true
        } else {
            _isPiPModeRequested.value = false
        }
    }
}
