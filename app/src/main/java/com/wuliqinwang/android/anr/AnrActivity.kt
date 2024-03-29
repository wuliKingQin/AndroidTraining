package com.wuliqinwang.android.anr

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wuliqinwang.act.register.annotation.ActRegister
import com.wuliqinwang.android.R
import com.wuliqinwang.android.databinding.ActivityAnrTestBinding

@ActRegister(name = "ANR发生时，历史消息调度情况测试", position = 1)
class AnrActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityAnrTestBinding>(this, R.layout.activity_anr_test)
            ?.apply {
                val anrViewModel = ViewModelProvider(this@AnrActivity).get(AnrViewModel::class.java)
                vm = anrViewModel
                lifecycleOwner = this@AnrActivity
                recordRv.layoutManager = LinearLayoutManager(
                    this@AnrActivity,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )
                recordRv.adapter = anrViewModel.recordAdapter
            }
    }
}