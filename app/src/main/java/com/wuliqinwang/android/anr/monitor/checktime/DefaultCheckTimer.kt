package com.wuliqinwang.android.anr.monitor.checktime

import com.wuliqinwang.android.anr.monitor.stack.StackGetter

// 默认实现的超时检查器
class DefaultCheckTimer: CheckTimer {

    companion object {
        // 用于标志分发的调度时间检查任务的关键值
        private const val DISPATCH_TIME_OUT_TASK = "dispatchTimeoutTask"
    }

    // 保存超时检查时间
    private var mTimeout: Long = 0L

    override fun startCheck(timeout: Long, stackGetter: StackGetter) {
        mTimeout = timeout
        checkTimeout(DISPATCH_TIME_OUT_TASK, timeout, stackGetter)
    }

    override fun cancelCheck() {
        cancelTimeout(DISPATCH_TIME_OUT_TASK, mTimeout)
    }
}