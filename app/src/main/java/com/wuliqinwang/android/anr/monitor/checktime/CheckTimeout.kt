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