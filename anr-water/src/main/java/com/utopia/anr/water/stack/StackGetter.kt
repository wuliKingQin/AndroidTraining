package com.utopia.anr.water.stack

import android.os.Looper

// 抽象出一个调用栈获取器
interface StackGetter: Runnable {

    // 添加记录Id
    fun addId(id: Int)

    // 移除记录Id
    fun removeId()

    // 调用该方法来收集堆栈信息
    fun startCollect() {
        run()
    }

    // 获取主线程堆栈
    fun gainStackInfo(): String {
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