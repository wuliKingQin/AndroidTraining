package com.wuliqinwang.android.mvvm

import android.graphics.Color
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.wuliqinwang.android.R

class MvvmViewModel: ViewModel(){
    var userInfo = MutableLiveData<DataModel>()

    init {
        userInfo.value = DataModel("999999999")
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