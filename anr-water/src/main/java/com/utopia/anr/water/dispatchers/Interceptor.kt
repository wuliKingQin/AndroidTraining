package com.utopia.anr.water.dispatchers

import com.utopia.anr.water.cache.Record

// 分发拦截器
interface Interceptor {

    // 拦截器内部调用
    fun interceptedInner(next: Chain): Record {
        try {
            return onIntercepted(next)
        } finally {
        }
    }

    // 执行拦截前的内部调用
    fun interceptedBeforeInner(before: ProcessBefore) {
        try {
            onInterceptedBefore(before)
        } finally {
        }
    }

    // 子类可以重新该方法实现其他任务
    fun onInterceptedBefore(before: ProcessBefore) {
        if (!before.isTraverseOver()) {
            before.processBefore(before.getRecorder())
        }
    }

    // 拦截器处理方法
    fun onIntercepted(next: Chain): Record

    // 抽象处理前的接口
    interface ProcessBefore {

        // 执行处理前的操作, 在该方法中不能调用process方法
        fun processBefore(recorder: Recorder)

        // 返回记录器，提供给在拦截器中使用
        fun getRecorder(): Recorder

        // 用于判断是否遍历结束
        fun isTraverseOver(): Boolean
    }

    // 拦截器调用链抽象接口
    interface Chain: ProcessBefore {
        // 用于重置索引
        fun resetIndex()

        // 拦截器处理器方法
        fun process(recorder: Recorder): Record
    }
}