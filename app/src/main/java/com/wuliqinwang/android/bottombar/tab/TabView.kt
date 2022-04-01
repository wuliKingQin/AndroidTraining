package com.wuliqinwang.android.bottombar.tab

import android.content.Context
import android.graphics.Color
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.wuliqinwang.android.bottombar.TabItemView
import java.lang.Exception

interface TabView: TouchEvent {
    // DES: 位置
    var position: Int
    // DES: 设置图标加载器
    var iconLoader: IconLoader?
    // DES: 初始化方法
    fun initializer(model: TabItemView.TabItemModel, itemContainer: TabItemView)
    // DES: 设置选中状态
    fun setSelected(position: Int, selected: Boolean)
}

// DES: 抽象一个简单的tabView，如果继承该类需要自己实现选中状态的处理
abstract class AbstractTabView(
    override var iconLoader: IconLoader? = null
): TabView {
    override var position: Int = TabItemView.INVALID_POSITION
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
    open fun setItemView(layout: Int) {
        try {
            View.inflate(mItemContainer.context, layout, mItemContainer)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent?) = false

    override fun onTouchEvent(event: MotionEvent?): Boolean = false

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean = false

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
    fun <T: View> findViewById(id: Int, generator: (()-> T)? = null): T? {
        return try {
            mItemContainer.findViewById(id) ?: generator?.invoke()
        } catch (e: Exception) {
            generator?.invoke()
        }
    }

    // DES: 设置名字的状态
    open fun setNameStatus(
        // DES: 目标视图
        targetView: TextView,
        // DES: 是否选中
        selected: Boolean = isSelected(),

        // DES: 默认选中颜色
        defaultSelectedColor: Int = Color.BLACK,
        // DES: 默认未选中的颜色
        defaultUnselectedColor: Int = Color.GRAY,
        // DES: 当前位置
        position: Int = TabItemView.INVALID_POSITION
    ) {
        targetView.text = model.tabName
        targetView.setTextColor(TabHelper.getFontColor(targetView.context, if(selected) {
            model.selectedFontColor
        } else {
            model.unselectedFontColor
        }, if (selected) defaultSelectedColor else defaultUnselectedColor))
    }

    // DES: 设置图标状态
    open fun setIconStatus(
        targetView: ImageView,
        selected: Boolean = isSelected(),
        position: Int = -1
    ) {
        TabHelper.setIconStatus(
            targetView,
            selected,
            model.selectedIcon,
            model.unselectedIcon,
            iconLoader,
            position
        )
    }

    // DES: 获取到上下文
    fun getContext() = mItemContainer.context
}

// DES: 抽象一个已经实现了tab状态的处理的TabView， 你只需要提供你自己item布局
abstract class AbstractSampleTabView(
    iconLoader: IconLoader? = null
): AbstractTabView(iconLoader) {

    // DES: tab图标视图
    var iconView: ImageView? = null
        private set
    // DES: tab名字视图
    var nameView: TextView? = null
        private set

    abstract fun getTabIconViewId(): Int

    abstract fun getTabNameViewId(): Int

    abstract fun getTabLayoutId(): Int

    override fun createTabView(context: Context) {
        setItemView(getTabLayoutId())
    }

    override fun setItemView(layout: Int) {
        super.setItemView(layout)
        var iconViewId = getTabIconViewId()
        var nameViewId = getTabNameViewId()
        iconView = findViewById(iconViewId){
            iconViewId = View.generateViewId()
            nameViewId = View.generateViewId()
            createDefaultIconView(iconViewId, nameViewId)
        }?.apply {
            setIconStatus(this, isSelected)
        }
        nameView = findViewById(nameViewId) {
            createDefaultNameView(nameViewId)
        }?.apply {
            setNameStatus(this, isSelected)
        }
    }

    // DES: 创建默认的Icon视图
    private fun createDefaultIconView(iconViewId: Int, nameViewId: Int): ImageView {
        return ImageView(getContext()).apply {
            id = iconViewId
            layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                constrainedHeight = true
                bottomToTop = nameViewId
                topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
                verticalChainStyle = ConstraintLayout.LayoutParams.CHAIN_PACKED
                bottomMargin = model.iconToNameInterval
            }
            addView(this)
        }
    }

    // DES: 创建默认的名字文本视图
    private fun createDefaultNameView(nameViewId: Int): TextView {
        return TextView(getContext()).apply {
            id = nameViewId
            layoutParams = ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                leftToLeft = ConstraintLayout.LayoutParams.PARENT_ID
                rightToRight = ConstraintLayout.LayoutParams.PARENT_ID
                bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID
                if (iconView != null) {
                    topToBottom = iconView!!.id
                } else {
                    topToTop = ConstraintLayout.LayoutParams.PARENT_ID
                }
            }
            addView(this)
        }
    }

    override fun setIconStatus(targetView: ImageView, selected: Boolean, position: Int) {
        super.setIconStatus(targetView, selected, position)
        setIconStyle(targetView)
    }

    // DES: 设置图标风格
    private fun setIconStyle(targetView: ImageView) {
        var isDimensionChange = false
        val layoutParams = (targetView.layoutParams as? ViewGroup.MarginLayoutParams)?.apply {
            if (width != model.iconWidth) {
                isDimensionChange = true
                width = model.iconWidth
            }
            if (height != model.iconHeight) {
                isDimensionChange = true
                height = model.iconHeight
            }
            if (bottomMargin != model.giveSize.toInt() && model.isGive) {
                isDimensionChange = true
                bottomMargin = model.giveSize.toInt()
            }
        }
        if (isDimensionChange && layoutParams != null) {
            targetView.layoutParams = layoutParams
        }
    }

    override fun setNameStatus(
        targetView: TextView,
        selected: Boolean,
        defaultSelectedColor: Int,
        defaultUnselectedColor: Int,
        position: Int
    ) {
        if (!model.isGive) {
            super.setNameStatus(targetView, selected, defaultSelectedColor, defaultUnselectedColor, position)
        }
        targetView.visibility = if (model.isGive) View.GONE else View.VISIBLE
    }

    override fun setSelected(position: Int, selected: Boolean) {
        iconView?.apply {
            setIconStatus(this, selected, position)
        }
        nameView?.apply {
            setNameStatus(this, selected)
        }
    }
}