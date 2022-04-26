package com.utopia.anr.water.impls.interceptors

import com.utopia.anr.water.cache.LruRecorder
import com.utopia.anr.water.cache.Record
import com.utopia.anr.water.dispatchers.Interceptor

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