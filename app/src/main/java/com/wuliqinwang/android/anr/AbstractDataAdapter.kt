package com.wuliqinwang.android.anr

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView

// 抽象一个适合DataBinding的数据适配器
abstract class AbstractDataAdapter<T> :
    RecyclerView.Adapter<AbstractDataAdapter.DataViewHolder>() {

    // 数据列表
    abstract val dataList: DataOperator<T>

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataViewHolder {
        val layoutId = getItemLayoutId(viewType)
        val viewBinding = DataBindingUtil.inflate<ViewDataBinding>(
            LayoutInflater.from(parent.context),
            layoutId,
            parent,
            false
        )
        return DataViewHolder(viewBinding.root)
    }

    // 根据位置获取Item的数据模型
    private fun getItem(position: Int): T? {
        return if (position in 0 until itemCount) {
            dataList[position]
        } else {
            null
        }
    }

    // 获取Item的布局Id根据视图类型
    abstract fun getItemLayoutId(viewType: Int): Int

    override fun onBindViewHolder(holder: DataViewHolder, position: Int) {
        val viewDataBinding = DataBindingUtil.getBinding<ViewDataBinding>(holder.itemView)
        val dataModel = getItem(position)
        if (dataModel != null) {
            viewDataBinding?.onDataBinding(dataModel)
        }
        viewDataBinding?.executePendingBindings()
    }

    // 在该方法中进行自己数据设置
    abstract fun ViewDataBinding.onDataBinding(data: T)

    override fun getItemCount(): Int {
        return dataList.size
    }

    // 包装了ViewDataBinding的ViewHolder类
    class DataViewHolder(root: View) : RecyclerView.ViewHolder(root)

    // 数据操作类的委托类
    class DataOperator<T>(
        private val adapter: RecyclerView.Adapter<*>,
        dataList: MutableList<T>
    ) : MutableList<T> by dataList {

        // 添加元素带刷新功能
        fun addWithNotify(data: T?, index: Int = adapter.itemCount) {
            if (data != null) {
                add(index, data)
                adapter.notifyItemInserted(index)
            }
        }

        // 添加列表数据
        fun addAllWithNotify(dataSet: List<T>?, index: Int = adapter.itemCount) {
            if (dataSet.isNullOrEmpty()) {
                return
            }
            if (addAll(index, dataSet)) {
                adapter.notifyItemRangeInserted(index, dataSet.size)
            }
        }

        // 该方法会先清除之前的数据，在进行添加信息的数据集合
        fun setDataWithNotify(dataList: List<T>?) {
            if (dataList.isNullOrEmpty()) {
                return
            }
            clearWithNotify()
            addAllWithNotify(dataList)
        }

        // 移除数据带通知
        fun removeWithNotify(data: T?) {
            val index = indexOf(data)
            if (remove(data)) {
                adapter.notifyItemRemoved(index)
            }
        }

        // 移除所有的数据带通知
        fun removeAllWithNotify(dataList: List<T>?) {
            if (dataList.isNullOrEmpty()) {
                return
            }
            val index = indexOf(dataList[0])
            if (removeAll(dataList)) {
                adapter.notifyItemRangeRemoved(index, dataList.size)
            }
        }

        // 移除数据带通知，根据位置
        fun removeAtWithNotify(index: Int) {
            if (removeAt(index) != null) {
                adapter.notifyItemChanged(index)
            }
        }

        // 清除所有的数据带通知
        fun clearWithNotify() {
            clear()
            adapter.notifyDataSetChanged()
        }
    }
}