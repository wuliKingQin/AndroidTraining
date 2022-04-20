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
import java.lang.AssertionError
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.atomic.AtomicInteger

// 实现一个消息调度的一个监控类
class MessageMonitor(
    looper: Looper
) : AbstractMonitor(looper) {

    override var dispatcher: Dispatcher? = null

    override fun startMonitor() {
        if (dispatcher == null) {
            dispatcher = MessageDispatcher().apply {
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
        }

        // 保存当前的消息Id
        private var mRecordId = -1

        // 开始调度的时间
        private var mStartTime = 0L

        // 调度完成的消耗时间
        private var mConsumingTime = 0L

        // 用于保存当前生成的记录
        private var mCurRecord: Record? = null

        private val interceptors by lazy {
            ArrayList<DispatchInterceptor>()
        }

        // 分发链
        private var dispatchChain: DispatchInterceptor.Chain? = null

        init {
            interceptors.add(DefaultDispatchInterceptor())
            interceptors.add(ActThreadHDispatchInterceptor())
        }

        private val mStackExecutor by lazy {
            DispatchStackExecutor()
        }

        private val dispatchMessage by lazy {
            DispatchMessage()
        }

        override fun onDispatching(what: Int, handler: String?) {
            if (mRecordId < 0) {
                mRecordId = recordIdGenerator.incrementAndGet()
            }
            dispatchMessage.recordId = mRecordId
            dispatchMessage.what = what
            dispatchMessage.handler = handler
            dispatchMessage.startDispatchTime = System.currentTimeMillis()
            mStackExecutor.addId(mRecordId)
            checkTimeout(DISPATCH_TIME_OUT_TASK, DISPATCH_TIME_OUT_TIME, mStackExecutor)
        }

        override fun onDispatched(what: Int, handler: String?) {
            mStackExecutor.removeId()
            dispatchMessage.endDispatchTime = System.currentTimeMillis()
            ULog.d(TAG, "recordId: $mRecordId what: $what handler: $handler wall: $mConsumingTime")
            cancelTimeout(DISPATCH_TIME_OUT_TASK, DISPATCH_TIME_OUT_TIME)
            val record = mCurRecord ?: getOrCreateRecord()
            if (record.wall + mConsumingTime > 300) {
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
            } else {
                statisticalRecord(what, handler, record)
            }
            doReal()
        }

        private fun doReal() {
            if (dispatchChain == null) {
                dispatchChain = RealDispatchInterceptorChain(interceptors)
            }
            dispatchChain?.process(dispatchMessage)
        }

        // 根据记录Id得到或创建一个记录对象
        private fun getOrCreateRecord(): Record {
            return LruRecorder.getRecord(mRecordId) ?: LruRecorder.putRecord(
                Record(mRecordId, what = getWhat(), handler = handler)
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

        data class DispatchMessage(
            var recordId: Int = 0,
            var what: Int = 0,
            var handler: String? = null,
            var startDispatchTime: Long = 0,
            var endDispatchTime: Long = 0
        )

        interface DispatchInterceptor {

            fun intercepted(next: Chain): Record

            interface Chain {

                // 调度的消息
                fun getDispatchMessage(): DispatchMessage

                // 处理器
                fun process(message: DispatchMessage): Record
            }
        }

        class DefaultDispatchInterceptor: DispatchInterceptor {

            override fun intercepted(next: DispatchInterceptor.Chain): Record {
                val message = next.getDispatchMessage()
                val timeOfConsuming = message.endDispatchTime - message.startDispatchTime
                val record = LruRecorder.getRecord(message.recordId)
                return next.process(message)
            }
        }

        class ActThreadHDispatchInterceptor: DispatchInterceptor{

            companion object {
                private const val ACTIVITY_THREAD_HANDLER = "(android.app.ActivityThread\$H)"
            }

            override fun intercepted(next: DispatchInterceptor.Chain): Record {
                val message = next.getDispatchMessage()
                if (message.handler == ACTIVITY_THREAD_HANDLER) {
                    Record(message.what)
                }
                return next.process(message)
            }
        }

        class RealDispatchInterceptorChain(
            private var interceptors: List<DispatchInterceptor>,
            private var execIndex: Int = 0
        ) : DispatchInterceptor.Chain {

            private lateinit var message: DispatchMessage

            override fun process(message: DispatchMessage): Record {
                this.message = message
                return doProcess()
            }

            override fun getDispatchMessage(): DispatchMessage {
                return message
            }

            private fun doProcess(): Record {
                if (execIndex < 0 || execIndex >= interceptors.size) {
                    throw AssertionError("")
                }
                val nextChain = RealDispatchInterceptorChain(interceptors, execIndex + 1)
                val interceptor = interceptors[execIndex]
                return interceptor.intercepted(nextChain)
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