package com.wuliqinwang.android.progress

import android.content.Context
import android.content.res.Resources
import android.graphics.*
import android.text.SpannableString
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View

/**
 * des: 抽象绘制接口
 * author 秦王
 * time 2022/4/1 14:08
 */
interface Draw {
    /**
     * des:  绘制方法
     * time: 2022/4/1 14:09
     */
    fun onDraw(canvas: Canvas?)
}

/**
 * des: 抽象进度接口
 * author 秦王
 * time 2022/4/1 14:08
 */
interface Progress : Draw {
    /**
     * des: 获取进度百分比
     * time: 2022/4/1 16:32
     */
    fun getProgressRate(): Float
    /**
     * des: 获取进度路径测量对象
     * time: 2022/4/1 16:32
     */
    fun getProgressPathMeasure(): PathMeasure
    /**
     * des: 获取进度高度
     * time: 2022/4/1 16:33
     */
    fun getProgressHeight(): Int
    /**
     * des: 获取资源对象
     * time: 2022/4/1 16:33
     */
    fun getResources(): Resources
    /**
     * des: 获取dp转px值
     * time: 2022/4/1 16:33
     */
    fun getAdapterSize(size: Int): Float
    /**
     * des: 获取当前进度
     * time: 2022/4/1 16:34
     */
    fun getCurrProgress(): Int

    /**
     * des: 获取最大进度
     * time: 2022/4/1 16:34
     */
    fun getMaxProgress(): Int
}

/**
 * des: 带有气泡和底部标尺的进度
 * time: 2022/4/1 15:52
 */
interface BubbleAndScaleProgress : Progress {

    // doc: 气泡
    val bubble: Draw?

    // doc: 标尺
    val underScale: Draw?

    // doc: 右边图标
    val rightIcon: Draw?

    override fun onDraw(canvas: Canvas?) {
        rightIcon?.onDraw(canvas)
        bubble?.onDraw(canvas)
        underScale?.onDraw(canvas)
    }
}

/**
 * des: 气泡进度视图
 * author 秦王
 * time 2022/4/1 16:34
 */
class BubbleProgressView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    style: Int = 0
) : View(context, attrs, style), BubbleAndScaleProgress {

    // doc: 进度百分比
    private var mProgressRate = 0f

    // doc: 当前进度值
    private var mProgress = 0

    // doc: 进度最大值
    private var mMaxProgress = 0

    // doc: 进度最大值
    private var mProgressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
        setStyle(Paint.Style.STROKE)
        strokeWidth = getProgressHeight().toFloat()
    }

    // doc: 进度值的绘制路径
    private var mPath = Path()

    // doc: 进度背景的绘制路径
    private var mPathBg = Path()

    // doc: 路径长度测量
    private var mPathMeasure = PathMeasure()

    // doc: 进度背景颜色
    private var mProgressBgColor = Color.parseColor("#1AFF4335")

    // doc: 进度颜色
    private var mProgressColor = Color.parseColor("#FF4335")

    // doc: 气泡文本
    var bubbleText: CharSequence? = ""
        set(value) {
            field = value
            bubble.text = value
            invalidate()
        }

    // doc: 右边图标资源Id
    var rightIconRes = 0
        set(value) {
            field = value
            rightIcon.icon = try {
                BitmapFactory.decodeResource(resources, value)
            } catch (e: Exception) {
                null
            }
            invalidate()
        }

    override val bubble by lazy {
        Bubble(this, bubbleText)
    }

    override val underScale by lazy {
        UnderScale(this)
    }

    override val rightIcon by lazy {
        RightIcon(this)
    }

    // doc: 默认控件的宽度
    private val mDefaultWidth by lazy {
        getAdapterSize(250).toInt()
    }

    // doc: 默认控件的高度
    private val mDefaultHeight by lazy {
        getAdapterSize(70).toInt()
    }

    // doc: 进度内边距
    private val mInnerMargin by lazy {
        getAdapterSize(30)
    }

    // doc: 有气泡需要将进度整体下移
    private val mHasBubbleOffset by lazy {
        getAdapterSize(10).toInt()
    }

    override fun onDraw(canvas: Canvas?) {
        if (!bubbleText.isNullOrEmpty()) {
            measureProgressPath(measuredWidth, measuredHeight + mHasBubbleOffset)
        }
        super<BubbleAndScaleProgress>.onDraw(canvas)
        canvas ?: return
        drawProgressColor(canvas)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val tempWidth = measureDimension(mDefaultWidth, widthMeasureSpec)
        val tempHeight = measureDimension(mDefaultHeight, heightMeasureSpec)
        setMeasuredDimension(tempWidth, tempHeight)
    }

    /**
     * des: 适配控件的宽高方法
     * time: 2022/4/1 16:46
     */
    private fun measureDimension(defaultSize: Int, measureSpec: Int): Int {
        val mode = MeasureSpec.getMode(measureSpec)
        val specSize = MeasureSpec.getSize(measureSpec)
        return when(mode) {
            MeasureSpec.EXACTLY -> defaultSize.coerceAtLeast(specSize)
            MeasureSpec.AT_MOST -> defaultSize.coerceAtMost(specSize)
            else -> defaultSize
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        measureProgressPath(w, h)
        invalidate()
    }

    private fun measureProgressPath(width: Int, height: Int) {
        val progressHeight = (height.shr(1)).toFloat()
        mPathBg.reset()
        mPathBg.moveTo(mInnerMargin, progressHeight)
        mPathBg.lineTo(width - mInnerMargin, progressHeight)
        mPathMeasure.setPath(mPathBg, false)
    }

    private fun drawProgressColor(canvas: Canvas) {
        mPath.reset()
        // doc: 绘制进度背景
        mProgressPaint.color = mProgressBgColor
        canvas.drawPath(mPathBg, mProgressPaint)
        // doc: 绘制当前进度, 得到进度条路径
        val stop = mPathMeasure.length * mProgressRate
        mPathMeasure.getSegment(0f, stop, mPath, true)
        mProgressPaint.color = mProgressColor
        canvas.drawPath(mPath, mProgressPaint)
    }

    /**
     * des: 设置进度和最大进度值
     * time: 2022/3/31 17:55
     */
    fun setProgress(progress: Int, maxProgress: Int) {
        if (maxProgress <= 0) {
            return
        }
        mProgress = progress
        mMaxProgress = maxProgress
        mProgressRate = progress.toFloat().div(maxProgress)
        invalidate()
    }

    override fun getProgressRate(): Float {
        return mProgressRate
    }

    override fun getProgressPathMeasure(): PathMeasure {
        return mPathMeasure
    }

    override fun getProgressHeight(): Int {
        return getAdapterSize(4).toInt()
    }

    override fun getAdapterSize(size: Int): Float {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            size.toFloat(), resources.displayMetrics
        )
    }

    override fun getCurrProgress(): Int {
        return mProgress
    }

    override fun getMaxProgress(): Int {
        return mMaxProgress
    }

    /**
     * des: 设置进度底部的最小值最大值刻度单位字符串
     * time: 2022/4/1 15:51
     */
    fun setProgressBottomUnit(unit: String?) {
        underScale.unit = unit ?: ""
        invalidate()
    }

    /**
     * des:  气泡配置方法
     * time: 2022/4/1 13:42
     */
    fun setBubbleConfig(
        bgColor: Int = -1,
        textColor: Int = -1,
        textSize: Int = -1,
        virtualMargin: Int = -1,
        horizontalMargin: Int = -1
    ) {
        if (bgColor != -1) {
            bubble.backgroundColor = bgColor
        }
        if (textColor != -1) {
            bubble.textColor = textColor
        }
        if (textSize != 1) {
            bubble.textSize = textSize
        }
        if (virtualMargin > 0) {
            bubble.virtualMargin = virtualMargin
        }
        if (horizontalMargin > 0) {
            bubble.horizontalMargin = horizontalMargin
        }
        invalidate()
    }

    /**
     * des: 封装气泡类
     * author 秦王
     * time 2022/4/1 13:42
     */
    class Bubble(
        private var progress: Progress,
        var text: CharSequence? = null
    ) : Draw {
        var horizontalMargin = 0
        var virtualMargin = progress.getAdapterSize(8).toInt()
        var backgroundColor = Color.parseColor("#FFEDEB")
            set(value) {
                field = value
                mPaint.color = value
            }
        var textColor = Color.parseColor("#FF4335")
            set(value) {
                field = value
                mTextPaint.color = value
            }
        var textSize = 10
            set(value) {
                field = value
                mTextPaint.textSize = progress.getAdapterSize(value)
            }

        // doc: 显示区域
        private val mDisplayRect = Rect()
        private val mPath = Path()
        private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = backgroundColor
            strokeCap = Paint.Cap.ROUND
            style = Paint.Style.FILL
            strokeJoin = Paint.Join.ROUND
        }
        private val mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            strokeWidth = 1f
            style = Paint.Style.FILL
            color = textColor
            textSize = progress.getAdapterSize(this@Bubble.textSize)
            textAlign = Paint.Align.CENTER
        }
        private val mFontMetrics = mTextPaint.fontMetrics
        private val mPoint = FloatArray(2)
        private val mTPoint = FloatArray(2)

        private val mTriangleHeight = progress.getAdapterSize(6).toInt()
        private val mTriangleWidth = progress.getAdapterSize(10).toInt()
        private val mTextRectF = RectF()
        private var mWidth = 0
        private var mHeight = 0
        private var mRoundRadius = 0

        private val mSingleTextWidth by lazy {
            mTextPaint.getTextBounds("中", 0, 1, mDisplayRect)
            mDisplayRect.width()
        }

        override fun onDraw(canvas: Canvas?) {
            var content = text ?: return
            if (content.isEmpty()) {
                return
            }
            val progressMaxLength = progress.getProgressPathMeasure().length
            content = textWrap(content, progressMaxLength)
            mWidth = mDisplayRect.width()
            mHeight = mDisplayRect.height() + virtualMargin
            mRoundRadius = (mHeight).shr(1)
            mPath.reset()
            progress.getProgressPathMeasure()
                .getPosTan(progressMaxLength * progress.getProgressRate(), mPoint, mTPoint)
            mPath.apply {
                moveTo(mPoint[0], mPoint[1] - progress.getProgressHeight())
                lineTo(
                    mPoint[0] + mTriangleWidth,
                    mPoint[1] - mTriangleHeight - progress.getProgressHeight()
                )
                lineTo(
                    mPoint[0] - mTriangleWidth,
                    mPoint[1] - mTriangleHeight - progress.getProgressHeight()
                )
                close()
            }
            val textLeft = if (progress.getProgressRate() == 0f) {
                // 进度为0的时候，需要剪掉下面三角宽度，不然会导致三角绘制到了气泡半圆外
                mPoint[0] - mTriangleWidth - mRoundRadius
            } else {
                // 进度不为0时, 可以省掉三角宽度
                mPoint[0] - mRoundRadius - progress.getProgressRate() * mWidth
            }
            val textTop = mPoint[1] - mTriangleHeight - progress.getProgressHeight() - mHeight
            val textRight = if (progress.getProgressRate() == 1f) {
                mPoint[0] + mRoundRadius + mTriangleWidth
            } else {
                mPoint[0] + mRoundRadius + (1 - progress.getProgressRate()) * mWidth + horizontalMargin
            }
            mTextRectF.set(textLeft, textTop, textRight, textTop + mHeight)
            mPath.addRoundRect(mTextRectF, mHeight.toFloat(), mHeight.toFloat(), Path.Direction.CW)
            canvas?.drawPath(mPath, mPaint)
            val textOffset = (mFontMetrics.bottom - mFontMetrics.ascent) / 2 - mFontMetrics.bottom
            canvas?.drawText(
                content.toString(),
                mTextRectF.centerX(),
                mTextRectF.centerY() + textOffset,
                mTextPaint
            )
        }

        /**
         * des: 用于文本自适应气泡，使其不超过进度最大宽度
         * time: 2022/4/1 14:06
         */
        private fun textWrap(content: CharSequence, progressMaxLength: Float): CharSequence {
            mTextPaint.getTextBounds(content.toString(), 0, content.length, mDisplayRect)
            return if ((mDisplayRect.width() + horizontalMargin) > progressMaxLength) {
                val wrapSize = (progressMaxLength - mSingleTextWidth.shl(2)) / mSingleTextWidth
                val text = content.substring(0, wrapSize.toInt()).plus("...")
                textWrap(text, progressMaxLength)
            } else {
                content
            }
        }
    }

    /**
     * des: 用来封装绘制进度下面的最小值，最大值，当前值的刻度
     * time: 2022/4/1 14:19
     */
    class UnderScale(
        private var progress: Progress,
        var unit: String = "人"
    ) : Draw {

        var textColor = Color.parseColor("#666666")
            set(value) {
                field = value
                mTextPaint.color = value
            }
        var textSize = progress.getAdapterSize(12)
            set(value) {
                field = value
                mTextPaint.textSize = value
            }

        // doc: 初始化画笔
        private val mTextPaint by lazy {
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                strokeWidth = 1f
                style = Paint.Style.FILL
                color = textColor
                textSize = this@UnderScale.textSize
            }
        }

        // doc: 显示区域
        private val mDisplayRect = Rect()

        private val mPoint = FloatArray(2)
        private val mTPoint = FloatArray(2)

        private val mProgressMaxLength = progress.getProgressPathMeasure().length

        private var mMinProgressTextLeft = 0f
        private var mMinProgressTextRight = 0f
        private var mMaxProgressTextLeft = 0f
        private var mMaxProgressTextRight = 0f

        override fun onDraw(canvas: Canvas?) {
            canvas ?: return
            drawMinProgressText(canvas)
            drawMaxProgressText(canvas)
            drawCurrProgressText(canvas)
        }

        private fun drawMinProgressText(canvas: Canvas) {
            val minProgressText = "0$unit"
            mTextPaint.getTextBounds(minProgressText, 0, minProgressText.length, mDisplayRect)
            progress.getProgressPathMeasure()
                .getPosTan(0f, mPoint, mTPoint)
            mMinProgressTextLeft = mPoint[0]
            mMinProgressTextRight = mMinProgressTextLeft + mDisplayRect.width()
            canvas.drawProgressText(minProgressText, mMinProgressTextLeft)
        }

        private fun drawMaxProgressText(canvas: Canvas) {
            val maxProgressText = "${progress.getMaxProgress()}$unit"
            mTextPaint.getTextBounds(maxProgressText, 0, maxProgressText.length, mDisplayRect)
            progress.getProgressPathMeasure().getPosTan(mProgressMaxLength, mPoint, mTPoint)
            mMaxProgressTextLeft = mPoint[0] - mDisplayRect.width()
            mMaxProgressTextRight = mPoint[0]
            canvas.drawProgressText(maxProgressText, mMaxProgressTextLeft)
        }

        private fun drawCurrProgressText(canvas: Canvas) {
            val currProgressText = "${progress.getCurrProgress()}$unit"
            mTextPaint.getTextBounds(currProgressText, 0, currProgressText.length, mDisplayRect)
            progress.getProgressPathMeasure()
                .getPosTan(mProgressMaxLength * progress.getProgressRate(), mPoint, mTPoint)
            var currPointX = mPoint[0]
            currPointX = when {
                currPointX <= mMinProgressTextLeft || currPointX >= mMaxProgressTextRight -> -1f
                currPointX > mMinProgressTextLeft && currPointX <= mMinProgressTextRight -> {
                    mMinProgressTextRight + mDisplayRect.width().shr(1) + progress.getAdapterSize(5)
                }
                currPointX >= mMaxProgressTextLeft && currPointX < mMaxProgressTextRight -> {
                    mMaxProgressTextLeft - mDisplayRect.width().shr(1) - progress.getAdapterSize(5)
                }
                else -> mPoint[0]
            }
            if (currPointX != -1f) {
                canvas.drawProgressText(currProgressText, currPointX, Paint.Align.CENTER)
            }
        }

        private fun Canvas.drawProgressText(
            text: String,
            textX: Float,
            textAlign: Paint.Align = Paint.Align.LEFT
        ) {
            mTextPaint.textAlign = textAlign
            val textY = mPoint[1] + progress.getAdapterSize(9) + mDisplayRect.height()
            drawText(text, textX, textY, mTextPaint)
        }
    }

    /**
     * des: 右边图标
     * author 秦王
     * time 2022/4/1 16:18
     */
    class RightIcon(
        private var progress: Progress,
        var icon: Bitmap? = null
    ) : Draw {

        private val mPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        private val mPoint = FloatArray(2)
        private val mTPoint = FloatArray(2)

        override fun onDraw(canvas: Canvas?) {
            canvas ?: return
            val tempIcon = icon ?: return
            val progressMaxLength = progress.getProgressPathMeasure().length
            progress.getProgressPathMeasure()
                .getPosTan(progressMaxLength, mPoint, mTPoint)
            val top = mPoint[1] - tempIcon.height.shr(1)
            canvas.drawBitmap(tempIcon, mPoint[0] + progress.getAdapterSize(5), top, mPaint)
        }
    }
}