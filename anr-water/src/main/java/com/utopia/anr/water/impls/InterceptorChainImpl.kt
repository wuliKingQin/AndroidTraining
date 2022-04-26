package com.utopia.anr.water.impls

import com.utopia.anr.water.cache.Record
import com.utopia.anr.water.dispatchers.Interceptor
import com.utopia.anr.water.dispatchers.Recorder
import java.lang.AssertionError

// 拦截器链的默认实现类
class InterceptorChainImpl(
    private var interceptors: ArrayList<Interceptor>,
    private var recorder: Recorder,
    private var index: Int = 0,
    private var nextIndex: Int = 0
) : Interceptor.Chain {

    override fun resetIndex() {
        index = 0
        nextIndex = 0
    }

    override fun processBefore(recorder: Recorder) {
        traversalUp()
        val interceptor = interceptors[index]
        interceptor.interceptedBeforeInner(this)
    }

    override fun process(recorder: Recorder): Record {
        traversalUp()
        val interceptor = interceptors[index]
        return interceptor.interceptedInner(this)
    }

    private fun traversalUp() {
        index = nextIndex
        if (index >= interceptors.size) {
            throw AssertionError("out of bound of array index: $index")
        }
        nextIndex = index + 1
    }

    override fun getRecorder(): Recorder {
        return recorder
    }

    override fun isTraverseOver(): Boolean {
        return nextIndex >= interceptors.size
    }
}