package com.wuliqinwang.android.bitmap

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.wuliqinwang.act.register.annotation.ActRegister
import com.wuliqinwang.android.R
import com.wuliqinwang.android.common_lib.base.AbstractListActivity
import com.wuliqinwang.android.databinding.RvBitmapOptimizationItemBinding

@ActRegister(name = "Bitmap优化相关", position = 3)
class BitmapOptimizationActivity: AbstractListActivity<Bitmap>(){

    override fun createItemViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup,
        viewType: Int
    ): ViewBinding = RvBitmapOptimizationItemBinding
        .inflate(inflater, parent, false)

    override fun onBindDataForItemView(
        viewBinding: ViewBinding,
        currentData: Bitmap,
        position: Int
    ) {
        if (viewBinding is RvBitmapOptimizationItemBinding) {
            Log.d("test===", "bitmap size=${currentData.width * currentData.height * currentData.config.ordinal}")
            viewBinding.iconIv.setImageBitmap(currentData)
        }
    }

    override fun onLoadListData(savedInstanceState: Bundle?) {
        for (index in 0 until 100) {
            addListData(
                BitmapUtils.getBitmap(this, R.mipmap.ic_launcher, 20, 20),
                isRefresh = true
            )
        }
    }
}