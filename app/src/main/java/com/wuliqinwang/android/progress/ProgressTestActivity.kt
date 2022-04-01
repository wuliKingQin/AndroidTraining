package com.wuliqinwang.android.progress

import android.os.Bundle
import com.wuliqinwang.act.register.annotation.ActRegister
import com.wuliqinwang.android.R
import com.wuliqinwang.android.common_lib.base.BaseActivity
import com.wuliqinwang.android.databinding.ActivityProgressTestBinding

@ActRegister(name = "特殊进度视图测试", position = 1)
class ProgressTestActivity: BaseActivity<ActivityProgressTestBinding>(){

    override fun ActivityProgressTestBinding.onBindDataForView(savedInstanceState: Bundle?) {
        progressView.setProgress(0, 20)
        progressView.rightIconRes = R.mipmap.ic_pop_operating_funds_reward
        progressView.bubbleText = "15人已领取 还差5人获得神秘大奖还差5人获得神秘大奖还差5人获得神秘大奖"

        progressView01.setProgress(1, 20)
        progressView01.rightIconRes = R.mipmap.ic_pop_operating_funds_reward
        progressView01.bubbleText = "15人已领取 还差5人获得神秘大奖"

        progressView02.setProgress(19, 20)
        progressView02.rightIconRes = R.mipmap.ic_pop_operating_funds_reward
        progressView02.bubbleText = "15人已领取"

        progressView03.setProgress(20, 20)
        progressView03.rightIconRes = R.mipmap.ic_pop_operating_funds_reward
        progressView03.bubbleText = "15人"

        progressView04.setProgress(10, 20)
        progressView04.rightIconRes = R.mipmap.ic_pop_operating_funds_reward
        progressView04.bubbleText = "15人已领取 还差5人获得"
    }

}