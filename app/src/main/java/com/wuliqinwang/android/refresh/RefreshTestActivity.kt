package com.wuliqinwang.android.refresh

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.wuliqinwang.act.register.annotation.ActRegister
import com.wuliqinwang.android.common_lib.base.BaseActivity
import com.wuliqinwang.android.common_lib.base.CommonAdapter
import com.wuliqinwang.android.databinding.ActivityRefreshTestBinding
import com.wuliqinwang.android.databinding.RvRefreshTestItemBinding

@ActRegister(name = "下拉刷新测试")
class RefreshTestActivity: BaseActivity<ActivityRefreshTestBinding>(){

    private val adapter by lazy {
        object : CommonAdapter<String>() {
            override fun createItemViewBinding(
                layoutInflater: LayoutInflater,
                parent: ViewGroup,
                viewType: Int
            ): ViewBinding {
                return RvRefreshTestItemBinding.inflate(layoutInflater, parent, false)
            }

            override fun onBindDataForView(
                viewBinding: ViewBinding,
                currentData: String,
                position: Int
            ) {
                if (viewBinding is RvRefreshTestItemBinding) {
                    viewBinding.testTv.text = currentData
                }
            }
        }
    }

    override fun ActivityRefreshTestBinding.onBindDataForView(savedInstanceState: Bundle?) {
        refreshContent.adapter = adapter
        refreshContent.layoutManager = LinearLayoutManager(this@RefreshTestActivity)
        refreshContent.addItemDecoration(DividerItemDecoration(this@RefreshTestActivity, DividerItemDecoration.HORIZONTAL))
        for (i in 0 until 100) {
            adapter.add("===========i=$i==============")
        }
    }
}