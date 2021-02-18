package com.wuliqinwang.android.bottombar.tab

interface TouchEvent {
    fun onTouchEvent(event: TouchEvent): Boolean
    fun dispatchTouchEvent(event: TouchEvent): Boolean
    fun onInterceptTouchEvent(event: TouchEvent): Boolean
    fun requestDisallowInterceptTouchEvent(disallowIntercept: Boolean)
}