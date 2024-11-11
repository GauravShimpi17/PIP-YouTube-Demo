package com.neosofttech.pip_demo.view

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.neosofttech.pip_demo.databinding.ActivityMainBinding
import com.neosofttech.pip_demo.viewModel.ViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var youTubePlayer: YouTubePlayer? = null
    private val viewModel: ViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.btnWeb.setOnClickListener { setUpListener() }

        lifecycle.addObserver(binding.pipView)

        viewModel.video.observe(this) { viewModel ->
            binding.pipView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    this@MainActivity.youTubePlayer = youTubePlayer
                    youTubePlayer.loadVideo(viewModel.videoId, 0f)
                }
            })
        }

        viewModel.isPipModeEnabled.observe(this) { isEnabled ->
            if (isEnabled) {
                enterPipMode()
            }
        }

    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        viewModel.enablePipMode()
    }

    private fun enterPipMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val aspectRatio = Rational(binding.pipView.width, binding.pipView.height)
            val pipParams = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .setActions(emptyList())
                .build()
            enterPictureInPictureMode(pipParams)
        }
    }

    private fun setUpListener(){
        val intent = Intent(this@MainActivity, WebView::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.pipView.release()
    }

}