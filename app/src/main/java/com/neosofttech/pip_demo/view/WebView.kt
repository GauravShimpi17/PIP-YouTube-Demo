package com.neosofttech.pip_demo.view

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Rational
import android.view.*
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.neosofttech.pip_demo.databinding.ActivityWebViewBinding
import com.neosofttech.pip_demo.databinding.FloatngWindowBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class WebView : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var pipButton: Button
    private lateinit var customStopButton: Button

    private var floatingPlayerView: View? = null
    private var floatingYouTubePlayer: YouTubePlayer? = null

    private val REQUEST_CODE = 1001
    private var isPiPModeRequested = false // Track whether PiP was requested

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pipButton = binding.pipButton

        pipButton.setOnClickListener {
            // Check for overlay permission
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                startActivityForResult(intent, REQUEST_CODE)
            } else {
                createFloatingPlayer() // Permission granted, create the floating player
            }
        }
    }

    private fun createFloatingPlayer() {
        if (floatingPlayerView == null) {
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val floatingBinding = FloatngWindowBinding.inflate(inflater)
            floatingPlayerView = floatingBinding.root

            val floatingYouTubePlayerView: YouTubePlayerView = floatingBinding.youtubePlayerView
            val customStopButton: ImageView = floatingBinding.customStopButton

            // Add lifecycle observer for the floating YouTube player
            lifecycle.addObserver(floatingYouTubePlayerView)

            // Set up YouTube player listener
            floatingYouTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    floatingYouTubePlayer = youTubePlayer
                    youTubePlayer.loadVideo("jxcnVDy3U3A", 0f) // Set video ID and start from 0
                    youTubePlayer.play() // Play the video automatically
                    //youTubePlayer.mute() // Mute the video by default (optional)
                }
            })

            // Define the layout parameters for the floating player
            val params = if (isPiPModeRequested){
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT, // Match parent for full screen width
                    FrameLayout.LayoutParams.WRAP_CONTENT // Wrap content for height
                )
            }else{
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT, // Match parent for full screen width
                    FrameLayout.LayoutParams.WRAP_CONTENT // Wrap content for height
                ).apply {
                    gravity = Gravity.TOP or Gravity.START
                    leftMargin = 100 // Start from the left edge
                    topMargin = 100 // Start from the top edge
                }
            }


            // Add the floating player view to the root layout
            val rootLayout: FrameLayout = findViewById(android.R.id.content)
            rootLayout.addView(floatingPlayerView, params)

            // Set up Stop button click listener
            customStopButton.setOnClickListener {
                stopVideo() // Stop and remove the floating player
            }

            floatingPlayerView?.visibility = View.VISIBLE
        } else {
            floatingPlayerView?.visibility = View.VISIBLE
        }
    }

    // Function to stop (pause and cancel) the video and remove the floating window
    private fun stopVideo() {
        floatingYouTubePlayer?.pause() // Pause the video
        floatingYouTubePlayer?.seekTo(0f) // Seek to the beginning of the video (optional)
        floatingYouTubePlayer = null // Release the reference to the player

        // Remove floating window
        floatingPlayerView?.let {
            val rootLayout: FrameLayout = findViewById(android.R.id.content)
            rootLayout.removeView(it)
            floatingPlayerView = null
        }
    }

    override fun onPause() {
        super.onPause()

        if (!isPiPModeRequested) {
            // When the app is paused, check if it should enter PiP mode
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val width = resources.displayMetrics.widthPixels // Match parent width
                val height = (width * 9) / 16 // Maintain a 16:9 aspect ratio

                val aspectRatio = Rational(width, height) // Aspect ratio for PiP window
                val pipParams = PictureInPictureParams.Builder()
                    .setAspectRatio(aspectRatio)
                    .build()

                // Enter PiP mode only if another activity is on top or home is pressed
                enterPictureInPictureMode(pipParams)
                isPiPModeRequested = true

                // Hide other views in the activity when PiP mode starts
                binding.root.visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()

        // When the activity is resumed, we check if PiP mode was requested
        if (isPiPModeRequested) {
            isPiPModeRequested = false // Reset PiP request flag
            // Re-enable the activity views if PiP mode was not triggered
            binding.root.visibility = View.VISIBLE
        }
    }

    override fun onStop() {
        super.onStop()

        // If the app is backgrounded, remove the floating window or stop PiP mode
        stopVideo() // Remove floating window or stop video when the app is stopped
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVideo() // Ensure the video is stopped when the activity is destroyed
    }

    // Handle the permission request result (if user grants overlay permission)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                createFloatingPlayer() // Permission granted, create the floating player
            } else {
                // Optionally show a message or inform the user that permission is required
            }
        }
    }
}
