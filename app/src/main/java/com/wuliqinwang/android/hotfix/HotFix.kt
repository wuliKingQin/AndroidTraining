package com.wuliqinwang.android.hotfix

import android.content.Context
import com.wuliqinwang.android.getFieldValue
import com.wuliqinwang.android.getObjMethod
import com.wuliqinwang.android.setFieldValue
import java.io.File
import java.io.IOException
import java.lang.reflect.Array

/**
 * @author: 秦王
 */
object HotFix {

    // DES: 实现热修复,
    fun fix(context: Context, patchDexPath: String) {
        // DES: 获取PathClassLoader的class
        val pathClassLoader = context.classLoader::class.java
        // DES: 获取BaseDexClassLoader的属性pathList对象实例
        val dexPathList = pathClassLoader.getFieldValue("pathList", context.classLoader)
        // DES: 获取DexPathList的dexElements对象
        val oldDexElements = dexPathList?.javaClass.getFieldValue("dexElements", dexPathList) as? kotlin.Array<*>
        val patchDexFile = File(patchDexPath)
        // DES: 使用DexPathList的makePathElement方法将补丁包dex转换为Element数组
        val ioExceptionList = arrayListOf<IOException>()
        val patchElements = dexPathList?.javaClass
            .getObjMethod(
                "makeDexElements",
                List::class.java,
                File::class.java,
                List::class.java,
                ClassLoader::class.java
            )?.invoke(null, arrayListOf(patchDexFile), null, ioExceptionList, pathClassLoader.classLoader) as? kotlin.Array<*>
        // DES: 创建新的数组
        val oldDexElementsSize = oldDexElements?.size ?: 0
        val patchElementsSize = patchElements?.size ?: 0
        val elementClass = oldDexElements?.get(0)?.javaClass
        val newElements = Array.newInstance(elementClass, oldDexElementsSize + patchElementsSize)
        // DES: 将补丁包和老的dex文件拷贝到新的数组中
        System.arraycopy(newElements, 0, patchElements, 0, patchElementsSize)
        System.arraycopy(newElements, patchElementsSize, oldDexElements, 0, oldDexElementsSize)
        // DES: 设置新的值
        dexPathList?.javaClass.setFieldValue("dexElements", newElements)
    }
}