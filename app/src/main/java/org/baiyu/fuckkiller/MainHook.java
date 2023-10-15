package org.baiyu.fuckkiller;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final Settings settings = Settings.getInstance();
    private static final int maxCachedProcesses = Math.max(settings.getMaxCachedProcesses(), settings.getMinProcesses());
    private static final int maxPhantomProcesses = Math.max(settings.getMaxPhantomProcesses(), settings.getMinProcesses());
    private static final long maxEmptyTimeMillis = 1000L * 60L * 60L * 1000L;

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("android")) {
            return;
        }

        Class<?> activityManagerConstants = XposedHelpers.findClass("com.android.server.am.ActivityManagerConstants", lpparam.classLoader);

        XposedHelpers.setStaticLongField(activityManagerConstants, "DEFAULT_MAX_EMPTY_TIME_MILLIS", maxEmptyTimeMillis);

        XposedHelpers.setStaticIntField(activityManagerConstants, "DEFAULT_MAX_CACHED_PROCESSES", maxCachedProcesses);
        XposedBridge.hookAllConstructors(activityManagerConstants, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                XposedHelpers.setIntField(param.thisObject, "mCustomizedMaxCachedProcesses", maxCachedProcesses);
                XposedHelpers.setIntField(param.thisObject, "MAX_CACHED_PROCESSES", maxCachedProcesses);
                XposedHelpers.setIntField(param.thisObject, "CUR_MAX_CACHED_PROCESSES", maxCachedProcesses);
                XposedHelpers.setIntField(param.thisObject, "CUR_MAX_EMPTY_PROCESSES", maxCachedProcesses / 2);
                XposedHelpers.setLongField(param.thisObject, "mMaxEmptyTimeMillis", maxEmptyTimeMillis);
            }
        });

        XposedHelpers.setStaticIntField(activityManagerConstants, "DEFAULT_MAX_PHANTOM_PROCESSES", maxPhantomProcesses);
        XposedBridge.hookAllConstructors(activityManagerConstants, new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                XposedHelpers.setIntField(param.thisObject, "MAX_PHANTOM_PROCESSES", maxPhantomProcesses);
            }
        });

        if (settings.isRecentTasksHookEnabled()) {
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
                        protected void beforeHookedMethod(MethodHookParam param) {
                            param.args[2] = 0;
                        }
                    });
        }
    }
}
