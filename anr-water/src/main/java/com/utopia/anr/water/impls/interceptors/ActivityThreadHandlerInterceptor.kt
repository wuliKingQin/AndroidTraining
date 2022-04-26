package com.utopia.anr.water.impls.interceptors

import com.utopia.anr.water.cache.Record
import com.utopia.anr.water.dispatchers.Interceptor
import com.utopia.anr.water.dispatchers.Recorder
import com.utopia.anr.water.what.What

// 处理ActivityThread里面的Handler发送的一些消息，进行单独监控
class ActivityThreadHandlerInterceptor: Interceptor {

    companion object {
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

    override fun onIntercepted(next: Interceptor.Chain): Record {
        val recorder = next.getRecorder()
        val record = next.process(recorder)
        if (isActivityThreadHandler(recorder)) {
            val whatRecord = if (record.count == 0 && record.wall == 0L) {
                record
            } else {
                record.newBuilder()
                    .setId(recorder.produceId())
                    .setCount(0)
                    .build()
            }
            whatRecord.des = getDes(recorder) ?: whatRecord.des
            whatRecord.count = 1
            whatRecord.what = recorder.what
            whatRecord.wall = recorder.calDispatchConsuming()
            whatRecord.handler = recorder.handler
            recorder.resetRecordId()
        }
        return record
    }

    // 筛选出对应的What的描述信息
    private fun getDes(recorder: Recorder): String? {
        return mWhats.firstOrNull { it.what == recorder.what }?.des
    }

    // 判断是否是ActivityThread里面Handler发送的消息
    private fun isActivityThreadHandler(recorder: Recorder): Boolean {
        val isHasWhat = mWhats.any { it.what == recorder.what }
        return isHasWhat && recorder.handler == ACTIVITY_THREAD_HANDLER
    }
}