package com.wuliqinwang.android.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import com.wuliqinwang.android.getMethodEx
import java.lang.reflect.ParameterizedType

/**
 * @Description: 所有界面的基类
 */
abstract class BaseActivity<T : ViewBinding> : AppCompatActivity() {

    // DES: 保存获取视图对象
    lateinit var viewHolder: T
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewHolder = instanceViewBinding()!!
        setContentView(viewHolder.root)
        viewHolder.onBindDataForView(savedInstanceState)
    }

    // DES: 实例化ViewBinding
    @Suppress("UNCHECKED_CAST")
    private fun instanceViewBinding(): T? {
        return findTargetClass(javaClass, ViewBinding::class.java)
            .getMethodEx("inflate", LayoutInflater::class.java)
            ?.invoke(null, layoutInflater) as? T
    }

    // 找目标ViewBinding的class，从范型里面
    private fun findTargetClass(findClass: Class<*>?, targetClass: Class<*>): Class<*>? {
        return (findClass?.genericSuperclass as? ParameterizedType)?.actualTypeArguments?.let {
            var targetClassEx: Class<*>? = null
            for (type in it) {
                val tempClass = type as? Class<*>
                (tempClass)?.genericInterfaces?.let { inters ->
                    for(inter in inters) {
                        if (inter == targetClass) {
                            targetClassEx = tempClass
                            break
                        }
                    }
                }
                if (targetClassEx != null) {
                    break
                }
            }
            targetClassEx ?: findTargetClass(findClass.superclass, targetClass)
        }
    }

    // DES: 为视图绑定数据
    abstract fun T.onBindDataForView(savedInstanceState: Bundle?)
}