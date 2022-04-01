package com.wuliqinwang.patch.asm

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class ActivityMethodVisitor(
    methodVisitor: MethodVisitor,
    private var className: String,
    private var methodName: String
) : MethodVisitor(Opcodes.ASM7, methodVisitor) {
    override fun visitCode() {
        super.visitCode()
        println("ActivityMethodVisitor==================visitCode")
        mv.visitLdcInsn("TAG")
        mv.visitLdcInsn("$className---->$methodName")
        mv.visitMethodInsn(
            Opcodes.INVOKESTATIC,
            "android/util/Log",
            "i",
            "(Ljava/lang/String;Ljava/lang/String;)I",
            false
        )
        mv.visitInsn(Opcodes.POP)
    }
}