package com.wuliqinwang.android.bottombar.tab

import android.widget.ImageView

// DES: 用于图标的加载接口
interface IconLoader {
    // DES: 处理加载图标的方法
    fun onLoading(position: Int, iconView: ImageView, iconModel: Any?)
}