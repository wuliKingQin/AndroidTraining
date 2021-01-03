package com.wuliqinwang.android.bitmap

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.wuliqinwang.android.tryCatch
import java.io.InputStream

// 封装长图的加载视图
class LongImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultStyle: Int = 0
) : View(context, attrs, defaultStyle){

    // 用于显示裁剪区域
    private val mCutRect by lazy {
        Rect()
    }

    // 用于Bitmap加载的参数获取
    private val mOptions by lazy {
        BitmapFactory.Options()
    }
    // 保存图像解析器
    private var mBitmapDecoder: BitmapRegionDecoder? = null

    private var mImageWidth = 0
    private var mImageHeight = 0
    private var mViewWidth = 0
    private var mViewHeight = 0
    private var mScale = 1f
    private var mTempBitmap: Bitmap? = null

    // 设置长图的加载
    fun setImage(imageIs: InputStream) {
        mOptions.inJustDecodeBounds = true
        BitmapFactory.decodeStream(imageIs, null, mOptions)
        mImageWidth = mOptions.outWidth
        mImageHeight = mOptions.outHeight
        mOptions.inMutable = true
        mOptions.inPreferredConfig = Bitmap.Config.RGB_565
        mOptions.inJustDecodeBounds = false
        tryCatch { isError, _ ->
            if(!isError) {
                mBitmapDecoder = BitmapRegionDecoder.newInstance(imageIs, false)
                requestLayout()
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        mBitmapDecoder ?: return
        mViewWidth = measuredWidth
        mViewHeight = measuredHeight
        mScale = mViewWidth / mImageWidth.toFloat()
        mCutRect.apply {
            left = 0
            top = 0
            right = mImageWidth
            bottom = (mViewHeight / mScale).toInt()
        }
    }

    @SuppressLint("DrawAllocation")
    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        mBitmapDecoder?.apply {
            mOptions.inBitmap = mTempBitmap
            mTempBitmap = decodeRegion(mCutRect, mOptions)
            mTempBitmap?.apply {
                val matrix = Matrix()
                matrix.setScale(mScale, mScale)
                canvas?.drawBitmap(this, matrix, null)
            }
        }
    }
}