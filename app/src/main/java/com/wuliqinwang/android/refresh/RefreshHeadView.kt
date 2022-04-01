package com.wuliqinwang.android.refresh

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import com.bumptech.glide.Glide
import com.scwang.smartrefresh.layout.api.RefreshHeader
import com.scwang.smartrefresh.layout.api.RefreshKernel
import com.scwang.smartrefresh.layout.api.RefreshLayout
import com.scwang.smartrefresh.layout.constant.RefreshState
import com.scwang.smartrefresh.layout.constant.SpinnerStyle
import com.wuliqinwang.android.R
import kotlinx.android.synthetic.main.view_refresh_head.view.*

class RefreshHeadView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultStyle: Int = 0
): ConstraintLayout(context, attrs, defaultStyle), RefreshHeader {

    private var mRefreshKernel: RefreshKernel? = null

    init {
        View.inflate(context, R.layout.view_refresh_head, this)
    }

    override fun onStateChanged(
        refreshLayout: RefreshLayout,
        oldState: RefreshState,
        newState: RefreshState
    ) {
        when(newState) {
            RefreshState.Refreshing -> {
                Glide.with(this)
                    .load(R.mipmap.ic_refresh_head2)
                    .into(refreshIcon)
            }
            RefreshState.RefreshReleased -> {

            }
            RefreshState.None -> {
                refreshIcon.setImageResource(0)
            }
            else -> {
            }
        }
    }

    override fun getView(): View {
        return this
    }

    override fun getSpinnerStyle(): SpinnerStyle {
        return SpinnerStyle.Translate
    }

    override fun setPrimaryColors(vararg colors: Int) {
    }

    override fun onInitialized(kernel: RefreshKernel, height: Int, maxDragHeight: Int) {
        mRefreshKernel = kernel
        kernel.refreshLayout.refreshHeader?.apply {
            kernel.requestDrawBackgroundFor(this, Color.WHITE)
        }
    }

    override fun onMoving(
        isDragging: Boolean,
        percent: Float,
        offset: Int,
        height: Int,
        maxDragHeight: Int
    ) {
    }

    override fun onReleased(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {
        println("RefreshHeadView onReleased===========")
    }

    override fun onStartAnimator(refreshLayout: RefreshLayout, height: Int, maxDragHeight: Int) {
        println("RefreshHeadView onStartAnimator===========")
    }

    override fun onFinish(refreshLayout: RefreshLayout, success: Boolean): Int {
        if (success) {
            refreshIcon.setImageResource(R.mipmap.ic_launcher)
        } else {
            refreshIcon.setImageResource(R.mipmap.icon_bar_infant_mom_check)
        }
        return 500
    }

    override fun onHorizontalDrag(percentX: Float, offsetX: Int, offsetMax: Int) {
    }

    override fun isSupportHorizontalDrag(): Boolean {
        println("RefreshHeadView isSupportHorizontalDrag===========")
        return false
    }
}