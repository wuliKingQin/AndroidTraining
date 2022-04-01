package com.wuliqinwang.android

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : AppCompatActivity() {
    private var time = 3
    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        timeTv.text = "我是启动闪屏页面${time}秒后启动"
        testBtn.setOnClickListener {
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        launchMain()
    }

    private fun launchMain() {
        handler.postDelayed({
            timeTv.text = "我是启动闪屏页面${time}秒后启动"
            if (time > 0) {
                time -= 1
                launchMain()
            } else {
                startActivity(Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        }, 1000)
    }
}