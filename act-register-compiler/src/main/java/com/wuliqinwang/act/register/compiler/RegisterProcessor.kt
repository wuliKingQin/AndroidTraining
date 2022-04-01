package com.wuliqinwang.act.register.compiler

import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import com.wuliqinwang.act.register.annotation.ActRegister
import org.apache.commons.collections4.CollectionUtils
import java.util.*
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.element.Element
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap

/**
 * @Author: 秦王
 */
@AutoService(Processor::class)
open class RegisterProcessor: BaseProcessor(){

    private val mRegisterActivitiesMap by lazy {
        ArrayList<ActRegisterBo>()
    }

    override fun getModuleNameKey(): String = "REGISTER_MODULE_NAME"

    override fun process(annotations: MutableSet<out TypeElement>?, roundEnvironment: RoundEnvironment?): Boolean {
        if (CollectionUtils.isNotEmpty(annotations)) {
            val registers = roundEnvironment?.getElementsAnnotatedWith(ActRegister::class.java)
            if (CollectionUtils.isNotEmpty(registers)) {
                logI("===========start act register============")
                return try {
                    parseRegister(registers!!)
                    true
                } catch (e: Exception) {
                    e.printStackTrace()
                    false
                }
            }
        }
        return false
    }

    // DES: 解析注解器生成代码
    private fun parseRegister(registers: Set<Element>) {
        logI("===========Found Activity size: ${registers.size}============")
        val typeParam = ParameterizedTypeName.get(LinkedHashMap::class.java, String::class.java, Class::class.java)
        val mActivityMapsField = FieldSpec.builder(typeParam, "mActivityMaps")
            .addModifiers(Modifier.PRIVATE, Modifier.FINAL)
            .initializer("new \$T()", typeParam)
            .build()
        val getActivitiesMethod = MethodSpec.methodBuilder("getActivities")
            .addModifiers(Modifier.PUBLIC)
            .returns(typeParam)
            .addStatement("return mActivityMaps")
            .build()
        val activityType = typeUtils.getElementType(Constants.ACTIVITY)
        mRegisterActivitiesMap.clear()
        registers.forEach { element ->
            val elementType = element.asType()
            val actRegister = element.getAnnotation<ActRegister>(ActRegister::class.java)
            if (typeUtils.isSubtype(elementType, activityType)) {
                mRegisterActivitiesMap.add(
                    ActRegisterBo(
                        actRegister,
                        element
                    )
                )
            }
        }
        val statement = MethodSpec.constructorBuilder()
            .addModifiers(Modifier.PUBLIC)
        mRegisterActivitiesMap.sort()
        mRegisterActivitiesMap.forEach {
            val key = if (it.actRegister.name.isEmpty()) it.targetElement.toString() else it.actRegister.name
            val targetElement = ClassName.get(it.targetElement as TypeElement)
            statement.addStatement("mActivityMaps.put(\$S, \$T.class)", key, targetElement)
        }
        val activitiesFactoryClass = TypeSpec.classBuilder("ActivitiesFactory")
            .addModifiers(Modifier.PUBLIC)
            .addMethod(statement.build())
            .addField(mActivityMapsField)
            .addMethod(getActivitiesMethod)
            .build()
        JavaFile.builder(Constants.ACT_REGISTER_PACKAGE_NAME, activitiesFactoryClass)
            .build()
            .writeTo(targetFiler)
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return hashSetOf(
            ActRegister::class.java.canonicalName
        )
    }
    
    // DES: 注解信息
    data class ActRegisterBo(
        val actRegister: ActRegister,
        val targetElement: Element
    ): Comparable<ActRegisterBo>{
        override fun compareTo(other: ActRegisterBo): Int {
            return if (actRegister.position == other.actRegister.position) {
                actRegister.name.compareTo(other.actRegister.name)
            } else {
                actRegister.position - other.actRegister.position
            }
        }
    }
}