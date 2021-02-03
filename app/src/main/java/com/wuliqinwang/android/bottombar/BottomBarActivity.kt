package com.wuliqinwang.android.bottombar

import android.content.Context
import android.os.Bundle
import android.view.ViewGroup
import com.wuliqinwang.act.register.annotation.ActRegister
import com.wuliqinwang.android.R
import com.wuliqinwang.android.common_lib.base.BaseActivity
import com.wuliqinwang.android.databinding.ActivityBottomBarBinding

/**
 * @Description: 底部bar的界面
 */
@ActRegister(name = "底部bar测试")
class BottomBarActivity: BaseActivity<ActivityBottomBarBinding>(){

    override fun ActivityBottomBarBinding.onBindDataForView(savedInstanceState: Bundle?) {
        bottomTabBar.setDefaultTabBar(OnlineBottomBarCreator(arrayListOf(
            BarDataBo(
                "特卖",
                R.mipmap.tab_shop_check_vip,
                R.mipmap.tab_shop_nocheck_vip
            ),
            BarDataBo(
                "超市",
                R.mipmap.tab_cart_check_vip,
                R.mipmap.tab_cart_nocheck_vip
            ),
            BarDataBo(
                "我的",
                R.mipmap.tab_mine_check_vip,
                R.mipmap.tab_mine_nocheck_vip
            )
        )))
        bottomTabBar.setCurrentTab(1)
    }

    class OnlineBottomBarCreator(
        bottomBarData: List<BarDataBo>
    ): IBottomBarCreator {
        private val mBottomBarList by lazy {
            ArrayList<TabItemView.TabItemBuilder>(bottomBarData.size).apply {
                bottomBarData.forEach { tabItem ->
                    add(TabItemView
                        .builder()
                        .setIcon(tabItem.selectedIconUrl, tabItem.unselectedIconUrl)
                        .setTabName(tabItem.name)
                    )
                }
            }
        }
        override fun getTabItemCount(): Int = mBottomBarList.size

        override fun createTabItem(context: Context, index: Int): ViewGroup {
            return mBottomBarList[index].build(context)
        }
    }

    data class BarDataBo(
        var name: String? = null,
        var selectedIconUrl: Any? = null,
        var unselectedIconUrl: Any? = null
    )
}