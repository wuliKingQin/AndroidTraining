package com.wuliqinwang.android.common_lib.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.wuliqinwang.android.databinding.ActivityAbstractListBinding


// 封装列表视图，减少重复类的创建
abstract class AbstractListActivity<D>: BaseActivity<ActivityAbstractListBinding>(){

    // 用于数据适配器
    private val mDataAdapter by lazy {
        object : CommonAdapter<D>() {
            override fun createItemViewBinding(
                layoutInflater: LayoutInflater,
                parent: ViewGroup,
                viewType: Int
            ): ViewBinding = this@AbstractListActivity.createItemViewBinding(layoutInflater, parent, viewType)

            override fun onBindDataForView(
                viewBinding: ViewBinding,
                currentData: D,
                position: Int
            ) {
                onBindDataForItemView(viewBinding, currentData, position)
            }

        }
    }

    // 创建Item的ViewBinding
    abstract fun createItemViewBinding(inflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewBinding

    // 为列表里的item绑定数据
    abstract fun onBindDataForItemView(viewBinding: ViewBinding, currentData: D, position: Int)

    override fun ActivityAbstractListBinding.onBindDataForView(savedInstanceState: Bundle?) {
        topContentFl.apply {
            createTopView()
        }
        dataRlv.apply {
            adapter = mDataAdapter
            layoutManager = this@AbstractListActivity.getLayoutManager()
            addItemDecoration(getItemDecoration())
            this@AbstractListActivity.getItemAnimator()?.apply {
                itemAnimator = this
            }
        }
        onLoadListData(savedInstanceState)
    }

    // 在该方法中导入列表数据
    abstract fun onLoadListData(savedInstanceState: Bundle?)

    // 在该方法中添加顶部视图
    open fun FrameLayout.createTopView() {
    }

    // 设置列表数据
    fun setListData(dataSet: List<D>?) {
        if (dataSet.isNullOrEmpty()) {
            return
        }
        mDataAdapter.clear()
        mDataAdapter.setDataSet(dataSet)
    }

    // 清楚列表数据
    fun clearList() {
        mDataAdapter.clear()
    }

    // 添加到数据到列表
    fun addListData(data: D, index: Int = 0, isRefresh: Boolean = false) {
        mDataAdapter.add(data, index, isRefresh)
    }

    // 追加数据集合
    fun appendDataSet(dataSet: List<D>?) {
        if (dataSet.isNullOrEmpty()) {
            return
        }
        mDataAdapter.addAll(dataSet)
    }

    // 通知数据集合改变
    fun notifyItemChanged(position: Int) {
        mDataAdapter.notifyItemChanged(position)
    }

    // 设置Item的动画
    open fun getItemAnimator(): RecyclerView.ItemAnimator? = null

    // 设置分割线
    open fun getItemDecoration(): RecyclerView.ItemDecoration
    = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)

    // 获取列表的布局管理器
    open fun getLayoutManager(): RecyclerView.LayoutManager? {
        return LinearLayoutManager(this)
    }
}