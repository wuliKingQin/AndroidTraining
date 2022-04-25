package com.wuliqinwang.android.anr.monitor.config

import com.wuliqinwang.android.anr.monitor.dispatchers.Interceptor

// 监控配置信息
class Config private constructor(
    builder: Builder
) {
    // 用于消息调度的检查超时的时间，默认是1秒
    val dispatchCheckTime = builder.dispatchCheckTime
    // 用于累计消息调度耗时的阈值，默认是300毫秒
    val cumulativeThreshold = builder.cumulativeThreshold
    // 拦截器
    val interceptors = builder.interceptors

    // 配置构建器
    data class Builder @JvmOverloads constructor(
        internal var dispatchCheckTime: Long = 1000L,
        internal var cumulativeThreshold: Long = 300L,
        internal var interceptors: ArrayList<Interceptor>? = null
    ) {

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

        // 构建配置类
        fun builder(): Config {
            return Config(this)
        }
    }
}