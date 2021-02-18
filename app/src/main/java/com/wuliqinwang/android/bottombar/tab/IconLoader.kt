package com.wuliqinwang.android.bottombar.tab

import android.widget.ImageView

interface IconLoader {
    fun onLoading(imageView: ImageView, iconModel: Any?)
}