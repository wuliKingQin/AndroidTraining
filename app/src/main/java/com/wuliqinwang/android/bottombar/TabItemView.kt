package com.wuliqinwang.android.bottombar

import android.annotation.SuppressLint
import android.content.Context
import android.view.MotionEvent
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.wuliqinwang.android.bottombar.tab.IconLoader
import com.wuliqinwang.android.bottombar.tab.TabView

@SuppressLint("ViewConstructor")
class TabItemView private constructor(
    context: Context,
    private var tabView: TabView,
    model: TabItemModel
): ConstraintLayout(context){

    // DES: TabItem的数据模型
    val dataModel = model

    companion object {
        // DES: 无效位置
        const val INVALID_POSITION = -1
        // DES: 构建一个TabItemBuilder实例
        fun builder(tabItemModel: TabItemModel? = null): TabItemBuilder {
            return if (tabItemModel != null) {
                TabItemBuilder(tabItemModel)
            } else {
                TabItemBuilder()
            }
        }
    }

    init {
        tabView.initializer(model, this)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        return if (!tabView.onTouchEvent(event)) {
            super.onTouchEvent(event)
        } else {
            true
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (!tabView.dispatchTouchEvent(ev)) {
            super.dispatchTouchEvent(ev)
        } else {
            true
        }
    }

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (!tabView.onInterceptTouchEvent(ev)) {
            super.onInterceptTouchEvent(ev)
        } else {
            true
        }
    }

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        tabView.setSelected((tag as? Int) ?: INVALID_POSITION, selected)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T: TabView> getTabView(): T? {
        return tabView as? T
    }

    // TabItemBuilder
    class TabItemBuilder(
        private var model: TabItemModel = TabItemModel()
    ) {

        // DES: 保存tabView
        private var mTabView: TabView? = null
        // DES: 图标加载器
        private var mIconLoader: IconLoader? = null

        // DES: 设置选中和未选中的图标, 支持资源Id, Bitmap, Drawable, 以及连接地址
        fun setIcon(selectedIcon: Any?, unselectedIcon: Any?): TabItemBuilder {
            model.selectedIcon = selectedIcon
            model.unselectedIcon = unselectedIcon
            return this
        }

        // DES: 设置图标的宽度
        fun setIconWidth(width: Int): TabItemBuilder {
            model.iconWidth = width
            return this
        }

        // DES: 设置图标的高度
        fun setIconHeight(height: Int): TabItemBuilder {
            model.iconHeight = height
            return this
        }

        // DES: 设置图标的大小
        fun setIconSize(width: Int, height: Int): TabItemBuilder {
            model.iconWidth = width
            model.iconHeight = height
            return this
        }

        // DES: 图标到名字的间隔大小
        fun setIconToNameInterval(interval: Int): TabItemBuilder {
            model.iconToNameInterval = interval
            return this
        }

        // DES: 设置Tab的名字
        fun setTabName(tabName: CharSequence?): TabItemBuilder {
            model.tabName = tabName
            return this
        }

        // DES: 设置选中和未选中的字体颜色，支持Color.BLACK、R.color.black或者#ffffff等类型
        fun setFontColor(selectedFontColor: Any?, unselectedFontColor: Any?): TabItemBuilder {
            model.selectedFontColor = selectedFontColor
            model.unselectedFontColor = unselectedFontColor
            return this
        }

        // DES: 设置字体大小
        fun setFontSize(fontSize: Float): TabItemBuilder {
            model.fontSize = fontSize
            return this
        }

        // DES: 设置是否透出，以及透出图标距离底部的距离
        fun setGiveModel(isGive: Boolean, giveSize: Float): TabItemBuilder {
            model.isGive = isGive
            model.giveSize = giveSize
            return this
        }

        // DES: 设置自己实现的TabItemView业务部分
        fun setTabView(tabView: TabView): TabItemBuilder {
            mTabView = tabView
            return this
        }
        
        // DES: 图标加载器
        fun setIconLoader(iconLoader: IconLoader): TabItemBuilder {
            mIconLoader = iconLoader
            return this
        }

        // DES: 设置你的其他数据信息
        fun setYourData(yourData: Any?): TabItemBuilder {
            model.yourData = yourData
            return this
        }

        // DES: 开始构建
        fun build(context: Context, index: Int = INVALID_POSITION): TabItemView {
            val tabView = (mTabView ?: BottomTabBar.DefaultTabView()).apply {
                iconLoader = mIconLoader
                position = index
            }
            return TabItemView(context, tabView, model)
        }
    }

    // DES: 数据模型
    data class TabItemModel(
        // DES: 选中图标
        var selectedIcon: Any? = null,
        // DES: 未选中图标
        var unselectedIcon: Any? = null,
        // DES: 图标的宽度，默认是ViewGroup.LayoutParams.WRAP_CONTENT
        var iconWidth: Int = ViewGroup.LayoutParams.WRAP_CONTENT,
        // DES: 图标的高度，默认是ViewGroup.LayoutParams.WRAP_CONTENT
        var iconHeight: Int = iconWidth,
        // DES: 图标到tab名字的间隔大小
        var iconToNameInterval: Int = 0,
        // DES: tab的名字
        var tabName: CharSequence? = null,
        // DES: 选中字体颜色, 默认是Color.GRAY
        var selectedFontColor: Any? = null,
        // DES: 为选中字体颜色, 默认是Color.BLACK
        var unselectedFontColor: Any? = null,
        // DES: 字体大小, 默认是12f
        var fontSize: Float = 12f,
        // DES: 用于判断是否需要透出，否认是不需要
        var isGive: Boolean = false,
        // DES: 图标距离底部的透出大小，默认是0f,
        var giveSize: Float = 0f,
        // DES: 你的数据其他数据
        var yourData: Any? = null
    )
}