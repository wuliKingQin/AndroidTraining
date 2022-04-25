package com.wuliqinwang.android.anr.monitor.impls

import android.os.Looper
import com.wuliqinwang.android.anr.monitor.cache.LruRecorder
import com.wuliqinwang.android.anr.monitor.cache.Record
import java.util.concurrent.ConcurrentLinkedDeque

// 调度栈获取器，当发生超过指定阈值的时候执行
class DispatchStackGetter: Runnable {

    // 记录Id的队列，因为是异步的，防止获取堆栈的时候，
    // Id发生变化，对应消息不能获取到对应的调度栈
    private val mIdQueue by lazy {
        ConcurrentLinkedDeque<Int>()
    }

    // 用来判断是否正在执行超时获取堆栈信息，
    // 如果正在获取堆栈，则移除时暂时不要主动移除操作
    @Volatile
    private var isTimeout = false

    // 添加记录Id
    fun addId(id: Int) {
        if (mIdQueue.peekLast() != id) {
            isTimeout = false
            mIdQueue.offerLast(id)
        }
    }

    // 移除记录Id
    fun removeId() {
        if (mIdQueue.isEmpty()) {
            return
        }
        if (!isTimeout) {
            mIdQueue.removeLast()
        }
    }

    override fun run() {
        isTimeout = true
        if (mIdQueue.isEmpty()) {
            return
        }
        val id = mIdQueue.pollFirst() ?: return
        var record = LruRecorder.getRecord(id)
        if (record == null) {
            record = Record(id)
            LruRecorder.putRecord(record)
        }
        record.stackInfo = gainStackInfo()
    }

    // 获取主线程堆栈
    private fun gainStackInfo(): String {
        val stackBuilder = StringBuilder()
        Looper.getMainLooper().thread.stackTrace.forEachIndexed { index, stackTraceElement ->
            if (index != 0) {
                stackBuilder.append("\n")
            }
            stackBuilder.append(stackTraceElement.toString())
        }
        return stackBuilder.toString()
    }
}