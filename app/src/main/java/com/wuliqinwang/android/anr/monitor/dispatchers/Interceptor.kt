package com.wuliqinwang.android.anr.monitor.dispatchers

import com.wuliqinwang.android.anr.monitor.cache.Record

// 分发拦截器
interface Interceptor {

    // 拦截器处理方法
    fun intercepted(next: Chain): Record

    // 拦截器调用链抽象接口
    interface Chain {
        // 用于重置索引
        fun resetIndex()

        // 拦截器处理器方法
        fun process(recorder: Recorder): Record

        // 返回记录器，提供给在拦截器中使用
        fun getRecorder(): Recorder
    }
}