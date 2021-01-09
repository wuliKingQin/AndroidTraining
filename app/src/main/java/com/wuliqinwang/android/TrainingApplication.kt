package com.wuliqinwang.android

import android.app.Application
import android.content.Context
import com.wuliqinwang.hotfix.HotFixUtils
import java.io.File

// DES: 应用程序入口
class TrainingApplication: Application() {
    override fun onCreate() {
        super.onCreate()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        base ?: return
        // 热修复配置
        val hotFixDir = base.getDir("patch", Context.MODE_PRIVATE)
        val hotFixPatchFile = File(hotFixDir, "patch.dex")
        HotFixUtils.installPatch(this, hotFixPatchFile)
    }
}