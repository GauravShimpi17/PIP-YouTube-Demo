package com.neosofttech.pip_demo.view

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Rational
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.neosofttech.pip_demo.databinding.ActivityWebViewBinding
import com.neosofttech.pip_demo.databinding.FloatngWindowBinding
import com.neosofttech.pip_demo.viewModel.WebViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class WebView : AppCompatActivity() {

    private lateinit var binding: ActivityWebViewBinding
    private lateinit var floatingBinding: FloatngWindowBinding
    private val viewModel: WebViewModel by viewModels()

    private var floatingParentLayout: View? = null
    private var youtubePlayerView: YouTubePlayerView? = null

    private var windowManager: WindowManager? = null

    private var xDelta = 0f
    private var yDelta = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWebViewBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.isFloatingWindowVisible.observe(this) { isVisible ->
            if (isVisible) {
                // Disable the PiP button if the floating window is visible
                binding.pipButton.isEnabled = false
            } else {
                // Enable the PiP button if the floating window is not visible
                binding.pipButton.isEnabled = true
            }
        }

        binding.pipButton.setOnClickListener {
            if (viewModel.isFloatingWindowVisible.value == true) {
                // If the floating window is visible, stop the video and remove the window
                viewModel.stopVideo()
            } else {
                // Otherwise, check for permissions and create the floating player
                viewModel.checkOverlayPermission(this)
            }
        }

        viewModel.hasOverlayPermission.observe(this) { hasPermission ->
            if (hasPermission) {
                // Disable the PiP button if overlay permission is granted
                binding.pipButton.isEnabled = false
                createFloatingPlayer()  // Proceed to create the floating player
            } else {
                // Enable the PiP button if overlay permission is not granted
                binding.pipButton.isEnabled = true
                Toast.makeText(this, "Permit Overlay", Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.isPiPModeRequested.observe(this) { isPiPModeRequested ->
            if (isPiPModeRequested) {
                enterPiPModeIfNeeded()
            }
        }

        binding.pipButton4.setOnClickListener {
            Intent(this, MovableDemo::class.java).also {
                startActivity(it)
            }
        }

        binding.pipButton3.setOnClickListener {
            Intent(this, MainActivity::class.java).also {
                startActivity(it)
            }
        }
    }

    private fun createFloatingPlayer() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingBinding = FloatngWindowBinding.inflate(inflater)

        floatingParentLayout = floatingBinding.root
        youtubePlayerView = floatingBinding.youtubePlayerView

        val rootLayout: FrameLayout = findViewById(android.R.id.content)
        rootLayout.addView(floatingParentLayout)

        viewModel.createFloatingPlayer(youtubePlayerView!!)

        floatingBinding.customStopButton.setOnClickListener {
            viewModel.stopVideo()
            removeFloatingPlayer()
        }

        // Set initial parameters for the floating window layout
        setFloatingWindowLayoutParams()

        // Set touch listeners for both move button and parent layout
        floatingBinding.move.setOnTouchListener(getTouchListener(floatingParentLayout!!))
        floatingParentLayout?.setOnTouchListener(getTouchListener(floatingParentLayout!!))
    }

    private fun getTouchListener(viewToMove: View): View.OnTouchListener {
        return object : View.OnTouchListener {
            override fun onTouch(v: View, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        xDelta = viewToMove.x - event.rawX
                        yDelta = viewToMove.y - event.rawY
                    }
                    MotionEvent.ACTION_MOVE -> {
                        viewToMove.animate()
                            .x(event.rawX + xDelta)
                            .y(event.rawY + yDelta)
                            .setDuration(0)
                            .start()
                    }
                    else -> return false
                }
                return true
            }
        }
    }

    private fun setFloatingWindowLayoutParams() {
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        floatingParentLayout?.layoutParams = params
    }

    private fun removeFloatingPlayer() {
        floatingParentLayout?.let {
            val rootLayout: FrameLayout = findViewById(android.R.id.content)
            rootLayout.removeView(it)
            floatingParentLayout = null
        }
    }

    private fun enterPiPModeIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && floatingParentLayout != null) {
            val aspectRatio = Rational(floatingParentLayout!!.width, floatingParentLayout!!.height)
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
            // When PiP mode is active
            viewModel.onPictureInPictureModeChanged(true)

            // Set the YouTube player view to MATCH_PARENT (full screen)
            youtubePlayerView?.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )

            // Hide the move button and fullscreen button in PiP mode
            floatingBinding.move.visibility = View.GONE
            floatingBinding.fullscreen.visibility = View.GONE
            floatingBinding.customStopButton.visibility = View.GONE
        } else {
            // When PiP mode is inactive (switching back to floating window)
            viewModel.onPictureInPictureModeChanged(false)

            // Reset the YouTube player view layout to its original size
            setFloatingWindowLayoutParams()

            // Make the move button and fullscreen button visible again
            floatingBinding.move.visibility = View.VISIBLE
            floatingBinding.fullscreen.visibility = View.VISIBLE
            floatingBinding.customStopButton.visibility = View.VISIBLE
        }
    }


    private fun setPiPWindowParams() {
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        floatingParentLayout?.layoutParams = params
    }

    private fun resetWindowParams() {
        setFloatingWindowLayoutParams()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        viewModel.onActivityResult(requestCode, resultCode, data)
    }
}
