package com.wuliqinwang.android.anr.monitor.impls

import android.os.Looper
import com.utopia.android.ulog.ULog
import com.wuliqinwang.android.anr.monitor.AbstractMonitor
import com.wuliqinwang.android.anr.monitor.cache.LruRecorder
import com.wuliqinwang.android.anr.monitor.cache.Record
import com.wuliqinwang.android.anr.monitor.checktime.cancelTimeout
import com.wuliqinwang.android.anr.monitor.checktime.checkTimeout
import com.wuliqinwang.android.anr.monitor.dispatchers.AbstractDispatcher
import com.wuliqinwang.android.anr.monitor.dispatchers.Dispatcher
import com.wuliqinwang.android.anr.monitor.dispatchers.Interceptor
import com.wuliqinwang.android.anr.monitor.dispatchers.Recorder
import com.wuliqinwang.android.anr.monitor.what.What
import java.lang.AssertionError
import java.nio.BufferOverflowException
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicInteger

// 实现一个消息调度的一个监控类
class MessageMonitor(
    looper: Looper
) : AbstractMonitor(looper) {

    override var dispatcher: Dispatcher? = null

    override fun startMonitor() {
        if (dispatcher == null) {
            dispatcher = DefaultDispatcher().apply {
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

    class DefaultDispatcher: AbstractDispatcher() {

        companion object {
            private const val DISPATCH_TIME_OUT_TASK = "dispatchTimeoutTask"
            private const val DISPATCH_TIME_OUT_TIME = 1000L
        }

        // 消息分发记录器
        private val mRecorder by lazy(LazyThreadSafetyMode.NONE) {
            Recorder()
        }

        // 拦截器
        private val mInterceptors by lazy(LazyThreadSafetyMode.NONE) {
            ArrayList<Interceptor>()
        }

        private val mStackExecutor by lazy {
            DispatchStackExecutor()
        }

        init {
            mInterceptors.add(RecordAggregation())
            mInterceptors.add(SplitRecord())
            mInterceptors.add(ActivityThreadHandler())
            mInterceptors.add(GainOrCreateRecord())
        }

        private var mInterceptorChain: Interceptor.Chain? = null

        override fun onDispatching(what: Int, handler: String?) {
            mRecorder.dispatchStart(what, handler)
            mStackExecutor.addId(mRecorder.recordId)
            checkTimeout(DISPATCH_TIME_OUT_TASK, DISPATCH_TIME_OUT_TIME, mStackExecutor)
        }

        override fun onDispatched(what: Int, handler: String?) {
            mRecorder.dispatchFinish()
            mStackExecutor.removeId()
            cancelTimeout(DISPATCH_TIME_OUT_TASK, DISPATCH_TIME_OUT_TIME)
            executeRecords()
        }

        private fun executeRecords() {
            if (mInterceptorChain == null) {
                mInterceptorChain = InterceptorChainImpl(mInterceptors, mRecorder)
            }
            mInterceptorChain?.apply {
                resetIndex()
                process(mRecorder)
            }
        }

        class InterceptorChainImpl(
            private var interceptors: ArrayList<Interceptor>,
            private var recorder: Recorder,
            private var index: Int = 0
        ): Interceptor.Chain {

            override fun resetIndex() {
                index = 0
            }

            override fun process(recorder: Recorder): Record {
                if (index >= interceptors.size) {
                    throw AssertionError("越界")
                }
                val chainImpl = InterceptorChainImpl(interceptors, recorder, index + 1)
                return interceptors[index].intercepted(chainImpl)
            }

            override fun getRecorder(): Recorder {
                return recorder
            }
        }

        class GainOrCreateRecord: Interceptor {

            override fun intercepted(next: Interceptor.Chain): Record {
                val recorder = next.getRecorder()
                return LruRecorder.getRecord(recorder.recordId)
                    ?: Record(recorder.recordId).apply {
                        what = next.getRecorder().what
                        handler = next.getRecorder().handler
                        LruRecorder.putRecord(this)
                    }
            }
        }

        class ActivityThreadHandler: Interceptor {

            companion object {
                private const val TAG = "ActivityThreadHandler"
                private const val ACTIVITY_THREAD_HANDLER = "(android.app.ActivityThread\$H)"
            }

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

            override fun intercepted(next: Interceptor.Chain): Record {
                val recorder = next.getRecorder()
                val record = next.process(recorder)
                if (isActivityThreadHandler(recorder)) {
                    val whatRecord = if (record.count == 0 && record.wall == 0L) {
                        record
                    } else {
                        Record(recorder.produceId()).apply {
                            LruRecorder.putRecord(this)
                        }
                    }
                    whatRecord.des = mWhats.firstOrNull { it.what == recorder.what }?.des ?: whatRecord.des
                    whatRecord.count += 1
                    whatRecord.what = recorder.what
                    whatRecord.wall = recorder.calDispatchConsuming()
                    whatRecord.handler = recorder.handler
                    recorder.recordId = -1
                }
                return record
            }

            private fun isActivityThreadHandler(recorder: Recorder): Boolean {
                val isHasWhat = mWhats.any { it.what == recorder.what }
                ULog.d(TAG, "isHasWhat:$isHasWhat handler: ${recorder.handler}")
                return isHasWhat && recorder.handler == ACTIVITY_THREAD_HANDLER
            }
        }

        class SplitRecord: Interceptor{
            override fun intercepted(next: Interceptor.Chain): Record {
                val recorder = next.getRecorder()
                val record = next.process(recorder)
                val dispatchConsuming = recorder.calDispatchConsuming()
                if (recorder.recordId != -1 && record.wall + dispatchConsuming > 300) {
                    if (record.wall > 0 && record.wall > 200 && (dispatchConsuming - record.wall) >= 0) {
                        Record(recorder.produceId()).apply {
                            what = recorder.what
                            handler = recorder.handler
                            count += 1
                            wall = dispatchConsuming
                            LruRecorder.putRecord(this)
                        }
                    } else {
                        statisticalRecord(recorder, record)
                    }
                    recorder.recordId = -1
                }
                return record
            }

            // 统计耗时段的消息到一个记录里面
            private fun statisticalRecord(recorder: Recorder, record: Record) {
                record.apply {
                    this.what = recorder.what
                    this.handler = recorder.handler
                    wall += recorder.calDispatchConsuming()
                    count += 1
                }
            }
        }

        class RecordAggregation: Interceptor {

            override fun intercepted(next: Interceptor.Chain): Record {
                val recorder = next.getRecorder()
                val record = next.process(recorder)
                if (recorder.recordId != -1) {
                    statisticalRecord(recorder, record)
                }
                return record
            }

            // 统计耗时段的消息到一个记录里面
            private fun statisticalRecord(recorder: Recorder, record: Record) {
                record.apply {
                    this.what = recorder.what
                    this.handler = recorder.handler
                    wall += recorder.calDispatchConsuming()
                    count += 1
                }
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
}