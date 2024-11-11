package com.neosofttech.pip_demo.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.neosofttech.pip_demo.model.VideoData

class ViewModel : ViewModel() {
    private val _video = MutableLiveData<VideoData>()
    val video: LiveData<VideoData> get() = _video

    private val _isPipModeEnabled = MutableLiveData<Boolean>()
    val isPipModeEnabled: LiveData<Boolean> get() = _isPipModeEnabled

    init {
        loadVideo()
    }

    private fun loadVideo() {
        _video.value = VideoData(videoId = "FgY-pKShwJs")
    }

    fun enablePipMode() {
        _isPipModeEnabled.value = true
    }
}