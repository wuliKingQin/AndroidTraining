package com.wuliqinwang.android.bottombar

import android.content.Context
import android.view.ViewGroup

/**
 * @Version: 1.0.0
 */
interface IBottomBarCreator{

    // DES: 返回TabItem的数量
    fun getTabItemCount(): Int

    // DES: 创建TabItem视图
    fun createTabItem(context: Context, index: Int): ViewGroup
}