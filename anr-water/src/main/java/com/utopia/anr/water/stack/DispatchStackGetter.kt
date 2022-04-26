package com.utopia.anr.water.stack

import com.utopia.anr.water.cache.LruRecorder
import com.utopia.anr.water.cache.Record
import java.util.concurrent.ConcurrentLinkedDeque

// 消息调度栈获取器的实现类
class DispatchStackGetter: StackGetter {

    // 记录Id的队列，因为是异步的，防止获取堆栈的时候，
    // Id发生变化，对应消息不能获取到对应的调度栈
    private val mIdQueue by lazy {
        ConcurrentLinkedDeque<Int>()
    }

    // 用来判断是否正在执行超时获取堆栈信息，
    // 如果正在获取堆栈，则移除时暂时不要主动移除操作
    @Volatile
    private var isTimeout = false

    override fun addId(id: Int) {
        if (mIdQueue.peekLast() != id) {
            isTimeout = false
            mIdQueue.offerLast(id)
        }
    }

    override fun removeId() {
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
}