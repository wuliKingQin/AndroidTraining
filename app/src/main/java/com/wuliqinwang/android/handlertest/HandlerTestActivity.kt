package com.wuliqinwang.android.handlertest

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.wuliqinwang.act.register.annotation.ActRegister
import com.wuliqinwang.android.common_lib.base.BaseActivity
import com.wuliqinwang.android.databinding.ActivityHandlerTestBinding
import java.lang.ref.WeakReference

@ActRegister(name = "Handler内存泄露测试")
class HandlerTestActivity : BaseActivity<ActivityHandlerTestBinding>() {

    private val mHandler = MyHandler(this)

    override fun ActivityHandlerTestBinding.onBindDataForView(savedInstanceState: Bundle?) {
        sendMessageBtn.setOnClickListener {
            mHandler.postDelayed(Run(), 1000000)
            finish()
        }
    }

    class Run: Runnable {
        override fun run() {
            print("我发送了消息")
        }
    }

    fun print() {
        print("我发送了消息3")
    }

    class MyHandler(context: HandlerTestActivity): Handler(Looper.getMainLooper()) {
        private var mContext = WeakReference<HandlerTestActivity>(context)
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            mContext.get()?.print()
            print("我发送了消息2")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeCallbacksAndMessages(null)
    }
}