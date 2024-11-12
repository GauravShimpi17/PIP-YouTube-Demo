package com.neosofttech.pip_demo.view

import android.app.PictureInPictureParams
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.neosofttech.pip_demo.databinding.ActivityWebViewBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class WebView : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var youTubePlayerView: YouTubePlayerView
    private lateinit var pipButton: Button
    private var youTubePlayer: YouTubePlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        youTubePlayerView = binding.youtubePlayerView
        pipButton = binding.pipButton

        lifecycle.addObserver(youTubePlayerView)

        youTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                this@WebView.youTubePlayer = youTubePlayer
                youTubePlayer.loadVideo("FgY-pKShwJs", 0f)
                youTubePlayer.pause()  // Initially pause the video
            }
        })

        pipButton.setOnClickListener {
            binding.youtubePlayerView.visibility = View.VISIBLE
            youTubePlayer?.play()  // Play the video when PiP button is clicked

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                enterPictureInPictureMode()
            } else {
                Toast.makeText(this, "PiP is not supported on your device", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Updated PiP mode entry logic
    override fun enterPictureInPictureMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(16, 9)  // Aspect ratio of the video
            val pipBuilder = PictureInPictureParams.Builder()
            pipBuilder.setAspectRatio(aspectRatio)
            enterPictureInPictureMode(pipBuilder.build())  // Enter PiP mode
        }
    }

    // Keep the activity alive when PiP mode is triggered (do not pause the activity)
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            enterPictureInPictureMode()  // Enter PiP when user leaves the activity
        }
    }

    // Ensure the video stays playing even when the activity is paused or stopped
    override fun onPause() {
        super.onPause()
        // You can choose whether or not to pause the video onPause
        // youTubePlayer?.pause() // Keep this line if you want to pause video in the background
    }

    override fun onStop() {
        super.onStop()
        // You can release the YouTubePlayer if needed
        // youTubePlayer?.pause()
    }

    override fun onResume() {
        super.onResume()
        // Ensure the video continues playing when returning from PiP (if required)
        youTubePlayer?.play()
    }
}
