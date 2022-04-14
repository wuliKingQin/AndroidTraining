package com.wuliqinwang.android.anr.monitor

import android.os.Looper
import com.wuliqinwang.android.anr.monitor.dispatchers.Dispatcher

// 抽象一个监控器类，方便具体的监控器实现
abstract class AbstractMonitor(
    protected var looper: Looper
): Monitor {
    // 声明一个分发器，给子类自己实现
    protected abstract var dispatcher: Dispatcher?
}