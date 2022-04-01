package com.wuliqinwang.android

import android.app.Application
import android.content.Context
import com.qiyi.lens.Lens
import com.qiyi.lens.LensUtil
import com.qiyi.lens.utils.UIUtils
import com.qiyi.lens.utils.configs.NetworkAnalyzeConfig
import com.tencent.matrix.Matrix
import com.tencent.matrix.plugin.DefaultPluginListener
import com.tencent.matrix.report.Issue
import com.tencent.matrix.trace.TracePlugin
import com.tencent.matrix.trace.config.TraceConfig
import com.utopia.android.ulog.ULog
import com.utopia.android.ulog.config.UConfig
import java.io.File


// DES: 应用程序入口
class TrainingApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        val filePath = File(externalCacheDir?.absolutePath, "log").path
        val config = UConfig.Builder()
            .setCacheLogDir(filePath)
            .setWriteFile(false)
            .build()
        ULog.init(this, config)
        val tracePlugin = TracePlugin(TraceConfig.Builder()
            .enableAnrTrace(true)
            .enableFPS(true)
            .enableStartup(true)
            .enableHistoryMsgRecorder(true)
            .enableAppMethodBeat(true)
            .enableIdleHandlerTrace(true)
            .enableSignalAnrTrace(true)
            .enableTouchEventTrace(true)
            .splashActivities("com.wuliqinwang.android.SplashActivity")
            .anrTracePath(filePath)
            .build())
        val matrixBuilder = Matrix.Builder(this)
            .plugin(tracePlugin)
            .pluginListener(PluginListener(this))
            .build()
        Matrix.init(matrixBuilder)

        Matrix.with().startAllPlugins()
        ULog.d("TrainingApplication", "thread: ${Thread.currentThread()}")
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        base ?: return
        // 热修复配置
//        val hotFixDir = base.getDir("patch", Context.MODE_PRIVATE)
//        val hotFixPatchFile = File(hotFixDir, "patch.dex")
//        HotFixUtils.installPatch(this, hotFixPatchFile)
    }

    class PluginListener(context: Context): DefaultPluginListener(context) {
        override fun onReportIssue(issue: Issue?) {
            ULog.d("TrainingApplication", issue.toString())
        }
    }
}