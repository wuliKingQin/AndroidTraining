package com.wuliqinwang.android.hotfix

import android.os.Bundle
import com.wuliqinwang.act.register.annotation.ActRegister
import com.wuliqinwang.android.common_lib.base.BaseActivity
import com.wuliqinwang.android.databinding.ActivityHotfixBinding

/**
 * @Author: 秦王
 */
@ActRegister(name = "热修复测试", position = 2)
class HotFixActivity: BaseActivity<ActivityHotfixBinding>(){
    override fun ActivityHotfixBinding.onBindDataForView(savedInstanceState: Bundle?) {
        hotFixTestTv.text = "来了，老弟，进来表示修复成功"
    }
}