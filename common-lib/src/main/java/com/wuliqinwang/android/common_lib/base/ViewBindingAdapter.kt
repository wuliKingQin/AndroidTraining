package com.wuliqinwang.android.common_lib.base

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import androidx.viewbinding.ViewBinding

/**
 * @Description: 常用RecyclerView数据适配器
 * @CreateDate: 2019-10-13
 * @Version: 1.0.0
 */
abstract class CommonAdapter<T>(dataSet: ArrayList<T>? = null):
    RecyclerView.Adapter<CommonViewHolder>(){

    // 数据集合
    private var mData = dataSet

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommonViewHolder {
        val viewBinding = createItemViewBinding(LayoutInflater.from(parent.context), parent, viewType)
        var viewHolder = onLoadViewHolder(viewBinding, viewType)
        if (viewHolder == null) {
            viewHolder = CommonViewHolder(viewBinding)
        }
        return viewHolder
    }

    // 获取Item对应的ViewBinding
    abstract fun createItemViewBinding(layoutInflater: LayoutInflater, parent: ViewGroup, viewType: Int): ViewBinding

    /**
     * DES: 获取对应位置的数据
     * @param position 位置
     * @return 返回对应位置的数据实例
     **/
    fun getItem(position: Int): T? {
        return if(position in 0 until itemCount) {
            mData?.get(position)
        } else {
            null
        }
    }

    /**
     * 设置数据集合
     * @param dataSet 数据集合
     * @param isRefresh 是否立即刷新，默认是true
     */
    fun setDataSet(dataSet: List<T>?, isRefresh: Boolean = true) {
        if(getDataList().isNotEmpty()) {
            clear()
        }
        if(dataSet?.isNotEmpty() == true) {
            addAll(dataSet, isRefresh = false)
        }
        if(isRefresh) {
            notifyDataSetChanged()
        }
    }

    /**
     * DES: 移除数据
     * @param index 索引位置
     * @param isRefresh 是否刷新
     **/
    fun remove(index: Int, isRefresh: Boolean = false) {
        synchronized(this) {
            if(index in 0 until itemCount) {
                getDataList().removeAt(index)
                if(isRefresh) {
                    notifyItemRemoved(index)
                }
            }
        }
    }

    /**
     * DES: 移除数据
     * @param data 数据
     * @param isRefresh 是否刷新
     **/
    fun remove(data: T, isRefresh: Boolean = false) {
        val removeIndex = getDataList().indexOf(data)
        remove(removeIndex, isRefresh)
    }

    /**
     * DES: 添加单个数据
     * @param data 数据
     * @param index 索引位置
     * @param isRefresh 是否刷新
     **/
    open fun add(data: T?, index: Int = 0, isRefresh: Boolean = true): Boolean {
        return data?.let {
            if(!getDataList().contains(it)) {
                val insertIndex = getFormatIndex(index)
                getDataList().add(insertIndex, it)
                if(isRefresh) {
                    notifyItemInserted(insertIndex)
                }
                true
            } else {
                false
            }
        } ?: false
    }

    /**
     * DES: 添加数据集合
     * @param index 插入的位置
     * @param dataSet 需要插入的数据
     * @param isRefresh 是否更新
     **/
    fun addAll(dataSet: List<T>?, index: Int = 0, isRefresh: Boolean = true) {
        if(dataSet?.isNotEmpty() == true) {
            val insertIndex = getFormatIndex(index)
            getDataList().addAll(insertIndex, dataSet)
            if(isRefresh) {
                notifyItemRangeInserted(insertIndex, dataSet.size)
            }
        }
    }

    /**
     * DES: 截取子列表
     * @param start 开始位置
     * @param end 结束位置
     * @return 返回截取的子列表
     **/
    fun clearSubList(start: Int, end: Int) {
        if(start in 0 until itemCount && end > start && end <= itemCount) {
            getDataList().subList(start, end).clear()
        }
    }

    // DES: 清除数据
    open fun clear() {
        getDataList().clear()
        notifyDataSetChanged()
    }

    /**
     * DES: 通知数据更新
     * @param data 数据集合
     **/
    open fun notifyTargetDataChange(data: T?) {
        data ?: return
        val refreshPosition = getDataList().indexOf(data)
        val position = getFormatIndex(refreshPosition, 1)
        notifyItemChanged(position)
    }

    /**
     * DES: 获取标准的索引值，不会越界的
     * @param index 索引值
     * @param revise 需要修正的值
     **/
    private fun getFormatIndex(index: Int, revise: Int = 0): Int  = when {
        index < 0 -> 0
        index > itemCount -> itemCount - revise
        else -> index
    }

    /**
     * DES: 获取数据列表
     * @return 列表实例
     **/
    fun getDataList(): ArrayList<T> {
        if(mData == null) {
            mData = ArrayList(10)
        }
        return mData!!
    }

    /**
     * 根据ViewType导入视图ViewHolder
     * 默认返回null
     * @param itemView item视图
     * @param viewType 视图类型
     * @return ViewHolder实例，默认返回null
     */
    open fun onLoadViewHolder(itemView: ViewBinding, viewType: Int): CommonViewHolder? = null

    override fun getItemCount(): Int = mData?.size ?: 0

    override fun onBindViewHolder(holder: CommonViewHolder, position: Int) {
        val currentData = mData?.get(position)
        if(currentData != null) {
            val viewType = getItemViewType(position)
            onBindDataForView(holder.viewBinding, currentData, position)
        }
    }

    /**
     * 为视图绑定数据
     * @param currentData 当前位置数据
     * @param position 位置
     */
    abstract fun onBindDataForView(viewBinding: ViewBinding, currentData: T, position: Int)
}

open class CommonViewHolder(var viewBinding: ViewBinding): RecyclerView.ViewHolder(viewBinding.root)


/**
 * DES: 获取向量类型的drawable, 该drawable可以修改颜色值
 *
 * @param vectorDrawableId 向量drawable资源Id
 * @return 返回向量类型的Drawable
 **/
fun ViewBinding.getVectorDrawable(vectorDrawableId: Int): VectorDrawableCompat? {
    return VectorDrawableCompat.create(root.resources, vectorDrawableId, root.context.theme)
}

/**
 * 获取字符资源Id
 * @param id 字符资源Id
 * @param args 参数
 * @return 返回字符串
 */
fun ViewBinding.getString(id: Int, vararg args: Any): String? {
    return root.context.getString(id, args)
}

/**
 * 获取颜色值
 * @param colorId 颜色资源id
 * @return 颜色值
 */
fun ViewBinding.getColor(colorId: Int): Int {
    return ContextCompat.getColor(root.context, colorId)
}

/**
 * 根据资源Id获取图标Drawable
 * @param iconResId 资源Id
 * @return Drawable实例
 */
fun ViewBinding.getDrawable(iconResId: Int): Drawable? {
    return ContextCompat.getDrawable(root.context, iconResId)
}
