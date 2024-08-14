package org.lyaaz.fuckkiller

import android.os.Build
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

        runCatching {
            XposedHelpers.findClass(
                "com.android.server.am.ActivityManagerConstants",
                lpparam.classLoader
            )
        }.onFailure {
            XposedBridge.log(it)
        }.getOrNull()?.let {
            hookActivityManagerConstant(it)
        }

        if (settings.isRecentTasksHookEnabled) {
            runCatching {
                XposedHelpers.findClass("com.android.server.wm.RecentTasks", lpparam.classLoader)
            }.onFailure {
                XposedBridge.log(it)
            }.getOrNull()?.let {
                hookRecentTask(it)
            }
        }
    }

    private fun hookActivityManagerConstant(activityManagerConstants: Class<*>) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.TIRAMISU) {
            runCatching {
                XposedHelpers.setStaticLongField(
                    activityManagerConstants,
                    "DEFAULT_MAX_EMPTY_TIME_MILLIS",
                    MAX_EMPTY_TIME_MILLIS
                )
                XposedBridge.hookAllConstructors(
                    activityManagerConstants,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            runCatching {
                                XposedHelpers.setLongField(
                                    param.thisObject, "mMaxEmptyTimeMillis", MAX_EMPTY_TIME_MILLIS
                                )
                            }
                        }
                    }
                )
            }.onSuccess {
                XposedBridge.log("set DEFAULT_MAX_EMPTY_TIME_MILLIS: $MAX_EMPTY_TIME_MILLIS")
            }.onFailure {
                XposedBridge.log(it)
            }
        }

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.TIRAMISU) {
            runCatching {
                XposedHelpers.setStaticIntField(
                    activityManagerConstants, "DEFAULT_MAX_CACHED_PROCESSES", maxCachedProcesses
                )
                XposedBridge.hookAllConstructors(
                    activityManagerConstants,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            runCatching {
                                XposedHelpers.setIntField(
                                    param.thisObject, "MAX_CACHED_PROCESSES", maxCachedProcesses
                                )
                            }
                        }
                    }
                )
            }.onSuccess {
                XposedBridge.log("set DEFAULT_MAX_CACHED_PROCESSES: $maxCachedProcesses")
            }.onFailure {
                XposedBridge.log(it)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            runCatching {
                XposedHelpers.setStaticIntField(
                    activityManagerConstants, "DEFAULT_MAX_PHANTOM_PROCESSES", maxPhantomProcesses
                )
                XposedBridge.hookAllConstructors(
                    activityManagerConstants,
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            runCatching {
                                XposedHelpers.setIntField(
                                    param.thisObject, "MAX_PHANTOM_PROCESSES", maxPhantomProcesses
                                )
                            }
                        }
                    }
                )
            }.onSuccess {
                XposedBridge.log("set DEFAULT_MAX_PHANTOM_PROCESSES: $maxPhantomProcesses")
            }.onFailure {
                XposedBridge.log(it)
            }
        }

        XposedBridge.hookAllConstructors(activityManagerConstants, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                mapOf(
                    "CUR_MAX_CACHED_PROCESSES" to maxCachedProcesses,
                    "CUR_MAX_EMPTY_PROCESSES" to maxCachedProcesses / 2,
                    "CUR_TRIM_EMPTY_PROCESSES" to maxCachedProcesses / 4,
                    "CUR_TRIM_CACHED_PROCESSES" to maxCachedProcesses / 6
                ).forEach { (name, value) ->
                    runCatching {
                        XposedHelpers.setIntField(param.thisObject, name, value)
                    }.onSuccess {
                        XposedBridge.log("Set $name = $value")
                    }.onFailure {
                        XposedBridge.log(it)
                    }
                }
            }
        })
    }

    private fun hookRecentTask(recentTasks: Class<*>) {
        runCatching {
            XposedHelpers.findAndHookMethod(
                recentTasks,
                "isInVisibleRange",
                "com.android.server.wm.Task",
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                Boolean::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        param.args[2] = 0
                    }
                }
            )
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
