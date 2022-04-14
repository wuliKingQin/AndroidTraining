package com.wuliqinwang.android.anr

import android.annotation.SuppressLint
import android.os.Handler
import android.view.Choreographer
import com.wuliqinwang.android.common_lib.getFieldValue
import java.lang.Exception

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
    fun getMainHandler(): Handler? {
        return try {
            val activityThreadObj = "android.app.ActivityThread".getFieldValue("sCurrentActivityThread")
            activityThreadObj.getValueOfField("mH")
        } catch (e: Exception) {
            null
        }
    }

    @Synchronized
    fun getChoreographerHandler(): Handler? {
        return try {
            Choreographer.getInstance().getValueOfField("mHandler")
        } catch (e: Exception) {
            null
        }
    }
}