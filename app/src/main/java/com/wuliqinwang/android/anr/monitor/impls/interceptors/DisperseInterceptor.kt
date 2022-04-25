package com.wuliqinwang.android.anr.monitor.impls.interceptors

import com.wuliqinwang.android.anr.monitor.cache.Record
import com.wuliqinwang.android.anr.monitor.dispatchers.Interceptor
import com.wuliqinwang.android.anr.monitor.dispatchers.Recorder

// 将一些特殊的记录进行单独记录的拦截器
class DisperseInterceptor(
    private val cumulativeThreshold: Long
): Interceptor {

    override fun onInterceptedBefore(before: Interceptor.ProcessBefore) {
        val recorder = before.getRecorder()
        before.processBefore(recorder)
        if (recorder.idleStartTime > 0) {
            val idleConsuming = recorder.dispatchStartTime - recorder.idleStartTime
            if (idleConsuming >= cumulativeThreshold) {
                recorder.buildRecord {
                    des = "idle记录"
                    wall = idleConsuming
                    count += 1
                    this.what = 0
                    this.handler = null
                }
            }
        }
    }

    override fun onIntercepted(next: Interceptor.Chain): Record {
        val recorder = next.getRecorder()
        val record = next.process(recorder)
        val dispatchConsuming = recorder.calDispatchConsuming()
        if (recorder.recordId != Recorder.INVALID_ID && record.wall + dispatchConsuming > cumulativeThreshold) {
            if (record.wall > 0 && dispatchConsuming >= cumulativeThreshold) {
                recorder.buildRecord {
                    what = recorder.what
                    handler = recorder.handler
                    count += 1
                    wall = dispatchConsuming
                }
            } else {
                statisticalRecord(recorder, record)
            }
            recorder.resetRecordId()
        }
        return record
    }

    // 统计耗时段的消息到一个记录里面
    private fun statisticalRecord(recorder: Recorder, record: Record) {
        record.apply {
            this.what = recorder.what
            this.handler = recorder.handler
            wall += recorder.calDispatchConsuming()
            count += 1
        }
    }
}