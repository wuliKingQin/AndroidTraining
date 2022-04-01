package com.wuliqinwang.android.gson

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.wuliqinwang.act.register.annotation.ActRegister
import com.wuliqinwang.android.common_lib.base.BaseActivity
import com.wuliqinwang.android.databinding.ActivityGsonAndSpTestBinding


/**
 * @Description: 用于测试GSON和SP存储，看是否又异常发生
 * @CreateDate: 2021/11/2 13:43
 * @Version: 1.0.0
 */
@ActRegister(name = "GSON和SP异常测试")
class GSONAndSPTestActivity : BaseActivity<ActivityGsonAndSpTestBinding>() {

    private val data by lazy {
        """{
  "data": {
    "agreementId": 18,
    "content": "为更好保障您的合法权益，我们依据最新的监管要求对《云集隐私政策》进行了更新。此版本《云集隐私政策》的更新主要集中在，对【个人信息】中的【个人网络信息】和【个人常用设备信息】的内容进行了调整，增加了您在使用云集的产品及/或服务时可能涉及的极光SDK的相关描述，进一步细化了用户信息收集、使用和共享的范围，并向您明示了云集APP的开发者主体身份。\n【特别提示】在使用云集产品及/或服务前，请仔细阅读《云集隐私政策》（尤其是以粗体下划线标识的条款）并确定了解我们对您个人信息的处理规则。阅读过程中，如您有任何疑问，可通过《云集隐私政策》中指定的方式与我们联系并进行咨询。如您不同意更新后的《云集隐私政策》，您可能无法享受我们提供的新的产品或服务。\n感谢您对云集的支持与关注！",
    "display": 1,
    "effectiveTime": 1628784000000,
    "endTime": 1628783999000,
    "isNotify": 1,
    "isRemind": 1,
    "isShow": 1,
    "jumpLink": "https://a.yj.ink/MxZvi2",
    "modifyTime": 1628132735385,
    "startTime": 1628092800000,
    "title": "《云集隐私政策》更新",
    "weight": 1
  },
  "errorCode": 0,
  "errorMessage": ""
}"""
    }

    private val handler by lazy {
        Handler(Looper.getMainLooper())
    }

    private val gson by lazy {
        Gson()
    }

    private val dataBean by lazy {
        gson.fromJson(data, TestJsonBean::class.java)
    }

    private val mSp by lazy {
        getSharedPreferences("test_gson_and_sp", Context.MODE_PRIVATE)
    }

    override fun ActivityGsonAndSpTestBinding.onBindDataForView(savedInstanceState: Bundle?) {
        val thread = TestThread()
        startTestBtn.setOnClickListener {
            if (!thread.isAlive) {
                thread.start()
            }
        }
    }

    inner class TestThread: Thread(){
        override fun run() {
            var index = 0
            showLog("开始put测试", false)
            while (index < 1000000) {
                executePutTask(index)
                index ++
            }
            index = 0
            showLog("\n结束put测试\n开始get测试", false)
            while (index < 1000000) {
                executeGetTask(index)
                index ++
            }
            showLog("\n结束get测试", false)
        }

        private fun executeGetTask(index: Int) {
            var data: String? = null
            tryCatch("第${index}次sp get操作") {
                data = mSp.getString("test_k", "")
            }
            tryCatch("第${index}次gson 解析操作") {
                val hashMap = HashMap<String, TestJsonBean>()
                val jsonObject = JsonParser().parse(data).asJsonObject
                val entrySet: Set<Map.Entry<String, JsonElement?>> = jsonObject.entrySet()
                for ((entryKey, value1) in entrySet) {
                    hashMap[entryKey] = gson.fromJson(value1, TestJsonBean::class.java)
                }
                showLog("\nhashMap size=${hashMap.size}")
            }
        }

        @SuppressLint("ApplySharedPref")
        private fun executePutTask(index: Int) {
            var putValue: String? = null
            tryCatch("第${index}次json对象转换操作") {
                val map = HashMap<Int, TestJsonBean>().apply {
                    put(0, dataBean)
                }
                putValue = gson.toJson(map)
            }
            tryCatch("第${index}次sp commit操作") {
                mSp.edit()
                    .putString("test_k", putValue)
                    .commit()
            }
        }

        private fun tryCatch(key: String, action: ()-> Unit) {
            var isFilter = true
            var errorInfo = "\n执行${key}成功!}"
            try {
                action()
            } catch (e: Exception) {
                isFilter = false
                errorInfo = "\n===========================执行${key}异常:\n${e.message.toString()}==========================="
            }
            showLog(errorInfo, isFilter)
        }

        private fun showLog(info: String, filter: Boolean=true) {
            if (filter) {
                return
            }
            handler.post {
                viewHolder.showTextInfoTv.apply {
                    append(info)
                }
            }
        }
    }

    data class TestJsonBean(
        @SerializedName("data")
        var `data`: Data?,
        @SerializedName("errorCode")
        var errorCode: Int?, // 0
        @SerializedName("errorMessage")
        var errorMessage: String?
    ) {
        data class Data(
            @SerializedName("agreementId")
            var agreementId: Int?, // 18
            @SerializedName("content")
            var content: String?, // 为更好保障您的合法权益，我们依据最新的监管要求对《云集隐私政策》进行了更新。此版本《云集隐私政策》的更新主要集中在，对【个人信息】中的【个人网络信息】和【个人常用设备信息】的内容进行了调整，增加了您在使用云集的产品及/或服务时可能涉及的极光SDK的相关描述，进一步细化了用户信息收集、使用和共享的范围，并向您明示了云集APP的开发者主体身份。【特别提示】在使用云集产品及/或服务前，请仔细阅读《云集隐私政策》（尤其是以粗体下划线标识的条款）并确定了解我们对您个人信息的处理规则。阅读过程中，如您有任何疑问，可通过《云集隐私政策》中指定的方式与我们联系并进行咨询。如您不同意更新后的《云集隐私政策》，您可能无法享受我们提供的新的产品或服务。感谢您对云集的支持与关注！
            @SerializedName("display")
            var display: Int?, // 1
            @SerializedName("effectiveTime")
            var effectiveTime: Long?, // 1628784000000
            @SerializedName("endTime")
            var endTime: Long?, // 1628783999000
            @SerializedName("isNotify")
            var isNotify: Int?, // 1
            @SerializedName("isRemind")
            var isRemind: Int?, // 1
            @SerializedName("isShow")
            var isShow: Int?, // 1
            @SerializedName("jumpLink")
            var jumpLink: String?, // https://a.yj.ink/MxZvi2
            @SerializedName("modifyTime")
            var modifyTime: Long?, // 1628132735385
            @SerializedName("startTime")
            var startTime: Long?, // 1628092800000
            @SerializedName("title")
            var title: String?, // 《云集隐私政策》更新
            @SerializedName("weight")
            var weight: Int? // 1
        )
    }
}