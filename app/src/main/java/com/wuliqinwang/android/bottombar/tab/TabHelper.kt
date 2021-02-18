package com.wuliqinwang.android.bottombar.tab

import android.content.Context
import android.graphics.Color
import android.widget.ImageView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import java.lang.Exception

object TabHelper {

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

    // DES: 设置图标的状态
    fun setIconStatus(
        targetView: ImageView,
        selected: Boolean,
        selectedIcon: Any?,
        unselectedIcon: Any?,
        loader: IconLoader? = null
    ) {
        setIcon(
            targetView,
            if (selected) selectedIcon else unselectedIcon,
            loader
        )
    }

    // DES: 设置图标
    private fun setIcon(targetView: ImageView, icon: Any?, loader: IconLoader?) {
        if (loader != null) {
            loader.onLoading(targetView, icon)
        } else {
            loadIconWithGlide(targetView, icon)
        }
    }

    // DES: 导入网络图，改地方可以被重写
    private fun loadIconWithGlide(targetView: ImageView, iconModel: Any?) {
        Glide.with(targetView)
            .load(iconModel)
            .into(targetView)
    }
}