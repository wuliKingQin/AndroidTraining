package com.wuliqinwang.android.bitmap

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.viewbinding.ViewBinding
import com.wuliqinwang.act.register.annotation.ActRegister
import com.wuliqinwang.android.R
import com.wuliqinwang.android.base.AbstractListActivity
import com.wuliqinwang.android.databinding.RvBitmapOptimizationItemBinding

@ActRegister(name = "Bitmap优化相关", position = 10)
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
            viewBinding.iconIv.setImageBitmap(currentData)
        }
    }

    override fun onLoadListData(savedInstanceState: Bundle?) {
        for (index in 0 until 100) {
            addListData(
                BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher),
                isRefresh = true
            )
        }
    }
}