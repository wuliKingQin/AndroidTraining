package com.wuliqinwang.android

import android.app.Activity
import android.content.Intent
import android.util.Log
import java.lang.Exception
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method

/**
 * @Description: 用于扩展kt的方法类
 */
fun String?.findClass(): Class<*>? {
    this ?: return null
    return tryCatch { isError, _ ->
        if (!isError) {
            Class.forName(this)
        } else {
            null
        }
    }
}

// DES: 获取属性对象
fun String?.getField(fieldName: String): Field? {
    return findClass()?.getDeclaredField(fieldName)?.apply {
        isAccessible = true
    }
}

// DES: 获取属性的值
fun String?.getFieldValue(fieldName: String, instanceObj: Any? = null): Any? {
    return getField(fieldName)?.get(instanceObj)
}

// DES: 获取属性的值
fun Class<*>?.getFieldValue(fieldName: String, instanceObj: Any? = null): Any? {
    this ?: return null
    return tryCatch { isError, _ ->
        if(!isError) {
            getDeclaredField(fieldName).let {
                it.isAccessible = true
                it.get(instanceObj)
            }
        } else {
            null
        }
    }
}

// DES: 重新设置属性的值
fun String?.setFieldValue(fieldName: String, value: Any?, instanceObj: Any? = null) {
    getField(fieldName)?.set(instanceObj, value)
}

// DES: 设置属性值
fun Class<*>?.setFieldValue(fieldName: String, value: Any?, instanceObj: Any ?= null) {
    this ?: return
    tryCatch { isError, _ ->
        if(!isError) {
            getDeclaredField(fieldName).let {
                it.isAccessible = true
                it.set(instanceObj, value)
            }
        }
    }
}

// DES: 设置静态的属性值
fun String?.setStaticFieldValue(fieldName: String, value: Any?) {
    getField(fieldName)?.set(null, value)
}

// DES: 获取类的方法
fun String?.getMethod(methodName: String, vararg paramTypes: Class<*>): Method? {
    return findClass()?.getDeclaredMethod(methodName, *paramTypes)
}

// DES: 获取构造器
fun String?.getConstructor(vararg paramTypes: Class<*>): Constructor<*>? {
    return findClass()?.getDeclaredConstructor(*paramTypes)
}

// DES: 用于处理异常信息
inline fun <T> tryCatch(action: (isError: Boolean, errorInfo: Exception?)->T? ): T? {
    return try {
        action(false, null)
    } catch (e: Exception){
        e.printStackTrace()
        Log.d("training====", e.message.toString())
        action(true, e)
    }
}

// 启动界面
fun Activity?.startActivityEx(targetClass: Class<*>, action: Intent.()-> Unit = {}) {
    this ?: return
    startActivity(Intent(this, targetClass).apply(action))
}