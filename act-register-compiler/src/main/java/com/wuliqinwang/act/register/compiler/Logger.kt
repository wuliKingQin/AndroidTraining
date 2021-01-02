package com.wuliqinwang.act.register.compiler

import org.apache.commons.lang3.StringUtils
import javax.annotation.processing.Messager
import javax.tools.Diagnostic

/**
 * @Author: 秦王
 */
class Logger(private val msg: Messager?) {
    /**
     * Print info log.
     */
    fun info(info: CharSequence) {
        if (StringUtils.isNotEmpty(info)) {
            msg?.printMessage(Diagnostic.Kind.NOTE, info)
        }
    }

    fun error(error: CharSequence) {
        if (StringUtils.isNotEmpty(error)) {
            msg!!.printMessage(Diagnostic.Kind.ERROR,
                "An exception is encountered, [${error}]"
            )
        }
    }

    fun error(error: Throwable?) {
        if (null != error) {
            msg!!.printMessage(
                Diagnostic.Kind.ERROR,
                "An exception is encountered, [${error.message}]\n${formatStackTrace(error.stackTrace)}"
            )
        }
    }

    fun warning(warning: CharSequence) {
        if (StringUtils.isNotEmpty(warning)) {
            msg!!.printMessage(
                Diagnostic.Kind.WARNING,
                warning
            )
        }
    }

    private fun formatStackTrace(stackTrace: Array<StackTraceElement>): String {
        val sb = StringBuilder()
        for (element in stackTrace) {
            sb.append("    at ").append(element.toString())
            sb.append("\n")
        }
        return sb.toString()
    }

}