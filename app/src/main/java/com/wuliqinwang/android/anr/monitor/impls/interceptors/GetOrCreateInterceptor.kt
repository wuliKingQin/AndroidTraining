package com.wuliqinwang.android.anr.monitor.impls.interceptors

import com.wuliqinwang.android.anr.monitor.cache.LruRecorder
import com.wuliqinwang.android.anr.monitor.cache.Record
import com.wuliqinwang.android.anr.monitor.dispatchers.Interceptor

// 获取或者创建消息记录的拦截器
class GetOrCreateInterceptor: Interceptor{
    override fun onIntercepted(next: Interceptor.Chain): Record {
        val recorder = next.getRecorder()
        return LruRecorder.getRecord(recorder.recordId)
            ?: recorder.buildRecord(recorder.recordId) {
                what = next.getRecorder().what
                handler = next.getRecorder().handler
            }
    }
}