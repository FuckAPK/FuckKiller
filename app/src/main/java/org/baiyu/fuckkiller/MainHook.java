package org.baiyu.fuckkiller;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class MainHook implements IXposedHookLoadPackage {

    private static final Settings settings = Settings.getInstance();

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals("android")) {
            return;
        }

        Class<?> activityManagerConstants = XposedHelpers.findClass("com.android.server.am.ActivityManagerConstants", lpparam.classLoader);

        if (settings.getMaxCachedProcesses() >= settings.getMinProcesses()) {
            XposedHelpers.setStaticIntField(activityManagerConstants, "DEFAULT_MAX_CACHED_PROCESSES", settings.getMaxCachedProcesses());
            XposedBridge.hookAllConstructors(activityManagerConstants, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    XposedHelpers.setIntField(param.thisObject, "mCustomizedMaxCachedProcesses", settings.getMaxCachedProcesses());
                    XposedHelpers.setIntField(param.thisObject, "MAX_CACHED_PROCESSES", settings.getMaxCachedProcesses());
                    XposedHelpers.setIntField(param.thisObject, "CUR_MAX_CACHED_PROCESSES", settings.getMaxCachedProcesses());
                    XposedHelpers.setIntField(param.thisObject, "CUR_MAX_EMPTY_PROCESSES", settings.getMaxCachedProcesses() / 2);
                }
            });
        }

        if (settings.getMaxPhantomProcesses() > settings.getMinProcesses()) {
            XposedHelpers.setStaticIntField(activityManagerConstants, "DEFAULT_MAX_PHANTOM_PROCESSES", settings.getMaxPhantomProcesses());
            XposedBridge.hookAllConstructors(activityManagerConstants, new XC_MethodHook() {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    super.afterHookedMethod(param);
                    XposedHelpers.setIntField(param.thisObject, "MAX_PHANTOM_PROCESSES", settings.getMaxPhantomProcesses());
                }
            });
        }

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
                        protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
                            super.beforeHookedMethod(param);
                            param.args[2] = 0;
                        }
                    });
        }
    }
}
