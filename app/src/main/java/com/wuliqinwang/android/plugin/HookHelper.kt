package com.wuliqinwang.android.plugin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Handler
import android.os.Message
import com.wuliqinwang.android.getFieldValue
import com.wuliqinwang.android.setFieldValue
import java.lang.Exception
import java.lang.reflect.Proxy

/**
 * @Description: 用于插件使用的hook帮助类
 */
object HookHelper {

    private const val ACTIVITY_MANAGER = "ActivityManager"
    private const val ACTIVITY_TASK_MANAGER = "ActivityTaskManager"
    private const val ACTIVITY_MANAGER_SINGLETON = "IActivityManagerSingleton"
    private const val ACTIVITY_TASK_MANAGER_SINGLETON = "IActivityTaskManagerSingleton"

    @SuppressLint("PrivateApi")
    fun hookStartActivity() {
        try {
            val aTaskManagerObj = "android.app.${getValue(ACTIVITY_TASK_MANAGER, ACTIVITY_MANAGER)}".getFieldValue(
                getValue(ACTIVITY_TASK_MANAGER_SINGLETON, ACTIVITY_MANAGER_SINGLETON)
            )
            val mInstanceObj = "android.util.Singleton".getFieldValue("mInstance", aTaskManagerObj)
            val activityTaskManagerProxy = Proxy.newProxyInstance(
                Thread.currentThread().contextClassLoader,
                arrayOf(Class.forName("android.app.I${getValue(ACTIVITY_TASK_MANAGER, ACTIVITY_MANAGER)}"))
            ) { _, method, args ->
                if (method.name == "startActivity") {
                    var targetIntentIndex = -1
                    var targetIntent: Intent? = null
                    for (index in args.indices) {
                        val argObj = args[index]
                        if(argObj is Intent) {
                            targetIntentIndex = index
                            targetIntent = argObj
                            break
                        }
                    }
                    targetIntent?.apply {
                        setClassName(
                            "com.wuliqinwang.android",
                            "com.wuliqinwang.android.plugin.DummyPluginActivity"
                        )
                        args[targetIntentIndex] = this
                    }
                }
                method.invoke(mInstanceObj, *args)
            }
            "android.util.Singleton".setFieldValue(
                "mInstance",
                activityTaskManagerProxy,
                aTaskManagerObj
            )
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // DES: 获取到兼容值
    private fun getValue(vararg values: String): String{
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) values[0] else values[1]
    }

    @SuppressLint("PrivateApi")
    fun hookHandleMessageCallback() {
        try {
            val activityThreadObj = "android.app.ActivityThread".getFieldValue("sCurrentActivityThread")
            val mHandleObj = activityThreadObj?.javaClass?.getFieldValue("mH", activityThreadObj)
            val callback = Handler.Callback { msg ->
                var mIntent: Intent? = null
                if (msg.what == 100) {
                    val intentField = msg.obj::class.java.getDeclaredField("intent")
                    intentField.isAccessible = true
                    mIntent = intentField.get(msg.obj) as? Intent
                } else if(msg.what == 159) {
                    val intentField = msg.obj::class.java.getDeclaredField("mIntent")
                    intentField.isAccessible = true
                    mIntent = intentField.get(msg.obj) as? Intent
                }
                mIntent?.apply {
                    setClassName("com.wuliqinwang.android", "com.wuliqinwang.android.plugin.ReallyPluginActivity")
                }
                false
            }
            mHandleObj?.javaClass?.superclass?.setFieldValue("mCallback", callback, mHandleObj)
        }catch (e: Exception) {
            e.printStackTrace()
        }
    }
}