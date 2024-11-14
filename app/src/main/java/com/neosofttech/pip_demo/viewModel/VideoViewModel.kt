package com.neosofttech.pip_demo.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class VideoViewModel : ViewModel() {

    // LiveData to observe the video state or errors
    private val _videoState = MutableLiveData<String>()
    val videoState: LiveData<String> get() = _videoState

    // LiveData for controlling the visibility of the player
    private val _isPlayerVisible = MutableLiveData<Boolean>()
    val isPlayerVisible: LiveData<Boolean> get() = _isPlayerVisible

    // Function to update video state
    fun updateVideoState(state: String) {
        _videoState.postValue(state)
    }

    // Function to update player visibility
    fun setPlayerVisibility(isVisible: Boolean) {
        _isPlayerVisible.postValue(isVisible)
    }

    // You can add more logic related to video states, like managing buffering, error, etc.
}
