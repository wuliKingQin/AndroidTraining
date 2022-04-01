package com.wuliqinwang.android.anr

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.wuliqinwang.act.register.annotation.ActRegister
import com.wuliqinwang.android.R
import com.wuliqinwang.android.databinding.ActivityAnrTestBinding

@ActRegister(name = "ANR发生时，历史消息调度情况测试", position = 1)
class AnrActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityAnrTestBinding>(this, R.layout.activity_anr_test)?.apply {
            lifecycleOwner = this@AnrActivity
        }
    }
}