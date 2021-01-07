package com.wuliqinwang.patch

import com.android.build.gradle.AppExtension
import com.android.build.gradle.AppPlugin
import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.tasks.R8Task
import com.android.utils.FileUtils
import jdk.internal.org.objectweb.asm.*
import org.apache.commons.compress.utils.IOUtils
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.regex.Matcher

/**
 * @Version: 1.0.0
 */
class PatchPlugin : Plugin<Project> {

    companion object {
        // DES: 混淆文件名
        private const val MAPPING_FILE_NAME = "mapping.txt"

        // DES: 用于记录需要热修复的Class的md5值
        private const val MD5_FILE_NAME = "classesMd5.txt"

        // DES: 用于存放需要热修复的Class文件
        private const val PATCH_CLASS_FILE_NAME = "patchClasses.jar"

        // DES: 热修复包文件名
        private const val PATCH_DEX_FILE_NAME = "patch.dex"
    }

    override fun apply(target: Project) {
        if (!target.plugins.hasPlugin(AppPlugin::class.java)) {
            throw RuntimeException("非app应用，不适合该插件")
        }
        // DES: 在build.gradle文件中创建一个热修复配置字段
        target.extensions.create(HotFixPatch.HOT_FIX_PATCH, HotFixPatch::class.java)
        // DES: 在build.gradle解析完成后执行我们自己的
        target.afterEvaluate { project ->
            // DES: 获取热修复的配置信息
            val hotFixPatch = project.extensions.getByType(HotFixPatch::class.java)
            // DES: 获取app的配置信息
            val appExtension = project.extensions.findByType(AppExtension::class.java)
            // DES: 变量应用程序的变体，比如debug或者release环境等
            appExtension?.applicationVariants?.all { variant ->
                // DES: 如果实在debug环境，并且热修复在debug的开发是关闭的，则不进行热修复包的生成
                if (variant.name.contains("debug") && !hotFixPatch.isDebugOn) {
                    return@all
                }
                // DES: 生成热修复包以及其他相关文件
                generatePatchDexFile(project, hotFixPatch, variant)
            }
        }
    }

    private fun generatePatchDexFile(
        project: Project,
        hotFixPatch: HotFixPatch,
        variant: ApplicationVariant
    ) {
        val variantName = PatchUtils.getCapitalLetter(
            variant.name
        ) ?: "debug"
        // DES: 获取patch的包存放文件目录
        val patchOutputDir = getPatchOutputDir(project, hotFixPatch, variantName)
        // DES: 获取Android的混淆任务
        val proguardTask = project.tasks
            .findByName("minify${variantName}WithR8")
        // DES: 获取备份的混淆映射文件
        val mappingBackFile = File(patchOutputDir,
            MAPPING_FILE_NAME
        )
        // DES: 开启混淆，我则在混淆完成后，进行备份混淆的映射文件
        backupMappingFile(project, proguardTask, mappingBackFile)
        // DES: 添加之前的混淆文件
        applyTestedMapping(proguardTask as? R8Task, mappingBackFile)
        // DES: 这里就开始执行生成补丁包文件
        doPatchDexFile(project, patchOutputDir, hotFixPatch, variant, variantName)
    }

    // DES: 执行备份混淆文件到指定文件
    private fun backupMappingFile(project: Project, proguardTask: Task?, mappingBackFile: File) {
        proguardTask?.doLast {
            for (file in it.outputs.files.files) {
                if (file.name.endsWith(MAPPING_FILE_NAME)) {
                    try {
                        FileUtils.copyFile(file, mappingBackFile)
                        project.logger.debug("备份混淆文件到: ${mappingBackFile.canonicalPath}")
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    break
                }
            }
        }
    }

    // DES: 使用之前的混淆规则完成这次的混淆，使其打出的修复包混淆和之前的一致
    private fun applyTestedMapping(proguardTask: R8Task?, mappingFile: File) {
        if (mappingFile.exists() && proguardTask != null) {
            proguardTask.testedMappingFile.apply {
                var isOldMappingFile = false
                for (file in files) {
                    if (file.name == mappingFile.name) {
                        isOldMappingFile = true
                        break
                    }
                }
                if (!isOldMappingFile) {
                    files.add(mappingFile)
                }
            }
        }
    }

    // DES: 得到差分包的输出目录
    private fun getPatchOutputDir(
        project: Project,
        hotFixPatch: HotFixPatch,
        variantName: String
    ): File {
        return (if (hotFixPatch.patchOutputDir.isNotEmpty()) {
            File(hotFixPatch.patchOutputDir, variantName)
        } else {
            File(project.buildDir, "patch/${variantName}")
        }).apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }

    // DES: 做生成补丁包的开始方法
    private fun doPatchDexFile(
        project: Project,
        outputDir: File,
        hotFixPatch: HotFixPatch,
        variant: ApplicationVariant,
        variantName: String
    ) {
        project.tasks.findByName("compile${variantName}JavaWithJavac")?.doLast {
            val generator =
                PatchGenerator(project, outputDir)
            val classFiles = it.outputs.files.files
            val applicationName = hotFixPatch.applicationName.replace(
                "\\.",
                Matcher.quoteReplacement(File.separator)
            )
            val md5Map = HashMap<String, String>(classFiles.size)
            for (file in classFiles) {
                val filePath = file.absolutePath
                when {
                    filePath.endsWith(".jar") -> {
                        processJar(
                            applicationName,
                            file
                        ) { className, md5Hex, codeByteArray ->
                            md5Map[className] = md5Hex
                            generator.addClassToJarFile(className, md5Hex, codeByteArray)
                        }
                    }
                    filePath.endsWith(".class") -> {
                        processClass(
                            applicationName,
                            variant.dirName,
                            file
                        ) { className, md5Hex, codeByteArray ->
                            md5Map[className] = md5Hex
                            generator.addClassToJarFile(className, md5Hex, codeByteArray)
                        }
                    }
                }
            }
            // DES: 在这里生成热修复包
            generator.generate(md5Map)
        }
    }

    // DES: 处理class
    private inline fun processClass(
        applicationName: String,
        dirName: String,
        file: File,
        handleCallback: (String, String, ByteArray) -> Unit
    ) {
        val className = file.absolutePath.split(dirName)[1].substring(1)
        if (!isClassHandle(applicationName, className)) {
            return
        }
        try {
            // DES: 执行插桩操作，解决类加载时检验失败的问题
            val codeByteArray = FileInputStream(file).use {
                insertNewCode(it)
            }
            // DES: 修改后的代码覆盖之前的代码
            FileOutputStream(file).use {
                it.write(codeByteArray)
            }
            // DES: 重新计算md5
            val md5Hex = PatchUtils.toMd5Hex(codeByteArray)
            // DES: 将结果回调
            handleCallback(className, md5Hex, codeByteArray)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // DES: 处理jar包里的class
    private inline fun processJar(
        applicationName: String,
        file: File,
        handleCallback: (String, String, ByteArray) -> Unit
    ) {
        val tempApplicationName = applicationName.replace(
            Matcher.quoteReplacement(File.separator),
            "/")
        val backupFile = File(file.parent, "${file.name}.bak")
        // DES: 用于临时缓存
        JarOutputStream(FileOutputStream(backupFile)).use { jarOs ->
            val jarFile = JarFile(file)
            val entries = jarFile.entries()
            while (entries.hasMoreElements()) {
                val jarEntry = entries.nextElement()
                val className = jarEntry.name
                jarOs.putNextEntry(JarEntry(className))
                jarFile.getInputStream(jarEntry).use { jarIo ->
                    if (className.endsWith(".class")
                        && isClassHandle(tempApplicationName, className)
                    ) {
                        val byteArray = insertNewCode(jarIo)
                        val md5Hex = PatchUtils.toMd5Hex(byteArray)
                        handleCallback(className, md5Hex, byteArray)
                        jarOs.write(byteArray)
                    } else {
                        jarOs.write(IOUtils.toByteArray(jarIo))
                    }
                }
                jarOs.closeEntry()
            }
            jarFile.close()
            file.delete()
            backupFile.renameTo(file)
        }
    }

    // DES: 用于判断该class是否需要进行处理
    private fun isClassHandle(applicationName: String, className: String): Boolean {
        return !className.startsWith(applicationName)
                && !className.startsWith("android")
                && !className.startsWith("androidx")
                && !className.startsWith("com.wuliqinwang.hotfix.patch")
    }

    // DES: 在该方法中进行插桩操作
    private fun insertNewCode(fileIo: InputStream): ByteArray {
        val reader = ClassReader(fileIo)
        val writer = ClassWriter(reader, 0)
        val visitor = object : ClassVisitor(Opcodes.ASM5, writer) {
            override fun visitMethod(
                access: Int,
                name: String?,
                desc: String?,
                signature: String?,
                exceptions: Array<out String>?
            ): MethodVisitor {
                val visitorMethod = super.visitMethod(access, name, desc, signature, exceptions)
                return object : MethodVisitor(api, visitorMethod) {
                    override fun visitInsn(opCodes: Int) {
                        // DES: 在构造器中添加一个FixCompatible类的引用
                        if (name == "<init>" && opCodes == Opcodes.RETURN) {
                            super.visitLdcInsn(Type.getType("Lcom.wuliqinwang.hotfix.FixCompatible;"))
                        }
                        super.visitInsn(opCodes)
                    }
                }
            }
        }
        reader.accept(visitor, 0)
        return writer.toByteArray()
    }
}