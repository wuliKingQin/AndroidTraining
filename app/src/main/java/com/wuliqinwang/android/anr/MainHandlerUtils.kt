package com.wuliqinwang.android.anr

import android.annotation.SuppressLint
import java.lang.Exception
import java.lang.reflect.Field

/**
 * des: 用于反射获取ActivityThread类里面的mH的相关信息
 * author 秦王
 * time 2022/3/29 17:44
 */
object MainHandlerUtils {

    @Synchronized
    @SuppressLint("PrivateApi")
    fun getActivityThreadCls(): Class<*>? {
        return try {
            return Class.forName("android.app.ActivityThread")
        } catch (e: ClassNotFoundException) {
            null
        }
    }

    @Synchronized
    fun getMainHandlerField(): Field? {
        return try {
            null
        } catch (e: Exception) {
            null
        }
    }
}