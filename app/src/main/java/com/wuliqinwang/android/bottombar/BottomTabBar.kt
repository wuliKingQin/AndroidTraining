package com.wuliqinwang.android.bottombar

import android.content.Context
import android.text.TextUtils
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.Space
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.get
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.wuliqinwang.android.bottombar.tab.AbstractTabView
import com.wuliqinwang.android.bottombar.tab.IconLoader
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

    fun setViewPager(viewPager: ViewPager) {
    }

    fun setViewPager2(viewPager: ViewPager2) {
    }

    fun setTabItemClickListener() {

    }

    // DES: 设置当前选中的tab
    fun setCurrentTab(index: Int) {
        if (index in 1 until childCount) {
            get(index).isSelected = true
        }
    }

    fun setCurrentTab(tabName: String) {

    }

    fun getTabItemView(index: Int): View? {
        return null
    }

    fun getTabItemViewByName(tabName: String): View? {
        return null
    }

    fun setIconLoader() {

    }

    fun setDefaultTabBar(creator: IBottomBarCreator) {
        val tabItemsSize = creator.getTabItemCount() - 1
        if (tabItemsSize <= 0) {
            throw RuntimeException("The number of bars at the bottom cannot be zero")
        }
        removeAllViews()
        var lastViewId = 0
        var nextViewId = View.generateViewId()
        for(index in 0 until (tabItemsSize + 1)) {
            val tabItemView = creator.createTabItem(context, index)
            tabItemView.tag = index
            tabItemView.id = nextViewId
            nextViewId = View.generateViewId()
            val params = LayoutParams(0, LayoutParams.MATCH_PARENT).apply {
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
            tabItemView.setOnClickListener(this)
            addView(tabItemView, params)
            lastViewId = tabItemView.id
        }
    }

    override fun onClick(clickView: View?) {
        for (index in 0 until childCount) {
            get(index).apply {
                isSelected = (this == clickView)
            }
        }
    }

    // DES: 默认TabItem实现类
    class DefaultTabItemView(
        iconLoader: IconLoader? = null
    ): AbstractTabView(iconLoader) {

        // tab的图标
        private lateinit var mTabIconIv: ImageView

        // DES: tab的名字
        private lateinit var mTabNameTv: TextView

        // DES: 透出Space
        private lateinit var mGiveSpace: Space

        override fun createTabView(context: Context) {
            mTabIconIv = ImageView(context).apply {
                id = View.generateViewId()
                setIconStatus(this, this@DefaultTabItemView.isSelected())
            }
            mTabNameTv = TextView(context).apply {
                id = View.generateViewId()
                text = model.tabName
                textSize = model.fontSize
                gravity = Gravity.CENTER
                ellipsize = TextUtils.TruncateAt.END
                setNameStatus(this, this@DefaultTabItemView.isSelected())
            }
            mGiveSpace = Space(context).apply {
                id = View.generateViewId()
                layoutParams = LayoutParams(
                    LayoutParams.WRAP_CONTENT,
                    LayoutParams.WRAP_CONTENT
                ).apply {
                    bottomToTop = LayoutParams.PARENT_ID
                    leftToLeft = LayoutParams.PARENT_ID
                    if (model.isGive) {
                        bottomMargin = model.giveSize.toInt()
                    }
                }
                addView(this)
            }
            addTabView()
        }

        private fun addTabView() {
            val iconWidth = if (model.isGive) {
                0
            } else {
                model.iconWidth
            }
            addView(mTabIconIv, LayoutParams(iconWidth, model.iconHeight).apply {
                topToBottom = mGiveSpace.id
                leftToLeft = LayoutParams.PARENT_ID
                rightToRight = LayoutParams.PARENT_ID
                verticalChainStyle = LayoutParams.CHAIN_PACKED
                bottomMargin = model.iconToNameInterval
                if (!model.isGive) {
                    bottomToTop = mTabNameTv.id
                }
            })
            if (!model.isGive) {
                addView(mTabNameTv, LayoutParams(
                    0,
                    LayoutParams.WRAP_CONTENT
                ).apply {
                    topToBottom = mTabIconIv.id
                    leftToLeft = LayoutParams.PARENT_ID
                    rightToRight = LayoutParams.PARENT_ID
                    bottomToBottom = LayoutParams.PARENT_ID
                })
            }
        }

        override fun setSelected(selected: Boolean) {
            setIconStatus(mTabIconIv, selected)
            if (!model.isGive) {
                setNameStatus(mTabNameTv, selected)
            }
        }
    }
}