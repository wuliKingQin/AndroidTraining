package com.wuliqinwang.android.anr.monitor.impls

import android.os.Looper
import com.utopia.android.ulog.ULog
import com.wuliqinwang.android.anr.monitor.AbstractMonitor
import com.wuliqinwang.android.anr.monitor.cache.LruRecorder
import com.wuliqinwang.android.anr.monitor.cache.Record
import com.wuliqinwang.android.anr.monitor.checktime.DefaultCheckTimer
import com.wuliqinwang.android.anr.monitor.checktime.cancelTimeout
import com.wuliqinwang.android.anr.monitor.checktime.checkTimeout
import com.wuliqinwang.android.anr.monitor.config.Config
import com.wuliqinwang.android.anr.monitor.dispatchers.AbstractDispatcher
import com.wuliqinwang.android.anr.monitor.dispatchers.Dispatcher
import com.wuliqinwang.android.anr.monitor.dispatchers.Interceptor
import com.wuliqinwang.android.anr.monitor.dispatchers.Recorder
import com.wuliqinwang.android.anr.monitor.impls.interceptors.ActivityThreadHandlerInterceptor
import com.wuliqinwang.android.anr.monitor.impls.interceptors.AggregationInterceptor
import com.wuliqinwang.android.anr.monitor.impls.interceptors.DisperseInterceptor
import com.wuliqinwang.android.anr.monitor.impls.interceptors.GetOrCreateInterceptor
import com.wuliqinwang.android.anr.monitor.stack.DispatchStackGetter
import com.wuliqinwang.android.anr.monitor.what.What
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicInteger

// 实现一个消息调度的一个监控类
class MessageMonitor(
    looper: Looper,
    config: Config
) : AbstractMonitor(looper, config) {

    companion object {
        const val TAG = "MessageMonitor"
    }

    override fun startMonitor() {
        registerDispatcherToLooper()
    }

    // 用于注册分发调度器到主线程的Looper中
    private fun registerDispatcherToLooper() {
        if (dispatcher == null) {
            dispatcher = DefaultDispatcher(config)
        }
        dispatcher?.apply {
            looper.setMessageLogging(this)
        }
    }

    // 默认实现的调度器
    class DefaultDispatcher(
        private val config: Config
    ) : AbstractDispatcher() {

        // 消息分发记录器
        private val mRecorder by lazy(LazyThreadSafetyMode.NONE) {
            Recorder()
        }

        // 拦截器列表
        private val mInterceptors by lazy(LazyThreadSafetyMode.NONE) {
            ArrayList<Interceptor>()
        }

        // 超时检查器
        private val mCheckTimer by lazy {
            // 如果为空则走默认的超时检查器
            config.checkTimer ?: DefaultCheckTimer()
        }

        // 用于消息调度耗时到指定阈值的时候获取对应堆栈
        private val mDispatchStackGetter by lazy {
            DispatchStackGetter()
        }

        // 拦截器执行链
        private var mInterceptorChain: Interceptor.Chain? = null

        override fun onDispatching(what: Int, handler: String?) {
            mRecorder.dispatchStart(what, handler)
            executeInterceptors {
                processBefore(mRecorder)
            }
            mDispatchStackGetter.addId(mRecorder.recordId)
            // todo 这个地方目前还是比较好性能的，采用子线程创建Looper来实现的延迟执行，
            //  由于主线程消息比较多，这里可能会造成GC严重。 这里可以想办法进行优化
            mCheckTimer.startCheck(config.dispatchCheckTime, mDispatchStackGetter)
        }

        override fun onDispatched(what: Int, handler: String?) {
            mRecorder.dispatchFinish()
            mDispatchStackGetter.removeId()
            mCheckTimer.cancelCheck()
            executeInterceptors {
                process(mRecorder)
            }
        }

        // 执行拦截处理器
        private inline fun executeInterceptors(action: Interceptor.Chain.()-> Unit) {
            if (mInterceptorChain == null) {
                packInterceptors()
                mInterceptorChain = InterceptorChainImpl(mInterceptors, mRecorder)
                mInterceptorChain
            }
            val chain = mInterceptorChain ?: return
            chain.resetIndex()
            action(chain)
        }

        // 打包需要处理器拦截器
        private fun packInterceptors() {
            val interceptors = config.interceptors
            if (!interceptors.isNullOrEmpty()) {
                interceptors.forEach { interceptor ->
                    mInterceptors.add(interceptor)
                }
            }
            mInterceptors.add(AggregationInterceptor(config.cumulativeThreshold))
            mInterceptors.add(DisperseInterceptor(config.cumulativeThreshold))
            mInterceptors.add(ActivityThreadHandlerInterceptor())
            mInterceptors.add(GetOrCreateInterceptor())
        }
    }
}