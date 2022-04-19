package com.wuliqinwang.android.anr.monitor.dispatchers

import android.util.Printer

// 抽象一个消息调度器的接口
interface Dispatcher : Printer {

    companion object {
        // 开始调度消息的标志位
        private const val START_DISPATCH = ">>>>> Dispatching to"

        // 结束调度消息的标志位
        private const val END_DISPATCH = "<<<<< Finished to "

        // 信息分割符号
        const val MESSAGE_SEPARATOR = " "

        // 无效What
        const val INVALID_WHAT = Int.MIN_VALUE
    }

    // 消息类型
    var messageWhat: Int?

    // 消息处理的Handler
    var handler: String?

    override fun println(messageInfo: String?) {
        onMsgParsing(messageInfo)
        when {
            isDispatching(messageInfo) -> {
                onDispatching(getWhat(), handler)
            }
            isDispatched(messageInfo) -> {
                onDispatched(getWhat(), handler)
            }
        }
        if(isDispatched(messageInfo)) {
            onFinishDispatch()
        }
    }

    // 消息结束调度
    fun onFinishDispatch()

    // 用于处理消息解析
    fun onMsgParsing(messageInfo: String?)

    // 用于处理消息正在调用
    fun onDispatching(what: Int, handler: String?)

    // 用于处理消息调度完成
    fun onDispatched(what: Int, handler: String?)

    // 获取安全的What
    fun getWhat(): Int {
        return messageWhat ?: INVALID_WHAT
    }

    // 判断是否正在分发
    fun isDispatching(messageInfo: String?): Boolean {
        return messageInfo?.startsWith(START_DISPATCH) == true
    }

    // 判断是否分发结束
    fun isDispatched(messageInfo: String?): Boolean {
        return messageInfo?.startsWith(END_DISPATCH) == true
    }
}