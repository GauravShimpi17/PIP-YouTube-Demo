package com.neosofttech.pip_demo.view

import android.app.PictureInPictureParams
import android.os.Bundle
import android.util.Rational
import androidx.appcompat.app.AppCompatActivity
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.neosofttech.pip_demo.databinding.ActivityWebViewBinding

class WebView : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var player: ExoPlayer
    private lateinit var playerView: PlayerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        playerView = PlayerView(this)
        player = ExoPlayer.Builder(this).build()
        playerView.player = player

        val videoUrl = "https://www.youtube.com/watch?v=FgY-pKShwJs"  // Replace with your YouTube video URL
        val mediaItem = createYouTubeMediaItem(videoUrl)

        player.setMediaItem(mediaItem)
        player.prepare()

        binding.pipButton.setOnClickListener {
            startPiPMode()
        }
    }

    private fun createYouTubeMediaItem(videoUrl: String): MediaItem {
        // Creating a MediaItem from the video URL
        return MediaItem.fromUri(videoUrl)  // Media3 supports playing URLs directly
    }

    private fun startPiPMode() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val aspectRatio = Rational(16, 9) // Aspect ratio for YouTube videos
            val pipBuilder = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)

            // Trigger PiP mode
            enterPictureInPictureMode(pipBuilder.build())
        }
    }

    override fun onStop() {
        super.onStop()
        player.release() // Release the player when the activity is stopped
    }
}