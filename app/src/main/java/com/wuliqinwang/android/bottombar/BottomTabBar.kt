package com.wuliqinwang.android.bottombar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.wuliqinwang.android.bottombar.tab.AbstractSampleTabView
import java.lang.RuntimeException

/**
 * @Description: 用于封装底部TabBar
 * @Version: 1.0.0
 */
class BottomTabBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defaultStyle: Int = 0
) : ConstraintLayout(context, attrs, defaultStyle), View.OnClickListener{

    // DES: 保存设置的ViewPager2
    private var mViewPager2: ViewPager2? = null
    // DES: 保存tab改变的监听器
    private var mTabChangeListener: OnTabChangeListener? = null

    // DES: 设置ViewPager2
    fun setViewPager2(viewPager: ViewPager2) {
        mViewPager2 = viewPager
        viewPager.registerOnPageChangeCallback(PageChangeListenerImpl())
    }

    // DES: 初始化底部bar
    fun initBottomBar(
        activity: FragmentActivity,
        viewPager: ViewPager2,
        tabCreateAdapter: TabCreateAdapter
    ) {
        val innerAdapter = DefaultAdapterWrapper(
            activity.supportFragmentManager,
            activity.lifecycle,
            tabCreateAdapter
        )
        tabCreateAdapter.tabBar = this
        tabCreateAdapter.fragmentAdapter = innerAdapter
        viewPager.adapter = innerAdapter
        setViewPager2(viewPager)
        setTabs(tabCreateAdapter)
    }

    // DES: 设置Tab的改变监听器
    fun setOnTabChangeListener(tabChangeListener: OnTabChangeListener) {
        mTabChangeListener = tabChangeListener
    }

    // DES: 设置当前选中的tab
    fun setCurrentTab(index: Int) {
        if (index in 0 until childCount && childCount > 0) {
            getChildAt(index)?.apply {
                onClick(this)
            }
        }
    }

    // DES: 通过名字设置当前选中的tab
    fun setCurrentTab(tabName: String) {
        findTabViewByTabName(tabName)?.apply {
            onClick(this)
        }
    }

    // DES: 通过tab名字来获取TabItemView
    fun getTabItemViewByName(tabName: String): TabItemView? {
        return findTabViewByTabName(tabName)
    }

    // DES: 通过tab名字找到tabView以及位置信息
    private fun findTabViewByTabName(
        tabName: String
    ): TabItemView? {
        val childCount = childCount
        var selectTabView: TabItemView? = null
        for (index in 0 until childCount) {
            val tempTabView = getChildAt(index) as TabItemView
            if (tempTabView.dataModel.tabName == tabName) {
                selectTabView = tempTabView
                break
            }
        }
        return selectTabView
    }

    // DES: 通过提供的创建器来初始化tab
    fun setTabs(creator: TabCreator, isInit: Boolean = false) {
        // DES: 更新创建器
        if (creator is TabCreateAdapter && mViewPager2?.adapter is DefaultAdapterWrapper && !isInit) {
            (mViewPager2?.adapter as? DefaultAdapterWrapper)?.tabCreateAdapter = creator
        }
        createBottomBar(creator)
    }

    // DES: 通过位置替换对应的tab
    fun replaceTabByIndex(position: Int, replaceTab: TabItemView) {
        if (position in 0 until childCount) {
            val selectTabView = getChildAt(position) as? TabItemView
            val replaceTabLayoutParams = removeTabView(selectTabView)
            if (replaceTabLayoutParams != null) {
                replaceTab.id = selectTabView!!.id
                replaceTab.tag = selectTabView
                replaceTab.layoutParams = replaceTabLayoutParams
                replaceTab.setOnClickListener(this@BottomTabBar)
                addView(replaceTab, position)
                if (selectTabView.isSelected) {
                    onClick(replaceTab)
                }
            }
        }
    }

    private fun removeTabView(targetTabItemView: TabItemView?): LayoutParams? {
        targetTabItemView ?: return null
        (targetTabItemView.parent as? ViewGroup)?.removeView(targetTabItemView)
        removeView(targetTabItemView)
        return targetTabItemView.layoutParams as? LayoutParams
    }

    // DES: 创建底部Bar根据用户传进来的创建器
    private fun createBottomBar(creator: TabCreator) {
        val tabItemsSize = creator.getItemCount() - 1
        if (tabItemsSize <= 0) {
            throw RuntimeException("The number of bars at the bottom cannot be zero")
        }
        removeAllViews()
        var lastViewId = 0
        var nextViewId = View.generateViewId()
        for(index in 0 until (tabItemsSize + 1)) {
            val tabItemView = creator.createTabItemView(context, index).apply {
                id = nextViewId
                setOnClickListener(this@BottomTabBar)
            }
            nextViewId = View.generateViewId()
            tabItemView.tag = index
            addView(tabItemView, createTabLayoutParams(index, lastViewId, nextViewId, tabItemsSize))
            lastViewId = tabItemView.id
        }
    }

    // DES: 创建Tab的LayoutParams参数
    private fun createTabLayoutParams(
        index: Int,
        lastViewId: Int,
        nextViewId: Int,
        tabItemsSize: Int
    ): LayoutParams {
        return LayoutParams(0, LayoutParams.MATCH_PARENT).apply {
            topToTop = LayoutParams.PARENT_ID
            bottomToBottom = LayoutParams.PARENT_ID
            if (index == 0) {
                leftToLeft = LayoutParams.PARENT_ID
            } else {
                leftToRight = lastViewId
            }
            if (index == tabItemsSize) {
                rightToRight = LayoutParams.PARENT_ID
            } else {
                rightToLeft = nextViewId
            }
            horizontalWeight = 1f
        }
    }

    override fun onClick(clickView: View?) {
        for (index in 0 until childCount) {
            getChildAt(index)?.apply {
                // DES: 将是否被选中的设置提前，目的是可以在点击时候，去临时替换一些Tab的风格之类的
                val selected = this == clickView
                setTabChange(this as TabItemView, selected, index)
                isSelected = selected
            }
        }
    }

    // DES: 设置tab改变
    private fun setTabChange(targetTabItemView: TabItemView, selected: Boolean, index: Int) {
        if (selected) {
            mViewPager2?.currentItem = index
            mTabChangeListener?.onTabSelected(targetTabItemView, index)
        }
        mTabChangeListener?.onTabChange(targetTabItemView, index, selected)
    }

    // DES: 默认内部实现适配包装类
    class DefaultAdapterWrapper(
        fragmentManager: FragmentManager,
        lifecycle: Lifecycle,
        var tabCreateAdapter: TabCreateAdapter
    ): FragmentStateAdapter(fragmentManager, lifecycle) {

        override fun getItemCount() = tabCreateAdapter.getItemCount()

        override fun createFragment(position: Int): Fragment {
            val fragment = tabCreateAdapter.getFragment(position)
            return fragment ?: throw RuntimeException("Fragments in $position locations are null")
        }

        override fun getItemId(position: Int) = tabCreateAdapter.getFragment(position).hashCode().toLong()
    }

    // DES: 用于兼容ViewPager2以及ViewPager页面改变监听实现类
    inner class PageChangeListenerImpl: ViewPager2.OnPageChangeCallback(){
        override fun onPageSelected(position: Int) {
            setCurrentTab(position)
        }
    }

    // DES: tab改变监听器接口
    interface OnTabChangeListener {
        // DES: 选中改变的方法
        fun onTabSelected(itemView: TabItemView, position: Int)
        // DES: 发生改变的tab
        fun onTabChange(itemView: TabItemView, position: Int, selected: Boolean)
    }

    // DES: 用于tab的创建器
    interface TabCreator {
        // DES: 返回TabItem的数量
        fun getItemCount(): Int

        // DES: 创建TabItem
        fun createTabItemView(context: Context, index: Int): TabItemView
    }

    // DES: 用于创建BottomBar的抽象接口
    interface TabCreateAdapter: TabCreator {
        // DES: 碎片集合
        var fragmentList: MutableList<Fragment>
        // DES: tabBar控制器
        var tabBar: BottomTabBar?
        // DES: 碎片适配器
        var fragmentAdapter: FragmentStateAdapter?

        // DES: 创建TabItem
        override fun createTabItemView(context: Context, index: Int): TabItemView {
            val fragment = onCreateFragment(context, index)
            if (fragment != null) {
                fragmentList.add(fragment)
            }
            return onBuildTabItemView(context, index)
        }

        /** DES: 构建tabItem对应的视图 */
        fun onBuildTabItemView(context: Context, index: Int): TabItemView

        /** DES: 创建对应的碎片 */
        fun onCreateFragment(context: Context, index: Int): Fragment?

        /** DES: 获取碎片 */
        fun getFragment(position: Int): Fragment? {
            return if (position >= 0 && position < getItemCount()) {
                fragmentList[position]
            } else {
                null
            }
        }

        // DES: 通过索引替换碎片某个位置上的碎片
        fun replaceTabItemByIndex(position: Int, newFragment: Fragment, tabItemView: TabItemView) {
            if (position >= 0 && position < getItemCount()) {
                fragmentList.removeAt(position)
                fragmentList.add(position, newFragment)
                tabBar?.replaceTabByIndex(position, tabItemView)
                fragmentAdapter?.notifyDataSetChanged()
            }
        }

        /** DES: 移除一个tab */
        fun removeTab(position: Int) {
            if (position >= 0 && position < getItemCount()) {
                fragmentList.removeAt(position)
                tabBar?.removeViewAt(position)
                fragmentAdapter?.notifyDataSetChanged()
            }
        }

        // DES: 移除所有的tab
        fun removeAllTabs() {
            fragmentList.clear()
            tabBar?.removeAllViews()
            fragmentAdapter?.notifyDataSetChanged()
        }

        // DES: 通过名字找tab视图
        fun findTabViewByName(name: String): TabItemView? {
            return tabBar?.findTabViewByTabName(name)
        }

        // DES: 通过索引找tab
        fun findTabViewByIndex(position: Int): TabItemView? {
            return if (position >= 0 && position < (tabBar?.childCount ?: 0)) {
                tabBar?.getChildAt(position) as? TabItemView
            } else {
                null
            }
        }
    }

    // DES: 抽象出一个实现了创建Tab的适配器，给外部实现所有
    abstract class AbstractTabCreateAdapter<T>(
        private var dataList: MutableList<T>? = null
    ): TabCreateAdapter {

        override var fragmentList: MutableList<Fragment> = arrayListOf()

        override var tabBar: BottomTabBar? = null

        override var fragmentAdapter: FragmentStateAdapter? = null

        override fun getItemCount() = dataList?.size ?: 0

        override fun onBuildTabItemView(context: Context, index: Int): TabItemView {
            return if (index >= 0 && index < (dataList?.size ?: 0)) {
                dataList!![index].onBuildTabItem(context, index)
            } else {
                throw RuntimeException("The external data does not match the internal data size")
            }
        }

        // DES: 重新设置数据
        fun setTabs(dataSet: MutableList<T>?, selectedIndex: Int = 0) {
            if (dataSet?.isNotEmpty() == true) {
                dataList = dataSet
                fragmentList.clear()
                tabBar?.setTabs(this)
                fragmentAdapter?.notifyDataSetChanged()
                tabBar?.setCurrentTab(selectedIndex)
            }
        }

        // DES: 构建tabItem
        abstract fun T.onBuildTabItem(context: Context, index: Int): TabItemView
    }

    // DES: 默认TabItem实现类
    class DefaultTabView: AbstractSampleTabView() {

        override fun getTabIconViewId(): Int = 0

        override fun getTabNameViewId(): Int = 0

        override fun getTabLayoutId(): Int = 0
    }
    
    // DES: OnTabChangeListener默认实现
    abstract class TabChangeListenerAdapter: OnTabChangeListener {
        override fun onTabSelected(itemView: TabItemView, position: Int) {
        }

        override fun onTabChange(itemView: TabItemView, position: Int, selected: Boolean) {
        }
    }
}