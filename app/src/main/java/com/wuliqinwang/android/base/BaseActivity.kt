package com.wuliqinwang.android.base

import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
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
        return ((javaClass.genericSuperclass as? ParameterizedType)
            ?.actualTypeArguments?.get(0) as? Class<*>)
            ?.getDeclaredMethod("inflate", LayoutInflater::class.java)?.let {
                it.isAccessible = true
                it.invoke(null, layoutInflater)
            } as? T
    }

    // DES: 为视图绑定数据
    abstract fun T.onBindDataForView(savedInstanceState: Bundle?)
}