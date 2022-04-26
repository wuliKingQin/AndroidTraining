package com.utopia.anr.water

import android.os.Looper
import com.utopia.anr.water.config.Config
import com.utopia.anr.water.impls.MessageMonitor
import com.utopia.anr.water.monitor.Monitor

// 监控门面类，主要封装进行初始化监控的入口
object Water {

    // 保存主线程消息调度的监控器
    private var mMessageMonitor: Monitor? = null

    // 用该方法初始化监控方法
    @JvmStatic
    fun init(config: Config) {
        if (mMessageMonitor == null) {
            mMessageMonitor = MessageMonitor(Looper.getMainLooper(), config)
        }
        mMessageMonitor?.startMonitor()
    }
}