package com.wuliqinwang.android.anr.monitor

// 抽象一个监控的接口
interface Monitor {
    // 在该方法中开始监控的逻辑
    fun startMonitor()
}