package com.wuliqinwang.patch

import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformInvocation
import com.android.build.gradle.internal.pipeline.TransformManager
import com.wuliqinwang.patch.asm.ActivityClassVisitor
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import java.io.File
import java.io.FileOutputStream

/**
 * @Description:
 * @Version: 1.0.0
 */
class HotFixTransform: Transform() {

    override fun getName(): String {
        return "hotFix"
    }

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> {
        return TransformManager.CONTENT_CLASS
    }

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    override fun isIncremental(): Boolean {
        return true
    }

    override fun isCacheable(): Boolean {
        return true
    }

    override fun transform(transformInvocation: TransformInvocation?) {
        transformInvocation?.inputs?.forEach {
            it.directoryInputs?.forEach { input ->
                val file = input.file
                if (file.isDirectory) {

                }
            }
            it.jarInputs?.forEach {
            }
        }
    }

    private fun insertCode(file: File) {
        println("find class:${file.name}")
        val classReader = ClassReader(file.readBytes())
        val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
        val classVisitor = ActivityClassVisitor(classWriter)
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        val newBytes = classWriter.toByteArray()
        val outputStream = FileOutputStream(file.path)
        outputStream.write(newBytes)
        outputStream.close()
    }
}