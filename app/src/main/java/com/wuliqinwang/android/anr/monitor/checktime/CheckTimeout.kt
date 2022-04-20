package com.wuliqinwang.android.anr.monitor.checktime

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import com.utopia.android.ulog.ULog
import com.wuliqinwang.android.anr.monitor.cache.LruRecorder
import com.wuliqinwang.android.anr.monitor.cache.Record
import java.lang.Exception
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.HashMap

// 用于超时检测的任务处理器
private val mCheckTimeHandler by lazy {
    val thread = HandlerThread("checkTimeTask").apply {
        start()
    }
    Handler(thread.looper)
}

// 用于存储超时检测任务
private val mCheckTimeTaskMap by lazy {
    ConcurrentHashMap<String, CheckTimeExecutor>()
}

// 生成超时检测任务的key
private fun getCacheKey(taskName: String, timeout: Long): String {
    return "${taskName}_${timeout}"
}

// 检测超时方法
fun checkTimeout(taskName: String, timeout: Long, runnable: Runnable) {
    val taskKey = getCacheKey(taskName, timeout)
    var taskExecutor = mCheckTimeTaskMap[taskKey]
    if (taskExecutor == null) {
        taskExecutor = CheckTimeExecutor(taskKey, runnable)
        mCheckTimeTaskMap[taskKey] = taskExecutor
    } else {
        mCheckTimeHandler.removeCallbacks(taskExecutor)
    }
    mCheckTimeHandler.postDelayed(taskExecutor, timeout)
}

// 取消超时检测
fun cancelTimeout(taskName: String, timeout: Long) {
    val taskKey = getCacheKey(taskName, timeout)
    val taskExecutor = mCheckTimeTaskMap[taskKey]
    if (taskExecutor != null) {
        mCheckTimeTaskMap.remove(taskKey)
        mCheckTimeHandler.removeCallbacks(taskExecutor)
    }
}

@JvmOverloads
fun cancelAllTimeout(taskPrefix: String? = null) {
    val tempTaskMap = if (taskPrefix.isNullOrEmpty()) {
        HashMap(mCheckTimeTaskMap)
    } else {
        mCheckTimeTaskMap.filter { it.key.startsWith(taskPrefix) }
    }
    tempTaskMap.forEach {
        mCheckTimeTaskMap.remove(it.key)
        mCheckTimeHandler.removeCallbacks(it.value)
    }
}

// 超时检测的执行器
private class CheckTimeExecutor(
    var key: String,
    private val runnable: Runnable
): Runnable {
    override fun run() {
        runnable.run()
        if (mCheckTimeTaskMap.contains(key)) {
            mCheckTimeTaskMap.remove(key)
        }
    }
}

// 用于检测消息调度超时获取对应堆栈的线程
class TimeoutDispatchStackThread @JvmOverloads constructor(
    var waitTime: Long = DEFAULT_TIMEOUT_TIME
): Thread(THREAD_NAME){

    companion object {
        private const val TAG = "MessageDispatcher"
        // 消息调度超时检测线程的名字
        private const val THREAD_NAME = "message-timeout-dispatch-check-thread"
        // 默认的执行等待超时的时间是1秒
        private const val DEFAULT_TIMEOUT_TIME = 1000L
    }

    // 调用wait方法时记录一下当前的时间
    private var mWaitStartTime = 0L

    // 记录wait方法执行完后的真的耗时时间
    private var mWaitRealConsumingTime = 0L

    // 用来表示检查耗时的状态, 当大于0的时候表示需要在规定的时间内等待，
    // 到时间等待结束消息调度还没有结束，则获取主线程的堆栈信息，
    // 当等于-1时表示线程进入一直等待状态
    @Volatile
    private var mCheckTimeStatus = -1L

    // 用于记录产生消息的唯一Id,方便拿着该Id去获取对应的记录实例，把堆栈进行保存
    @Volatile
    private var mRecordId = -1

    @Volatile
    private var isEndCheckTime = false

    // 锁对象，用于实现等待
    private val mWaitLock = Object()

    // 开始超时检测
    fun startCheckTime(recordId: Int) {
        mRecordId = recordId
        mCheckTimeStatus = 2
        interrupt()
    }

    // 取消超时检测
    fun cancelCheckTime() {
        mCheckTimeStatus = -1
        mRecordId = -1
        interrupt()
    }

    override fun run() {
        try {
            realDoCheckTimeout()
        } catch (e: Exception) {
            ULog.d(TAG, e)
        }
    }

    // 在该方法中真的开始做超时检查的逻辑
    private fun realDoCheckTimeout() {
        while (!isEndCheckTime) {
            mWaitStartTime = System.currentTimeMillis()
            try {
                sleep(if (mCheckTimeStatus <= 0) Long.MAX_VALUE else waitTime)
            } catch (e: Exception) {
            }
            if(mCheckTimeStatus > 0) {
                mWaitRealConsumingTime = System.currentTimeMillis() - mWaitStartTime
                var record: Record? = null
                if (mCheckTimeStatus > 0 && mRecordId != -1 && mWaitRealConsumingTime >= waitTime) {
                    record = LruRecorder.getRecord(mRecordId)
                    if (record == null) {
                        record = Record(mRecordId)
                        LruRecorder.putRecord(record)
                    }
                    record.stackInfo = gainStackInfo()
                    ULog.d(TAG, record.stackInfo)
                }
            }
        }
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