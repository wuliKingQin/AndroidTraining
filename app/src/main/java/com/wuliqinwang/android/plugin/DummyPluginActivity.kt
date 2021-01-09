package com.wuliqinwang.android.plugin

import android.os.Bundle
import com.wuliqinwang.android.common_lib.base.BaseActivity
import com.wuliqinwang.android.databinding.ActivityPluginDummyBinding

/**
 * @Description: 用于打开插件用的傀儡界面
 */
class DummyPluginActivity: BaseActivity<ActivityPluginDummyBinding>() {

    override fun ActivityPluginDummyBinding.onBindDataForView(savedInstanceState: Bundle?) {
        dummyTv.text = "我是傀儡界面"
    }
}