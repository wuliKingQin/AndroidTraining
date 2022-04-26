package com.utopia.anr.water.impls.interceptors

import com.utopia.anr.water.cache.Record
import com.utopia.anr.water.dispatchers.Interceptor
import com.utopia.anr.water.dispatchers.Recorder

// 将记录信息进行聚合的拦截
class AggregationInterceptor(
    private var cumulativeThreshold: Long
): Interceptor {

    override fun onIntercepted(next: Interceptor.Chain): Record {
        val recorder = next.getRecorder()
        val record = next.process(recorder)
        if (recorder.recordId != Recorder.INVALID_ID) {
            record.what = recorder.what
            record.handler = recorder.handler
            record.wall += recorder.calDispatchConsuming()
            record.count += 1
            if (recorder.isResetRecordId(record, cumulativeThreshold)) {
                recorder.resetRecordId()
            }
        }
        return record
    }
}