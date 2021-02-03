package com.wuliqinwang.android.bottombar

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import java.lang.Exception

/**
 * @Version: 1.0.0
 */
interface ITabView {

    // DES: 初始化方法
    fun initializer(model: TabItemView.TabItemModel, itemContainer: TabItemView)

    // DES: 获取字体颜色
    fun getFontColor(
        context: Context,
        // DES: 颜色值
        color: Any?,
        // DES: 默认选中颜色
        defaultColor: Int
    ): Int {
        return when (color) {
            is Int -> try {
                ContextCompat.getColor(context, color)
            } catch (e: Exception) {
                e.printStackTrace()
                defaultColor
            }
            is String -> try {
                Color.parseColor(color)
            } catch (e: Exception) {
                e.printStackTrace()
                defaultColor
            }
            else -> {
                println("color=${defaultColor}")
                defaultColor
            }
        }
    }

    // DES: 设置选中状态
    fun setSelectedStatus(selected: Boolean)

    // DES: 设置图标的状态
    fun setIconStatus(
        targetView: ImageView,
        selected: Boolean,
        selectedIcon: Any?,
        unselectedIcon: Any?
    ) {
        setIcon(targetView, if (selected) selectedIcon else unselectedIcon)
    }

    // DES: 设置图标
    private fun setIcon(targetView: ImageView, icon: Any?) {
        when (icon) {
            is Bitmap -> targetView.setImageBitmap(icon)
            is Drawable -> targetView.setImageDrawable(icon)
            is String -> loadNetworkIcon(targetView, icon)
            is Int -> targetView.setImageResource(icon)
        }
    }

    // DES: 导入网络图，改地方可以被重写
    fun loadNetworkIcon(targetView: ImageView, url: String) {
        Glide.with(targetView)
            .load(url)
            .into(targetView)
    }
}

// DES: 抽象出一个模板TabView
abstract class AbstractTabView: ITabView {
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
    fun isSelectedStatus(): Boolean = mItemContainer.isSelected

    // DES: 在该方法中创建自己的tab视图
    abstract fun createTabView(context: Context)

    // DES: 添加视图
    fun addView(child: View, params: ConstraintLayout.LayoutParams? = null) {
        if (params == null) {
            mItemContainer.addView(child)
        } else {
            mItemContainer.addView(child, params)
        }
        println("child size=${mItemContainer.childCount}")
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
        targetView.setTextColor(getFontColor(targetView.context, if(selected) {
            model.selectedFontColor
        } else {
            model.unselectedFontColor
        }, if (selected) defaultSelectedColor else defaultUnselectedColor))
    }
    
    // DES: 设置图标状态
    fun setIconStatus(targetView: ImageView, selected: Boolean) {
        setIconStatus(targetView, selected, model.selectedIcon, model.unselectedIcon)
    }

    // DES: 获取到上下文
    fun getContext() = mItemContainer.context
}