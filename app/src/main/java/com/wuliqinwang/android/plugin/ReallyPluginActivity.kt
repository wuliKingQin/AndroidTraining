package com.wuliqinwang.android.plugin

import android.os.Bundle
import com.wuliqinwang.act.register.annotation.ActRegister
import com.wuliqinwang.android.base.BaseActivity
import com.wuliqinwang.android.databinding.ActivityPluginReallyBinding

/**
 * @Description: 真的插件界面
 */
class ReallyPluginActivity: BaseActivity<ActivityPluginReallyBinding>() {

    override fun ActivityPluginReallyBinding.onBindDataForView(savedInstanceState: Bundle?) {
        reallyPluginTv.text = "我是真的插件哦"
    }
}