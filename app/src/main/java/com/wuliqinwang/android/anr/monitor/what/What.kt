package com.wuliqinwang.android.anr.monitor.what

// 定义需要监控的消息类型
enum class What(var what: Int, var des: String? = null){
    AT_HANDLER_WHAT_100(100, "launchActivity"),
    AT_HANDLER_WHAT_101(101, "pauseActivity"),
    AT_HANDLER_WHAT_103(103, "stopActivityShow"),
    AT_HANDLER_WHAT_104(104, "stopActivityHide"),
    AT_HANDLER_WHAT_105(105, "showWindow"),
    AT_HANDLER_WHAT_106(106, "hideWindow"),
    AT_HANDLER_WHAT_107(107, "resumeActivity"),
    AT_HANDLER_WHAT_109(109, "destroyActivity"),
    AT_HANDLER_WHAT_126(126, "relaunchActivity126"),
    AT_HANDLER_WHAT_113(113, "receiver"),
    AT_HANDLER_WHAT_114(114, "createService"),
    AT_HANDLER_WHAT_115(115, "serviceOnStartCommand"),
    AT_HANDLER_WHAT_116(116, "stopService"),
    AT_HANDLER_WHAT_121(121, "bindService"),
    AT_HANDLER_WHAT_122(122, "unbindService"),
    AT_HANDLER_WHAT_145(145, "installProvider"),
    AT_HANDLER_WHAT_159(159, "activityLifecycleTransaction"),
    AT_HANDLER_WHAT_160(160, "relaunchActivity160"),
}