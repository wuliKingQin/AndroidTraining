package com.wuliqinwang.hotfix

import android.app.Application
import android.content.Context
import android.os.Build
import com.wuliqinwang.android.getFieldValue
import com.wuliqinwang.android.setFieldValue
import dalvik.system.DexFile
import dalvik.system.PathClassLoader
import java.io.File
import java.lang.Exception
import java.lang.StringBuilder

// ClassLoader创建器
object ClassLoaderCreator {

    private const val PATH_LIST = "pathList"
    private const val DEX_ELEMENTS = "dexElements"
    private const val DEX_FILE = "dexFile"
    private const val NATIVE_LIBRARY_DIRECTORIES = "nativeLibraryDirectories"
    private const val CONTEXT_BASE = "mBase"
    private const val CONTEXT_PACKAGE_INFO = "mPackageInfo"
    private const val CLASS_LOADER = "mClassLoader"
    private const val DRAWABLE_INFLATER = "mDrawableInflater"

    // 创建新的PatchClassLoader
    fun createClassLoader(app: Application): ClassLoader {
        return try {
            val pathClassLoader = doCreateClassLoader(app, app.classLoader, app.classLoader.parent)
            doReplaceClassLoader(app, pathClassLoader)
            pathClassLoader
        } catch (e: Exception) {
            e.printStackTrace()
            app.classLoader
        }
    }

    // 创建ClassLoader
    private fun doCreateClassLoader(
        context: Context,
        oldClassLoader: ClassLoader,
        parent: ClassLoader
    ): ClassLoader {
        val oldPathList = oldClassLoader.getFieldValue<Any>(PATH_LIST)
        val oldDexElements = oldPathList.getFieldValue<Array<*>>(DEX_ELEMENTS)
        // 构造dexPath
        val dexPathBuilder = StringBuilder()
        val packageName = context.packageName
        val oldDexElementsSize = oldDexElements?.size ?: 0
        for(index in 0 until oldDexElementsSize) {
            val oldElement = oldDexElements?.get(index)
            val dexPath = oldElement.getFieldValue<DexFile>(DEX_FILE)?.name
            if (dexPath.isNullOrEmpty()) {
                continue
            }
            if (!dexPath.contains("/$packageName")) {
                continue
            }
            if (index != 0) {
                dexPathBuilder.append(File.pathSeparator)
            }
            dexPathBuilder.append(dexPath)
        }
        // 构造lib库目录
        val oldNativeLibraryDirs = oldPathList.getFieldValue<List<File>>(NATIVE_LIBRARY_DIRECTORIES)
        val libDirBuilder = StringBuilder()
        val oldNativeLibraryDirSize = oldNativeLibraryDirs?.size ?: 0
        for (index in 0 until oldNativeLibraryDirSize) {
            val libDir = oldNativeLibraryDirs?.get(index) ?: continue
            if (index != 0) {
                libDirBuilder.append(File.pathSeparator)
            }
            libDirBuilder.append(libDir.absolutePath)
        }
        return PathClassLoader(
            dexPathBuilder.toString(),
            libDirBuilder.toString(),
            parent
        )
    }

    // 用于替换PathClassLoader
    private fun doReplaceClassLoader(app: Application, classLoader: ClassLoader) {
        Thread.currentThread().contextClassLoader = classLoader
        app.getFieldValue<Context>(CONTEXT_BASE)
            .getFieldValue<Any>(CONTEXT_PACKAGE_INFO)
            .setFieldValue(CLASS_LOADER, classLoader)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1) {
            app.resources.setFieldValue(CLASS_LOADER, classLoader)
            app.resources.getFieldValue<Any>(DRAWABLE_INFLATER)
                .setFieldValue(CLASS_LOADER, classLoader)
        }
    }
}