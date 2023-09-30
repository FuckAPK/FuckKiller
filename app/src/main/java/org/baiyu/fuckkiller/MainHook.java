package org.baiyu.fuckkiller;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    final XSharedPreferences prefs = new XSharedPreferences(BuildConfig.APPLICATION_ID);
    final String PREF_MAX_CACHED_PROCESSES = "MAX_CACHED_PROCESSES";
    final String PREF_MAX_PHANTOM_PROCESSES = "MAX_PHANTOM_PROCESSES";
    final int MAX_CACHED_PROCESSES = Integer.parseInt(prefs.getString(PREF_MAX_CACHED_PROCESSES, "1024"));
    final int MAX_PHANTOM_PROCESSES = Integer.parseInt(prefs.getString(PREF_MAX_PHANTOM_PROCESSES, "1024"));

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("android")) {
            return;
        }

        Class<?> activityManagerConstants = XposedHelpers.findClass("com.android.server.am.ActivityManagerConstants", lpparam.classLoader);
        XposedHelpers.setStaticIntField(activityManagerConstants, "DEFAULT_MAX_CACHED_PROCESSES", MAX_CACHED_PROCESSES);
        XposedHelpers.setStaticIntField(activityManagerConstants, "DEFAULT_MAX_PHANTOM_PROCESSES", MAX_PHANTOM_PROCESSES);
        XposedBridge.hookAllConstructors(activityManagerConstants, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                super.afterHookedMethod(param);

                XposedHelpers.setIntField(param.thisObject, "mCustomizedMaxCachedProcesses", MAX_CACHED_PROCESSES);
                XposedHelpers.setIntField(param.thisObject, "MAX_CACHED_PROCESSES", MAX_CACHED_PROCESSES);
                XposedHelpers.setIntField(param.thisObject, "CUR_MAX_CACHED_PROCESSES", MAX_CACHED_PROCESSES);
                XposedHelpers.setIntField(param.thisObject, "CUR_MAX_EMPTY_PROCESSES", MAX_CACHED_PROCESSES / 2);

                XposedHelpers.setIntField(param.thisObject, "MAX_PHANTOM_PROCESSES", MAX_PHANTOM_PROCESSES);
            }
        });

        Class<?> recentTasks = XposedHelpers.findClass("com.android.server.wm.RecentTasks", lpparam.classLoader);
        XposedHelpers.findAndHookMethod(
                recentTasks,
                "isInVisibleRange",
                XposedHelpers.findClass("com.android.server.wm.Task", lpparam.classLoader),
                int.class,
                int.class,
                boolean.class,
                new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                        super.beforeHookedMethod(param);
                        param.args[2] = 0;
                    }
                });
    }
}
