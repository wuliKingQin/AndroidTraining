package com.wuliqinwang.hotfix

import android.app.Application
import android.content.Context
import android.os.Build
import android.util.Log
import java.io.*

// 用于热修复的工具类
object HotFixUtils {

    private const val PACK_FILE_NAME = "hotfix-lib.jar"

    // 安装热修复包
    fun installPatch(application: Application, patchFile: File) {
        /**
         * 1. 获取热修复dex包以及被全类引用到的hack文件
         * 2. 为兼容Android N的开启的混合编译导致的热修复失败，重新构建一个PathCLassLoader替换
         * 3. 将热修复包文件转换成Element数组
         * 4. 将新的Element数组和老的融合成一个新的dexElements数组
         * 5. 将PathDexList里的dexElements值替换成新的值。
         */
        // 热修复包的文件获取以及将hack携带的jar包拷贝到指定目录，方便加载
        val patchList = getPatchFiles(application, patchFile)
        // 兼容Android 7.0的混合编译导致的不能热修复的问题，直接创建一个PathClassLoader来进行替代
        val pathClassLoader = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            ClassLoaderCreator.createClassLoader(application)
        } else {
            application.classLoader
        }
        // 替换添加热修复包的dexElements数组的值
        DexElementsUtils.replaceDexElements(application, pathClassLoader, patchList)
    }

    // 获取热修复包所需要的文件
    private fun getPatchFiles(application: Application, patchFile: File): List<File> {
        val fileList = ArrayList<File>(2)
        try {
            val packDir = application.getDir("back", Context.MODE_PRIVATE)
            val packFile = File(packDir, PACK_FILE_NAME)
            if (!packFile.exists()) {
                FileOutputStream(packFile).use { packOut ->
                    BufferedInputStream(application.assets.open(PACK_FILE_NAME)).use { fileIs ->
                        val buffer = ByteArray(4096)
                        var len = -1
                        while (fileIs.read(buffer).also { len = it } != -1) {
                            packOut.write(buffer, 0 , len)
                        }
                    }
                }
            }
            Log.d("test===", "pack file exist=${packFile.exists()}")
            fileList.add(packFile)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        if (patchFile.exists()) {
            fileList.add(patchFile)
        }
        return fileList
    }
}