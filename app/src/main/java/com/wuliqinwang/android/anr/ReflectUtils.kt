package com.wuliqinwang.android.anr

import java.lang.reflect.AccessibleObject
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.util.*


// ================================反射类的工具类================================ \\
private class ReflectUtilsImpl {

    // 反射缓存对象
    private val mReflectCacheMap by lazy {
        WeakHashMap<String, AccessibleObject>(DEFAULT_CACHE_SIZE)
    }

    companion object {
        // 反射缓存的默认大小64
        private const val DEFAULT_CACHE_SIZE = 64
    }

    // 获取缓存的key通过Class和需要缓存的名字
    private fun Class<*>.getCacheKey(cacheObjName: String, vararg paramTypes: Class<*>): String {
        val keyBuilder = StringBuilder()
        keyBuilder.append(toString())
        keyBuilder.append("#")
        keyBuilder.append(cacheObjName)
        return if (paramTypes.isEmpty()) {
            keyBuilder.toString()
        } else {
            keyBuilder.append("#")
            paramTypes.forEachIndexed { index, paramType ->
                if (index != 0) {
                    keyBuilder.append("-")
                }
                keyBuilder.append(paramType.simpleName)
            }
            keyBuilder.toString()
        }
    }

    // 获取对象的属性，包含属性方法以及属性对象
    @Suppress("UNCHECKED_CAST")
    fun <T : AccessibleObject> getObjProperty(
        cls: Class<*>?,
        propertyName: String,
        findAction: Class<*>.() -> T?,
        vararg paramTypes: Class<*>
    ): T? {
        cls ?: return null
        val cacheKey = cls.getCacheKey(propertyName, *paramTypes)
        var tempField: AccessibleObject? = null
        synchronized(mReflectCacheMap) {
            tempField = mReflectCacheMap[cacheKey]
        }
        return if (tempField != null) {
            tempField as? T
        } else {
            cls.findProperty(cacheKey, findAction)
        }
    }

    // 找对象的属性，包含属性方法以及属性对象
    @Suppress("UNCHECKED_CAST")
    private fun <T : AccessibleObject> Class<*>?.findProperty(
        cacheKey: String,
        findAction: Class<*>.() -> T?
    ): T? {
        this ?: return null
        var tempCls = this
        var tempField: AccessibleObject? = null
        while (tempField == null && tempCls != null) {
            try {
                tempField = tempCls.findAction()
                if (tempField?.isAccessible == false) {
                    tempField.isAccessible = true
                }
                if (tempField != null) {
                    synchronized(mReflectCacheMap) {
                        mReflectCacheMap[cacheKey] = tempField
                    }
                    break
                }
            } catch (e: Exception) {
            }
            tempCls = tempCls.superclass
        }
        return tempField as? T
    }
}

// 单列实例对象
@Volatile
private var mReflectUtilsImpl: ReflectUtilsImpl? = null

// 获取反射的单列实例
private fun getInstance(): ReflectUtilsImpl? {
    if (mReflectUtilsImpl == null) {
        synchronized(ReflectUtilsImpl::class.java) {
            if (mReflectUtilsImpl == null) {
                mReflectUtilsImpl = ReflectUtilsImpl()
            }
        }
    }
    return mReflectUtilsImpl
}

// 获取XX对象的属性对象，根据属性名
fun Class<*>?.getObjField(fieldName: String): Field? {
    return getInstance()?.getObjProperty(this, fieldName, {
        getDeclaredField(fieldName)
    })
}

// 获取对象的方法的对象
fun Class<*>?.getObjMethod(methodName: String, vararg paramTypes: Class<*>): Method? {
    this ?: return null
    return getInstance()?.getObjProperty(this, methodName, {
        getDeclaredMethod(methodName, *paramTypes)
    }, *paramTypes)
}

// 获取Field属性对象实例值
@Suppress("UNCHECKED_CAST")
fun <T> Any?.getValueOfField(fieldName: String): T? {
    this ?: return null
    val field = this::class.java.getObjField(fieldName)
    return field?.get(this) as? T
}

// 设置指定名字属性的值
fun Any?.setValueOfField(fieldName: String, value: Any?) {
    this ?: return
    this::class.java.getObjField(fieldName)?.set(this, value)
}

// 设置指定名字静态属性的值
fun Any?.setValueOfStaticField(fieldName: String, value: Any?) {
    this ?: return
    this::class.java.getObjField(fieldName)?.set(null, value)
}

// 获取Field属性对象的静态实例值
@Suppress("UNCHECKED_CAST")
fun <T> Class<*>?.getValueOfStaticField(fieldName: String): T? {
    this ?: return null
    val field = getObjField(fieldName)
    return field?.get(null) as? T
}

// 执行某个类的方法
@Suppress("UNCHECKED_CAST")
private fun <T> Any?.runObjMethod(
    cls: Class<*>?,
    methodName: String,
    isStatic: Boolean,
    paramsAndValues: Map<Class<*>, Any?>? = null
): T? {
    cls ?: return null
    val paramTypes = paramsAndValues?.keys?.toTypedArray()
    val method: Method?
    return if (paramTypes != null) {
        method = cls.getObjMethod(methodName, *paramTypes)
        method?.invoke(if (isStatic) null else this, *paramsAndValues.values.toTypedArray()) as? T
    } else {
        method = cls.getObjMethod(methodName)
        method?.invoke(if (isStatic) null else this) as? T
    }
}

// 执行某个类的方法
@JvmOverloads
fun <T> Any?.runMethod(methodName: String, paramsAndValues: Map<Class<*>, Any?>? = null): T? {
    this ?: return null
    return runObjMethod(this::class.java, methodName, false, paramsAndValues)
}

// 执行某个类的静态类方法, kotlin的伴生类如果是静态的需要添加@JvmStatic注解
@JvmOverloads
fun <T> Class<*>?.runStaticMethod(
    methodName: String,
    paramsAndValues: Map<Class<*>, Any?>? = null
): T? {
    return runObjMethod(this, methodName, true, paramsAndValues)
}