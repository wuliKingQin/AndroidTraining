package com.wuliqinwang.android.anr.monitor

import android.os.Looper
import com.wuliqinwang.android.anr.monitor.config.Config
import com.wuliqinwang.android.anr.monitor.dispatchers.Dispatcher

// 抽象一个监控器类，方便具体的监控器实现
abstract class AbstractMonitor(
    // 消息循环器, 用来设置消息调度器
    protected var looper: Looper,
    // 监控的配置信息
    protected var config: Config
): Monitor {
    // 声明一个分发器，给子类自己实现
    protected var dispatcher = config.dispatcher
}