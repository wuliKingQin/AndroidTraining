package com.wuliqinwang.android.recyclerview

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewbinding.ViewBinding
import com.utopia.android.ulog.ULog
import com.wuliqinwang.act.register.annotation.ActRegister
import com.wuliqinwang.android.common_lib.base.BaseActivity
import com.wuliqinwang.android.common_lib.base.CommonAdapter
import com.wuliqinwang.android.databinding.ActivityRecyclerviewTestBinding
import com.wuliqinwang.android.databinding.ActivityRecyclerviewTestItemBinding
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

/**
 * @Description:
 * @Author: 秦王
 * @Version: 1.0.0
 */
@ActRegister(name = "RecyclerView测试")
class TestRecyclerViewActivity: BaseActivity<ActivityRecyclerviewTestBinding>(){

    private val context by lazy {
        Job() + Dispatchers.Main + CoroutineName("主协程")
    }

    private var mAdapter: CommonAdapter<String>? = null

    override fun ActivityRecyclerviewTestBinding.onBindDataForView(savedInstanceState: Bundle?) {
        mAdapter = object :CommonAdapter<String>() {

            override fun createItemViewBinding(
                layoutInflater: LayoutInflater,
                parent: ViewGroup,
                viewType: Int
            ): ViewBinding {
                return ActivityRecyclerviewTestItemBinding.inflate(layoutInflater, parent, false)
            }

            override fun onBindDataForView(
                viewBinding: ViewBinding,
                currentData: String,
                position: Int
            ) {
                if (viewBinding is ActivityRecyclerviewTestItemBinding) {
                    viewBinding.testText.text = currentData
                }
            }

        }
        testRecyclerView.layoutManager = LinearLayoutManager(this@TestRecyclerViewActivity)
        testRecyclerView.adapter = mAdapter
        testBtn.setOnClickListener {
            mAdapter?.notifyItemChanged(0)
        }
        mAdapter?.setDataSet(arrayListOf("东方不败"))
        mAdapter?.notifyDataSetChanged()
        ULog.d("00", "===========================")
    }
}