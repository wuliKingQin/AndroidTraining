package com.utopia.anr.water.checktime

import com.utopia.anr.water.stack.StackGetter

// 默认实现的超时检查器
// todo 这个地方目前还是比较耗性能的，采用子线程创建Looper来实现的延迟执行，
//  由于主线程消息比较多，这里可能会造成GC严重。 这里可以想办法进行优化
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