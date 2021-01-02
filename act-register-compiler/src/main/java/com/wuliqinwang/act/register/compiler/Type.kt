package com.wuliqinwang.act.register.compiler

import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.TypeMirror
import javax.lang.model.util.Elements
import javax.lang.model.util.Types

/**
 * @Author: 秦王
 */


enum class TypeKind {
    // Base type
    BOOLEAN, BYTE, SHORT, INT, LONG, CHAR, FLOAT, DOUBLE,  // Other type
    STRING, SERIALIZABLE, PARCELABLE, OBJECT
}


class TypeUtils(
    private val types: Types,
    private val elements: Elements
){
    private val mParcelableType = elements.getTypeElement(Constants.PARCELABLE).asType()
    private val mSerializableType = elements.getTypeElement(Constants.SERIALIZABLE).asType()

    fun isSubtype(type1: TypeMirror?, type2: TypeMirror?): Boolean{
        return types.isSubtype(type1, type2)
    }

    fun getTypeElement(var1: CharSequence?): TypeElement? {
        return elements.getTypeElement(var1)
    }

    fun getElementType(var1: CharSequence?): TypeMirror? {
        return elements.getTypeElement(var1)?.asType()
    }

    /**
     * Diagnostics out the true java type
     *
     * @param element Raw type
     * @return Type class of java
     */
    fun typeExchange(element: Element): Int {
        val typeMirror = element.asType()
        // Primitive
        return if (typeMirror.kind.isPrimitive) {
            element.asType().kind.ordinal
        } else when (typeMirror.toString()) {
            Constants.BYTE -> TypeKind.BYTE.ordinal
            Constants.SHORT -> TypeKind.SHORT.ordinal
            Constants.INTEGER -> TypeKind.INT.ordinal
            Constants.LONG -> TypeKind.LONG.ordinal
            Constants.FLOAT -> TypeKind.FLOAT.ordinal
            Constants.DOUBEL -> TypeKind.DOUBLE.ordinal
            Constants.BOOLEAN -> TypeKind.BOOLEAN.ordinal
            Constants.CHAR -> TypeKind.CHAR.ordinal
            Constants.STRING -> TypeKind.STRING.ordinal
            else ->
                when {
                    types.isSubtype(typeMirror, mParcelableType) -> TypeKind.PARCELABLE.ordinal
                    types.isSubtype(typeMirror, mSerializableType) -> TypeKind.SERIALIZABLE.ordinal
                    else -> TypeKind.OBJECT.ordinal
                }
        }
    }
}