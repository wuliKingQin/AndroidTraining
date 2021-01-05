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
    return findClass()?.getObjField(fieldName)
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
            getObjField(fieldName)?.get(instanceObj)
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
            getObjField(fieldName)
                ?.set(instanceObj, value)
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

// DES: 循环获取对象的属性
fun Class<*>?.getObjField(fieldName: String): Field? {
    this ?: return null
    var tempField: Field? = null
    for (field in declaredFields) {
        if (field.name == fieldName) {
            tempField = field
            tempField.isAccessible = true
            break
        }
    }
    return tempField ?: superclass.getObjField(fieldName)
}

// DES: 获取对象方法，当前对象没有则找到父类的
fun Class<*>?.getObjMethod(methodName: String, vararg paramTypes: Class<*>): Method? {
    this ?: return null
    var tempMethod: Method? = null
    for(method in declaredMethods) {
        if (method.name == methodName && method.parameterTypes.contentDeepEquals(paramTypes)) {
            tempMethod = method
            tempMethod.isAccessible = true
            break
        }
    }
    return tempMethod ?: superclass.getObjMethod(methodName, *paramTypes)
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