package com.utopia.anr.water.config

import com.utopia.anr.water.checktime.CheckTimer
import com.utopia.anr.water.dispatchers.Dispatcher
import com.utopia.anr.water.dispatchers.Interceptor

// 监控配置信息
class Config private constructor(
    builder: Builder
) {
    // 用于判断是否debug模式
    var isDebug = builder.isDebug
    // 灰度开关，用于线上有问题时，可以关掉
    var graySwitch = builder.graySwitch
    // 用于消息调度的检查超时的时间，默认是1秒
    val dispatchCheckTime = builder.dispatchCheckTime
    // 用于累计消息调度耗时的阈值，默认是300毫秒
    val cumulativeThreshold = builder.cumulativeThreshold
    // 拦截器
    val interceptors = builder.interceptors
    // 消息调度器
    val dispatcher = builder.dispatcher
    // 超时检查器
    val checkTimer = builder.checkTimer

    // 配置构建器
    data class Builder @JvmOverloads constructor(
        internal var isDebug: Boolean = false,
        internal var graySwitch: Boolean = true,
        internal var dispatchCheckTime: Long = 1000L,
        internal var cumulativeThreshold: Long = 300L,
        internal var interceptors: ArrayList<Interceptor>? = null,
        internal var dispatcher: Dispatcher? = null,
        internal var checkTimer: CheckTimer? = null
    ) {

        // 设置是否是debug模式
        fun setDebug(isDebug: Boolean): Builder {
            this.isDebug = isDebug
            return this
        }

        // 设置灰度开关是否打开
        fun setGraySwitch(graySwitch: Boolean): Builder {
            this.graySwitch = graySwitch
            return this
        }

        // 设置调度超时时间，超过后就直接去获取对应栈信息
        fun setDispatchCheckTime(dispatchCheckTime: Long): Builder {
            this.dispatchCheckTime = dispatchCheckTime
            return this
        }

        // 设置累计消息调度耗时的阈值时间，目的是用来做记录的分类，耗时严重的要进行单独统计
        fun setCumulativeThreshold(cumulativeThreshold: Long): Builder {
            this.cumulativeThreshold = cumulativeThreshold
            return this
        }

        // 添加自己的调度消息统计的拦截器业务
        fun addInterceptor(interceptor: Interceptor): Builder {
            if (interceptors == null) {
                interceptors = ArrayList()
            }
            if (interceptors?.contains(interceptor) == false) {
                interceptors?.add(interceptor)
            }
            return this
        }

        // 设置调度器
        fun setDispatcher(dispatcher: Dispatcher?): Builder {
            this.dispatcher = dispatcher
            return this
        }

        // 设置超时检查器
        fun setCheckTimer(checkTimer: CheckTimer?): Builder {
            this.checkTimer = checkTimer
            return this
        }

        // 构建配置类
        fun build(): Config {
            return Config(this)
        }
    }
}