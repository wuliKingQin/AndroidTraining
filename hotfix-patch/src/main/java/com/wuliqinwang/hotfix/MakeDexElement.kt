package com.wuliqinwang.hotfix

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import com.wuliqinwang.android.getFieldValue
import com.wuliqinwang.android.invokeMethod
import com.wuliqinwang.android.setFieldValue
import java.io.File

// 热修复需要替换的dexElements的工具类
object DexElementsUtils {
    // 替换
    @SuppressLint("ObsoleteSdkInt")
    fun replaceDexElements(context: Context, classLoader: ClassLoader, pathEntries: List<File>) {
        //23 6.0及以上
        val dexElements = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.M -> V23()
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT -> V19()
            else -> V14()
        }
        dexElements.replaceDexElements(classLoader, pathEntries, context.cacheDir)
    }
}

// 抽象出创建DexElements数组的接口，以兼容不同版本，
// makeDexElements静态方法不同的问题
interface IDexElement {

    // 替换dexElements
    fun replaceDexElements(
        classLoader: ClassLoader,
        pathEntries: List<File>,
        optimizedDirectory: File
    ) {
        try {
            val dexPathList = classLoader.getFieldValue<Any>("pathList")
            val suppressedExceptions = ArrayList<Exception>()
            val newElements = makeDexElements(
                dexPathList,
                pathEntries,
                optimizedDirectory,
                suppressedExceptions
            )
            doReplaceDexElements(dexPathList, newElements)
            // 如果异常爬出来
            suppressedExceptions.forEach { exception ->
                throw exception
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 执行替换操作
    private fun doReplaceDexElements(pathList: Any?, patchDexElements: Array<*>?) {
        val oldDexElements = pathList.getFieldValue<Array<*>>("dexElements")
        val oldDexElementSize = oldDexElements?.size ?: 0
        val patchDexElementSize = patchDexElements?.size ?: 0
        val newElementSize = oldDexElementSize + patchDexElementSize
        // 新建Element数组用于替换老的dexElements的值
        val newElements = oldDexElements?.get(0)?.javaClass?.let { elementClass ->
            java.lang.reflect.Array.newInstance(
                elementClass,
                newElementSize
            )
        }
        if (newElements != null && patchDexElements != null) {
            // 将热修复包优先拷贝到数组中，在将老得拷贝到新数组使其加载类时优先加载热修复包里的类
            System.arraycopy(newElements, 0, patchDexElements, 0, patchDexElementSize)
            System.arraycopy(newElements, patchDexElementSize, oldDexElements, 0, oldDexElementSize)
            // 替换新的元素内容
            pathList.setFieldValue("dexElements", newElements)
        }
    }

    fun makeDexElements(
        dexPathList: Any?,
        pathEntries: List<File>,
        optimizedDirectory: File,
        suppressedExceptions: java.util.ArrayList<Exception>
    ): Array<*>?
}

// 兼容版本23以上
class V23: IDexElement {

    override fun makeDexElements(
        dexPathList: Any?,
        pathEntries: List<File>,
        optimizedDirectory: File,
        suppressedExceptions: java.util.ArrayList<Exception>
    ): Array<*>? {
        return dexPathList.invokeMethod<Array<*>>(
            "makePathElements",
            hashMapOf(
                List::class.java to pathEntries,
                File::class.java to optimizedDirectory,
                List::class.java to suppressedExceptions
            )
        )
    }
}

// 兼容版本19
class V19: IDexElement {
    override fun makeDexElements(
        dexPathList: Any?,
        pathEntries: List<File>,
        optimizedDirectory: File,
        suppressedExceptions: java.util.ArrayList<Exception>
    ): Array<*>? {
        return dexPathList.invokeMethod<Array<*>>(
            "makeDexElements",
            hashMapOf(
                ArrayList::class.java to pathEntries,
                File::class.java to optimizedDirectory,
                ArrayList::class.java to suppressedExceptions
            )
        )
    }
}

// 兼容版本14
class V14: IDexElement {

    override fun makeDexElements(
        dexPathList: Any?,
        pathEntries: List<File>,
        optimizedDirectory: File,
        suppressedExceptions: java.util.ArrayList<Exception>
    ): Array<*>? {
        return dexPathList.invokeMethod<Array<*>>(
            "makeDexElements",
            hashMapOf(
                ArrayList::class.java to pathEntries,
                File::class.java to optimizedDirectory
            )
        )
    }

}