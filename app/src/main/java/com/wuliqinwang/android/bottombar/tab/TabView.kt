package com.wuliqinwang.android.bottombar.tab

import com.wuliqinwang.android.bottombar.TabItemView

interface TabView: TouchEvent{
    // DES: 初始化方法
    fun initializer(model: TabItemView.TabItemModel, itemContainer: TabItemView)
    // DES: 设置选中状态
    fun setSelected(selected: Boolean)
}