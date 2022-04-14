package com.wuliqinwang.android.anr.monitor.impls

import android.os.Looper
import com.utopia.android.ulog.ULog
import com.wuliqinwang.android.anr.monitor.AbstractMonitor
import com.wuliqinwang.android.anr.monitor.dispatchers.AbstractDispatcher
import com.wuliqinwang.android.anr.monitor.dispatchers.Dispatcher

// 实现一个消息调度的一个监控类
class MessageMonitor(
    looper: Looper
): AbstractMonitor(looper) {

    override var dispatcher: Dispatcher? = null

    override fun startMonitor() {
        if (dispatcher == null) {
            dispatcher = MessageDispatcher().apply {
                looper.setMessageLogging(this)
            }
        }
    }

    // 消息调度器的具体实现类
    class MessageDispatcher: AbstractDispatcher() {

        companion object {
            private const val TAG = "MessageDispatcher"
        }

        override fun onDispatching(what: Int, handler: String?) {
            ULog.d(TAG, "message what: $what handler: $handler")
        }

        override fun onDispatched(what: Int, handler: String?) {
        }
    }
}