package com.wuliqinwang.android

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.viewbinding.ViewBinding
import com.wuliqinwang.act.register.ActivitiesFactory
import com.wuliqinwang.android.base.AbstractListActivity
import com.wuliqinwang.android.databinding.RvMainActivityItemBinding

// 用于测试使用的主界面
class MainActivity : AbstractListActivity<MainActivity.ActivityBo>() {

    private val mActivityFactory by lazy {
        ActivitiesFactory()
    }

    // DES: 实例
    data class ActivityBo(
        var name: String,
        var targetClass: Class<*>
    )

    override fun createItemViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ) = RvMainActivityItemBinding.inflate(inflater, parent, false)

    override fun onBindDataForItemView(
        viewBinding: ViewBinding,
        currentData: ActivityBo,
        position: Int
    ) {
        if (viewBinding is RvMainActivityItemBinding) {
            viewBinding.activityNameTv.text = currentData.name
        }
        viewBinding.root.setOnClickListener {
            // 启动测试界面
            startActivityEx(currentData.targetClass)
        }
    }

    override fun FrameLayout.createTopView() {
        addView(
            ImageView(context).apply {
                setImageResource(R.mipmap.ic_launcher)
                layoutParams = FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    gravity = Gravity.CENTER
                }
            }
        )
    }

    override fun onLoadListData(savedInstanceState: Bundle?) {
        val activityList = arrayListOf<ActivityBo>()
        mActivityFactory.activities.forEach {
            activityList.add(ActivityBo(it.key, it.value))
        }
        Log.e("log", "onLoadListData: " )
        setListData(activityList)
    }
}