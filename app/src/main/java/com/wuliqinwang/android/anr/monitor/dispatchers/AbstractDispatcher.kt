package com.wuliqinwang.android.anr.monitor.dispatchers

// 该类实现了对消息关键字段的解析工作，比如消息的what值和消息对应的Handler
abstract class AbstractDispatcher: Dispatcher{

    override var messageWhat: Int? = null

    override var handler: String? = null

    override fun onMsgParsing(messageInfo: String?) {
        if (isDispatching(messageInfo)) {
            messageInfo?.split(Dispatcher.MESSAGE_SEPARATOR)?.apply {
                messageWhat = lastOrNull()?.toInt()
                handler = getMsgHandler(this)
            }
        }
    }

    // 获取消息的Handler, 留出接口防止有变动，子类可以进行重写解决
    protected open fun getMsgHandler(list: List<String>): String? {
        return list[4]
    }

    override fun onFinishDispatch() {
        messageWhat = null
        handler = null
    }
}