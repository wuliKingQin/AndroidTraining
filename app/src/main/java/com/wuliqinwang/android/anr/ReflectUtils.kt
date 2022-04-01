package com.wuliqinwang.android.anr

import java.lang.reflect.Field

/**
 * des: 反射工具类
 * author 秦王
 * time 2022/3/29 17:53
 */
object ReflectUtils {
    // doc: 用于缓存反射的Field类
    private val mCacheFields by lazy {
        HashMap<String, Field>()
    }

    fun Class<*>?.getObjField(fieldName: String?): Field? {
        if (fieldName.isNullOrEmpty()) {
            return null
        }
        return null
    }
}