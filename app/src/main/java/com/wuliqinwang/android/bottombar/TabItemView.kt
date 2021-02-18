package com.wuliqinwang.android.bottombar

import android.annotation.SuppressLint
import android.content.Context
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import com.wuliqinwang.android.bottombar.tab.TabView

@SuppressLint("ViewConstructor")
class TabItemView private constructor(
    context: Context,
    private var tabView: TabView,
    private var model: TabItemModel
): ConstraintLayout(context){

    companion object {
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

    override fun setSelected(selected: Boolean) {
        super.setSelected(selected)
        tabView.setSelected(isSelected)
    }

    // TabItemBuilder
    class TabItemBuilder(
        private var model: TabItemModel = TabItemModel()
    ) {

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

        // DES: 开始构建
        fun build(context: Context, tabView: TabView? = null): TabItemView {
            return TabItemView(context, tabView ?: BottomTabBar.DefaultTabItemView(), model)
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
        var giveSize: Float = 0f
    )
}