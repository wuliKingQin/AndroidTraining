package com.wuliqinwang.patch.asm

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class ActivityClassVisitor(classVisitor: ClassVisitor): ClassVisitor(Opcodes.ASM7, classVisitor){

    private var className: String? = null
    private var superName: String? = null

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        className = name
        this.superName = superName
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {
        val methodVisitor = cv.visitMethod(access, name, descriptor, signature, exceptions)
        return if (superName == "android/app/Activity") {
            if (name == "onCreate") {
                ActivityMethodVisitor(methodVisitor, className!!, name)
            } else {
                methodVisitor
            }
        } else {
            methodVisitor
        }
    }
}