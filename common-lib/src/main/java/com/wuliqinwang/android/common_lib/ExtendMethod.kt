package com.wuliqinwang.android.common_lib

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.text.Html
import android.util.Log
import androidx.viewbinding.ViewBinding
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

@Suppress("UNCHECKED_CAST")
fun <T> Any?.getFieldValue(fieldName: String): T? {
    this ?: return null
    return this::class.java.getFieldValue(fieldName, this) as? T
}

// DES: 获取属性的值
fun Class<*>?.getFieldValue(fieldName: String, instanceObj: Any? = null): Any? {
    this ?: return null
    return tryCatch { isError, _ ->
        if (!isError) {
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
fun Any?.setFieldValue(fieldName: String, value: Any?) {
    this ?: return
    this::class.java.setFieldValue(fieldName, value, this)
}

// DES: 设置属性值
fun Class<*>?.setFieldValue(fieldName: String, value: Any?, instanceObj: Any? = null) {
    this ?: return
    tryCatch { isError, _ ->
        if (!isError) {
            getObjField(fieldName)
                ?.set(instanceObj, value)
        }
    }
}

// DES: 设置静态的属性值
fun String?.setStaticFieldValue(fieldName: String, value: Any?) {
    getField(fieldName)?.set(null, value)
}

// DES: 设置静态的属性值
fun Any?.setStaticFieldValue(fieldName: String, value: Any?) {
    this ?: return
    this::class.java.setFieldValue(fieldName, value, null)
}

// DES: 获取类的方法
fun String?.getMethod(methodName: String, vararg paramTypes: Class<*>): Method? {
    return findClass()?.getDeclaredMethod(methodName, *paramTypes)
}

// 调用方法
@Suppress("UNCHECKED_CAST")
fun <T> Any?.invokeMethod(
    methodName: String,
    paramTypes: List<Class<*>>,
    paramValues: List<Any?>,
    isStaticMethod: Boolean = false
): T? {
    this ?: return null
    return this::class.java.getObjMethod(methodName, *paramTypes.toTypedArray())
        ?.invoke(if (isStaticMethod) null else this, *paramValues.toTypedArray()) as? T
}

// DES: 获取构造器
fun String?.getConstructor(vararg paramTypes: Class<*>): Constructor<*>? {
    return findClass()?.getDeclaredConstructor(*paramTypes)
}

// DES: 循环获取对象的属性
fun Class<*>?.getObjField(fieldName: String): Field? {
    this ?: return null
    var tempField: Field? = null
    try {
        tempField = getDeclaredField(fieldName)
        if (!tempField.isAccessible) {
            tempField.isAccessible = true
        }
    } catch (e: Exception) {
    }
    return tempField ?: superclass.getObjField(fieldName)
}

// DES: 获取对象方法，当前对象没有则找到父类的
fun Class<*>?.getObjMethod(methodName: String, vararg paramTypes: Class<*>): Method? {
    this ?: return null
    var tempMethod: Method? = null
    try {
        tempMethod = getDeclaredMethod(methodName, *paramTypes)
        if (!tempMethod.isAccessible) {
            tempMethod.isAccessible = true
        }
    } catch (e: Exception) {
    }
    return tempMethod ?: superclass.getObjMethod(methodName, *paramTypes)
}

// 返回字符串的长度
fun String?.ofSize(): Int {
    return this?.length ?: 0
}

// 返回内容在字符串中的开始位置
fun CharSequence?.ofIndex(content: String?): Int {
    if (this.isNullOrEmpty() || content.isNullOrEmpty()) {
        return -1
    }
    return indexOf(content)
}

// 经过扩展的apply
fun <T, R> T?.applyEx(action: T.() -> R?): R?{
    this ?: return null
    return action(this)
}

// 将英文首字母大小
fun String?.toCapitalLetter(default: String? = ""): String? {
    this ?: return default
    if (isEmpty() || !this[0].isLetter()) return default
    return this[0].toUpperCase().plus(substring(1))
}

// DES: 用于处理异常信息
inline fun <T> tryCatch(action: (isError: Boolean, errorInfo: Exception?) -> T?): T? {
    return try {
        action(false, null)
    } catch (e: Exception) {
        e.printStackTrace()
        Log.d("training====", e.message.toString())
        action(true, e)
    }
}

// 启动界面
fun Activity?.startActivityEx(targetClass: Class<*>, action: Intent.() -> Unit = {}) {
    this ?: return
    startActivity(Intent(this, targetClass).apply(action))
}

// doc: 富文本使用Html显示字符串
fun String?.toHtml(): CharSequence {
    this ?: return ""
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
        } else {
            Html.fromHtml(this)
        }
    } catch (e: Exception) {
        this
    }
}

@JvmOverloads
fun <T: Activity> Context?.launch(targetActivityClass: Class<T>, dataAction: (Intent.() -> Unit)? = null) {
    this ?: return
    startActivity(Intent(this, targetActivityClass).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
        dataAction?.invoke(this)
    })
}

// 启动页面
fun <A: Activity, T: ViewBinding> T?.launch(targetActivityClass: Class<A>, dataAction: (Intent.() -> Unit)? = null) {
    this ?: return
    root.context.launch(targetActivityClass, dataAction)
}