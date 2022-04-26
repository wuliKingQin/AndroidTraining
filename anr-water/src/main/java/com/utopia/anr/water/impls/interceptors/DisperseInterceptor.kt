package com.utopia.anr.water.impls.interceptors

import com.utopia.anr.water.cache.Record
import com.utopia.anr.water.dispatchers.Interceptor
import com.utopia.anr.water.dispatchers.Recorder

// 将一些特殊的记录进行单独记录的拦截器，比如说
// 消息队列空闲时间超过指定阈值和单个消息调度时间超过阈值的情况
class DisperseInterceptor(
    private val cumulativeThreshold: Long
) : Interceptor {

    override fun onInterceptedBefore(before: Interceptor.ProcessBefore) {
        val recorder = before.getRecorder()
        before.processBefore(recorder)
        if (recorder.idleStartTime > 0) {
            val idleConsuming = recorder.dispatchStartTime - recorder.idleStartTime
            if (idleConsuming >= cumulativeThreshold) {
                // 消息队列空闲时间超过阈值的情况
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
        if (recorder.recordId != Recorder.INVALID_ID) {
            if (recorder.isResetRecordId(record, cumulativeThreshold) &&
                record.wall > 0 && dispatchConsuming >= cumulativeThreshold) {
                // 判断单个消息调度耗时超过阈值的情况
                record.newBuilder()
                    .setId(recorder.produceId())
                    .setCount(1)
                    .setWall(dispatchConsuming)
                    .build()
                record.stackInfo = null
                recorder.resetRecordId()
            }
        }
        return record
    }
}