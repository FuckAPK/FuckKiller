package org.baiyu.fuckkiller

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XSharedPreferences
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import kotlin.math.max

class MainHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        if (lpparam.packageName != "android") {
            return
        }

        val activityManagerConstants = XposedHelpers.findClass(
            "com.android.server.am.ActivityManagerConstants",
            lpparam.classLoader
        )

        XposedHelpers.setStaticLongField(
            activityManagerConstants,
            "DEFAULT_MAX_EMPTY_TIME_MILLIS",
            MAX_EMPTY_TIME_MILLIS
        )

        XposedHelpers.setStaticIntField(
            activityManagerConstants,
            "DEFAULT_MAX_CACHED_PROCESSES",
            maxCachedProcesses
        )
        XposedBridge.hookAllConstructors(activityManagerConstants, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                param.thisObject.apply {
                    XposedHelpers.setIntField(
                        this,
                        "mCustomizedMaxCachedProcesses",
                        maxCachedProcesses
                    )
                    XposedHelpers.setIntField(this, "MAX_CACHED_PROCESSES", maxCachedProcesses)
                    XposedHelpers.setIntField(this, "CUR_MAX_CACHED_PROCESSES", maxCachedProcesses)
                    XposedHelpers.setIntField(
                        this,
                        "CUR_MAX_EMPTY_PROCESSES",
                        maxCachedProcesses / 2
                    )
                    XposedHelpers.setLongField(this, "mMaxEmptyTimeMillis", MAX_EMPTY_TIME_MILLIS)
                }
            }
        })

        XposedHelpers.setStaticIntField(
            activityManagerConstants,
            "DEFAULT_MAX_PHANTOM_PROCESSES",
            maxPhantomProcesses
        )
        XposedBridge.hookAllConstructors(activityManagerConstants, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                XposedHelpers.setIntField(
                    param.thisObject,
                    "MAX_PHANTOM_PROCESSES",
                    maxPhantomProcesses
                )
            }
        })

        if (settings.isRecentTasksHookEnabled) {
            val recentTasks =
                XposedHelpers.findClass("com.android.server.wm.RecentTasks", lpparam.classLoader)
            XposedHelpers.findAndHookMethod(
                recentTasks,
                "isInVisibleRange",
                XposedHelpers.findClass("com.android.server.wm.Task", lpparam.classLoader),
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.args[2] = 0
                    }
                })
        }
    }

    companion object {
        private val prefs: XSharedPreferences by lazy {
            XSharedPreferences(BuildConfig.APPLICATION_ID)
        }
        private val settings: Settings by lazy {
            Settings.getInstance(prefs)
        }
        private val maxCachedProcesses: Int by lazy {
            max(settings.maxCachedProcesses, 32)
        }
        private val maxPhantomProcesses: Int by lazy {
            max(settings.maxPhantomProcesses, 32)
        }
        private const val MAX_EMPTY_TIME_MILLIS = 1000L * 60L * 60L * 1000L
    }
}
