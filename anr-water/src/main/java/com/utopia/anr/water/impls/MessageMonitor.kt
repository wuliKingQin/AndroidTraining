package com.utopia.anr.water.impls

import android.os.Looper
import com.utopia.anr.water.monitor.AbstractMonitor
import com.utopia.anr.water.checktime.DefaultCheckTimer
import com.utopia.anr.water.config.Config
import com.utopia.anr.water.dispatchers.AbstractDispatcher
import com.utopia.anr.water.dispatchers.Interceptor
import com.utopia.anr.water.dispatchers.Recorder
import com.utopia.anr.water.impls.interceptors.ActivityThreadHandlerInterceptor
import com.utopia.anr.water.impls.interceptors.AggregationInterceptor
import com.utopia.anr.water.impls.interceptors.DisperseInterceptor
import com.utopia.anr.water.impls.interceptors.GetOrCreateInterceptor
import com.utopia.anr.water.stack.DispatchStackGetter

// 实现一个消息调度的一个监控类
class MessageMonitor(
    looper: Looper,
    config: Config
) : AbstractMonitor(looper, config) {

    companion object {
        const val TAG = "MessageMonitor"
    }

    override fun startMonitor() {
        if (config.graySwitch || config.isDebug) {
            registerDispatcherToLooper()
        }
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