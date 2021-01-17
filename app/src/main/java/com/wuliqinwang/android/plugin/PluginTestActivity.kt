package com.wuliqinwang.android.plugin

import android.os.Bundle
import com.wuliqinwang.act.register.annotation.ActRegister
import com.wuliqinwang.android.common_lib.base.BaseActivity
import com.wuliqinwang.android.common_lib.startActivityEx
import com.wuliqinwang.android.databinding.ActivityPluginTestBinding

// 用于测试Activity的插件化
@ActRegister(name = "Activity插件化测试", position = 1)
class PluginTestActivity: BaseActivity<ActivityPluginTestBinding>(){

    override fun ActivityPluginTestBinding.onBindDataForView(savedInstanceState: Bundle?) {
        jumpPluginBtn.setOnClickListener {
            HookHelper.hookStartActivity()
            HookHelper.hookHandleMessageCallback()
            startActivityEx(ReallyPluginActivity::class.java)
        }
    }
}