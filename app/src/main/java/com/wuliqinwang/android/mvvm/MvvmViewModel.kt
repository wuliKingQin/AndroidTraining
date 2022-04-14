package com.wuliqinwang.android.mvvm

import android.graphics.Color
import android.os.Handler
import android.os.Looper
import androidx.databinding.ObservableField
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wuliqinwang.android.R

class MvvmViewModel: ViewModel(){
    var userInfo = MutableLiveData<DataModel>()

    var progress = ObservableField(0)
    var bubbleText = ObservableField("")
    private val uHandler by lazy {
        Handler(Looper.getMainLooper())
    }

    init {
        userInfo.value = DataModel("999999999")
        uHandler.postDelayed(object :Runnable {
            override fun run() {
                val currProgress = progress.get() ?: 0
                if (currProgress < 20) {
                    bubbleText.set("${currProgress + 1}人已领取, 还有${20 - currProgress - 1}人未领取")
                    progress.set(currProgress + 1)
                    uHandler.postDelayed(this, 1000)
                }
            }
        }, 1000)
    }

    fun random() {
        userInfo.value?.apply {
            name = "232323232323"
            headUrl = getImageUrl()
        }
        userInfo.postValue(userInfo.value)
    }

    fun getImageUrl(): String {
        return "https://gimg2.baidu.com/image_search/src=http%3A%2F%2Fimg.jj20.com%2Fup%2Fallimg%2Ftp09%2F210611094Q512b-0-lp.jpg&refer=http%3A%2F%2Fimg.jj20.com&app=2002&size=f9999,10000&q=a80&n=0&g=0n&fmt=auto?sec=1651042243&t=b15323ceba4592677b37261d06d5bf66"
    }

    fun getImageColor(): Int {
        return Color.BLUE
    }

    fun getImageResId(): Int {
        return R.mipmap.ic_launcher
    }
}