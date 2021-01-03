package com.wuliqinwang.android.bitmap

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory

// 用于压缩图片的工具类
object BitmapUtils {

    fun getBitmap(context: Context, drawableResId: Int, maxWidth: Int, maxHeight: Int): Bitmap {
        val resource = context.resources
        val options = BitmapFactory.Options()
        // 表示在解析图片的时候只解析outXX参数
        options.inJustDecodeBounds = true
        BitmapFactory.decodeResource(resource, drawableResId, options)
        val w = options.outWidth
        val h = options.outHeight
        options.inSampleSize = getBitmapSampleSize(w, h, maxWidth, maxHeight)
        // 表示解析所有的信息
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeResource(resource, drawableResId, options)
    }

    private fun getBitmapSampleSize(w: Int, h: Int, maxWidth: Int, maxHeight: Int): Int {
        var inSampleSize = 1
        if(w > maxWidth && h > maxHeight) {
            inSampleSize = 2
            while (w / inSampleSize > maxWidth && h / inSampleSize > maxHeight) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}