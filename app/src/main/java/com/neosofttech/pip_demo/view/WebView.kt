package com.neosofttech.pip_demo.view

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import com.neosofttech.pip_demo.databinding.ActivityWebViewBinding
import com.neosofttech.pip_demo.databinding.FloatngWindowBinding
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class WebView : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var pipButton: Button

    private var floatingPlayerView: View? = null
    private var floatingYouTubePlayer: YouTubePlayer? = null

    private var lastX = 0
    private var lastY = 0
    private var isMoving = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pipButton = binding.pipButton

        pipButton.setOnClickListener {
            createFloatingPlayer()
        }
    }

    private fun createFloatingPlayer() {
        if (floatingPlayerView == null) {
            val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val floatingBinding = FloatngWindowBinding.inflate(inflater)
            floatingPlayerView = floatingBinding.root

            val floatingYouTubePlayerView: YouTubePlayerView = floatingBinding.youtubePlayerView

            lifecycle.addObserver(floatingYouTubePlayerView)
            floatingYouTubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    floatingYouTubePlayer = youTubePlayer
                    youTubePlayer.loadVideo("jxcnVDy3U3A", 0f)
                    youTubePlayer.play()

                    // Attempt to hide the default UI controls if possible
                    youTubePlayer.mute() // Mute the video by default
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

            floatingPlayerView?.setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        lastX = event.rawX.toInt()
                        lastY = event.rawY.toInt()
                        isMoving = true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        if (isMoving) {
                            val deltaX = event.rawX.toInt() - lastX
                            val deltaY = event.rawY.toInt() - lastY

                            val layoutParams = v.layoutParams as FrameLayout.LayoutParams
                            layoutParams.leftMargin += deltaX
                            layoutParams.topMargin += deltaY
                            v.layoutParams = layoutParams

                            lastX = event.rawX.toInt()
                            lastY = event.rawY.toInt()
                        }
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        isMoving = false
                        v.performClick()
                    }
                }
                true
            }

            floatingPlayerView?.visibility = View.VISIBLE
        } else {
            floatingPlayerView?.visibility = View.VISIBLE
        }
    }

    override fun onStop() {
        super.onStop()
        floatingYouTubePlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingPlayerView?.let {
            val rootLayout: FrameLayout = findViewById(android.R.id.content)
            rootLayout.removeView(it)
            floatingPlayerView = null
        }
    }
}
