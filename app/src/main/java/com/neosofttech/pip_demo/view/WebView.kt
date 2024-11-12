package com.neosofttech.pip_demo.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.neosofttech.pip_demo.databinding.ActivityWebViewBinding
import com.neosofttech.pip_demo.databinding.FloatngWindowBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class WebView : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var pipButton: Button
//    private lateinit var customStopButton: Button

    private var floatingPlayerView: View? = null
    private var floatingYouTubePlayer: YouTubePlayer? = null

    private val REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pipButton = binding.pipButton

        pipButton.setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                startActivityForResult(intent, REQUEST_CODE)
            } else {
                createFloatingPlayer()
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

            lifecycle.addObserver(floatingYouTubePlayerView)

            floatingYouTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    floatingYouTubePlayer = youTubePlayer
                    youTubePlayer.loadVideo("jxcnVDy3U3A", 0f)
                    youTubePlayer.play()
//                    youTubePlayer.mute()
                }
            })

            val params = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                leftMargin = 100
                topMargin = 100
            }

            val rootLayout: FrameLayout = findViewById(android.R.id.content)
            rootLayout.addView(floatingPlayerView, params)

            customStopButton.setOnClickListener {
                stopVideo()
            }

            floatingPlayerView?.visibility = View.VISIBLE
        } else {
            floatingPlayerView?.visibility = View.VISIBLE
        }
    }

    private fun stopVideo() {
        floatingYouTubePlayer?.pause()
//        floatingYouTubePlayer?.seekTo(0f)
        floatingYouTubePlayer = null

        // Remove floating window
        floatingPlayerView?.let {
            val rootLayout: FrameLayout = findViewById(android.R.id.content)
            rootLayout.removeView(it)
            floatingPlayerView = null
        }
    }

    override fun onStop() {
        super.onStop()
        stopVideo()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopVideo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE) {
            if (Settings.canDrawOverlays(this)) {
                createFloatingPlayer()
            } else {
                // Optionally show a message or inform the user that permission is required
            }
        }
    }
}
