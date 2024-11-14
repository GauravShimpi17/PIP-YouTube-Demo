package com.neosofttech.pip_demo.view

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.neosofttech.pip_demo.databinding.ActivityMovableDemoBinding
import com.neosofttech.pip_demo.viewModel.VideoViewModel
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class MovableDemo : AppCompatActivity() {
    private lateinit var binding: ActivityMovableDemoBinding

    private val videoViewModel: VideoViewModel by viewModels()
    private var xDelta = 0f
    private var yDelta = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovableDemoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get references to the move button and parent layout
        val moveButton: ImageButton = binding.move
        val parentLayout: View = binding.youtubePlayerView // Assuming a container for youtubePlayerView
        val player :YouTubePlayerView = binding.youtubePlayerView1

        // Set up touch listener for moveButton
        moveButton.setOnTouchListener(getTouchListener(parentLayout))
        parentLayout.setOnTouchListener(getTouchListener(parentLayout))
    }

    private fun getTouchListener(viewToMove: View): View.OnTouchListener {
        return View.OnTouchListener { _, event ->
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
                else -> return@OnTouchListener false
            }
            true
        }
    }
}
