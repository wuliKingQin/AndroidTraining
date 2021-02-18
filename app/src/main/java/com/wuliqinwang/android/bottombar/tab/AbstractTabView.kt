package com.wuliqinwang.android.bottombar.tab

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.wuliqinwang.android.bottombar.TabItemView

abstract class AbstractTabView(
    private var iconLoader: IconLoader? = null
): TabView {

    // DES: 数据
    lateinit var model: TabItemView.TabItemModel
    // DES: 容器
    private lateinit var mItemContainer: TabItemView

    override fun initializer(model: TabItemView.TabItemModel, itemContainer: TabItemView) {
        this.model = model
        mItemContainer = itemContainer
        createTabView(getContext())
    }

    // DES: 是被选中
    fun isSelected(): Boolean = mItemContainer.isSelected

    // DES: 在该方法中创建自己的tab视图
    abstract fun createTabView(context: Context)

    // 设置布局视图
    fun setContentView(layout: Int) {
        View.inflate(mItemContainer.context, layout, mItemContainer)
    }

    override fun onInterceptTouchEvent(event: TouchEvent) = false

    override fun onTouchEvent(event: TouchEvent): Boolean = false

    override fun dispatchTouchEvent(event: TouchEvent): Boolean = false

    override fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        mItemContainer.requestDisallowInterceptTouchEvent(disallowIntercept)
    }

    // DES: 添加视图
    fun addView(child: View, params: ConstraintLayout.LayoutParams? = null) {
        if (params == null) {
            mItemContainer.addView(child)
        } else {
            mItemContainer.addView(child, params)
        }
    }

    // DES: 通过Id找到目标视图
    fun <T: View> findViewById(id: Int): T {
        return mItemContainer.findViewById(id)
    }

    // DES: 设置名字的状态
    fun setNameStatus(
        // DES: 目标视图
        targetView: TextView,
        // DES: 是否选中
        selected: Boolean,
        // DES: 默认选中颜色
        defaultSelectedColor: Int = Color.BLACK,
        // DES: 默认未选中的颜色
        defaultUnselectedColor: Int = Color.GRAY) {
        targetView.setTextColor(TabHelper.getFontColor(targetView.context, if(selected) {
            model.selectedFontColor
        } else {
            model.unselectedFontColor
        }, if (selected) defaultSelectedColor else defaultUnselectedColor))
    }

    // DES: 设置图标状态
    fun setIconStatus(targetView: ImageView, selected: Boolean) {
        TabHelper.setIconStatus(
            targetView,
            selected,
            model.selectedIcon,
            model.unselectedIcon,
            iconLoader
        )
    }

    // DES: 获取到上下文
    fun getContext() = mItemContainer.context
}