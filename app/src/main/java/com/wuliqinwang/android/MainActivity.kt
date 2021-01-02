package com.wuliqinwang.android

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.wuliqinwang.act.register.ActivitiesFactory
import com.wuliqinwang.android.base.BaseActivity
import com.wuliqinwang.android.base.CommonAdapter
import com.wuliqinwang.android.databinding.ActivityMainBinding
import com.wuliqinwang.android.databinding.ViewActivityItemBinding

// 用于测试使用的主界面
class MainActivity : BaseActivity<ActivityMainBinding>() {

    private val mActivityFactory by lazy {
        ActivitiesFactory()
    }

    // DES: 适配器
    private val mAdapter by lazy {
        object :CommonAdapter<ActivityBo>() {

            override fun createItemViewBinding(
                layoutInflater: LayoutInflater,
                parent: ViewGroup,
                viewType: Int
            ): ViewBinding = ViewActivityItemBinding.inflate(layoutInflater, parent, false)

            override fun onBindDataForView(
                viewBinding: ViewBinding,
                currentData: ActivityBo,
                position: Int
            ) {
                if (viewBinding is ViewActivityItemBinding) {
                    viewBinding.activityNameTv.text = currentData.name
                }
                viewBinding.root.setOnClickListener {
                    // 启动测试界面
                    startActivityEx(currentData.targetClass)
                }
            }
        }
    }

    override fun ActivityMainBinding.onBindDataForView(savedInstanceState: Bundle?) {
        val activityList = arrayListOf<ActivityBo>()
        mActivityFactory.activities.forEach {
            activityList.add(ActivityBo(it.key, it.value))
        }
        activitiesRlv.apply {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.HORIZONTAL))
            adapter = mAdapter
        }
        mAdapter.addAll(activityList)
    }

    // DES: 实例
    data class ActivityBo(
        var name: String,
        var targetClass: Class<*>
    )
}