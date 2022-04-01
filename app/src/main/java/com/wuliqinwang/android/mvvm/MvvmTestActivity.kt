package com.wuliqinwang.android.mvvm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.wuliqinwang.act.register.annotation.ActRegister
import com.wuliqinwang.android.R
import com.wuliqinwang.android.databinding.ActivityMvvmTestBinding

@ActRegister(name = "MVVM设计模式测试", position = 0)
class MvvmTestActivity: AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        DataBindingUtil.setContentView<ActivityMvvmTestBinding>(this, R.layout.activity_mvvm_test).apply {
            vm = ViewModelProvider(this@MvvmTestActivity).get(MvvmViewModel::class.java)
            lifecycleOwner = this@MvvmTestActivity
        }
    }
}