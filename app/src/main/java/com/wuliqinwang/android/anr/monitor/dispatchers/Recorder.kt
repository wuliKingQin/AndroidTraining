package com.wuliqinwang.android.anr.monitor.dispatchers

import com.wuliqinwang.android.anr.monitor.cache.LruRecorder
import com.wuliqinwang.android.anr.monitor.cache.Record

// 分发记录器
data class Recorder(
    // 消息类型
    var what: Int = 0,
    // 消息对应的处理器
    var handler: String? = null,
    // 分发开始时间
    var dispatchStartTime: Long = 0L,
    // 分发结束时间，也是消息队列空闲的开始时间
    var idleStartTime: Long = 0L,
    // 记录Id
    var recordId: Int = INVALID_ID
) {
    companion object {
        // 无效Id
        const val INVALID_ID = -1
    }
    // 计算分发耗时时间
    fun calDispatchConsuming() = idleStartTime - dispatchStartTime

    // 计算消息队列空闲的耗时时间
    fun calIdleConsuming() = dispatchStartTime - idleStartTime

    // 生成记录Id, 如果记录Id小于等于0才进行生成
    private fun produceRecordId() {
        if (recordId < 0) {
            recordId = produceId()
        }
    }
    // 生成新的记录唯一Id
    fun produceId(): Int = Dispatcher.idGenerator.incrementAndGet()

    // 更新记录信息
    fun dispatchStart(what: Int, handler: String?) {
        produceRecordId()
        dispatchStartTime = System.currentTimeMillis()
        this.what = what
        this.handler = handler
    }

    // 调度完成
    fun dispatchFinish() {
        idleStartTime = System.currentTimeMillis()
    }

    // 重置记录Id
    fun resetRecordId() {
        recordId = INVALID_ID
    }

    // 创建一个新的记录对象并保存到缓存中
    @JvmOverloads
    inline fun buildRecord(recordId: Int= INVALID_ID, action: Record.()-> Unit = {}): Record{
        return Record(if (recordId != INVALID_ID) recordId else produceId()).apply {
            action()
            LruRecorder.putRecord(this)
        }
    }
}

