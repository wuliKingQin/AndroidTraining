package com.wuliqinwang.act.register.compiler

import org.apache.commons.collections4.MapUtils
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Filer
import javax.annotation.processing.ProcessingEnvironment
import javax.lang.model.SourceVersion

/**
 * @author: wuli秦王
 */
abstract class BaseProcessor : AbstractProcessor() {

    // DES: 用于写入文件
    protected lateinit var targetFiler: Filer

    // DES: 写日志
    private var mLogger: Logger? = null

    // DES: 类型工具类
    protected lateinit var typeUtils: TypeUtils
        private set

    // DES: 保存模块名
    private var mModuleName: String? = null

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        processingEnv ?: return
        mLogger = Logger(processingEnv.messager)
        targetFiler = processingEnv.filer
        typeUtils = TypeUtils(processingEnv.typeUtils, processingEnv.elementUtils)
        if (MapUtils.isNotEmpty(processingEnv.options)) {
            mModuleName = processingEnv.options[getModuleNameKey()]
        }
    }

    protected fun logE(error: Any?) {
        if (error is Throwable) {
            mLogger?.error(error)
        } else {
            mLogger?.error(error.toString())
        }
    }

    protected fun logW(wInfo: CharSequence) {
        mLogger?.warning(wInfo)
    }

    protected fun logI(info: CharSequence) {
        mLogger?.info(info)
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    // DES: 获取模块名的key, 该key用于获取工程名
    abstract fun getModuleNameKey(): String
}