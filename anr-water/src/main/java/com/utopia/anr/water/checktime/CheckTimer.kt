package com.utopia.anr.water.checktime

import com.utopia.anr.water.stack.StackGetter

// 抽象出一个超时检查接口
interface CheckTimer {

    // 开启一个超时检查, timeout是超时时间，等到超时后执行获取堆栈的逻辑
    fun startCheck(timeout: Long, stackGetter: StackGetter)

    // 取消超时检查
    fun cancelCheck()
}