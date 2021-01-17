package com.wuliqinwang.patch

import com.android.build.gradle.AppExtension
import org.apache.tools.ant.taskdefs.condition.Os
import org.gradle.api.Project
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import java.util.jar.JarEntry
import java.util.jar.JarOutputStream

// DES: 热修复包生成器
class PatchGenerator(
    // DES: 工程对象
    private val project: Project,
    // DES: 输出目录
    outputDir: File
){
    companion object {
        // DES: 用于记录需要热修复的Class的md5值
        private const val MD5_FILE_NAME = "classesMd5.txt"
        // DES: 用于存放需要热修复的Class文件
        private const val PATCH_CLASS_FILE_NAME = "patchClasses.jar"
        // DES: 热修复包文件名
        private const val PATCH_DEX_FILE_NAME = "patch.dex"
        // DES: 工程的本地属性配置文件
        private const val LOCAL_PROPERTIES = "local.properties"
    }
    // DES: 保存构建工具的版本
    private val mBuildToolVersion by lazy {
        project.extensions.getByType(AppExtension::class.java).buildToolsVersion
    }
    // DES: 记录md5的文件
    private val mMd5File by lazy {
        File(outputDir, MD5_FILE_NAME)
    }
    private val mOlMd5Map by lazy {
        if(mMd5File.exists()) {
            PatchUtils.readMd5HexFromFile(mMd5File)
        } else {
            hashMapOf()
        }
    }
    // DES: 用于存放需要热修复的Class文件
    private val mJarFile by lazy {
        File(outputDir, PATCH_CLASS_FILE_NAME)
    }
    // DES: 热修复包文件
    private val mPatchFile by lazy {
        File(outputDir, PATCH_DEX_FILE_NAME)
    }
    // DES: 保存一个全局的Jar输入出流对象
    private val mJarOutputStream by lazy {
        JarOutputStream(FileOutputStream(mJarFile)).apply {
        }
    }

    // DES: 把不相同的Md5的class加入到热修复包文件里
    fun addClassToJarFile(className: String, md5Hex: String?, codeByteArray: ByteArray) {
        if (mOlMd5Map.isEmpty()) return
        val oldMd5 = mOlMd5Map[className]
        println("className=${className} oldMd5=${oldMd5} md5Hex=$md5Hex")
        if (oldMd5 == null || oldMd5 != md5Hex) {
            try {
                mJarOutputStream.let {
                    it.putNextEntry(JarEntry(className))
                    it.write(codeByteArray)
                    it.closeEntry()
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // DES: 生成热修复包文件
    fun generate(md5HexMap: Map<String, String>) {
        // DES: 将新的md5信息缓存到指定文件
        PatchUtils.writeMd5HexToFile(md5HexMap, mMd5File)
        println("start generate patch =${mJarFile.exists()}======================")
        // DES: 如果不存在，则不生成热修复包
        if (!mJarFile.exists()) return
        println("start generate patch file======================")
        try {
            mJarOutputStream.close()
            // DES: 因为dx命令在 sdk中，获得sdk目录
            val localFile = project.rootProject.file(LOCAL_PROPERTIES)
            val sdkDir = if(localFile.exists()) {
                val properties = Properties()
                properties.load(FileInputStream(localFile))
                properties.getProperty("sdk.dir")
            } else {
                System.getenv("ANDROID_HOME")
            }
            // DES: windows使用 dx.bat命令,linux/mac使用 dx命令
            val dxSuffix = if(Os.isFamily(Os.FAMILY_WINDOWS)) ".bat" else ""
            // 执行：dx --dex --output=output.jar input.jar
            val dxPath = "${sdkDir}/build-tools/${mBuildToolVersion}/dx${dxSuffix}"
            val outputPatch = "--output=${mPatchFile.absolutePath}"
            val cmd = "$dxPath --dex $outputPatch ${mJarFile.absolutePath}"
            println("dex cmd path=$cmd")
            val process = Runtime.getRuntime().exec(cmd)
            process.waitFor()
            mJarFile.delete()
            //命令执行失败
            if (process.exitValue() != 0) {
                throw IOException("generate patch file error:$cmd")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}