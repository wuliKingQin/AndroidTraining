package com.wuliqinwang.android.bottombar

import android.content.Context
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.wuliqinwang.act.register.annotation.ActRegister
import com.wuliqinwang.android.R
import com.wuliqinwang.android.bottombar.tab.AbstractSampleTabView
import com.wuliqinwang.android.bottombar.tab.IconLoader
import com.wuliqinwang.android.common_lib.base.BaseActivity
import com.wuliqinwang.android.databinding.ActivityBottomBarBinding

/**
 * @Description: 底部bar的界面
 */
@ActRegister(name = "底部bar测试")
class BottomBarActivity: BaseActivity<ActivityBottomBarBinding>(){

    companion object {
        fun dp2px(dpValue: Float): Int {
            val scale =
                Resources.getSystem().displayMetrics.density
            return (dpValue * scale + 0.5f).toInt()
        }
    }

    override fun onResume() {
        super.onResume()
        val startTime = System.currentTimeMillis()
        while (true) {
            if (System.currentTimeMillis() - startTime > 5000) {
                break
            }
        }
    }

    override fun ActivityBottomBarBinding.onBindDataForView(savedInstanceState: Bundle?) {
        pageContent.isUserInputEnabled = false
        val on = OnlineBottomBarCreator(arrayListOf(
            BarDataBo(
                "特卖",
                R.mipmap.tab_shop_check_vip,
                R.mipmap.tab_shop_nocheck_vip
            ),
            BarDataBo(
                "超市",
                R.mipmap.icon_bar_infant_mom_check,
                R.mipmap.icon_bar_infant_mom_check
            ),
            BarDataBo(
                "我的",
                R.mipmap.tab_mine_check_vip,
                R.mipmap.tab_mine_nocheck_vip
            )
        ))
        bottomTabBar.initBottomBar(this@BottomBarActivity, pageContent, on)
        bottomTabBar.setCurrentTab(1)
        var isf = false
        changeBtn.setOnClickListener {
            Thread(Runnable {
                changeBtn.setBackgroundColor(Color.GRAY)
                changeBtn.requestLayout()
            }).start()
//            on.setTabs(if (isf) {
//                isf = false
//                arrayListOf(
//                    BarDataBo(
//                        "特卖",
//                        R.mipmap.tab_shop_check_vip,
//                        R.mipmap.tab_shop_nocheck_vip
//                    ),
//                    BarDataBo(
//                        "超市",
//                        R.mipmap.icon_bar_infant_mom_check,
//                        R.mipmap.icon_bar_infant_mom_check
//                    ),
//                    BarDataBo(
//                        "我的",
//                        R.mipmap.tab_mine_check_vip,
//                        R.mipmap.tab_mine_nocheck_vip
//                    )
//                )
//            } else {
//                isf = true
//                arrayListOf(
//                    BarDataBo(
//                        "9999",
//                        R.mipmap.tab_shop_check_vip,
//                        R.mipmap.tab_shop_nocheck_vip
//                    ),
//                    BarDataBo(
//                        "超市",
//                        R.mipmap.tab_cart_check_vip,
//                        R.mipmap.tab_cart_nocheck_vip
//                    )
//                )
//            }, 1)
        }
    }

    class OnlineBottomBarCreator(
        bottomBarData: MutableList<BarDataBo>
    ): BottomTabBar.AbstractTabCreateAdapter<BarDataBo>(bottomBarData), IconLoader{

        override fun onLoading(position: Int, iconView: ImageView, iconModel: Any?) {
            if (iconModel is Int) {
                iconView.setImageResource(iconModel)
            }
        }

        override fun onCreateFragment(context: Context, index: Int): Fragment? {
            return PageFragment(index)
        }

        override fun BarDataBo.onBuildTabItem(context: Context, index: Int): TabItemView {
            val isBumpModel = index == 1
            val builder = TabItemView
                .builder()
                .setIcon(selectedIconUrl, unselectedIconUrl)
                .setTabName(name)
                .setIconSize(dp2px(64f), if (isBumpModel) dp2px(64f) else dp2px(32f))
                .setGiveModel(index == 1, dp2px(17f).toFloat())
                .setTabView(MyTabItemView())
                .setIconLoader(this@OnlineBottomBarCreator)
            return builder.build(context, index)
        }

        inner class MyTabItemView: AbstractSampleTabView() {
            override fun getTabIconViewId() = R.id.icon_iv
            override fun getTabNameViewId() = R.id.tab_name_tv
            override fun getTabLayoutId() = R.layout.view_tab_item

            override fun createTabView(context: Context) {
                super.createTabView(context)
            }
        }
    }

    data class BarDataBo(
        var name: String? = null,
        var selectedIconUrl: Any? = null,
        var unselectedIconUrl: Any? = null
    )

    // DES: 碎片适配器
    class FragmentAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

        private val mFragments = ArrayList<PageFragment>()

        override fun getItemCount(): Int = mFragments.size

        init {
            mFragments.add(PageFragment(0))
            mFragments.add(PageFragment(1))
            mFragments.add(PageFragment(2))
        }

        override fun createFragment(position: Int): Fragment {
            return mFragments[position]
        }

        override fun getItemId(position: Int): Long {
            return mFragments[position].itemId.toLong()
        }

        fun replaceFragment(index: Int, fragment: PageFragment) {
            mFragments.removeAt(index)
            mFragments.add(index, fragment)
        }
    }

    class PageFragment(var itemId: Int): Fragment() {



        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            return inflater.inflate(R.layout.fragment_view, container).apply {
                findViewById<TextView>(R.id.text_tv)?.apply {
                    text = "第一页面=${itemId}"
                    setTextColor(Color.BLACK)
                }
            }
        }
    }
}