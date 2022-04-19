package com.wuliqinwang.android.anr

import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Printer
import android.view.View
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.utopia.android.ulog.ULog
import com.wuliqinwang.android.anr.monitor.cache.LruRecorder
import com.wuliqinwang.android.anr.monitor.cache.Record
import com.wuliqinwang.android.anr.monitor.impls.MessageMonitor
import com.wuliqinwang.android.common_lib.launch
import com.wuliqinwang.android.mvvm.DataModel
import com.wuliqinwang.android.mvvm.MvvmTestActivity
import java.io.Serializable
import java.util.*
import kotlin.collections.LinkedHashMap
import kotlin.random.Random
import kotlin.reflect.full.companionObject
import kotlin.reflect.full.companionObjectInstance

class AnrViewModel : ViewModel() {

    var printer: Printer? = null
    val reflectContent by lazy {
        ObservableField("")
    }

    val recordAdapter by lazy {
        RecordDataAdapter(this)
    }

    private val mUiHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val mMonitor by lazy {
        MessageMonitor(Looper.getMainLooper())
    }

    // 开始反射点击处理
    fun startReflectClick() {
        mMonitor.startMonitor()
//        val frameHandler = MainHandlerUtils.getChoreographerHandler()
//        frameHandler.setValueOfField("mCallback", object : Handler.Callback {
//            override fun handleMessage(msg: Message): Boolean {
//                printMessage("Frame Handler message what: ${msg.what} message when: ${msg.`when`}")
//                return false
//            }
//        })
//        printMessage(frameHandler.toString(), 50)
//        for (index in 0..1000) {
//            LruRecorder.putRecord(Record(index, wall = index * 1000L))
//        }
//        LruRecorder.clearAll()
//        printMessage("record size: ${LruRecorder.getRecordSize()} id: ${LruRecorder.getRecord(999)?.wall}")
    }

    private fun printMessage(msg: String?, delayed: Long = 0) {
        ULog.d("AnrViewModel", msg)
        if (delayed <= 0) {
            reflectContent.set(msg.plus("\n"))
        } else {
            mUiHandler.postDelayed({
                reflectContent.set(msg.plus("\n"))
            }, delayed)
        }
    }

    fun startActivityClick(view: View) {
        if (LruRecorder.getRecordSize() > 0) {
            recordAdapter.dataList.setDataWithNotify(LruRecorder.getAllRecords())
        }
    }

    fun timeConsumingClick() {
        val list = arrayListOf(0.4f, 5f, 0.3f, 3f, 0.01f)
        for (time in list) {
            mUiHandler.post(ConsumingRunnable(time))
        }
    }

    fun rvItemClick(record: Record) {
        reflectContent.set(record.stackInfo)
    }

    class ConsumingRunnable(
        private var executeTime: Float
    ): Runnable {
        override fun run() {
            val index = System.currentTimeMillis()
            while (true) {
                if ((System.currentTimeMillis() - index) >= executeTime * 1000) {
                    ULog.d("AnrViewModel", "ConsumingRunnable executeTime=${executeTime}")
                    break
                }
            }
        }
    }

    class MainThreadMessageScheduleCounter(
        private var callback: (String) -> Unit
    ): Printer{

        companion object {
            private const val MAX_MESSAGE_SIZE = 100
        }

        private var mCurrentMsgTime: Long = 0
        private var mCurrentMessageId: Long = 0
        private val mCacheMessageList by lazy {
            LinkedHashMap<Long, Msg>(MAX_MESSAGE_SIZE, 2f, true)
        }

        private val mStackThread by lazy {
            HandlerThread("AnrStack").apply {
                start()
            }
        }

        private val mStackHandler by lazy {
            Handler(mStackThread.looper)
        }

        private val mTimeoutCheckRunnable by lazy(LazyThreadSafetyMode.NONE) {
            TimeoutCheckRunnable()
        }


        init {
            Looper.getMainLooper().setMessageLogging(this)
        }

        override fun println(message: String?) {
            when {
                message?.startsWith(">>>>> Dispatching to ") == true -> {
                    startRunMessage()
                }
                message?.startsWith("<<<<< Finished to ") == true -> {
                    endRunMessage()
                }
            }
        }

        private fun startRunMessage() {

            mCurrentMsgTime = System.currentTimeMillis()
            if (mCurrentMessageId <= 0) {
                mCurrentMessageId = System.currentTimeMillis()
            }
            mStackHandler.postDelayed(mTimeoutCheckRunnable, 300)
        }

        private fun endRunMessage() {
            mStackHandler.removeCallbacks(mTimeoutCheckRunnable)
            val diffTime = System.currentTimeMillis() - mCurrentMsgTime
            ULog.d("AnrViewModel", "记录消息个数：${getMsgSize()} diffTime: $diffTime")
            var msg = mCacheMessageList[mCurrentMessageId]
            if (msg != null) {
                msg.wall += diffTime
                if (msg.wall >= 300) {
                    mCurrentMessageId = 0
                    ULog.d("AnrViewModel", "记录消息个数：${getMsgSize()} 当前消息耗时: ${msg.wall}")
                }
            } else {
                msg = Msg(wall = diffTime, count = 1)
                mCacheMessageList[mCurrentMessageId] = msg
            }
        }

        fun getMsgSize(): Int = mCacheMessageList.size

        inner class TimeoutCheckRunnable: Runnable {

            override fun run() {
                mStackHandler.post(GainMainStackRunnable(mCurrentMessageId))
            }
        }

        // 耗时消息统计模型
        data class Msg(
            var id: Long = 0,
            var wall: Long = 0,
            var name: String = "普通消息",
            var count: Int = 0,
            var stackInfo: String? = null
        ): Serializable

        inner class GainMainStackRunnable(
            private var msgId: Long
        ): Runnable{
            override fun run() {
                val stackBuilder = StringBuilder()
                Looper.getMainLooper().thread.stackTrace.forEachIndexed { index, stackTraceElement ->
                    if (index != 0) {
                        stackBuilder.append("\n")
                    }
                    stackBuilder.append(stackTraceElement.toString())
                }
                var msg: Msg?
                val tempMsgList = mCacheMessageList
                synchronized(tempMsgList) {
                    msg = tempMsgList[msgId]
                    if (msg == null) {
                        msg = Msg(msgId)
                        tempMsgList[msgId] = msg!!
                    }
                }
                msg?.stackInfo = stackBuilder.toString()
                ULog.d("AnrViewModel",  msg?.stackInfo)
            }
        }
    }
}