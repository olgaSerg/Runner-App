package com.example.runnerapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.ImageView
import bolts.Task
import com.example.runnerapp.R
import java.lang.Thread.sleep

class InitActivity : AppCompatActivity() {

    private var imageViewLogo: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        imageViewLogo = findViewById(R.id.image_view_logo)

        val imageViewLogo = imageViewLogo ?: return
        val animationRotateCenter = AnimationUtils.loadAnimation(
            this, R.anim.rotate_anim
        )

        imageViewLogo.startAnimation(animationRotateCenter)

        Task.callInBackground {
            sleep(2000)
        }.onSuccess {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
        }
    }
}