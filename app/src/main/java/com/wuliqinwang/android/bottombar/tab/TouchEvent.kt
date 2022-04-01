package com.wuliqinwang.android.bottombar.tab

import android.view.MotionEvent

interface TouchEvent {
    fun onTouchEvent(event: MotionEvent?): Boolean
    fun dispatchTouchEvent(event: MotionEvent?): Boolean
    fun onInterceptTouchEvent(event: MotionEvent?): Boolean
    fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean)
}