package com.wuliqinwang.android.anr.monitor.impls

import android.os.Looper
import com.utopia.android.ulog.ULog
import com.wuliqinwang.android.anr.monitor.AbstractMonitor
import com.wuliqinwang.android.anr.monitor.cache.LruRecorder
import com.wuliqinwang.android.anr.monitor.cache.Record
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

    override var dispatcher: Dispatcher? = null

    override fun startMonitor() {
        if (dispatcher == null) {
            dispatcher = DefaultDispatcher(config).apply {
                looper.setMessageLogging(this)
            }
        }
    }

    // 消息调度器的具体实现类
    class MessageDispatcher : AbstractDispatcher() {

        companion object {
            private const val TAG = "MessageDispatcher"

            // 记录Id生成器
            val recordIdGenerator by lazy {
                AtomicInteger(-1)
            }
            private const val DISPATCH_TIME_OUT_TASK = "dispatchTimeoutTask"
            private const val DISPATCH_TIME_OUT_TIME = 1000L
            private const val ACTIVITY_THREAD_HANDLER = "(android.app.ActivityThread\$H)"
        }

        private var mIdleStartTime = 0L

        // 保存当前的消息Id
        private var mRecordId = -1

        // 开始调度的时间
        private var mStartTime = 0L

        // 调度完成的消耗时间
        private var mConsumingTime = 0L

        // 用于保存当前生成的记录
        private var mCurRecord: Record? = null

        // Handler消息处理类型
        private val mWhats by lazy {
            arrayListOf(
                What.AT_HANDLER_WHAT_100,
                What.AT_HANDLER_WHAT_101,
                What.AT_HANDLER_WHAT_103,
                What.AT_HANDLER_WHAT_104,
                What.AT_HANDLER_WHAT_105,
                What.AT_HANDLER_WHAT_106,
                What.AT_HANDLER_WHAT_107,
                What.AT_HANDLER_WHAT_109,
                What.AT_HANDLER_WHAT_113,
                What.AT_HANDLER_WHAT_114,
                What.AT_HANDLER_WHAT_115,
                What.AT_HANDLER_WHAT_116,
                What.AT_HANDLER_WHAT_121,
                What.AT_HANDLER_WHAT_122,
                What.AT_HANDLER_WHAT_126,
                What.AT_HANDLER_WHAT_145,
                What.AT_HANDLER_WHAT_159,
                What.AT_HANDLER_WHAT_160
            )
        }

        private val mStackExecutor by lazy {
            DispatchStackExecutor()
        }

        override fun onDispatching(what: Int, handler: String?) {
            mStartTime = System.currentTimeMillis()
            if (mIdleStartTime > 0 && mStartTime - mIdleStartTime >= 300) {
                getOrCreateRecord(recordIdGenerator.incrementAndGet()).apply {
                    des = "idle记录"
                    wall = mStartTime - mIdleStartTime
                    count += 1
                    this.what = 0
                    this.handler = null
                }
            }
            if (mRecordId < 0) {
                mRecordId = recordIdGenerator.incrementAndGet()
            }
            mStackExecutor.addId(mRecordId)
            checkTimeout(DISPATCH_TIME_OUT_TASK, DISPATCH_TIME_OUT_TIME, mStackExecutor)
        }

        override fun onDispatched(what: Int, handler: String?) {
            mIdleStartTime = System.currentTimeMillis()
            mStackExecutor.removeId()
            mConsumingTime = mIdleStartTime - mStartTime
            ULog.d(TAG, "recordId: $mRecordId what: $what handler: $handler wall: $mConsumingTime")
            cancelTimeout(DISPATCH_TIME_OUT_TASK, DISPATCH_TIME_OUT_TIME)
            val record = mCurRecord ?: getOrCreateRecord()
            when {
                isActivityThreadHandler(what, handler) -> {
                    val whatRecord = if (record.count == 0 && record.wall == 0L) {
                        record
                    } else {
                        getOrCreateRecord(recordIdGenerator.incrementAndGet())
                    }
                    whatRecord.des = mWhats.firstOrNull { it.what == what }?.des ?: whatRecord.des
                    whatRecord.what = what
                    whatRecord.count += 1
                    whatRecord.wall = mConsumingTime
                    whatRecord.handler = handler
                    mRecordId = -1
                }
                record.wall + mConsumingTime > 300 -> {
                    if (record.wall > 0 && record.wall > 200 && (mConsumingTime - record.wall) >= 0) {
                        newSingleRecord(what, handler, record)
                    } else {
                        statisticalRecord(what, handler, record)
                    }
                    mRecordId = -1
                    ULog.d(
                        TAG,
                        "recordId: $mRecordId what: $what handler: $handler wall: ${record.wall} record size: ${LruRecorder.getRecordSize()}"
                    )
                }
                else -> statisticalRecord(what, handler, record)
            }
        }

        private fun isActivityThreadHandler(what: Int, handler: String?): Boolean {
            val isHasWhat = mWhats.any { it.what == what }
            return isHasWhat && handler == ACTIVITY_THREAD_HANDLER
        }

        // 根据记录Id得到或创建一个记录对象
        private fun getOrCreateRecord(recordId: Int = mRecordId): Record {
            return LruRecorder.getRecord(recordId) ?: LruRecorder.putRecord(
                Record(recordId, what = getWhat(), handler = handler)
            )
        }

        // 新增一个单独的记录对象
        private fun newSingleRecord(what: Int, handler: String?, record: Record) {
            val recordId = recordIdGenerator.incrementAndGet()
            LruRecorder.putRecord(Record(recordId).apply {
                this.what = what
                this.handler = handler
                this.stackInfo = record.stackInfo
                wall = mConsumingTime
                count += 1
            })
            record.stackInfo = null
        }

        // 统计耗时段的消息到一个记录里面
        private fun statisticalRecord(what: Int, handler: String?, record: Record) {
            record.apply {
                this.what = what
                this.handler = handler
                wall += mConsumingTime
                count += 1
            }
        }

        class DispatchStackExecutor : Runnable {

            private val mIdQueue by lazy {
                ConcurrentLinkedDeque<Int>()
            }

            @Volatile
            private var isTimeout = false

            fun addId(id: Int) {
                if (mIdQueue.peekLast() != id) {
                    isTimeout = false
                    mIdQueue.offerLast(id)
                }
            }

            // 移除Id
            fun removeId() {
                if (mIdQueue.isEmpty()) {
                    return
                }
                if (!isTimeout) {
                    mIdQueue.removeLast()
                }
            }

            override fun run() {
                isTimeout = true
                if (mIdQueue.isEmpty()) {
                    return
                }
                val id = mIdQueue.pollFirst() ?: return
                var record = LruRecorder.getRecord(id)
                if (record == null) {
                    record = Record(id)
                    LruRecorder.putRecord(record)
                }
                record.stackInfo = gainStackInfo()
            }

            // 获取主线程堆栈
            private fun gainStackInfo(): String {
                val stackBuilder = StringBuilder()
                Looper.getMainLooper().thread.stackTrace.forEachIndexed { index, stackTraceElement ->
                    if (index != 0) {
                        stackBuilder.append("\n")
                    }
                    stackBuilder.append(stackTraceElement.toString())
                }
                return stackBuilder.toString()
            }
        }
    }

    // 默认实现的调度器
    class DefaultDispatcher(
        private val config: Config
    ) : AbstractDispatcher() {

        companion object {
            // 用于标志分发的调度时间检查任务的任务关键值
            private const val DISPATCH_TIME_OUT_TASK = "dispatchTimeoutTask"
        }

        // 消息分发记录器
        private val mRecorder by lazy(LazyThreadSafetyMode.NONE) {
            Recorder()
        }

        // 拦截器
        private val mInterceptors by lazy(LazyThreadSafetyMode.NONE) {
            ArrayList<Interceptor>()
        }

        // 用于消息调度耗时到指定阈值的时候获取对应堆栈
        private val mStackGetter by lazy {
            DispatchStackGetter()
        }

        // 拦截器执行链
        private var mInterceptorChain: Interceptor.Chain? = null

        override fun onDispatching(what: Int, handler: String?) {
            mRecorder.dispatchStart(what, handler)
            executeInterceptors {
                processBefore(mRecorder)
            }
            mStackGetter.addId(mRecorder.recordId)
            checkTimeout(DISPATCH_TIME_OUT_TASK, config.dispatchCheckTime, mStackGetter)
        }

        override fun onDispatched(what: Int, handler: String?) {
            mRecorder.dispatchFinish()
            mStackGetter.removeId()
            cancelTimeout(DISPATCH_TIME_OUT_TASK, config.dispatchCheckTime)
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
            mInterceptors.add(AggregationInterceptor())
            mInterceptors.add(DisperseInterceptor(config.cumulativeThreshold))
            mInterceptors.add(ActivityThreadHandlerInterceptor())
            mInterceptors.add(GetOrCreateInterceptor())
        }
    }
}