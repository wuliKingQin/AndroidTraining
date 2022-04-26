package com.wuliqinwang.android.anr

import android.os.Handler
import android.os.Looper
import android.util.Printer
import android.view.View
import androidx.databinding.ObservableField
import androidx.lifecycle.ViewModel
import com.utopia.android.ulog.ULog
import com.utopia.anr.water.cache.LruRecorder
import com.utopia.anr.water.cache.Record
import com.wuliqinwang.android.bottombar.BottomBarActivity
import com.wuliqinwang.android.common_lib.launch

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

    // 开始反射点击处理
    fun startReflectClick() {
        val list = arrayListOf(0.4f, 5f, 0.3f, 3f, 0.01f)
        for (time in list) {
            mUiHandler.post(ConsumingRunnable(time))
        }
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

    fun timeConsumingClick(view: View) {
        view.context.launch(BottomBarActivity::class.java)
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
}