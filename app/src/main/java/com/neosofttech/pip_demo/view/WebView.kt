package com.neosofttech.pip_demo.view

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Rational
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.neosofttech.pip_demo.databinding.ActivityWebViewBinding
import com.neosofttech.pip_demo.databinding.FloatngWindowBinding
import com.neosofttech.pip_demo.viewmodel.WebViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class WebView : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var floatingBinding: FloatngWindowBinding
    private lateinit var pipButton: Button
    private val viewModel: WebViewModel by viewModels()

    private var floatingPlayerView: View? = null
    private var floatingYouTubePlayerView: YouTubePlayerView? = null

    private val REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        pipButton = binding.pipButton

        pipButton.setOnClickListener {
            viewModel.checkOverlayPermission(this)
        }

        viewModel.hasOverlayPermission.observe(this, Observer { hasPermission ->
            if (hasPermission) {
                createFloatingPlayer()
            } else {
                // Show a message requesting overlay permission
            }
        })

        viewModel.isPiPModeRequested.observe(this, Observer { isPiPModeRequested ->
            if (isPiPModeRequested) {
                enterPiPModeIfNeeded()
            }
        })
    }

    private fun createFloatingPlayer() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingBinding = FloatngWindowBinding.inflate(inflater)

        floatingPlayerView = floatingBinding.root
        floatingYouTubePlayerView = floatingBinding.youtubePlayerView

        val rootLayout: FrameLayout = findViewById(android.R.id.content)

        // Add the floating player view to the layout
        rootLayout.addView(floatingPlayerView)

        // Initialize the YouTube player
        viewModel.createFloatingPlayer(floatingYouTubePlayerView!!)

        // Set stop button behavior
        floatingBinding.customStopButton.setOnClickListener {
            viewModel.stopVideo()
            removeFloatingPlayer()
        }

        // Set initial parameters for the floating window
        setFloatingWindowLayoutParams()
    }

    private fun setFloatingWindowLayoutParams() {
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            leftMargin = 50
            topMargin = 50
            rightMargin = 50
        }
        floatingPlayerView?.layoutParams = params
    }

    private fun removeFloatingPlayer() {
        floatingPlayerView?.let {
            val rootLayout: FrameLayout = findViewById(android.R.id.content)
            rootLayout.removeView(it)
            floatingPlayerView = null
        }
    }

    private fun enterPiPModeIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && floatingPlayerView != null) {
            val aspectRatio = Rational(floatingPlayerView!!.width, floatingPlayerView!!.height)
            val pipParams = PictureInPictureParams.Builder()
                .setAspectRatio(aspectRatio)
                .setActions(emptyList())
                .build()

            enterPictureInPictureMode(pipParams)
            binding.root.visibility = View.GONE
        }
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        viewModel.onUserLeaveHint()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onPause()
    }

    override fun onStop() {
        super.onStop()
        viewModel.stopVideo()
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopVideo()
    }

    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode)
        if (isInPictureInPictureMode) {
            viewModel.onPictureInPictureModeChanged(true)
            floatingBinding.customStopButton.visibility = View.GONE
            setPiPWindowParams()
        } else {
            viewModel.onPictureInPictureModeChanged(false)
            binding.root.visibility = View.VISIBLE
            resetWindowParams()
        }
    }

    private fun setPiPWindowParams() {
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        floatingPlayerView?.layoutParams = params
    }

    private fun resetWindowParams() {
        setFloatingWindowLayoutParams()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onActivityResult(requestCode, resultCode, data)
    }
}
